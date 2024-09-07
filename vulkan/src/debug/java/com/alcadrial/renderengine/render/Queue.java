package com.alcadrial.renderengine.render;

import java.util.concurrent.Future;
import java.util.function.Consumer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkSubmitInfo;

import com.alcadrial.renderengine.render.util.AbstractDeviceResource;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;

public class Queue extends AbstractDeviceResource<VkQueue> {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private final int familyIndex;
	//	private Future<?> submission = CompletableFuture.completedFuture(null);
	
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
	
	public synchronized void submit(VkSubmitInfo submitInfo, long fence)
	{
		VulkanErrorUtils.assertNoError(VK10.vkQueueSubmit(vk, submitInfo, fence), "Failed to submit queue");
	}
	
	public synchronized void waitIdle()
	{
		VK10.vkQueueWaitIdle(vk);
	}
	
	public Future<?> waitAsyncIdle(Runnable action)
	{
		return RenderEngine.getExecutor().submit(() -> {
			VK10.vkQueueWaitIdle(vk);
			action.run();
			
		});
	}
	
	public Future<?> waitAsyncIdle(Consumer<Queue> action)
	{
		return RenderEngine.getExecutor().submit(() -> {
			VK10.vkQueueWaitIdle(vk);
			action.accept(this);
		});
	}
	
	public int present(VkPresentInfoKHR presentInfo)
	{
		return KHRSwapchain.vkQueuePresentKHR(vk, presentInfo);
	}
	
	@Override
	public void debug()
	{
		Debug.print("vk: " + Debug.address(vk));
		Debug.print("familyIndex: " + familyIndex);
		//	Debug.print("isIdle: " + submission.isDone());
	}
}
