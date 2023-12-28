package com.alcadrial.renderengine.render.shader.layouts;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.alcadrial.renderengine.core.util.Nullable;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.shader.ShaderInputType;
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.util.JsonUtils;
import com.google.gson.stream.JsonReader;

public final class ShaderStorageBufferLayout extends ShaderBufferLayout {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public static ShaderStorageBufferLayout parseJson(JsonReader reader) throws IOException
	{
		Integer binding = null;
		String name = null;
		Usage usage = Usage.OTHER;
		List<ShaderVariable> variables = null;
		
		while (reader.hasNext())
		{
			switch (reader.nextName())
			{
				case "binding" -> binding = reader.nextInt();
				case "name" -> name = reader.nextString();
				case "usage" -> JsonUtils.require((usage = Usage.of(reader.nextString())) != null, JsonUtils.invalid(reader, "usage"));
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
		
		return new ShaderStorageBufferLayout(binding, name, usage, variables);
	}
	
	public enum Usage {
		
		INPUT,
		OUTPUT,
		OTHER;
		
		public static Usage of(String name)
		{
			return switch (name)
			{
				case "input" -> INPUT;
				case "output" -> OUTPUT;
				case "other" -> OTHER;
				default -> null;
			};
		}
	}
	
	private Usage usage;
	
	public ShaderStorageBufferLayout(int binding, @Nullable String name, Usage usage, List<ShaderVariable> variables)
	{
		super(Type.STORAGE_BUFFER, binding, name, variables);
		this.usage = Objects.requireNonNull(usage);
	}
	
	public Usage getUsage()
	{
		return usage;
	}
}
