package com.alcadrial.renderengine.render.buffer;

import java.nio.FloatBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;

import com.alcadrial.renderengine.render.CommandBufferData;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;

public class VertexBuffer extends VulkanDoubleBuffer {
	
	public VertexBuffer(float[] data)
	{
		super(data.length * 4, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			Device device = RenderEngine.getVulkan().getDevice();
			PointerBuffer gpuData = stack.mallocPointer(1);
			VulkanErrorUtils.assertNoError(VK10.vkMapMemory(device.getDevice(), memory, 0, size, 0, gpuData), "Failed to map vertex buffer memory");
			FloatBuffer d = gpuData.getFloatBuffer((int) size);
			d.put(data);
			VK10.vkUnmapMemory(device.getDevice(), memory);
		}
	}
	
	public void bind(CommandBufferData commandBufferData)
	{
		VK10.vkCmdBindVertexBuffers(commandBufferData.getCommandBuffer(), 0, new long[] {gpuId}, new long[] {0});
	}
}
