package com.alcadrial.renderengine.render.shader.layouts;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.jspecify.annotations.Nullable;

import com.alcadrial.renderengine.core.util.Pair;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.shader.ShaderVariable;

import tools.jackson.databind.JsonNode;

public final class ShaderInputLayout extends ShaderLayout {
	
	static
	{
		RenderEngine.assertLoaded();
	}

	public static ShaderInputLayout parse(JsonNode json) throws IOException
	{
		return parse(json, "$");
	}
	
	public static ShaderInputLayout parse(JsonNode json, String location) throws IOException
	{
		int binding = json.get("binding").intValue();
		String name = json.get("name").stringValue(null);
		JsonNode variablesNode = json.get("variables").requireNonNull();
		
		List<Pair<Integer, ShaderVariable>> variables = new LinkedList<>();
		
		for (JsonNode variableNode : variablesNode)
		{
			String newLocation = location + ".variables[" + variables.size() + "]";
			int variableLocation = variableNode.get("location").intValue();
			if (variableLocation < 0) throw new IOException("Invalid value of " + newLocation + ".location");
			ShaderVariable variable = ShaderVariable.parse(variableNode.get("declaration"), newLocation + ".declaration");
			
			variables.add(new Pair<>(variableLocation, variable));
		}
		
		return new ShaderInputLayout(binding, name, variables);
	}
	
	private final int binding;
	private final @Nullable String name;
	private final List<Pair<Integer, ShaderVariable>> variables;
	
	public ShaderInputLayout(int binding, @Nullable String name, List<Pair<Integer, ShaderVariable>> variables)
	{
		super(Type.INPUT);
		this.binding = binding;
		this.name = name;
		this.variables = List.copyOf(variables);
	}
	
	public int getBinding()
	{
		return binding;
	}
	
	public @Nullable String getName()
	{
		return name;
	}
	
	public List<Pair<Integer, ShaderVariable>> getVariables()
	{
		return variables;
	}
}
