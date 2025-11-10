package com.alcadrial.renderengine.render.commandbuffer;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkSubmitInfo;

import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.queue.Queue;
import com.alcadrial.renderengine.render.util.VulkanFuture;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public class CommandBufferStream {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	/**
	 * Implementation with {@link #flush()} not thread-safe.
	 * 
	 * @param queue
	 * @return a new CommandBufferStream
	 */
	public static CommandBufferStream create(Queue queue)
	{
		return new CommandBufferStream(queue);
	}
	
	/**
	 * Implementation with {@link #flush()} thread-safe.
	 * 
	 * @param queue
	 * @param lock
	 * @return a new SynchronizedCommandBufferStream
	 */
	public static SynchronizedCommandBufferStream create(Queue queue, Lock lock)
	{
		return new SynchronizedCommandBufferStream(queue, lock);
	}
	
	protected Queue queue;
	protected ConcurrentLinkedQueue<Entry<VkCommandBuffer, CompletableFuture<?>>> stream;
	
	protected CommandBufferStream(Queue queue)
	{
		this.queue = queue;
		stream = new ConcurrentLinkedQueue<>();
	}
	
	/**
	 * Insert a command buffer into this stream.
	 * 
	 * @param commandBuffer
	 * @see #add(VkCommandBuffer)
	 * @see #flush()
	 */
	public CompletableFuture<?> add(CommandBuffer commandBuffer)
	{
		return add(commandBuffer.getVk());
	}
	
	/**
	 * Insert a command buffer into this stream.
	 * 
	 * @param commandBuffer
	 * @see #add(CommandBuffer)
	 * @see #flush()
	 */
	public CompletableFuture<?> add(VkCommandBuffer commandBuffer)
	{
		CompletableFuture<?> f = new CompletableFuture<>();
		stream.add(Map.entry(commandBuffer, f));
		return f;
	}
	
	/**
	 * Flushed all the command buffers added using {@link #add}.
	 * <p>
	 * Not thread-safe, should be called from only 1 thread.
	 * 
	 * @return a {@link CompletableFuture} that is completed when all the command buffer are executed.
	 * @throws VulkanException
	 */
	public VulkanFuture<?> flush() throws VulkanException
	{
		List<Entry<VkCommandBuffer, CompletableFuture<?>>> buffers = extractBuffers();
		if (!buffers.isEmpty())
		{
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				
				PointerBuffer p = stack.mallocPointer(buffers.size());
				for (Entry<VkCommandBuffer, CompletableFuture<?>> e : buffers) p.put(e.getKey().address());
				
				VkSubmitInfo info = VkSubmitInfo.malloc(stack);
				info.sType(VK10.VK_STRUCTURE_TYPE_SUBMIT_INFO);
				info.pNext(0);
				info.waitSemaphoreCount(0);
				info.pWaitSemaphores(null);
				info.pWaitDstStageMask(null);
				info.pCommandBuffers(p.rewind());
				info.pSignalSemaphores(null);
				Device device = queue.getDevice();
				return RenderEngine.composeAfter(queue.submitAsync(info, device.createFence(stack, false)), fences -> {
					device.destroyFences(fences); 
					for (Entry<VkCommandBuffer, CompletableFuture<?>> e : buffers) e.getValue().complete(null);
				});
			}
		}
		return RenderEngine.completedTask();
	}
	
	protected List<Entry<VkCommandBuffer, CompletableFuture<?>>> extractBuffers()
	{
		LinkedList<Entry<VkCommandBuffer, CompletableFuture<?>>> buffers = new LinkedList<>();
		Entry<VkCommandBuffer, CompletableFuture<?>> e;
		while ((e = stream.poll()) != null) buffers.add(e);
		return buffers;
	}
}
