package com.alcadrial.kirry.render.resources;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import com.alcadrial.kirry.core.resources.AbstractResource;
import com.alcadrial.kirry.core.resources.ResourceManager;
import com.alcadrial.kirry.render.util.future.VulkanFuture;


public abstract class AbstractVulkanResource<R extends AbstractVulkanResource<R>> extends AbstractResource<R> {
	
	public AbstractVulkanResource(Path path, ResourceManager resourceManager)
	{
		super(path, resourceManager);
	}
	
	@Override
	protected void loadData(CompletableFuture<?> future, InputStreamSupplier stream)
	{
		VulkanFuture.submitAsync(() -> {
			try
			{
				loadData(stream);
				future.complete(null);
			}
			catch (Throwable e)
			{
				future.completeExceptionally(e);
			}
			return null;
		});
	}
	
	protected abstract void loadData(InputStreamSupplier stream) throws IOException;
}
