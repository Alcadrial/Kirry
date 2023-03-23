package com.alcadra.renderengine.renderer.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alcadra.renderengine.shader.ShaderInput;

public class FloatListVertexData implements VertexData {
	
	private final Map<ShaderInput, float[]> data;
	private final List<Float> vertexData;
	
	public FloatListVertexData(Map<ShaderInput, float[]> data, int vertexSize)
	{
		this.data = Map.copyOf(data);
		vertexData = new ArrayList<>(vertexSize);
		for (float[] entry : this.data.values()) for (float f : entry) vertexData.add(f);
	}
	
	@Override
	public float getData(int index)
	{
		return vertexData.get(index);
	}
	
	@Override
	public int getLength()
	{
		return vertexData.size();
	}
}
