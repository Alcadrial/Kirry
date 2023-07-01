package com.alcadrial.renderengine.core.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * <p>
 * Indicates that the annotated element could be null.
 * If the annotated element is of a primitive type like int means that
 * a value of 0 is considerable as null.
 * </p>
 * <p>
 * Based on the annotated element, various cases rises up:
 * 
 * <ul>
 *   <li> For a module, all packages in the module will inherit this annotation as well.
 * 
 *   <li> For a package, all classes and sub-packages will inherit this annotation as well.
 * 
 *   <li> For an annotation, all attributes could be null.
 *   <li> For a class, all fields and methods will inherit this annotation as well.
 *        
 *   <li> For a field, it could be null.
 *   <li> For a method, its return value could be null.
 *   <li> For a parameter, it's value could be null.
 *   <li> For a variable, it could be null.
 * </ul>
 * 
 * @author Alcadrial
 */
@Target({ElementType.MODULE,
	ElementType.PACKAGE,
	ElementType.ANNOTATION_TYPE, ElementType.TYPE,
	ElementType.RECORD_COMPONENT, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
public @interface Nullable {
	
}
