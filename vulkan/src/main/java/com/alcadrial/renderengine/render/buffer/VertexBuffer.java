package com.alcadrial.renderengine.render.buffer;

import java.util.concurrent.Future;

import org.lwjgl.vulkan.VK10;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.kector.vector.Vector3i;
import com.alcadrial.kector.vector.Vector4f;
import com.alcadrial.kector.vector.Vector4i;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;

public class VertexBuffer extends VulkanDoubleBuffer {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private long offset;
	private int cursor;
	
	public VertexBuffer(float[] data)
	{
		this(data.length);
		asFloatBuffer().put(data);
		unmapBuffer();
	}
	
	public VertexBuffer(int size)
	{
		this(RenderEngine.getVulkan().getDevice(), size);
	}
	
	public VertexBuffer(int size, int extraUsage)
	{
		this(RenderEngine.getVulkan().getDevice(), size, extraUsage);
	}
	
	public VertexBuffer(int size, int extraUsage, boolean bidirectional)
	{
		this(RenderEngine.getVulkan().getDevice(), size, extraUsage, bidirectional);
	}
	
	public VertexBuffer(Device device, int size)
	{
		this(device, size, 0);
	}
	
	public VertexBuffer(Device device, int size, int extraUsage)
	{
		this(device, size, extraUsage, false);
	}
	
	public VertexBuffer(Device device, int size, int extraUsage, boolean bidirectional)
	{
		super(device, size * 4, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | extraUsage, bidirectional);
		offset = 0;
		cursor = 0;
	}
	
	public VertexBuffer(VertexBuffer buffer, long offset, int size)
	{
		super(buffer, offset * 4, size * 4);
	}
	
	public long getOffset()
	{
		return offset;
	}
	
	public void setOffset(long offset)
	{
		this.offset = offset;
	}
	
	@Override
	public synchronized Future<?> sendToPhysicalDevice()
	{
		return super.sendToPhysicalDevice(0, size);
	}
	
	@Override
	public synchronized Future<?> sendToPhysicalDevice(long dstBuffer)
	{
		return super.sendToPhysicalDevice(0, size, dstBuffer);
	}
	
	@Override
	public synchronized Future<?> sendToPhysicalDevice(long offset, int size)
	{
		return super.sendToPhysicalDevice(offset * 4, size * 4);
	}
	
	public void resetCursor()
	{
		cursor = 0;
	}
	
	public void setCursor(int index)
	{
		if (index < 0 || index >= size) throw new IndexOutOfBoundsException(index);
		cursor = index;
	}
	
	public void put(int value)
	{
		asIntBuffer().put(cursor, value);
		cursor += 1;
	}
	
	public void put(float value)
	{
		asFloatBuffer().put(cursor, value);
		cursor += 1;
	}
	
	public void put(Vector2f value)
	{
		asFloatBuffer().put(cursor, value.getValuesArray());
		cursor += 2;
	}
	
	public void put(Vector3f value)
	{
		asFloatBuffer().put(cursor, value.getValuesArray());
		cursor += 3;
	}
	
	public void put(Vector4f value)
	{
		asFloatBuffer().put(cursor, value.getValuesArray());
		cursor += 4;
	}
	
	public void put(Vector2i value)
	{
		asIntBuffer().put(cursor, value.getValuesArray());
		cursor += 2;
	}
	
	public void put(Vector3i value)
	{
		asIntBuffer().put(cursor, value.getValuesArray());
		cursor += 3;
	}
	
	public void put(Vector4i value)
	{
		asIntBuffer().put(cursor, value.getValuesArray());
		cursor += 4;
	}
	
	public void put(Matrix4f value)
	{
		asFloatBuffer().put(cursor, value.toArray());
		cursor += 16;
	}
}
