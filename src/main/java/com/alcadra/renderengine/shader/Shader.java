package com.alcadra.renderengine.shader;

import java.io.File;
import java.io.IOException;

import org.lwjgl.opengl.GL20;

import com.alcadra.math.Matrix4f;
import com.alcadra.math.vector.IVector4f;
import com.alcadra.math.vector.Vector2f;
import com.alcadra.math.vector.Vector3f;
import com.alcadra.renderengine.util.Util;

public class Shader {
	
	private static int programInUse = 0;
	private ShaderInputDataType colorInputDataType, uvInputDataType, normalInputDataType;
	private int program;
	
	public static void disableShaders()
	{
		if (programInUse != 0)
		{
			GL20.glUseProgram(0);
			programInUse = 0;
		}
	}
	
	public Shader(ShaderInputDataType colorInputDataType, ShaderInputDataType uvInputDataType, ShaderInputDataType normalInputDataType)
	{
		this.colorInputDataType = colorInputDataType;
		this.uvInputDataType = uvInputDataType;
		this.normalInputDataType = normalInputDataType;
		program = GL20.glCreateProgram();
	}
	
	public boolean useColor()
	{
		return colorInputDataType == ShaderInputDataType.PER_VERTEX;
	}
	
	public boolean hasUniformColor()
	{
		return colorInputDataType == ShaderInputDataType.UNIFORM;
	}
	
	public boolean useUV()
	{
		return uvInputDataType == ShaderInputDataType.PER_VERTEX;
	}
	
	public boolean hasUniformUV()
	{
		return uvInputDataType == ShaderInputDataType.UNIFORM;
	}
	
	public boolean useNormal()
	{
		return normalInputDataType == ShaderInputDataType.PER_VERTEX;
	}
	
	public boolean hasUniformNormal()
	{
		return normalInputDataType == ShaderInputDataType.UNIFORM;
	}
	
	public void bindAttribute(int attribute, String name)
	{
		GL20.glBindAttribLocation(program, attribute, name);
	}
	
	public void setUniform(int value, String name)
	{
		GL20.glUniform1i(GL20.glGetUniformLocation(program, name), value);
	}
	
	public void setUniform(float value, String name)
	{
		GL20.glUniform1f(GL20.glGetUniformLocation(program, name), value);
	}
	
	public void setUniform(Vector2f value, String name)
	{
		GL20.glUniform2f(GL20.glGetUniformLocation(program, name), value.x(), value.y());
	}
	
	public void setUniform(Vector3f value, String name)
	{
		GL20.glUniform3f(GL20.glGetUniformLocation(program, name), value.x(), value.y(), value.z());
	}
	
	public void setUniform(IVector4f value, String name)
	{
		GL20.glUniform4f(GL20.glGetUniformLocation(program, name), value.x(), value.y(), value.z(), value.w());
	}
	
	public void setUniform(Matrix4f value, String variableName)
	{
		GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(program, variableName), true, value.toArray());
	}
	
	public void build()
	{
		GL20.glLinkProgram(program);
		GL20.glValidateProgram(program);
	}
	
	public void enable()
	{
		if (programInUse != program)
		{
			GL20.glUseProgram(program);
			programInUse = program;
		}
	}
	
	public void delete()
	{
		GL20.glDeleteProgram(program);
	}
	
	public ShaderStatus loadShaderFile(File file) throws IOException
	{
		String pathname = file.getPath();
		return loadShaderFile(file, ShaderType.fromExtension(pathname.substring(pathname.lastIndexOf('.'))));
	}
	
	public ShaderStatus loadShaderFile(File file, ShaderType type) throws IOException
	{
		if (type == ShaderType.UNKNOWN) return new ShaderStatus(false, 0, "unknown");
		String code = Util.loadFileToString(file);
		int shader = GL20.glCreateShader(type.getId());
		GL20.glShaderSource(shader, code);
		GL20.glCompileShader(shader);
		if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == 0) return new ShaderStatus(false, shader, code);
		GL20.glAttachShader(program, shader);
		GL20.glDeleteShader(shader);
		return new ShaderStatus(true, shader, code);
	}
}