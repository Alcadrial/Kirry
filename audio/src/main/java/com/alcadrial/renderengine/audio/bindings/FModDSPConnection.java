package com.alcadrial.renderengine.audio.bindings;

import org.lwjgl.fmod.FMOD;

public class FModDSPConnection {

	public FModDSPConnection()
	{

	}

	/**
	 *@see FMOD#FMOD_DSPConnection_GetType
	 */
	public static int getType(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_DSPConnection_GetType(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_DSPConnection_SetMix
	 */
	public static int setMix(long arg0, float arg1)
	{
		return FMOD.FMOD_DSPConnection_SetMix(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_DSPConnection_GetMix
	 */
	public static int getMix(long arg0, java.nio.FloatBuffer arg1)
	{
		return FMOD.FMOD_DSPConnection_GetMix(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_DSPConnection_GetUserData
	 */
	public static int getUserData(long arg0, org.lwjgl.PointerBuffer arg1)
	{
		return FMOD.FMOD_DSPConnection_GetUserData(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_DSPConnection_GetInput
	 */
	public static int getInput(long arg0, org.lwjgl.PointerBuffer arg1)
	{
		return FMOD.FMOD_DSPConnection_GetInput(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_DSPConnection_GetOutput
	 */
	public static int getOutput(long arg0, org.lwjgl.PointerBuffer arg1)
	{
		return FMOD.FMOD_DSPConnection_GetOutput(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_DSPConnection_SetMixMatrix
	 */
	public static int setMixMatrix(long arg0, java.nio.FloatBuffer arg1, int arg2, int arg3, int arg4)
	{
		return FMOD.FMOD_DSPConnection_SetMixMatrix(arg0, arg1, arg2, arg3, arg4);
	}

	/**
	 *@see FMOD#FMOD_DSPConnection_SetUserData
	 */
	public static int setUserData(long arg0, long arg1)
	{
		return FMOD.FMOD_DSPConnection_SetUserData(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_DSPConnection_GetMixMatrix
	 */
	public static int getMixMatrix(long arg0, java.nio.FloatBuffer arg1, java.nio.IntBuffer arg2, java.nio.IntBuffer arg3, int arg4)
	{
		return FMOD.FMOD_DSPConnection_GetMixMatrix(arg0, arg1, arg2, arg3, arg4);
	}

}
