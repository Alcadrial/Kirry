package com.alcadra.renderengine.shader;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.opengl.GL20;

import com.alcadra.renderengine.util.NotUsed;
import com.alcadra.renderengine.util.WorkInProgress;

@WorkInProgress
public class ShaderProgram {
	
	private List<ShaderUpdate>[] attachedShaders;
	private List<ShaderInput> knownInputs;
	private List<ShaderInput> programInputs;
	private int id;
	private boolean isBuilded;
	
	@SuppressWarnings("unchecked")
	public ShaderProgram(@NotUsed ShaderInput... knownInputs)
	{
		attachedShaders = new List[6];
		for (int i = 0; i < attachedShaders.length; i++) attachedShaders[i] = new LinkedList<>();
		this.knownInputs = List.of(knownInputs);
		programInputs = List.of();
		id = GL20.glCreateProgram();
		isBuilded = false;
	}
	
	public void attachShader(ShaderUpdate shader)
	{
		if (id != -1)
		{
			ShaderType type = shader.getType();
			if (type != ShaderType.UNKNOWN)
			{
				GL20.glAttachShader(id, shader.getId());
				GL20.glDeleteShader(shader.getId());
				attachedShaders[type.ordinal()].add(shader);
				shader.addProgram(this);
			}
		}
	}
	
	public void enable()
	{
		if (id != -1) GL20.glUseProgram(id);
	}
	
	public void build()
	{
		if (!isBuilded)
		{
			GL20.glLinkProgram(id);
			GL20.glValidateProgram(id);
			learnInputs();
			isBuilded = true;
		}
	}
	
	void learnInputs()
	{
		programInputs = new ArrayList<>();
		for (ShaderUpdate shader : attachedShaders[0]) if (shader.getStatus().isCompiled())
		{
			String[] code = shader.getCode();
			for (String line : code)
			{
				String line0 = line.stripLeading();
				if (line0.startsWith("in "))
				{
					String line1 = line0.substring(3).stripLeading();
					int i = line1.indexOf(" ");
					if (i != -1 && i >= 1)
					{
						String line2 = line1.substring(i).stripLeading();
						int j0 = line2.indexOf(";");
						if (j0 != -1 && j0 == line2.length() - 1)
						{
							int j1 = line2.indexOf(" ");
							if (j1 == -1 || line2.substring(j1).isBlank())
							{
								String typeName = line1.substring(0, i);
								String inputName = line2.substring(0, j1 == -1 ? j0 : j1);
								programInputs.add(ShaderInput.of(ShaderInputType.of(typeName), inputName));
							}
						}
					}
				}
			}
		}
		programInputs = List.copyOf(programInputs);
	}
	
	public boolean isBuilded()
	{
		return isBuilded;
	}
	
	@SuppressWarnings("unchecked")
	public void delete()
	{
		for (List<ShaderUpdate> list : attachedShaders) for (ShaderUpdate shader : list) shader.removeProgram(this);
		attachedShaders = new List[0];
		knownInputs = List.of();
		programInputs = knownInputs;
		GL20.glDeleteProgram(id);
		id = -1;
	}
	
	public List<ShaderUpdate> getShaders(ShaderType type)
	{
		if (id == -1 || type == ShaderType.UNKNOWN) return List.of();
		return List.copyOf(attachedShaders[type.ordinal()]);
	}
	
	public List<ShaderInput> getKnownInputs()
	{
		return knownInputs;
	}
	
	public List<ShaderInput> getProgramInputs()
	{
		return programInputs;
	}
}
