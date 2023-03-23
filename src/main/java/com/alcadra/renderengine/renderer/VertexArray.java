package com.alcadra.renderengine.renderer;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL30;

import com.alcadra.renderengine.buffer.SimpleBuffer;

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
	
	private Map<Integer, SimpleBuffer> binds = new HashMap<>();
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
	
	public void bindBuffer(SimpleBuffer buffer)
	{
		bind();
		buffer.bind();
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
	
	public SimpleBuffer getBinded(int target)
	{
		SimpleBuffer b = binds.get(target);
		return b == null ? new SimpleBuffer(target, 0) {
			
			@Override
			public SimpleBuffer getNull()
			{
				return this;
			}
		} : b;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof VertexArray v && v.id == id;
	}
}
