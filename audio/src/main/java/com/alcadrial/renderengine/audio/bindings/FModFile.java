package com.alcadrial.renderengine.audio.bindings;

import org.lwjgl.fmod.FMOD;

public class FModFile {

	public FModFile()
	{

	}

	/**
	 *@see FMOD#FMOD_File_GetDiskBusy
	 */
	public static int getDiskBusy(java.nio.IntBuffer arg0)
	{
		return FMOD.FMOD_File_GetDiskBusy(arg0);
	}

	/**
	 *@see FMOD#FMOD_File_SetDiskBusy
	 */
	public static int setDiskBusy(int arg0)
	{
		return FMOD.FMOD_File_SetDiskBusy(arg0);
	}

}
