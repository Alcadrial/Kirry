package com.alcadrial.renderengine.opengl.rendertest.object;

import java.util.Arrays;
import java.util.Map;

import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.renderengine.opengl.renderer.object.BufferableModel;
import com.alcadrial.renderengine.opengl.renderer.object.DataGetter;
import com.alcadrial.renderengine.opengl.shader.ShaderInput;

@SuppressWarnings("deprecation")
public class Quad implements BufferableModel {
	
	private Map<ShaderInput, float[]> map;
	private Vector2f start;
	private Vector2f size;
	
	public Quad(Vector2f start, Vector2f size)
	{
		this.start = start;
		this.size = size;
		map = Map.of(ShaderInput.VERTEX_POSITION, new float[] {
			start.x(), start.y() + size.y(), 0,
			start.x() + size.x(), start.y() + size.y(), 0,
			start.x() + size.x(), start.y(), 0,
			start.x(), start.y(), 0,
		}, ShaderInput.UV, new float[] {
			0, 1,
			1, 1,
			1, 0,
			0, 0,
		});
	}
	
	public Vector2f getStart()
	{
		return start;
	}
	
	public Vector2f getSize()
	{
		return size;
	}
	
	@Override
	public DataGetter getVertexData(int vertex, ShaderInput shaderInput)
	{
		int size = shaderInput.getType().getSize();
		float[] vertexData = Arrays.copyOfRange(map.getOrDefault(shaderInput, new float[size * vertices()]), vertex * size, vertex * size + size);
		return () -> vertexData;
	}
	
	@Override
	public int vertexSize()
	{
		return 12;
	}
	
	@Override
	public int vertices()
	{
		return 4;
	}
}
