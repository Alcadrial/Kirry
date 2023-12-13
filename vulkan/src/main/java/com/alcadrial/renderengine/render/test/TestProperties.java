package com.alcadrial.renderengine.render.test;

import com.alcadrial.kroperties.BooleanProperty;
import com.alcadrial.kroperties.IntegerProperty;
import com.alcadrial.kroperties.Property;
import com.alcadrial.renderengine.core.util.Test;
import com.alcadrial.renderengine.render.util.Properties;

@Test
public class TestProperties {
	
	public static final Property<Boolean> PRINT_LOADING_TIMES = register(new BooleanProperty("printLoadingTimes", true));
	public static final Property<Boolean> PRINT_FRAME_TIMES = register(new BooleanProperty("printFrameTimes", true));
	public static final Property<Integer> MAX_FRAME_RATE = register(new IntegerProperty("maxFrameRate", 120));
	
	private static <T> Property<T> register(Property<T> property)
	{
		return Properties.register(property);
	}
	
	public static void init(String[] values)
	{
		Property.parseProperties(values, Properties.PROPERTIES);
	}
}
