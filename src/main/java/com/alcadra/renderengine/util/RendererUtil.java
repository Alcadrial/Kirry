package com.alcadra.renderengine.util;

import com.alcadra.math.Matrix4f;
import com.alcadra.math.vector.Vector2f;
import com.alcadra.math.vector.Vector3f;
import com.alcadra.renderengine.renderer.universal.UniversalRenderer;
import com.alcadra.renderengine.util.color.Color;

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
	
	public static void loadRectangle(UniversalRenderer renderer, float sX, float sY, float eX, float eY, float z, float r, float g, float b, float a)
	{
		renderer.vertex(sX, eY, z).color(r, g, b, a).end();
		renderer.vertex(eX, eY, z).color(r, g, b, a).end();
		renderer.vertex(eX, sY, z).color(r, g, b, a).end();
		renderer.vertex(sX, sY, z).color(r, g, b, a).end();
	}
	
	public static void loadTexturedRectangle(UniversalRenderer renderer, float sX, float sY, float eX, float eY, float z, float u0, float v0, float u1, float v1)
	{
		renderer.vertex(sX, eY, z).uv(u0, v1).end();
		renderer.vertex(eX, eY, z).uv(u1, v1).end();
		renderer.vertex(eX, sY, z).uv(u1, v0).end();
		renderer.vertex(sX, sY, z).uv(u0, v0).end();
	}
}
