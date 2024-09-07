package com.alcadrial.renderengine.audio.bindings;

import org.lwjgl.fmod.FMOD;

public class FModMemory {

	public FModMemory()
	{

	}

	/**
	 *@see FMOD#FMOD_Memory_Initialize
	 */
	public static int initialize(java.nio.ByteBuffer arg0, org.lwjgl.fmod.FMOD_MEMORY_ALLOC_CALLBACKI arg1, org.lwjgl.fmod.FMOD_MEMORY_REALLOC_CALLBACKI arg2, org.lwjgl.fmod.FMOD_MEMORY_FREE_CALLBACKI arg3, int arg4)
	{
		return FMOD.FMOD_Memory_Initialize(arg0, arg1, arg2, arg3, arg4);
	}

	/**
	 *@see FMOD#FMOD_Memory_GetStats
	 */
	public static int getStats(java.nio.IntBuffer arg0, java.nio.IntBuffer arg1, int arg2)
	{
		return FMOD.FMOD_Memory_GetStats(arg0, arg1, arg2);
	}

}
