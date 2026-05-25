package com.alcadrial.renderengine.render.resources.filesystem;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.file.ClosedFileSystemException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Sets;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.alcadrial.renderengine.render.resources.filesystem.MergedWatchService.Key;

public class MergedFileSystem extends FileSystem {
	
	static record Source(Path path, int priority) implements Comparable<Source> {
		
		@Override
		public int compareTo(Source o)
		{
			return Integer.compare(priority, o.priority);
		}
	}
	
	static record FileTree(Set<MergedPath> paths, ConcurrentSkipListMap<MergedPath, Source> sourceMap) {}
	
	private ConcurrentSkipListMap<Source, Boolean> sources;
	private MergedFileSystemProvider provider;
	private MergedPath root;
	private Path absoluteRootPath;
	private CopyOnWriteArrayList<MergedWatchService> watchServices;
	//files may be inconsistent with the file systems, only used for the WatchServices, lazy initialized
	private AtomicReference<@Nullable FileTree> filesReference;
	private Object watchServicesRemovalLock;
	private ReadWriteLock closeLock;
	private Lock readCloseLock;
	private Lock writeCloseLock;
	private volatile boolean isClosed;
	
	MergedFileSystem(MutableList<Path> sources, MergedFileSystemProvider provider)
	{
		this.sources = new ConcurrentSkipListMap<>((s0, s1) -> -s0.compareTo(s1));
		int i = sources.size();
		for (Path path : sources)
		{
			Source s = new Source(path, i--);
			this.sources.put(s, true);
		}
		this.provider = provider;
		watchServices = new CopyOnWriteArrayList<>();
		root = new MergedPath(this, "");
		absoluteRootPath = root.path.toAbsolutePath();
		
		filesReference = new AtomicReference<>();
		
		watchServicesRemovalLock = new Object();
		closeLock = new ReentrantReadWriteLock();
		readCloseLock = closeLock.readLock();
		writeCloseLock = closeLock.writeLock();
	}

	public @Nullable Source getSource(Path sourcePath)
	{
		for (Source source : sources.keySet()) if (source.path.equals(sourcePath)) return source;
		return null;
	}
	
	public void addSourcePath(Path sourcePath) throws IOException
	{
		addLastSourcePath(sourcePath);
	}
	
	public void addFirstSourcePath(Path sourcePath) throws IOException
	{
		addSourcePath(sourcePath, 0);
	}
	
	public void addLastSourcePath(Path sourcePath) throws IOException
	{
		addSourcePath(sourcePath, sources.size());
	}
	
	public void addSourcePath(Path sourcePath, int priority) throws IOException
	{
		readCloseLock.lock();
		try
		{
			if (isClosed) throw new ClosedFileSystemException();
			for (Source source : getSources()) if (source.path.equals(sourcePath)) return;
			Source source = new Source(sourcePath, priority);
			ListIterator<MergedWatchService> i = watchServices.listIterator();
			try
			{
				while (i.hasNext())
				{
					try
					{
						i.next().addSource(source);
					}
					catch (ClosedWatchServiceException _)
					{}
				}
			}
			catch (IOException e)
			{
				while (i.hasPrevious())
				{
					try
					{
						i.previous().removeSource(source);
					}
					catch (IOException | ClosedWatchServiceException _)
					{}
				}
				throw e;
			}
			sources.put(source, false);
			FileTree files = filesReference.get();
			addRealFilesFromSource(source, files);
			sources.put(source, true);
		}
		finally
		{
			readCloseLock.unlock();
		}
	}
	
	public void removeSourcePath(Path source)
	{
		readCloseLock.lock();
		try
		{
			if (isClosed) throw new ClosedFileSystemException();
			for (Iterator<Entry<Source, Boolean>> i = sources.entrySet().iterator(); i.hasNext();)
			{
				Entry<Source, Boolean> e = i.next();
				Source s = e.getKey();
				boolean removable = e.getValue();
				if (s.path.equals(source) && removable)
				{
					i.remove();
					removeRealFilesFromSource(s, filesReference.get());
					for (MergedWatchService watchService : watchServices)
					{
						try
						{
							watchService.removeSource(s);
						}
						catch (IOException | ClosedWatchServiceException _)
						{}
					}
					return;
				}
			}
		}
		finally
		{
			readCloseLock.unlock();
		}
	}
	
	//weakly consistent
	private boolean addRealFilesFromSource(Source source, @Nullable FileTree files)
	{
		if (files != null)
		{
			return addRealFilesFromSource(source, source.path, files.sourceMap);
		}
		return false;
	}
	
	private boolean addRealFilesFromSource(Source source, Path path, ConcurrentSkipListMap<MergedPath, Source> files)
	{
		return addRealFilesFromSource(source, path, files, true, true);
	}
	
	private void addRealFilesFromSource(Source source, Path path, ConcurrentSkipListMap<MergedPath, Source> files, Set<MergedPath> ignoreFiles)
	{
		Iterator<MergedPath> iterator = ignoreFiles.iterator();
		if (!iterator.hasNext())
		{
			addRealFilesFromSource(source, path, files, false, false);
			return;
		}
		addRealFilesFromSource(source, path, files, iterator, iterator.next());
	}
	
	private void addRealFilesFromSource(Source source, Path path, ConcurrentSkipListMap<MergedPath, Source> files, Iterator<MergedPath> nextIgnore, @Nullable MergedPath currentIgnore)
	{
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path))
		{
			for (Path file : stream)
			{
				if (currentIgnore != null)
				{
					int c;
					while ((c = currentIgnore.compareTo(file)) < 0 && nextIgnore.hasNext()) currentIgnore = nextIgnore.next();
					if (c == 0)
					{
						if (nextIgnore.hasNext()) currentIgnore = nextIgnore.next();
						else currentIgnore = null;
						continue;
					}
					else if (c < 0)
					{
						currentIgnore = null;
					}
				}
				if (Files.isReadable(file))
				{
					files.put(new MergedPath(this, source, file), source);
				}
			}
		}
		catch (IOException _)
		{}
	}

	private boolean addRealFilesFromSource(Source source, Path path, ConcurrentSkipListMap<MergedPath, Source> files, boolean signalEvents, boolean recursive)
	{
		boolean modified = false;
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path))
		{
			for (Path file : stream)
			{
				if (Files.isReadable(file))
				{
					MergedPath mergedPath = new MergedPath(this, source, file);
					Source oldSource = files.put(mergedPath, source);
					if (signalEvents) signalWatchService(oldSource == null ? StandardWatchEventKinds.ENTRY_CREATE : StandardWatchEventKinds.ENTRY_MODIFY, mergedPath);
					modified = true;
					if (recursive && Files.isDirectory(file, LinkOption.NOFOLLOW_LINKS))
					{
						addRealFilesFromSource(source, file, files, signalEvents, recursive);
					}
				}
			}
		}
		catch (IOException _)
		{}
		return modified;
	}
	
	//weakly consistent
	private void removeRealFilesFromSource(Source source, @Nullable FileTree fileTree)
	{
		if (fileTree != null)
		{
			ConcurrentSkipListMap<MergedPath, Source> files = fileTree.sourceMap;
			for (Iterator<Entry<MergedPath, Source>> i = files.entrySet().iterator(); i.hasNext();)
			{
				Entry<MergedPath, Source> e = i.next();
				if (e.getValue().equals(source))
				{
					MergedPath path = e.getKey();
					Source newSource = findRealHighestTailSource(source, path);
					if (newSource == null)
					{
						i.remove();
						signalWatchService(StandardWatchEventKinds.ENTRY_DELETE, path);
					}
					else
					{
						files.put(path, newSource);
						signalWatchService(StandardWatchEventKinds.ENTRY_MODIFY, path);
					}
				}
			}
		}
	}

	void buildFiles(MergedPath path)
	{
		//already synchronized in MergedWatchService#register
		ConcurrentSkipListMap<MergedPath, Source> files = new ConcurrentSkipListMap<>();
		boolean[] updated = { false };
		FileTree fileTree = filesReference.getAndUpdate(t -> {
			if (t == null)
			{
				updated[0] = true;
				return new FileTree(Set.of(path), files);
			}
			for (Path root : t.paths) if (path.startsWith(root))
			{
				updated[0] = false;
				return t;
			}
			updated[0] = true;
			return new FileTree(Sets.union(t.paths, Set.of(path)), files);
		});
		if (updated[0])
		{
			for (Source source : getSources()) addRealFilesFromSource(source, path.toSourcePath(source), files, fileTree == null ? Set.of() : fileTree.paths);
		}
	}
	
	@Nullable
	Source findRealHighestTailSource(Source source, MergedPath path)
	{
		for (Source s : sources.tailMap(source).keySet())
		{
			if (Files.exists(path.toSourcePath(s, LinkOption.NOFOLLOW_LINKS), LinkOption.NOFOLLOW_LINKS))
			{
				return s;
			}
		}
		return null;
	}

	@Nullable
	Source findRealHighestSource(MergedPath path, LinkOption...options)
	{
		for (Source s : sources.keySet())
		{
			if (Files.exists(path.toSourcePath(s, options), options))
			{
				return s;
			}
		}
		return null;
	}
	
	@Nullable
	Source findRealHighestWritableSource()
	{
		for (Source s : sources.keySet()) if (!s.path.getFileSystem().isReadOnly()) return s;
		return null;
	}
	
	@Nullable
	Source findRealNextWritableSource(Source source)
	{
		for (Source s : sources.tailMap(source).keySet()) if (!s.path.getFileSystem().isReadOnly()) return s;
		return null;
	}
	
	@Nullable
	Source findHighestHeadSource(Source source, MergedPath path, ConcurrentSkipListMap<MergedPath, Source> files)
	{
		Source s = files.get(path);
		if (s == null || s.compareTo(source) <= 0) return null;
		return s;
	}
	
	@Nullable
	Source findHighestSource(MergedPath path, ConcurrentSkipListMap<MergedPath, Source> files)
	{
		return files.get(path);
	}
	
	MergedWatchService.@Nullable Key handleKey(Source source, WatchKey serviceKey, Key key)
	{
		FileTree fileTree = filesReference.getAcquire();
		//closed all WatchServices
		if (fileTree == null) return null;
		ConcurrentSkipListMap<MergedPath, Source> files = fileTree.sourceMap;

		boolean visibleEventFound = false;
		for (WatchEvent<?> event : serviceKey.pollEvents())
		{
			if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE)
			{
				Path path = (Path) event.context();
				if (path == null) continue;
				MergedPath merged = new MergedPath(this, source, ((Path) serviceKey.watchable()).resolve(path));
				Source highestSource = findHighestSource(merged, files);
				if (highestSource == null)
				{
					key.addEvent(new MergedWatchService.Event<>(StandardWatchEventKinds.ENTRY_CREATE, path));
					files.put(merged, source);
					visibleEventFound = true;
				}
				else if (highestSource.compareTo(source) < 0)
				{
					key.addEvent(new MergedWatchService.Event<>(StandardWatchEventKinds.ENTRY_MODIFY, path));
					files.put(merged, source);
					visibleEventFound = true;
				}
				else
				{
					//an higher source exists or a non deleted file is created, ignore
				}
			}
			else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE)
			{
				Path path = (Path) event.context();
				if (path == null) continue;
				MergedPath merged = new MergedPath(this, source, ((Path) serviceKey.watchable()).resolve(path));
				Source highestSource = findHighestSource(merged, files);
				if (highestSource == null)
				{
					//a deleted file is delete, ignore
				}
				else if (highestSource.compareTo(source) < 0)
				{
					files.remove(merged, source);
					//a deleted file was skipped, ignore
				}
				else if (highestSource.compareTo(source) == 0)
				{
					highestSource = findRealHighestTailSource(source, merged);
					if (highestSource == null)
					{
						key.addEvent(new MergedWatchService.Event<>(StandardWatchEventKinds.ENTRY_DELETE, path));
						files.remove(merged);
						visibleEventFound = true;
					}
					else
					{
						key.addEvent(new MergedWatchService.Event<>(StandardWatchEventKinds.ENTRY_MODIFY, path));
						files.put(merged, highestSource);
						visibleEventFound = true;
					}
				}
				else
				{
					//an higher source exists, ignore
				}
			}
			else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY)
			{
				Path path = (Path) event.context();
				if (path == null) continue;
				MergedPath merged = new MergedPath(this, source, ((Path) serviceKey.watchable()).resolve(path));
				Source higherSource = findHighestSource(merged, files);
				if (higherSource == null)
				{
					key.addEvent(new MergedWatchService.Event<>(StandardWatchEventKinds.ENTRY_CREATE, path));
					files.put(merged, source);
					visibleEventFound = true;
				}
				else if (higherSource.compareTo(source) < 0)
				{
					key.addEvent(new MergedWatchService.Event<>(StandardWatchEventKinds.ENTRY_MODIFY, path));
					files.put(merged, source);
					visibleEventFound = true;
				}
				else if (higherSource.compareTo(source) == 0)
				{
					key.addEvent(new MergedWatchService.Event<>(StandardWatchEventKinds.ENTRY_MODIFY, path));
					files.put(merged, source);
					visibleEventFound = true;
				}
				else
				{
					//an higher source exists, ignore
				}
			}
			else if (event.kind() == StandardWatchEventKinds.OVERFLOW)
			{
				boolean modified = addRealFilesFromSource(source, fileTree);
				if (modified)
				{
					visibleEventFound = true;
				}
			}
			serviceKey.reset();
		}
		if (visibleEventFound) return key;
		
		return null;
	}
	
	NavigableSet<Source> getSources()
	{
		return sources.keySet();
	}
	
	@Override
	public FileSystemProvider provider()
	{
		if (isClosed) throw new ClosedFileSystemException();
		return provider;
	}
	
	@Override
	public boolean isOpen()
	{
		if (isClosed) throw new ClosedFileSystemException();
		return true;
	}
	
	@Override
	public boolean isReadOnly()
	{
		if (isClosed) throw new ClosedFileSystemException();
		return false;
	}
	
	@Override
	public String getSeparator()
	{
		if (isClosed) throw new ClosedFileSystemException();
		return "/";
	}
	
	@Override
	public Iterable<Path> getRootDirectories()
	{
		if (isClosed) throw new ClosedFileSystemException();
		return List.of(root);
	}
	
	@Override
	public Iterable<FileStore> getFileStores()
	{
		if (isClosed) throw new ClosedFileSystemException();
		return () -> new Iterator<FileStore>() {
			
			private Iterator<Source> sourcesIterator = getSources().iterator();
			private @Nullable Iterator<FileStore> sourceFileStoreIterator;
			
			@Override
			public boolean hasNext()
			{
				if (sourceFileStoreIterator == null)
				{
					if (!sourcesIterator.hasNext()) return false;
					sourceFileStoreIterator = sourcesIterator.next().path.getFileSystem().getFileStores().iterator();
				}
				return ((@NonNull Iterator<FileStore>) sourceFileStoreIterator).hasNext();
			}
			
			@Override
			public FileStore next()
			{
				if (sourceFileStoreIterator == null)
				{
					sourceFileStoreIterator = sourcesIterator.next().path.getFileSystem().getFileStores().iterator();
				}
				return ((@NonNull Iterator<FileStore>) sourceFileStoreIterator).next();
			}
		};
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Set<String> supportedFileAttributeViews()
	{
		readCloseLock.lock();
		try
		{
			if (isClosed) throw new ClosedFileSystemException();
			return Sets.intersectAll((Set<String>[]) getSources().stream().map(s -> s.path.getFileSystem().supportedFileAttributeViews()).toArray());
		}
		finally
		{
			readCloseLock.unlock();
		}
	}
	
	@Override
	public MergedPath getPath(String first, String... more)
	{
		if (isClosed) throw new ClosedFileSystemException();
		return new MergedPath(this, first, more);
	}
	
	@Override
	public PathMatcher getPathMatcher(String syntaxAndPattern)
	{
		readCloseLock.lock();
		try
		{
			if (isClosed) throw new ClosedFileSystemException();
			return p -> {
				for (Source source : getSources()) if (source.path.getFileSystem().getPathMatcher(syntaxAndPattern).matches(p)) return true;
				return false;
			};
		}
		finally
		{
			readCloseLock.unlock();
		}
	}
	
	@Override
	public UserPrincipalLookupService getUserPrincipalLookupService()
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public WatchService newWatchService() throws IOException
	{
		readCloseLock.lock();
		try
		{
			if (isClosed) throw new ClosedFileSystemException();
			MergedWatchService watchService = new MergedWatchService(this);
			synchronized (watchServicesRemovalLock)//atomic
			{
				watchServices.add(watchService);
			}
			return watchService;
		}
		finally
		{
			readCloseLock.unlock();
		}
	}
	
	void removeWatchService(MergedWatchService watchService)
	{
		//already writeCloseLock.lock() or watchService.closeLock.lock(), no concurrency with close()
		watchServices.remove(watchService);
		FileTree fileTree = null;
		synchronized (watchServicesRemovalLock)//atomic
		{
			if (watchServices.isEmpty())
			{
				fileTree = filesReference.getAndSet(null);
			}
		}
		if (fileTree != null) fileTree.sourceMap.clear();
	}
	
	private void signalWatchService(Kind<Path> kind, MergedPath path)
	{
		for (MergedWatchService watchService : watchServices)
		{
			watchService.signal(kind, path);
		}
	}
	
	public MergedPath getRoot()
	{
		if (isClosed) throw new ClosedFileSystemException();
		return root;
	}
	
	public Path getAbsoluteRootPath()
	{
		if (isClosed) throw new ClosedFileSystemException();
		return absoluteRootPath;
	}
	
	public @Nullable FileTree getFileTree()
	{
		return filesReference.get();
	}
	
	@Override
	public void close() throws IOException
	{
		if ((boolean) IS_CLOSED.getAndSetAcquire(this, true)) throw new ClosedFileSystemException();
		writeCloseLock = closeLock.writeLock();
		writeCloseLock.lock();
		try
		{
			for (MergedWatchService watchService : watchServices) watchService.close();
			FileTree fileTree = filesReference.getAndSet(null);
			if (fileTree != null) fileTree.sourceMap.clear();
			sources.clear();
		}
		finally
		{
			writeCloseLock.unlock();
		}
	}
	
	private static final VarHandle IS_CLOSED;
	
	static
	{
		try
		{
			IS_CLOSED = MethodHandles.lookup().findVarHandle(MergedFileSystem.class, "isClosed", Boolean.TYPE);
		}
		catch (ReflectiveOperationException e)
		{
			throw new ExceptionInInitializerError(e);
		}
	}
}
