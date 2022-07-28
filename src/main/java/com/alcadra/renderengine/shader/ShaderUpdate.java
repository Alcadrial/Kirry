package com.alcadra.renderengine.shader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.lwjgl.opengl.GL20;

import com.alcadra.renderengine.util.WorkInProgress;

@WorkInProgress
public class ShaderUpdate extends Shader {
	
	private List<ShaderInput<?>> knownInputs;
	private List<ShaderInput<?>> programInputs;
	private File file;
	private ShaderType type;
	private final int id;
	
	public ShaderUpdate(File file, ShaderInput<?>... knownInputs)
	{
		this.knownInputs = List.of(knownInputs);
		this.file = file;
		String pathname = file.getPath();
		type = ShaderType.fromExtension(pathname.substring(pathname.lastIndexOf('.')));
		id = GL20.glCreateShader(type.getId());
	}
	
	public ShaderUpdate(File file, ShaderType type, ShaderInput<?>... knownInputs)
	{
		this.knownInputs = List.of(knownInputs);
		this.file = file;
		this.type = type;
		id = GL20.glCreateShader(type.getId());
	}
	
	public ShaderStatus loadShader() throws IOException
	{
		if (type == ShaderType.UNKNOWN) return new ShaderStatus(false, 0, "unknown", "Unknown shader cannot be loaded!");
		String code = Files.readString(file.toPath());
		GL20.glShaderSource(id, code);
		GL20.glCompileShader(id);
		if (GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS) == 0) return new ShaderStatus(false, id, code, GL20.glGetShaderInfoLog(id));
		return new ShaderStatus(true, id, code, GL20.glGetShaderInfoLog(id));
	}
	
	public ShaderType getType()
	{
		return type;
	}
	
	public int getId()
	{
		return id;
	}
}
