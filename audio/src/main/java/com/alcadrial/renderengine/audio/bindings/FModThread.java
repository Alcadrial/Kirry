package com.alcadrial.renderengine.audio.bindings;

import org.lwjgl.fmod.FMOD;

public class FModThread {

	public FModThread()
	{

	}

	/**
	 *@see FMOD#FMOD_Thread_SetAttributes
	 */
	public static int setAttributes(int arg0, long arg1, int arg2, int arg3)
	{
		return FMOD.FMOD_Thread_SetAttributes(arg0, arg1, arg2, arg3);
	}

}
