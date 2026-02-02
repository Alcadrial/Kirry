package com.alcadrial.renderengine.render.buffer;

import java.nio.ByteBuffer;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.kector.vector.Vector3i;
import com.alcadrial.kector.vector.Vector4f;
import com.alcadrial.kector.vector.Vector4i;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.commandbuffer.CommandBuffer;
import com.alcadrial.renderengine.render.util.VulkanFuture;

final class NullBufferWriter extends BufferWriter {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	static final NullBufferWriter INSTANCE = new NullBufferWriter();
	
	private NullBufferWriter()
	{
		
	}
	
	@Override
	public BufferWriter resetCursor()
	{
		return this;
	}
	
	@Override
	public BufferWriter setCursor(int index)
	{
		return this;
	}
	
	@Override
	public BufferWriter put(int value)
	{
		return this;
	}
	
	@Override
	public BufferWriter put(int... values)
	{
		return this;
	}
	
	@Override
	public BufferWriter put(byte... values)
	{
		return this;
	}
	
	@Override
	public BufferWriter put(ByteBuffer value)
	{
		return this;
	}
	
	@Override
	public BufferWriter put(byte[] value, int offset, int size)
	{
		return this;
	}
	
	@Override
	public BufferWriter put(int[] value, int offset, int size)
	{
		return this;
	}
	
	@Override
	public BufferWriter put(float value)
	{
		return this;
	}
	
	@Override
	public BufferWriter put(float... value)
	{
		return this;
	}
	
	@Override
	public BufferWriter put(Vector2f value)
	{
		return this;
	}
	
	@Override
	public BufferWriter put(Vector3f value)
	{
		return this;
	}
	
	@Override
	public BufferWriter put(Vector4f value)
	{
		return this;
	}
	
	@Override
	public BufferWriter put(Vector2i value)
	{
		return this;
	}
	
	@Override
	public BufferWriter put(Vector3i value)
	{
		return this;
	}
	
	@Override
	public BufferWriter put(Vector4i value)
	{
		return this;
	}
	
	@Override
	public BufferWriter put(Matrix4f value)
	{
		return this;
	}
	
	@Override
	public void flush()
	{
		
	}
	
	@Override
	public void flush(CommandBuffer commandBuffer)
	{
		
	}
	
	@Override
	public VulkanFuture<?> getLoad()
	{
		return RenderEngine.completedTask();
	}
	
	@Override
	public void setCommandBuffer(CommandBuffer commandBuffer)
	{
		
	}
	
	@Override
	public void close()
	{
		
	}
}
