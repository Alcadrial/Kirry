package com.alcadrial.renderengine.render.util;

import java.util.LinkedList;
import java.util.List;

import com.alcadrial.kroperties.BooleanProperty;
import com.alcadrial.kroperties.Property;
import com.alcadrial.kroperties.StringProperty;

public class Properties {
	
	private Properties()
	{
		
	}
	
	public static final List<Property<?>> PROPERTIES = new LinkedList<>();
	
	public static final Property<Boolean> DEBUG = register(new BooleanProperty("debug", "Enable debug", false, true));
	public static final Property<String> RESOURCE_PATH = register(new StringProperty("resourcePath", "[REQUIRED] Set the path to the assets directory", null));
	public static final Property<String> SHADER_COMPILER_PATH = register(new StringProperty("shaderCompilerPath", "[REQUIRED] Set the path to the directory where glslc.exe is located", System.getenv("VULKAN_SDK") + "/Bin"));
	
	public static <P extends Property<?>> P register(P property)
	{
		PROPERTIES.add(property);
		return property;
	}
}
