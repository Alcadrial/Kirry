package com.alcadrial.kirry.render.resources.filesystem;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.alcadrial.kirry.render.resources.filesystem.MergedFileSystem.FileTree;
import com.alcadrial.kirry.render.resources.filesystem.MergedFileSystem.Source;

public class MergedDirectoryStream implements DirectoryStream<MergedPath> {
	
	private static final byte NEW = 0;
	private static final byte STARTED = 1;
	private static final byte CLOSED = 2;
	
	private final MergedPath directory;
	private final @Nullable Filter<? super Path> filter;
	private byte state;
	
	public MergedDirectoryStream(MergedPath directory, @Nullable Filter<? super Path> filter)
	{
		this.directory = directory;
		this.filter = filter;
		state = NEW;
	}
	
	@Override
	public Iterator<MergedPath> iterator()
	{
		if (state != NEW) throw new IllegalStateException("Iteration already started");
		state = STARTED;
		MergedFileSystem fileSystem = directory.getFileSystem();
		
		FileTree fileTree = fileSystem.getFileTree();
		
		if (fileTree != null && fileTree.paths().contains(directory))
		{
			return new ViewIterator(fileTree.sourceMap().keySet().tailSet(directory, false).iterator());
		}
		
		Iterator<Source> sources = fileSystem.getSources().iterator();
		if (!sources.hasNext()) return MergedFileSystemProvider.cast(List.of().iterator());
		try
		{
			return new InspectIterator(sources);
		}
		catch (IOException e)
		{
			throw new DirectoryIteratorException(e);
		}
	}
	
	@Override
	public void close()
	{
		state = CLOSED;
	}
	
	private class InspectIterator implements Iterator<MergedPath> {
		
		private MutableSet<Path> knownPaths;
		
		private Iterator<Source> sources;
		private @Nullable Source currentSource;
		private @Nullable Iterator<Path> paths;
		private @Nullable DirectoryStream<Path> stream;
		
		private @Nullable MergedPath next;
		
		public InspectIterator(Iterator<Source> sources) throws IOException
		{
			this.sources = sources;
			knownPaths = new UnifiedSet<>(128);
		}
		
		@Override
		public boolean hasNext()
		{
			if (next != null) return true;
			if (state == CLOSED) return false;
			try
			{
				while (paths == null && state != CLOSED) tryNextSource();
				if (state == CLOSED) return false;
				
				if (next != null) return true;
				
				while (!((@NonNull Iterator<Path>) paths).hasNext() && state != CLOSED) tryNextSource();
				if (state == CLOSED) return false;
				
				if (next == null) next = new MergedPath(directory.getFileSystem(), ((@NonNull Source) currentSource).path().relativize(((@NonNull Iterator<Path>) paths).next()));
			}
			catch (IOException e)
			{
				state = CLOSED;
				throw new DirectoryIteratorException(e);
			}
			return true;
		}
		
		private boolean tryNextSource() throws IOException
		{
			if (stream != null)
			{
				try
				{
					stream.close();
				}
				catch (IOException e)
				{}
			}
			
			if (!sources.hasNext())
			{
				state = CLOSED;
				return false;
			}
			Source source = sources.next();
			currentSource = source;
			
			DirectoryStream<Path> newStream;
			Iterator<Path> paths;
			MergedPath merged;
			try
			{
				newStream = Files.newDirectoryStream(directory.toSourcePath(source), filter);
				
				paths = newStream.iterator();
				if (!paths.hasNext()) return false;
				
				Path p = source.path().relativize(paths.next());
				if (!knownPaths.add(p)) return false;
				merged = new MergedPath(directory.getFileSystem(), p);
			}
			catch (IOException e)
			{
				return false;
			}
			
			if (filter != null)
			{
				try
				{
					if (!filter.accept(merged)) return false;
				}
				catch (IOException e)
				{
					try
					{
						newStream.close();
					}
					catch (Exception _)
					{}
					throw e;
				}
			}
			next = merged;
			stream = newStream;
			this.paths = paths;
			return true;
		}
		
		@Override
		public MergedPath next()
		{
			if (!hasNext()) throw new NoSuchElementException();
			MergedPath p = (@NonNull MergedPath) next;
			next = null;
			return p;
		}
	}
	
	private class ViewIterator implements Iterator<MergedPath> {
		
		private Iterator<MergedPath> iterator;
		private @Nullable MergedPath next;
		
		public ViewIterator(Iterator<MergedPath> iterator)
		{
			this.iterator = iterator;
		}
		
		@Override
		public boolean hasNext()
		{
			if (next != null) return true;
			if (state == CLOSED) return false;
			while (iterator.hasNext())
			{
				next = iterator.next();
				if (filter == null) break;
				try
				{
					if (filter.accept(next)) break;
				}
				catch (IOException e)
				{
					state = CLOSED;
					throw new DirectoryIteratorException(e);
				}
			}
			if (next == null)
			{
				state = CLOSED;
				return false;
			}
			if (!next.startsWith(directory))
			{
				iterator = MergedFileSystemProvider.cast(List.of().iterator());
				next = null;
				state = CLOSED;
				return false;
			}
			return true;
		}
		
		@Override
		public MergedPath next()
		{
			if (!hasNext()) throw new NoSuchElementException();
			MergedPath p = (@NonNull MergedPath) next;
			next = null;
			return p;
		}
	}
}
