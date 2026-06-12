package com.alcadrial.kirry.core.resources;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface Resource {

	/**Load the resource into memory, does nothing if already loaded*/
	CompletableFuture<?> load();
	CompletableFuture<?> reload();
	//TODO: void unload();
	void addReloadListener(Consumer<Resource> action);
	void removeReloadListener(Consumer<Resource> action);
	
	boolean isFound();
	boolean isValid();

	Path getPath();
	void close();
}
