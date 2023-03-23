package com.alcadra.renderengine.renderer;

import java.util.function.Supplier;

import org.lwjgl.opengl.GL30;

import com.alcadra.renderengine.buffer.ArrayBuffer;
import com.alcadra.renderengine.buffer.ElementArrayBuffer;
import com.alcadra.renderengine.shader.Shader;
import com.alcadra.renderengine.util.Util;

public abstract class BaseRenderer<AB extends ArrayBuffer<?>> {
	
	private VertexArray vertexArray;
	private AB buffer;
	private ElementArrayBuffer elements;
	protected Shader shader;
	
	public BaseRenderer(Shader shader, VertexArray vertexArray, AB arrayBuffer, ElementArrayBuffer elementArrayBuffer)
	{
		this.shader = shader;
		this.vertexArray = vertexArray;
		buffer = arrayBuffer;
		elements = elementArrayBuffer;
	}
	
	public BaseRenderer(Supplier<Shader> shader, Supplier<? extends VertexArray> vertexArray, Supplier<? extends AB> arrayBuffer, Supplier<? extends ElementArrayBuffer> elementArrayBuffer)
	{
		this.shader = shader.get();
		this.vertexArray = vertexArray.get();
		buffer = arrayBuffer.get();
		elements = elementArrayBuffer.get();
	}
	
	protected void syncBuffers()
	{
		getVertexArray().bindBuffer(getArrayBuffer());
		Util.printError("BaseRenderer[INTERNAL] sync binding");
		getArrayBuffer().sync();
		Util.printError("BaseRenderer[INTERNAL] array sync");
	}
	
	protected void draw(int mode, int count)
	{
		getVertexArray().bind();
		syncBuffers();
		Util.printError("BaseRenderer[INTERNAL] sync");
		GL30.glDrawArrays(mode, 0, count);
		Util.printError("BaseRenderer[INTERNAL] draw");
	}
	
	protected final void drawIndices(int mode)
	{
		getVertexArray().bind();
		syncBuffers();
		getVertexArray().bindBuffer(getElementArrayBuffer());
		getElementArrayBuffer().sync();
		GL30.glDrawElements(mode, getElementArrayBuffer().size(), GL30.GL_UNSIGNED_INT, 0);
	}
	
	public void clear()
	{
		getArrayBuffer().clear();
		getElementArrayBuffer().clear();
	}
	
	protected VertexArray getVertexArray()
	{
		return vertexArray;
	}
	
	protected AB getArrayBuffer()
	{
		return buffer;
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
