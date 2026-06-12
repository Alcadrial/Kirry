package com.alcadrial.kirry.render.commandbuffer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;

import com.alcadrial.kirry.render.KirryVulkan;
import com.alcadrial.kirry.render.queue.Queue;
import com.alcadrial.kirry.render.util.VulkanErrorUtils;
import com.alcadrial.kirry.render.util.exception.VulkanException;
import com.alcadrial.kirry.render.util.future.VulkanFuture;

public class SynchronizedCommandBufferStream extends CommandBufferStream {
	
	static
	{
		KirryVulkan.assertLoaded();
	}
	
	private Lock lock;
	
	protected SynchronizedCommandBufferStream(Queue queue, Lock lock)
	{
		super(queue);
		this.lock = VulkanErrorUtils.nonNull(lock);
	}
	
	/**
	 * Flushed all the command buffers added using {@link #add}.
	 * 
	 * @return a {@link CompletableFuture} that is completed when all the command buffer are executed.
	 * @throws VulkanException
	 */
	@Override
	public VulkanFuture<?> flush() throws VulkanException
	{
		lock.lock();
		try
		{
			return super.flush();
		}
		finally
		{
			lock.unlock();
		}
	}
}
