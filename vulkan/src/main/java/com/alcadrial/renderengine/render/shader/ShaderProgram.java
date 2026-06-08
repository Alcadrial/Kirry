package com.alcadrial.renderengine.render.shader;

import java.util.Arrays;
import java.util.Base64;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.jspecify.annotations.Nullable;

import com.alcadrial.renderengine.render.RenderEngineVulkan;

public class ShaderProgram {
	
	static
	{
		RenderEngineVulkan.assertLoaded();
	}
	
	public static class Builder {
		
		private MutableList<PreparedShader> shaders;
		
		private Builder()
		{
			shaders = Lists.mutable.of();
		}
		
		public Builder add(PreparedShader shader)
		{
			shaders.add(shader);
			return this;
		}
		
		public Builder add(PreparedShader... shaders)
		{
			this.shaders.addAll(Arrays.asList(shaders));
			return this;
		}
		
		public Builder addAll(Iterable<PreparedShader> shaders)
		{
			this.shaders.addAllIterable(shaders);
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
		byte[] binary = Base64.getDecoder().decode(
			"AwIjBwAAAQALAA0AGwAAAAAAAAARAAIAAQAAAAsABgABAAAAR0xTTC5zdGQuNDUwAAAAAA4AAwAAAAAAAQAAAA8ABwAAAAAABAAAAG1haW4AAAAADQAAABIAAAADAAMAAgAAAMIBAAAEAAoAR0xfR09PR0xFX2NwcF9zdHlsZV9saW5lX2RpcmVjdGl2ZQAABAAIAEdMX0dPT0dMRV9pbmNsdWRlX2RpcmVjdGl2ZQAFAAQABAAAAG1haW4AAAAABQAGAAsAAABnbF9QZXJWZXJ0ZXgAAAAABgAGAAsAAAAAAAAAZ2xfUG9zaXRpb24ABgAHAAsAAAABAAAAZ2xfUG9pbnRTaXplAAAAAAYABwALAAAAAgAAAGdsX0NsaXBEaXN0YW5jZQAGAAcACwAAAAMAAABnbF9DdWxsRGlzdGFuY2UABQADAA0AAAAAAAAABQAGABIAAAB2ZXJ0ZXhQb3NpdGlvbgAASAAFAAsAAAAAAAAACwAAAAAAAABIAAUACwAAAAEAAAALAAAAAQAAAEgABQALAAAAAgAAAAsAAAADAAAASAAFAAsAAAADAAAACwAAAAQAAABHAAMACwAAAAIAAABHAAQAEgAAAB4AAAAAAAAAEwACAAIAAAAhAAMAAwAAAAIAAAAWAAMABgAAACAAAAAXAAQABwAAAAYAAAAEAAAAFQAEAAgAAAAgAAAAAAAAACsABAAIAAAACQAAAAEAAAAcAAQACgAAAAYAAAAJAAAAHgAGAAsAAAAHAAAABgAAAAoAAAAKAAAAIAAEAAwAAAADAAAACwAAADsABAAMAAAADQAAAAMAAAAVAAQADgAAACAAAAABAAAAKwAEAA4AAAAPAAAAAAAAABcABAAQAAAABgAAAAMAAAAgAAQAEQAAAAEAAAAQAAAAOwAEABEAAAASAAAAAQAAACsABAAGAAAAFAAAAAAAgD8gAAQAGQAAAAMAAAAHAAAANgAFAAIAAAAEAAAAAAAAAAMAAAD4AAIABQAAAD0ABAAQAAAAEwAAABIAAABRAAUABgAAABUAAAATAAAAAAAAAFEABQAGAAAAFgAAABMAAAABAAAAUQAFAAYAAAAXAAAAEwAAAAIAAABQAAcABwAAABgAAAAVAAAAFgAAABcAAAAUAAAAQQAFABkAAAAaAAAADQAAAA8AAAA+AAMAGgAAABgAAAD9AAEAOAABAA==");
		Shader shader = new Shader("""
			#version 450
			
			layout (location = 0) in vec3 vertexPosition;
			
			void main()
			{
				gl_Position = vec4(vertexPosition, 1);
			}
			""");
		shader.loadCompiled(binary);
		DEFAULT = create().add(new PreparedShader(shader, ShaderType.VERTEX)).build();
	}
	
	private final MutableList<PreparedShader> shaders;
	private final PreparedShader vertexShader;
	
	private ShaderProgram(Builder builder)
	{
		shaders = Lists.mutable.ofAll(builder.shaders);
		for (PreparedShader ps : shaders) if (ps.type() == ShaderType.VERTEX)
		{
			vertexShader = ps;
			return;
		}
		throw new IllegalStateException("A ShaderProgram must have at least 1 VertexShader");
	}
	
	public MutableList<PreparedShader> getShaders()
	{
		return shaders;
	}
	
	public PreparedShader getVertexShader()
	{
		return vertexShader;
	}
	
	@Override
	public int hashCode()
	{
		return shaders.hashCode();
	}
	
	@Override
	public boolean equals(@Nullable Object obj)
	{
		return obj instanceof ShaderProgram p && shaders.equals(p.shaders);
	}
}
