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
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public class Queue extends AbstractDeviceResource<VkQueue> {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public static Queue createQueue(Device device, int familyIndex)
	{
		return new Queue(device, familyIndex);
	}
	
	private final int familyIndex;
	
	private Queue(Device device, int familyIndex)
	{
		super(device);
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			PointerBuffer p = stack.mallocPointer(1);
			VK10.vkGetDeviceQueue(device.getVk(), familyIndex, 0, p);
			vk = new VkQueue(p.get(0), device.getVk());
		}
		this.familyIndex = familyIndex;
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
	
	public synchronized void submit(VkSubmitInfo submitInfo, long fence) throws VulkanException
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
	}
}
