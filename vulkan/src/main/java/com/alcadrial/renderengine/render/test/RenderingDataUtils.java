package com.alcadrial.renderengine.render.test;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.renderengine.core.util.Nullable;
import com.alcadrial.renderengine.core.util.color.Color;
import com.alcadrial.renderengine.render.Renderer;
import com.alcadrial.renderengine.render.VertexAccumulator;
import com.alcadrial.renderengine.render.buffer.VertexBuffer;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;

public class RenderingDataUtils {
	
	public static void loadOrthoCube(Renderer renderer, VertexBuffer vertexBuffer, Matrix4f matrix, Vector3f start, Vector3f end, Color color)
	{
		loadOrthoCube(renderer, vertexBuffer, matrix.multiply(start.xyz1()).xyz(), matrix.multiply(end.xyz1()).xyz(), color);
	}
	
	public static void loadOrthoCube(Renderer renderer, VertexBuffer vertexBuffer, Vector3f start, Vector3f end, Color color)
	{
		loadOrthoCube(renderer, vertexBuffer, start.x(), start.y(), start.z(), end.x(), end.y(), end.z(), color.r(), color.g(), color.b(), color.a());
	}
	
	public static void loadOrthoCube(Renderer renderer, VertexBuffer vertexBuffer, float sX, float sY, float sZ, float eX, float eY, float eZ, float r, float g, float b, float a)
	{
		VertexAccumulator accumulator = renderer.accumulate(vertexBuffer);
		accumulator.vertex(sX, eY, sZ).color(r, g, b, a).normal(0, 0, -1).end();
		accumulator.vertex(eX, eY, sZ).color(r, g, b, a).normal(0, 0, -1).end();
		accumulator.vertex(eX, sY, sZ).color(r, g, b, a).normal(0, 0, -1).end();
		accumulator.vertex(sX, sY, sZ).color(r, g, b, a).normal(0, 0, -1).end();
		
		accumulator.vertex(sX, eY, eZ).color(r, g, b, a).normal(-1, 0, 0).end();
		accumulator.vertex(sX, eY, sZ).color(r, g, b, a).normal(-1, 0, 0).end();
		accumulator.vertex(sX, sY, sZ).color(r, g, b, a).normal(-1, 0, 0).end();
		accumulator.vertex(sX, sY, eZ).color(r, g, b, a).normal(-1, 0, 0).end();
		
		accumulator.vertex(eX, eY, eZ).color(r, g, b, a).normal(0, 0, 1).end();
		accumulator.vertex(sX, eY, eZ).color(r, g, b, a).normal(0, 0, 1).end();
		accumulator.vertex(sX, sY, eZ).color(r, g, b, a).normal(0, 0, 1).end();
		accumulator.vertex(eX, sY, eZ).color(r, g, b, a).normal(0, 0, 1).end();
		
		accumulator.vertex(eX, eY, sZ).color(r, g, b, a).normal(1, 0, 0).end();
		accumulator.vertex(eX, eY, eZ).color(r, g, b, a).normal(1, 0, 0).end();
		accumulator.vertex(eX, sY, eZ).color(r, g, b, a).normal(1, 0, 0).end();
		accumulator.vertex(eX, sY, sZ).color(r, g, b, a).normal(1, 0, 0).end();
		
		accumulator.vertex(sX, eY, eZ).color(r, g, b, a).normal(0, 1, 0).end();
		accumulator.vertex(eX, eY, eZ).color(r, g, b, a).normal(0, 1, 0).end();
		accumulator.vertex(eX, eY, sZ).color(r, g, b, a).normal(0, 1, 0).end();
		accumulator.vertex(sX, eY, sZ).color(r, g, b, a).normal(0, 1, 0).end();
		
		accumulator.vertex(sX, sY, sZ).color(r, g, b, a).normal(0, -1, 0).end();
		accumulator.vertex(eX, sY, sZ).color(r, g, b, a).normal(0, -1, 0).end();
		accumulator.vertex(eX, sY, eZ).color(r, g, b, a).normal(0, -1, 0).end();
		accumulator.vertex(sX, sY, eZ).color(r, g, b, a).normal(0, -1, 0).end();
		accumulator.end();
	}
	
	public static void loadOrthoCube(VulkanBuffer vertexBuffer, int vertexBufferOffset, Vector3f start, Vector3f end, BiFunction<Integer, Integer, Color> color)
	{
		loadOrthoCube(vertexBuffer, vertexBufferOffset, null, 0, 0, start, end, color);
	}
	
	public static void loadOrthoCube(VulkanBuffer vertexBuffer, int vertexBufferOffset, @Nullable VulkanBuffer indexBuffer, int indexBufferOffset, int indexOffset, Vector3f start, Vector3f end, BiFunction<Integer, Integer, Color> color)
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
		if (indices != null) loadRectangleIndices(indices, indexOffset);
		
		loadVertex(buffer, sX, eY, eZ, color.apply(1, 0), -1, 0, 0);
		loadVertex(buffer, sX, eY, sZ, color.apply(1, 1), -1, 0, 0);
		loadVertex(buffer, sX, sY, sZ, color.apply(1, 2), -1, 0, 0);
		loadVertex(buffer, sX, sY, eZ, color.apply(1, 3), -1, 0, 0);
		if (indices != null) loadRectangleIndices(indices, indexOffset += 4);
		
		loadVertex(buffer, eX, eY, eZ, color.apply(2, 0), 0, 0, 1);
		loadVertex(buffer, sX, eY, eZ, color.apply(2, 1), 0, 0, 1);
		loadVertex(buffer, sX, sY, eZ, color.apply(2, 2), 0, 0, 1);
		loadVertex(buffer, eX, sY, eZ, color.apply(2, 3), 0, 0, 1);
		if (indices != null) loadRectangleIndices(indices, indexOffset += 4);
		
		loadVertex(buffer, eX, eY, sZ, color.apply(3, 0), 1, 0, 0);
		loadVertex(buffer, eX, eY, eZ, color.apply(3, 1), 1, 0, 0);
		loadVertex(buffer, eX, sY, eZ, color.apply(3, 2), 1, 0, 0);
		loadVertex(buffer, eX, sY, sZ, color.apply(3, 3), 1, 0, 0);
		if (indices != null) loadRectangleIndices(indices, indexOffset += 4);
		
		loadVertex(buffer, sX, eY, eZ, color.apply(4, 0), 0, 1, 0);
		loadVertex(buffer, eX, eY, eZ, color.apply(4, 1), 0, 1, 0);
		loadVertex(buffer, eX, eY, sZ, color.apply(4, 2), 0, 1, 0);
		loadVertex(buffer, sX, eY, sZ, color.apply(4, 3), 0, 1, 0);
		if (indices != null) loadRectangleIndices(indices, indexOffset += 4);
		
		loadVertex(buffer, sX, sY, sZ, color.apply(5, 0), 0, -1, 0);
		loadVertex(buffer, eX, sY, sZ, color.apply(5, 1), 0, -1, 0);
		loadVertex(buffer, eX, sY, eZ, color.apply(5, 2), 0, -1, 0);
		loadVertex(buffer, sX, sY, eZ, color.apply(5, 3), 0, -1, 0);
		if (indices != null) loadRectangleIndices(indices, indexOffset += 4);
	}
	
	private static void loadRectangleIndices(IntBuffer buffer, int startIndex)
	{
		buffer.put(startIndex).put(startIndex + 1).put(startIndex + 3);
		buffer.put(startIndex + 1).put(startIndex + 2).put(startIndex + 3);
	}
	
	public static class OrthoCubeVertexResult {
		
		private RectangleVertexResult[] result;
		
		public OrthoCubeVertexResult(RectangleVertexResult r0, RectangleVertexResult r1, RectangleVertexResult r2, RectangleVertexResult r3, RectangleVertexResult r4, RectangleVertexResult r5)
		{
			result = new RectangleVertexResult[6];
			result[0] = r0;
			result[1] = r1;
			result[2] = r2;
			result[3] = r3;
			result[4] = r4;
			result[5] = r5;
		}
		
		public void foreach(Consumer<RectangleVertexResult> action)
		{
			for (RectangleVertexResult r : result) action.accept(r);
		}
	}
	
	private static void loadVertex(FloatBuffer vertexBuffer, float x, float y, float z, Color color, float nX, float nY, float nZ)
	{
		vertexBuffer.put(x).put(y).put(z).put(color.r() / 255).put(color.g() / 255).put(color.b() / 255).put(color.a() / 255).put(nX).put(nY).put(nZ);
	}
	
	public static void loadVertex(VulkanBuffer vertexBuffer, int offset, float x, float y, float z, Color color, float nX, float nY, float nZ)
	{
		loadVertex(vertexBuffer.asFloatBuffer().position(offset), x, y, z, color, nX, nY, nZ);
	}
	
	public static void loadRectangle(VulkanBuffer vertexBuffer, Matrix4f matrix, Vector3f start, Vector3f end, float z, Color color)
	{
		loadRectangle(vertexBuffer, matrix.multiply(start.xyz1()).xy(), matrix.multiply(end.xyz1()).xy(), z, color);
	}
	
	public static void loadRectangle(VulkanBuffer vertexBuffer, Vector2f start, Vector2f end, float z, Color color)
	{
		loadRectangleXYZRGBA(vertexBuffer, start.x(), start.y(), end.x(), end.y(), z, color.r(), color.g(), color.b(), color.a());
	}
	
	/**
	 * <pre>
	 * R0       R1
	 *  *-------*
	 *  |       |
	 *  |       |
	 *  |       |
	 *  |       |
	 *  *-------*
	 * R3       R2
	 * </pre>
	 */
	public static class RectangleVertexResult {
		
		private int[] result;
		
		public RectangleVertexResult(int r0, int r1, int r2, int r3)
		{
			result = new int[4];
			result[0] = r0;
			result[1] = r1;
			result[2] = r2;
			result[3] = r3;
		}
		
		public int getR0()
		{
			return result[0];
		}
		
		public int getR1()
		{
			return result[1];
		}
		
		public int getR2()
		{
			return result[2];
		}
		
		public int getR3()
		{
			return result[3];
		}
		
		public void foreach(Consumer<Integer> action)
		{
			for (int r : result) action.accept(r);
		}
	}
	
	public static void loadRectangleXYZRGBA(VulkanBuffer vertexBuffer, float sX, float sY, float eX, float eY, float z, float r, float g, float b, float a)
	{
		FloatBuffer buffer = vertexBuffer.asFloatBuffer();
		buffer.put(sX).put(eY).put(z).put(r).put(g).put(b).put(a);
		buffer.put(eX).put(eY).put(z).put(r).put(g).put(b).put(a);
		buffer.put(eX).put(sY).put(z).put(r).put(g).put(b).put(a);
		buffer.put(sX).put(sY).put(z).put(r).put(g).put(b).put(a);
	}
	
	public static void loadRectangleXYZUV(VulkanBuffer vertexBuffer, float sX, float sY, float eX, float eY, float z, float u0, float v0, float u1, float v1)
	{
		FloatBuffer buffer = vertexBuffer.asFloatBuffer();
		buffer.put(sX).put(eY).put(z).put(u0).put(v0);
		buffer.put(eX).put(eY).put(z).put(u1).put(v0);
		buffer.put(eX).put(sY).put(z).put(u1).put(v1);
		buffer.put(sX).put(sY).put(z).put(u0).put(v1);
	}
}
