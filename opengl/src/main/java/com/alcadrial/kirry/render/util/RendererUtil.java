package com.alcadrial.kirry.render.util;

import java.util.function.Consumer;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.kirry.core.util.Test;
import com.alcadrial.kirry.core.util.color.Color;
import com.alcadrial.kirry.render.renderer.VertexResult;
import com.alcadrial.kirry.render.renderer.universal.UniversalRenderer;
import com.alcadrial.kirry.render.shader.ShaderInput;

@Test
public class RendererUtil {
	
	public static void loadOrthoCube(UniversalRenderer renderer, Matrix4f matrix, Vector3f start, Vector3f end, Color color)
	{
		loadOrthoCube(renderer, matrix.multiply(start.xyz1()).xyz(), matrix.multiply(end.xyz1()).xyz(), color);
	}
	
	public static void loadOrthoCube(UniversalRenderer renderer, Vector3f start, Vector3f end, Color color)
	{
		loadOrthoCube(renderer, start.x(), start.y(), start.z(), end.x(), end.y(), end.z(), color.r(), color.g(), color.b(), color.a());
	}
	
	public static void loadOrthoCube(UniversalRenderer renderer, float sX, float sY, float sZ, float eX, float eY, float eZ, float r, float g, float b, float a)
	{
		renderer.vertex(sX, eY, sZ).color(r, g, b, a).normal(0, 0, -1).end();
		renderer.vertex(eX, eY, sZ).color(r, g, b, a).normal(0, 0, -1).end();
		renderer.vertex(eX, sY, sZ).color(r, g, b, a).normal(0, 0, -1).end();
		renderer.vertex(sX, sY, sZ).color(r, g, b, a).normal(0, 0, -1).end();
		
		renderer.vertex(sX, eY, eZ).color(r, g, b, a).normal(-1, 0, 0).end();
		renderer.vertex(sX, eY, sZ).color(r, g, b, a).normal(-1, 0, 0).end();
		renderer.vertex(sX, sY, sZ).color(r, g, b, a).normal(-1, 0, 0).end();
		renderer.vertex(sX, sY, eZ).color(r, g, b, a).normal(-1, 0, 0).end();
		
		renderer.vertex(eX, eY, eZ).color(r, g, b, a).normal(0, 0, 1).end();
		renderer.vertex(sX, eY, eZ).color(r, g, b, a).normal(0, 0, 1).end();
		renderer.vertex(sX, sY, eZ).color(r, g, b, a).normal(0, 0, 1).end();
		renderer.vertex(eX, sY, eZ).color(r, g, b, a).normal(0, 0, 1).end();
		
		renderer.vertex(eX, eY, sZ).color(r, g, b, a).normal(1, 0, 0).end();
		renderer.vertex(eX, eY, eZ).color(r, g, b, a).normal(1, 0, 0).end();
		renderer.vertex(eX, sY, eZ).color(r, g, b, a).normal(1, 0, 0).end();
		renderer.vertex(eX, sY, sZ).color(r, g, b, a).normal(1, 0, 0).end();
		
		renderer.vertex(sX, eY, eZ).color(r, g, b, a).normal(0, 1, 0).end();
		renderer.vertex(eX, eY, eZ).color(r, g, b, a).normal(0, 1, 0).end();
		renderer.vertex(eX, eY, sZ).color(r, g, b, a).normal(0, 1, 0).end();
		renderer.vertex(sX, eY, sZ).color(r, g, b, a).normal(0, 1, 0).end();
		
		renderer.vertex(sX, sY, sZ).color(r, g, b, a).normal(0, -1, 0).end();
		renderer.vertex(eX, sY, sZ).color(r, g, b, a).normal(0, -1, 0).end();
		renderer.vertex(eX, sY, eZ).color(r, g, b, a).normal(0, -1, 0).end();
		renderer.vertex(sX, sY, eZ).color(r, g, b, a).normal(0, -1, 0).end();
	}
	
	public static void loadRectangle(UniversalRenderer renderer, Matrix4f matrix, Vector3f start, Vector3f end, float z, Color color)
	{
		loadRectangle(renderer, matrix.multiply(start.xyz1()).xy(), matrix.multiply(end.xyz1()).xy(), z, color);
	}
	
	public static void loadRectangle(UniversalRenderer renderer, Vector2f start, Vector2f end, float z, Color color)
	{
		loadRectangle(renderer, start.x(), start.y(), end.x(), end.y(), z, color.r(), color.g(), color.b(), color.a());
	}
	
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
	
	public static RectangleVertexResult loadRectangle(UniversalRenderer renderer, float sX, float sY, float eX, float eY, float z, float r, float g, float b, float a)
	{
		VertexResult r0 = renderer.vertex(sX, eY, z).color(r, g, b, a).end();
		VertexResult r1 = renderer.vertex(eX, eY, z).color(r, g, b, a).end();
		VertexResult r2 = renderer.vertex(eX, sY, z).color(r, g, b, a).end();
		VertexResult r3 = renderer.vertex(sX, sY, z).color(r, g, b, a).end();
		return new RectangleVertexResult(r0, r1, r2, r3);
	}
	
	public static void loadTexturedRectangle(UniversalRenderer renderer, float sX, float sY, float eX, float eY, float z, float u0, float v0, float u1, float v1)
	{
		renderer.vertex(sX, eY, z).uv(u0, v1).end();
		renderer.vertex(eX, eY, z).uv(u1, v1).end();
		renderer.vertex(eX, sY, z).uv(u1, v0).end();
		renderer.vertex(sX, sY, z).uv(u0, v0).end();
	}
}
