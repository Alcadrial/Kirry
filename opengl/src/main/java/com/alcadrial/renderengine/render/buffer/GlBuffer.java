package com.alcadrial.renderengine.render.buffer;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryUtil;

import com.alcadrial.renderengine.core.buffer.SynchronizableArrayBuffer;

public abstract class GlBuffer extends SynchronizableArrayBuffer {
	
	public GlBuffer()
	{
		this(GL15.GL_ARRAY_BUFFER);
	}
	
	protected GlBuffer(int target)
	{
		super(target, GL15.glGenBuffers());
	}
	
	protected GlBuffer(int target, int id)
	{
		super(target, id);
	}
	
	@Override
	public void bind()
	{
		GL15.glBindBuffer(getTarget(), getId());
	}
	
	@Override
	public void unbind()
	{
		GL15.glBindBuffer(getTarget(), 0);
	}
	
	@Override
	public int getGpuByteSize()
	{
		return GL15.glGetBufferParameteri(getTarget(), GL15.GL_BUFFER_SIZE);
	}
	
	@Override
	public byte[] getGpuData()
	{
		ByteBuffer buffer = MemoryUtil.memAlloc(getGpuByteSize());
		GL15.glGetBufferSubData(getTarget(), 0, buffer);
		byte[] v = new byte[getGpuByteSize()];
		for (int i = 0; i < v.length; i++) v[i] = buffer.get(i);
		MemoryUtil.memFree(buffer);
		return v;
	}
	
	@Override
	public int[] getGpuIntData()
	{
		IntBuffer buffer = MemoryUtil.memAllocInt(getGpuByteSize() / 4);
		GL15.glGetBufferSubData(getTarget(), 0, buffer);
		int[] v = new int[buffer.limit()];
		for (int i = 0; i < v.length; i++) v[i] = buffer.get(i);
		MemoryUtil.memFree(buffer);
		return v;
	}
	
	@Override
	public float[] getGpuFloatData()
	{
		FloatBuffer buffer = MemoryUtil.memAllocFloat(getGpuByteSize() / 4);
		GL15.glGetBufferSubData(getTarget(), 0, buffer);
		float[] v = new float[buffer.limit()];
		for (int i = 0; i < v.length; i++) v[i] = buffer.get(i);
		MemoryUtil.memFree(buffer);
		return v;
	}
	
	@Override
	protected void syncRewrite(ByteBuffer buffer)
	{
		GL15.glBufferData(getTarget(), buffer, GL15.GL_DYNAMIC_DRAW);
	}
	
	@Override
	protected void syncModify(ByteBuffer buffer, int from, int to)
	{
		GL15.glBufferSubData(getTarget(), from, buffer.position((from - 1) * elementSize()).limit(to * elementSize()));
	}
	
	@Override
	public void close()
	{
		GL15.glDeleteBuffers(getId());
	}
}
