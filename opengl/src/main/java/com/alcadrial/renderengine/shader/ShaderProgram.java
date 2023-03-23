package com.alcadrial.renderengine.shader;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.opengl.GL30;

import com.alcadrial.renderengine.shader.uniform.ShaderUniform;
import com.alcadrial.renderengine.util.Deletable;
import com.alcadrial.renderengine.util.NotUsed;
import com.alcadrial.renderengine.util.Util;

public class ShaderProgram implements Deletable {
	
	public static final ShaderProgram DEFAULT = new ShaderProgram() {
		
		@Override
		public void delete()
		{
			
		}
	};
	
	static
	{
		Util.printError("default program pre-build");
		DEFAULT.attachShader(new Shader("""
				#version 460
				
				layout (location = 0) in vec3 position;
				
				void main()
				{
					gl_Position = vec4(position, 1);
				}
				""", ShaderType.VERTEX));
		DEFAULT.build();
		DEFAULT.attachedShaders[0].get(0).delete();
		Util.printError("default program build");
	}
	
	private List<Shader>[] attachedShaders;
	@NotUsed
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
		id = GL30.glCreateProgram();
		status = ProgramStatus.of(id, "Needs to be builded!");
	}
	
	public void attachShader(Shader shader)
	{
		if (id != -1)
		{
			ShaderType type = shader.getType();
			if (type.ordinal() < ShaderType.COMPUTE.ordinal())
			{
				Util.printError("shader pre-attach");
				GL30.glAttachShader(id, shader.getId());
				Util.printError("shader attach");
				attachedShaders[type.ordinal()].add(shader);
				shader.addProgram(this);
			}
		}
	}
	
	public void enable()
	{
		if (id != -1) GL30.glUseProgram(id);
	}
	
	public ProgramStatus build()
	{
		if (!status.isBuilded() && id != -1)
		{
			Util.printError("shader pre-build");
			GL30.glLinkProgram(id);
			if (GL30.glGetProgrami(id, GL30.GL_LINK_STATUS) == 0)
			{
				status = ProgramStatus.ofLinkFail(id);
				programInputs = DEFAULT.programInputs;
				return status;
			}
			Util.printError("shader linking");
			GL30.glValidateProgram(id);
			if (GL30.glGetProgrami(id, GL30.GL_VALIDATE_STATUS) == 0)
			{
				status = ProgramStatus.ofValidationFail(id);
				programInputs = DEFAULT.programInputs;
				return status;
			}
			Util.printError("shader validating");
			learnInputs();
			status = ProgramStatus.ofSuccess(id);
		}
		return status;
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
		if (id != -1)
		{
			for (List<Shader> list : attachedShaders) for (Shader shader : list) shader.removeProgram(this);
			attachedShaders = new List[0];
			knownInputs = List.of();
			programInputs = knownInputs;
			GL30.glDeleteProgram(id);
		}
		id = -1;
	}
	
	public <T> void setUniform(ShaderUniform<T> uniform, T value)
	{
		if (id != -1) uniform.setValue(GL30.glGetUniformLocation(id, uniform.getName()), value);
	}
	
	public <T> void setFragmentData(String name, int colorNumber)
	{
		if (id != -1) GL30.glBindFragDataLocation(id, colorNumber, name);
	}
	
	public List<Shader> getShaders(ShaderType type)
	{
		if (id == -1 || type.ordinal() >= ShaderType.COMPUTE.ordinal()) return List.of();
		return List.copyOf(attachedShaders[type.ordinal()]);
	}
	
	@NotUsed
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
	
	public int getId()
	{
		return id;
	}
	
	@Override
	public void close()
	{
		delete();
	}
}
