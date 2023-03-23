package com.alcadra.renderengine.util.color;

import com.alcadra.math.vector.Vector3f;

public class VectorColorRGB extends Vector3f implements ColorRGB {
	
	public VectorColorRGB(float r, float g, float b)
	{
		super(r / 255, g / 255, b / 255);
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
	public float w()
	{
		return a();
	}
}
