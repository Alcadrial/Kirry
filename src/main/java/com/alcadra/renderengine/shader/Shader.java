package com.alcadra.renderengine.shader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.lwjgl.opengl.GL20;

import com.alcadra.math.Matrix4f;
import com.alcadra.math.vector.IVector4f;
import com.alcadra.math.vector.Vector2f;
import com.alcadra.math.vector.Vector2i;
import com.alcadra.math.vector.Vector3f;
import com.alcadra.math.vector.Vector3i;
import com.alcadra.math.vector.Vector4i;
import com.alcadra.renderengine.util.update.NoReplacement;
import com.alcadra.renderengine.util.update.UseInstead;

@NoReplacement
@UseInstead(name = "ShaderProgram", descriptions = "Shader logic is now divided in ShaderProgram and ShaderUpdate, a ShaderUpdate will handle a single shader file and a ShaderProgram will use ShaderUpdates to handle a program")
public class Shader {
	
	private static int programInUse = 0;
	
	public static void disableShaders()
	{
		if (programInUse != 0)
		{
			GL20.glUseProgram(0);
			programInUse = 0;
		}
	}
	
	private ShaderInputDataType colorInputDataType, uvInputDataType, normalInputDataType;
	private int program;
	
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
	
	public void setUniformFloat(Vector2i value, String name)
	{
		GL20.glUniform2f(GL20.glGetUniformLocation(program, name), Float.intBitsToFloat(value.x()), Float.intBitsToFloat(value.y()));
	}
	
	public void setUniformFloat(Vector3i value, String name)
	{
		GL20.glUniform3f(GL20.glGetUniformLocation(program, name), Float.intBitsToFloat(value.x()), Float.intBitsToFloat(value.y()), Float.intBitsToFloat(value.z()));
	}
	
	public void setUniformFloat(Vector4i value, String name)
	{
		GL20.glUniform4f(GL20.glGetUniformLocation(program, name), Float.intBitsToFloat(value.x()), Float.intBitsToFloat(value.y()), Float.intBitsToFloat(value.z()), Float.intBitsToFloat(value.w()));
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
	
	public ShaderStatus reload(File file, ShaderType type) throws IOException
	{
		delete();
		ShaderStatus shaderStatus = loadShaderFile(file, type);
		if (shaderStatus.isCompiled()) build();
		return shaderStatus;
	}
	
	public ShaderStatus loadShaderFile(File file) throws IOException
	{
		String pathname = file.getPath();
		return loadShaderFile(file, ShaderType.fromExtension(pathname.substring(pathname.lastIndexOf('.'))));
	}
	
	@SuppressWarnings("removal")
	public ShaderStatus loadShaderFile(File file, ShaderType type) throws IOException
	{
		if (type == ShaderType.UNKNOWN) return new ShaderStatus(false, 0, new String[0], "Unknown shader cannot be loaded!");
		String code = Files.readString(file.toPath());
		int shader = GL20.glCreateShader(type.getId());
		GL20.glShaderSource(shader, code);
		GL20.glCompileShader(shader);
		if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == 0) return new ShaderStatus(false, shader, code, GL20.glGetShaderInfoLog(shader));
		GL20.glAttachShader(program, shader);
		GL20.glDeleteShader(shader);
		return new ShaderStatus(true, shader, code, GL20.glGetShaderInfoLog(shader));
	}
}