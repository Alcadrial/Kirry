package com.alcadrial.renderengine.render.shader.layouts;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.alcadrial.renderengine.core.util.Pair;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.shader.ShaderInputType;
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.util.JsonUtils;
import com.google.gson.stream.JsonReader;

public final class ShaderInputLayout extends ShaderLayout {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public static ShaderInputLayout parseJson(JsonReader reader) throws IOException
	{
		Integer binding = null;
		List<Pair<Integer, ShaderVariable>> variables = null;
		
		while (reader.hasNext())
		{
			switch (reader.nextName())
			{
				case "binding" -> binding = reader.nextInt();
				case "variables" ->
				{
					if (variables != null) throw new IOException(JsonUtils.duplicate(reader, "variables"));
					variables = new LinkedList<>();
					
					reader.beginArray();
					while (reader.hasNext())
					{
						reader.beginObject();
						
						Integer location = null;
						String[] declaration = null;
						
						while (reader.hasNext())
						{
							switch (reader.nextName())
							{
								case "location" -> location = reader.nextInt();
								case "declaration" -> declaration = reader.nextString().split(" +");
								default -> reader.skipValue();
							}
						}
						if (declaration == null) throw new IOException(JsonUtils.missing(reader, "declaration"));
						if (location == null) throw new IOException(JsonUtils.missing(reader, "location"));
						if (declaration.length != 2) throw new IOException(JsonUtils.invalid(reader, "declaration"));
						if (location < 0) throw new IOException(JsonUtils.invalid(reader, "location"));
						
						ShaderInputType type = ShaderInputType.of(declaration[0].trim());
						if (type == null) throw new IOException(JsonUtils.invalid(reader, "declaration"));
						
						variables.add(new Pair<>(location, ShaderVariable.of(type, declaration[1].trim())));
						
						reader.endObject();
					}
					reader.endArray();
				}
				default -> reader.skipValue();
			}
		}
		if (binding == null) throw new IOException(JsonUtils.missing(reader, "binding"));
		if (variables == null) throw new IOException(JsonUtils.missing(reader, "variables"));
		
		return new ShaderInputLayout(binding, variables);
	}
	
	private int binding;
	private List<Pair<Integer, ShaderVariable>> variables;
	
	public ShaderInputLayout(int binding, List<Pair<Integer, ShaderVariable>> variables)
	{
		super(Type.INPUT);
		this.binding = binding;
		this.variables = List.copyOf(variables);
	}
	
	public int getBinding()
	{
		return binding;
	}
	
	public List<Pair<Integer, ShaderVariable>> getVariables()
	{
		return variables;
	}
}
