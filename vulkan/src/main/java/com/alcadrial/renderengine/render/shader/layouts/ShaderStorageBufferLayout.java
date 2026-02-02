package com.alcadrial.renderengine.render.shader.layouts;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.jspecify.annotations.Nullable;

import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.shader.ShaderVariable;

import tools.jackson.databind.JsonNode;

public final class ShaderStorageBufferLayout extends ShaderBufferLayout {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public static ShaderStorageBufferLayout parse(JsonNode json) throws IOException
	{
		return parse(json, "$");
	}
	
	public static ShaderStorageBufferLayout parse(JsonNode json, String location) throws IOException
	{
		int binding = json.get("binding").intValue();
		String name = json.get("name").stringValue(null);
		JsonNode variablesNode = json.get("variables").requireNonNull();
		Usage usage = Usage.of(json.get("usage").requireNonNull().stringValue());
		if (usage == null) throw new IOException("Invalid usage of " + location + ".usage");
		
		List<ShaderVariable> variables = new LinkedList<>();
		for (JsonNode variableNode : variablesNode) variables.add(ShaderVariable.parse(variableNode, location + ".variables[" + variables.size() + "]"));
		
		return new ShaderStorageBufferLayout(binding, name, usage, variables);
	}
	
	public enum Usage {
		INPUT,
		OUTPUT,
		OTHER;
		
		public static @Nullable Usage of(String name)
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
		this.usage = usage;
	}
	
	public Usage getUsage()
	{
		return usage;
	}
}
