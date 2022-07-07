package com.alcadra.renderengine.shader;

public class ShaderStatus {
	
	private String code;
	private String info;
	private boolean isCompiled;
	private int id;
	
	public ShaderStatus(boolean isCompiled, int id, String code, String info)
	{
		this.code = code;
		this.info = info;
		this.isCompiled = isCompiled;
		this.id = id;
	}
	
	public String getCode()
	{
		return code;
	}
	
	public String getInfo()
	{
		return info;
	}
	
	public boolean isCompiled()
	{
		return isCompiled;
	}
	
	public int getId()
	{
		return id;
	}
	
	@Override
	public String toString()
	{
		if (isCompiled) return "Shader compiled";
		return code + "\n" + info;
	}
}
