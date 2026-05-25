package com.alcadrial.renderengine.render.resources;

import java.nio.file.Path;

import com.alcadrial.renderengine.core.resources.ResourceManager;
import com.alcadrial.renderengine.core.resources.ResourceType;


public class TextureResource extends AbstractVulkanResource {

	public static final ResourceType<TextureResource> TYPE = ResourceType.of(TextureResource.class);
	
	public TextureResource(Path path, ResourceManager resourceManager)
	{
		super(path, resourceManager);
	}

	@Override
	protected void loadData(InputStreamSupplier stream) throws Exception
	{
		
	}
}
