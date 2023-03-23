package com.alcadra.renderengine.util;

import static java.lang.annotation.ElementType.*;

import java.lang.annotation.Target;

/**
 * A method with this annotation is a method than cannot garantee it's functionalities.
 * 
 * @author Alcadrial
 */
@Target({METHOD, CONSTRUCTOR})
public @interface Broken {
	
}
