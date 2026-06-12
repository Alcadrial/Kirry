package com.alcadrial.kirry.render.shader;

import java.nio.file.Path;

import org.lwjgl.vulkan.VK10;

public enum ShaderType {
	
	VERTEX("vert", VK10.VK_SHADER_STAGE_VERTEX_BIT),
	TESS_CONTROL("tesc", VK10.VK_SHADER_STAGE_TESSELLATION_CONTROL_BIT),
	TESS_EVALUATION("tese", VK10.VK_SHADER_STAGE_TESSELLATION_EVALUATION_BIT),
	GEOMETRY("geom", VK10.VK_SHADER_STAGE_GEOMETRY_BIT),
	FRAGMENT("frag", VK10.VK_SHADER_STAGE_FRAGMENT_BIT),
	COMPUTE("comp", VK10.VK_SHADER_STAGE_COMPUTE_BIT),
	UNKNOWN("", 0);
	
	public static ShaderType fromPath(Path path)
	{
		String name = path.getFileName().toString();
		return ShaderType.fromExtension(name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : name);
	}
	
	public static ShaderType fromExtension(String extension)
	{
		for (int i = 5; i >= 0; i--) if (values()[i].extension.equals(extension)) return values()[i];
		return UNKNOWN;
	}
	
	private String extension;
	private int stageBit;
	
	ShaderType(String extension, int stageBit)
	{
		this.extension = extension;
		this.stageBit = stageBit;
	}
	
	public String getExtension()
	{
		return extension;
	}
	
	public int getStageBit()
	{
		return stageBit;
	}
}