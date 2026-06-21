package com.alcadrial.kirry.render.shader.layout;

import com.alcadrial.kector.vector.Vector3ir;

public final class ShaderLocalSizeLayout extends ShaderLayout {
	
	private Vector3ir localSize;
	
	public ShaderLocalSizeLayout(int x, int y, int z)
	{
		super(Type.LOCAL_SIZE, 0);
		localSize = Vector3ir.of(x, y, z);
	}
	
	public ShaderLocalSizeLayout(Vector3ir localSize)
	{
		super(Type.LOCAL_SIZE, 0);
		this.localSize = Vector3ir.of(localSize);
	}
	
	public Vector3ir getLocalSize()
	{
		return localSize;
	}
}
