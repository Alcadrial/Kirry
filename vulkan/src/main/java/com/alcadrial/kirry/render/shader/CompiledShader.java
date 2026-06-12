package com.alcadrial.kirry.render.shader;

public record CompiledShader(boolean hasErrors, byte[] binary) {
	
	private static final CompiledShader FAIL = new CompiledShader(true, new byte[0]);
	
	static CompiledShader ofSuccess(String[] code, byte[] binary)
	{
		CompiledShader compiled = new CompiledShader(false, binary);
		return compiled;
	}
	
	public static CompiledShader ofFail()
	{
		return FAIL;
	}
}
