package com.alcadrial.renderengine.render.shader;

import org.lwjgl.opengl.GL20;

public class ProgramStatus {
	
	public static ProgramStatus of(int id, String info)
	{
		return new ProgramStatus(false, false, id, info);
	}
	
	public static ProgramStatus ofLinkFail(int id)
	{
		return new ProgramStatus(false, false, id, getInfo(id));
	}
	
	public static ProgramStatus ofValidationFail(int id)
	{
		return new ProgramStatus(true, false, id, getInfo(id));
	}
	
	public static ProgramStatus ofSuccess(int id)
	{
		return new ProgramStatus(true, true, id, getInfo(id));
	}
	
	private static String getInfo(int id)
	{
		if (!GL20.glIsProgram(id)) throw new IllegalArgumentException("A program with id \"" + id + "\" do not exist!");
		return GL20.glGetProgramInfoLog(id);
	}
	
	private String info;
	private boolean isLinked;
	private boolean isValid;
	private int id;
	
	private ProgramStatus(boolean isLinked, boolean isValid, int id, String info)
	{
		this.info = info;
		this.isLinked = isLinked;
		this.isValid = isValid;
		this.id = id;
	}
	
	public String getInfo()
	{
		return info;
	}
	
	public boolean isLinked()
	{
		return isLinked;
	}
	
	public boolean isValid()
	{
		return isValid;
	}
	
	public boolean isBuilded()
	{
		return isValid;
	}
	
	public int getId()
	{
		return id;
	}
	
	@Override
	public String toString()
	{
		if (isValid) return "Program builded";
		if (isLinked) return "Program linked";
		return "Not linked\n" + info;
	}
}
