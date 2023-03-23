package com.alcadra.renderengine.renderer;

import java.util.function.Supplier;

import org.lwjgl.opengl.GL30;

import com.alcadra.renderengine.buffer.ArrayBuffer;
import com.alcadra.renderengine.buffer.ElementArrayBuffer;
import com.alcadra.renderengine.shader.Shader;
import com.alcadra.renderengine.util.NoReplacement;
import com.alcadra.renderengine.util.ToRename;
import com.alcadra.renderengine.util.UseInstead;
import com.alcadra.renderengine.util.Util;

@ToRename(name = "BaseShaderRenderer")
public abstract class BaseRenderer<AB extends ArrayBuffer<?>> implements ShaderRenderer {
	
	protected final VertexArray vertexArray;
	protected final AB arrayBuffer;
	@Deprecated(since = "1.2", forRemoval = true)
	@UseInstead(value = {"BaseIndicesRenderer.elements"}, descriptions = {"For indices extend BaseIndicesRenderer instead"})
	protected ElementArrayBuffer elements;
	protected Shader shader;
	
	public BaseRenderer(Shader shader, VertexArray vertexArray, AB arrayBuffer)
	{
		this.shader = shader;
		this.vertexArray = vertexArray;
		this.arrayBuffer = arrayBuffer;
		elements = null;
	}
	
	@Deprecated(since = "1.2", forRemoval = true)
	@UseInstead(value = {"BaseRenderer(Shader, VertexArray, AB)"}, descriptions = {"For indices extend BaseIndicesRenderer instead"})
	public BaseRenderer(Shader shader, VertexArray vertexArray, AB arrayBuffer, ElementArrayBuffer elementArrayBuffer)
	{
		this.shader = shader;
		this.vertexArray = vertexArray;
		this.arrayBuffer = arrayBuffer;
		elements = elementArrayBuffer;
	}
	
	@Deprecated(since = "1.2", forRemoval = true)
	@NoReplacement
	public BaseRenderer(Supplier<Shader> shader, Supplier<? extends VertexArray> vertexArray, Supplier<? extends AB> arrayBuffer, Supplier<? extends ElementArrayBuffer> elementArrayBuffer)
	{
		this.shader = shader.get();
		this.vertexArray = vertexArray.get();
		this.arrayBuffer = arrayBuffer.get();
		elements = elementArrayBuffer.get();
	}
	
	protected void initializeArrayBuffer()
	{
		
	}
	
	protected void disableAttributes()
	{
		
	}
	
	@Override
	public void changeShader(Shader other)
	{
		disableAttributes();
		shader = other;
		initializeArrayBuffer();
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
	
	@Deprecated(since = "1.2", forRemoval = true)
	@UseInstead(value = {"BaseIndicesRenderer#drawIndices(int)"}, descriptions = {"For indices extend BaseIndicesRenderer instead"})
	protected void drawIndices(int mode)
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
	
	@Deprecated(since = "1.2", forRemoval = true)
	@NoReplacement
	protected VertexArray getVertexArray()
	{
		return vertexArray;
	}
	
	@Deprecated(since = "1.2", forRemoval = true)
	@NoReplacement
	protected AB getArrayBuffer()
	{
		return arrayBuffer;
	}
	
	@Deprecated(since = "1.2", forRemoval = true)
	@NoReplacement
	protected ElementArrayBuffer getElementArrayBuffer()
	{
		return elements;
	}
	
	@Override
	public Shader getShader()
	{
		return shader;
	}
}
