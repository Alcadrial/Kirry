package com.alcadra.renderengine.renderer.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alcadra.renderengine.shader.ShaderInput;

public class FloatListVertexData implements VertexData {
	
	private final Map<ShaderInput, List<Float>> data;
	private final List<Float> vertexData;
	
	public FloatListVertexData(Map<ShaderInput, List<Float>> data, int vertexSize)
	{
		this.data = Map.copyOf(data);
		vertexData = new ArrayList<>(vertexSize);
		for (List<Float> entry : this.data.values()) vertexData.addAll(entry);
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
