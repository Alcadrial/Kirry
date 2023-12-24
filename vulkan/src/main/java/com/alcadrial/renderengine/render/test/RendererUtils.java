package com.alcadrial.renderengine.render.test;

import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.renderengine.core.util.color.Color;
import com.alcadrial.renderengine.render.Renderer;
import com.alcadrial.renderengine.render.VertexAccumulator;
import com.alcadrial.renderengine.render.VertexAccumulator.VertexResult;
import com.alcadrial.renderengine.render.shader.ShaderInput;
import com.alcadrial.renderengine.render.util.Tracker;

public class RendererUtils {
	
	public static void loadOrthoCube(Renderer renderer, Matrix4f matrix, Vector3f start, Vector3f end, Color color)
	{
		loadOrthoCube(renderer, matrix.multiply(start.xyz1()).xyz(), matrix.multiply(end.xyz1()).xyz(), color);
	}
	
	public static void loadOrthoCube(Renderer renderer, Vector3f start, Vector3f end, Color color)
	{
		loadOrthoCube(renderer, start.x(), start.y(), start.z(), end.x(), end.y(), end.z(), color.r(), color.g(), color.b(), color.a());
	}
	
	public static void loadOrthoCube(Renderer renderer, float sX, float sY, float sZ, float eX, float eY, float eZ, float r, float g, float b, float a)
	{
		VertexAccumulator accumulator = renderer.accumulate();
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
	
	public static OrthoCubeVertexResult loadOrthoCube(Renderer renderer, Vector3f start, Vector3f end, BiFunction<Integer, Integer, Color> color)
	{
		float sX = start.x();
		float sY = start.y();
		float sZ = start.z();
		float eX = end.x();
		float eY = end.y();
		float eZ = end.z();
		
		VertexAccumulator accumulator = renderer.accumulate();
		VertexResult r0 = loadVertex(accumulator, sX, eY, sZ, color.apply(0, 0), 0, 0, -1);
		VertexResult r1 = loadVertex(accumulator, eX, eY, sZ, color.apply(0, 1), 0, 0, -1);
		VertexResult r2 = loadVertex(accumulator, eX, sY, sZ, color.apply(0, 2), 0, 0, -1);
		VertexResult r3 = loadVertex(accumulator, sX, sY, sZ, color.apply(0, 3), 0, 0, -1);
		RectangleVertexResult rr0 = new RectangleVertexResult(r0, r1, r2, r3);
		
		r0 = loadVertex(accumulator, sX, eY, eZ, color.apply(1, 0), -1, 0, 0);
		r1 = loadVertex(accumulator, sX, eY, sZ, color.apply(1, 1), -1, 0, 0);
		r2 = loadVertex(accumulator, sX, sY, sZ, color.apply(1, 2), -1, 0, 0);
		r3 = loadVertex(accumulator, sX, sY, eZ, color.apply(1, 3), -1, 0, 0);
		RectangleVertexResult rr1 = new RectangleVertexResult(r0, r1, r2, r3);
		
		r0 = loadVertex(accumulator, eX, eY, eZ, color.apply(2, 0), 0, 0, 1);
		r1 = loadVertex(accumulator, sX, eY, eZ, color.apply(2, 1), 0, 0, 1);
		r2 = loadVertex(accumulator, sX, sY, eZ, color.apply(2, 2), 0, 0, 1);
		r3 = loadVertex(accumulator, eX, sY, eZ, color.apply(2, 3), 0, 0, 1);
		RectangleVertexResult rr2 = new RectangleVertexResult(r0, r1, r2, r3);
		
		r0 = loadVertex(accumulator, eX, eY, sZ, color.apply(3, 0), 1, 0, 0);
		r1 = loadVertex(accumulator, eX, eY, eZ, color.apply(3, 1), 1, 0, 0);
		r2 = loadVertex(accumulator, eX, sY, eZ, color.apply(3, 2), 1, 0, 0);
		r3 = loadVertex(accumulator, eX, sY, sZ, color.apply(3, 3), 1, 0, 0);
		RectangleVertexResult rr3 = new RectangleVertexResult(r0, r1, r2, r3);
		
		r0 = loadVertex(accumulator, sX, eY, eZ, color.apply(4, 0), 0, 1, 0);
		r1 = loadVertex(accumulator, eX, eY, eZ, color.apply(4, 1), 0, 1, 0);
		r2 = loadVertex(accumulator, eX, eY, sZ, color.apply(4, 2), 0, 1, 0);
		r3 = loadVertex(accumulator, sX, eY, sZ, color.apply(4, 3), 0, 1, 0);
		RectangleVertexResult rr4 = new RectangleVertexResult(r0, r1, r2, r3);
		
		r0 = loadVertex(accumulator, sX, sY, sZ, color.apply(5, 0), 0, -1, 0);
		r1 = loadVertex(accumulator, eX, sY, sZ, color.apply(5, 1), 0, -1, 0);
		r2 = loadVertex(accumulator, eX, sY, eZ, color.apply(5, 2), 0, -1, 0);
		r3 = loadVertex(accumulator, sX, sY, eZ, color.apply(5, 3), 0, -1, 0);
		RectangleVertexResult rr5 = new RectangleVertexResult(r0, r1, r2, r3);
		accumulator.end();
		return new OrthoCubeVertexResult(rr0, rr1, rr2, rr3, rr4, rr5);
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
		
		public void track(ShaderInput input, Consumer<Tracker<int[]>> action)
		{
			for (RectangleVertexResult r : result) r.track(input, action);
		}
	}
	
	public static VertexResult loadVertex(VertexAccumulator accumulator, float x, float y, float z, Color color, float nX, float nY, float nZ)
	{
		return accumulator.vertex(x, y, z).color(color.r(), color.g(), color.b(), color.a()).normal(nX, nY, nZ).end();
	}
	
	public static void loadRectangle(VertexAccumulator accumulator, Matrix4f matrix, Vector3f start, Vector3f end, float z, Color color)
	{
		loadRectangle(accumulator, matrix.multiply(start.xyz1()).xy(), matrix.multiply(end.xyz1()).xy(), z, color);
	}
	
	public static void loadRectangle(VertexAccumulator accumulator, Vector2f start, Vector2f end, float z, Color color)
	{
		loadRectangleXYZRGBA(accumulator, start.x(), start.y(), end.x(), end.y(), z, color.r(), color.g(), color.b(), color.a());
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
		
		private VertexResult[] result;
		
		public RectangleVertexResult(VertexResult r0, VertexResult r1, VertexResult r2, VertexResult r3)
		{
			result = new VertexResult[4];
			result[0] = r0;
			result[1] = r1;
			result[2] = r2;
			result[3] = r3;
		}
		
		public VertexResult getR0()
		{
			return result[0];
		}
		
		public VertexResult getR1()
		{
			return result[1];
		}
		
		public VertexResult getR2()
		{
			return result[2];
		}
		
		public VertexResult getR3()
		{
			return result[3];
		}
		
		public void foreach(Consumer<VertexResult> action)
		{
			for (VertexResult r : result) action.accept(r);
		}
		
		public void track(ShaderInput input, Consumer<Tracker<int[]>> action)
		{
			for (VertexResult r : result) action.accept(r.track(input));
		}
	}
	
	public static RectangleVertexResult loadRectangleXYZRGBA(VertexAccumulator accumulator, float sX, float sY, float eX, float eY, float z, float r, float g, float b, float a)
	{
		VertexResult r0 = accumulator.vertex(sX, eY, z).color(r, g, b, a).end();
		VertexResult r1 = accumulator.vertex(eX, eY, z).color(r, g, b, a).end();
		VertexResult r2 = accumulator.vertex(eX, sY, z).color(r, g, b, a).end();
		VertexResult r3 = accumulator.vertex(sX, sY, z).color(r, g, b, a).end();
		return new RectangleVertexResult(r0, r1, r2, r3);
	}
	
	public static RectangleVertexResult sendRectangleXYZRGBA(Renderer renderer, float sX, float sY, float eX, float eY, float z, float r, float g, float b, float a)
	{
		VertexAccumulator accumulator = renderer.accumulate();
		VertexResult r0 = accumulator.vertex(sX, eY, z).color(r, g, b, a).end();
		VertexResult r1 = accumulator.vertex(eX, eY, z).color(r, g, b, a).end();
		VertexResult r2 = accumulator.vertex(eX, sY, z).color(r, g, b, a).end();
		VertexResult r3 = accumulator.vertex(sX, sY, z).color(r, g, b, a).end();
		accumulator.end();
		return new RectangleVertexResult(r0, r1, r2, r3);
	}
	
	public static RectangleVertexResult loadRectangleXYZUV(VertexAccumulator accumulator, float sX, float sY, float eX, float eY, float z, float u0, float v0, float u1, float v1)
	{
		VertexResult r0 = accumulator.vertex(sX, eY, z).uv(u1, v0).end();
		VertexResult r1 = accumulator.vertex(eX, eY, z).uv(u0, v0).end();
		VertexResult r2 = accumulator.vertex(eX, sY, z).uv(u0, v1).end();
		VertexResult r3 = accumulator.vertex(sX, sY, z).uv(u1, v1).end();
		return new RectangleVertexResult(r0, r1, r2, r3);
	}
	
	public static RectangleVertexResult sendRectangleXYZUV(Renderer renderer, float sX, float sY, float eX, float eY, float z, float u0, float v0, float u1, float v1)
	{
		VertexAccumulator accumulator = renderer.accumulate();
		VertexResult r0 = accumulator.vertex(sX, eY, z).uv(u1, v0).end();
		VertexResult r1 = accumulator.vertex(eX, eY, z).uv(u0, v0).end();
		VertexResult r2 = accumulator.vertex(eX, sY, z).uv(u0, v1).end();
		VertexResult r3 = accumulator.vertex(sX, sY, z).uv(u1, v1).end();
		accumulator.end();
		return new RectangleVertexResult(r0, r1, r2, r3);
	}
}
