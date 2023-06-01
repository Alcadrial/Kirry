package com.alcadrial.renderengine.render;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkQueue;

public class Queue {
	
	private VkQueue queue;
	private Device device;
	
	public Queue(Device device, int familyIndex)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			this.device = device;
			PointerBuffer pGraphicsQueue = stack.pointers(VK10.VK_NULL_HANDLE);
			VK10.vkGetDeviceQueue(device.getDevice(), familyIndex, 0, pGraphicsQueue);
			queue = new VkQueue(pGraphicsQueue.get(0), device.getDevice());
		}
	}
	
	public VkQueue getQueue()
	{
		return queue;
	}
	
	public Device getDevice()
	{
		return device;
	}
}
