package com.alcadrial.renderengine.render.shader.layouts;

import java.io.IOException;

import com.alcadrial.kector.vector.Vector3i;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.util.JsonUtils;
import com.google.gson.stream.JsonReader;

public final class ShaderLocalSizeLayout extends ShaderLayout {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public static ShaderLocalSizeLayout parseJson(JsonReader reader) throws IOException
	{
		Integer xSize = null;
		Integer ySize = 1;
		Integer zSize = 1;
		
		while (reader.hasNext())
		{
			switch (reader.nextName())
			{
				case "xSize" -> xSize = reader.nextInt();
				case "ySize" -> ySize = reader.nextInt();
				case "zSize" -> zSize = reader.nextInt();
				default -> reader.skipValue();
			}
		}
		if (xSize == null) throw new IOException(JsonUtils.missing(reader, "xSize"));
		if (xSize <= 0) throw new IOException(JsonUtils.invalid(reader, "xSize"));
		if (ySize <= 0) throw new IOException(JsonUtils.invalid(reader, "ySize"));
		if (zSize <= 0) throw new IOException(JsonUtils.invalid(reader, "zSize"));
		
		return new ShaderLocalSizeLayout(xSize, ySize, zSize);
	}
	
	private Vector3i localSize;
	
	public ShaderLocalSizeLayout(int x, int y, int z)
	{
		this();
		localSize = Vector3i.ofImmutable(x, y, z);
	}
	
	public ShaderLocalSizeLayout(Vector3i localSize)
	{
		this();
		this.localSize = Vector3i.copyOfImmutable(localSize);
	}
	
	private ShaderLocalSizeLayout()
	{
		super(Type.LOCAL_SIZE);
	}
	
	public Vector3i getLocalSize()
	{
		return localSize;
	}
}
