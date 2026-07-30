package com.alcadrial.kirry.render.test.util;

import java.nio.file.Path;

import org.jspecify.annotations.NonNull;

import com.alcadrial.kirry.core.resources.ResourceManager;
import com.alcadrial.kirry.render.resources.ShaderResource;


public class RecompileShaderResource extends ShaderResource {
	
	public RecompileShaderResource(@NonNull Path path, @NonNull ResourceManager resourceManager)
	{
		super(path, resourceManager, TestProperties.FORCE_RECOMPILE_SHADERS.getValue());
	}
}
