package com.alcadrial.renderengine.render.shader;

public class ShaderStatus {
	
	private String[] code;
	private String info;
	private boolean isCompiled;
	private int id;
	
	public ShaderStatus(boolean isCompiled, int id, String[] code, String info)
	{
		this.code = code;
		this.info = info;
		this.isCompiled = isCompiled;
		this.id = id;
	}
	
	public String[] getCode()
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
	
	public boolean isDeleted()
	{
		return id == -1;
	}
	
	public int getId()
	{
		return id;
	}
	
	@Override
	public String toString()
	{
		if (id == -1) return "Shader Deleted";
		if (isCompiled) return "Shader compiled";
		String s = "";
		for (String line : code) s += line + "\n";
		return s + info;
	}
}
