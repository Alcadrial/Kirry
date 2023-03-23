package com.alcadra.renderengine.shader;

import com.alcadra.renderengine.util.UseInstead;

public class ShaderStatus {
	
	private String code;
	private String[] code0;
	private String info;
	private boolean isCompiled;
	private int id;
	
	@Deprecated(since = "1.2", forRemoval = true)
	@UseInstead(value = "ShaderStatus(boolean, int, String[], String)", descriptions = "Break code into a String[] containing a String for every line")
	public ShaderStatus(boolean isCompiled, int id, String code, String info)
	{
		this.code = code;
		this.info = info;
		this.isCompiled = isCompiled;
		this.id = id;
	}
	
	public ShaderStatus(boolean isCompiled, int id, String[] code, String info)
	{
		this.code = "";
		for (String line : code) this.code += line + "\n";
		code0 = code;
		this.info = info;
		this.isCompiled = isCompiled;
		this.id = id;
	}
	
	public String getCode()
	{
		return code;
	}
	
	public String[] getCode0()
	{
		return code0;
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
		if (code0 == null) return code + "\n" + info;
		String s = "";
		for (String line : code0) s += line + "\n";
		return s + info;
	}
}
