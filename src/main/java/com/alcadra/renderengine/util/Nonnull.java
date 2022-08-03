package com.alcadra.renderengine.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated element must be nonnull.
 * <p>
 * Based on the annotated element, various cases rises up:
 * 
 * <ul>
 *   <li> For a module, all packages in the module will inherit this annotation as well.
 * 
 *   <li> For a package, all classes and sub-packages will inherit this annotation as well.
 * 
 *   <li> For an annotation, all attributes will never be null.
 *   <li> For a class, all fields and methods will inherit this annotation as well.
 *        
 *   <li> For a field, it will never be null.
 *   <li> For a method, its return value will never be null.
 *   <li> For a parameter, it's value must not be null.
 *   <li> For a variable, it will never be null.
 * </ul>
 * 
 * @author Alcadrial
 */
@Target({ElementType.MODULE,
	ElementType.PACKAGE,
	ElementType.ANNOTATION_TYPE, ElementType.TYPE,
	ElementType.RECORD_COMPONENT, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
public @interface Nonnull {
	
}
