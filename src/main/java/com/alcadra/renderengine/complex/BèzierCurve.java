package com.alcadra.renderengine.complex;

import com.alcadra.math.Math;
import com.alcadra.math.vector.Vector2f;
import com.alcadra.renderengine.renderer.universal.UniversalRenderer;
import com.alcadra.renderengine.util.color.Color;

public class BèzierCurve {
	
	public static void renderCurve(UniversalRenderer renderer, float precision, Color color, Vector2f... points)
	{
		int length = points.length;
		if (length == 0) return;
		if (length == 1)
		{
			renderer.vertex(points[0].x(), points[0].y(), points[0].z()).color(color.r(), color.g(), color.b(), color.a()).end();
			renderer.vertex(points[0].x(), points[0].y(), points[0].z()).color(color.r(), color.g(), color.b(), color.a()).end();
			return;
		}
		if (length == 2)
		{
			renderer.vertex(points[0].x(), points[0].y(), points[0].z()).color(color.r(), color.g(), color.b(), color.a()).end();
			renderer.vertex(points[1].x(), points[1].y(), points[1].z()).color(color.r(), color.g(), color.b(), color.a()).end();
			return;
		}
		precision = 1 / precision;
		for (float i = 0; i < 1; i += precision)
		{
			Vector2f point = next(i, points);
			renderer.vertex(point.x(), point.y(), point.z()).color(color.r(), color.g(), color.b(), color.a()).end();
		}
		Vector2f last = points[points.length - 1];
		renderer.vertex(last.x(), last.y(), last.z()).color(color.r(), color.g(), color.b(), color.a()).end();
		renderer.vertex(last.x(), last.y(), last.z()).color(color.r(), color.g(), color.b(), color.a()).end();
		renderer.vertex(last.x(), last.y(), last.z()).color(color.r(), color.g(), color.b(), color.a()).end();
	}
	
	private static Vector2f next(float delta, Vector2f... points)
	{
		if (points.length == 2) return Math.lerp(points[0], points[1], delta);
		Vector2f[] nextPoints = new Vector2f[points.length - 1];
		for (int i = 0; i < nextPoints.length; i++) nextPoints[i] = Math.lerp(points[i], points[i + 1], delta);
		return next(delta, nextPoints);
	}
}
