package com.alcadrial.renderengine.render.shader.layout;

import java.io.IOException;

import com.alcadrial.kector.vector.Vector3i;
import com.alcadrial.renderengine.render.RenderEngineVulkan;

import tools.jackson.databind.JsonNode;

public final class ShaderLocalSizeLayout extends ShaderLayout {
	
	static
	{
		RenderEngineVulkan.assertLoaded();
	}
	
	public static ShaderLocalSizeLayout parse(JsonNode json) throws IOException
	{
		return parse(json, "$");
	}
	
	public static ShaderLocalSizeLayout parse(JsonNode json, String location) throws IOException
	{
		int xSize = json.get("xSize").requireNonNull().intValue();
		int ySize = json.get("ySize").intValue(1);
		int zSize = json.get("zSize").intValue(1);
		
		if (xSize <= 0) throw new IOException("Invalid value of " + location + ".xSize");
		if (ySize <= 0) throw new IOException("Invalid value of " + location + ".ySize");
		if (zSize <= 0) throw new IOException("Invalid value of " + location + ".zSize");
		
		return new ShaderLocalSizeLayout(xSize, ySize, zSize);
	}
	
	private Vector3i localSize;
	
	public ShaderLocalSizeLayout(int x, int y, int z)
	{
		super(Type.LOCAL_SIZE, 0);
		localSize = Vector3i.ofImmutable(x, y, z);
	}
	
	public ShaderLocalSizeLayout(Vector3i localSize)
	{
		super(Type.LOCAL_SIZE, 0);
		this.localSize = Vector3i.copyOfImmutable(localSize);
	}
	
	public Vector3i getLocalSize()
	{
		return localSize;
	}
}
