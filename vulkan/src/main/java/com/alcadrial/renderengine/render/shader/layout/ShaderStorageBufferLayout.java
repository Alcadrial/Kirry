package com.alcadrial.renderengine.render.shader.layout;

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
		int descriptorCount = json.optional("descriptorCount").map(n -> n.asInt(1)).orElse(1);
		if (descriptorCount <= 0) throw new IOException("Invalid descriptorCount of " + location);
		String name = json.optional("name").map(n -> n.stringValue(null)).orElse(null);
		JsonNode variablesNode = json.get("variables").requireNonNull();
		Usage usage = Usage.of(json.get("usage").requireNonNull().stringValue());
		if (usage == null) throw new IOException("Invalid usage of " + location + ".usage");
		
		List<ShaderVariable> variables = new LinkedList<>();
		for (JsonNode variableNode : variablesNode) variables.add(ShaderVariable.parse(variableNode, location + ".variables[" + variables.size() + "]"));
		
		return new ShaderStorageBufferLayout(binding, name, descriptorCount, usage, variables);
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
	
	public ShaderStorageBufferLayout(int binding, @Nullable String name, int descriptorCount, Usage usage, List<ShaderVariable> variables)
	{
		super(Type.STORAGE_BUFFER, binding, name, descriptorCount, variables);
		this.usage = usage;
	}
	
	public Usage getUsage()
	{
		return usage;
	}
}
