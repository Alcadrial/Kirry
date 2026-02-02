package com.alcadrial.renderengine.render.shader;

import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import com.alcadrial.renderengine.core.util.Pair;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.shader.layouts.ShaderInputLayout;

public class ShaderProgram {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public static class Builder {
		
		private List<PreparedShader> shaders;
		
		private Builder()
		{
			shaders = new LinkedList<>();
		}
		
		public Builder add(PreparedShader... shaders)
		{
			this.shaders.addAll(Arrays.asList(shaders));
			return this;
		}
		
		public ShaderProgram build()
		{
			return new ShaderProgram(this);
		}
	}
	
	public static Builder create()
	{
		return new Builder();
	}
	
	public static final ShaderProgram DEFAULT;
	
	static
	{
		byte[] binary = Base64.getDecoder().decode("AwIjBwAAAQALAA0AGwAAAAAAAAARAAIAAQAAAAsABgABAAAAR0xTTC5zdGQuNDUwAAAAAA4AAwAAAAAAAQAAAA8ABwAAAAAABAAAAG1haW4AAAAADQAAABIAAAADAAMAAgAAAMIBAAAEAAoAR0xfR09PR0xFX2NwcF9zdHlsZV9saW5lX2RpcmVjdGl2ZQAABAAIAEdMX0dPT0dMRV9pbmNsdWRlX2RpcmVjdGl2ZQAFAAQABAAAAG1haW4AAAAABQAGAAsAAABnbF9QZXJWZXJ0ZXgAAAAABgAGAAsAAAAAAAAAZ2xfUG9zaXRpb24ABgAHAAsAAAABAAAAZ2xfUG9pbnRTaXplAAAAAAYABwALAAAAAgAAAGdsX0NsaXBEaXN0YW5jZQAGAAcACwAAAAMAAABnbF9DdWxsRGlzdGFuY2UABQADAA0AAAAAAAAABQAGABIAAAB2ZXJ0ZXhQb3NpdGlvbgAASAAFAAsAAAAAAAAACwAAAAAAAABIAAUACwAAAAEAAAALAAAAAQAAAEgABQALAAAAAgAAAAsAAAADAAAASAAFAAsAAAADAAAACwAAAAQAAABHAAMACwAAAAIAAABHAAQAEgAAAB4AAAAAAAAAEwACAAIAAAAhAAMAAwAAAAIAAAAWAAMABgAAACAAAAAXAAQABwAAAAYAAAAEAAAAFQAEAAgAAAAgAAAAAAAAACsABAAIAAAACQAAAAEAAAAcAAQACgAAAAYAAAAJAAAAHgAGAAsAAAAHAAAABgAAAAoAAAAKAAAAIAAEAAwAAAADAAAACwAAADsABAAMAAAADQAAAAMAAAAVAAQADgAAACAAAAABAAAAKwAEAA4AAAAPAAAAAAAAABcABAAQAAAABgAAAAMAAAAgAAQAEQAAAAEAAAAQAAAAOwAEABEAAAASAAAAAQAAACsABAAGAAAAFAAAAAAAgD8gAAQAGQAAAAMAAAAHAAAANgAFAAIAAAAEAAAAAAAAAAMAAAD4AAIABQAAAD0ABAAQAAAAEwAAABIAAABRAAUABgAAABUAAAATAAAAAAAAAFEABQAGAAAAFgAAABMAAAABAAAAUQAFAAYAAAAXAAAAEwAAAAIAAABQAAcABwAAABgAAAAVAAAAFgAAABcAAAAUAAAAQQAFABkAAAAaAAAADQAAAA8AAAA+AAMAGgAAABgAAAD9AAEAOAABAA==");
		Shader shader = new Shader("""
			#version 450
			
			layout (location = 0) in vec3 vertexPosition;
			
			void main()
			{
				gl_Position = vec4(vertexPosition, 1);
			}
			""");
		shader.loadCompiled(binary);
		ShaderInputLayout shaderInputLayout = new ShaderInputLayout(0, "vertexInput", List.of(new Pair<>(0, ShaderVariable.VERTEX_POSITION)));
		ShaderInfo shaderInfo = new ShaderInfo(ShaderType.VERTEX, "main", List.of(shaderInputLayout));
		DEFAULT = create().add(new PreparedShader(shader, shaderInfo)).build();
	}
	
	private MutableList<PreparedShader> shaders;
	
	private ShaderProgram(Builder builder)
	{
		shaders = Lists.mutable.ofAll(builder.shaders);
		for (PreparedShader ps : shaders) if (ps.info().getType() == ShaderType.VERTEX) return;
		throw new IllegalStateException("A ShaderProgram must have at least 1 VertexShader");
	}
	
	public MutableList<PreparedShader> getShaders()
	{
		return shaders;
	}
}
