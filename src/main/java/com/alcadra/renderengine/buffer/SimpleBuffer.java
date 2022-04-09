package com.alcadra.renderengine.buffer;

import org.lwjgl.opengl.GL15;

public class SimpleBuffer {
	
	private static final SimpleBuffer NULL = new SimpleBuffer(0, 0);
	private int target, id;
	
	public SimpleBuffer()
	{
		this(GL15.GL_ARRAY_BUFFER);
	}
	
	protected SimpleBuffer(int target)
	{
		this(GL15.GL_ARRAY_BUFFER, GL15.glGenBuffers());
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
	
	public int getTarget()
	{
		return target;
	}
	
	public int getId()
	{
		return id;
	}
	
	public static SimpleBuffer getNull()
	{
		return NULL;
	}
}
