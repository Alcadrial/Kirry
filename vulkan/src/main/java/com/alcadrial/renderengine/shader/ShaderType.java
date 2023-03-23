package com.alcadrial.renderengine.shader;

public enum ShaderType {
	
	VERTEX("vert", 0),
	FRAGMENT("frag", 0),
	GEOMETRY("geom", 0),
	COMPUTE("comp", 0),
	TESS_CONTROL("tcon", 0),
	TESS_EVALUATION("teva", 0),
	UNKNOWN("", 0);
	
	public static ShaderType fromExtension(String extension)
	{
		for (int i = 5; i >= 0; i--) if (values()[i].extension.equals(extension)) return values()[i];
		return UNKNOWN;
	}
	
	private String extension;
	private int id;
	
	ShaderType(String extension, int id)
	{
		this.extension = extension;
		this.id = id;
	}
	
	public String getExtension()
	{
		return extension;
	}
	
	public int getId()
	{
		return id;
	}
}