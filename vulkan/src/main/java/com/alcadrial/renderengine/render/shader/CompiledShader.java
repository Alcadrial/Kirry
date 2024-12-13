package com.alcadrial.renderengine.render.shader;

public class CompiledShader {
	
	private static final CompiledShader FAIL = new CompiledShader();
	
	static
	{
		FAIL.hasErrors = true;
	}
	
	static CompiledShader ofSuccess(String[] code, byte[] binary)
	{
		CompiledShader compiled = new CompiledShader();
		compiled.hasErrors = false;
		compiled.binary = binary;
		return compiled;
	}
	
	public static CompiledShader ofFail()
	{
		return FAIL;
	}
	
	private boolean hasErrors;
	private byte[] binary;
	
	public boolean hasErrors()
	{
		return hasErrors;
	}
	
	public byte[] getBinary()
	{
		return binary;
	}
}
