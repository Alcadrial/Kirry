package com.alcadrial.renderengine.render.texture;

import java.nio.ByteBuffer;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;

import org.lwjgl.vulkan.VkBufferImageCopy;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.kector.vector.Vector3i;
import com.alcadrial.kector.vector.Vector4f;
import com.alcadrial.kector.vector.Vector4i;
import com.alcadrial.renderengine.core.util.Nullable;
import com.alcadrial.renderengine.render.CommandBuffer;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public class ImageWriter implements AutoCloseable {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private class Work {
		
		private Future<?> work;
		private Work next;
		
		public Work(Future<?> work)
		{
			this.work = work;
			next = null;
		}
	}
	
	private static final int MAX_COPY_COUNT = 16;
	
	private VulkanBuffer srcBuffer;
	private int srcOffset;
	private Texture dstTexture;
	private int xOffset, yOffset, zOffset;
	
	private ByteBuffer buffer;
	private int lastSetCursor;
	private int cursor;
	private int xExtent, yExtent, zExtent;
	
	private VkBufferImageCopy.Buffer vkBufferCopy;
	private int bufferCopyCount;
	
	private @Nullable CommandBuffer flushCommandBuffer;
	private Work firstWork;
	private Work lastWork;
	
	public ImageWriter(VulkanBuffer srcBuffer, int srcOffset, Texture dstTexture, Vector3i imageOffset, Vector3i imageExtent, @Nullable CommandBuffer flushCommandBuffer) throws VulkanException
	{
		this(srcBuffer, srcOffset, dstTexture, imageOffset.x(), imageOffset.y(), imageOffset.z(), imageExtent.x(), imageExtent.y(), imageExtent.z(), flushCommandBuffer);
	}
	
	public ImageWriter(VulkanBuffer srcBuffer, int srcOffset, Texture dstTexture, int xOffset, int yOffset, int zOffset, int xExtent, int yExtent, int zExtent, @Nullable CommandBuffer flushCommandBuffer) throws VulkanException
	{
		this.srcBuffer = srcBuffer;
		this.srcOffset = srcOffset;
		this.dstTexture = dstTexture;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.zOffset = zOffset;
		this.xExtent = xExtent;
		this.yExtent = yExtent;
		this.zExtent = zExtent;
		this.flushCommandBuffer = flushCommandBuffer;
		cursor = 0;
		buffer = srcBuffer.getByteBuffer(srcOffset, xExtent * yExtent * zExtent * 4);
		vkBufferCopy = VkBufferImageCopy.malloc(MAX_COPY_COUNT);
		firstWork = lastWork = null;
		lastSetCursor = -1;
	}
	
	public ImageWriter setCursor(int x, int y, int z)
	{
		if (x < 0 || x >= xExtent) throw new IndexOutOfBoundsException(x);
		if (y < 0 || y >= yExtent) throw new IndexOutOfBoundsException(y);
		if (z < 0 || z >= zExtent) throw new IndexOutOfBoundsException(z);
		cursor = x + y * xExtent + z * xExtent * yExtent;
		lastSetCursor = cursor;
		return this;
	}
	
	private void registerCopy() throws VulkanException
	{
		if (lastSetCursor != -1 && lastSetCursor != cursor)
		{
			VkBufferImageCopy region = vkBufferCopy.get();
			region.bufferOffset(srcOffset);
			region.bufferRowLength(0);
			region.bufferImageHeight(0);
			region.imageSubresource().aspectMask(dstTexture.getAspectMask());
			region.imageSubresource().mipLevel(0);
			region.imageSubresource().baseArrayLayer(0);
			region.imageSubresource().layerCount(1);
			region.imageOffset().set(xOffset, yOffset, zOffset);
			region.imageExtent().set(xExtent, yExtent, zExtent);
			
			bufferCopyCount++;
			if (bufferCopyCount == MAX_COPY_COUNT) flush();
		}
	}
	
	public ImageWriter put(int value)
	{
		buffer.asIntBuffer().put(cursor, value);
		cursor += 1;
		return this;
	}
	
	public ImageWriter put(int... value)
	{
		buffer.asIntBuffer().put(cursor, value);
		cursor += value.length;
		return this;
	}
	
	public ImageWriter put(float value)
	{
		buffer.asFloatBuffer().put(cursor, value);
		cursor += 1;
		return this;
	}
	
	public ImageWriter put(float... value)
	{
		buffer.asFloatBuffer().put(cursor, value);
		cursor += value.length;
		return this;
	}
	
	public ImageWriter put(Vector2f value)
	{
		buffer.asFloatBuffer().put(cursor, value.getValuesArray());
		cursor += 2;
		return this;
	}
	
	public ImageWriter put(Vector3f value)
	{
		buffer.asFloatBuffer().put(cursor, value.getValuesArray());
		cursor += 3;
		return this;
	}
	
	public ImageWriter put(Vector4f value)
	{
		buffer.asFloatBuffer().put(cursor, value.getValuesArray());
		cursor += 4;
		return this;
	}
	
	public ImageWriter put(Vector2i value)
	{
		buffer.asIntBuffer().put(cursor, value.getValuesArray());
		cursor += 2;
		return this;
	}
	
	public ImageWriter put(Vector3i value)
	{
		buffer.asIntBuffer().put(cursor, value.getValuesArray());
		cursor += 3;
		return this;
	}
	
	public ImageWriter put(Vector4i value)
	{
		buffer.asIntBuffer().put(cursor, value.getValuesArray());
		cursor += 4;
		return this;
	}
	
	public ImageWriter put(Matrix4f value)
	{
		buffer.asFloatBuffer().put(cursor, value.toArray());
		cursor += 16;
		return this;
	}
	
	public void flush() throws VulkanException
	{
		if (lastSetCursor != -1 && lastSetCursor != cursor)
		{
			if (flushCommandBuffer != null)
			{
				flush(flushCommandBuffer);
				return;
			}
			registerCopy();
			Device device = srcBuffer.getDevice();
			Work work = new Work(device.configureAndSubmitSingleUseCommandBuffer(device.getGraphicsQueue(), this::flush));
			
			if (firstWork == null)
			{
				firstWork = lastWork = work;
			}
			else
			{
				lastWork.next = work;
				lastWork = work;
			}
		}
	}
	
	public void flush(CommandBuffer commandBuffer) throws VulkanException
	{
		if (lastSetCursor != -1 && lastSetCursor != cursor)
		{
			registerCopy();
			commandBuffer.copyBufferToImage(srcBuffer, dstTexture, vkBufferCopy.flip());
			vkBufferCopy.clear();
			bufferCopyCount = 0;
			lastSetCursor = cursor;
		}
	}
	
	public Future<?> getLoad()
	{
		return RenderEngine.getExecutor().submit(() -> {
			
			for (Work work = firstWork; work != null; work = work.next)
			{
				if (Thread.interrupted()) throw new CancellationException();
				work.work.get();
				firstWork = work;
			}
			return null;
		});
	}
	
	public int getSrcOffset()
	{
		return srcOffset;
	}
	
	public VulkanBuffer getSrcBuffer()
	{
		return srcBuffer;
	}
	
	public Texture getDstBuffer()
	{
		return dstTexture;
	}
	
	public void setCommandBuffer(CommandBuffer commandBuffer)
	{
		flushCommandBuffer = commandBuffer;
	}
	
	@Override
	public void close() throws VulkanException
	{
		flush:
		{
			if (lastSetCursor == -1)
			{
				if (cursor == 0) break flush;
				lastSetCursor = 0;
			}
			//	registerCopy();
			flush();
		}
		if (firstWork != null)
		{
			for (Work work = firstWork, work0 = work.next; work0 != null; work = work0, work0 = work0.next) work.next = null;
		}
		vkBufferCopy.free();
		srcBuffer.unmapBuffer();
	}
}
