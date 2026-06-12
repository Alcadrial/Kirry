package com.alcadrial.kirry.render.complex;

import com.alcadrial.kector.Math;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kirry.core.util.color.Color;
import com.alcadrial.kirry.render.renderer.universal.UniversalRenderer;

public class KochCurve {
	
	public static void renderCurve(UniversalRenderer renderer, Vector2f start, Vector2f end, int precision, Color color)
	{
		next(renderer, start, end, precision, color);
	}
	
	private static void next(UniversalRenderer renderer, Vector2f start, Vector2f end, int precision, Color color)
	{
		if (precision > 1)
		{
			Vector2f v0 = Math.lerp(start, end, 1 / 3F);
			Vector2f v2 = Math.lerp(start, end, 2 / 3F);
			Vector2f w = Math.lerp(v0, v2, 0.5F);
			Vector2f v1 = Vector2f.copyOf(w).subtract(v0).perpendicular().multiplyAll(Math.SQUARE_ROOT_3F).add(w);
			next(renderer, start, v0, precision - 1, color);
			next(renderer, v0, v1, precision - 1, color);
			next(renderer, v1, v2, precision - 1, color);
			next(renderer, v2, end, precision - 1, color);
			return;
		}
		renderer.vertex(start.x(), start.y(), 0).color(color.r(), color.g(), color.b(), color.a()).end();
		renderer.vertex(end.x(), end.y(), 0).color(color.r(), color.g(), color.b(), color.a()).end();
	}
}
