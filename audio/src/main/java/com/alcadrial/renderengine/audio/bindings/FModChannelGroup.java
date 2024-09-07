package com.alcadrial.renderengine.audio.bindings;

import org.lwjgl.fmod.FMOD;

public class FModChannelGroup {

	public FModChannelGroup()
	{

	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_Stop
	 */
	public static int stop(long arg0)
	{
		return FMOD.FMOD_ChannelGroup_Stop(arg0);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_SetPitch
	 */
	public static int setPitch(long arg0, float arg1)
	{
		return FMOD.FMOD_ChannelGroup_SetPitch(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_GetMute
	 */
	public static int getMute(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_ChannelGroup_GetMute(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_GetPitch
	 */
	public static int getPitch(long arg0, java.nio.FloatBuffer arg1)
	{
		return FMOD.FMOD_ChannelGroup_GetPitch(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_SetMute
	 */
	public static int setMute(long arg0, int arg1)
	{
		return FMOD.FMOD_ChannelGroup_SetMute(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_SetPan
	 */
	public static int setPan(long arg0, float arg1)
	{
		return FMOD.FMOD_ChannelGroup_SetPan(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_SetDelay
	 */
	public static int setDelay(long arg0, long arg1, long arg2, int arg3)
	{
		return FMOD.FMOD_ChannelGroup_SetDelay(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_GetDelay
	 */
	public static int getDelay(long arg0, java.nio.LongBuffer arg1, java.nio.LongBuffer arg2, java.nio.IntBuffer arg3)
	{
		return FMOD.FMOD_ChannelGroup_GetDelay(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_GetDSP
	 */
	public static int getDSP(long arg0, int arg1, org.lwjgl.PointerBuffer arg2)
	{
		return FMOD.FMOD_ChannelGroup_GetDSP(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_SetMode
	 */
	public static int setMode(long arg0, int arg1)
	{
		return FMOD.FMOD_ChannelGroup_SetMode(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_AddDSP
	 */
	public static int addDSP(long arg0, int arg1, long arg2)
	{
		return FMOD.FMOD_ChannelGroup_AddDSP(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_GetMode
	 */
	public static int getMode(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_ChannelGroup_GetMode(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_AddGroup
	 */
	public static int addGroup(long arg0, long arg1, int arg2, org.lwjgl.PointerBuffer arg3)
	{
		return FMOD.FMOD_ChannelGroup_AddGroup(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_GetName
	 */
	public static int getName(long arg0, java.nio.ByteBuffer arg1)
	{
		return FMOD.FMOD_ChannelGroup_GetName(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_GetGroup
	 */
	public static int getGroup(long arg0, int arg1, org.lwjgl.PointerBuffer arg2)
	{
		return FMOD.FMOD_ChannelGroup_GetGroup(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_Release
	 */
	public static int release(long arg0)
	{
		return FMOD.FMOD_ChannelGroup_Release(arg0);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_SetMixLevelsOutput
	 */
	public static int setMixLevelsOutput(long arg0, float arg1, float arg2, float arg3, float arg4, float arg5, float arg6, float arg7, float arg8)
	{
		return FMOD.FMOD_ChannelGroup_SetMixLevelsOutput(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_Set3DConeSettings
	 */
	public static int set3DConeSettings(long arg0, float arg1, float arg2, float arg3)
	{
		return FMOD.FMOD_ChannelGroup_Set3DConeSettings(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_Get3DConeSettings
	 */
	public static int get3DConeSettings(long arg0, java.nio.FloatBuffer arg1, java.nio.FloatBuffer arg2, java.nio.FloatBuffer arg3)
	{
		return FMOD.FMOD_ChannelGroup_Get3DConeSettings(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_SetReverbProperties
	 */
	public static int setReverbProperties(long arg0, int arg1, float arg2)
	{
		return FMOD.FMOD_ChannelGroup_SetReverbProperties(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_SetMixLevelsInput
	 */
	public static int setMixLevelsInput(long arg0, java.nio.FloatBuffer arg1)
	{
		return FMOD.FMOD_ChannelGroup_SetMixLevelsInput(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_Set3DMinMaxDistance
	 */
	public static int set3DMinMaxDistance(long arg0, float arg1, float arg2)
	{
		return FMOD.FMOD_ChannelGroup_Set3DMinMaxDistance(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_GetReverbProperties
	 */
	public static int getReverbProperties(long arg0, int arg1, java.nio.FloatBuffer arg2)
	{
		return FMOD.FMOD_ChannelGroup_GetReverbProperties(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_Get3DMinMaxDistance
	 */
	public static int get3DMinMaxDistance(long arg0, java.nio.FloatBuffer arg1, java.nio.FloatBuffer arg2)
	{
		return FMOD.FMOD_ChannelGroup_Get3DMinMaxDistance(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_SetVolume
	 */
	public static int setVolume(long arg0, float arg1)
	{
		return FMOD.FMOD_ChannelGroup_SetVolume(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_SetLowPassGain
	 */
	public static int setLowPassGain(long arg0, float arg1)
	{
		return FMOD.FMOD_ChannelGroup_SetLowPassGain(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_GetFadePoints
	 */
	public static int getFadePoints(long arg0, java.nio.IntBuffer arg1, java.nio.LongBuffer arg2, java.nio.FloatBuffer arg3)
	{
		return FMOD.FMOD_ChannelGroup_GetFadePoints(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_GetLowPassGain
	 */
	public static int getLowPassGain(long arg0, java.nio.FloatBuffer arg1)
	{
		return FMOD.FMOD_ChannelGroup_GetLowPassGain(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_AddFadePoint
	 */
	public static int addFadePoint(long arg0, long arg1, float arg2)
	{
		return FMOD.FMOD_ChannelGroup_AddFadePoint(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_RemoveDSP
	 */
	public static int removeDSP(long arg0, long arg1)
	{
		return FMOD.FMOD_ChannelGroup_RemoveDSP(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_GetDSPIndex
	 */
	public static int getDSPIndex(long arg0, long arg1, java.nio.IntBuffer arg2)
	{
		return FMOD.FMOD_ChannelGroup_GetDSPIndex(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_SetMixMatrix
	 */
	public static int setMixMatrix(long arg0, java.nio.FloatBuffer arg1, int arg2, int arg3, int arg4)
	{
		return FMOD.FMOD_ChannelGroup_SetMixMatrix(arg0, arg1, arg2, arg3, arg4);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_GetDSPClock
	 */
	public static int getDSPClock(long arg0, java.nio.LongBuffer arg1, java.nio.LongBuffer arg2)
	{
		return FMOD.FMOD_ChannelGroup_GetDSPClock(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_Set3DAttributes
	 */
	public static int set3DAttributes(long arg0, org.lwjgl.fmod.FMOD_VECTOR arg1, org.lwjgl.fmod.FMOD_VECTOR arg2)
	{
		return FMOD.FMOD_ChannelGroup_Set3DAttributes(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_GetMixMatrix
	 */
	public static int getMixMatrix(long arg0, java.nio.FloatBuffer arg1, java.nio.IntBuffer arg2, java.nio.IntBuffer arg3, int arg4)
	{
		return FMOD.FMOD_ChannelGroup_GetMixMatrix(arg0, arg1, arg2, arg3, arg4);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_SetDSPIndex
	 */
	public static int setDSPIndex(long arg0, long arg1, int arg2)
	{
		return FMOD.FMOD_ChannelGroup_SetDSPIndex(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_RemoveFadePoints
	 */
	public static int removeFadePoints(long arg0, long arg1, long arg2)
	{
		return FMOD.FMOD_ChannelGroup_RemoveFadePoints(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_Get3DOcclusion
	 */
	public static int get3DOcclusion(long arg0, java.nio.FloatBuffer arg1, java.nio.FloatBuffer arg2)
	{
		return FMOD.FMOD_ChannelGroup_Get3DOcclusion(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_SetFadePointRamp
	 */
	public static int setFadePointRamp(long arg0, long arg1, float arg2)
	{
		return FMOD.FMOD_ChannelGroup_SetFadePointRamp(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_GetVolume
	 */
	public static int getVolume(long arg0, java.nio.FloatBuffer arg1)
	{
		return FMOD.FMOD_ChannelGroup_GetVolume(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_IsPlaying
	 */
	public static int isPlaying(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_ChannelGroup_IsPlaying(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_Get3DSpread
	 */
	public static int get3DSpread(long arg0, java.nio.FloatBuffer arg1)
	{
		return FMOD.FMOD_ChannelGroup_Get3DSpread(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_Set3DLevel
	 */
	public static int set3DLevel(long arg0, float arg1)
	{
		return FMOD.FMOD_ChannelGroup_Set3DLevel(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_Get3DLevel
	 */
	public static int get3DLevel(long arg0, java.nio.FloatBuffer arg1)
	{
		return FMOD.FMOD_ChannelGroup_Get3DLevel(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_SetUserData
	 */
	public static int setUserData(long arg0, long arg1)
	{
		return FMOD.FMOD_ChannelGroup_SetUserData(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_GetVolumeRamp
	 */
	public static int getVolumeRamp(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_ChannelGroup_GetVolumeRamp(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_Set3DSpread
	 */
	public static int set3DSpread(long arg0, float arg1)
	{
		return FMOD.FMOD_ChannelGroup_Set3DSpread(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_GetSystemObject
	 */
	public static int getSystemObject(long arg0, org.lwjgl.PointerBuffer arg1)
	{
		return FMOD.FMOD_ChannelGroup_GetSystemObject(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_GetUserData
	 */
	public static int getUserData(long arg0, org.lwjgl.PointerBuffer arg1)
	{
		return FMOD.FMOD_ChannelGroup_GetUserData(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_Get3DAttributes
	 */
	public static int get3DAttributes(long arg0, org.lwjgl.fmod.FMOD_VECTOR arg1, org.lwjgl.fmod.FMOD_VECTOR arg2)
	{
		return FMOD.FMOD_ChannelGroup_Get3DAttributes(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_SetVolumeRamp
	 */
	public static int setVolumeRamp(long arg0, int arg1)
	{
		return FMOD.FMOD_ChannelGroup_SetVolumeRamp(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_SetCallback
	 */
	public static int setCallback(long arg0, org.lwjgl.fmod.FMOD_CHANNELCONTROL_CALLBACKI arg1)
	{
		return FMOD.FMOD_ChannelGroup_SetCallback(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_GetNumDSPs
	 */
	public static int getNumDSPs(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_ChannelGroup_GetNumDSPs(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_Set3DOcclusion
	 */
	public static int set3DOcclusion(long arg0, float arg1, float arg2)
	{
		return FMOD.FMOD_ChannelGroup_Set3DOcclusion(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_GetNumGroups
	 */
	public static int getNumGroups(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_ChannelGroup_GetNumGroups(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_SetPaused
	 */
	public static int setPaused(long arg0, int arg1)
	{
		return FMOD.FMOD_ChannelGroup_SetPaused(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_GetAudibility
	 */
	public static int getAudibility(long arg0, java.nio.FloatBuffer arg1)
	{
		return FMOD.FMOD_ChannelGroup_GetAudibility(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_GetPaused
	 */
	public static int getPaused(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_ChannelGroup_GetPaused(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_GetNumChannels
	 */
	public static int getNumChannels(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_ChannelGroup_GetNumChannels(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_GetChannel
	 */
	public static int getChannel(long arg0, int arg1, org.lwjgl.PointerBuffer arg2)
	{
		return FMOD.FMOD_ChannelGroup_GetChannel(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_GetParentGroup
	 */
	public static int getParentGroup(long arg0, org.lwjgl.PointerBuffer arg1)
	{
		return FMOD.FMOD_ChannelGroup_GetParentGroup(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_Set3DDopplerLevel
	 */
	public static int set3DDopplerLevel(long arg0, float arg1)
	{
		return FMOD.FMOD_ChannelGroup_Set3DDopplerLevel(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_Get3DDopplerLevel
	 */
	public static int get3DDopplerLevel(long arg0, java.nio.FloatBuffer arg1)
	{
		return FMOD.FMOD_ChannelGroup_Get3DDopplerLevel(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_Set3DCustomRolloff
	 */
	public static int set3DCustomRolloff(long arg0, org.lwjgl.fmod.FMOD_VECTOR.Buffer arg1)
	{
		return FMOD.FMOD_ChannelGroup_Set3DCustomRolloff(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_Get3DConeOrientation
	 */
	public static int get3DConeOrientation(long arg0, org.lwjgl.fmod.FMOD_VECTOR arg1)
	{
		return FMOD.FMOD_ChannelGroup_Get3DConeOrientation(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_Get3DDistanceFilter
	 */
	public static int get3DDistanceFilter(long arg0, java.nio.IntBuffer arg1, java.nio.FloatBuffer arg2, java.nio.FloatBuffer arg3)
	{
		return FMOD.FMOD_ChannelGroup_Get3DDistanceFilter(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_Set3DConeOrientation
	 */
	public static int set3DConeOrientation(long arg0, org.lwjgl.fmod.FMOD_VECTOR arg1)
	{
		return FMOD.FMOD_ChannelGroup_Set3DConeOrientation(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_Get3DCustomRolloff
	 */
	public static int get3DCustomRolloff(long arg0, org.lwjgl.PointerBuffer arg1, java.nio.IntBuffer arg2)
	{
		return FMOD.FMOD_ChannelGroup_Get3DCustomRolloff(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_ChannelGroup_Set3DDistanceFilter
	 */
	public static int set3DDistanceFilter(long arg0, int arg1, float arg2, float arg3)
	{
		return FMOD.FMOD_ChannelGroup_Set3DDistanceFilter(arg0, arg1, arg2, arg3);
	}

}
