package com.alcadrial.renderengine.core.resources;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.MutableSet;
import org.jspecify.annotations.Nullable;

public abstract class AbstractResource implements Resource {
	
	private final MutableSet<Consumer<Resource>> listeners;
	protected final Path path;
	protected final ResourceManager resourceManager;
	private boolean isFound;
	private boolean isValid;
	private AtomicReference<@Nullable CompletableFuture<?>> currentLoading;
	
	@SuppressWarnings("null")
	public AbstractResource(Path path, ResourceManager resourceManager)
	{
		this.path = path;
		this.resourceManager = resourceManager;
		listeners = Sets.mutable.of();
		isFound = false;
		isValid = false;
		currentLoading = new AtomicReference<>();
	}
	
	@FunctionalInterface
	public interface InputStreamSupplier {
		
		InputStream get() throws IOException;
	}
	
	@SuppressWarnings("null")
	@Override
	public CompletableFuture<?> load()
	{
		CompletableFuture<?> future = new CompletableFuture<>();
		CompletableFuture<?> currentLoading = this.currentLoading.compareAndExchangeAcquire(null, future);
		if (currentLoading != null && currentLoading != future) return currentLoading;
		
		loadData(future, () -> {
			boolean found = true;
			try
			{
				return Files.newInputStream(path);
			}
			catch (FileNotFoundException e)
			{
				found = false;
				throw e;
			}
			finally
			{
				isFound = found;
			}
		});
		return future;
	}
	
	protected abstract void loadData(CompletableFuture<?> future, InputStreamSupplier stream);
	
	@SuppressWarnings("null")
	@Override
	public CompletableFuture<?> reload()
	{
		CompletableFuture<?> currentLoading = this.currentLoading.getAcquire();
		if (currentLoading != null && !currentLoading.isDone()) return currentLoading;
		CompletableFuture<?> future = new CompletableFuture<>();
		CompletableFuture<?> currentLoading0 = this.currentLoading.compareAndExchangeAcquire(currentLoading, future);
		if (currentLoading0 != future && currentLoading0 != currentLoading) return currentLoading;
		
		loadData(future, () -> {
			boolean found = true;
			try
			{
				return Files.newInputStream(path);
			}
			catch (FileNotFoundException e)
			{
				found = false;
				throw e;
			}
			finally
			{
				isFound = found;
			}
		});
		return future.handle((v, e) -> {
			if (e == null)
			{
				for (Consumer<Resource> action : listeners) action.accept(AbstractResource.this);
				isValid = true;
			}
			else isValid = false;
			return null;
		});
	}
	
	@Override
	public void addReloadListener(Consumer<Resource> action)
	{
		listeners.add(action);
	}
	
	@Override
	public void removeReloadListener(Consumer<Resource> action)
	{
		listeners.remove(action);
	}
	
	@Override
	public boolean isFound()
	{
		return isFound;
	}
	
	@Override
	public boolean isValid()
	{
		return isValid;
	}

	@Override
	public Path getPath()
	{
		return path;
	}
	
	@Override
	public void close()
	{
		listeners.clear();
		isValid = false;
	}
}
