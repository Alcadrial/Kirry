package com.alcadrial.renderengine.render.queue;

import java.util.concurrent.Future;
import java.util.function.Consumer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkSubmitInfo;

import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngineVulkan;
import com.alcadrial.renderengine.render.util.AbstractDeviceResource;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.util.future.VulkanFuture;

public class Queue extends AbstractDeviceResource<VkQueue> {
	
	static
	{
		RenderEngineVulkan.assertLoaded();
	}
	
	public static Queue createQueue(Device device, int familyIndex)
	{
		return new Queue(device, familyIndex);
	}
	
	public static PresentQueue createPresentQueue(Device device, int familyIndex)
	{
		return new PresentQueue(device, familyIndex);
	}
	
	private final int familyIndex;
	
	protected Queue(Device device, int familyIndex)
	{
		super(device, 0);
		VkQueue vkQueue;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			PointerBuffer p = stack.mallocPointer(1);
			VK10.vkGetDeviceQueue(device.getVk(), familyIndex, 0, p);
			vk = vkQueue = new VkQueue(p.get(0), device.getVk());
		}
		this.familyIndex = familyIndex;
		closeInternal();
		vk = vkQueue;
	}
	
	@Override
	public void setName(String name)
	{
		device.setObjectDebugName(this, name);
		this.name = name;
	}
	
	public int getFamilyIndex()
	{
		return familyIndex;
	}
	
	/**
	 * 
	 * @param submitInfo
	 * @throws VulkanException
	 */
	public synchronized void submit(VkSubmitInfo submitInfo) throws VulkanException
	{
		VulkanErrorUtils.assertNoError(VK10.vkQueueSubmit(vk, submitInfo, 0), "Failed to submit queue");
	}
	
	/**
	 * 
	 * @param submitInfo
	 * @param fence an optional handle to a fence to be signaled once all submitted command buffers have completed execution. If fence is not {@link VK10#VK_NULL_HANDLE NULL_HANDLER}, it defines a fence signal operation.
	 * @throws VulkanException
	 */
	public synchronized VulkanFuture<long[]> submitAsync(VkSubmitInfo submitInfo, long fence) throws VulkanException
	{
		VulkanErrorUtils.assertNoError(VK10.vkQueueSubmit(vk, submitInfo, fence), "Failed to submit queue");
		return device.waitForFencesAsync(fence);
	}
	
	public synchronized void submit(VkSubmitInfo submitInfo, long fence) throws VulkanException
	{
		VulkanErrorUtils.assertNoError(VK10.vkQueueSubmit(vk, submitInfo, fence), "Failed to submit queue");
		device.waitForFences(fence);
	}
	
	public synchronized void waitIdle()
	{
		VK10.vkQueueWaitIdle(vk);
	}
	
	public Future<?> waitAsyncIdle(Runnable action)
	{
		return RenderEngineVulkan.getExecutor().submit(() -> {
			waitIdle();
			action.run();
		});
	}
	
	public Future<?> waitAsyncIdle(Consumer<Queue> action)
	{
		return RenderEngineVulkan.getExecutor().submit(() -> {
			waitIdle();
			action.accept(this);
		});
	}
	
	@Override
	public void close()
	{
		
	}
	
	@Override
	public void debug()
	{
		Debug.print("vk: " + Debug.address(vk));
		Debug.print("familyIndex: " + familyIndex);
	}
}
