package com.alcadra.renderengine.renderer;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL30;

import com.alcadra.renderengine.buffer.SimpleBuffer;

public class VertexArray {
	
	private static final VertexArray NULL = new VertexArray(0);
	private static VertexArray binded = NULL;
	private Map<Integer, SimpleBuffer> binds = new HashMap<>();
	private int id;
	
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
		if (!getBinded(buffer.getTarget()).equals(buffer))
		{
			bind();
			buffer.bind();
			binds.put(buffer.getTarget(), buffer);
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
	
	public int getId()
	{
		return id;
	}
	
	public SimpleBuffer getBinded(int target)
	{
		SimpleBuffer b = binds.get(target);
		return b == null ? SimpleBuffer.getNull() : b;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof VertexArray v && v.id == id;
	}
	
	public static void unbind()
	{
		NULL.bind();
	}
	
	public static VertexArray getNull()
	{
		return NULL;
	}
	
	public static VertexArray getBinded()
	{
		return binded;
	}
}
