package com.alcadrial.renderengine.opengl.rendertest.object;

import java.util.Arrays;
import java.util.Map;

import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.renderengine.opengl.renderer.object.BufferableModel;
import com.alcadrial.renderengine.opengl.renderer.object.DataGetter;
import com.alcadrial.renderengine.opengl.shader.ShaderInput;

@SuppressWarnings("deprecation")
public class Cube implements BufferableModel {
	
	private Map<ShaderInput, float[]> map;
	private Vector3f start;
	private Vector3f end;
	
	public Cube(Vector3f start, Vector3f end)
	{
		this.start = start;
		this.end = end;
		map = Map.of(ShaderInput.VERTEX_POSITION, new float[] {
			start.x(), end.y(), start.z(),
			end.x(), end.y(), start.z(),
			end.x(), start.y(), start.z(),
			start.x(), start.y(), start.z(),
			
			end.x(), start.y(), start.z(),
			start.x(), start.y(), start.z(),
			start.x(), start.y(), end.z(),
			end.x(), start.y(), end.z(),
			
			start.x(), start.y(), end.z(),
			end.x(), start.y(), end.z(),
			end.x(), end.y(), end.z(),
			start.x(), end.y(), end.z(),
			
			start.x(), end.y(), end.z(),
			start.x(), end.y(), start.z(),
			end.x(), end.y(), start.z(),
			start.x(), end.y(), start.z(),
			
			start.x(), end.y(), end.z(),
			start.x(), end.y(), start.z(),
			start.x(), start.y(), start.z(),
			start.x(), start.y(), end.z(),
			
			end.x(), end.y(), end.z(),
			end.x(), start.y(), end.z(),
			end.x(), start.y(), start.z(),
			end.x(), end.y(), start.z(),
		}, ShaderInput.UV, new float[] {
			0, 1,
			1, 1,
			1, 0,
			0, 0,
			
			0, 1,
			1, 1,
			1, 0,
			0, 0,
			
			0, 1,
			1, 1,
			1, 0,
			0, 0,
			
			0, 1,
			1, 1,
			1, 0,
			0, 0,
			
			0, 1,
			1, 1,
			1, 0,
			0, 0,
			
			0, 1,
			1, 1,
			1, 0,
			0, 0,
		});
	}
	
	public Vector3f getStart()
	{
		return start;
	}
	
	public Vector3f getEnd()
	{
		return end;
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
		return 24;
	}
}
