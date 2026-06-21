package com.alcadrial.kirry.render.util;

import java.math.RoundingMode;

import com.alcadrial.kector.Math;
import com.alcadrial.kector.vector.Vector3fr;
import com.alcadrial.kirry.render.KirryVulkan;
import com.alcadrial.kirry.render.buffer.writer.BufferWriter;
import com.alcadrial.kirry.render.util.exception.VulkanException;

public class SphereDataCollector {
	
	static
	{
		KirryVulkan.assertLoaded();
	}
	
	public static void collect(BufferWriter vertexBuffer, BufferWriter indexBuffer, Vector3fr origin, float radius, int scan) throws VulkanException
	{
		if (radius < 0) throw new IllegalArgumentException("Negative radius");
		if (scan <= 0) throw new IllegalArgumentException("Non positive scan");
		
		if (radius == 0)
		{
			vertexBuffer.put(origin);
			return;
		}
		
		int parallelCount = 2 * scan - 1;
		
		float hp = Math.HALF_PIF;
		float yPass = hp / scan;
		float xPass = 2 * yPass;
		int meridianCount = Math.toInt(2 * Math.PI / xPass, RoundingMode.UP);
		
		int vertexCount = meridianCount * parallelCount + 2;
		int indexCount = 3 * meridianCount * (3 * parallelCount - 2);
		
		if (vertexBuffer.getSize() < vertexCount * 3 * 4) throw new OverflowException("VertexBuffer capacity required: " + vertexCount * 3 + ", available: " + vertexBuffer.getSize() / 4);
		if (indexBuffer.getSize() < indexCount * 4) throw new OverflowException("IndexBuffer capacity required: " + indexCount + ", available: " + indexBuffer.getSize() / 4);
		
		Vector3fr[][] vertexMatrix = new Vector3fr[parallelCount + 2][];
		
		vertexMatrix[0] = new Vector3fr[] {Vector3fr.of(origin.x(), origin.y() - radius, origin.z())};
		for (int i = 1; i < parallelCount + 1; i++)
		{
			float angle = yPass * i - hp;
			float y = radius * Math.sin(angle);
			float r = radius * Math.cos(angle);
			vertexMatrix[i] = getRingVertices(origin, 0, r, meridianCount, y);
		}
		vertexMatrix[parallelCount + 1] = new Vector3fr[] {Vector3fr.of(origin.x(), origin.y() + radius, origin.z())};
		
		for (Vector3fr[] ring : vertexMatrix) for (Vector3fr vertex : ring) vertexBuffer.put(vertex);
		
		for (int i = 1; i < meridianCount; i++) indexBuffer.put(0, i, i + 1);
		indexBuffer.put(0, meridianCount, 1);
		
		int offset = 1;
		int offsetExtra = offset + meridianCount;
		
		for (int i = 1; i < parallelCount; i++)
		{
			for (int j = 0; j < meridianCount - 1; j++)
			{
				indexBuffer.put(offset + j, offsetExtra + j, offsetExtra + j + 1);
				indexBuffer.put(offset + j + 1, offset + j, offsetExtra + j + 1);
			}
			indexBuffer.put(offsetExtra - 1, offsetExtra + meridianCount - 1, offsetExtra);
			indexBuffer.put(offset, offsetExtra - 1, offsetExtra);
			offset = offsetExtra;
			offsetExtra += meridianCount;
		}
		
		for (int i = 1; i < meridianCount; i++) indexBuffer.put(offset + i, offsetExtra, offset + i - 1);
		indexBuffer.put(offset, offsetExtra, offsetExtra - 1);
	}
	
	private static Vector3fr[] getRingVertices(Vector3fr origin, float offset, float radius, int vertexCount, float y)
	{
		y += origin.y();
		Vector3fr[] vertexArray = new Vector3fr[vertexCount];
		float pass = (float) (2 * Math.PI / vertexCount);
		offset %= pass;
		for (int i = 0; i < vertexCount; i++)
		{
			float angle = offset + pass * i;
			vertexArray[i] = Vector3fr.of(origin.x() + radius * Math.cos(angle), y, origin.z() + radius * Math.sin(angle));
		}
		return vertexArray;
	}
}
