package com.alcadrial.kirry.core;

public sealed interface Module permits OpenglModule {
	
	OpenglModule OPENGL = new OpenglModule();
	
	boolean isPresent();
}
