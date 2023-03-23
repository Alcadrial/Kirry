package com.alcadra.renderengine.renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.lwjgl.opengl.GL30;

import com.alcadra.renderengine.Launch;
import com.alcadra.renderengine.buffer.ArrayBuffer;
import com.alcadra.renderengine.buffer.ElementArrayBuffer;
import com.alcadra.renderengine.shader.Shader;

public abstract class BaseRenderer {
	
	private VertexArray vertexArray;
	private ArrayBuffer buffer;
	private ElementArrayBuffer elements;
	protected Shader shader;
	
	public BaseRenderer(Shader shader, VertexArray vertexArray, ArrayBuffer arrayBuffer, ElementArrayBuffer elementArrayBuffer)
	{
		this.shader = shader;
		this.vertexArray = vertexArray;
		buffer = arrayBuffer;
		elements = elementArrayBuffer;
	}
	
	public BaseRenderer(Supplier<Shader> shader, Supplier<? extends VertexArray> vertexArray, Supplier<? extends ArrayBuffer> arrayBuffer, Supplier<? extends ElementArrayBuffer> elementArrayBuffer)
	{
		this.shader = shader.get();
		this.vertexArray = vertexArray.get();
		buffer = arrayBuffer.get();
		elements = elementArrayBuffer.get();
	}
	
	protected <E> List<E> concat(List<List<E>> arrays, List<Integer> sizes)
	{
		List<E> array = new ArrayList<>(arrays.size());
		for (int i = 0; i < arrays.size(); i++) for (int j = 0; j < arrays.get(0).size(); i++) for (int k = 0; k < sizes.get(i); k++) array.add(arrays.get(j).get(i + k));
		return array;
	}
	
	protected void syncBuffers()
	{
		getVertexArray().bindBuffer(getArrayBuffer());
		Launch.printError("BaseRenderer[INTERNAL] sync binding");
		getArrayBuffer().sync();
		Launch.printError("BaseRenderer[INTERNAL] array sync");
	}
	
	protected void draw(int mode)
	{
		getVertexArray().bind();
		syncBuffers();
		Launch.printError("BaseRenderer[INTERNAL] sync");
		GL30.glDrawArrays(mode, 0, getArrayBuffer().size());
		Launch.printError("BaseRenderer[INTERNAL] draw");
	}
	
	protected final void drawIndices(int mode)
	{
		getVertexArray().bind();
		syncBuffers();
		getVertexArray().bindBuffer(getElementArrayBuffer());
		getElementArrayBuffer().sync();
		GL30.glDrawElements(mode, getElementArrayBuffer().size(), GL30.GL_UNSIGNED_INT, 0);
	}
	
	protected VertexArray getVertexArray()
	{
		return vertexArray;
	}
	
	protected ArrayBuffer getArrayBuffer()
	{
		return buffer;
	}
	
	protected ElementArrayBuffer getElementArrayBuffer()
	{
		return elements;
	}
}
