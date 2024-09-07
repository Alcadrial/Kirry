package com.alcadrial.renderengine.audio.bindings;

import org.lwjgl.fmod.FMOD;

public class FModDebug {

	public FModDebug()
	{

	}

	/**
	 *@see FMOD#FMOD_Debug_Initialize
	 */
	public static int initialize(int arg0, int arg1, org.lwjgl.fmod.FMOD_DEBUG_CALLBACKI arg2, java.lang.CharSequence arg3)
	{
		return FMOD.FMOD_Debug_Initialize(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_Debug_Initialize
	 */
	public static int initialize(int arg0, int arg1, org.lwjgl.fmod.FMOD_DEBUG_CALLBACKI arg2, java.nio.ByteBuffer arg3)
	{
		return FMOD.FMOD_Debug_Initialize(arg0, arg1, arg2, arg3);
	}

}
