package com.alcadrial.kirry.core.util.update;

public @interface UseInstead {
	
	String[] name() default {};
	
	String[] descriptions() default {};
}
