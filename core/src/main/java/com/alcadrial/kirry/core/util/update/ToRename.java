package com.alcadrial.kirry.core.util.update;

import static java.lang.annotation.ElementType.*;

import java.lang.annotation.Target;

@Target({TYPE, FIELD, METHOD, LOCAL_VARIABLE, ANNOTATION_TYPE, RECORD_COMPONENT})
public @interface ToRename {
	
	String value();
}
