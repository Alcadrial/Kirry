package com.alcadra.renderengine.renderer;

import java.util.function.Supplier;

import org.lwjgl.opengl.GL30;

import com.alcadra.renderengine.buffer.ArrayBuffer;
import com.alcadra.renderengine.buffer.ElementArrayBuffer;
import com.alcadra.renderengine.shader.Shader;
import com.alcadra.renderengine.util.Util;

public abstract class BaseRenderer<AB extends ArrayBuffer<?>> {
	
	private VertexArray vertexArray;
	private AB arrayBuffer;
	private ElementArrayBuffer elements;
	protected Shader shader;
	
	public BaseRenderer(Shader shader, VertexArray vertexArray, AB arrayBuffer, ElementArrayBuffer elementArrayBuffer)
	{
		this.shader = shader;
		this.vertexArray = vertexArray;
		this.arrayBuffer = arrayBuffer;
		elements = elementArrayBuffer;
	}
	
	public BaseRenderer(Supplier<Shader> shader, Supplier<? extends VertexArray> vertexArray, Supplier<? extends AB> arrayBuffer, Supplier<? extends ElementArrayBuffer> elementArrayBuffer)
	{
		this.shader = shader.get();
		this.vertexArray = vertexArray.get();
		this.arrayBuffer = arrayBuffer.get();
		elements = elementArrayBuffer.get();
	}
	
	protected void syncBuffers()
	{
		vertexArray.bindBuffer(arrayBuffer);
		Util.printError("BaseRenderer[INTERNAL] sync binding");
		arrayBuffer.sync();
		Util.printError("BaseRenderer[INTERNAL] array sync");
	}
	
	protected void draw(int mode, int count)
	{
		vertexArray.bind();
		syncBuffers();
		Util.printError("BaseRenderer[INTERNAL] sync");
		GL30.glDrawArrays(mode, 0, count);
		Util.printError("BaseRenderer[INTERNAL] draw");
	}
	
	protected final void drawIndices(int mode)
	{
		vertexArray.bind();
		syncBuffers();
		vertexArray.bindBuffer(elements);
		elements.sync();
		GL30.glDrawElements(mode, elements.size(), GL30.GL_UNSIGNED_INT, 0);
	}
	
	public void clear()
	{
		arrayBuffer.clear();
		if (elements != null) elements.clear();
	}
	
	protected VertexArray getVertexArray()
	{
		return vertexArray;
	}
	
	protected AB getArrayBuffer()
	{
		return arrayBuffer;
	}
	
	protected ElementArrayBuffer getElementArrayBuffer()
	{
		return elements;
	}
	
	public Shader getShader()
	{
		return shader;
	}
}
