package com.alcadrial.renderengine.vulkan.shader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.alcadrial.renderengine.vulkan.util.Properties;

public class Shader {
	
	public static final File GLSLC = new File(new File(Properties.SHADER_COMPILER_PATH.getValue()), "glslc");
	private File file;
	private ShaderType type;
	private String[] code;
	private CompiledShader compiled;
	
	public Shader(File file) throws IOException
	{
		this.file = file;
		String pathname = file.getPath();
		type = ShaderType.fromExtension(pathname.substring(pathname.lastIndexOf('.') + 1));
		code = Files.readAllLines(file.toPath()).toArray(String[]::new);
	}
	
	public CompiledShader compile()
	{
		try
		{
			File binary = new File(file + ".bin");
			new ProcessBuilder("\"" + GLSLC + "\" \"" + file + "\" -O -o \"" + binary + "\"").inheritIO().start().waitFor();
			if (binary.exists())
			{
				compiled = CompiledShader.ofSuccess(Files.readAllBytes(binary.toPath()));
				binary.delete();
			}
			else
			{
				compiled = CompiledShader.ofFail();
				System.out.println("Shader compilation failed");
			}
		}
		catch (InterruptedException | IOException e)
		{
			e.printStackTrace(System.out);
			compiled = CompiledShader.ofFail();
		}
		return compiled;
	}
	
	public CompiledShader getOrCompile()
	{
		if (compiled == null) compile();
		return compiled;
	}
	
	public CompiledShader getLastCompilation()
	{
		return compiled;
	}
	
	public File getFile()
	{
		return file;
	}
	
	public ShaderType getType()
	{
		return type;
	}
	
	public String[] getCode()
	{
		return code;
	}
}
