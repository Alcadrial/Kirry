package com.alcadrial.renderengine.render.resources;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import com.alcadrial.renderengine.core.resources.AbstractResource;
import com.alcadrial.renderengine.core.resources.ResourceManager;
import com.alcadrial.renderengine.render.util.future.VulkanFuture;


public abstract class AbstractVulkanResource extends AbstractResource {
	
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
	
	protected abstract void loadData(InputStreamSupplier stream) throws Exception;
}
