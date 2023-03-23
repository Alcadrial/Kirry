package com.alcadra.renderengine.shader.uniform.extra;

import org.lwjgl.opengl.GL20;

import com.alcadra.renderengine.shader.uniform.ShaderUniform;
import com.alcadra.renderengine.util.color.Color;
import com.alcadra.renderengine.util.color.ColorFormat;

public class ColorUniform extends ShaderUniform<Color> {
	
	private ColorFormat format;
	
	public ColorUniform(String name, ColorFormat format)
	{
		super(name);
		this.format = format;
	}
	
	@Override
	public void setValue(int location, Color value)
	{
		value = format.mix(value);
		switch (format.getChannels())
		{
			case 1 -> GL20.glUniform1f(location, value.r());
			case 2 -> GL20.glUniform2f(location, value.r(), value.g());
			case 3 -> GL20.glUniform3f(location, value.r(), value.g(), value.b());
			case 4 -> GL20.glUniform4f(location, value.r(), value.g(), value.b(), value.a());
		}
	}
}
