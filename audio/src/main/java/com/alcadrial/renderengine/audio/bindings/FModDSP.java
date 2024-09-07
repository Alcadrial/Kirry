package com.alcadrial.renderengine.audio.bindings;

import org.lwjgl.fmod.FMOD;

public class FModDSP {

	public FModDSP()
	{

	}

	/**
	 *@see FMOD#FMOD_DSP_GetParameterInt
	 */
	public static int getParameterInt(long arg0, int arg1, java.nio.IntBuffer arg2, java.nio.ByteBuffer arg3)
	{
		return FMOD.FMOD_DSP_GetParameterInt(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_DSP_GetChannelFormat
	 */
	public static int getChannelFormat(long arg0, java.nio.IntBuffer arg1, java.nio.IntBuffer arg2, java.nio.IntBuffer arg3)
	{
		return FMOD.FMOD_DSP_GetChannelFormat(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_DSP_SetParameterBool
	 */
	public static int setParameterBool(long arg0, int arg1, int arg2)
	{
		return FMOD.FMOD_DSP_SetParameterBool(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_DSP_GetUserData
	 */
	public static int getUserData(long arg0, org.lwjgl.PointerBuffer arg1)
	{
		return FMOD.FMOD_DSP_GetUserData(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_DSP_SetParameterInt
	 */
	public static int setParameterInt(long arg0, int arg1, int arg2)
	{
		return FMOD.FMOD_DSP_SetParameterInt(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_DSP_GetNumParameters
	 */
	public static int getNumParameters(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_DSP_GetNumParameters(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_DSP_DisconnectFrom
	 */
	public static int disconnectFrom(long arg0, long arg1, long arg2)
	{
		return FMOD.FMOD_DSP_DisconnectFrom(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_DSP_SetChannelFormat
	 */
	public static int setChannelFormat(long arg0, int arg1, int arg2, int arg3)
	{
		return FMOD.FMOD_DSP_SetChannelFormat(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_DSP_SetParameterFloat
	 */
	public static int setParameterFloat(long arg0, int arg1, float arg2)
	{
		return FMOD.FMOD_DSP_SetParameterFloat(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_DSP_GetParameterBool
	 */
	public static int getParameterBool(long arg0, int arg1, java.nio.IntBuffer arg2, java.nio.ByteBuffer arg3)
	{
		return FMOD.FMOD_DSP_GetParameterBool(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_DSP_GetMeteringInfo
	 */
	public static int getMeteringInfo(long arg0, org.lwjgl.fmod.FMOD_DSP_METERING_INFO arg1, org.lwjgl.fmod.FMOD_DSP_METERING_INFO arg2)
	{
		return FMOD.FMOD_DSP_GetMeteringInfo(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_DSP_SetWetDryMix
	 */
	public static int setWetDryMix(long arg0, float arg1, float arg2, float arg3)
	{
		return FMOD.FMOD_DSP_SetWetDryMix(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_DSP_GetNumOutputs
	 */
	public static int getNumOutputs(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_DSP_GetNumOutputs(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_DSP_GetParameterFloat
	 */
	public static int getParameterFloat(long arg0, int arg1, java.nio.FloatBuffer arg2, java.nio.ByteBuffer arg3)
	{
		return FMOD.FMOD_DSP_GetParameterFloat(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_DSP_GetParameterData
	 */
	public static int getParameterData(long arg0, int arg1, org.lwjgl.PointerBuffer arg2, java.nio.IntBuffer arg3, java.nio.ByteBuffer arg4)
	{
		return FMOD.FMOD_DSP_GetParameterData(arg0, arg1, arg2, arg3, arg4);
	}

	/**
	 *@see FMOD#FMOD_DSP_GetSystemObject
	 */
	public static int getSystemObject(long arg0, org.lwjgl.PointerBuffer arg1)
	{
		return FMOD.FMOD_DSP_GetSystemObject(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_DSP_GetNumInputs
	 */
	public static int getNumInputs(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_DSP_GetNumInputs(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_DSP_GetParameterInfo
	 */
	public static int getParameterInfo(long arg0, int arg1, org.lwjgl.PointerBuffer arg2)
	{
		return FMOD.FMOD_DSP_GetParameterInfo(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_DSP_DisconnectAll
	 */
	public static int disconnectAll(long arg0, int arg1, int arg2)
	{
		return FMOD.FMOD_DSP_DisconnectAll(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_DSP_SetCallback
	 */
	public static int setCallback(long arg0, org.lwjgl.fmod.FMOD_DSP_CALLBACKI arg1)
	{
		return FMOD.FMOD_DSP_SetCallback(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_DSP_SetParameterData
	 */
	public static int setParameterData(long arg0, int arg1, java.nio.ByteBuffer arg2)
	{
		return FMOD.FMOD_DSP_SetParameterData(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_DSP_GetWetDryMix
	 */
	public static int getWetDryMix(long arg0, java.nio.FloatBuffer arg1, java.nio.FloatBuffer arg2, java.nio.FloatBuffer arg3)
	{
		return FMOD.FMOD_DSP_GetWetDryMix(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_DSP_ShowConfigDialog
	 */
	public static int showConfigDialog(long arg0, long arg1, int arg2)
	{
		return FMOD.FMOD_DSP_ShowConfigDialog(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_DSP_SetUserData
	 */
	public static int setUserData(long arg0, long arg1)
	{
		return FMOD.FMOD_DSP_SetUserData(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_DSP_GetCPUUsage
	 */
	public static int getCPUUsage(long arg0, java.nio.IntBuffer arg1, java.nio.IntBuffer arg2)
	{
		return FMOD.FMOD_DSP_GetCPUUsage(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_DSP_GetBypass
	 */
	public static int getBypass(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_DSP_GetBypass(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_DSP_Reset
	 */
	public static int reset(long arg0)
	{
		return FMOD.FMOD_DSP_Reset(arg0);
	}

	/**
	 *@see FMOD#FMOD_DSP_GetInput
	 */
	public static int getInput(long arg0, int arg1, org.lwjgl.PointerBuffer arg2, org.lwjgl.PointerBuffer arg3)
	{
		return FMOD.FMOD_DSP_GetInput(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_DSP_SetActive
	 */
	public static int setActive(long arg0, int arg1)
	{
		return FMOD.FMOD_DSP_SetActive(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_DSP_GetInfo
	 */
	public static int getInfo(long arg0, java.nio.ByteBuffer arg1, java.nio.IntBuffer arg2, java.nio.IntBuffer arg3, java.nio.IntBuffer arg4, java.nio.IntBuffer arg5)
	{
		return FMOD.FMOD_DSP_GetInfo(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	/**
	 *@see FMOD#FMOD_DSP_GetOutput
	 */
	public static int getOutput(long arg0, int arg1, org.lwjgl.PointerBuffer arg2, org.lwjgl.PointerBuffer arg3)
	{
		return FMOD.FMOD_DSP_GetOutput(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_DSP_AddInput
	 */
	public static int addInput(long arg0, long arg1, org.lwjgl.PointerBuffer arg2, int arg3)
	{
		return FMOD.FMOD_DSP_AddInput(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_DSP_SetBypass
	 */
	public static int setBypass(long arg0, int arg1)
	{
		return FMOD.FMOD_DSP_SetBypass(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_DSP_GetActive
	 */
	public static int getActive(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_DSP_GetActive(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_DSP_Release
	 */
	public static int release(long arg0)
	{
		return FMOD.FMOD_DSP_Release(arg0);
	}

	/**
	 *@see FMOD#FMOD_DSP_GetType
	 */
	public static int getType(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_DSP_GetType(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_DSP_GetIdle
	 */
	public static int getIdle(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_DSP_GetIdle(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_DSP_GetOutputChannelFormat
	 */
	public static int getOutputChannelFormat(long arg0, int arg1, int arg2, int arg3, java.nio.IntBuffer arg4, java.nio.IntBuffer arg5, java.nio.IntBuffer arg6)
	{
		return FMOD.FMOD_DSP_GetOutputChannelFormat(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
	}

	/**
	 *@see FMOD#FMOD_DSP_GetMeteringEnabled
	 */
	public static int getMeteringEnabled(long arg0, java.nio.IntBuffer arg1, java.nio.IntBuffer arg2)
	{
		return FMOD.FMOD_DSP_GetMeteringEnabled(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_DSP_GetDataParameterIndex
	 */
	public static int getDataParameterIndex(long arg0, int arg1, java.nio.IntBuffer arg2)
	{
		return FMOD.FMOD_DSP_GetDataParameterIndex(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_DSP_SetMeteringEnabled
	 */
	public static int setMeteringEnabled(long arg0, int arg1, int arg2)
	{
		return FMOD.FMOD_DSP_SetMeteringEnabled(arg0, arg1, arg2);
	}

}
