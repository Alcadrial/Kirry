package com.alcadrial.renderengine.render.renderer;

import org.lwjgl.opengl.GL30;

public class VertexArray {
	
	public static final VertexArray NULL = new VertexArray(0);
	private static VertexArray binded = NULL;
	
	public static void unbind()
	{
		NULL.bind();
	}
	
	public static VertexArray getBinded()
	{
		return binded;
	}
	
	private final int id;
	
	public VertexArray()
	{
		this(GL30.glGenVertexArrays());
	}
	
	private VertexArray(int id)
	{
		this.id = id;
	}
	
	public void bind()
	{
		if (binded.id != id)
		{
			GL30.glBindVertexArray(id);
			binded = this;
		}
	}
	
	public void vertexAttributePointer(int index, int size, int type, boolean normalized, int stride, int offset)
	{
		GL30.glVertexAttribPointer(index, size, type, normalized, stride, offset);
	}
	
	public void enableVertexAttribute(int index)
	{
		GL30.glEnableVertexAttribArray(index);
	}
	
	public void disableVertexAttribute(int index)
	{
		GL30.glDisableVertexAttribArray(index);
	}
	
	public int getId()
	{
		return id;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof VertexArray v && v.id == id;
	}
}
