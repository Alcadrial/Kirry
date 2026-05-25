package com.alcadrial.renderengine.render.resources.filesystem;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.alcadrial.renderengine.render.resources.filesystem.MergedFileSystem.Source;

public class MergedWatchService implements WatchService {
	
	private record ServiceData(WatchService watchService, List<WatchKey> registeredKeys) {}
	
	private Object closeLock;
	private MergedFileSystem fileSystem;
	private ConcurrentSkipListMap<Source, ServiceData> services;
	private ConcurrentSkipListMap<MergedPath, Key> registeredKeys;
	private ConcurrentLinkedQueue<Key> signalledQueue;
	private ExecutorService executorService;
	private volatile boolean isClosed;
	
	public MergedWatchService(MergedFileSystem fileSystem) throws IOException
	{
		this.fileSystem = fileSystem;
		services = new ConcurrentSkipListMap<>();
		try
		{
			for (Source source : fileSystem.getSources())
			{
				try
				{
					services.put(source, new ServiceData(source.path().getFileSystem().newWatchService(), new ArrayList<>()));
				}
				catch (UnsupportedOperationException _)
				{}
			}
		}
		catch (IOException e)
		{
			for (ServiceData service : services.values())
			{
				try
				{
					service.watchService.close();
				}
				catch (IOException _)
				{}
			}
			throw e;
		}
		registeredKeys = new ConcurrentSkipListMap<>();
		signalledQueue = new ConcurrentLinkedQueue<>();
		
		executorService = Executors.newVirtualThreadPerTaskExecutor();
		closeLock = new Object();
	}
	
	public MergedFileSystem getFileSystem()
	{
		return fileSystem;
	}
	
	void addSource(Source source) throws IOException
	{
		Path sourceRoot = source.path();
		WatchService watchService = null;
		try
		{
			watchService = sourceRoot.getFileSystem().newWatchService();
		}
		catch (UnsupportedOperationException _)
		{
			return;
		}
		
		try
		{
			synchronized (this)
			{
				List<WatchKey> keys = new ArrayList<>();
				for (Entry<MergedPath, Key> e : registeredKeys.entrySet())
				{
					MergedPath path = e.getKey();
					Key key = e.getValue();
					
					Path keyPath = sourceRoot.resolve(path.toString());
					keys.add(keyPath.register(key.watchService, key.eventKinds, key.modifiers));
				}
				synchronized (closeLock)
				{
					if (!isClosed) services.put(source, new ServiceData(watchService, keys));
				}
			}
		}
		catch (IOException e)
		{
			try
			{
				watchService.close();
			}
			catch (IOException _)
			{}
			throw e;
		}
	}
	
	void removeSource(Source source) throws IOException
	{
		ServiceData service = services.remove(source);
		if (service != null)
		{
			service.watchService.close();
			service.registeredKeys.clear();
		}
	}
	
	Key register(MergedPath path, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException
	{
		Key key = new Key(path, this, events, modifiers);
		synchronized (this)
		{
			Key oldKey = registeredKeys.putIfAbsent(path, key);
			if (oldKey == null)
			{
				for (Entry<Source, ServiceData> e : services.entrySet())
				{
					ServiceData service = e.getValue();
					Path sourceRoot = e.getKey().path();
					Path keyPath = sourceRoot.resolve(path.toString());
					WatchKey serviceKey = keyPath.register(service.watchService, events, modifiers);
					service.registeredKeys.add(serviceKey);
					fileSystem.buildFiles(path);
				}
				return key;
			}
			return oldKey;
		}
	}
	
	void signal(Kind<Path> kind, MergedPath path)
	{
		for (MergedPath p = path; p != null; p = p.getParent())
		{
			Key key = registeredKeys.get(p);
			if (key != null)
			{
				boolean toEnqueue = !key.isSignalled;
				key.addEvent(new Event<>(kind, path));
				if (toEnqueue) enqueueKey(key);
			}
		}
	}
	
	private void enqueueKey(Key key)
	{
		signalledQueue.add(key);
	}
	
	@Override
	public @Nullable Key poll()
	{
		if (isClosed) throw new ClosedWatchServiceException();
		
		Key queueKey;
		while ((queueKey = signalledQueue.poll()) != null) if (queueKey.isSignalled) return queueKey;
		
		for (Entry<Source, ServiceData> e : services.entrySet())
		{
			ServiceData service = e.getValue();
			WatchKey serviceKey = service.watchService.poll();
			if (serviceKey != null)
			{
				Source source = e.getKey();
				Path sourceRoot = source.path();
				Path keyPath = new MergedPath(fileSystem, sourceRoot.relativize((Path) serviceKey.watchable()));
				
				Key key = registeredKeys.get(keyPath);
				if (key == null) continue;
				
				Key resultKey = fileSystem.handleKey(source, serviceKey, key);
				if (resultKey == null) continue;
				return resultKey;
			}
		}
		return null;
	}
	
	@Override
	public @Nullable Key poll(long timeout, TimeUnit unit) throws InterruptedException
	{
		if (isClosed) throw new ClosedWatchServiceException();
		
		Key queueKey;
		while ((queueKey = signalledQueue.poll()) != null) if (queueKey.isSignalled) return queueKey;
		
		Semaphore newKeySemaphore = new Semaphore(0);
		
		Phaser terminationPhaser = new Phaser(1);
		class Result {
			
			Source source;
			ServiceData service;
			@Nullable
			WatchKey key;
			
			public Result(Source source, ServiceData service)
			{
				this.source = source;
				this.service = service;
			}
		}
		LinkedList<Result> results = new LinkedList<>();
		
		for (Entry<Source, ServiceData> e : services.entrySet())
		{
			Result r = new Result(e.getKey(), e.getValue());
			executorService.execute(() -> {
				try
				{
					terminationPhaser.register();
					WatchKey key = r.service.watchService.take();
					r.key = key;
					newKeySemaphore.release();
				}
				catch (InterruptedException _)
				{}
				finally
				{
					terminationPhaser.arriveAndDeregister();
				}
			});
			results.add(r);
		}
		//TODO: subscribe for new sources
		try
		{
			timeout = unit.toNanos(timeout);
			long t = System.nanoTime();
			while (true)
			{
				timeout += (t - (t = System.nanoTime()));
				if (!newKeySemaphore.tryAcquire(timeout, TimeUnit.NANOSECONDS))
				{
					terminationPhaser.arriveAndDeregister();
					terminationPhaser.awaitAdvance(0);
					for (Result r : results) if (r.key != null) r.key.reset();
					return null;
				}
				for (Result r : results) if (r.key != null)
				{
					WatchKey rKey = r.key;
					Source source = r.source;
					Path sourceRoot = source.path();
					Path keyPath = new MergedPath(fileSystem, sourceRoot.relativize((Path) rKey.watchable()));
					
					Key key = registeredKeys.get(keyPath);
					if (key == null) continue;
					
					Key resultKey = fileSystem.handleKey(source, rKey, key);
					if (resultKey == null) continue;
					return resultKey;
				}
			}
		}
		catch (InterruptedException e)
		{
			terminationPhaser.arriveAndDeregister();
			terminationPhaser.awaitAdvance(0);
			for (Result r : results) if (r.key != null) r.key.reset();
			throw e;
		}
	}
	
	@Override
	public Key take() throws InterruptedException
	{
		return (@NonNull Key) poll(Long.MAX_VALUE, TimeUnit.DAYS);
	}
	
	@Override
	public void close()
	{
		synchronized (closeLock)
		{
			if (isClosed) throw new ClosedWatchServiceException();
			isClosed = true;
			
			executorService.shutdownNow();
			for (ServiceData service : services.values())
			{
				try
				{
					service.watchService.close();
				}
				catch (IOException | ClosedWatchServiceException _)
				{}
			}
			fileSystem.removeWatchService(this);
		}
	}
	
	static class Key implements WatchKey {
		
		private final MergedWatchService watchService;
		private final MergedPath path;
		private final WatchEvent.Kind<?>[] eventKinds;
		private final WatchEvent.Modifier[] modifiers;
		private MutableList<Event<?>> events;
		private boolean isSignalled;
		private volatile boolean isValid;
		
		public Key(MergedPath path, MergedWatchService watchService, WatchEvent.Kind<?>[] eventKinds, WatchEvent.Modifier[] modifiers)
		{
			this.watchService = watchService;
			this.path = path;
			events = Lists.mutable.of();
			isSignalled = false;
			this.isValid = true;
			this.eventKinds = eventKinds;
			this.modifiers = modifiers;
		}
		
		synchronized void addEvent(Event<?> event)
		{
			isSignalled = true;
			events.add(event);
		}
		
		synchronized void addIncrementEvent(Event<?> event)
		{
			isSignalled = true;
			Optional<Event<?>> last = events.getLastOptional();
			if (last.isPresent())
			{
				Event<?> e = last.get();
				if (e.kind() == event.kind)
				{
					e.increment();
					return;
				}
			}
			events.add(event);
		}
		
		@Override
		public synchronized List<WatchEvent<?>> pollEvents()
		{
			try
			{
				return List.copyOf(events);
			}
			finally
			{
				events.clear();
			}
		}
		
		@Override
		public synchronized boolean reset()
		{
			if (isSignalled && isValid)
			{
				if (events.isEmpty()) isSignalled = false;
				else watchService.enqueueKey(this);
			}
			return isValid;
		}
		
		@Override
		public boolean isValid()
		{
			return isValid;
		}
		
		@Override
		public void cancel()
		{
			isValid = false;
		}
		
		@Override
		public MergedPath watchable()
		{
			return path;
		}
	}
	
	static class Event<T> implements WatchEvent<T> {
		
		private final WatchEvent.Kind<T> kind;
		private final T context;
		private int count;
		
		Event(WatchEvent.Kind<T> type, T context)
		{
			this.kind = type;
			this.context = context;
			this.count = 1;
		}
		
		@Override
		public WatchEvent.Kind<T> kind()
		{
			return kind;
		}
		
		@Override
		public T context()
		{
			return context;
		}
		
		@Override
		public int count()
		{
			return count;
		}
		
		void increment()
		{
			count++;
		}
	}
}
