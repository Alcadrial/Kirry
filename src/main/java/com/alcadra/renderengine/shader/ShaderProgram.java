package com.alcadra.renderengine.shader;

import java.util.LinkedList;
import java.util.List;

import org.lwjgl.opengl.GL20;

import com.alcadra.renderengine.util.WorkInProgress;

@WorkInProgress
public class ShaderProgram {
	
	private List<ShaderUpdate>[] attachedShaders;
	private int id;
	
	@SuppressWarnings("unchecked")
	public ShaderProgram()
	{
		attachedShaders = new List[6];
		for (int i = 0; i < attachedShaders.length; i++) attachedShaders[i] = new LinkedList<>();
	}
	
	public void attachShader(ShaderUpdate shader)
	{
		if (shader.getType() != ShaderType.UNKNOWN)
		{
			GL20.glAttachShader(id, shader.getId());
			GL20.glDeleteShader(shader.getId());
			attachedShaders[shader.getId()].add(shader);
		}
	}
	
	public void enable()
	{
		GL20.glUseProgram(id);
	}
	
	public void build()
	{
		GL20.glLinkProgram(id);
		GL20.glValidateProgram(id);
	}
	
	public void delete()
	{
		GL20.glDeleteProgram(id);
	}
	
	public List<ShaderUpdate> getShaders(ShaderType type)
	{
		if (type == ShaderType.UNKNOWN) return List.of();
		return List.copyOf(attachedShaders[type.ordinal()]);
	}
}
