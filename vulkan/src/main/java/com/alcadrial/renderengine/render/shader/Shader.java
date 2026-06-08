package com.alcadrial.renderengine.render.shader;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.RenderEngineProperties;
import com.alcadrial.renderengine.render.RenderEngineVulkan;

/**
 * 
 * @author Alcadrial
 *
 *@see
 *	<a href="https://wikis.khronos.org/opengl/Core_Language_(GLSL)">GLSL specs</a>
 *	<a href="https://github.com/KhronosGroup/GLSL/blob/main/extensions/khr/GL_KHR_vulkan_glsl.txt">Vulkan extension</a>
 */
public class Shader {
	
	static
	{
		RenderEngineVulkan.assertLoaded();
	}
	
	public static final File GLSLC = new File(RenderEngineProperties.SHADER_COMPILER_PATH.getValue(), "glslc");
	public static final String GLSLC_PATH = GLSLC.toString();
	public static final Pattern SHADER_FILE_EXTENSION = Pattern.compile(".*(\\.vert|\\.tesc|\\.tese|\\.geom|\\.frag|\\.comp)");
	
	public static boolean isShaderFile(Path path)
	{
		Path name = path.getFileName();
		if (name == null) return false;
		return SHADER_FILE_EXTENSION.matcher(name.toString()).matches();
	}
	
	private @Nullable Path path;
	private String[] code;
	private @Nullable CompiledShader compiled;
	
	public Shader(Path path) throws IOException
	{
		this.path = path;
		code = Files.readAllLines(path).toArray(String[]::new);
	}
	
	public Shader(String sourceCode)
	{
		this(sourceCode.split("(\r\n)|\n"));
	}
	
	public Shader(String[] sourceCodeLines)
	{
		path = null;
		code = sourceCodeLines;
	}
	
	public CompiledShader loadCompiled(byte[] compiledCode)
	{
		return compiled = CompiledShader.ofSuccess(code, compiledCode);
	}
	
	public CompiledShader loadCompiled(Path compiledPath) throws IOException
	{
		return compiled = CompiledShader.ofSuccess(code, Files.readAllBytes(compiledPath));
	}
	
	public CompiledShader compile(ShaderType type)
	{
		Process process;
		try
		{
			Path path = this.path;
			if (path != null)
			{
				process = new ProcessBuilder(GLSLC_PATH, "-O", "-o", "-", "-fshader-stage=" + type.toString().toLowerCase(), path.toRealPath().toString()).redirectError(Redirect.INHERIT).start();
			}
			else
			{
				process = new ProcessBuilder(GLSLC_PATH, "-O", "-o", "-", "-fshader-stage=" + type.toString().toLowerCase(), "-").redirectError(Redirect.INHERIT).start();
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
	
	public @Nullable Path getPath()
	{
		return path;
	}
	
	public String[] getCode()
	{
		return code;
	}
	
	@Override
	public int hashCode()
	{
		if (path != null) return path.hashCode();
		return Arrays.hashCode(code);
	}
	
	@Override
	public boolean equals(@Nullable Object obj)
	{
		return obj instanceof Shader s && ((path != null && ((@NonNull Path) path).equals(s.path) || Arrays.equals(code, s.code)));
	}
}
