package com.alcadrial.renderengine.audio.bindings;

import org.lwjgl.fmod.FMOD;

public class FModSoundGroup {

	public FModSoundGroup()
	{

	}

	/**
	 *@see FMOD#FMOD_SoundGroup_GetName
	 */
	public static int getName(long arg0, java.nio.ByteBuffer arg1)
	{
		return FMOD.FMOD_SoundGroup_GetName(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_SoundGroup_GetSound
	 */
	public static int getSound(long arg0, int arg1, org.lwjgl.PointerBuffer arg2)
	{
		return FMOD.FMOD_SoundGroup_GetSound(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_SoundGroup_SetVolume
	 */
	public static int setVolume(long arg0, float arg1)
	{
		return FMOD.FMOD_SoundGroup_SetVolume(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_SoundGroup_GetVolume
	 */
	public static int getVolume(long arg0, java.nio.FloatBuffer arg1)
	{
		return FMOD.FMOD_SoundGroup_GetVolume(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_SoundGroup_Stop
	 */
	public static int stop(long arg0)
	{
		return FMOD.FMOD_SoundGroup_Stop(arg0);
	}

	/**
	 *@see FMOD#FMOD_SoundGroup_Release
	 */
	public static int release(long arg0)
	{
		return FMOD.FMOD_SoundGroup_Release(arg0);
	}

	/**
	 *@see FMOD#FMOD_SoundGroup_SetMuteFadeSpeed
	 */
	public static int setMuteFadeSpeed(long arg0, float arg1)
	{
		return FMOD.FMOD_SoundGroup_SetMuteFadeSpeed(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_SoundGroup_GetNumPlaying
	 */
	public static int getNumPlaying(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_SoundGroup_GetNumPlaying(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_SoundGroup_GetNumSounds
	 */
	public static int getNumSounds(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_SoundGroup_GetNumSounds(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_SoundGroup_GetSystemObject
	 */
	public static int getSystemObject(long arg0, org.lwjgl.PointerBuffer arg1)
	{
		return FMOD.FMOD_SoundGroup_GetSystemObject(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_SoundGroup_SetMaxAudible
	 */
	public static int setMaxAudible(long arg0, int arg1)
	{
		return FMOD.FMOD_SoundGroup_SetMaxAudible(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_SoundGroup_GetUserData
	 */
	public static int getUserData(long arg0, org.lwjgl.PointerBuffer arg1)
	{
		return FMOD.FMOD_SoundGroup_GetUserData(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_SoundGroup_GetMuteFadeSpeed
	 */
	public static int getMuteFadeSpeed(long arg0, java.nio.FloatBuffer arg1)
	{
		return FMOD.FMOD_SoundGroup_GetMuteFadeSpeed(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_SoundGroup_GetMaxAudible
	 */
	public static int getMaxAudible(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_SoundGroup_GetMaxAudible(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_SoundGroup_SetUserData
	 */
	public static int setUserData(long arg0, long arg1)
	{
		return FMOD.FMOD_SoundGroup_SetUserData(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_SoundGroup_GetMaxAudibleBehavior
	 */
	public static int getMaxAudibleBehavior(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_SoundGroup_GetMaxAudibleBehavior(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_SoundGroup_SetMaxAudibleBehavior
	 */
	public static int setMaxAudibleBehavior(long arg0, int arg1)
	{
		return FMOD.FMOD_SoundGroup_SetMaxAudibleBehavior(arg0, arg1);
	}

}
