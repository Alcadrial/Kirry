package com.alcadrial.renderengine.render.shader.layouts;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.jspecify.annotations.Nullable;

import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.shader.ShaderVariable;

import tools.jackson.databind.JsonNode;

public final class ShaderUniformBufferLayout extends ShaderBufferLayout {
	
	static
	{
		RenderEngine.assertLoaded();
	}

	public static ShaderUniformBufferLayout parse(JsonNode json) throws IOException
	{
		return parse(json, "$");
	}
	
	public static ShaderUniformBufferLayout parse(JsonNode json, String location) throws IOException
	{
		int binding = json.get("binding").intValue();
		int descriptorCount = json.optional("descriptorCount").map(n -> n.asInt(1)).orElse(1);
		if (descriptorCount <= 0) throw new IOException("Invalid descriptorCount of " + location);
		String name = json.optional("name").map(n -> n.stringValue(null)).orElse(null);
		JsonNode variablesNode = json.get("variables").requireNonNull();
		
		List<ShaderVariable> variables = new LinkedList<>();
		for (JsonNode variableNode : variablesNode) variables.add(ShaderVariable.parse(variableNode, location + ".variables[" + variables.size() + "]"));
		
		return new ShaderUniformBufferLayout(binding, name, descriptorCount, variables);
	}
	
	public ShaderUniformBufferLayout(int binding, @Nullable String name, int descriptorCount, List<ShaderVariable> variables)
	{
		super(Type.UNIFORM_BUFFER, binding, name, descriptorCount, variables);
	}
}
