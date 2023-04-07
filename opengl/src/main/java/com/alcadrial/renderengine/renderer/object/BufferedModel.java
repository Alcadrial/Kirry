package com.alcadrial.renderengine.renderer.object;

import java.nio.FloatBuffer;

import com.alcadrial.renderengine.buffer.ArrayBuffer;
import com.alcadrial.renderengine.core.util.NotYetImplemented;
import com.alcadrial.renderengine.shader.ShaderInput;

@NotYetImplemented
public class BufferedModel extends ArrayBuffer<FloatBuffer> implements BufferableModel {
	
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
