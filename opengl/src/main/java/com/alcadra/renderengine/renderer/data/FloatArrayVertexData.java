package com.alcadra.renderengine.renderer.data;

import java.util.Map;

import com.alcadra.renderengine.shader.ShaderInput;

public class FloatArrayVertexData implements VertexData {
	
	private final Map<ShaderInput, float[]> data;
	private final float[] vertexData;
	
	public FloatArrayVertexData(Map<ShaderInput, float[]> data, int vertexSize)
	{
		this.data = Map.copyOf(data);
		vertexData = new float[vertexSize];
		int i = 0;
		for (float[] entry : this.data.values()) for (float element : entry) vertexData[i++] = element;
	}
	
	@Override
	public float getData(int index)
	{
		return vertexData[index];
	}
	
	@Override
	public int getLength()
	{
		return vertexData.length;
	}
}
