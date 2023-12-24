package com.alcadrial.renderengine.render.test;

import java.io.File;

import com.alcadrial.kroperties.BooleanProperty;
import com.alcadrial.kroperties.IntegerProperty;
import com.alcadrial.kroperties.Property;
import com.alcadrial.kroperties.VoidProperty;
import com.alcadrial.renderengine.core.util.OperativeSystem;
import com.alcadrial.renderengine.render.util.Properties;

public class TestProperties {
	
	public static final Property<Void> HELP = register(new VoidProperty("help", "Print an example of usage and lists all of the available properties."));
	public static final Property<Boolean> PRINT_LOADING_TIMES = register(new BooleanProperty("printLoadingTimes", "Enable to print the registered time of various tasks.", true));
	public static final Property<Boolean> PRINT_FRAME_TIMES = register(new BooleanProperty("printFrameTimes", "Enable to print the time used to draw each frame.", true));
	public static final Property<Integer> MAX_FRAME_RATE = register(new IntegerProperty("maxFrameRate", "Set the max frame rate to use.", 120));
	public static final Property<Integer> DEVICE = register(new IntegerProperty("device", "Set the index of the physical device to use or -1 as \"don't care\".", -1));
	
	private static <T> Property<T> register(Property<T> property)
	{
		return Properties.register(property);
	}
	
	private static String invalidArgument(Property<?> property)
	{
		return "Invalid argument \"" + property.getName() + "=" + property.getEncodedValue() + "\", ";
	}
	
	public static void init(String[] values)
	{
		for (String string : values)
		{
			System.out.println(string);
		}
		System.out.println();
		Property.parseProperties(values, Properties.PROPERTIES);
		if (HELP.isPresent())
		{
			System.out.println("Usage:");
			System.out.println("\tJAR_NAME PROPERTY_NAME[=VALUE]...");
			System.out.println();
			System.out.println("If the value of a property is omitted, the default value, if any, is used.");
			System.out.println("Following the list of available properties and their default value.");
			System.out.println();
			int max = 0;
			for (Property<?> property : Properties.PROPERTIES) if (property.getName().length() > max) max = property.getName().length();
			String maxSpace = " ".repeat(max + 2);
			
			for (Property<?> property : Properties.PROPERTIES)
			{
				String name = property.getName();
				String space = " ".repeat(max - name.length());
				if (property instanceof VoidProperty)
				{
					System.out.println(name + ": " + space + property.getDescription().replaceAll("\n", "\n" + maxSpace));
					System.out.println();
					continue;
				}
				String encodedValue = property.getEncodedValue();
				property.setDefaultValue();
				System.out.println(name + ": " + space + "current value = " + encodedValue + ", (implicit = " + property.getEncodedValue() + ")");
				System.out.println(maxSpace + "  " + property.getDescription().replaceAll("\n", "\n" + maxSpace));
				System.out.println();
			}
			System.exit(0);
		}
		File glslc = OperativeSystem.OS.withExecutableExtension(new File(new File(Properties.SHADER_COMPILER_PATH.getValue()), "glslc"));
		if (!glslc.exists())
		{
			if (Properties.SHADER_COMPILER_PATH.isDefault()) System.err.println("Cannot find \"glslc\" in " + glslc.getParent() + ", try using \"shaderCompilerPath=...\"");
			else System.err.println("Cannot find \"glslc\" in " + glslc.getParent());
			System.exit(-1);
		}
		if (!Properties.RESOURCE_PATH.isPresent())
		{
			System.err.println("Missing parameter \"resourcePath=...\", cannot start without");
			System.exit(-2);
		}
		if (MAX_FRAME_RATE.getValue() <= 0)
		{
			System.err.println(invalidArgument(MAX_FRAME_RATE) + "the value must be positive");
			System.exit(-3);
		}
		if (DEVICE.getValue() < -1)
		{
			System.err.println(invalidArgument(DEVICE) + "the value must be non positive, 0 or -1");
			System.exit(-3);
		}
	}
}
