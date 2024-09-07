package com.alcadrial.renderengine.audio.bindings;

import org.lwjgl.fmod.FMOD;

public class FModChannel {

	public FModChannel()
	{

	}

	/**
	 *@see FMOD#FMOD_Channel_SetPan
	 */
	public static int setPan(long arg0, float arg1)
	{
		return FMOD.FMOD_Channel_SetPan(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_SetPitch
	 */
	public static int setPitch(long arg0, float arg1)
	{
		return FMOD.FMOD_Channel_SetPitch(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_GetMute
	 */
	public static int getMute(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_Channel_GetMute(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_SetMute
	 */
	public static int setMute(long arg0, int arg1)
	{
		return FMOD.FMOD_Channel_SetMute(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_GetPaused
	 */
	public static int getPaused(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_Channel_GetPaused(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_SetPaused
	 */
	public static int setPaused(long arg0, int arg1)
	{
		return FMOD.FMOD_Channel_SetPaused(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_SetVolumeRamp
	 */
	public static int setVolumeRamp(long arg0, int arg1)
	{
		return FMOD.FMOD_Channel_SetVolumeRamp(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_GetPitch
	 */
	public static int getPitch(long arg0, java.nio.FloatBuffer arg1)
	{
		return FMOD.FMOD_Channel_GetPitch(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_SetCallback
	 */
	public static int setCallback(long arg0, org.lwjgl.fmod.FMOD_CHANNELCONTROL_CALLBACKI arg1)
	{
		return FMOD.FMOD_Channel_SetCallback(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_GetAudibility
	 */
	public static int getAudibility(long arg0, java.nio.FloatBuffer arg1)
	{
		return FMOD.FMOD_Channel_GetAudibility(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_SetVolume
	 */
	public static int setVolume(long arg0, float arg1)
	{
		return FMOD.FMOD_Channel_SetVolume(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_GetVolume
	 */
	public static int getVolume(long arg0, java.nio.FloatBuffer arg1)
	{
		return FMOD.FMOD_Channel_GetVolume(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_GetVolumeRamp
	 */
	public static int getVolumeRamp(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_Channel_GetVolumeRamp(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_SetMode
	 */
	public static int setMode(long arg0, int arg1)
	{
		return FMOD.FMOD_Channel_SetMode(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_GetMode
	 */
	public static int getMode(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_Channel_GetMode(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_IsPlaying
	 */
	public static int isPlaying(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_Channel_IsPlaying(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_GetLoopCount
	 */
	public static int getLoopCount(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_Channel_GetLoopCount(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_Set3DLevel
	 */
	public static int set3DLevel(long arg0, float arg1)
	{
		return FMOD.FMOD_Channel_Set3DLevel(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_GetPriority
	 */
	public static int getPriority(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_Channel_GetPriority(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_GetLoopPoints
	 */
	public static int getLoopPoints(long arg0, java.nio.IntBuffer arg1, int arg2, java.nio.IntBuffer arg3, int arg4)
	{
		return FMOD.FMOD_Channel_GetLoopPoints(arg0, arg1, arg2, arg3, arg4);
	}

	/**
	 *@see FMOD#FMOD_Channel_GetNumDSPs
	 */
	public static int getNumDSPs(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_Channel_GetNumDSPs(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_AddFadePoint
	 */
	public static int addFadePoint(long arg0, long arg1, float arg2)
	{
		return FMOD.FMOD_Channel_AddFadePoint(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_Channel_GetIndex
	 */
	public static int getIndex(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_Channel_GetIndex(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_SetDelay
	 */
	public static int setDelay(long arg0, long arg1, long arg2, int arg3)
	{
		return FMOD.FMOD_Channel_SetDelay(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_Channel_GetDSP
	 */
	public static int getDSP(long arg0, int arg1, org.lwjgl.PointerBuffer arg2)
	{
		return FMOD.FMOD_Channel_GetDSP(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_Channel_GetFadePoints
	 */
	public static int getFadePoints(long arg0, java.nio.IntBuffer arg1, java.nio.LongBuffer arg2, java.nio.FloatBuffer arg3)
	{
		return FMOD.FMOD_Channel_GetFadePoints(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_Channel_GetDelay
	 */
	public static int getDelay(long arg0, java.nio.LongBuffer arg1, java.nio.LongBuffer arg2, java.nio.IntBuffer arg3)
	{
		return FMOD.FMOD_Channel_GetDelay(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_Channel_IsVirtual
	 */
	public static int isVirtual(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_Channel_IsVirtual(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_GetMixMatrix
	 */
	public static int getMixMatrix(long arg0, java.nio.FloatBuffer arg1, java.nio.IntBuffer arg2, java.nio.IntBuffer arg3, int arg4)
	{
		return FMOD.FMOD_Channel_GetMixMatrix(arg0, arg1, arg2, arg3, arg4);
	}

	/**
	 *@see FMOD#FMOD_Channel_SetDSPIndex
	 */
	public static int setDSPIndex(long arg0, long arg1, int arg2)
	{
		return FMOD.FMOD_Channel_SetDSPIndex(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_Channel_GetPosition
	 */
	public static int getPosition(long arg0, java.nio.IntBuffer arg1, int arg2)
	{
		return FMOD.FMOD_Channel_GetPosition(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_Channel_RemoveDSP
	 */
	public static int removeDSP(long arg0, long arg1)
	{
		return FMOD.FMOD_Channel_RemoveDSP(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_SetFrequency
	 */
	public static int setFrequency(long arg0, float arg1)
	{
		return FMOD.FMOD_Channel_SetFrequency(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_GetUserData
	 */
	public static int getUserData(long arg0, org.lwjgl.PointerBuffer arg1)
	{
		return FMOD.FMOD_Channel_GetUserData(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_GetDSPIndex
	 */
	public static int getDSPIndex(long arg0, long arg1, java.nio.IntBuffer arg2)
	{
		return FMOD.FMOD_Channel_GetDSPIndex(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_Channel_Get3DLevel
	 */
	public static int get3DLevel(long arg0, java.nio.FloatBuffer arg1)
	{
		return FMOD.FMOD_Channel_Get3DLevel(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_SetUserData
	 */
	public static int setUserData(long arg0, long arg1)
	{
		return FMOD.FMOD_Channel_SetUserData(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_Set3DSpread
	 */
	public static int set3DSpread(long arg0, float arg1)
	{
		return FMOD.FMOD_Channel_Set3DSpread(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_SetPriority
	 */
	public static int setPriority(long arg0, int arg1)
	{
		return FMOD.FMOD_Channel_SetPriority(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_AddDSP
	 */
	public static int addDSP(long arg0, int arg1, long arg2)
	{
		return FMOD.FMOD_Channel_AddDSP(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_Channel_SetLoopCount
	 */
	public static int setLoopCount(long arg0, int arg1)
	{
		return FMOD.FMOD_Channel_SetLoopCount(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_SetMixMatrix
	 */
	public static int setMixMatrix(long arg0, java.nio.FloatBuffer arg1, int arg2, int arg3, int arg4)
	{
		return FMOD.FMOD_Channel_SetMixMatrix(arg0, arg1, arg2, arg3, arg4);
	}

	/**
	 *@see FMOD#FMOD_Channel_SetLoopPoints
	 */
	public static int setLoopPoints(long arg0, int arg1, int arg2, int arg3, int arg4)
	{
		return FMOD.FMOD_Channel_SetLoopPoints(arg0, arg1, arg2, arg3, arg4);
	}

	/**
	 *@see FMOD#FMOD_Channel_Get3DSpread
	 */
	public static int get3DSpread(long arg0, java.nio.FloatBuffer arg1)
	{
		return FMOD.FMOD_Channel_Get3DSpread(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_GetFrequency
	 */
	public static int getFrequency(long arg0, java.nio.FloatBuffer arg1)
	{
		return FMOD.FMOD_Channel_GetFrequency(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_GetDSPClock
	 */
	public static int getDSPClock(long arg0, java.nio.LongBuffer arg1, java.nio.LongBuffer arg2)
	{
		return FMOD.FMOD_Channel_GetDSPClock(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_Channel_SetPosition
	 */
	public static int setPosition(long arg0, int arg1, int arg2)
	{
		return FMOD.FMOD_Channel_SetPosition(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_Channel_SetReverbProperties
	 */
	public static int setReverbProperties(long arg0, int arg1, float arg2)
	{
		return FMOD.FMOD_Channel_SetReverbProperties(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_Channel_Set3DMinMaxDistance
	 */
	public static int set3DMinMaxDistance(long arg0, float arg1, float arg2)
	{
		return FMOD.FMOD_Channel_Set3DMinMaxDistance(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_Channel_Get3DMinMaxDistance
	 */
	public static int get3DMinMaxDistance(long arg0, java.nio.FloatBuffer arg1, java.nio.FloatBuffer arg2)
	{
		return FMOD.FMOD_Channel_Get3DMinMaxDistance(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_Channel_Set3DConeSettings
	 */
	public static int set3DConeSettings(long arg0, float arg1, float arg2, float arg3)
	{
		return FMOD.FMOD_Channel_Set3DConeSettings(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_Channel_Get3DConeSettings
	 */
	public static int get3DConeSettings(long arg0, java.nio.FloatBuffer arg1, java.nio.FloatBuffer arg2, java.nio.FloatBuffer arg3)
	{
		return FMOD.FMOD_Channel_Get3DConeSettings(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_Channel_Get3DCustomRolloff
	 */
	public static int get3DCustomRolloff(long arg0, org.lwjgl.PointerBuffer arg1, java.nio.IntBuffer arg2)
	{
		return FMOD.FMOD_Channel_Get3DCustomRolloff(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_Channel_Get3DConeOrientation
	 */
	public static int get3DConeOrientation(long arg0, org.lwjgl.fmod.FMOD_VECTOR arg1)
	{
		return FMOD.FMOD_Channel_Get3DConeOrientation(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_Set3DAttributes
	 */
	public static int set3DAttributes(long arg0, org.lwjgl.fmod.FMOD_VECTOR arg1, org.lwjgl.fmod.FMOD_VECTOR arg2)
	{
		return FMOD.FMOD_Channel_Set3DAttributes(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_Channel_Set3DDopplerLevel
	 */
	public static int set3DDopplerLevel(long arg0, float arg1)
	{
		return FMOD.FMOD_Channel_Set3DDopplerLevel(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_GetSystemObject
	 */
	public static int getSystemObject(long arg0, org.lwjgl.PointerBuffer arg1)
	{
		return FMOD.FMOD_Channel_GetSystemObject(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_SetMixLevelsInput
	 */
	public static int setMixLevelsInput(long arg0, java.nio.FloatBuffer arg1)
	{
		return FMOD.FMOD_Channel_SetMixLevelsInput(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_GetReverbProperties
	 */
	public static int getReverbProperties(long arg0, int arg1, java.nio.FloatBuffer arg2)
	{
		return FMOD.FMOD_Channel_GetReverbProperties(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_Channel_SetMixLevelsOutput
	 */
	public static int setMixLevelsOutput(long arg0, float arg1, float arg2, float arg3, float arg4, float arg5, float arg6, float arg7, float arg8)
	{
		return FMOD.FMOD_Channel_SetMixLevelsOutput(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);
	}

	/**
	 *@see FMOD#FMOD_Channel_SetFadePointRamp
	 */
	public static int setFadePointRamp(long arg0, long arg1, float arg2)
	{
		return FMOD.FMOD_Channel_SetFadePointRamp(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_Channel_Set3DOcclusion
	 */
	public static int set3DOcclusion(long arg0, float arg1, float arg2)
	{
		return FMOD.FMOD_Channel_Set3DOcclusion(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_Channel_Get3DDopplerLevel
	 */
	public static int get3DDopplerLevel(long arg0, java.nio.FloatBuffer arg1)
	{
		return FMOD.FMOD_Channel_Get3DDopplerLevel(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_Set3DDistanceFilter
	 */
	public static int set3DDistanceFilter(long arg0, int arg1, float arg2, float arg3)
	{
		return FMOD.FMOD_Channel_Set3DDistanceFilter(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_Channel_SetLowPassGain
	 */
	public static int setLowPassGain(long arg0, float arg1)
	{
		return FMOD.FMOD_Channel_SetLowPassGain(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_GetLowPassGain
	 */
	public static int getLowPassGain(long arg0, java.nio.FloatBuffer arg1)
	{
		return FMOD.FMOD_Channel_GetLowPassGain(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_Set3DConeOrientation
	 */
	public static int set3DConeOrientation(long arg0, org.lwjgl.fmod.FMOD_VECTOR arg1)
	{
		return FMOD.FMOD_Channel_Set3DConeOrientation(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_Get3DOcclusion
	 */
	public static int get3DOcclusion(long arg0, java.nio.FloatBuffer arg1, java.nio.FloatBuffer arg2)
	{
		return FMOD.FMOD_Channel_Get3DOcclusion(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_Channel_Get3DDistanceFilter
	 */
	public static int get3DDistanceFilter(long arg0, java.nio.IntBuffer arg1, java.nio.FloatBuffer arg2, java.nio.FloatBuffer arg3)
	{
		return FMOD.FMOD_Channel_Get3DDistanceFilter(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_Channel_Get3DAttributes
	 */
	public static int get3DAttributes(long arg0, org.lwjgl.fmod.FMOD_VECTOR arg1, org.lwjgl.fmod.FMOD_VECTOR arg2)
	{
		return FMOD.FMOD_Channel_Get3DAttributes(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_Channel_SetChannelGroup
	 */
	public static int setChannelGroup(long arg0, long arg1)
	{
		return FMOD.FMOD_Channel_SetChannelGroup(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_RemoveFadePoints
	 */
	public static int removeFadePoints(long arg0, long arg1, long arg2)
	{
		return FMOD.FMOD_Channel_RemoveFadePoints(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_Channel_Set3DCustomRolloff
	 */
	public static int set3DCustomRolloff(long arg0, org.lwjgl.fmod.FMOD_VECTOR.Buffer arg1)
	{
		return FMOD.FMOD_Channel_Set3DCustomRolloff(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_Stop
	 */
	public static int stop(long arg0)
	{
		return FMOD.FMOD_Channel_Stop(arg0);
	}

	/**
	 *@see FMOD#FMOD_Channel_GetCurrentSound
	 */
	public static int getCurrentSound(long arg0, org.lwjgl.PointerBuffer arg1)
	{
		return FMOD.FMOD_Channel_GetCurrentSound(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Channel_GetChannelGroup
	 */
	public static int getChannelGroup(long arg0, org.lwjgl.PointerBuffer arg1)
	{
		return FMOD.FMOD_Channel_GetChannelGroup(arg0, arg1);
	}

}
