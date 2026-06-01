package com.alcadrial.renderengine.render.buffer.writer;

import java.nio.ByteBuffer;

import org.lwjgl.vulkan.VK10;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.kector.vector.Vector3i;
import com.alcadrial.kector.vector.Vector4f;
import com.alcadrial.kector.vector.Vector4i;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.commandbuffer.CommandBuffer;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public interface BufferWriter {
	
	static BufferWriter nullWriter()
	{
		return NullBufferWriter.INSTANCE;
	}
	
	static void writeData(CommandBuffer commandBuffer, ByteBuffer data, VulkanBuffer dst) throws VulkanException
	{
		VulkanBuffer src = new VulkanBuffer(dst.getDevice(), data.remaining(), VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, 0);
		DeferredBufferWriter writer = new DeferredBufferWriter(src, dst);
		
		src.getMemory().mapMemory();
		writer.put(data);
		src.getMemory().unmapMemory();
		writer.transferAll(commandBuffer);
		commandBuffer.addCompletionAction(() -> src.close());
	}
	
	static void writeData(CommandBuffer commandBuffer, int[] data, VulkanBuffer dst) throws VulkanException
	{
		VulkanBuffer src = new VulkanBuffer(dst.getDevice(), data.length, VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, 0);
		DeferredBufferWriter writer = new DeferredBufferWriter(src, dst);
		
		src.getMemory().mapMemory();
		writer.put(data);
		src.getMemory().unmapMemory();
		writer.transferAll(commandBuffer);
		commandBuffer.addCompletionAction(() -> src.close());
	}
	
	static void writeData(CommandBuffer commandBuffer, byte[] data, VulkanBuffer dst) throws VulkanException
	{
		VulkanBuffer src = new VulkanBuffer(dst.getDevice(), data.length, VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, 0);
		DeferredBufferWriter writer = new DeferredBufferWriter(src, dst);
		
		src.getMemory().mapMemory();
		writer.put(data);
		src.getMemory().unmapMemory();
		writer.transferAll(commandBuffer);
		commandBuffer.addCompletionAction(() -> src.close());
	}
	
	BufferWriter resetCursor() throws VulkanException;
	
	BufferWriter setCursor(int index) throws VulkanException;
	
	default BufferWriter put(ByteBuffer values)
	{
		return put(values, 0, values.limit());
	}
	
	BufferWriter put(ByteBuffer values, int offset, int size);
	
	default BufferWriter put(byte... values)
	{
		return put(values, 0, values.length);
	}
	
	BufferWriter put(byte[] values, int offset, int size);
	
	default BufferWriter put(int... values)
	{
		return put(values, 0, values.length);
	}
	
	BufferWriter put(int[] values, int offset, int size);
	
	default BufferWriter put(float... values)
	{
		return put(values, 0, values.length);
	}
	
	BufferWriter put(float[] values, int offset, int size);
	
	default BufferWriter put(Vector2f value)
	{
		return put(value.getValuesArray());
	}
	
	default BufferWriter put(Vector3f value)
	{
		return put(value.getValuesArray());
	}
	
	default BufferWriter put(Vector4f value)
	{
		return put(value.getValuesArray());
	}
	
	default BufferWriter put(Vector2i value)
	{
		return put(value.getValuesArray());
	}
	
	default BufferWriter put(Vector3i value)
	{
		return put(value.getValuesArray());
	}
	
	default BufferWriter put(Vector4i value)
	{
		return put(value.getValuesArray());
	}
	
	default BufferWriter put(Matrix4f value)
	{
		return put(value.toArray());
	}
	
	int getSrcOffset();
	int getDstOffset();
	
	VulkanBuffer getSrcBuffer();
	VulkanBuffer getDstBuffer();
	
	int getSize();
	
}
