package com.alcadrial.renderengine.render;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import com.alcadrial.kroperties.BooleanProperty;
import com.alcadrial.kroperties.FileProperty;
import com.alcadrial.kroperties.IntegerProperty;
import com.alcadrial.kroperties.Property;
import com.alcadrial.kroperties.StringProperty;

public class RenderEngineProperties {
	
	private RenderEngineProperties()
	{
		
	}
	
	public static final List<Property<?>> PROPERTIES = new LinkedList<>();
	
	public static final Property<Boolean> DEBUG = register(new BooleanProperty("debug", "Enable debug", false, true));
	public static final Property<String> RESOURCE_PATH = register(new StringProperty("resourcePath", "[REQUIRED] Set the path to the assets directory", null));
	public static final Property<File> SHADER_COMPILER_PATH = register(new FileProperty("shaderCompilerPath", "[REQUIRED] Set the path to the directory where glslc.exe is located", new File(System.getenv("VULKAN_SDK") + "/Bin")));
	
	public static final Property<Integer> DEVICE_DESTRUCTION_LIST_THRESHOLD = register(new IntegerProperty("deviceDestructionListThreshold", "Set the min number of vulkan objects that are unclosed when closing a device to trigger a warning in debug's output", 200));
	
	public static <P extends Property<?>> P register(P property)
	{
		PROPERTIES.add(property);
		return property;
	}
}
