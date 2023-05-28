package com.alcadrial.renderengine.opengl.complex;

import java.util.LinkedList;
import java.util.List;

import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.renderengine.core.util.NotYetImplemented;
import com.alcadrial.renderengine.opengl.renderer.universal.UniversalRenderer;
import com.alcadrial.renderengine.opengl.util.color.Color;

@NotYetImplemented
public class DragonCurve {
	
	public static void renderCurve(UniversalRenderer renderer, Vector2f start, Vector2f end, int precision, Color color)
	{
		if (precision < 0) return;
		for (Vector2f point : gatherPoints(start, end, precision)) renderer.vertex(point.x(), point.y(), point.z()).color(color.r(), color.g(), color.b(), color.a()).end();
	}
	
	private static List<Vector2f> gatherPoints(Vector2f start, Vector2f end, int precision)
	{
		@SuppressWarnings("unused")
		Vector2f[] points0 = new Vector2f[2 << precision];
		
		LinkedList<Vector2f> points = new LinkedList<>();
		
		points.addFirst(start);
		points.addLast(end);
		
		for (int i = 0; i < precision; i++)
		{
			for (int j = 1; j < points.size(); j++)
			{
				
				points.add(j, end);
			}
			
		}
		
		return points;
	}
}
