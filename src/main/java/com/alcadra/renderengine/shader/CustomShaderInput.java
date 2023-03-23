package com.alcadra.renderengine.shader;

public class CustomShaderInput<I> extends ShaderInput<I> {
	
	CustomShaderInput(String name, I input, int length)
	{
		super(name, input, length);
	}
}
