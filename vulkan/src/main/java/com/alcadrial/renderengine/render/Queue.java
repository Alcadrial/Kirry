package com.alcadrial.renderengine.render;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkQueue;

import com.alcadrial.renderengine.render.util.DeviceResource;

public class Queue extends DeviceResource<VkQueue> {
	
	public Queue(Device device, int familyIndex)
	{
		super(device);
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			PointerBuffer pGraphicsQueue = stack.pointers(VK10.VK_NULL_HANDLE);
			VK10.vkGetDeviceQueue(device.getVk(), familyIndex, 0, pGraphicsQueue);
			vk = new VkQueue(pGraphicsQueue.get(0), device.getVk());
		}
	}
}
