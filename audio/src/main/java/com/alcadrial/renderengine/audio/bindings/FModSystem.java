package com.alcadrial.renderengine.audio.bindings;

import org.lwjgl.fmod.FMOD;

public class FModSystem {

	public FModSystem()
	{

	}

	/**
	 *@see FMOD#FMOD_System_Create
	 */
	public static int create(org.lwjgl.PointerBuffer arg0, int arg1)
	{
		return FMOD.FMOD_System_Create(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_System_Init
	 */
	public static int init(long arg0, int arg1, int arg2, long arg3)
	{
		return FMOD.FMOD_System_Init(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_System_Close
	 */
	public static int close(long arg0)
	{
		return FMOD.FMOD_System_Close(arg0);
	}

	/**
	 *@see FMOD#FMOD_System_Update
	 */
	public static int update(long arg0)
	{
		return FMOD.FMOD_System_Update(arg0);
	}

	/**
	 *@see FMOD#FMOD_System_Release
	 */
	public static int release(long arg0)
	{
		return FMOD.FMOD_System_Release(arg0);
	}

	/**
	 *@see FMOD#FMOD_System_GetOutput
	 */
	public static int getOutput(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_System_GetOutput(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_System_GetDriverInfo
	 */
	public static int getDriverInfo(long arg0, int arg1, java.nio.ByteBuffer arg2, org.lwjgl.fmod.FMOD_GUID arg3, java.nio.IntBuffer arg4, java.nio.IntBuffer arg5, java.nio.IntBuffer arg6)
	{
		return FMOD.FMOD_System_GetDriverInfo(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
	}

	/**
	 *@see FMOD#FMOD_System_GetNumDrivers
	 */
	public static int getNumDrivers(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_System_GetNumDrivers(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_System_SetCallback
	 */
	public static int setCallback(long arg0, org.lwjgl.fmod.FMOD_SYSTEM_CALLBACKI arg1, int arg2)
	{
		return FMOD.FMOD_System_SetCallback(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_System_SetDriver
	 */
	public static int setDriver(long arg0, int arg1)
	{
		return FMOD.FMOD_System_SetDriver(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_System_SetOutput
	 */
	public static int setOutput(long arg0, int arg1)
	{
		return FMOD.FMOD_System_SetOutput(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_System_GetDriver
	 */
	public static int getDriver(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_System_GetDriver(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_System_SetFileSystem
	 */
	public static int setFileSystem(long arg0, org.lwjgl.fmod.FMOD_FILE_OPEN_CALLBACKI arg1, org.lwjgl.fmod.FMOD_FILE_CLOSE_CALLBACKI arg2, org.lwjgl.fmod.FMOD_FILE_READ_CALLBACKI arg3, org.lwjgl.fmod.FMOD_FILE_SEEK_CALLBACKI arg4, org.lwjgl.fmod.FMOD_FILE_ASYNCREAD_CALLBACKI arg5, org.lwjgl.fmod.FMOD_FILE_ASYNCCANCEL_CALLBACKI arg6, int arg7)
	{
		return FMOD.FMOD_System_SetFileSystem(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
	}

	/**
	 *@see FMOD#FMOD_System_LoadPlugin
	 */
	public static int loadPlugin(long arg0, java.lang.CharSequence arg1, java.nio.IntBuffer arg2, int arg3)
	{
		return FMOD.FMOD_System_LoadPlugin(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_System_LoadPlugin
	 */
	public static int loadPlugin(long arg0, java.nio.ByteBuffer arg1, java.nio.IntBuffer arg2, int arg3)
	{
		return FMOD.FMOD_System_LoadPlugin(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_System_Get3DSettings
	 */
	public static int get3DSettings(long arg0, java.nio.FloatBuffer arg1, java.nio.FloatBuffer arg2, java.nio.FloatBuffer arg3)
	{
		return FMOD.FMOD_System_Get3DSettings(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_System_PlayDSP
	 */
	public static int playDSP(long arg0, long arg1, long arg2, int arg3, org.lwjgl.PointerBuffer arg4)
	{
		return FMOD.FMOD_System_PlayDSP(arg0, arg1, arg2, arg3, arg4);
	}

	/**
	 *@see FMOD#FMOD_System_IsRecording
	 */
	public static int isRecording(long arg0, int arg1, java.nio.IntBuffer arg2)
	{
		return FMOD.FMOD_System_IsRecording(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_System_CreateGeometry
	 */
	public static int createGeometry(long arg0, int arg1, int arg2, org.lwjgl.PointerBuffer arg3)
	{
		return FMOD.FMOD_System_CreateGeometry(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_System_GetUserData
	 */
	public static int getUserData(long arg0, org.lwjgl.PointerBuffer arg1)
	{
		return FMOD.FMOD_System_GetUserData(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_System_MixerResume
	 */
	public static int mixerResume(long arg0)
	{
		return FMOD.FMOD_System_MixerResume(arg0);
	}

	/**
	 *@see FMOD#FMOD_System_GetCPUUsage
	 */
	public static int getCPUUsage(long arg0, org.lwjgl.fmod.FMOD_CPU_USAGE arg1)
	{
		return FMOD.FMOD_System_GetCPUUsage(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_System_GetFileUsage
	 */
	public static int getFileUsage(long arg0, java.nio.LongBuffer arg1, java.nio.LongBuffer arg2, java.nio.LongBuffer arg3)
	{
		return FMOD.FMOD_System_GetFileUsage(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_System_CreateDSP
	 */
	public static int createDSP(long arg0, org.lwjgl.fmod.FMOD_DSP_DESCRIPTION arg1, org.lwjgl.PointerBuffer arg2)
	{
		return FMOD.FMOD_System_CreateDSP(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_System_GetChannel
	 */
	public static int getChannel(long arg0, int arg1, org.lwjgl.PointerBuffer arg2)
	{
		return FMOD.FMOD_System_GetChannel(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_System_UnlockDSP
	 */
	public static int unlockDSP(long arg0)
	{
		return FMOD.FMOD_System_UnlockDSP(arg0);
	}

	/**
	 *@see FMOD#FMOD_System_RecordStop
	 */
	public static int recordStop(long arg0, int arg1)
	{
		return FMOD.FMOD_System_RecordStop(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_System_UnloadPlugin
	 */
	public static int unloadPlugin(long arg0, int arg1)
	{
		return FMOD.FMOD_System_UnloadPlugin(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_System_CreateStream
	 */
	public static int createStream(long arg0, java.nio.ByteBuffer arg1, int arg2, org.lwjgl.fmod.FMOD_CREATESOUNDEXINFO arg3, org.lwjgl.PointerBuffer arg4)
	{
		return FMOD.FMOD_System_CreateStream(arg0, arg1, arg2, arg3, arg4);
	}

	/**
	 *@see FMOD#FMOD_System_Set3DSettings
	 */
	public static int set3DSettings(long arg0, float arg1, float arg2, float arg3)
	{
		return FMOD.FMOD_System_Set3DSettings(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_System_SetPluginPath
	 */
	public static int setPluginPath(long arg0, java.nio.ByteBuffer arg1)
	{
		return FMOD.FMOD_System_SetPluginPath(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_System_SetPluginPath
	 */
	public static int setPluginPath(long arg0, java.lang.CharSequence arg1)
	{
		return FMOD.FMOD_System_SetPluginPath(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_System_RegisterOutput
	 */
	public static int registerOutput(long arg0, org.lwjgl.fmod.FMOD_OUTPUT_DESCRIPTION arg1, java.nio.IntBuffer arg2)
	{
		return FMOD.FMOD_System_RegisterOutput(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_System_CreateReverb3D
	 */
	public static int createReverb3D(long arg0, org.lwjgl.PointerBuffer arg1)
	{
		return FMOD.FMOD_System_CreateReverb3D(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_System_GetNumPlugins
	 */
	public static int getNumPlugins(long arg0, int arg1, java.nio.IntBuffer arg2)
	{
		return FMOD.FMOD_System_GetNumPlugins(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_System_LockDSP
	 */
	public static int lockDSP(long arg0)
	{
		return FMOD.FMOD_System_LockDSP(arg0);
	}

	/**
	 *@see FMOD#FMOD_System_RegisterCodec
	 */
	public static int registerCodec(long arg0, org.lwjgl.fmod.FMOD_CODEC_DESCRIPTION arg1, java.nio.IntBuffer arg2, int arg3)
	{
		return FMOD.FMOD_System_RegisterCodec(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_System_CreateSound
	 */
	public static int createSound(long arg0, java.nio.ByteBuffer arg1, int arg2, org.lwjgl.fmod.FMOD_CREATESOUNDEXINFO arg3, org.lwjgl.PointerBuffer arg4)
	{
		return FMOD.FMOD_System_CreateSound(arg0, arg1, arg2, arg3, arg4);
	}

	/**
	 *@see FMOD#FMOD_System_GetPluginInfo
	 */
	public static int getPluginInfo(long arg0, int arg1, java.nio.IntBuffer arg2, java.nio.ByteBuffer arg3, java.nio.IntBuffer arg4)
	{
		return FMOD.FMOD_System_GetPluginInfo(arg0, arg1, arg2, arg3, arg4);
	}

	/**
	 *@see FMOD#FMOD_System_GetVersion
	 */
	public static int getVersion(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_System_GetVersion(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_System_MixerSuspend
	 */
	public static int mixerSuspend(long arg0)
	{
		return FMOD.FMOD_System_MixerSuspend(arg0);
	}

	/**
	 *@see FMOD#FMOD_System_PlaySound
	 */
	public static int playSound(long arg0, long arg1, long arg2, int arg3, org.lwjgl.PointerBuffer arg4)
	{
		return FMOD.FMOD_System_PlaySound(arg0, arg1, arg2, arg3, arg4);
	}

	/**
	 *@see FMOD#FMOD_System_RecordStart
	 */
	public static int recordStart(long arg0, int arg1, long arg2, int arg3)
	{
		return FMOD.FMOD_System_RecordStart(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_System_LoadGeometry
	 */
	public static int loadGeometry(long arg0, java.nio.ByteBuffer arg1, org.lwjgl.PointerBuffer arg2)
	{
		return FMOD.FMOD_System_LoadGeometry(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_System_RegisterDSP
	 */
	public static int registerDSP(long arg0, org.lwjgl.fmod.FMOD_DSP_DESCRIPTION arg1, java.nio.IntBuffer arg2)
	{
		return FMOD.FMOD_System_RegisterDSP(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_System_SetUserData
	 */
	public static int setUserData(long arg0, long arg1)
	{
		return FMOD.FMOD_System_SetUserData(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_System_GetStreamBufferSize
	 */
	public static int getStreamBufferSize(long arg0, java.nio.IntBuffer arg1, java.nio.IntBuffer arg2)
	{
		return FMOD.FMOD_System_GetStreamBufferSize(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_System_CreateSoundGroup
	 */
	public static int createSoundGroup(long arg0, java.nio.ByteBuffer arg1, org.lwjgl.PointerBuffer arg2)
	{
		return FMOD.FMOD_System_CreateSoundGroup(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_System_CreateSoundGroup
	 */
	public static int createSoundGroup(long arg0, java.lang.CharSequence arg1, org.lwjgl.PointerBuffer arg2)
	{
		return FMOD.FMOD_System_CreateSoundGroup(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_System_GetAdvancedSettings
	 */
	public static int getAdvancedSettings(long arg0, org.lwjgl.fmod.FMOD_ADVANCEDSETTINGS arg1)
	{
		return FMOD.FMOD_System_GetAdvancedSettings(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_System_SetSoftwareFormat
	 */
	public static int setSoftwareFormat(long arg0, int arg1, int arg2, int arg3)
	{
		return FMOD.FMOD_System_SetSoftwareFormat(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_System_GetNumNestedPlugins
	 */
	public static int getNumNestedPlugins(long arg0, int arg1, java.nio.IntBuffer arg2)
	{
		return FMOD.FMOD_System_GetNumNestedPlugins(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_System_CreateDSPByPlugin
	 */
	public static int createDSPByPlugin(long arg0, int arg1, org.lwjgl.PointerBuffer arg2)
	{
		return FMOD.FMOD_System_CreateDSPByPlugin(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_System_GetOutputByPlugin
	 */
	public static int getOutputByPlugin(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_System_GetOutputByPlugin(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_System_GetDSPBufferSize
	 */
	public static int getDSPBufferSize(long arg0, java.nio.IntBuffer arg1, java.nio.IntBuffer arg2)
	{
		return FMOD.FMOD_System_GetDSPBufferSize(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_System_AttachFileSystem
	 */
	public static int attachFileSystem(long arg0, org.lwjgl.fmod.FMOD_FILE_OPEN_CALLBACKI arg1, org.lwjgl.fmod.FMOD_FILE_CLOSE_CALLBACKI arg2, org.lwjgl.fmod.FMOD_FILE_READ_CALLBACKI arg3, org.lwjgl.fmod.FMOD_FILE_SEEK_CALLBACKI arg4)
	{
		return FMOD.FMOD_System_AttachFileSystem(arg0, arg1, arg2, arg3, arg4);
	}

	/**
	 *@see FMOD#FMOD_System_Set3DNumListeners
	 */
	public static int set3DNumListeners(long arg0, int arg1)
	{
		return FMOD.FMOD_System_Set3DNumListeners(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_System_GetDSPInfoByPlugin
	 */
	public static int getDSPInfoByPlugin(long arg0, int arg1, org.lwjgl.PointerBuffer arg2)
	{
		return FMOD.FMOD_System_GetDSPInfoByPlugin(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_System_Get3DNumListeners
	 */
	public static int get3DNumListeners(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_System_Get3DNumListeners(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_System_GetSpeakerModeChannels
	 */
	public static int getSpeakerModeChannels(long arg0, int arg1, java.nio.IntBuffer arg2)
	{
		return FMOD.FMOD_System_GetSpeakerModeChannels(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_System_GetNestedPlugin
	 */
	public static int getNestedPlugin(long arg0, int arg1, int arg2, java.nio.IntBuffer arg3)
	{
		return FMOD.FMOD_System_GetNestedPlugin(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_System_GetOutputHandle
	 */
	public static int getOutputHandle(long arg0, org.lwjgl.PointerBuffer arg1)
	{
		return FMOD.FMOD_System_GetOutputHandle(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_System_GetChannelsPlaying
	 */
	public static int getChannelsPlaying(long arg0, java.nio.IntBuffer arg1, java.nio.IntBuffer arg2)
	{
		return FMOD.FMOD_System_GetChannelsPlaying(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_System_CreateDSPByType
	 */
	public static int createDSPByType(long arg0, int arg1, org.lwjgl.PointerBuffer arg2)
	{
		return FMOD.FMOD_System_CreateDSPByType(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_System_SetAdvancedSettings
	 */
	public static int setAdvancedSettings(long arg0, org.lwjgl.fmod.FMOD_ADVANCEDSETTINGS arg1)
	{
		return FMOD.FMOD_System_SetAdvancedSettings(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_System_GetSoftwareFormat
	 */
	public static int getSoftwareFormat(long arg0, java.nio.IntBuffer arg1, java.nio.IntBuffer arg2, java.nio.IntBuffer arg3)
	{
		return FMOD.FMOD_System_GetSoftwareFormat(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_System_SetStreamBufferSize
	 */
	public static int setStreamBufferSize(long arg0, int arg1, int arg2)
	{
		return FMOD.FMOD_System_SetStreamBufferSize(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_System_Set3DRolloffCallback
	 */
	public static int set3DRolloffCallback(long arg0, org.lwjgl.fmod.FMOD_3D_ROLLOFF_CALLBACKI arg1)
	{
		return FMOD.FMOD_System_Set3DRolloffCallback(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_System_GetPluginHandle
	 */
	public static int getPluginHandle(long arg0, int arg1, int arg2, java.nio.IntBuffer arg3)
	{
		return FMOD.FMOD_System_GetPluginHandle(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_System_SetOutputByPlugin
	 */
	public static int setOutputByPlugin(long arg0, int arg1)
	{
		return FMOD.FMOD_System_SetOutputByPlugin(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_System_GetSpeakerPosition
	 */
	public static int getSpeakerPosition(long arg0, int arg1, java.nio.FloatBuffer arg2, java.nio.FloatBuffer arg3, java.nio.IntBuffer arg4)
	{
		return FMOD.FMOD_System_GetSpeakerPosition(arg0, arg1, arg2, arg3, arg4);
	}

	/**
	 *@see FMOD#FMOD_System_SetSoftwareChannels
	 */
	public static int setSoftwareChannels(long arg0, int arg1)
	{
		return FMOD.FMOD_System_SetSoftwareChannels(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_System_GetSoftwareChannels
	 */
	public static int getSoftwareChannels(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_System_GetSoftwareChannels(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_System_SetSpeakerPosition
	 */
	public static int setSpeakerPosition(long arg0, int arg1, float arg2, float arg3, int arg4)
	{
		return FMOD.FMOD_System_SetSpeakerPosition(arg0, arg1, arg2, arg3, arg4);
	}

	/**
	 *@see FMOD#FMOD_System_GetDefaultMixMatrix
	 */
	public static int getDefaultMixMatrix(long arg0, int arg1, int arg2, java.nio.FloatBuffer arg3, int arg4)
	{
		return FMOD.FMOD_System_GetDefaultMixMatrix(arg0, arg1, arg2, arg3, arg4);
	}

	/**
	 *@see FMOD#FMOD_System_CreateChannelGroup
	 */
	public static int createChannelGroup(long arg0, java.lang.CharSequence arg1, org.lwjgl.PointerBuffer arg2)
	{
		return FMOD.FMOD_System_CreateChannelGroup(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_System_CreateChannelGroup
	 */
	public static int createChannelGroup(long arg0, java.nio.ByteBuffer arg1, org.lwjgl.PointerBuffer arg2)
	{
		return FMOD.FMOD_System_CreateChannelGroup(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_System_SetDSPBufferSize
	 */
	public static int setDSPBufferSize(long arg0, int arg1, int arg2)
	{
		return FMOD.FMOD_System_SetDSPBufferSize(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_System_GetRecordPosition
	 */
	public static int getRecordPosition(long arg0, int arg1, java.nio.IntBuffer arg2)
	{
		return FMOD.FMOD_System_GetRecordPosition(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_System_SetNetworkTimeout
	 */
	public static int setNetworkTimeout(long arg0, int arg1)
	{
		return FMOD.FMOD_System_SetNetworkTimeout(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_System_GetMasterSoundGroup
	 */
	public static int getMasterSoundGroup(long arg0, org.lwjgl.PointerBuffer arg1)
	{
		return FMOD.FMOD_System_GetMasterSoundGroup(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_System_GetNetworkTimeout
	 */
	public static int getNetworkTimeout(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_System_GetNetworkTimeout(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_System_GetRecordNumDrivers
	 */
	public static int getRecordNumDrivers(long arg0, java.nio.IntBuffer arg1, java.nio.IntBuffer arg2)
	{
		return FMOD.FMOD_System_GetRecordNumDrivers(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_System_GetGeometrySettings
	 */
	public static int getGeometrySettings(long arg0, java.nio.FloatBuffer arg1)
	{
		return FMOD.FMOD_System_GetGeometrySettings(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_System_GetReverbProperties
	 */
	public static int getReverbProperties(long arg0, int arg1, org.lwjgl.fmod.FMOD_REVERB_PROPERTIES arg2)
	{
		return FMOD.FMOD_System_GetReverbProperties(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_System_GetRecordDriverInfo
	 */
	public static int getRecordDriverInfo(long arg0, int arg1, java.nio.ByteBuffer arg2, org.lwjgl.fmod.FMOD_GUID arg3, java.nio.IntBuffer arg4, java.nio.IntBuffer arg5, java.nio.IntBuffer arg6, java.nio.IntBuffer arg7)
	{
		return FMOD.FMOD_System_GetRecordDriverInfo(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
	}

	/**
	 *@see FMOD#FMOD_System_GetNetworkProxy
	 */
	public static int getNetworkProxy(long arg0, java.nio.ByteBuffer arg1)
	{
		return FMOD.FMOD_System_GetNetworkProxy(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_System_SetReverbProperties
	 */
	public static int setReverbProperties(long arg0, int arg1, org.lwjgl.fmod.FMOD_REVERB_PROPERTIES arg2)
	{
		return FMOD.FMOD_System_SetReverbProperties(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_System_GetDSPInfoByType
	 */
	public static int getDSPInfoByType(long arg0, int arg1, org.lwjgl.PointerBuffer arg2)
	{
		return FMOD.FMOD_System_GetDSPInfoByType(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_System_GetMasterChannelGroup
	 */
	public static int getMasterChannelGroup(long arg0, org.lwjgl.PointerBuffer arg1)
	{
		return FMOD.FMOD_System_GetMasterChannelGroup(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_System_SetGeometrySettings
	 */
	public static int setGeometrySettings(long arg0, float arg1)
	{
		return FMOD.FMOD_System_SetGeometrySettings(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_System_GetGeometryOcclusion
	 */
	public static int getGeometryOcclusion(long arg0, org.lwjgl.fmod.FMOD_VECTOR arg1, org.lwjgl.fmod.FMOD_VECTOR arg2, java.nio.FloatBuffer arg3, java.nio.FloatBuffer arg4)
	{
		return FMOD.FMOD_System_GetGeometryOcclusion(arg0, arg1, arg2, arg3, arg4);
	}

	/**
	 *@see FMOD#FMOD_System_SetNetworkProxy
	 */
	public static int setNetworkProxy(long arg0, java.nio.ByteBuffer arg1)
	{
		return FMOD.FMOD_System_SetNetworkProxy(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_System_SetNetworkProxy
	 */
	public static int setNetworkProxy(long arg0, java.lang.CharSequence arg1)
	{
		return FMOD.FMOD_System_SetNetworkProxy(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_System_Get3DListenerAttributes
	 */
	public static int get3DListenerAttributes(long arg0, int arg1, org.lwjgl.fmod.FMOD_VECTOR arg2, org.lwjgl.fmod.FMOD_VECTOR arg3, org.lwjgl.fmod.FMOD_VECTOR arg4, org.lwjgl.fmod.FMOD_VECTOR arg5)
	{
		return FMOD.FMOD_System_Get3DListenerAttributes(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	/**
	 *@see FMOD#FMOD_System_DetachChannelGroupFromPort
	 */
	public static int detachChannelGroupFromPort(long arg0, long arg1)
	{
		return FMOD.FMOD_System_DetachChannelGroupFromPort(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_System_Set3DListenerAttributes
	 */
	public static int set3DListenerAttributes(long arg0, int arg1, org.lwjgl.fmod.FMOD_VECTOR arg2, org.lwjgl.fmod.FMOD_VECTOR arg3, org.lwjgl.fmod.FMOD_VECTOR arg4, org.lwjgl.fmod.FMOD_VECTOR arg5)
	{
		return FMOD.FMOD_System_Set3DListenerAttributes(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	/**
	 *@see FMOD#FMOD_System_AttachChannelGroupToPort
	 */
	public static int attachChannelGroupToPort(long arg0, int arg1, long arg2, long arg3, int arg4)
	{
		return FMOD.FMOD_System_AttachChannelGroupToPort(arg0, arg1, arg2, arg3, arg4);
	}

}
