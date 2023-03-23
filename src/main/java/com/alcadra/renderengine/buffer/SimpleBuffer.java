package com.alcadra.renderengine.buffer;

import org.lwjgl.opengl.GL15;

public abstract class SimpleBuffer implements AutoCloseable {
	
	private final int target, id;
	
	public SimpleBuffer()
	{
		this(GL15.GL_ARRAY_BUFFER);
	}
	
	protected SimpleBuffer(int target)
	{
		this(target, GL15.glGenBuffers());
	}
	
	protected SimpleBuffer(int target, int id)
	{
		this.target = target;
		this.id = id;
	}
	
	public void bind()
	{
		GL15.glBindBuffer(getTarget(), getId());
	}
	
	public void asBinded(Runnable runnable)
	{
		bind();
		runnable.run();
		unbind();
	}
	
	public void unbind()
	{
		GL15.glBindBuffer(getTarget(), 0);
	}
	
	public int getTarget()
	{
		return target;
	}
	
	public int getId()
	{
		return id;
	}
	
	@Override
	public void close()
	{
		GL15.glDeleteBuffers(id);
	}
}
