package com.alcadrial.renderengine.render.test;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.function.BiFunction;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.renderengine.core.util.Nullable;
import com.alcadrial.renderengine.core.util.color.Color;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;

public class RenderingDataUtils {
	
	public static void loadOrthoCube(VulkanBuffer vertexBuffer, int vertexBufferOffset, Vector3f start, Vector3f end, BiFunction<Integer, Integer, Color> color)
	{
		loadOrthoCube(vertexBuffer, vertexBufferOffset, null, 0, 0, start, end, color);
	}
	
	public static void loadOrthoCube(VulkanBuffer vertexBuffer, int vertexBufferOffset, @Nullable VulkanBuffer indexBuffer, int indexBufferOffset, int startIndex, Vector3f start, Vector3f end, BiFunction<Integer, Integer, Color> color)
	{
		IntBuffer indices = null;
		if (indexBuffer != null)
		{
			indices = indexBuffer.asIntBuffer();
			indices.position(indexBufferOffset);
		}
		
		float sX = start.x();
		float sY = start.y();
		float sZ = start.z();
		float eX = end.x();
		float eY = end.y();
		float eZ = end.z();
		
		FloatBuffer buffer = vertexBuffer.asFloatBuffer();
		loadVertex(buffer, sX, eY, sZ, color.apply(0, 0), 0, 0, -1);
		loadVertex(buffer, eX, eY, sZ, color.apply(0, 1), 0, 0, -1);
		loadVertex(buffer, eX, sY, sZ, color.apply(0, 2), 0, 0, -1);
		loadVertex(buffer, sX, sY, sZ, color.apply(0, 3), 0, 0, -1);
		if (indices != null) loadRectangleIndices(indices, startIndex);
		
		loadVertex(buffer, sX, eY, eZ, color.apply(1, 0), -1, 0, 0);
		loadVertex(buffer, sX, eY, sZ, color.apply(1, 1), -1, 0, 0);
		loadVertex(buffer, sX, sY, sZ, color.apply(1, 2), -1, 0, 0);
		loadVertex(buffer, sX, sY, eZ, color.apply(1, 3), -1, 0, 0);
		if (indices != null) loadRectangleIndices(indices, startIndex += 4);
		
		loadVertex(buffer, eX, eY, eZ, color.apply(2, 0), 0, 0, 1);
		loadVertex(buffer, sX, eY, eZ, color.apply(2, 1), 0, 0, 1);
		loadVertex(buffer, sX, sY, eZ, color.apply(2, 2), 0, 0, 1);
		loadVertex(buffer, eX, sY, eZ, color.apply(2, 3), 0, 0, 1);
		if (indices != null) loadRectangleIndices(indices, startIndex += 4);
		
		loadVertex(buffer, eX, eY, sZ, color.apply(3, 0), 1, 0, 0);
		loadVertex(buffer, eX, eY, eZ, color.apply(3, 1), 1, 0, 0);
		loadVertex(buffer, eX, sY, eZ, color.apply(3, 2), 1, 0, 0);
		loadVertex(buffer, eX, sY, sZ, color.apply(3, 3), 1, 0, 0);
		if (indices != null) loadRectangleIndices(indices, startIndex += 4);
		
		loadVertex(buffer, sX, eY, eZ, color.apply(4, 0), 0, 1, 0);
		loadVertex(buffer, eX, eY, eZ, color.apply(4, 1), 0, 1, 0);
		loadVertex(buffer, eX, eY, sZ, color.apply(4, 2), 0, 1, 0);
		loadVertex(buffer, sX, eY, sZ, color.apply(4, 3), 0, 1, 0);
		if (indices != null) loadRectangleIndices(indices, startIndex += 4);
		
		loadVertex(buffer, sX, sY, sZ, color.apply(5, 0), 0, -1, 0);
		loadVertex(buffer, eX, sY, sZ, color.apply(5, 1), 0, -1, 0);
		loadVertex(buffer, eX, sY, eZ, color.apply(5, 2), 0, -1, 0);
		loadVertex(buffer, sX, sY, eZ, color.apply(5, 3), 0, -1, 0);
		if (indices != null) loadRectangleIndices(indices, startIndex += 4);
	}
	
	public static void loadRectangleIndices(IntBuffer buffer, int startIndex)
	{
		buffer.put(startIndex).put(startIndex + 1).put(startIndex + 3);
		buffer.put(startIndex + 1).put(startIndex + 2).put(startIndex + 3);
	}
	
	public static void loadVertex(FloatBuffer vertexBuffer, float x, float y, float z, float... extraData)
	{
		vertexBuffer.put(x).put(y).put(z).put(extraData);
	}
	
	private static void loadVertex(FloatBuffer vertexBuffer, float x, float y, float z, Color color, float nX, float nY, float nZ)
	{
		loadVertex(vertexBuffer, x, y, z, color.r() / 255, color.g() / 255, color.b() / 255, color.a() / 255, nX, nY, nZ);
	}
	
	public static void loadRectangle(FloatBuffer vertexBuffer, Matrix4f matrix, Vector3f start, Vector3f end, float z, Color color)
	{
		loadRectangle(vertexBuffer, matrix.multiply(start.xyz1()).xy(), matrix.multiply(end.xyz1()).xy(), z, color);
	}
	
	public static void loadRectangle(FloatBuffer vertexBuffer, Vector2f start, Vector2f end, float z, Color color)
	{
		loadRectangleColor(vertexBuffer, start.x(), start.y(), end.x(), end.y(), z, color.r() / 255, color.g() / 255, color.b() / 255, color.a() / 255);
	}
	
	public static void loadRectangleColor(FloatBuffer vertexBuffer, float sX, float sY, float eX, float eY, float z, float r, float g, float b, float a)
	{
		loadVertex(vertexBuffer, sX, eY, z, r, g, b, a);
		loadVertex(vertexBuffer, eX, eY, z, r, g, b, a);
		loadVertex(vertexBuffer, eX, sY, z, r, g, b, a);
		loadVertex(vertexBuffer, sX, sY, z, r, g, b, a);
	}
	
	public static void loadRectangleColor(FloatBuffer vertexBuffer, IntBuffer indexBuffer, int startIndex, float sX, float sY, float eX, float eY, float z, float r, float g, float b, float a)
	{
		loadVertex(vertexBuffer, sX, eY, z, r, g, b, a);
		loadVertex(vertexBuffer, eX, eY, z, r, g, b, a);
		loadVertex(vertexBuffer, eX, sY, z, r, g, b, a);
		loadVertex(vertexBuffer, sX, sY, z, r, g, b, a);
		loadRectangleIndices(indexBuffer, startIndex);
	}
	
	public static void loadRectangleUv(FloatBuffer vertexBuffer, float sX, float sY, float eX, float eY, float z, float u0, float v0, float u1, float v1)
	{
		loadVertex(vertexBuffer, sX, eY, z, u0, v0);
		loadVertex(vertexBuffer, eX, eY, z, u1, v0);
		loadVertex(vertexBuffer, eX, sY, z, u1, v1);
		loadVertex(vertexBuffer, sX, sY, z, u0, v1);
	}
	
	public static void loadRectangleUv(FloatBuffer vertexBuffer, IntBuffer indexBuffer, int startIndex, float sX, float sY, float eX, float eY, float z, float u0, float v0, float u1, float v1)
	{
		loadVertex(vertexBuffer, sX, eY, z, u0, v0);
		loadVertex(vertexBuffer, eX, eY, z, u1, v0);
		loadVertex(vertexBuffer, eX, sY, z, u1, v1);
		loadVertex(vertexBuffer, sX, sY, z, u0, v1);
		loadRectangleIndices(indexBuffer, startIndex);
	}
}
