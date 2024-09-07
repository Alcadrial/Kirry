package com.alcadrial.renderengine.render;

import org.lwjgl.vulkan.VK10;

import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.renderpass.RenderPass;
import com.alcadrial.renderengine.render.shader.ShaderProgram;

public class RendererUtils {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private static Renderer.Builder defaultRenderer(ShaderProgram shaderProgram, VulkanBuffer uniformBuffer)
	{
		return Renderer.create().shaderProgram(shaderProgram).dynamicScissor(true).dynamicViewport(true);
	}
	
	public static Renderer getTriangleRenderer(Device device, ShaderProgram shaderProgram, VulkanBuffer uniformBuffer, RenderPass renderPass, Vector2i size)
	{
		return defaultRenderer(shaderProgram, uniformBuffer).build(device, size, renderPass);
	}
	
	public static Renderer getLineRenderer(Device device, ShaderProgram shaderProgram, VulkanBuffer uniformBuffer, RenderPass renderPass, Vector2i size)
	{
		return defaultRenderer(shaderProgram, uniformBuffer).polygonMode(VK10.VK_POLYGON_MODE_LINE).build(device, size, renderPass);
	}
}
