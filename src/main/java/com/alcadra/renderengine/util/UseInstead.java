package com.alcadra.renderengine.util;

public @interface UseInstead {
	
	String[] name() default {};
	
	String[] descriptions() default {};
}
