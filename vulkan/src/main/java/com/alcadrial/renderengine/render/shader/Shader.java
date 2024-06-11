package com.alcadrial.renderengine.render.shader;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.util.Optional;

import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.util.Properties;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;

public class Shader {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public static final File GLSLC = new File(Properties.SHADER_COMPILER_PATH.getValue(), "glslc");
	public static final String GLSLC_PATH = GLSLC.toString();
	
	private File file;
	private String[] code;
	private CompiledShader compiled;
	
	public Shader(File file) throws IOException
	{
		this.file = VulkanErrorUtils.nonNull(file);
		code = Files.readAllLines(file.toPath()).toArray(String[]::new);
	}
	
	Shader(byte[] defaultVertexShaderBinary)
	{
		file = new File("default");
		code = new String[0];
		compiled = CompiledShader.ofSuccess(code, defaultVertexShaderBinary);
	}
	
	public CompiledShader loadCompiled(File compiledFile) throws IOException
	{
		return compiled = CompiledShader.ofSuccess(code, Files.readAllBytes(compiledFile.toPath()));
	}
	
	public CompiledShader compile(ShaderInfo info)
	{
		try
		{
			Process process = new ProcessBuilder(GLSLC_PATH, "-O", "-o", "-", "-fshader-stage=" + info.getType().toString().toLowerCase(), "-").redirectError(Redirect.INHERIT).start();
			process.getOutputStream().write(Files.readAllBytes(file.toPath()));
			process.getOutputStream().close();
			
			compiled = process.waitFor() == 0 ? CompiledShader.ofSuccess(code, process.getInputStream().readAllBytes()) : CompiledShader.ofFail();
		}
		catch (InterruptedException | IOException e)
		{
			Debug.printStackTrace(e);
			compiled = CompiledShader.ofFail();
		}
		return compiled;
	}
	
	public Optional<CompiledShader> getLastCompilation()
	{
		return Optional.ofNullable(compiled);
	}
	
	public boolean isCompilationSuccessfull()
	{
		return compiled != null && !compiled.hasErrors();
	}
	
	public File getFile()
	{
		return file;
	}
	
	public String[] getCode()
	{
		return code;
	}
}
