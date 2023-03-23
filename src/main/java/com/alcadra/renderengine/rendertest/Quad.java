package com.alcadra.renderengine.rendertest;

import com.alcadra.math.vector.Vector2f;
import com.alcadra.renderengine.buffer.Bufferable;
import com.alcadra.renderengine.renderer.object.VertexModifier;
import com.alcadra.renderengine.util.Test;

@Test
public class Quad implements Bufferable {
	
	private Vector2f start;
	private Vector2f size;
	private int[][] data;
	private int[] uvData;
	
	public Quad(Vector2f start, Vector2f size)
	{
		this.start = start;
		this.size = size;
		data = new int[][] {
			new int[] {Float.floatToIntBits(start.x()), Float.floatToIntBits(start.y() + size.y()), 0},
			new int[] {Float.floatToIntBits(start.x() + size.x()), Float.floatToIntBits(start.y() + size.y()), 0},
			new int[] {Float.floatToIntBits(start.x() + size.x()), Float.floatToIntBits(start.y()), 0},
			new int[] {Float.floatToIntBits(start.x()), Float.floatToIntBits(start.y()), 0},
		};
		uvData = new int[] {
			0, 1,
			1, 1,
			1, 0,
			0, 0,
		};
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
		return 4;
	}
}
