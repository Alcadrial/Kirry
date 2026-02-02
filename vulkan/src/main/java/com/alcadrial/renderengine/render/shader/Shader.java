package com.alcadrial.renderengine.render.shader;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

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
	public static final Pattern SHADER_FILE_EXTENSION = Pattern.compile(".*(\\.vert|\\.tesc|\\.tese|\\.geom|\\.frag|\\.comp)");
	
	public static boolean isShaderFile(Path path)
	{
		Path name = path.getFileName();
		if (name == null) return false;
		return SHADER_FILE_EXTENSION.matcher(name.toString()).matches();
	}
	
	private @Nullable File file;
	private String[] code;
	private @Nullable CompiledShader compiled;
	
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
			File file = this.file;
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
			return compiled = process.waitFor() == 0 ? CompiledShader.ofSuccess(code, data) : CompiledShader.ofFail();
		}
		catch (InterruptedException | IOException e)
		{
			Debug.printStackTrace(e);
			return compiled = CompiledShader.ofFail();
		}
	}
	
	public Optional<CompiledShader> getLastCompilation()
	{
		return Optional.ofNullable(compiled);
	}
	
	public boolean isCompilationSuccessfull()
	{
		return compiled != null && !compiled.hasErrors();
	}
	
	public @Nullable File getFile()
	{
		return file;
	}
	
	public String[] getCode()
	{
		return code;
	}
}
