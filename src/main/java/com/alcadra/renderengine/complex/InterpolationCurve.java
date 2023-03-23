package com.alcadra.renderengine.complex;

import java.util.function.Function;

import com.alcadra.math.Math;
import com.alcadra.math.vector.Vector2f;
import com.alcadra.renderengine.renderer.universal.UniversalRenderer;
import com.alcadra.renderengine.util.color.Color;

public class InterpolationCurve {
	
	public static void renderCurve(UniversalRenderer renderer, Vector2f pos, Vector2f size, Function<Float, Float> interpolationFunction, int precision, Color color)
	{
		if (size.x() == 0 || size.y() == 0) return;
		
		float step = 1F / precision;
		Vector2f point = Vector2f.copyOf(pos);
		for (float i = 0; i < 1; i += step)
		{
			point.setAll(pos.x(), pos.y()).add(i * size.x(), Math.interpolation(0, size.y(), i, interpolationFunction));
			renderer.vertex(point.x(), point.y(), point.z()).color(color.r(), color.g(), color.b(), color.a()).end();
		}
		point.setAll(pos.x(), pos.y()).add(size.x(), Math.interpolation(0, size.y(), 1, interpolationFunction));
		renderer.vertex(point.x(), point.y(), point.z()).color(color.r(), color.g(), color.b(), color.a()).end();
	}
	
}
