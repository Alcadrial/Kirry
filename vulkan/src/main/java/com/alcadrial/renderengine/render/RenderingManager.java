package com.alcadrial.renderengine.render;

import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.window.VulkanWindow;

public class RenderingManager {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private CommandBuffer commandBufferData;
	
	private Renderer defaultRenderer;
	
	public RenderingManager(VulkanWindow window)
	{
		//	defaultRenderer = RendererUtils.getTriangleRenderer(window.getDevice(), shaderProgram, uniformBuffer, renderpass, size);
		window.addSurfaceUpdateActions(s -> defaultRenderer.startModificationPlan().size(s.getSize()).submit());
	}
	
	public void drawTexture() throws VulkanException
	{
		defaultRenderer.draw(commandBufferData);
	}
}
