package com.alcadrial.kirry.render.resources.filesystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.ReadOnlyFileSystemException;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Sets;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.alcadrial.kirry.render.resources.filesystem.MergedCopyOptions.FromSourceCopyOption;
import com.alcadrial.kirry.render.resources.filesystem.MergedCopyOptions.ToSourceCopyOption;
import com.alcadrial.kirry.render.resources.filesystem.MergedFileSystem.Source;

// URI format: rem:<fileSystemId>
public class MergedFileSystemProvider extends FileSystemProvider {
	
	private Map<URI, FileSystem> fileSystems;
	
	public MergedFileSystemProvider()
	{
		fileSystems = new HashMap<>();
	}
	
	@Override
	public String getScheme()
	{
		return "rem";
	}
	
	@Override
	public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException
	{
		checkUri(uri);
		Object sourcesEnv = env.get("sources");
		FileSystem fileSystem = switch (sourcesEnv)
		{
			case Path source -> new MergedFileSystem(Lists.mutable.of(source), this);
			case Path[] source -> new MergedFileSystem(Lists.mutable.of(source), this);
			case File source -> new MergedFileSystem(Lists.mutable.of(source.toPath()), this);
			case File[] source -> new MergedFileSystem(Lists.mutable.of(source).collect(File::toPath), this);
			case String source -> new MergedFileSystem(Lists.mutable.of(Path.of(source)), this);
			case String[] source -> new MergedFileSystem(Lists.mutable.of(source).collect(Path::of), this);
			case URI source -> new MergedFileSystem(Lists.mutable.of(Path.of(source)), this);
			case URI[] source -> new MergedFileSystem(Lists.mutable.of(source).collect(Path::of), this);
			case @SuppressWarnings("rawtypes") Iterable it -> {
				MutableList<Path> sources = Lists.mutable.of();
				for (Object object : it)
				{
					switch (object)
					{
						case Path source -> sources.add(source);
						case File source -> sources.add(source.toPath());
						case String source -> sources.add(Path.of(source));
						case URI source -> sources.add(Path.of(source));
						default -> throw new IllegalArgumentException("Invalid path: " + object);
					}
				}
				yield new MergedFileSystem(sources, this);
			}
			case null -> new MergedFileSystem(Lists.mutable.of(), this);
			default -> throw new IllegalArgumentException("Invalid path: " + sourcesEnv);
		};
		fileSystems.put(uri, fileSystem);
		return fileSystem;
	}
	
	@Override
	public FileSystem getFileSystem(URI uri)
	{
		checkUri(uri);
		FileSystem fs = fileSystems.get(uri);
		if (fs == null) throw new FileSystemNotFoundException();
		return fs;
	}
	
	@Override
	public Path getPath(URI uri)
	{
		checkUri(uri);
		String raw = uri.getRawSchemeSpecificPart();
		int separatorIndex = raw.indexOf("!/");
		if (separatorIndex == -1) throw new IllegalArgumentException("Invalid URI format: " + uri);
		URI rootUri = URI.create(raw.substring(0, separatorIndex));
		return getFileSystem(rootUri).getPath(raw.substring(separatorIndex + 1));
	}
	
	@Override
	public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException
	{
		//TODO: support CREATE_NEW, CREATE, DELETE_ON_CLOSE
		if (!Sets.intersect(options, Sets.immutable.of(
			StandardOpenOption.CREATE_NEW,
			StandardOpenOption.CREATE,
			StandardOpenOption.DELETE_ON_CLOSE
		).castToSet()).isEmpty()) throw new UnsupportedOperationException("TODO"); 
		return forwardToHighestFile(path, sourcePath -> Files.newByteChannel(sourcePath, options, attrs));
	}
	
	@Override
	public FileChannel newFileChannel(Path path, Set<? extends OpenOption> options, @NonNull FileAttribute<?>... attrs) throws IOException
	{
		//TODO: support CREATE_NEW, CREATE, DELETE_ON_CLOSE
		if (!Sets.intersect(options, Sets.immutable.of(
			StandardOpenOption.CREATE_NEW,
			StandardOpenOption.CREATE,
			StandardOpenOption.DELETE_ON_CLOSE
		).castToSet()).isEmpty()) throw new UnsupportedOperationException("TODO"); 
		return forwardToHighestFile(path, sourcePath -> FileChannel.open(sourcePath, options, attrs));
	}
	
	@Override
	public DirectoryStream<Path> newDirectoryStream(Path dir, @Nullable Filter<? super Path> filter) throws IOException
	{
		return cast(new MergedDirectoryStream(checkPath(dir), filter));
	}
	
	@Override
	public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException
	{
		MergedPath merged = checkPath(dir);
		MergedFileSystem fileSystem = merged.getFileSystem();
		Source source = fileSystem.findRealHighestWritableSource();
		if (source == null) throw new ReadOnlyFileSystemException();
		while (true)
		{
			try
			{
				Files.createDirectory(merged.toSourcePath(source), attrs);
				return;
			}
			catch (FileAlreadyExistsException e)
			{
				throw e;
			}
			catch (UnsupportedOperationException | IOException e)
			{
				source = fileSystem.findRealNextWritableSource(source);
				if (source == null) throw e;
			}
		}
	}
	
	@Override
	public void delete(Path path) throws IOException
	{
		Files.delete(path);
		//TODO: try to delete this path from every source, if fails restore all paths before throwing, NOT ATOMIC
	}
	
	@Override
	public void copy(Path source, Path target, CopyOption... options) throws IOException
	{
		Files.copy(source, target, options);
		MergedPath mergedSource = checkPath(source);
		MergedPath mergedTarget = checkPath(target);
		MergedFileSystem sourceFileSystem = mergedSource.getFileSystem();
		MergedFileSystem targetFileSystem = mergedTarget.getFileSystem();
		
		int cs = 0;
		Source fromSource = null;
		Source toSource = null;
		for (CopyOption option : options)
		{
			switch (option)
			{
				case FromSourceCopyOption o -> fromSource = sourceFileSystem.getSource(o.getSourcePath());
				case ToSourceCopyOption o -> toSource = targetFileSystem.getSource(o.getSourcePath());
				default -> cs++;
			}
		}
		CopyOption[] remainingOptions = new CopyOption[cs];
		cs = 0;
		for (CopyOption option : options)
		{
			switch (option)
			{
				case FromSourceCopyOption _ -> {}
				case ToSourceCopyOption _ -> {}
				default -> remainingOptions[cs++] = option;
			}
		}
		
		if (sourceFileSystem != targetFileSystem)
		{
			throw new UnsupportedOperationException("Copy between different file systems");
		}
		
		if (fromSource != null)
		{
			if (toSource != null)
			{
				Files.copy(mergedSource.toSourcePath(fromSource), mergedTarget.toSourcePath(toSource), remainingOptions);
				return;
			}
			//TODO: copy from fromSource to the highest writable visible source, NOT ATOMIC
			throw new UnsupportedOperationException("TODO");
		}
		if (toSource != null)
		{
			//TODO: search highest source and copy the file to toSource, NOT ATOMIC
			throw new UnsupportedOperationException("TODO");
		}
		MergedFileSystem fileSystem = sourceFileSystem;
		
		//TODO: search highest source and copy the file to the highest writable visible source, NOT ATOMIC, check custom CopyOptions
		throw new UnsupportedOperationException("TODO");
	}
	
	@Override
	public void move(Path source, Path target, CopyOption... options) throws IOException
	{
		Files.move(source, target, options);
		//TODO: search highest source and move the file to the highest writable visible source, NOT ATOMIC, check custom CopyOptions
	}
	
	@Override
	public boolean isSameFile(Path path, Path path2) throws IOException
	{
		if (path.equals(path2)) return true;
		MergedPath merged = checkPath(path);
		MergedFileSystem fileSystem = merged.getFileSystem();
		Source source = fileSystem.findRealHighestSource(merged);
		if (source == null) return false;
		return Files.isSameFile(path2, merged.toSourcePath(source));
	}
	
	@Override
	public boolean isHidden(Path path) throws IOException
	{
		return forwardToHighestFile(path, Files::isHidden);
	}
	
	@Override
	public FileStore getFileStore(Path path) throws IOException
	{
		MergedPath merged = checkPath(path);
		MergedFileSystem fileSystem = merged.getFileSystem();
		for (Source s : fileSystem.getSources())
		{
			Path sourcePath = merged.toSourcePath(s);
			try
			{
				return Files.getFileStore(path);
			}
			catch (IOException e)
			{
				if (Files.exists(sourcePath)) throw e;
			}
		}
		throw new FileNotFoundException(path.toString());
	}
	
	@Override
	public void checkAccess(Path path, AccessMode... modes) throws IOException
	{
		MergedPath merged = checkPath(path);
		MergedFileSystem fileSystem = merged.getFileSystem();
		for (Source s : fileSystem.getSources())
		{
			Path sourcePath = merged.toSourcePath(s);
			FileSystemProvider sourceProvider = sourcePath.getFileSystem().provider();
			try
			{
				sourceProvider.checkAccess(sourcePath, modes);
			}
			catch (IOException e)
			{
				try
				{
					sourceProvider.checkAccess(sourcePath);
				}
				catch (IOException _)
				{
					continue;
				}
				throw e;
			}
		}
		throw new FileNotFoundException(path.toString());
	}
	
	@Override
	public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options)
	{
		//TODO: return wrapper based on type
		throw new UnsupportedOperationException("TODO");
	}
	
	@Override
	public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException
	{
		return forwardToHighestFile(path, sourcePath -> Files.readAttributes(sourcePath, type, options), options);
	}
	
	@Override
	public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException
	{
		//TODO: parse attributes string
		throw new UnsupportedOperationException("TODO");
	}
	
	@Override
	public void setAttribute(Path path, String attribute, @Nullable Object value, LinkOption... options) throws IOException
	{
		//TODO: parse attributes string
		throw new UnsupportedOperationException("TODO");
	}
	
	private void checkUri(URI uri)
	{
		if (!getScheme().equalsIgnoreCase(uri.getScheme())) throw new ProviderMismatchException();
	}
	
	private MergedPath checkPath(Path path)
	{
		if (path instanceof MergedPath merged) return merged;
		throw new ProviderMismatchException();
	}
	
	private interface ForwardAction<T> {
		
		T forward(Path path) throws IOException;
	}
	
	private <T> T forwardToHighestFile(Path path, ForwardAction<T> action, LinkOption... options) throws IOException
	{
		MergedPath merged = checkPath(path);
		MergedFileSystem fileSystem = merged.getFileSystem();
		Source source = fileSystem.findRealHighestSource(merged, options);
		if (source == null) throw new FileNotFoundException(path.toString());
		Path sourcePath = merged.toSourcePath(source, options);
		return action.forward(sourcePath);
	}
	
	@SuppressWarnings("unchecked")
	static <X, T> X cast(T t)
	{
		return (X) t;
	}
}
