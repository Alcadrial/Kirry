package com.alcadrial.renderengine.render.shader;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
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
	
	public Shader(String sourceCode)
	{
		this(sourceCode.split("(\r\n)|\n"));
	}
	
	public Shader(String[] sourceCodeLines)
	{
		file = null;
		code = sourceCodeLines;
	}
	
	public CompiledShader loadCompiled(byte[] compiledCode)
	{
		return compiled = CompiledShader.ofSuccess(code, compiledCode);
	}
	
	public CompiledShader loadCompiled(File compiledFile) throws IOException
	{
		return compiled = CompiledShader.ofSuccess(code, Files.readAllBytes(compiledFile.toPath()));
	}
	
	public CompiledShader compile(ShaderInfo info)
	{
		Process process;
		try
		{
			if (file != null)
			{
				process = new ProcessBuilder(GLSLC_PATH, "-O", "-o", "-", "-fshader-stage=" + info.getType().toString().toLowerCase(), file.toPath().toAbsolutePath().toString()).redirectError(Redirect.INHERIT).start();
			}
			else
			{
				process = new ProcessBuilder(GLSLC_PATH, "-O", "-o", "-", "-fshader-stage=" + info.getType().toString().toLowerCase(), "-").redirectError(Redirect.INHERIT).start();
				for (String l : code)
				{
					process.getOutputStream().write(l.getBytes(StandardCharsets.UTF_8));
					process.getOutputStream().write('\n');
				}
				process.getOutputStream().close();
			}
			byte[] data = process.getInputStream().readAllBytes();
			compiled = process.waitFor() == 0 ? CompiledShader.ofSuccess(code, data) : CompiledShader.ofFail();
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
