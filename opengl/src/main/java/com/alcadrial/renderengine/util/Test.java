package com.alcadrial.renderengine.util;

/**
 * This Annotation is used to identify classes used for testing, 
 * If a class is annotated with this, it will be excluded from the buildpath.
 * <p>
 * If in a package-info file the package declaration is annotated with this,
 * The entire package will be excluded from the buildpath.
 * 
 * @author Alcadrial
 * 
 * @apiNote 
 * 		A class will be excluded from buildpath despite the annotation is not applied to the class because 
 * 		for now is enough having the annotation used in the file, same with package-info file. 
 */
@Test
@Instable
public @interface Test {
	
}
