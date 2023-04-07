package com.alcadrial.renderengine.core.util.update;

public @interface UseInstead {
	
	String[] name() default {};
	
	String[] descriptions() default {};
}
