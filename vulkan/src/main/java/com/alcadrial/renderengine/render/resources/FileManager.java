package com.alcadrial.renderengine.render.resources;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.alcadrial.renderengine.core.resources.Resource;
import com.alcadrial.renderengine.core.resources.ResourceManager;
import com.alcadrial.renderengine.core.resources.ResourceType;
import com.alcadrial.renderengine.core.util.ResizeFastList;

public class FileManager implements ResourceManager {
	
	public static URI jarUri(Path jarPath)
	{
		return URI.create("jar:" + jarPath.toString() + "!/");
	}
	
	public static URI loaderJarResourceUri(ClassLoader loader, String resource) throws IOException
	{
		URI uri = loaderResourceUri(loader, resource);
		String uriString = uri.toASCIIString();
		return URI.create(uriString.substring(0, uriString.lastIndexOf(resource)));
	}
	
	public static URI loaderJarResourceUri(String resource) throws IOException
	{
		return loaderJarResourceUri(ClassLoader.getSystemClassLoader(), resource);
	}
	
	public static URI loaderResourceUri(ClassLoader loader, String resource) throws IOException
	{
		try
		{
			URL url = loader.getResource(resource);
			if (url == null) throw new FileNotFoundException(resource);
			return url.toURI();
		}
		catch (URISyntaxException e)
		{
			throw new IOException(e);
		}
	}
	
	public static URI loaderResourceUri(String resource) throws IOException
	{
		return loaderResourceUri(ClassLoader.getSystemClassLoader(), resource);
	}
	
	public static URI loaderUri(ClassLoader loader) throws IOException
	{
		return loaderResourceUri(loader, "");
	}
	
	public static URI loaderUri() throws IOException
	{
		return loaderUri(ClassLoader.getSystemClassLoader());
	}
	
	public static FileManager ofJar() throws IOException
	{
		return ofJar("assets");
	}
	
	public static FileManager ofJar(String pathname) throws IOException
	{
		Enumeration<@NonNull URL> resources = ClassLoader.getSystemResources(pathname);
		MutableList<@NonNull URL> urls = Lists.mutable.ofAll(resources::asIterator);
		MutableList<@NonNull URI> uris = Lists.mutable.ofInitialCapacity(urls.size());
		for (URL url : urls)
		{
			try
			{
				uris.add(url.toURI());
			}
			catch (URISyntaxException _)
			{}
		}
		return new FileManager(uris);
	}
	
	public static FileManager ofDirectory(File file) throws IOException
	{
		return new FileManager(List.of(file));
	}
	
	public static FileManager ofDirectory(Path path) throws IOException
	{
		return new FileManager(List.of(path));
	}
	
	public static FileManager ofURIs(URI... uris) throws IOException
	{
		return new FileManager(List.of(uris));
	}
	
	/**
	 * Create a new {@link FileManager} that will resolve paths against the given roots,
	 * a root may be instances of the following classes:
	 * <ul>
	 * <li> {@link String}: will be converted {@link Path} using {@link Path#of(String, String...)} </li>
	 * <li> {@link File}: will be converted to Path using {@link File#toPath()} </li>
	 * <li> {@link URI}: will be converted to Path using {@link Path#of(URI)} </li>
	 * <li> {@link Path} </li>
	 * </ul>
	 * 
	 * @param roots the starting directories to search for files
	 * @return a new FileManager
	 * @throws IOException
	 */
	public static FileManager of(Object... roots) throws IOException
	{
		return new FileManager(List.of(roots));
	}
	
	private record ResourceAction<R extends Resource>(R resource, Consumer<R> action) {}
	private record PathRegistration(BiConsumer<Path, Kind<?>> action, WatchKey[] keyReference) {}
	
	private FileSystem fileSystem;
	private Path root;
	private TreeMap<Path, PathRegistration> registeredPaths;
	private TreeMap<Path, ResourceAction<?>> registeredResourcePaths;
	private FastList<BiFunction<Path, ResourceManager, ? extends Resource>> registeredResourceTypes;
	private @Nullable WatchService watchService;
	
	private FileManager(List<? extends Object> rootsUris) throws IOException
	{
		fileSystem = FileSystems.newFileSystem(URI.create("rem:" + System.identityHashCode(this)), Map.of("sources", rootsUris));
		root = fileSystem.getPath("");
		registeredPaths = new TreeMap<>();
		registeredResourcePaths = new TreeMap<>();
		registeredResourceTypes = new ResizeFastList<>();
	}
	
	@Override
	public <R extends Resource> void registerResourceType(ResourceType<R> type, BiFunction<Path, ResourceManager, R> loader)
	{
		registeredResourceTypes.set(type.getId(), loader);
	}
	
	@Override
	public <R extends Resource> @NonNull R getResource(String pathname, ResourceType<R> type) throws IOException
	{
		return getResource(getPath(pathname), type);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <R extends Resource> R getResource(Path path, ResourceType<R> type) throws IOException
	{
		if (registeredResourceTypes.size() <= type.getId()) throw new UnsupportedOperationException("Unknown resource type: " + type);
		return (@NonNull R) registeredResourceTypes.get(type.getId()).apply(path, this);
	}
	
	@Override
	public Path getPath(String pathname)
	{
		return root.resolve(pathname);
	}
	
	public Path getPath(File file) throws IOException
	{
		return root.resolve(file.toString());
	}
	
	@Override
	public void startWatchingResources() throws IOException
	{
		WatchService watchService = fileSystem.newWatchService();
		for (Path directory : registeredPaths.keySet())
		{
			directory.register(watchService,
				StandardWatchEventKinds.ENTRY_CREATE,
				StandardWatchEventKinds.ENTRY_MODIFY,
				StandardWatchEventKinds.ENTRY_DELETE,
				StandardWatchEventKinds.OVERFLOW);
		}
		this.watchService = watchService;
		Thread.startVirtualThread(this::watch);
	}
	
	@Override
	public void stopWatchingResources() throws IOException
	{
		if (watchService != null)
		{
			watchService.close();
			watchService = null;
		}
	}
	
	@Override
	public void registerForChanges(String pathname, BiConsumer<Path, Kind<?>> action) throws IOException
	{
		registerForChanges(getPath(pathname), action);
	}
	
	@Override
	public void registerForChanges(Path path, BiConsumer<Path, Kind<?>> action) throws IOException
	{
		WatchKey watchKey = null;
		if (watchService != null)
		{
			watchKey = path.register(watchService,
				StandardWatchEventKinds.ENTRY_CREATE,
				StandardWatchEventKinds.ENTRY_MODIFY,
				StandardWatchEventKinds.ENTRY_DELETE,
				StandardWatchEventKinds.OVERFLOW);
		}
		registeredPaths.put(path, new PathRegistration(action, new WatchKey[] { watchKey }));
	}
	
	@Override
	public <R extends Resource> void registerForChanges(R resource, Consumer<R> action) throws IOException
	{
		Path parent = resource.getPath().getParent();
		registeredResourcePaths.put(parent, new ResourceAction<>(resource, action));
		registerForChanges(parent, (_, _) -> {});
	}
	
	@Override
	public void unregisterForChanges(String pathname) throws IOException
	{
		unregisterForChanges(getPath(pathname));
	}
	
	@Override
	public void unregisterForChanges(Path path) throws IOException
	{
		PathRegistration registration = registeredPaths.remove(path);
		if (registration != null && registration.keyReference[0] != null) registration.keyReference[0].cancel();
	}
	
	private <R extends Resource> void watch()
	{
		WatchService watchService = this.watchService;
		if (watchService == null) return;
		try
		{
			while (true)
			{
				WatchKey key;
				key = watchService.take();
				Path keyPath = (Path) key.watchable();
				for (WatchEvent<?> event : key.pollEvents())
				{
					Kind<?> kind = event.kind();
					if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE || kind == StandardWatchEventKinds.ENTRY_MODIFY || event.kind() == StandardWatchEventKinds.ENTRY_DELETE || event.kind() == StandardWatchEventKinds.OVERFLOW)
					{
						Path path = keyPath.resolve((@NonNull Path) event.context());
						PathRegistration registration = registeredPaths.get(keyPath);
						if (registration != null)
						{
							try
							{
								registration.action.accept(path, kind);
							}
							catch (Throwable e)
							{
								e.printStackTrace();
							}
						}
						ResourceAction<?> resourceAction = registeredResourcePaths.get(keyPath);
						if (resourceAction != null) handleResourceUpdate(resourceAction);
					}
				}
				key.reset();
			}
		}
		catch (InterruptedException e)
		{}
	}
	
	private <R extends Resource> void handleResourceUpdate(ResourceAction<R> resourceAction)
	{
		try
		{
			resourceAction.action.accept(resourceAction.resource);
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
	}
}
