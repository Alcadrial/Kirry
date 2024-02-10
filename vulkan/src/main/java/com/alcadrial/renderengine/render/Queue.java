package com.alcadrial.renderengine.render;

import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkSubmitInfo;

import com.alcadrial.renderengine.render.util.DeviceResource;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;

public class Queue extends DeviceResource<VkQueue> {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private Lock lock;
	private final int familyIndex;
	
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
		lock = new ReentrantLock();
	}
	
	public int getFamilyIndex()
	{
		return familyIndex;
	}
	
	public void submit(VkSubmitInfo submitInfo, long fence)
	{
		lock.lock();
		try
		{
			VulkanErrorUtils.assertNoError(VK10.vkQueueSubmit(vk, submitInfo, fence), "Failed to submit queue");
		}
		finally
		{
			lock.unlock();
		}
	}
	
	public void waitIdle()
	{
		lock.lock();
		try
		{
			VK10.vkQueueWaitIdle(vk);
		}
		finally
		{
			lock.unlock();
		}
	}
	
	public Future<?> waitAsyncIdle(Runnable action)
	{
		return RenderEngine.getExecutor().submit(() -> {
			lock.lock();
			try
			{
				VK10.vkQueueWaitIdle(vk);
				action.run();
			}
			finally
			{
				lock.unlock();
			}
		});
	}
	
	public Future<?> waitAsyncIdle(Consumer<Queue> action)
	{
		return RenderEngine.getExecutor().submit(() -> {
			lock.lock();
			try
			{
				VK10.vkQueueWaitIdle(vk);
				action.accept(this);
			}
			finally
			{
				lock.unlock();
			}
		});
	}
}
