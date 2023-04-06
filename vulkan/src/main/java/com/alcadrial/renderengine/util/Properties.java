package com.alcadrial.renderengine.util;

import java.util.LinkedList;
import java.util.List;

import com.alcadrial.kroperties.BooleanProperty;
import com.alcadrial.kroperties.Property;
import com.alcadrial.kroperties.StringProperty;

public class Properties {
	
	private Properties()
	{}
	
	public static final List<Property<?>> PROPERTIES = new LinkedList<>();
	
	public static final Property<Boolean> DEBUG = register(new BooleanProperty("debug", false, true));
	public static final Property<String> RESOURCE_PATH = register(new StringProperty("resourcePath", null));
	public static final Property<String> SHADER_COMPILER_PATH = register(new StringProperty("shaderCompilerPath", null));
	
	public static <P extends Property<?>> P register(P property)
	{
		PROPERTIES.add(property);
		return property;
	}
}
