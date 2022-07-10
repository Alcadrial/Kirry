package com.alcadra.renderengine.rendertest;

import com.alcadra.math.vector.Vector3f;
import com.alcadra.renderengine.buffer.Bufferable;
import com.alcadra.renderengine.renderer.object.VertexModifier;
import com.alcadra.renderengine.util.Test;

@Test
public class Cube implements Bufferable {
	
	private Vector3f start;
	private Vector3f end;
	private int[][] data;
	private int[] uvData;
	
	public Cube(Vector3f start, Vector3f end)
	{
		this.start = start;
		this.end = end;
		float[] dataFloat = new float[] {
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
		};
		data = new int[dataFloat.length / 3][];
		for (int i = 0; i < data.length; i++)
		{
			data[i] = new int[3];
			for (int j = 0; j < 3; j++) data[i][j] = Float.floatToIntBits(dataFloat[i * 3 + j]);
		}
		uvData = new int[] {
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
		};
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
	public int[][] getData()
	{
		return data;
	}
	
	@Override
	public int[] getVertexData(int index, VertexModifier modifier, int[] minimalData)
	{
		int[] data = new int[3 + modifier.getModifierSize()];
		data[0] = minimalData[0];
		data[1] = minimalData[1];
		data[2] = minimalData[2];
		modifier.uv(uvData[index * 2], uvData[index * 2 + 1]);
		return modifier.insertModifier(data, 3);
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
