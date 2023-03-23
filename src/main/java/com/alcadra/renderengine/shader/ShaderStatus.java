package com.alcadra.renderengine.shader;

public class ShaderStatus {
	
	private String code;
	private boolean compiled;
	private int id;
	
	public ShaderStatus(boolean compiled, int id, String code)
	{
		this.code = code;
		this.compiled = compiled;
		this.id = id;
	}
	
	public String getCode()
	{
		return code;
	}
	
	public boolean isCompiled()
	{
		return compiled;
	}
	
	public int getId()
	{
		return id;
	}
}
