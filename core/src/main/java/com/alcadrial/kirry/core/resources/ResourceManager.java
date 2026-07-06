package com.alcadrial.kirry.core.resources;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.jspecify.annotations.NonNull;

public interface ResourceManager extends AutoCloseable {
	
	<R extends Resource> void registerResourceType(ResourceType<R> type, BiFunction<Path, ResourceManager, R> loader);
	
	<R extends Resource> @NonNull R getResource(String pathname, ResourceType<R> type) throws IOException;
	<R extends Resource> @NonNull R getResource(Path path, ResourceType<R> type) throws IOException;
	
	/**
	 * Retrieve a path to the given pathname relative to a
	 * Path
	 */
	Path getPath(String pathname);
	
	void startWatchingResources() throws IOException;
	/** Stop watching for changes in the FileSystem */
	void stopWatchingResources() throws IOException;
	
	void registerForChanges(String pathname, BiConsumer<Path, Kind<?>> action) throws IOException;
	void registerForChanges(Path path, BiConsumer<Path, Kind<?>> action) throws IOException;
	<R extends Resource> void registerForChanges(R resource, Consumer<R> action) throws IOException;
	
	void unregisterForChanges(String pathname) throws IOException;
	void unregisterForChanges(Path path) throws IOException;
	
	@Override
	void close() throws IOException;
}
