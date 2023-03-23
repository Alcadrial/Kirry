package com.alcadrial.renderengine.shader;

import org.lwjgl.opengl.GL43;

public enum ShaderType {
	
	VERTEX("vert", GL43.GL_VERTEX_SHADER),
	FRAGMENT("frag", GL43.GL_FRAGMENT_SHADER),
	GEOMETRY("geom", GL43.GL_GEOMETRY_SHADER),
	COMPUTE("comp", GL43.GL_COMPUTE_SHADER),
	TESS_CONTROL("tcon", GL43.GL_TESS_CONTROL_SHADER),
	TESS_EVALUATION("teva", GL43.GL_TESS_EVALUATION_SHADER),
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