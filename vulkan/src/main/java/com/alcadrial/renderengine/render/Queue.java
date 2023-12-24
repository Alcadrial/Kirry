package com.alcadrial.renderengine.render;

import java.util.concurrent.Future;
import java.util.function.Consumer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkQueue;

import com.alcadrial.renderengine.render.util.DeviceResource;

public class Queue extends DeviceResource<VkQueue> {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private int familyIndex;
	
	public Queue(Device device, int familyIndex)
	{
		super(device);
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			PointerBuffer pGraphicsQueue = stack.pointers(VK10.VK_NULL_HANDLE);
			VK10.vkGetDeviceQueue(device.getVk(), familyIndex, 0, pGraphicsQueue);
			vk = new VkQueue(pGraphicsQueue.get(0), device.getVk());
			this.familyIndex = familyIndex;
		}
	}
	
	public int getFamilyIndex()
	{
		return familyIndex;
	}
	
	public synchronized void waitIdle()
	{
		VK10.vkQueueWaitIdle(vk);
	}
	
	public Future<?> waitAsyncIdle(Runnable action)
	{
		return RenderEngine.getExecutor().submit(() -> {
			waitIdle();
			action.run();
		});
	}
	
	public Future<?> waitAsyncIdle(Consumer<Queue> action)
	{
		return RenderEngine.getExecutor().submit(() -> {
			waitIdle();
			action.accept(this);
		});
	}
}
