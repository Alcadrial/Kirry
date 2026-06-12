package com.alcadrial.kirry.render.resources.filesystem;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Iterator;

import org.jspecify.annotations.Nullable;

import com.alcadrial.kirry.render.resources.filesystem.MergedFileSystem.Source;

public class MergedPath implements Path {
	
	private final MergedFileSystem fileSystem;
	final Path path;
	
	public MergedPath(MergedFileSystem fileSystem, String path, String... more)
	{
		this(fileSystem, Path.of(path, more));
	}
	
	MergedPath(MergedFileSystem fileSystem, Path path)
	{
		this.fileSystem = fileSystem;
		this.path = path;
	}
	
	MergedPath(MergedFileSystem fileSystem, Source source, Path path)
	{
		this.fileSystem = fileSystem;
		this.path = source.path().relativize(path);
	}
	
	@Override
	public MergedFileSystem getFileSystem()
	{
		return fileSystem;
	}
	
	@Override
	public boolean isAbsolute()
	{
		return path.isAbsolute();
	}
	
	@Override
	public @Nullable MergedPath getRoot()
	{
		return isAbsolute() ? fileSystem.getRoot() : null;
	}
	
	@Override
	public @Nullable MergedPath getFileName()
	{
		return new MergedPath(fileSystem, path.getFileName());
	}
	
	@Override
	public @Nullable MergedPath getParent()
	{
		return new MergedPath(fileSystem, path.getParent());
	}
	
	@Override
	public int getNameCount()
	{
		return path.getNameCount();
	}
	
	@Override
	public MergedPath getName(int index)
	{
		return new MergedPath(fileSystem, path.getName(index));
	}
	
	@Override
	public MergedPath subpath(int beginIndex, int endIndex)
	{
		return new MergedPath(fileSystem, path.subpath(beginIndex, endIndex));
	}
	
	@Override
	public boolean startsWith(Path other)
	{
		if (other instanceof MergedPath p) return path.startsWith(p.path);
		return false;
	}
	
	@Override
	public boolean endsWith(Path other)
	{
		if (other instanceof MergedPath p) return path.endsWith(p.path);
		return false;
	}
	
	@Override
	public MergedPath normalize()
	{
		return new MergedPath(fileSystem, path.normalize());
	}
	
	@Override
	public Path resolve(String other)
	{
		return new MergedPath(fileSystem, path.resolve(other));
	}
	
	@Override
	public MergedPath resolve(Path other)
	{
		return new MergedPath(fileSystem, path.resolve(other));
	}
	
	@Override
	public Path resolve(@Nullable Path first, Path @Nullable... more)
	{
		return new MergedPath(fileSystem, path.resolve(first, more));
	}
	
	@Override
	public Path resolve(@Nullable String first, String @Nullable... more)
	{
		return new MergedPath(fileSystem, path.resolve(first, more));
	}
	
	@Override
	public Path resolveSibling(Path other)
	{
		return new MergedPath(fileSystem, path.resolveSibling(other));
	}
	
	@Override
	public Path resolveSibling(String other)
	{
		return new MergedPath(fileSystem, path.resolveSibling(other));
	}
	
	@Override
	public MergedPath relativize(Path other)
	{
		return new MergedPath(fileSystem, path.relativize(other));
	}
	
	public Path unsafeRelativize(Path other)
	{
		return path.relativize(other);
	}
	
	@Override
	public URI toUri()
	{
		return URI.create(fileSystem.provider().getScheme() + ":" + path.toString().replaceFirst(".*:", ""));
	}
	
	@Override
	public MergedPath toAbsolutePath()
	{
		if (isAbsolute()) return this;
		return new MergedPath(fileSystem, fileSystem.getAbsoluteRootPath().relativize(path.toAbsolutePath()));
	}
	
	@Override
	public Path toRealPath(LinkOption... options) throws IOException
	{
		Source source = fileSystem.findRealHighestSource(this, options);
		if (source == null) throw new FileNotFoundException(toString());
		return toSourcePath(source, options).toRealPath(options);
	}
	
	public Path toSourcePath(Source source, LinkOption... options)
	{
		return source.path().resolve(path.toString());
	}
	
	@Override
	public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException
	{
		if (watcher instanceof MergedWatchService mergedWatcher && mergedWatcher.getFileSystem().equals(fileSystem))
		{
			return mergedWatcher.register(this, events, modifiers);
		}
		throw new IllegalArgumentException("Incompatible watcher");
	}
	
	@Override
	public int compareTo(Path other)
	{
		if (other instanceof MergedPath p) return path.compareTo(p.path);
		return -1;
	}
	
	@Override
	public Iterator<Path> iterator()
	{
		return Path.super.iterator();
	}
	
	@Override
	public String toString()
	{
		return path.toString();
	}
}
