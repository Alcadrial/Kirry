package com.alcadrial.kirry.core.util;

import static java.lang.annotation.ElementType.*;

import java.lang.annotation.Target;

/**
 * this annotation marks a method for the possibility of an optimization.
 * 
 * @author Alcadrial
 */
@Target({METHOD, CONSTRUCTOR})
public @interface Optimizable {
	
}
