package com.alcadra.renderengine.util.update;

import static java.lang.annotation.ElementType.*;

import java.lang.annotation.Target;

@Target({TYPE, FIELD, METHOD, LOCAL_VARIABLE, ANNOTATION_TYPE, PACKAGE, TYPE_PARAMETER, RECORD_COMPONENT})
public @interface ToMove {
	
	String value();
}
