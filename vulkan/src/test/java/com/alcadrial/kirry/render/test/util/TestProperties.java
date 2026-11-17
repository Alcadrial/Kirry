package com.alcadrial.kirry.render.test.util;

import java.io.File;

import com.alcadrial.kirry.core.util.OperativeSystem;
import com.alcadrial.kirry.render.KirryProperties;
import com.alcadrial.kroperties.BooleanProperty;
import com.alcadrial.kroperties.IntegerProperty;
import com.alcadrial.kroperties.Property;
import com.alcadrial.kroperties.VoidProperty;

public class TestProperties {

	public static final Property<Void> HELP = register(new VoidProperty("help", "Print an example of usage and lists all of the available properties."));
	public static final Property<Boolean> PRINT_LOADING_TIMES = register(new BooleanProperty("printLoadingTimes", "Enable to print the registered time of various tasks.", false, true));
	public static final Property<Boolean> PRINT_FRAME_TIMES = register(new BooleanProperty("printFrameTimes", "Enable to print the time used to draw each frame.", false, true));
	public static final Property<Integer> MAX_FRAME_RATE = register(new IntegerProperty("maxFrameRate", "Set the max frame rate to use.", 120));
	public static final Property<Integer> DEVICE = register(new IntegerProperty("device", "Set the index of the physical device to use or -1 as \"don't care\".", -1));
	public static final Property<Boolean> LIST_DEVICES = register(new BooleanProperty("listDevices", "Print a list of all available physical devices.", false, true));
	public static final Property<Boolean> FORCE_RECOMPILE_SHADERS = register(new BooleanProperty("forceRecompileShader", "Forces shaders compiling", false, true));

	private static <T> Property<T> register(Property<T> property)
	{
		return KirryProperties.register(property);
	}

	private static String invalidArgument(Property<?> property)
	{
		return "Invalid argument \"" + property.getName() + "=" + property.getEncodedValue() + "\", ";
	}

	public static void init(String[] values)
	{
		Property.parseProperties(values, KirryProperties.PROPERTIES);
		if (HELP.isPresent())
		{
			System.out.println("Usage:");
			System.out.println("\tJAR_NAME PROPERTY_NAME[=VALUE]...");
			System.out.println();
			System.out.println("If the value of a property is omitted, the default value, if any, is used.");
			System.out.println("Following the list of available properties and their default value.");
			System.out.println();
			int max = 0;
			for (Property<?> property : KirryProperties.PROPERTIES)
			{
				if (property.getName().length() > max)
				{
					max = property.getName().length();
				}
			}
			String maxSpace = " ".repeat(max + 2);

			for (Property<?> property : KirryProperties.PROPERTIES)
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
		File glslc = OperativeSystem.OS.withExecutableExtension(new File(KirryProperties.SHADER_COMPILER_PATH.getValue(), "glslc"));
		if (!glslc.exists())
		{
			if (KirryProperties.SHADER_COMPILER_PATH.isDefault())
			{
				System.err.println("Cannot find \"glslc\" in " + glslc.getParent() + ", try using \"shaderCompilerPath=...\"");
			}
			else
			{
				System.err.println("Cannot find \"glslc\" in " + glslc.getParent());
			}
			System.exit(-1);
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
