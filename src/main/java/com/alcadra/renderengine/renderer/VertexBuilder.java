package com.alcadra.renderengine.renderer;

import com.alcadra.math.vector.Vector2f;
import com.alcadra.math.vector.Vector3f;
import com.alcadra.math.vector.Vector4f;

public interface VertexBuilder<R extends VertexBuilder<R>> {
	
	default R color(float r, float g, float b, float a)
	{
		return color(new Vector4f(r, g, b, a));
	}
	
	public R color(Vector4f color);
	
	default R uv(float u, float v)
	{
		return uv(new Vector2f(u, v));
	}
	
	public R uv(Vector2f uv);
	
	default R normal(float x, float y, float z)
	{
		return normal(new Vector3f(x, y, z));
	}
	
	public R normal(Vector3f normal);
}