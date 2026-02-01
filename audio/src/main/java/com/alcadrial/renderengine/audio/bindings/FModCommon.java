package com.alcadrial.renderengine.audio.bindings;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.jspecify.annotations.Nullable;
import org.lwjgl.fmod.FMOD;
import org.lwjgl.fmod.FMOD_DEBUG_CALLBACKI;
import org.lwjgl.fmod.FMOD_MEMORY_ALLOC_CALLBACKI;
import org.lwjgl.fmod.FMOD_MEMORY_FREE_CALLBACKI;
import org.lwjgl.fmod.FMOD_MEMORY_REALLOC_CALLBACKI;

/**
 * <div class="manual-content api"><h1>11. Core API Reference | Common</h1><strong>File:</strong>
 * <p>
 * <ul>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#file_getdiskbusy">File_GetDiskBusy</a> Information function to retrieve the state of FMOD disk access.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#file_setdiskbusy">File_SetDiskBusy</a> Sets the busy state for disk access ensuring mutual exclusion of file operations.</span></li>
 * </ul>
 * <strong>Memory:</strong>
 * <p>
 * <ul>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#memory_getstats">Memory_GetStats</a> Returns information on the memory usage of FMOD.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#memory_initialize">Memory_Initialize</a> Specifies a method for FMOD to allocate and free memory, either through user supplied callbacks or through a user supplied memory buffer with a fixed size.</span></li>
 * </ul>
 * <hr><ul>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_memory_alloc_callback">FMOD_MEMORY_ALLOC_CALLBACK</a> Callback to allocate a block of memory.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_memory_free_callback">FMOD_MEMORY_FREE_CALLBACK</a> Callback to free a block of memory.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_memory_realloc_callback">FMOD_MEMORY_REALLOC_CALLBACK</a> Callback to re-allocate a block of memory to a different size.</span></li>
 * </ul>
 * <hr><ul>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_memory_type">FMOD_MEMORY_TYPE</a> Bitfields for memory allocation type being passed into FMOD memory callbacks.</span></li>
 * </ul>
 * <strong>Debugging:</strong>
 * <p>
 * <ul>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#debug_initialize">Debug_Initialize</a> Specify the level and delivery method of log messages when using the logging version of FMOD.</span></li>
 * </ul>
 * <hr><ul>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_debug_callback">FMOD_DEBUG_CALLBACK</a> Callback for debug messages when using the logging version of FMOD.</span></li>
 * </ul>
 * <hr><ul>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_debug_mode">FMOD_DEBUG_MODE</a> Specify the destination of log output when using the logging version of FMOD.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_debug_flags">FMOD_DEBUG_FLAGS</a> Specify the requested information to be output when using the logging version of FMOD.</span></li>
 * </ul>
 * <strong>Threading:</strong>
 * <p>
 * <ul>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#thread_setattributes">Thread_SetAttributes</a> Specify the affinity, priority and stack size for all FMOD created threads.</span></li>
 * </ul>
 * <hr><ul>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_thread_type">FMOD_THREAD_TYPE</a> Named constants for threads created at runtime.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_thread_affinity">FMOD_THREAD_AFFINITY</a> Bitfield for specifying the CPU core a given thread runs on.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_thread_priority">FMOD_THREAD_PRIORITY</a> Scheduling priority to assign a given thread to.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_thread_stack_size">FMOD_THREAD_STACK_SIZE</a> Stack space available to the given thread.</span></li>
 * </ul>
 * <strong>General:</strong>
 * <p>
 * <ul>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d_attributes">FMOD_3D_ATTRIBUTES</a> Structure describing a position, velocity and orientation.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_guid">FMOD_GUID</a> Structure describing a globally unique identifier.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_syncpoint">FMOD_SYNCPOINT</a> Named marker for a given point in time.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_vector">FMOD_VECTOR</a> Structure describing a point in 3D space.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_cpu_usage">FMOD_CPU_USAGE</a> Performance information for Core API functionality.</span></li>
 * </ul>
 * <hr><ul>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_channelmask">FMOD_CHANNELMASK</a> Flags that describe the speakers present in a given <a href="https://www.fmod.com/docs/2.03/api/glossary.html#signal">signal</a>.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_channelorder">FMOD_CHANNELORDER</a> Speaker ordering for multi-channel <a href="https://www.fmod.com/docs/2.03/api/glossary.html#signal">signals</a>.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_max_channel_width">FMOD_MAX_CHANNEL_WIDTH</a> Maximum number of channels per sample of audio supported by audio files, buffers, connections and <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSPs</a>.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_max_listeners">FMOD_MAX_LISTENERS</a> Maximum number of listeners supported.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_max_systems">FMOD_MAX_SYSTEMS</a> Maximum number of System objects allowed.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_version">FMOD_VERSION</a> Current FMOD version number.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_mode">FMOD_MODE</a> Sound description bitfields, bitwise OR them together for loading and describing Sounds.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_result">FMOD_RESULT</a> Error codes returned from every function.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_speaker">FMOD_SPEAKER</a> Assigns an enumeration for a speaker index.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_speakermode">FMOD_SPEAKERMODE</a> Speaker mode types.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit">FMOD_TIMEUNIT</a> Time types used for position or length.</span></li>
 * </ul>
 * 
 * @author FMOD team, autogenerated by Alcadrial
 */
public class FModCommon {

	private FModCommon()
	{

	}

	/**
	 */
	public static int initialize(int flags, int mode, @Nullable FMOD_DEBUG_CALLBACKI callback, @Nullable ByteBuffer filename)
	{
		return FMOD.FMOD_Debug_Initialize(flags, mode, callback, filename);
	}

	/**
	 */
	public static int initialize(int flags, int mode, @Nullable FMOD_DEBUG_CALLBACKI callback, @Nullable CharSequence filename)
	{
		return FMOD.FMOD_Debug_Initialize(flags, mode, callback, filename);
	}

	/**
	 */
	public static java.lang.String ErrorString(int errcode)
	{
		return FMOD.FMOD_ErrorString(errcode);
	}

	/**
	 */
	public static int getDiskBusy(java.nio.IntBuffer file)
	{
		return FMOD.FMOD_File_GetDiskBusy(file);
	}

	/**
	 */
	public static int setDiskBusy(int file)
	{
		return FMOD.FMOD_File_SetDiskBusy(file);
	}

	/**
	 */
	public static int getStats(@Nullable IntBuffer currentalloced, @Nullable IntBuffer maxalloced, int blocking)
	{
		return FMOD.FMOD_Memory_GetStats(currentalloced, maxalloced, blocking);
	}

	/**
	 */
	public static int initialize(@Nullable ByteBuffer poolmem, @Nullable FMOD_MEMORY_ALLOC_CALLBACKI useralloc, @Nullable FMOD_MEMORY_REALLOC_CALLBACKI userrealloc, @Nullable FMOD_MEMORY_FREE_CALLBACKI userfree, int memtypeflags)
	{
		return FMOD.FMOD_Memory_Initialize(poolmem, useralloc, userrealloc, userfree, memtypeflags);
	}

	/**
	 */
	public static int setAttributes(int type, long affinity, int priority, int stacksize)
	{
		return FMOD.FMOD_Thread_SetAttributes(type, affinity, priority, stacksize);
	}

}
