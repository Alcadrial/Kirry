package com.alcadrial.renderengine;

import java.util.LinkedList;
import java.util.List;

import com.alcadrial.kroperties.Property;
import com.alcadrial.kroperties.StringProperty;

public class Properties {
	
	private Properties()
	{}
	
	public static final List<Property<?>> PROPERTIES = new LinkedList<>();
	
	public static final Property<String> RESOURCE_PATH = new StringProperty("resourcePath", null);
	public static final Property<String> SHADER_COMPILER_PATH = new StringProperty("shaderCompilerPath", null);
	
	static
	{
		PROPERTIES.add(RESOURCE_PATH);
		PROPERTIES.add(SHADER_COMPILER_PATH);
	}
}
