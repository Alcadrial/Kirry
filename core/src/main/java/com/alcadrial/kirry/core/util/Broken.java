package com.alcadrial.kirry.core.util;

import static java.lang.annotation.ElementType.*;

import java.lang.annotation.Target;

/**
 * A method with this annotation is a method than cannot garantee it's functionalities.
 * 
 * A method parameter with this annotation indicates that that parameter's functionalities are not geranted.
 * 
 * @author Alcadrial
 */
@Target({METHOD, CONSTRUCTOR, PARAMETER})
public @interface Broken {
	
}
