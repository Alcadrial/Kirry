package com.alcadrial.renderengine.render.buffer;

import org.lwjgl.vulkan.VK10;

import com.alcadrial.renderengine.render.CommandBufferData;

public class VertexBuffer extends VulkanDoubleBuffer {
	
	public VertexBuffer(int size)
	{
		super(size * 4, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
	}
	
	public VertexBuffer(float[] data)
	{
		this(data.length);
		asFloatBuffer().put(data);
	}
	
	public void bind(CommandBufferData commandBufferData)
	{
		VK10.vkCmdBindVertexBuffers(commandBufferData.getCommandBuffer(), 0, new long[] {gpuId}, new long[] {0});
	}
}
