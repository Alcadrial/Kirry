package com.alcadra.renderengine.util;

public @interface UseInstead {
	
	String[] value() default {};
	
	String[] descriptions() default {};
}
