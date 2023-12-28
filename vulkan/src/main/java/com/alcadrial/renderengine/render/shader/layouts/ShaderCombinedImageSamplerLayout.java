package com.alcadrial.renderengine.render.shader.layouts;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.alcadrial.renderengine.core.util.Nullable;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.shader.ShaderInputType;
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.util.JsonUtils;
import com.google.gson.stream.JsonReader;

public final class ShaderCombinedImageSamplerLayout extends ShaderBufferLayout {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public static ShaderCombinedImageSamplerLayout parseJson(JsonReader reader) throws IOException
	{
		Integer binding = null;
		String name = null;
		List<ShaderVariable> variables = null;
		
		while (reader.hasNext())
		{
			switch (reader.nextName())
			{
				case "binding" -> binding = reader.nextInt();
				case "name" -> name = reader.nextString();
				case "variables" ->
				{
					if (variables != null) throw new IOException(JsonUtils.duplicate(reader, "variables"));
					variables = new LinkedList<>();
					
					reader.beginArray();
					while (reader.hasNext())
					{
						String[] declaration = reader.nextString().split(" ");
						if (declaration.length != 2) throw new IOException(JsonUtils.invalid(reader, "variables[" + variables.size() + "]"));
						variables.add(new ShaderVariable(ShaderInputType.of(declaration[0].trim()), declaration[1].trim()));
					}
					reader.endArray();
				}
				default -> reader.skipValue();
			}
		}
		if (binding == null) throw new IOException(JsonUtils.missing(reader, "binding"));
		if (variables == null) throw new IOException(JsonUtils.missing(reader, "variables"));
		
		return new ShaderCombinedImageSamplerLayout(binding, name, variables);
	}
	
	public ShaderCombinedImageSamplerLayout(int binding, @Nullable String name, List<ShaderVariable> variables)
	{
		super(Type.COMBINED_IMAGE_SAMPLER, binding, name, variables);
	}
}
