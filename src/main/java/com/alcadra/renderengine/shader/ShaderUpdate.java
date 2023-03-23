package com.alcadra.renderengine.shader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.opengl.GL20;

import com.alcadra.renderengine.util.WorkInProgress;

@WorkInProgress
public class ShaderUpdate extends Shader {
	
	private final int id;
	private File file;
	private ShaderType type;
	private String[] code;
	private ShaderStatus status;
	private List<ShaderProgram> programs;
	
	public ShaderUpdate(File file)
	{
		this.file = file;
		String pathname = file.getPath();
		type = ShaderType.fromExtension(pathname.substring(pathname.lastIndexOf('.') + 1));
		id = GL20.glCreateShader(type.getId());
		code = new String[0];
		status = new ShaderStatus(false, id, new String[0], "Needs to be compiled!");
		programs = new LinkedList<>();
	}
	
	public ShaderUpdate(File file, ShaderType type)
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
		code = Files.lines(file.toPath()).toArray(i -> new String[i]);
		StringBuilder builder = new StringBuilder();
		for (String s : code) builder.append(s).append('\n');
		GL20.glShaderSource(id, builder);
		GL20.glCompileShader(id);
		status = new ShaderStatus(GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS) == 1, id, code, GL20.glGetShaderInfoLog(id));
		if (status.isCompiled()) for (ShaderProgram program : programs) if (program.getStatus().isBuilded()) program.build();
		return status;
	}
	
	public int getId()
	{
		return id;
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
	
	@Override
	public String toString()
	{
		return "Shader{id=" + id + ", type=" + type + "}";
	}
}
