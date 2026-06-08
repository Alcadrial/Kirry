package com.alcadrial.renderengine.render.test.util;

import java.nio.file.Path;

import org.jspecify.annotations.NonNull;

import com.alcadrial.renderengine.core.resources.ResourceManager;
import com.alcadrial.renderengine.render.resources.ShaderResource;


public class RecompileShaderResource extends ShaderResource {
	
	public RecompileShaderResource(@NonNull Path path, @NonNull ResourceManager resourceManager)
	{
		super(path, resourceManager, true);
	}
}
