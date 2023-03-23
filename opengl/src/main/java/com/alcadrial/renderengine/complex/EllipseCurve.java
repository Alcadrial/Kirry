package com.alcadrial.renderengine.complex;

import com.alcadra.math.Math;
import com.alcadra.math.vector.Vector2f;
import com.alcadrial.renderengine.renderer.universal.UniversalRenderer;
import com.alcadrial.renderengine.util.color.Color;

public class EllipseCurve {
	
	public static void renderCurve(UniversalRenderer renderer, Vector2f center, float width, float height, float precision, Color color)
	{
		if (width == 0 || height == 0) return;
		
		float p2 = Math.PIF * 2;
		float step = p2 / precision;
		float halfWidth = width / 2;
		float halfHeight = height / 2;
		for (float i = 0; i < p2; i += step)
		{
			Vector2f point = next(center, halfWidth, halfHeight, i);
			renderer.vertex(point.x(), point.y(), point.z()).color(color.r(), color.g(), color.b(), color.a()).end();
		}
		renderer.vertex(center.x(), center.y() + halfHeight, center.z()).color(color.r(), color.g(), color.b(), color.a()).end();
		renderer.vertex(center.x(), center.y() + halfHeight, center.z()).color(color.r(), color.g(), color.b(), color.a()).end();
	}
	
	private static Vector2f next(Vector2f center, float halfWidth, float halfHeight, float delta)
	{
		return Vector2f.of(halfWidth * Math.sin(delta), halfHeight * Math.cos(delta)).add(center);
	}
}
