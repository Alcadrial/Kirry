package com.alcadrial.renderengine.audio.bindings;

import org.lwjgl.fmod.FMOD;

public class FModSound {

	public FModSound()
	{

	}

	/**
	 *@see FMOD#FMOD_Sound_Release
	 */
	public static int release(long arg0)
	{
		return FMOD.FMOD_Sound_Release(arg0);
	}

	/**
	 *@see FMOD#FMOD_Sound_Lock
	 */
	public static int lock(long arg0, int arg1, int arg2, org.lwjgl.PointerBuffer arg3, org.lwjgl.PointerBuffer arg4, java.nio.IntBuffer arg5, java.nio.IntBuffer arg6)
	{
		return FMOD.FMOD_Sound_Lock(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
	}

	/**
	 *@see FMOD#FMOD_Sound_Unlock
	 */
	public static int unlock(long arg0, java.nio.ByteBuffer arg1, java.nio.ByteBuffer arg2)
	{
		return FMOD.FMOD_Sound_Unlock(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_Sound_GetSystemObject
	 */
	public static int getSystemObject(long arg0, org.lwjgl.PointerBuffer arg1)
	{
		return FMOD.FMOD_Sound_GetSystemObject(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Sound_SetDefaults
	 */
	public static int setDefaults(long arg0, float arg1, int arg2)
	{
		return FMOD.FMOD_Sound_SetDefaults(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_Sound_GetUserData
	 */
	public static int getUserData(long arg0, org.lwjgl.PointerBuffer arg1)
	{
		return FMOD.FMOD_Sound_GetUserData(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Sound_SetLoopCount
	 */
	public static int setLoopCount(long arg0, int arg1)
	{
		return FMOD.FMOD_Sound_SetLoopCount(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Sound_GetLoopPoints
	 */
	public static int getLoopPoints(long arg0, java.nio.IntBuffer arg1, int arg2, java.nio.IntBuffer arg3, int arg4)
	{
		return FMOD.FMOD_Sound_GetLoopPoints(arg0, arg1, arg2, arg3, arg4);
	}

	/**
	 *@see FMOD#FMOD_Sound_AddSyncPoint
	 */
	public static int addSyncPoint(long arg0, int arg1, int arg2, java.nio.ByteBuffer arg3, org.lwjgl.PointerBuffer arg4)
	{
		return FMOD.FMOD_Sound_AddSyncPoint(arg0, arg1, arg2, arg3, arg4);
	}

	/**
	 *@see FMOD#FMOD_Sound_AddSyncPoint
	 */
	public static int addSyncPoint(long arg0, int arg1, int arg2, java.lang.CharSequence arg3, org.lwjgl.PointerBuffer arg4)
	{
		return FMOD.FMOD_Sound_AddSyncPoint(arg0, arg1, arg2, arg3, arg4);
	}

	/**
	 *@see FMOD#FMOD_Sound_GetFormat
	 */
	public static int getFormat(long arg0, java.nio.IntBuffer arg1, java.nio.IntBuffer arg2, java.nio.IntBuffer arg3, java.nio.IntBuffer arg4)
	{
		return FMOD.FMOD_Sound_GetFormat(arg0, arg1, arg2, arg3, arg4);
	}

	/**
	 *@see FMOD#FMOD_Sound_SetLoopPoints
	 */
	public static int setLoopPoints(long arg0, int arg1, int arg2, int arg3, int arg4)
	{
		return FMOD.FMOD_Sound_SetLoopPoints(arg0, arg1, arg2, arg3, arg4);
	}

	/**
	 *@see FMOD#FMOD_Sound_GetOpenState
	 */
	public static int getOpenState(long arg0, java.nio.IntBuffer arg1, java.nio.IntBuffer arg2, java.nio.IntBuffer arg3, java.nio.IntBuffer arg4)
	{
		return FMOD.FMOD_Sound_GetOpenState(arg0, arg1, arg2, arg3, arg4);
	}

	/**
	 *@see FMOD#FMOD_Sound_SetMusicSpeed
	 */
	public static int setMusicSpeed(long arg0, float arg1)
	{
		return FMOD.FMOD_Sound_SetMusicSpeed(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Sound_GetLength
	 */
	public static int getLength(long arg0, java.nio.IntBuffer arg1, int arg2)
	{
		return FMOD.FMOD_Sound_GetLength(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_Sound_GetDefaults
	 */
	public static int getDefaults(long arg0, java.nio.FloatBuffer arg1, java.nio.IntBuffer arg2)
	{
		return FMOD.FMOD_Sound_GetDefaults(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_Sound_GetSubSound
	 */
	public static int getSubSound(long arg0, int arg1, org.lwjgl.PointerBuffer arg2)
	{
		return FMOD.FMOD_Sound_GetSubSound(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_Sound_GetNumSubSounds
	 */
	public static int getNumSubSounds(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_Sound_GetNumSubSounds(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Sound_GetNumTags
	 */
	public static int getNumTags(long arg0, java.nio.IntBuffer arg1, java.nio.IntBuffer arg2)
	{
		return FMOD.FMOD_Sound_GetNumTags(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_Sound_ReadData
	 */
	public static int readData(long arg0, java.nio.ByteBuffer arg1, java.nio.IntBuffer arg2)
	{
		return FMOD.FMOD_Sound_ReadData(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_Sound_GetLoopCount
	 */
	public static int getLoopCount(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_Sound_GetLoopCount(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Sound_GetSyncPoint
	 */
	public static int getSyncPoint(long arg0, int arg1, org.lwjgl.PointerBuffer arg2)
	{
		return FMOD.FMOD_Sound_GetSyncPoint(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_Sound_SeekData
	 */
	public static int seekData(long arg0, int arg1)
	{
		return FMOD.FMOD_Sound_SeekData(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Sound_GetMusicSpeed
	 */
	public static int getMusicSpeed(long arg0, java.nio.FloatBuffer arg1)
	{
		return FMOD.FMOD_Sound_GetMusicSpeed(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Sound_SetSoundGroup
	 */
	public static int setSoundGroup(long arg0, long arg1)
	{
		return FMOD.FMOD_Sound_SetSoundGroup(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Sound_GetSoundGroup
	 */
	public static int getSoundGroup(long arg0, org.lwjgl.PointerBuffer arg1)
	{
		return FMOD.FMOD_Sound_GetSoundGroup(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Sound_SetUserData
	 */
	public static int setUserData(long arg0, long arg1)
	{
		return FMOD.FMOD_Sound_SetUserData(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Sound_DeleteSyncPoint
	 */
	public static int deleteSyncPoint(long arg0, long arg1)
	{
		return FMOD.FMOD_Sound_DeleteSyncPoint(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Sound_GetSubSoundParent
	 */
	public static int getSubSoundParent(long arg0, org.lwjgl.PointerBuffer arg1)
	{
		return FMOD.FMOD_Sound_GetSubSoundParent(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Sound_SetMusicChannelVolume
	 */
	public static int setMusicChannelVolume(long arg0, int arg1, float arg2)
	{
		return FMOD.FMOD_Sound_SetMusicChannelVolume(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_Sound_GetMusicChannelVolume
	 */
	public static int getMusicChannelVolume(long arg0, int arg1, java.nio.FloatBuffer arg2)
	{
		return FMOD.FMOD_Sound_GetMusicChannelVolume(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_Sound_GetMusicNumChannels
	 */
	public static int getMusicNumChannels(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_Sound_GetMusicNumChannels(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Sound_GetSyncPointInfo
	 */
	public static int getSyncPointInfo(long arg0, long arg1, java.nio.ByteBuffer arg2, java.nio.IntBuffer arg3, int arg4)
	{
		return FMOD.FMOD_Sound_GetSyncPointInfo(arg0, arg1, arg2, arg3, arg4);
	}

	/**
	 *@see FMOD#FMOD_Sound_Set3DCustomRolloff
	 */
	public static int set3DCustomRolloff(long arg0, org.lwjgl.fmod.FMOD_VECTOR.Buffer arg1)
	{
		return FMOD.FMOD_Sound_Set3DCustomRolloff(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Sound_GetNumSyncPoints
	 */
	public static int getNumSyncPoints(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_Sound_GetNumSyncPoints(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Sound_Get3DCustomRolloff
	 */
	public static int get3DCustomRolloff(long arg0, org.lwjgl.PointerBuffer arg1, java.nio.IntBuffer arg2)
	{
		return FMOD.FMOD_Sound_Get3DCustomRolloff(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_Sound_Set3DMinMaxDistance
	 */
	public static int set3DMinMaxDistance(long arg0, float arg1, float arg2)
	{
		return FMOD.FMOD_Sound_Set3DMinMaxDistance(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_Sound_Get3DMinMaxDistance
	 */
	public static int get3DMinMaxDistance(long arg0, java.nio.FloatBuffer arg1, java.nio.FloatBuffer arg2)
	{
		return FMOD.FMOD_Sound_Get3DMinMaxDistance(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_Sound_Set3DConeSettings
	 */
	public static int set3DConeSettings(long arg0, float arg1, float arg2, float arg3)
	{
		return FMOD.FMOD_Sound_Set3DConeSettings(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_Sound_Get3DConeSettings
	 */
	public static int get3DConeSettings(long arg0, java.nio.FloatBuffer arg1, java.nio.FloatBuffer arg2, java.nio.FloatBuffer arg3)
	{
		return FMOD.FMOD_Sound_Get3DConeSettings(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_Sound_GetName
	 */
	public static int getName(long arg0, java.nio.ByteBuffer arg1)
	{
		return FMOD.FMOD_Sound_GetName(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Sound_GetMode
	 */
	public static int getMode(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_Sound_GetMode(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Sound_SetMode
	 */
	public static int setMode(long arg0, int arg1)
	{
		return FMOD.FMOD_Sound_SetMode(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Sound_GetTag
	 */
	public static int getTag(long arg0, java.nio.ByteBuffer arg1, int arg2, org.lwjgl.fmod.FMOD_TAG arg3)
	{
		return FMOD.FMOD_Sound_GetTag(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_Sound_GetTag
	 */
	public static int getTag(long arg0, java.lang.CharSequence arg1, int arg2, org.lwjgl.fmod.FMOD_TAG arg3)
	{
		return FMOD.FMOD_Sound_GetTag(arg0, arg1, arg2, arg3);
	}

}
