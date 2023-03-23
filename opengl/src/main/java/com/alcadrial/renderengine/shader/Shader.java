package com.alcadrial.renderengine.shader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.alcadrial.renderengine.util.Util;

public class Shader {
	
	private final int id;
	private File file;
	private ShaderType type;
	private String[] code;
	private ShaderStatus status;
	private List<ShaderProgram> programs;
	
	Shader(String file, ShaderType type)
	{
		this.file = null;
		this.type = type;
		id = GL20.glCreateShader(type.getId());
		code = file.split("[\r\n,\n,\r]");
		programs = new LinkedList<>();
		GL20.glShaderSource(id, file);
		GL20.glCompileShader(id);
		status = new ShaderStatus(GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS) == 1, id, code, GL20.glGetShaderInfoLog(id));
	}
	
	public Shader(File file)
	{
		this.file = file;
		String pathname = file.getPath();
		type = ShaderType.fromExtension(pathname.substring(pathname.lastIndexOf('.') + 1));
		id = GL20.glCreateShader(type.getId());
		code = new String[0];
		status = new ShaderStatus(false, id, new String[0], "Needs to be compiled!");
		programs = new LinkedList<>();
	}
	
	public Shader(File file, ShaderType type)
	{
		this.file = file;
		this.type = type;
		id = GL20.glCreateShader(type.getId());
		code = new String[0];
		status = new ShaderStatus(false, id, new String[0], "Needs to be compiled!");
		programs = new LinkedList<>();
	}
	
	public ShaderStatus loadShader() throws IOException
	{
		if (type == ShaderType.UNKNOWN) return new ShaderStatus(false, 0, new String[0], "Unknown shader cannot be loaded!");
		if (!status.isDeleted())
		{
			code = Files.lines(file.toPath()).toArray(i -> new String[i]);
			StringBuilder builder = new StringBuilder();
			for (String s : code) builder.append(s).append('\n');
			Util.printError("shader loaded");
			GL20.glShaderSource(id, builder);
			GL20.glCompileShader(id);
			Util.printError("shader compiled");
			status = new ShaderStatus(GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS) == 1, id, code, GL20.glGetShaderInfoLog(id));
			if (status.isCompiled()) for (ShaderProgram program : programs) if (program.getStatus().isBuilded()) program.build();
		}
		return status;
	}
	
	public int getId()
	{
		return status.getId();
	}
	
	public ShaderType getType()
	{
		return type;
	}
	
	public String[] getCode()
	{
		return code;
	}
	
	public ShaderStatus getStatus()
	{
		return status;
	}
	
	void addProgram(ShaderProgram program)
	{
		programs.add(program);
	}
	
	void removeProgram(ShaderProgram program)
	{
		programs.remove(program);
	}
	
	public void delete()
	{
		if (!status.isDeleted())
		{
			GL30.glDeleteProgram(id);
			Util.printError("program d " + id);
			status = new ShaderStatus(status.isCompiled(), -1, status.getCode(), status.getInfo());
		}
	}
	
	@Override
	public String toString()
	{
		return "Shader{id=" + id + ", type=" + type + "}";
	}
}
