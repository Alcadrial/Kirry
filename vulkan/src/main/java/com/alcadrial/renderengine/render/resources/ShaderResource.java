package com.alcadrial.renderengine.render.resources;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jspecify.annotations.Nullable;

import com.alcadrial.renderengine.core.resources.ResourceManager;
import com.alcadrial.renderengine.core.resources.ResourceType;
import com.alcadrial.renderengine.render.shader.PreparedShader;
import com.alcadrial.renderengine.render.shader.Shader;
import com.alcadrial.renderengine.render.shader.ShaderType;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;

public class ShaderResource extends AbstractVulkanResource {
	
	public static final ResourceType<ShaderResource> TYPE = ResourceType.of(ShaderResource.class);
	
	private boolean forceRecompile;
	private @Nullable PreparedShader shader;
	
	public ShaderResource(Path path, ResourceManager resourceManager)
	{
		this(path, resourceManager, false);
	}
	
	public ShaderResource(Path path, ResourceManager resourceManager, boolean forceRecompile)
	{
		super(path, resourceManager);
		this.forceRecompile = forceRecompile;
	}
	
	@Override
	protected void loadData(InputStreamSupplier stream) throws Exception
	{
		Shader shader = new Shader(path);
		ShaderType type = ShaderType.fromPath(path);
		if (forceRecompile) shader.compile(type);
		else
		{
			Path bin = path.resolveSibling(path.getFileName() + ".bin");
			if (Files.isReadable(bin)) shader.loadCompiled(Files.readAllBytes(bin));
			else shader.compile(type);
		}
		if (!shader.isCompilationSuccessfull()) throw new IOException("Shader " + shader.getPath() + " has errors");
		this.shader = new PreparedShader(shader, type);
	}
	
	public PreparedShader getShader()
	{
		return VulkanErrorUtils.nonNull(shader, "Shader was not loaded");
	}
	
	public void setForceRecompile(boolean forceRecompile)
	{
		this.forceRecompile = forceRecompile;
	}
}
