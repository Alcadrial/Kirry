package com.alcadra.renderengine.util;

import com.alcadra.math.Matrix4f;
import com.alcadra.math.vector.Vector3f;
import com.alcadra.math.vector.Vector4f;
import com.alcadra.renderengine.renderer.universal.UniversalRenderer;

@Test
public class RendererUtil {
	
	public static void loadOrthoCube(UniversalRenderer renderer, Matrix4f matrix, Vector3f start, Vector3f end, Vector4f color)
	{
		loadOrthoCube(renderer, matrix.multiply(new Vector4f(start, 1)).xyz(), matrix.multiply(new Vector4f(end, 1)).xyz(), color);
	}
	
	public static void loadOrthoCube(UniversalRenderer renderer, Vector3f start, Vector3f end, Vector4f color)
	{
		loadOrthoCube(renderer, start.x(), start.y(), start.z(), end.x(), end.y(), end.z(), color.x(), color.y(), color.z(), color.w());
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
}
