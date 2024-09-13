package com.alcadrial.renderengine.audio;

import java.util.function.Supplier;

import org.lwjgl.fmod.FMOD;

import com.alcadrial.renderengine.core.util.DataIncosistenceException;

public class FModErrorUtils {
	
	public static <O> void match(O obj, O other)
	{
		if (!obj.equals(other)) throw new DataIncosistenceException();
	}
	
	public static <O> void match(O obj, O other, String error)
	{
		if (!obj.equals(other)) throw new DataIncosistenceException(error);
	}
	
	public static <O> O nonNull(O obj)
	{
		if (obj == null) throw new NullPointerException();
		return obj;
	}
	
	public static <O> O nonNull(O obj, String error)
	{
		if (obj == null) throw new NullPointerException(error);
		return obj;
	}
	
	public static int nonNull(int id)
	{
		if (id == 0) throw new NullPointerException();
		return id;
	}
	
	public static int nonNull(int id, String error)
	{
		if (id == 0) throw new NullPointerException(error);
		return id;
	}
	
	public static long nonNull(long pointer)
	{
		if (pointer == 0) throw new NullPointerException();
		return pointer;
	}
	
	public static long nonNull(long pointer, String error)
	{
		if (pointer == 0) throw new NullPointerException(error);
		return pointer;
	}
	
	public static void assertStatement(boolean flag, String details)
	{
		if (!flag) throw new AssertionError(details);
	}
	
	public static void assertNoError(int error, Supplier<String> details)
	{
		if (error != 0) throw new AssertionError(details.get() + ": " + getErrorString(error));
	}
	
	public static void assertNoError(int error, String details)
	{
		if (error != 0) throw new AssertionError(details + ": " + getErrorString(error));
	}
	
	public static void assertNoError(int error)
	{
		if (error != 0) throw new AssertionError(getErrorString(error));
	}
	
	public static String getErrorString(int error)
	{
		return switch (error)
		{
			case FMOD.FMOD_OK -> "OK";
			case FMOD.FMOD_ERR_BADCOMMAND -> "ERR BADCOMMAND";
			case FMOD.FMOD_ERR_CHANNEL_ALLOC -> "ERR CHANNEL ALLOC";
			case FMOD.FMOD_ERR_CHANNEL_STOLEN -> "ERR CHANNEL STOLEN";
			case FMOD.FMOD_ERR_DMA -> "ERR DMA";
			case FMOD.FMOD_ERR_DSP_CONNECTION -> "ERR DSP CONNECTION";
			case FMOD.FMOD_ERR_DSP_DONTPROCESS -> "ERR DSP DONTPROCESS";
			case FMOD.FMOD_ERR_DSP_FORMAT -> "ERR DSP FORMAT";
			case FMOD.FMOD_ERR_DSP_INUSE -> "ERR DSP INUSE";
			case FMOD.FMOD_ERR_DSP_NOTFOUND -> "ERR DSP NOTFOUND";
			case FMOD.FMOD_ERR_DSP_RESERVED -> "ERR DSP RESERVED";
			case FMOD.FMOD_ERR_DSP_SILENCE -> "ERR DSP SILENCE";
			case FMOD.FMOD_ERR_DSP_TYPE -> "ERR DSP TYPE";
			case FMOD.FMOD_ERR_FILE_BAD -> "ERR FILE_BAD";
			case FMOD.FMOD_ERR_FILE_COULDNOTSEEK -> "ERR FILE COULDNOTSEEK";
			case FMOD.FMOD_ERR_FILE_DISKEJECTED -> "ERR FILE DISKEJECTED";
			case FMOD.FMOD_ERR_FILE_EOF -> "ERR FILE EOF";
			case FMOD.FMOD_ERR_FILE_ENDOFDATA -> "ERR FILE ENDOFDATA";
			case FMOD.FMOD_ERR_FILE_NOTFOUND -> "ERR FILE NOTFOUND";
			case FMOD.FMOD_ERR_FORMAT -> "ERR FORMAT";
			case FMOD.FMOD_ERR_HEADER_MISMATCH -> "ERR HEADER MISMATCH";
			case FMOD.FMOD_ERR_HTTP -> "ERR HTTP";
			case FMOD.FMOD_ERR_HTTP_ACCESS -> "ERR HTTP ACCESS";
			case FMOD.FMOD_ERR_HTTP_PROXY_AUTH -> "ERR HTTP PROXY AUTH";
			case FMOD.FMOD_ERR_HTTP_SERVER_ERROR -> "ERR HTTP SERVER ERROR";
			case FMOD.FMOD_ERR_HTTP_TIMEOUT -> "ERR HTTP TIMEOUT";
			case FMOD.FMOD_ERR_INITIALIZATION -> "ERR INITIALIZATION";
			case FMOD.FMOD_ERR_INITIALIZED -> "ERR INITIALIZED";
			case FMOD.FMOD_ERR_INTERNAL -> "ERR INTERNAL";
			case FMOD.FMOD_ERR_INVALID_FLOAT -> "ERR INVALID FLOAT";
			case FMOD.FMOD_ERR_INVALID_HANDLE -> "ERR INVALID HANDLE";
			case FMOD.FMOD_ERR_INVALID_PARAM -> "ERR INVALID PARAM";
			case FMOD.FMOD_ERR_INVALID_POSITION -> "ERR INVALID POSITION";
			case FMOD.FMOD_ERR_INVALID_SPEAKER -> "ERR INVALID SPEAKER";
			case FMOD.FMOD_ERR_INVALID_SYNCPOINT -> "ERR INVALID SYNCPOINT";
			case FMOD.FMOD_ERR_INVALID_THREAD -> "ERR INVALID THREAD";
			case FMOD.FMOD_ERR_INVALID_VECTOR -> "ERR INVALID VECTOR";
			case FMOD.FMOD_ERR_MAXAUDIBLE -> "ERR MAXAUDIBLE";
			case FMOD.FMOD_ERR_MEMORY -> "ERR MEMORY";
			case FMOD.FMOD_ERR_MEMORY_CANTPOINT -> "ERR MEMORY CANTPOINT";
			case FMOD.FMOD_ERR_NEEDS3D -> "ERR NEEDS3D";
			case FMOD.FMOD_ERR_NEEDSHARDWARE -> "ERR NEEDSHARDWARE";
			case FMOD.FMOD_ERR_NET_CONNECT -> "ERR NET CONNECT";
			case FMOD.FMOD_ERR_NET_SOCKET_ERROR -> "ERR NET SOCKET ERROR";
			case FMOD.FMOD_ERR_NET_URL -> "ERR NET URL";
			case FMOD.FMOD_ERR_NET_WOULD_BLOCK -> "ERR NET WOULD BLOCK";
			case FMOD.FMOD_ERR_NOTREADY -> "ERR NOTREADY";
			case FMOD.FMOD_ERR_OUTPUT_ALLOCATED -> "ERR OUTPUT ALLOCATED";
			case FMOD.FMOD_ERR_OUTPUT_CREATEBUFFER -> "ERR OUTPUT CREATEBUFFER";
			case FMOD.FMOD_ERR_OUTPUT_DRIVERCALL -> "ERR OUTPUT DRIVERCALL";
			case FMOD.FMOD_ERR_OUTPUT_FORMAT -> "ERR OUTPUT FORMAT";
			case FMOD.FMOD_ERR_OUTPUT_INIT -> "ERR OUTPUT INIT";
			case FMOD.FMOD_ERR_OUTPUT_NODRIVERS -> "ERR OUTPUT NODRIVERS";
			case FMOD.FMOD_ERR_PLUGIN -> "ERR PLUGIN";
			case FMOD.FMOD_ERR_PLUGIN_MISSING -> "ERR PLUGIN MISSING";
			case FMOD.FMOD_ERR_PLUGIN_RESOURCE -> "ERR PLUGIN RESOURCE";
			case FMOD.FMOD_ERR_PLUGIN_VERSION -> "ERR PLUGIN VERSION";
			case FMOD.FMOD_ERR_RECORD -> "ERR RECORD";
			case FMOD.FMOD_ERR_REVERB_CHANNELGROUP -> "ERR REVERB CHANNELGROUP";
			case FMOD.FMOD_ERR_REVERB_INSTANCE -> "ERR REVERB INSTANCE";
			case FMOD.FMOD_ERR_SUBSOUNDS -> "ERR SUBSOUNDS";
			case FMOD.FMOD_ERR_SUBSOUND_ALLOCATED -> "ERR SUBSOUND ALLOCATED";
			case FMOD.FMOD_ERR_SUBSOUND_CANTMOVE -> "ERR SUBSOUND CANTMOVE";
			case FMOD.FMOD_ERR_TAGNOTFOUND -> "ERR TAGNOTFOUND";
			case FMOD.FMOD_ERR_TOOMANYCHANNELS -> "ERR TOOMANYCHANNELS";
			case FMOD.FMOD_ERR_TRUNCATED -> "ERR TRUNCATED";
			case FMOD.FMOD_ERR_UNIMPLEMENTED -> "ERR UNIMPLEMENTED";
			case FMOD.FMOD_ERR_UNINITIALIZED -> "ERR UNINITIALIZED";
			case FMOD.FMOD_ERR_UNSUPPORTED -> "ERR UNSUPPORTED";
			case FMOD.FMOD_ERR_VERSION -> "ERR VERSION";
			case FMOD.FMOD_ERR_EVENT_ALREADY_LOADED -> "ERR EVENT ALREADY LOADED";
			case FMOD.FMOD_ERR_EVENT_LIVEUPDATE_BUSY -> "ERR EVENT LIVEUPDATE BUSY";
			case FMOD.FMOD_ERR_EVENT_LIVEUPDATE_MISMATCH -> "ERR EVENT LIVEUPDATE MISMATCH";
			case FMOD.FMOD_ERR_EVENT_LIVEUPDATE_TIMEOUT -> "ERR EVENT LIVEUPDATE TIMEOUT";
			case FMOD.FMOD_ERR_EVENT_NOTFOUND -> "ERR EVENT NOTFOUND";
			case FMOD.FMOD_ERR_STUDIO_UNINITIALIZED -> "ERR STUDIO UNINITIALIZED";
			case FMOD.FMOD_ERR_STUDIO_NOT_LOADED -> "ERR STUDIO NOT LOADED";
			case FMOD.FMOD_ERR_INVALID_STRING -> "ERR INVALID STRING";
			case FMOD.FMOD_ERR_ALREADY_LOCKED -> "ERR ALREADY LOCKED";
			case FMOD.FMOD_ERR_NOT_LOCKED -> "ERR NOT LOCKED";
			case FMOD.FMOD_ERR_RECORD_DISCONNECTED -> "ERR RECORD DISCONNECTED";
			case FMOD.FMOD_ERR_TOOMANYSAMPLES -> "ERR TOOMANYSAMPLES";
		
			default -> "UNKNOWN ERROR " + error;
		};
	}
	
	public static String getInstanceTypeString(int instanceType)
	{
		return switch (instanceType)
		{
			case FMOD.FMOD_ERRORCALLBACK_INSTANCETYPE_NONE -> "???";
			case FMOD.FMOD_ERRORCALLBACK_INSTANCETYPE_SYSTEM -> "System";
			case FMOD.FMOD_ERRORCALLBACK_INSTANCETYPE_CHANNEL -> "Channel";
			case FMOD.FMOD_ERRORCALLBACK_INSTANCETYPE_CHANNELGROUP -> "ChannelGroup";
			case FMOD.FMOD_ERRORCALLBACK_INSTANCETYPE_CHANNELCONTROL -> "ChannelControl";
			case FMOD.FMOD_ERRORCALLBACK_INSTANCETYPE_SOUND -> "Sound";
			case FMOD.FMOD_ERRORCALLBACK_INSTANCETYPE_SOUNDGROUP -> "SoundGroup";
			case FMOD.FMOD_ERRORCALLBACK_INSTANCETYPE_DSP -> "Dsp";
			case FMOD.FMOD_ERRORCALLBACK_INSTANCETYPE_DSPCONNECTION -> "DspConnection";
			case FMOD.FMOD_ERRORCALLBACK_INSTANCETYPE_GEOMETRY -> "Geometry";
			case FMOD.FMOD_ERRORCALLBACK_INSTANCETYPE_REVERB3D -> "Reverb3d";
			case FMOD.FMOD_ERRORCALLBACK_INSTANCETYPE_STUDIO_SYSTEM -> "StudioSystem";
			case FMOD.FMOD_ERRORCALLBACK_INSTANCETYPE_STUDIO_EVENTDESCRIPTION -> "StudioEventDescription ";
			case FMOD.FMOD_ERRORCALLBACK_INSTANCETYPE_STUDIO_EVENTINSTANCE -> "StudioeventInstance";
			case FMOD.FMOD_ERRORCALLBACK_INSTANCETYPE_STUDIO_PARAMETERINSTANCE -> "StudioParameterInstance";
			case FMOD.FMOD_ERRORCALLBACK_INSTANCETYPE_STUDIO_BUS -> "StudioBus";
			case FMOD.FMOD_ERRORCALLBACK_INSTANCETYPE_STUDIO_VCA -> "StudioVca";
			case FMOD.FMOD_ERRORCALLBACK_INSTANCETYPE_STUDIO_BANK -> "StudioBank";
			case FMOD.FMOD_ERRORCALLBACK_INSTANCETYPE_STUDIO_COMMANDREPLAY -> "StudioCommandReplay";
			default -> "???";
		};
	}
}
