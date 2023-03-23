package com.alcadra.renderengine.complex;

import com.alcadra.math.Math;
import com.alcadra.math.vector.Vector2f;
import com.alcadra.renderengine.renderer.universal.UniversalRenderer;
import com.alcadra.renderengine.util.Broken;
import com.alcadra.renderengine.util.NotYetImplemented;
import com.alcadra.renderengine.util.color.Color;

public class RhodoneaCurve {
	
	@NotYetImplemented
	public static void renderCurve(UniversalRenderer renderer, Vector2f center, float size, float ratio, float rotation, float precision, Color color)
	{
		if (ratio % 1 == 0) renderCurve(renderer, center, size, (int) ratio, rotation, precision, color);
		if (ratio == 0) return;
		
		precision = 1 / precision;
		
	}
	
	@Broken
	public static void renderCurve(UniversalRenderer renderer, Vector2f center, float size, @Broken int ratio, float rotation, float precision, Color color)
	{
		if (ratio == 0) return;
		
		float rounds = ratio % 2 == 0 ? ratio * 2 * Math.PIF : ratio * Math.PIF;
		float precision0 = rounds / precision;
		for (float i = 0; i < precision; i++)
		{
			Vector2f point = next(center, size, i * 2 * Math.PIF / precision + rotation, i * precision0);
			renderer.vertex(point.x(), point.y(), point.z()).color(color.r(), color.g(), color.b(), color.a()).end();
		}
	}
	
	private static Vector2f next(Vector2f center, float size, float radians, float delta)
	{
		float radius = size / 2;
		Vector2f v = Vector2f.of(-radius * Math.sin(radians), -radius * Math.cos(radians));
		Vector2f w = Vector2f.of(radius * Math.sin(radians), radius * Math.cos(radians));
		return Math.lerp(v, w, Math.cos(delta * 2)).add(center);
	}
}
