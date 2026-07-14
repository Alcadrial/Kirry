package com.alcadrial.kirry.render.test.widgets;

import java.io.IOException;
import java.nio.file.Path;

import org.jspecify.annotations.NonNull;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2ir;
import com.alcadrial.kector.vector.Vector3ir;
import com.alcadrial.kirry.core.resources.ResourceManager;
import com.alcadrial.kirry.render.Renderer;
import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;
import com.alcadrial.kirry.render.resources.TextureResource;
import com.alcadrial.kirry.render.texture.TextureManager;
import com.alcadrial.kirry.render.util.VulkanErrorUtils;
import com.alcadrial.kirry.render.util.exception.ExternalException;
import com.alcadrial.kirry.render.util.exception.VulkanException;
import com.alcadrial.kirry.render.window.VulkanWindow;
import com.alcadrial.kirry.render.window.widget.Sprite;

public class HiddenSprite extends Sprite {
	
	public static HiddenSprite ofFull(VulkanWindow window, String pathname) throws VulkanException
	{
		return ofWindowed(window, Vector3ir.of(), window.getSurface().getData().size(), pathname);
	}
	
	public static HiddenSprite ofFull(VulkanWindow window, Path path) throws VulkanException
	{
		return ofWindowed(window, Vector3ir.of(), window.getSurface().getData().size(), path);
	}
	
	public static HiddenSprite ofFull(VulkanWindow window, TextureResource texture) throws VulkanException
	{
		return ofWindowed(window, Vector3ir.of(), window.getSurface().getData().size(), texture);
	}
	
	public static HiddenSprite ofWindowed(VulkanWindow window, Vector3ir pos, Vector2ir size, String pathname) throws VulkanException
	{
		ResourceManager resourceManager = VulkanErrorUtils.nonNull(window.getResourceManager(), "Missing resource manager");
		
		try
		{
			return ofWindowed(window, pos, size, resourceManager.getResource(pathname, TextureResource.TYPE));
		}
		catch (IOException e)
		{
			throw new ExternalException(e);
		}
	}
	
	public static HiddenSprite ofWindowed(VulkanWindow window, Vector3ir pos, Vector2ir size, Path path) throws VulkanException
	{
		ResourceManager resourceManager = VulkanErrorUtils.nonNull(window.getResourceManager(), "Missing resource manager");
		
		try
		{
			return ofWindowed(window, pos, size, resourceManager.getResource(path, TextureResource.TYPE));
		}
		catch (IOException e)
		{
			throw new ExternalException(e);
		}
	}
	
	public static HiddenSprite ofWindowed(VulkanWindow window, Vector3ir pos, Vector2ir size, TextureResource texture) throws VulkanException
	{
		return new HiddenSprite(window, pos, size, texture);
	}
	
	private boolean isShown;
	
	public HiddenSprite(@NonNull VulkanWindow window, @NonNull Vector3ir pos, @NonNull Vector2ir size, @NonNull TextureResource texture)
	{
		super(window, pos, size, texture);
		isShown = true;
	}
	
	public void hide()
	{
		isShown = false;
	}
	
	public void show()
	{
		isShown = true;
	}
	
	public void setShown(boolean show)
	{
		isShown = show;
	}
	
	@Override
	public void draw(@NonNull Matrix4f matrix, @NonNull CommandBuffer commandBuffer, @NonNull Renderer renderer, @NonNull TextureManager textureManager) throws VulkanException
	{
		if (isShown) super.draw(matrix, commandBuffer, renderer, textureManager);
	}
}
