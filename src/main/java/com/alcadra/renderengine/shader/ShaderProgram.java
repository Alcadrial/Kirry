package com.alcadra.renderengine.shader;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.opengl.GL20;

import com.alcadra.renderengine.shader.uniform.ShaderUniform;
import com.alcadra.renderengine.util.NotUsed;

public class ShaderProgram {
	
	private List<Shader>[] attachedShaders;
	private List<ShaderInput> knownInputs;
	private List<ShaderInput> programInputs;
	private int id;
	private ProgramStatus status;
	
	@SuppressWarnings("unchecked")
	public ShaderProgram(@NotUsed ShaderInput... knownInputs)
	{
		attachedShaders = new List[3];
		for (int i = 0; i < attachedShaders.length; i++) attachedShaders[i] = new LinkedList<>();
		this.knownInputs = List.of(knownInputs);
		programInputs = List.of();
		id = GL20.glCreateProgram();
		status = ProgramStatus.of(id, "Needs to be builded!");
	}
	
	public void attachShader(Shader shader)
	{
		if (id != -1)
		{
			ShaderType type = shader.getType();
			if (type.ordinal() < ShaderType.COMPUTE.ordinal())
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
		if (!status.isBuilded())
		{
			GL20.glLinkProgram(id);
			if (GL20.glGetProgrami(id, GL20.GL_LINK_STATUS) == 0)
			{
				status = ProgramStatus.ofLinkFail(id);
				return;
			}
			GL20.glValidateProgram(id);
			if (GL20.glGetProgrami(id, GL20.GL_VALIDATE_STATUS) == 0)
			{
				status = ProgramStatus.ofValidationFail(id);
				return;
			}
			learnInputs();
			status = ProgramStatus.ofSuccess(id);
		}
	}
	
	void learnInputs()
	{
		programInputs = new ArrayList<>();
		for (Shader shader : attachedShaders[0]) if (shader.getStatus().isCompiled())
		{
			String[] code = shader.getCode();
			for (String line : code)
			{
				String line0 = line.stripLeading();
				if (line0.startsWith("layout")) line0 = line0.substring(line0.indexOf(")") + 1).stripLeading();
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
	
	@SuppressWarnings("unchecked")
	public void delete()
	{
		for (List<Shader> list : attachedShaders) for (Shader shader : list) shader.removeProgram(this);
		attachedShaders = new List[0];
		knownInputs = List.of();
		programInputs = knownInputs;
		GL20.glDeleteProgram(id);
		id = -1;
	}
	
	public <T> void setUniform(ShaderUniform<T> uniform, T value)
	{
		uniform.setValue(GL20.glGetUniformLocation(id, uniform.getName()), value);
	}
	
	public List<Shader> getShaders(ShaderType type)
	{
		if (id == -1 || type.ordinal() < ShaderType.COMPUTE.ordinal()) return List.of();
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
	
	public ProgramStatus getStatus()
	{
		return status;
	}
}
