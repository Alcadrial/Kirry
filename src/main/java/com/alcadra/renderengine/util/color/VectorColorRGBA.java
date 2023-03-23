package com.alcadra.renderengine.util.color;

import com.alcadra.math.vector.Vector4f;

public class VectorColorRGBA extends Vector4f implements Color {
	
	public VectorColorRGBA(float r, float g, float b, float a)
	{
		super(r / 255, g / 255, b / 255, a / 255);
	}
	
	@Override
	public float r()
	{
		return x();
	}
	
	@Override
	public float g()
	{
		return y();
	}
	
	@Override
	public float b()
	{
		return z();
	}
	
	@Override
	public float a()
	{
		return w();
	}
}
