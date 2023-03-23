package com.alcadra.renderengine.util;

import static java.lang.annotation.ElementType.*;

import java.lang.annotation.Target;

/**
 * This Annotation is used to identify all classes, costructors, methods and packages
 * that have their behaviour only structured.
 * <p>
 * If the annotation is applied to a package, all classes declared in it will also be NYI
 * 
 * @see WorkInProgress
 * 
 * @author Alcadrial
 */
@Target({TYPE, CONSTRUCTOR, METHOD, PACKAGE})
public @interface NotYetImplemented {
	
}
