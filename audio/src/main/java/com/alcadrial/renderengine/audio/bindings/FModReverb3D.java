package com.alcadrial.renderengine.audio.bindings;

import org.lwjgl.fmod.FMOD;

public class FModReverb3D {

	public FModReverb3D()
	{

	}

	/**
	 *@see FMOD#FMOD_Reverb3D_Set3DAttributes
	 */
	public static int set3DAttributes(long arg0, org.lwjgl.fmod.FMOD_VECTOR arg1, float arg2, float arg3)
	{
		return FMOD.FMOD_Reverb3D_Set3DAttributes(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_Reverb3D_GetProperties
	 */
	public static int getProperties(long arg0, org.lwjgl.fmod.FMOD_REVERB_PROPERTIES arg1)
	{
		return FMOD.FMOD_Reverb3D_GetProperties(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Reverb3D_SetProperties
	 */
	public static int setProperties(long arg0, org.lwjgl.fmod.FMOD_REVERB_PROPERTIES arg1)
	{
		return FMOD.FMOD_Reverb3D_SetProperties(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Reverb3D_Get3DAttributes
	 */
	public static int get3DAttributes(long arg0, org.lwjgl.fmod.FMOD_VECTOR arg1, java.nio.FloatBuffer arg2, java.nio.FloatBuffer arg3)
	{
		return FMOD.FMOD_Reverb3D_Get3DAttributes(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_Reverb3D_GetActive
	 */
	public static int getActive(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_Reverb3D_GetActive(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Reverb3D_SetUserData
	 */
	public static int setUserData(long arg0, long arg1)
	{
		return FMOD.FMOD_Reverb3D_SetUserData(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Reverb3D_SetActive
	 */
	public static int setActive(long arg0, int arg1)
	{
		return FMOD.FMOD_Reverb3D_SetActive(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Reverb3D_Release
	 */
	public static int release(long arg0)
	{
		return FMOD.FMOD_Reverb3D_Release(arg0);
	}

	/**
	 *@see FMOD#FMOD_Reverb3D_GetUserData
	 */
	public static int getUserData(long arg0, org.lwjgl.PointerBuffer arg1)
	{
		return FMOD.FMOD_Reverb3D_GetUserData(arg0, arg1);
	}

}
