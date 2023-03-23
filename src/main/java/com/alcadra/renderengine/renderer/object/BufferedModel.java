package com.alcadra.renderengine.renderer.object;

import java.nio.FloatBuffer;

import com.alcadra.renderengine.buffer.ArrayBufferUpdate;
import com.alcadra.renderengine.shader.ShaderInput;

public class BufferedModel extends ArrayBufferUpdate<FloatBuffer> implements BufferableModel {
	
	@Override
	public DataGetter getVertexData(int vertex, ShaderInput shaderInput)
	{
		return null;
	}
	
	@Override
	public int vertexSize()
	{
		return 0;
	}
	
	@Override
	public int vertices()
	{
		return 0;
	}
	
	@Override
	public int size()
	{
		return 0;
	}
	
	@Override
	public int byteSize()
	{
		return 0;
	}
	
	@Override
	public void clear()
	{}
	
	@Override
	protected int elementSize()
	{
		return 0;
	}
	
}
