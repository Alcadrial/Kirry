package com.alcadrial.renderengine.util.update;

public @interface UseInstead {
	
	String[] name() default {};
	
	String[] descriptions() default {};
}
