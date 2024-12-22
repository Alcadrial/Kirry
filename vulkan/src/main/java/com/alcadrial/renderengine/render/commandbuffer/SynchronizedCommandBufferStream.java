package com.alcadrial.renderengine.render.commandbuffer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;

import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.queue.Queue;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public class SynchronizedCommandBufferStream extends CommandBufferStream {
	
	static
	{
		RenderEngine.assertLoaded();
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
	public CompletableFuture<?> flush() throws VulkanException
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
