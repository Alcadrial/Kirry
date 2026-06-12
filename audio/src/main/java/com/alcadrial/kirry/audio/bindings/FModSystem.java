package com.alcadrial.kirry.audio.bindings;

import org.lwjgl.fmod.FMOD;

/**
 * <div class="manual-content api"><strong>11. Core API Reference | System</strong>Management object from which all resources are created and played.
 * <p>
 * Create with {@link FModSystem#create}.
 * <p>
 * <strong>Lifetime management:</strong>
 * <p>
 * <ul>
 *     <li><span>{@link FModSystem#create} Creates an instance of the FMOD <a href="https://www.fmod.com/docs/2.03/api/glossary.html#system">system</a>.</span></li>
 *     <li><span>{@link FModSystem#init} Initialize the <a href="https://www.fmod.com/docs/2.03/api/glossary.html#system">system</a> object and prepare FMOD for playback.</span></li>
 *     <li><span>{@link FModSystem#close} Close the connection to the output and return to an uninitialized state without releasing the object.</span></li>
 *     <li><span>{@link FModSystem#release} Closes and frees this object and its resources.</span></li>
 *     <li><span>{@link FModSystem#update} Updates the FMOD <a href="https://www.fmod.com/docs/2.03/api/glossary.html#system">system</a>.</span></li>
 *     <li><span>{@link FModSystem#mixerSuspend} Suspend mixer thread and relinquish usage of audio hardware while maintaining internal state.</span></li>
 *     <li><span>{@link FModSystem#mixerResume} Resume mixer thread and reacquire access to audio hardware.</span></li>
 * </ul>
 * <hr><ul>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_initflags">FMOD_INITFLAGS</a> Configuration flags used when initializing the System object.</span></li>
 * </ul>
 * <strong>Device selection:</strong>
 * <p>
 * <ul>
 *     <li><span>{@link FModSystem#setOutput} Sets the type of output interface used to run the mixer.</span></li>
 *     <li><span>{@link FModSystem#getOutput} Retrieves the type of output interface used to run the mixer.</span></li>
 *     <li><span>{@link FModSystem#getNumDrivers} Retrieves the number of output drivers available for the selected output type.</span></li>
 *     <li><span>{@link FModSystem#getDriverInfo} Retrieves identification information about a sound device specified by its index, and specific to the selected output mode.</span></li>
 *     <li><span>{@link FModSystem#setDriver} Sets the output driver for the selected output type.</span></li>
 *     <li><span>{@link FModSystem#getDriver} Retrieves the output driver for the selected output type.</span></li>
 * </ul>
 * <hr><ul>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_outputtype">FMOD_OUTPUTTYPE</a> Built-in output types that can be used to run the mixer.</span></li>
 * </ul>
 * <strong>Setup:</strong>
 * <p>
 * <ul>
 *     <li><span>{@link FModSystem#setSoftwareChannels} Sets the maximum number of software mixed Channels possible.</span></li>
 *     <li><span>{@link FModSystem#getSoftwareChannels} Retrieves the maximum number of software mixed Channels possible.</span></li>
 *     <li><span>{@link FModSystem#setSoftwareFormat} Sets the output format for the software mixer.</span></li>
 *     <li><span>{@link FModSystem#getSoftwareFormat} Retrieves the output format for the software mixer.</span></li>
 *     <li><span>{@link FModSystem#setDSPBufferSize} Sets the buffer size for the FMOD software mixing engine.</span></li>
 *     <li><span>{@link FModSystem#getDSPBufferSize} Retrieves the buffer size settings for the FMOD software mixing engine.</span></li>
 *     <li><span>{@link FModSystem#setStreamBufferSize} Sets the default file buffer size for newly opened streams.</span></li>
 *     <li><span>{@link FModSystem#getStreamBufferSize} Retrieves the default file buffer size for newly opened streams.</span></li>
 *     <li><span>{@link FModSystem#setAdvancedSettings} Sets advanced settings for the <a href="https://www.fmod.com/docs/2.03/api/glossary.html#system">system</a> object, typically to allow adjusting of settings related to resource usage or audio quality.</span></li>
 *     <li><span>{@link FModSystem#getAdvancedSettings} Retrieves the advanced settings for the <a href="https://www.fmod.com/docs/2.03/api/glossary.html#system">system</a> object.</span></li>
 *     <li><span>{@link FModSystem#setSpeakerPosition} Sets the position of the specified speaker for the current speaker mode.</span></li>
 *     <li><span>{@link FModSystem#getSpeakerPosition} Retrieves the position of the specified speaker for the current speaker mode.</span></li>
 *     <li><span>{@link FModSystem#set3DSettings} Sets the global doppler scale, distance factor and log roll-off scale for all 3D sound in FMOD.</span></li>
 *     <li><span>{@link FModSystem#get3DSettings} Retrieves the global doppler scale, distance factor and roll-off scale for all 3D sounds.</span></li>
 *     <li><span>{@link FModSystem#set3DNumListeners} Sets the number of 3D 'listeners' in the 3D sound scene.</span></li>
 *     <li><span>{@link FModSystem#get3DNumListeners} Retrieves the number of 3D listeners.</span></li>
 *     <li><span>{@link FModSystem#set3DRolloffCallback} Sets a callback to allow custom calculation of distance attenuation.</span></li>
 * </ul>
 * <hr><ul>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_advancedsettings">FMOD_ADVANCEDSETTINGS</a> Advanced configuration settings.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_3d_rolloff_callback">FMOD_3D_ROLLOFF_CALLBACK</a> Callback to allow custom calculation of distance attenuation.</span></li>
 * </ul>
 * <hr><ul>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_dsp_resampler">FMOD_DSP_RESAMPLER</a> List of interpolation types used for resampling.</span></li>
 * </ul>
 * <strong>File system setup:</strong>
 * <p>
 * <ul>
 *     <li><span>{@link FModSystem#setFileSystem} Set callbacks to implement all file I/O instead of using the platform native method.</span></li>
 *     <li><span>{@link FModSystem#attachFileSystem} 'Piggyback' on FMOD file reading routines to capture data as it's read.</span></li>
 * </ul>
 * <hr><ul>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_asyncreadinfo">FMOD_ASYNCREADINFO</a> Information about a single asynchronous file operation.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_file_asyncdone_func">FMOD_FILE_ASYNCDONE_FUNC</a> Function to be called when asynchronous reading is finished.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_file_open_callback">FMOD_FILE_OPEN_CALLBACK</a> Callback for opening a file.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_file_close_callback">FMOD_FILE_CLOSE_CALLBACK</a> Calback for closing a file.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_file_read_callback">FMOD_FILE_READ_CALLBACK</a> Callback for reading from a file.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_file_seek_callback">FMOD_FILE_SEEK_CALLBACK</a> Callback for seeking within a file.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_file_asyncread_callback">FMOD_FILE_ASYNCREAD_CALLBACK</a> Callback for reading from a file asynchronously.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_file_asynccancel_callback">FMOD_FILE_ASYNCCANCEL_CALLBACK</a> Callback for cancelling a pending asynchronous read.</span></li>
 * </ul>
 * <strong>Plug-in support:</strong>
 * <p>
 * <ul>
 *     <li><span>{@link FModSystem#setPluginPath} Specify a base search path for plug-ins so they can be placed somewhere else than the directory of the main executable.</span></li>
 *     <li><span>{@link FModSystem#loadPlugin} Loads an FMOD plug-in (<a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP</a>, output or codec) from a file.</span></li>
 *     <li><span>{@link FModSystem#unloadPlugin} Unloads an FMOD (<a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP</a>, Output or Codec) plug-in.</span></li>
 *     <li><span>{@link FModSystem#getNumNestedPlugins} Retrieves the number of nested plug-ins from the selected plug-in.</span></li>
 *     <li><span>{@link FModSystem#getNestedPlugin} Retrieves the handle of a nested plug-in.</span></li>
 *     <li><span>{@link FModSystem#getNumPlugins} Retrieves the number of loaded plug-ins.</span></li>
 *     <li><span>{@link FModSystem#getPluginHandle} Retrieves the handle of a plug-in based on its type and relative index.</span></li>
 *     <li><span>{@link FModSystem#getPluginInfo} Retrieves information for the selected plug-in.</span></li>
 *     <li><span>{@link FModSystem#setOutputByPlugin} Selects an output type given a plug-in handle.</span></li>
 *     <li><span>{@link FModSystem#getOutputByPlugin} Retrieves the plug-in handle for the currently selected output type.</span></li>
 *     <li><span>{@link FModSystem#createDSPByPlugin} Create a <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP unit</a> with a specified plug-in handle.</span></li>
 *     <li><span>{@link FModSystem#getDSPInfoByPlugin} Retrieve the description structure for a pre-existing <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP</a> plug-in.</span></li>
 *     <li><span>{@link FModSystem#registerCodec} Register a Codec plug-in description structure for later use.</span></li>
 *     <li><span>{@link FModSystem#registerDSP} Register a <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP</a> plug-in description structure for later use.</span></li>
 *     <li><span>{@link FModSystem#registerOutput} Register an Output plug-in description structure for later use.</span></li>
 * </ul>
 * <hr><ul>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_pluginlist">FMOD_PLUGINLIST</a> Used to support lists of plug-ins within the one dynamic library.</span></li>
 * </ul>
 * <hr><ul>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_plugintype">FMOD_PLUGINTYPE</a> Types of plug-in used to extend functionality.</span></li>
 * </ul>
 * <strong>Network configuration:</strong>
 * <p>
 * <ul>
 *     <li><span>{@link FModSystem#setNetworkProxy} Set a proxy server to use for all subsequent internet connections.</span></li>
 *     <li><span>{@link FModSystem#getNetworkProxy} Retrieves the URL of the proxy server used in internet streaming.</span></li>
 *     <li><span>{@link FModSystem#setNetworkTimeout} Set the timeout for network streams.</span></li>
 *     <li><span>{@link FModSystem#getNetworkTimeout} Retrieve the timeout value for network streams.</span></li>
 * </ul>
 * <strong>Information:</strong>
 * <p>
 * <ul>
 *     <li><span>{@link FModSystem#getVersion} Retrieves the FMOD version and build number.</span></li>
 *     <li><span>{@link FModSystem#getOutputHandle} Retrieves an output type specific internal native interface.</span></li>
 *     <li><span>{@link FModSystem#getChannelsPlaying} Retrieves the number of currently playing Channels.</span></li>
 *     <li><span>{@link FModSystem#getCPUUsage} Retrieves the amount of CPU used for different parts of the Core API.</span></li>
 *     <li><span>{@link FModSystem#getFileUsage} Retrieves information about file reads.</span></li>
 *     <li><span>{@link FModSystem#getDefaultMixMatrix} Retrieves the default matrix used to convert from one speaker mode to another.</span></li>
 *     <li><span>{@link FModSystem#getSpeakerModeChannels} Retrieves the channel count for a given speaker mode.</span></li>
 * </ul>
 * <strong>Creation and retrieval:</strong>
 * <p>
 * <ul>
 *     <li><span>{@link FModSystem#createSound} Loads a sound into memory, opens it for streaming or sets it up for callback based sounds.</span></li>
 *     <li><span>{@link FModSystem#createStream} Opens a sound for streaming.</span></li>
 *     <li><span>{@link FModSystem#createDSP} Create a <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP unit</a> given a plug-in description structure.</span></li>
 *     <li><span>{@link FModSystem#createDSPByType} Create a <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP unit</a> with a specified built-in type index.</span></li>
 *     <li><span>{@link FModSystem#createDSPConnection} Create a DSPConnection object.</span></li>
 *     <li><span>{@link FModSystem#createChannelGroup} Create a ChannelGroup object.</span></li>
 *     <li><span>{@link FModSystem#createSoundGroup} Creates a SoundGroup object.</span></li>
 *     <li><span>{@link FModSystem#createReverb3D} Creates a 'virtual reverb' object. This object reacts to 3D location and morphs the reverb environment based on how close it is to the reverb object's center.</span></li>
 *     <li><span>{@link FModSystem#playSound} Plays a Sound on a Channel.</span></li>
 *     <li><span>{@link FModSystem#playDSP} Plays a <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP</a> along with any of its inputs on a <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>.</span></li>
 *     <li><span>{@link FModSystem#getChannel} Retrieves a handle to a Channel by ID.</span></li>
 *     <li><span>{@link FModSystem#getDSPInfoByType} Retrieve the description structure for a built-in <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP</a> plug-in.</span></li>
 *     <li><span>{@link FModSystem#getMasterChannelGroup} Retrieves the master ChannelGroup that all sounds ultimately route to.</span></li>
 *     <li><span>{@link FModSystem#getMasterSoundGroup} Retrieves the default SoundGroup, where all sounds are placed when they are created.</span></li>
 * </ul>
 * <hr><ul>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_createsoundexinfo">FMOD_CREATESOUNDEXINFO</a> Additional options for creating a Sound.</span></li>
 * </ul>
 * <strong>Runtime control:</strong>
 * <p>
 * <ul>
 *     <li><span>{@link FModSystem#set3DListenerAttributes} Sets the position, velocity and orientation of the specified 3D sound listener.</span></li>
 *     <li><span>{@link FModSystem#get3DListenerAttributes} Retrieves the position, velocity and orientation of the specified 3D sound listener.</span></li>
 *     <li><span>{@link FModSystem#setReverbProperties} Sets parameters for the global reverb environment.</span></li>
 *     <li><span>{@link FModSystem#getReverbProperties} Retrieves the current reverb environment for the specified reverb instance.</span></li>
 *     <li><span>{@link FModSystem#attachChannelGroupToPort} Connect the output of the specified ChannelGroup to an audio port on the output driver.</span></li>
 *     <li><span>{@link FModSystem#detachChannelGroupFromPort} Disconnect the output of the specified ChannelGroup from an audio port on the output driver.</span></li>
 * </ul>
 * <hr><ul>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_reverb_properties">FMOD_REVERB_PROPERTIES</a> Structure defining a reverb environment.</span></li>
 * </ul>
 * <hr><ul>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_port_index">FMOD_PORT_INDEX</a> Output type specific index for when there are multiple instances or destinations for a port type.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_port_type">FMOD_PORT_TYPE</a> Port types available for routing audio.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_reverb_maxinstances">FMOD_REVERB_MAXINSTANCES</a> The maximum number of global reverb instances.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_reverb_presets">FMOD_REVERB_PRESETS</a> Predefined reverb configurations.  To simplify usage, and avoid manually selecting reverb parameters, a table of common presets is supplied for ease of use.</span></li>
 * </ul>
 * <strong>Recording:</strong>
 * <p>
 * <ul>
 *     <li><span>{@link FModSystem#getRecordNumDrivers} Retrieves the number of recording devices available for this output mode. Use this to enumerate all recording devices possible so that the user can select one.</span></li>
 *     <li><span>{@link FModSystem#getRecordDriverInfo} Retrieves identification information about an audio device specified by its index, and specific to the output mode.</span></li>
 *     <li><span>{@link FModSystem#getRecordPosition} Retrieves the current recording position of the record buffer in PCM samples.</span></li>
 *     <li><span>{@link FModSystem#recordStart} Starts the recording engine recording to a pre-created Sound object.</span></li>
 *     <li><span>{@link FModSystem#recordStop} Stops the recording engine from recording to a pre-created Sound object.</span></li>
 *     <li><span>{@link FModSystem#isRecording} Retrieves the state of the FMOD recording API, ie if it is currently recording or not.</span></li>
 * </ul>
 * <hr><ul>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_driver_state">FMOD_DRIVER_STATE</a> Flags that provide additional information about a particular driver.</span></li>
 * </ul>
 * <strong>Geometry management:</strong>
 * <p>
 * <ul>
 *     <li><span>{@link FModSystem#createGeometry} Geometry creation function. This function will create a base geometry object which can then have polygons added to it.</span></li>
 *     <li><span>{@link FModSystem#setGeometrySettings} Sets the maximum world size for the geometry engine for performance / precision reasons.</span></li>
 *     <li><span>{@link FModSystem#getGeometrySettings} Retrieves the maximum world size for the geometry engine.</span></li>
 *     <li><span>{@link FModSystem#loadGeometry} Creates a geometry object from a block of memory which contains pre-saved geometry data.</span></li>
 *     <li><span>{@link FModSystem#getGeometryOcclusion} Calculates geometry occlusion between a listener and a sound source.</span></li>
 * </ul>
 * <strong>General:</strong>
 * <p>
 * <ul>
 *     <li><span>{@link FModSystem#lockDSP} Mutual exclusion function to lock the <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-engine">DSP engine</a> (which runs asynchronously in another thread), so that it will not execute.</span></li>
 *     <li><span>{@link FModSystem#unlockDSP} Mutual exclusion function to unlock the <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-engine">DSP engine</a> (which runs asynchronously in another thread) and let it continue executing.</span></li>
 *     <li><span>{@link FModSystem#setCallback} Sets the callback for System level notifications.</span></li>
 *     <li><span>{@link FModSystem#setUserData} Sets a user value associated with a System object.</span></li>
 *     <li><span>{@link FModSystem#getUserData} Retrieves a user value associated with a System object.</span></li>
 * </ul>
 * <hr><ul>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_errorcallback_info">FMOD_ERRORCALLBACK_INFO</a> Information describing an error that has occurred.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_system_callback">FMOD_SYSTEM_CALLBACK</a> Callback for System notifications.</span></li>
 * </ul>
 * <hr><ul>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_errorcallback_instancetype">FMOD_ERRORCALLBACK_INSTANCETYPE</a> Identifier used to represent the different types of instance in the error callback.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_system_callback_type">FMOD_SYSTEM_CALLBACK_TYPE</a> Types of callbacks called by the System.</span></li>
 * </ul>
 * 
 * @author FMOD team, autogenerated by Alcadrial
 */
public class FModSystem {

	private FModSystem()
	{

	}

	/**
	 * Connect the output of the specified ChannelGroup to an audio port on the output driver.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_AttachChannelGroupToPort(
	 *   FMOD_SYSTEM *system,
	 *   FMOD_PORT_TYPE portType,
	 *   FMOD_PORT_INDEX portIndex,
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   FMOD_BOOL passThru
	 * );
	 * </code></pre>
	 * <p>
	 * Ports are additional outputs supported by some
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_outputtype">FMOD_OUTPUTTYPE</a> plug-ins and can include things like controller headsets or dedicated background music streams. See the Port Support section (where applicable) of each platform's getting started guide found in the <a href="https://www.fmod.com/docs/2.03/api/platforms.html">platform details</a> chapter.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#detachChannelGroupFromPort}
	 * <p>
	 * 
	 * @param system
	 * @param portType <dd>Port type (output mode specific). (<a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_port_type">FMOD_PORT_TYPE</a>)
	 * @param portIndex <dd>Index to specify which instance of the specified <code>portType</code> to use (output mode specific) (<a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_port_index">FMOD_PORT_INDEX</a>)
	 * @param channelgroup <dd>Group to attach the port to. (<a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a>)
	 * @param passThru <dd><p>Whether the <a href="https://www.fmod.com/docs/2.03/api/glossary.html#signal">signal</a> should additionally route to the existing <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a> output.</p><ul><li><span class="label">Units:</span> Boolean</li></ul>
	 */
	public static int attachChannelGroupToPort(long system, int portType, long portIndex, long channelgroup, int passThru)
	{
		return FMOD.FMOD_System_AttachChannelGroupToPort(system, portType, portIndex, channelgroup, passThru);
	}

	/**
	 * 'Piggyback' on FMOD file reading routines to capture data as it's read.
	 * <p>
	 * This allows users to capture data as FMOD reads it, which may be useful for extracting the raw data that FMOD reads for hard to support sources (for example internet streams).
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_AttachFileSystem(
	 *   FMOD_SYSTEM *system,
	 *   FMOD_FILE_OPEN_CALLBACK useropen,
	 *   FMOD_FILE_CLOSE_CALLBACK userclose,
	 *   FMOD_FILE_READ_CALLBACK userread,
	 *   FMOD_FILE_SEEK_CALLBACK userseek
	 * );
	 * </code></pre>
	 * <p>
	 * To detach, pass null or equivalent as the callback parameters.
	 * <p>
	 * Note: This function is not to replace FMOD's file system. For this functionality, see
	 * {@link FModSystem#setFileSystem}.
	 * <p>
	 * <strong>See Also:</strong> <a href="https://www.fmod.com/docs/2.03/api/glossary.html#callback-behavior">Callback Behavior</a>
	 * <p>
	 * 
	 * @param system
	 * @param useropen <span>(<i>Opt</i>) <dd>Callback for after a file is opened. (<a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_file_open_callback">FMOD_FILE_OPEN_CALLBACK</a>)
	 * @param userclose <span>(<i>Opt</i>) <dd>Callback for after a file is closed. (<a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_file_close_callback">FMOD_FILE_CLOSE_CALLBACK</a>)
	 * @param userread <span>(<i>Opt</i>) <dd>Callback for after a read operation. (<a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_file_read_callback">FMOD_FILE_READ_CALLBACK</a>)
	 * @param userseek <span>(<i>Opt</i>) <dd>Callback for after a seek operation. (<a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_file_seek_callback">FMOD_FILE_SEEK_CALLBACK</a>)
	 */
	public static int attachFileSystem(long system, org.lwjgl.fmod.FMOD_FILE_OPEN_CALLBACKI useropen, org.lwjgl.fmod.FMOD_FILE_CLOSE_CALLBACKI userclose, org.lwjgl.fmod.FMOD_FILE_READ_CALLBACKI userread, org.lwjgl.fmod.FMOD_FILE_SEEK_CALLBACKI userseek)
	{
		return FMOD.FMOD_System_AttachFileSystem(system, useropen, userclose, userread, userseek);
	}

	/**
	 * Close the connection to the output and return to an uninitialized state without releasing the object.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_Close(
	 * FMOD_SYSTEM *system
	 * );
	 * </code></pre>
	 * <p>
	 * Closing renders objects created with this System invalid. Make sure any
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html">Sound</a>, <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a>, <a href="https://www.fmod.com/docs/2.03/api/core-api-geometry.html">Geometry</a> and <a href="https://www.fmod.com/docs/2.03/api/core-api-dsp.html">DSP</a> objects are released before calling this.
	 * <p>
	 * All pre-initialize configuration settings will remain and the System can be reinitialized as needed.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#init}, {@link FModSystem#release}
	 * <p>
	 * 
	 * @param system
	 */
	public static int close(long system)
	{
		return FMOD.FMOD_System_Close(system);
	}

	/**
	 * Creates an instance of the FMOD
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#system">system</a>.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_Create(
	 *   FMOD_SYSTEM **system,
	 *   unsigned int headerversion
	 * );
	 * </code></pre>
	 * <p>
	 * Pass FMOD_VERSION in
	 * <code>headerversion</code> to ensure the library and header versions being used match.
	 * <p>
	 * This must be called first to create an FMOD
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html">System</a> object before any other API calls (except for <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#memory_initialize">Memory_Initialize</a> and <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#debug_initialize">Debug_Initialize</a>). Use this function to create 1 or multiple instances of FMOD System objects.
	 * <p>
	 * Use
	 * {@link FModSystem#release} to free a system object.
	 * <p>
	 * <div class="admonition warning">
	 * <p>
	 * Calls to
	 * {@link FModSystem#create} and {@link FModSystem#release} are not thread-safe. Do not call these functions simultaneously from multiple threads at once.
	 * <p>
	 * </div>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#init}
	 * <p>
	 * 
	 * @param system <span>(<i>Out</i>) <dd>Newly created object. (<a href="https://www.fmod.com/docs/2.03/api/core-api-system.html">System</a>)
	 * @param headerversion <dd>Expected FMOD Engine version.
	 */
	public static int create(org.lwjgl.PointerBuffer system, int headerversion)
	{
		return FMOD.FMOD_System_Create(system, headerversion);
	}

	/**
	 * Create a ChannelGroup object.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_CreateChannelGroup(
	 *   FMOD_SYSTEM *system,
	 *   const char *name,
	 *   FMOD_CHANNELGROUP **channelgroup
	 * );
	 * </code></pre>
	 * <p>
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a>s can be used to assign / group <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s, for things such as volume scaling. <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a>s are also used for sub-mixing. Any <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s that are assigned to a <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a> get submixed into that <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a>'s 'tail' <a href="https://www.fmod.com/docs/2.03/api/core-api-dsp.html">DSP</a>.  See <a href="https://www.fmod.com/docs/2.03/api/core-api-channelcontrol.html#fmod_channelcontrol_dsp_tail">FMOD_CHANNELCONTROL_DSP_TAIL</a>.
	 * <p>
	 * If a
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a> has an <a href="https://www.fmod.com/docs/2.03/api/glossary.html#effect">effect</a> added to it, the effect is processed post-mix from the <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s and <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a>s below it in the mix hierarchy. For more information, see the <a href="https://www.fmod.com/docs/2.03/api/using-dsp-effects-in-the-core-api.html#dsp-architecture-and-usage">DSP Architecture and Usage</a> section of the <a href="https://www.fmod.com/docs/2.03/api/using-dsp-effects-in-the-core-api.html">Using DSP Effects in the Core API</a> chapter.
	 * <p>
	 * All
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a>s will initially output directly to the master <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a> (See {@link FModSystem#getMasterChannelGroup}). <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a>s can be re-parented this with {@link FModChannelGroup#addGroup}.
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannelGroup#release}
	 * <p>
	 * 
	 * @param system
	 * @param name <span>(<i>Opt</i>) <dd>Label for identification purposes. (<a href="https://www.fmod.com/docs/2.03/api/glossary.html#string-format">UTF-8 string</a>)
	 * @param channelgroup <span>(<i>Out</i>) <dd>Newly created group. (<a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a>)
	 */
	public static int createChannelGroup(long system, java.nio.ByteBuffer name, org.lwjgl.PointerBuffer channelgroup)
	{
		return FMOD.FMOD_System_CreateChannelGroup(system, name, channelgroup);
	}

	/**
	 * Create a ChannelGroup object.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_CreateChannelGroup(
	 *   FMOD_SYSTEM *system,
	 *   const char *name,
	 *   FMOD_CHANNELGROUP **channelgroup
	 * );
	 * </code></pre>
	 * <p>
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a>s can be used to assign / group <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s, for things such as volume scaling. <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a>s are also used for sub-mixing. Any <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s that are assigned to a <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a> get submixed into that <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a>'s 'tail' <a href="https://www.fmod.com/docs/2.03/api/core-api-dsp.html">DSP</a>.  See <a href="https://www.fmod.com/docs/2.03/api/core-api-channelcontrol.html#fmod_channelcontrol_dsp_tail">FMOD_CHANNELCONTROL_DSP_TAIL</a>.
	 * <p>
	 * If a
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a> has an <a href="https://www.fmod.com/docs/2.03/api/glossary.html#effect">effect</a> added to it, the effect is processed post-mix from the <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s and <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a>s below it in the mix hierarchy. For more information, see the <a href="https://www.fmod.com/docs/2.03/api/using-dsp-effects-in-the-core-api.html#dsp-architecture-and-usage">DSP Architecture and Usage</a> section of the <a href="https://www.fmod.com/docs/2.03/api/using-dsp-effects-in-the-core-api.html">Using DSP Effects in the Core API</a> chapter.
	 * <p>
	 * All
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a>s will initially output directly to the master <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a> (See {@link FModSystem#getMasterChannelGroup}). <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a>s can be re-parented this with {@link FModChannelGroup#addGroup}.
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannelGroup#release}
	 * <p>
	 * 
	 * @param system
	 * @param name <span>(<i>Opt</i>) <dd>Label for identification purposes. (<a href="https://www.fmod.com/docs/2.03/api/glossary.html#string-format">UTF-8 string</a>)
	 * @param channelgroup <span>(<i>Out</i>) <dd>Newly created group. (<a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a>)
	 */
	public static int createChannelGroup(long system, java.lang.CharSequence name, org.lwjgl.PointerBuffer channelgroup)
	{
		return FMOD.FMOD_System_CreateChannelGroup(system, name, channelgroup);
	}

	/**
	 * Create a
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP unit</a> given a plug-in description structure.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_CreateDSP(
	 *   FMOD_SYSTEM *system,
	 *   const FMOD_DSP_DESCRIPTION *description,
	 *   FMOD_DSP **dsp
	 * );
	 * </code></pre>
	 * <p>
	 * A DSP unit is a module that can be inserted into the
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-graph">DSP graph</a> to allow sound filtering or sound generation. For more information, see the <a href="https://www.fmod.com/docs/2.03/api/using-dsp-effects-in-the-core-api.html#dsp-architecture-and-usage">DSP Architecture and Usage</a> section of the <a href="https://www.fmod.com/docs/2.03/api/using-dsp-effects-in-the-core-api.html">Using DSP Effects in the Core API</a> chapter.
	 * <p>
	 * DSPs must be attached to the DSP graph before they become active, either via
	 * {@link FModChannel#addDSP} or {@link FModDSP#addInput}.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#createDSPByType}, {@link FModSystem#createDSPByPlugin}
	 * <p>
	 * 
	 * @param system
	 * @param description <dd>Structure describing the DSP to create. (<a href="https://www.fmod.com/docs/2.03/api/plugin-api-dsp.html#fmod_dsp_description">FMOD_DSP_DESCRIPTION</a>)
	 * @param dsp <span>(<i>Out</i>) <dd>Newly created unit. (<a href="https://www.fmod.com/docs/2.03/api/core-api-dsp.html">DSP</a>)
	 */
	public static int createDSP(long system, org.lwjgl.fmod.FMOD_DSP_DESCRIPTION description, org.lwjgl.PointerBuffer dsp)
	{
		return FMOD.FMOD_System_CreateDSP(system, description, dsp);
	}

	/**
	 * Create a
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP unit</a> with a specified plug-in handle.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_CreateDSPByPlugin(
	 *   FMOD_SYSTEM *system,
	 *   unsigned int handle,
	 *   FMOD_DSP **dsp
	 * );
	 * </code></pre>
	 * <p>
	 * A DSP object is a module that can be inserted into the
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-graph">DSP graph</a> to allow sound filtering or sound generation. For more information, see the <a href="https://www.fmod.com/docs/2.03/api/using-dsp-effects-in-the-core-api.html#dsp-architecture-and-usage">DSP Architecture and Usage</a> section of the <a href="https://www.fmod.com/docs/2.03/api/using-dsp-effects-in-the-core-api.html">Using DSP Effects in the Core API</a> chapter.
	 * <p>
	 * A handle can come from a newly loaded plug-in with
	 * {@link FModSystem#loadPlugin} or an existing plug-in with {@link FModSystem#getPluginHandle}.
	 * <p>
	 * DSPs must be attached to the DSP graph before they become active, either via
	 * {@link FModChannel#addDSP} or {@link FModDSP#addInput}.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#createDSP}, {@link FModSystem#createDSPByType}
	 * <p>
	 * 
	 * @param system
	 * @param handle <dd>Handle to an already loaded DSP plug-in.
	 * @param dsp <span>(<i>Out</i>) <dd>Newly created unit. (<a href="https://www.fmod.com/docs/2.03/api/core-api-dsp.html">DSP</a>)
	 */
	public static int createDSPByPlugin(long system, int handle, org.lwjgl.PointerBuffer dsp)
	{
		return FMOD.FMOD_System_CreateDSPByPlugin(system, handle, dsp);
	}

	/**
	 * Create a
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP unit</a> with a specified built-in type index.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_CreateDSPByType(
	 *   FMOD_SYSTEM *system,
	 *   FMOD_DSP_TYPE type,
	 *   FMOD_DSP **dsp
	 * );
	 * </code></pre>
	 * <p>
	 * A DSP unit (or "DSP") is a module that can be inserted into the
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-graph">DSP graph</a> to allow sound filtering or sound generation. For more information, see the <a href="https://www.fmod.com/docs/2.03/api/using-dsp-effects-in-the-core-api.html#dsp-architecture-and-usage">DSP Architecture and Usage</a> section of the <a href="https://www.fmod.com/docs/2.03/api/using-dsp-effects-in-the-core-api.html">Using DSP Effects in the Core API</a> chapter.
	 * <p>
	 * DSPs must be attached to the DSP graph before they become active, either via
	 * {@link FModChannel#addDSP} or {@link FModDSP#addInput}.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#createDSP}
	 * <p>
	 * 
	 * @param system
	 * @param type <dd>Type of built in unit. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common-dsp-effects.html#fmod_dsp_type">FMOD_DSP_TYPE</a>)
	 * @param dsp <span>(<i>Out</i>) <dd>Newly created unit. (<a href="https://www.fmod.com/docs/2.03/api/core-api-dsp.html">DSP</a>)
	 */
	public static int createDSPByType(long system, int type, org.lwjgl.PointerBuffer dsp)
	{
		return FMOD.FMOD_System_CreateDSPByType(system, type, dsp);
	}

	/**
	 * Create a DSPConnection object.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_CreateDSPConnection(
	 *   FMOD_SYSTEM *system,
	 *   FMOD_DSPCONNECTION_TYPE type,
	 *   FMOD_DSP **connection
	 * );
	 * </code></pre>
	 * <p>
	 * Creating a DSP connection with this function allows you to configure its initial state before using it to connect two DSPs via
	 * {@link FModDSP#addInput}.
	 * <p>
	 * 
	 * @param system
	 * @param type <dd>Type of connection. (<a href="https://www.fmod.com/docs/2.03/api/core-api-dspconnection.html#fmod_dspconnection_type">FMOD_DSPCONNECTION_TYPE</a>)
	 * @param connection <span>(<i>Out</i>) <dd>Newly created connection. (<a href="https://www.fmod.com/docs/2.03/api/core-api-dspconnection.html">DSPConnection</a>)
	 */
	public static int createDSPConnection(long system, int type, org.lwjgl.PointerBuffer connection)
	{
		return FMOD.FMOD_System_CreateDSPConnection(system, type, connection);
	}

	/**
	 * Geometry creation function. This function will create a base geometry object which can then have polygons added to it.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_CreateGeometry(
	 *   FMOD_SYSTEM *system,
	 *   int maxpolygons,
	 *   int maxvertices,
	 *   FMOD_GEOMETRY **geometry
	 * );
	 * </code></pre>
	 * <p>
	 * Polygons can be added to a geometry object using
	 * {@link FModGeometry#addPolygon}. For best efficiency, avoid overlapping of polygons and long thin polygons.
	 * <p>
	 * A geometry object stores its polygons in a group to allow optimization for line testing, insertion and updating of geometry in real-time.
	 * <br>Geometry objects also allow for efficient rotation, scaling and translation of groups of polygons.
	 * <p>
	 * It is important to set the value of maxworldsize to an appropriate value using
	 * {@link FModSystem#setGeometrySettings}.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#setGeometrySettings}, {@link FModSystem#loadGeometry}, {@link FModGeometry#addPolygon}, {@link FModGeometry#setPosition}, {@link FModGeometry#setRotation}, {@link FModGeometry#setScale}
	 * <p>
	 * 
	 * @param system
	 * @param maxpolygons <dd>Maximum number of polygons within this object.
	 * @param maxvertices <dd>Maximum number of vertices within this object.
	 * @param geometry <span>(<i>Out</i>) <dd>Newly created geometry object. (<a href="https://www.fmod.com/docs/2.03/api/core-api-geometry.html">Geometry</a>)
	 */
	public static int createGeometry(long system, int maxpolygons, int maxvertices, org.lwjgl.PointerBuffer geometry)
	{
		return FMOD.FMOD_System_CreateGeometry(system, maxpolygons, maxvertices, geometry);
	}

	/**
	 * Creates a 'virtual reverb' object. This object reacts to 3D location and morphs the reverb environment based on how close it is to the reverb object's center.
	 * <p>
	 * Multiple reverb objects can be created to achieve a multi-reverb environment. 1 reverb object is used for all 3D reverb objects (slot 0 by default).
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_CreateReverb3D(
	 *   FMOD_SYSTEM *system,
	 *   FMOD_REVERB3D **reverb
	 * );
	 * </code></pre>
	 * <p>
	 * The 3D reverb object is a sphere having 3D attributes (position, minimum distance, maximum distance) and reverb properties.
	 * <p>
	 * The properties and 3D attributes of all reverb objects collectively determine, along with the listener's position, the settings of and input gains into a single
	 * <a href="https://www.fmod.com/docs/2.03/api/effects-reference.html#sfx-reverb">3D reverb DSP</a>.
	 * <p>
	 * When the listener is within the sphere of effect of one or more 3D reverbs, the listener's 3D reverb properties are a weighted combination of such 3D reverbs.
	 * <p>
	 * When the listener is outside all of the reverbs, no reverb is applied.
	 * <p>
	 * {@link FModSystem#setReverbProperties} can be used to create an alternative reverb that can be used for 2D and background global reverb.
	 * <p>
	 * To avoid this reverb interfering with the reverb slot used by the 3D reverb, 2D reverb should use a different slot id with
	 * {@link FModSystem#setReverbProperties}, otherwise <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_advancedsettings_reverb3dinstance">FMOD_ADVANCEDSETTINGS::reverb3Dinstance</a> can also be used to place 3D reverb on a different reverb slot.
	 * <p>
	 * Use
	 * {@link FModChannel#setReverbProperties} to turn off reverb for 2D sounds (ie set wet = 0).
	 * <p>
	 * Creating multiple reverb objects does not impact performance. This is because these reverb objects are 'virtual reverbs': There is only one reverb
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP unit</a> running at a time, and its parameter values are morphed to those of each virtual reverb as needed.
	 * <p>
	 * To remove the reverb DSP unit and the associated CPU cost, first make sure all 3D reverb objects are released. Then, call
	 * {@link FModSystem#setReverbProperties} with the 3D reverb's slot ID (0 by default) with a property point of 0 or NULL, to signal that the reverb instance should be deleted.
	 * <p>
	 * If a 3D reverb is still present, and
	 * {@link FModSystem#setReverbProperties} function is called to free the reverb, the 3D reverb system recreates it upon the next {@link FModSystem#update} call.
	 * <p>
	 * The 3D reverb system does not affect Studio events unless it is explicitly enabled by calling
	 * <a href="https://www.fmod.com/docs/2.03/api/studio-api-eventinstance.html#studio_eventinstance_setreverblevel">Studio::EventInstance::setReverbLevel</a> on each event instance.
	 * <p>
	 * <strong>See Also:</strong> {@link FModReverb3D#release}
	 * <p>
	 * 
	 * @param system
	 * @param reverb <span>(<i>Out</i>) <dd>Newly created virtual reverb object. (<a href="https://www.fmod.com/docs/2.03/api/core-api-reverb3d.html">Reverb3D</a>)
	 */
	public static int createReverb3D(long system, org.lwjgl.PointerBuffer reverb)
	{
		return FMOD.FMOD_System_CreateReverb3D(system, reverb);
	}

	/**
	 * Loads a sound into memory, opens it for streaming or sets it up for callback based sounds.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_CreateSound(
	 *   FMOD_SYSTEM *system,
	 *   const char *name_or_data,
	 *   FMOD_MODE mode,
	 *   FMOD_CREATESOUNDEXINFO *exinfo,
	 *   FMOD_SOUND **sound
	 * );
	 * </code></pre>
	 * <p>
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_createsample">FMOD_CREATESAMPLE</a> will try to load and decompress the whole sound into memory, use <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_createstream">FMOD_CREATESTREAM</a> to open it as a stream and have it play back in realtime from disk or another medium. <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_createcompressedsample">FMOD_CREATECOMPRESSEDSAMPLE</a> can also be used for certain formats to play the sound directly in its compressed format from the mixer.
	 * <p>
	 * <ul><li>To open a file or URL as a stream, so that it decompresses / reads at runtime, instead of loading / decompressing into memory all at the time of this call, use the <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_createstream">FMOD_CREATESTREAM</a> flag.</li><li>To open a file or URL as a compressed sound effect that is not streamed and is not decompressed into memory at load time, use <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_createcompressedsample">FMOD_CREATECOMPRESSEDSAMPLE</a>. This is supported with MPEG (mp2/mp3), ADPCM/FADPCM, XMA, AT9 and FSB Vorbis files only. This is useful for those who want realtime compressed soundeffects, but not the overhead of disk access.</li><li>To open a sound as 2D, so that it is not affected by 3D processing, use the <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_2d">FMOD_2D</a> flag. 3D sound commands will be ignored on these types of sounds.</li><li>To open a sound as 3D, so that it is treated as a 3D sound, use the <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d">FMOD_3D</a> flag.</li></ul>
	 * <p>
	 * Note that
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_openraw">FMOD_OPENRAW</a>, <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_openmemory">FMOD_OPENMEMORY</a>, <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_openmemory_point">FMOD_OPENMEMORY_POINT</a> and <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_openuser">FMOD_OPENUSER</a> will not work here without the exinfo structure present, as more information is needed.
	 * <p>
	 * Use
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_nonblocking">FMOD_NONBLOCKING</a> to have the sound open or load in the background. You can use {@link FModSound#getOpenState} to determine if it has finished loading / opening or not. While it is loading (not ready), sound functions are not accessible for that sound.  Do not free memory provided with <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_openmemory">FMOD_OPENMEMORY</a> if the sound is not in a ready state, as it will most likely lead to a crash.
	 * <p>
	 * To account for slow media that might cause buffer underrun (skipping / stuttering / repeating blocks of audio) with sounds created with
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_createstream">FMOD_CREATESTREAM</a>, use {@link FModSystem#setStreamBufferSize} to increase read ahead.
	 * <p>
	 * As using
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_openuser">FMOD_OPENUSER</a> causes FMOD to ignore whatever is passed as the first argument <code>name_or_data</code>, recommended practice is to pass null or equivalent.
	 * <p>
	 * Specifying
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_openmemory_point">FMOD_OPENMEMORY_POINT</a> will POINT to your memory rather allocating its own sound buffers and duplicating it internally, this means you cannot free the memory while FMOD is using it, until after {@link FModSound#release} is called.
	 * <p>
	 * With
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_openmemory_point">FMOD_OPENMEMORY_POINT</a>, only PCM formats and compressed formats using <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_createcompressedsample">FMOD_CREATECOMPRESSEDSAMPLE</a> are supported.
	 * <p>
	 * <div class="admonition warning">
	 * <p>
	 * Use of FMOD.NON_BLOCKING is currently not supported for JavaScript.
	 * <p>
	 * @param system
	 * @param name_or_data <dd>Name of the file or URL to open (<a href="https://www.fmod.com/docs/2.03/api/glossary.html#string-format">UTF-8 string</a>) or a pointer to a preloaded sound memory block if <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_openmemory">FMOD_OPENMEMORY</a> / <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_openmemory_point">FMOD_OPENMEMORY_POINT</a> is used.
	 * @param mode <dd><p>Behavior modifier for opening the sound. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_mode">FMOD_MODE</a>)</p><ul><li><span class="label">Default:</span> <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_default">FMOD_DEFAULT</a></li></ul>
	 * @param exinfo <span>(<i>Opt</i>) <dd>Extended information for creating the sound. (<a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_createsoundexinfo">FMOD_CREATESOUNDEXINFO</a>)
	 * @param sound <span>(<i>Out</i>) <dd>Newly created Sound object. (<a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html">Sound</a>)
	 */
	public static int createSound(long system, java.nio.ByteBuffer name_or_data, int mode, org.lwjgl.fmod.FMOD_CREATESOUNDEXINFO exinfo, org.lwjgl.PointerBuffer sound)
	{
		return FMOD.FMOD_System_CreateSound(system, name_or_data, mode, exinfo, sound);
	}

	/**
	 * Creates a SoundGroup object.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_CreateSoundGroup(
	 *   FMOD_SYSTEM *system,
	 *   const char *name,
	 *   FMOD_SOUNDGROUP **soundgroup
	 * );
	 * </code></pre>
	 * <p>
	 * A
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-soundgroup.html">SoundGroup</a> is a way to address multiple <a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html">Sound</a>s at once with group level commands, such as:
	 * <p>
	 * <ul><li>Attributes of <a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html">Sound</a>s that are playing or about to be played, such as volume. See ({@link FModSoundGroup#setVolume}).</li><li>Control of playback, such as stopping <a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html">Sound</a>s. See ({@link FModSoundGroup#stop}).</li><li>Playback behavior such as 'max audible', to limit playback of certain types of <a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html">Sound</a>s. See ({@link FModSoundGroup#setMaxAudible}).</li></ul>
	 * <p>
	 * Once a
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-soundgroup.html">SoundGroup</a> is created, {@link FModSound#setSoundGroup} is used to put a <a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html">Sound</a> in a <a href="https://www.fmod.com/docs/2.03/api/core-api-soundgroup.html">SoundGroup</a>.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSoundGroup#release}, {@link FModSound#setSoundGroup}
	 * <p>
	 * 
	 * @param system
	 * @param name <dd>Name of <a href="https://www.fmod.com/docs/2.03/api/core-api-soundgroup.html">SoundGroup</a>. (<a href="https://www.fmod.com/docs/2.03/api/glossary.html#string-format">UTF-8 string</a>)
	 * @param soundGroup <span>(<i>Out</i>) <dd>Newly created group. (<a href="https://www.fmod.com/docs/2.03/api/core-api-soundgroup.html">SoundGroup</a>)
	 */
	public static int createSoundGroup(long system, java.lang.CharSequence name, org.lwjgl.PointerBuffer soundGroup)
	{
		return FMOD.FMOD_System_CreateSoundGroup(system, name, soundGroup);
	}

	/**
	 * Creates a SoundGroup object.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_CreateSoundGroup(
	 *   FMOD_SYSTEM *system,
	 *   const char *name,
	 *   FMOD_SOUNDGROUP **soundgroup
	 * );
	 * </code></pre>
	 * <p>
	 * A
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-soundgroup.html">SoundGroup</a> is a way to address multiple <a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html">Sound</a>s at once with group level commands, such as:
	 * <p>
	 * <ul><li>Attributes of <a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html">Sound</a>s that are playing or about to be played, such as volume. See ({@link FModSoundGroup#setVolume}).</li><li>Control of playback, such as stopping <a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html">Sound</a>s. See ({@link FModSoundGroup#stop}).</li><li>Playback behavior such as 'max audible', to limit playback of certain types of <a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html">Sound</a>s. See ({@link FModSoundGroup#setMaxAudible}).</li></ul>
	 * <p>
	 * Once a
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-soundgroup.html">SoundGroup</a> is created, {@link FModSound#setSoundGroup} is used to put a <a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html">Sound</a> in a <a href="https://www.fmod.com/docs/2.03/api/core-api-soundgroup.html">SoundGroup</a>.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSoundGroup#release}, {@link FModSound#setSoundGroup}
	 * <p>
	 * 
	 * @param system
	 * @param name <dd>Name of <a href="https://www.fmod.com/docs/2.03/api/core-api-soundgroup.html">SoundGroup</a>. (<a href="https://www.fmod.com/docs/2.03/api/glossary.html#string-format">UTF-8 string</a>)
	 * @param soundGroup <span>(<i>Out</i>) <dd>Newly created group. (<a href="https://www.fmod.com/docs/2.03/api/core-api-soundgroup.html">SoundGroup</a>)
	 */
	public static int createSoundGroup(long system, java.nio.ByteBuffer name, org.lwjgl.PointerBuffer soundGroup)
	{
		return FMOD.FMOD_System_CreateSoundGroup(system, name, soundGroup);
	}

	/**
	 * Opens a sound for streaming.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_CreateStream(
	 *   FMOD_SYSTEM *system,
	 *   const char *name_or_data,
	 *   FMOD_MODE mode,
	 *   FMOD_CREATESOUNDEXINFO *exinfo,
	 *   FMOD_SOUND **sound
	 * );
	 * </code></pre>
	 * <p>
	 * This is a convenience function for
	 * {@link FModSystem#createSound} with the <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_createstream">FMOD_CREATESTREAM</a> flag added.
	 * <p>
	 * A stream only has one decode buffer and file handle, and therefore can only be played once. It cannot play multiple times at once because it cannot share a stream buffer if the stream is playing at different positions. Open multiple streams to have them play concurrently.
	 * <p>
	 * 
	 * @param system
	 * @param name_or_data <dd>Name of the file or URL to open (<a href="https://www.fmod.com/docs/2.03/api/glossary.html#string-format">UTF-8 string</a>) or a pointer to a preloaded sound memory block if <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_openmemory">FMOD_OPENMEMORY</a> / <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_openmemory_point">FMOD_OPENMEMORY_POINT</a> is used.
	 * @param mode <dd><p>Behavior modifier for opening the sound. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_mode">FMOD_MODE</a>)</p><ul><li><span class="label">Default:</span> <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_default">FMOD_DEFAULT</a></li></ul>
	 * @param exinfo <span>(<i>Opt</i>) <dd>Extended information while playing the sound. (<a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_createsoundexinfo">FMOD_CREATESOUNDEXINFO</a>)
	 * @param sound <span>(<i>Out</i>) <dd>Newly created Sound object. (<a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html">Sound</a>)
	 */
	public static int createStream(long system, java.nio.ByteBuffer name_or_data, int mode, org.lwjgl.fmod.FMOD_CREATESOUNDEXINFO exinfo, org.lwjgl.PointerBuffer sound)
	{
		return FMOD.FMOD_System_CreateStream(system, name_or_data, mode, exinfo, sound);
	}

	/**
	 * Disconnect the output of the specified ChannelGroup from an audio port on the output driver.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_DetachChannelGroupFromPort(
	 *   FMOD_SYSTEM *system,
	 *   FMOD_CHANNELGROUP *channelgroup
	 * );
	 * </code></pre>
	 * <p>
	 * Removing a
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a> from a port will reroute the audio back to the main mix.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#attachChannelGroupToPort}
	 * <p>
	 * 
	 * @param system
	 * @param channelgroup <dd>Group to detach the port from. (<a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a>)
	 */
	public static int detachChannelGroupFromPort(long system, long channelgroup)
	{
		return FMOD.FMOD_System_DetachChannelGroupFromPort(system, channelgroup);
	}

	/**
	 * Retrieves the position, velocity and orientation of the specified 3D sound listener.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_Get3DListenerAttributes(
	 *   FMOD_SYSTEM *system,
	 *   int listener,
	 *   FMOD_VECTOR *pos,
	 *   FMOD_VECTOR *vel,
	 *   FMOD_VECTOR *forward,
	 *   FMOD_VECTOR *up
	 * );
	 * </code></pre>
	 * <p>
	 * Users of the Studio API should call
	 * <a href="https://www.fmod.com/docs/2.03/api/studio-api-system.html#studio_system_getlistenerattributes">Studio::System::getListenerAttributes</a> instead of this function.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#set3DListenerAttributes}
	 * <p>
	 * 
	 * @param system
	 * @param listener <dd><p>Index of listener to get 3D attributes for. Listeners are indexed from 0, to <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_max_listeners">FMOD_MAX_LISTENERS</a> - 1, in a multi-listener environment.</p><ul><li><span class="label">Range:</span> [0, <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_max_listeners">FMOD_MAX_LISTENERS</a>)</li></ul>
	 * @param pos <span>(<i>Out, Opt</i>) <dd><p>Position in 3D space used for panning and attenuation. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_vector">FMOD_VECTOR</a>)</p><ul><li><span class="label">Units:</span> <a href="https://www.fmod.com/docs/2.03/api/glossary.html#distance-units">Distance units</a></li></ul>
	 * @param vel <span>(<i>Out, Opt</i>) <dd><p>Velocity in 3D space used for doppler. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_vector">FMOD_VECTOR</a>)</p><ul><li><span class="label">Units:</span> <a href="https://www.fmod.com/docs/2.03/api/glossary.html#distance-units">Distance units</a></li></ul>
	 * @param forward <span>(<i>Out, Opt</i>) <dd>Forwards orientation. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_vector">FMOD_VECTOR</a>)
	 * @param up <span>(<i>Out, Opt</i>) <dd>Upwards orientation. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_vector">FMOD_VECTOR</a>)
	 */
	public static int get3DListenerAttributes(long system, int listener, org.lwjgl.fmod.FMOD_VECTOR pos, org.lwjgl.fmod.FMOD_VECTOR vel, org.lwjgl.fmod.FMOD_VECTOR forward, org.lwjgl.fmod.FMOD_VECTOR up)
	{
		return FMOD.FMOD_System_Get3DListenerAttributes(system, listener, pos, vel, forward, up);
	}

	/**
	 * Retrieves the number of 3D listeners.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_Get3DNumListeners(
	 *   FMOD_SYSTEM *system,
	 *   int *numlisteners
	 * );
	 * </code></pre>
	 * <p>
	 * Users of the Studio API should call
	 * <a href="https://www.fmod.com/docs/2.03/api/studio-api-system.html#studio_system_getnumlisteners">Studio::System::getNumListeners</a> instead of this function.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#set3DNumListeners}
	 * <p>
	 * 
	 * @param system
	 * @param numlisteners <span>(<i>Out</i>) <dd>Number of 3D listeners in the 3D scene.
	 */
	public static int get3DNumListeners(long system, java.nio.IntBuffer numlisteners)
	{
		return FMOD.FMOD_System_Get3DNumListeners(system, numlisteners);
	}

	/**
	 * Retrieves the global doppler scale, distance factor and roll-off scale for all 3D sounds.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_Get3DSettings(
	 *   FMOD_SYSTEM *system,
	 *   float *dopplerscale,
	 *   float *distancefactor,
	 *   float *rolloffscale
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#set3DSettings}
	 * <p>
	 * 
	 * @param system
	 * @param dopplerscale <span>(<i>Out, Opt</i>) <dd>A scaling factor for doppler shift.
	 * @param distancefactor <span>(<i>Out, Opt</i>) <dd>A factor for converting game distance units to FMOD distance units.
	 * @param rolloffscale <span>(<i>Out, Opt</i>) <dd>A scaling factor for distance attenuation. When a sound uses a roll-off mode other than FMOD_3D_CUSTOMROLLOFF and the distance is greater than the sound's minimum distance, the distance is scaled by the roll-off scale.
	 */
	public static int get3DSettings(long system, java.nio.FloatBuffer dopplerscale, java.nio.FloatBuffer distancefactor, java.nio.FloatBuffer rolloffscale)
	{
		return FMOD.FMOD_System_Get3DSettings(system, dopplerscale, distancefactor, rolloffscale);
	}

	/**
	 * Retrieves the advanced settings for the
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#system">system</a> object.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_GetAdvancedSettings(
	 *   FMOD_SYSTEM *system,
	 *   FMOD_ADVANCEDSETTINGS *settings
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#setAdvancedSettings}
	 * <p>
	 * 
	 * @param system
	 * @param settings <span>(<i>Out</i>) <dd>Advanced settings (<a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_advancedsettings">FMOD_ADVANCEDSETTINGS</a>)
	 */
	public static int getAdvancedSettings(long system, org.lwjgl.fmod.FMOD_ADVANCEDSETTINGS settings)
	{
		return FMOD.FMOD_System_GetAdvancedSettings(system, settings);
	}

	/**
	 * Retrieves the amount of CPU used for different parts of the Core API.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_GetCPUUsage(
	 *   FMOD_SYSTEM *system,
	 *   FMOD_CPU_USAGE *usage
	 * );
	 * </code></pre>
	 * <p>
	 * For readability, the percentage values are smoothed to provide a more stable output.
	 * <p>
	 * <strong>See Also:</strong> <a href="https://www.fmod.com/docs/2.03/api/studio-api-system.html#studio_system_getcpuusage">Studio::System::getCPUUsage</a>, <a href="https://www.fmod.com/docs/2.03/api/studio-api-system.html#fmod_studio_cpu_usage">FMOD_STUDIO_CPU_USAGE</a>
	 * <p>
	 * 
	 * @param system
	 * @param usage <span>(<i>Out</i>) <dd>CPU usage values. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_cpu_usage">FMOD_CPU_USAGE</a>)
	 */
	public static int getCPUUsage(long system, org.lwjgl.fmod.FMOD_CPU_USAGE usage)
	{
		return FMOD.FMOD_System_GetCPUUsage(system, usage);
	}

	/**
	 * Retrieves a handle to a Channel by ID.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_GetChannel(
	 *   FMOD_SYSTEM *system,
	 *   int channelid,
	 *   FMOD_CHANNEL **channel
	 * );
	 * </code></pre>
	 * <p>
	 * This function is mainly for getting handles to existing (playing)
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s and setting their attributes. The only way to 'create' an instance of a <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a> for playback is to use {@link FModSystem#playSound} or [System::playDSP].
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#init}
	 * <p>
	 * 
	 * @param system
	 * @param channelid <dd>Index in the FMOD <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a> pool. Specify a <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a> number from 0 to the 'maxchannels' value specified in {@link FModSystem#init} minus 1.
	 * @param channel <span>(<i>Out</i>) <dd>Requested <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>. (<a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>)
	 */
	public static int getChannel(long system, int channelid, org.lwjgl.PointerBuffer channel)
	{
		return FMOD.FMOD_System_GetChannel(system, channelid, channel);
	}

	/**
	 * Retrieves the number of currently playing Channels.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_GetChannelsPlaying(
	 *   FMOD_SYSTEM *system,
	 *   int *channels,
	 *   int *realchannels
	 * );
	 * </code></pre>
	 * <p>
	 * For differences between real and virtual voices see the
	 * <a href="https://www.fmod.com/docs/2.03/api/managing-resources-in-the-core-api.html#virtual-voice-system">Virtual Voice System</a> section of the <a href="https://www.fmod.com/docs/2.03/api/managing-resources-in-the-core-api.html">Managing Resources in the Core API</a> chapter for more information.
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#isPlaying}, {@link FModChannel#isVirtual}
	 * <p>
	 * 
	 * @param system
	 * @param channels <span>(<i>Opt, Out</i>) <dd>Number of playing <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s (both real and virtual).
	 * @param realchannels <span>(<i>Opt, Out</i>) <dd>Number of playing real (non-virtual) <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s.
	 */
	public static int getChannelsPlaying(long system, java.nio.IntBuffer channels, java.nio.IntBuffer realchannels)
	{
		return FMOD.FMOD_System_GetChannelsPlaying(system, channels, realchannels);
	}

	/**
	 * Retrieves the buffer size settings for the FMOD software mixing engine.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_GetDSPBufferSize(
	 *   FMOD_SYSTEM *system,
	 *   unsigned int *bufferlength,
	 *   int *numbuffers
	 * );
	 * </code></pre>
	 * <p>
	 * To get the
	 * <code>bufferlength</code> in milliseconds, divide it by the output rate and multiply the result by 1000. For a <code>bufferlength</code> of 1024 and an output rate of 48khz (see {@link FModSystem#setSoftwareFormat}), milliseconds = 1024 / 48000 * 1000 = 21.33ms. This means the mixer updates every 21.33ms.
	 * <p>
	 * To get the total buffer size multiply the
	 * <code>bufferlength</code> by the <code>numbuffers</code> value. By default this would be 4 * 1024 = 4096 samples, or 4 * 21.33ms = 85.33ms. This would generally be the total latency of the software mixer, but in reality due to one of the buffers being written to constantly, and the cursor position of the buffer that is audible, the latency is typically more like the (number of buffers - 1.5) multiplied by the buffer length.
	 * <p>
	 * To convert from milliseconds back to 'samples', simply multiply the value in milliseconds by the sample rate of the output (ie 48000 if that is what it is set to), then divide by 1000.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#setDSPBufferSize}
	 * <p>
	 * 
	 * @param system
	 * @param bufferlength <span>(<i>Opt, Out</i>) <dd><p>Mixer engine block size.</p><ul><li><span class="label">Units:</span> Samples</li><li><span class="label">Default:</span> 1024</li></ul>
	 * @param numbuffers <span>(<i>Opt, Out</i>) <dd><p>Mixer engine number of buffers used.</p><ul><li><span class="label">Default:</span> 4</li></ul>
	 */
	public static int getDSPBufferSize(long system, java.nio.IntBuffer bufferlength, java.nio.IntBuffer numbuffers)
	{
		return FMOD.FMOD_System_GetDSPBufferSize(system, bufferlength, numbuffers);
	}

	/**
	 * Retrieve the description structure for a pre-existing
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP</a> plug-in.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_GetDSPInfoByPlugin(
	 *   FMOD_SYSTEM *system,
	 *   unsigned int handle,
	 *   const FMOD_DSP_DESCRIPTION **description
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#loadPlugin}
	 * <p>
	 * 
	 * @param system
	 * @param handle <dd>Handle to a pre-existing DSP plug-in, loaded by {@link FModSystem#loadPlugin}.
	 * @param description <span>(<i>Out</i>) <dd>Description structure for the DSP. (<a href="https://www.fmod.com/docs/2.03/api/plugin-api-dsp.html#fmod_dsp_description">FMOD_DSP_DESCRIPTION</a>)
	 */
	public static int getDSPInfoByPlugin(long system, int handle, org.lwjgl.PointerBuffer description)
	{
		return FMOD.FMOD_System_GetDSPInfoByPlugin(system, handle, description);
	}

	/**
	 * Retrieve the description structure for a built-in
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP</a> plug-in.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_GetDSPInfoByType(
	 *   FMOD_SYSTEM *system,
	 *   FMOD_DSP_TYPE type,
	 *   const FMOD_DSP_DESCRIPTION **description
	 * );
	 * </code></pre>
	 * <p>
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common-dsp-effects.html#fmod_dsp_type_mixer">FMOD_DSP_TYPE_MIXER</a> not supported.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#getDSPInfoByPlugin}
	 * <p>
	 * 
	 * @param system
	 * @param type <dd>Type of built in unit. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common-dsp-effects.html#fmod_dsp_type">FMOD_DSP_TYPE</a>)
	 * @param description <span>(<i>Out</i>) <dd>Description structure for the DSP. (<a href="https://www.fmod.com/docs/2.03/api/plugin-api-dsp.html#fmod_dsp_description">FMOD_DSP_DESCRIPTION</a>)
	 */
	public static int getDSPInfoByType(long system, int type, org.lwjgl.PointerBuffer description)
	{
		return FMOD.FMOD_System_GetDSPInfoByType(system, type, description);
	}

	/**
	 * Retrieves the default matrix used to convert from one speaker mode to another.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_GetDefaultMixMatrix(
	 *   FMOD_SYSTEM *system,
	 *   FMOD_SPEAKERMODE sourcespeakermode,
	 *   FMOD_SPEAKERMODE targetspeakermode,
	 *   float *matrix,
	 *   int matrixhop
	 * );
	 * </code></pre>
	 * <p>
	 * The gain for source channel 's' to target channel 't' is matrix[t * matrixhop + s].
	 * <p>
	 * If 'sourcespeakermode' or 'targetspeakermode' is
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_speakermode_raw">FMOD_SPEAKERMODE_RAW</a>, this function will return <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_err_invalid_param">FMOD_ERR_INVALID_PARAM</a>.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#getSpeakerModeChannels}
	 * <p>
	 * 
	 * @param system
	 * @param sourcespeakermode <dd>The speaker mode being converted from. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_speakermode">FMOD_SPEAKERMODE</a>)
	 * @param targetspeakermode <dd>The speaker mode being converted to. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_speakermode">FMOD_SPEAKERMODE</a>)
	 * @param matrix <span>(<i>Out</i>) <dd>The output matrix. Its minimum size in number of floats must be the number of source channels multiplied by the number of target channels. Source and target channels cannot exceed <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_max_channel_width">FMOD_MAX_CHANNEL_WIDTH</a>.
	 * @param matrixhop <span>(<i>Opt</i>) <dd>The number of source channels in the matrix. If this is 0, the number of source channels will be derived from <code>sourcespeakermode</code>. Maximum of <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_max_channel_width">FMOD_MAX_CHANNEL_WIDTH</a>.
	 */
	public static int getDefaultMixMatrix(long system, int sourcespeakermode, int targetspeakermode, java.nio.FloatBuffer matrix, int matrixhop)
	{
		return FMOD.FMOD_System_GetDefaultMixMatrix(system, sourcespeakermode, targetspeakermode, matrix, matrixhop);
	}

	/**
	 * Retrieves the output driver for the selected output type.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_GetDriver(
	 *   FMOD_SYSTEM *system,
	 *   int *driver
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#setDriver}
	 * <p>
	 * 
	 * @param system
	 * @param driver <span>(<i>Out</i>) <dd>Driver index where 0 represents the default for the output type.
	 */
	public static int getDriver(long system, java.nio.IntBuffer driver)
	{
		return FMOD.FMOD_System_GetDriver(system, driver);
	}

	/**
	 * Retrieves identification information about a sound device specified by its index, and specific to the selected output mode.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_GetDriverInfo(
	 *   FMOD_SYSTEM *system,
	 *   int id,
	 *   char *name,
	 *   int namelen,
	 *   FMOD_GUID *guid,
	 *   int *systemrate,
	 *   FMOD_SPEAKERMODE *speakermode,
	 *   int *speakermodechannels
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#setOutput}
	 * <p>
	 * 
	 * @param system
	 * @param id <dd><p>Index of the sound driver device.</p><ul><li><span class="label">Range:</span> [0, {@link FModSystem#getNumDrivers})</li></ul>
	 * @param name <span>(<i>Opt, Out</i>) <dd>Name of the device. (<a href="https://www.fmod.com/docs/2.03/api/glossary.html#string-format">UTF-8 string</a>)
	 * @param guid <span>(<i>Opt, Out</i>) <dd>A <a href="https://www.fmod.com/docs/2.03/api/glossary.html#guid">GUID</a> that uniquely identifies the device. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_guid">FMOD_GUID</a>)
	 * @param systemrate <span>(<i>Opt, Out</i>) <dd>Sample rate this device operates at.
	 * @param speakermode <span>(<i>Opt, Out</i>) <dd>Speaker setup this device is currently using. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_speakermode">FMOD_SPEAKERMODE</a>)
	 * @param speakermodechannels <span>(<i>Opt, Out</i>) <dd>Number of channels in the current speaker setup.
	 */
	public static int getDriverInfo(long system, int id, java.nio.ByteBuffer name, org.lwjgl.fmod.FMOD_GUID guid, java.nio.IntBuffer systemrate, java.nio.IntBuffer speakermode, java.nio.IntBuffer speakermodechannels)
	{
		return FMOD.FMOD_System_GetDriverInfo(system, id, name, guid, systemrate, speakermode, speakermodechannels);
	}

	/**
	 * Retrieves information about file reads.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_GetFileUsage(
	 *   FMOD_SYSTEM *system,
	 *   long long *sampleBytesRead,
	 *   long long *streamBytesRead,
	 *   long long *otherBytesRead
	 * );
	 * </code></pre>
	 * <p>
	 * The values returned are running totals that never reset.
	 * <p>
	 * 
	 * @param system
	 * @param sampleBytesRead <span>(<i>Opt, Out</i>) <dd><p>Total bytes read from file for loading sample data.</p><ul><li><span class="label">Units:</span> Bytes</li></ul>
	 * @param streamBytesRead <span>(<i>Opt, Out</i>) <dd><p>Total bytes read from file for streaming sounds.</p><ul><li><span class="label">Units:</span> Bytes</li></ul>
	 * @param otherBytesRead <span>(<i>Opt, Out</i>) <dd><p>Total bytes read for non-audio data such as <a href="https://www.fmod.com/docs/2.03/api/glossary.html#bank-file">bank files</a> created in <a href="https://www.fmod.com/docs/2.03/api/glossary.html#fmod-studio">FMOD Studio</a>.</p><ul><li><span class="label">Units:</span> Bytes</li></ul>
	 */
	public static int getFileUsage(long system, java.nio.LongBuffer sampleBytesRead, java.nio.LongBuffer streamBytesRead, java.nio.LongBuffer otherBytesRead)
	{
		return FMOD.FMOD_System_GetFileUsage(system, sampleBytesRead, streamBytesRead, otherBytesRead);
	}

	/**
	 * Calculates geometry occlusion between a listener and a sound source.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_GetGeometryOcclusion(
	 *   FMOD_SYSTEM *system,
	 *   const FMOD_VECTOR *listener,
	 *   const FMOD_VECTOR *source,
	 *   float *direct,
	 *   float *reverb
	 * );
	 * </code></pre>
	 * <p>
	 * If single sided polygons have been created, it is important to get the source and listener positions around the right way,
	 * <br>as the occlusion from point A to point B may not be the same as the occlusion from point B to point A.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#createGeometry}
	 * <p>
	 * 
	 * @param system
	 * @param listener <dd>The listener position. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_vector">FMOD_VECTOR</a>)
	 * @param source <dd>The source position. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_vector">FMOD_VECTOR</a>)
	 * @param direct <span>(<i>Opt, Out</i>) <dd>Direct occlusion value. 0 = not occluded at all / full volume, 1 = fully occluded / silent.
	 * @param reverb <span>(<i>Opt, Out</i>) <dd>Reverb occlusion value. 0 = not occluded at all / wet, 1 = fully occluded / dry.
	 */
	public static int getGeometryOcclusion(long system, org.lwjgl.fmod.FMOD_VECTOR listener, org.lwjgl.fmod.FMOD_VECTOR source, java.nio.FloatBuffer direct, java.nio.FloatBuffer reverb)
	{
		return FMOD.FMOD_System_GetGeometryOcclusion(system, listener, source, direct, reverb);
	}

	/**
	 * Retrieves the maximum world size for the geometry engine.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_GetGeometrySettings(
	 *   FMOD_SYSTEM *system,
	 *   float *maxworldsize
	 * );
	 * </code></pre>
	 * <p>
	 * FMOD uses an efficient spatial partitioning system to store polygons for ray casting purposes.
	 * <br>The maximum size of the world should be set to allow processing within a known range.<br>Outside of this range, objects and polygons will not be processed as efficiently.<br>Excessive world size settings can also cause loss of precision and efficiency.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#setGeometrySettings}
	 * <p>
	 * 
	 * @param system
	 * @param maxworldsize <span>(<i>Out</i>) <dd>Maximum world size.
	 */
	public static int getGeometrySettings(long system, java.nio.FloatBuffer maxworldsize)
	{
		return FMOD.FMOD_System_GetGeometrySettings(system, maxworldsize);
	}

	/**
	 * Retrieves the master ChannelGroup that all sounds ultimately route to.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_GetMasterChannelGroup(
	 *   FMOD_SYSTEM *system,
	 *   FMOD_CHANNELGROUP **channelgroup
	 * );
	 * </code></pre>
	 * <p>
	 * This is the default
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a> that <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s play on, unless a different <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a> is specified with {@link FModSystem#playSound}, {@link FModSystem#playDSP} or {@link FModChannel#setChannelGroup}.<br>A master <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a> can be used to do things like set the 'master volume' for all playing <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s. See {@link FModChannel#setVolume}.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#createChannelGroup}
	 * <p>
	 * 
	 * @param system
	 * @param channelgroup <span>(<i>Out</i>) <dd>Master group. (<a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a>)
	 */
	public static int getMasterChannelGroup(long system, org.lwjgl.PointerBuffer channelgroup)
	{
		return FMOD.FMOD_System_GetMasterChannelGroup(system, channelgroup);
	}

	/**
	 * Retrieves the default SoundGroup, where all sounds are placed when they are created.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_GetMasterSoundGroup(
	 *   FMOD_SYSTEM *system,
	 *   FMOD_SOUNDGROUP **soundgroup
	 * );
	 * </code></pre>
	 * <p>
	 * If
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-soundgroup.html">SoundGroup</a> is released, the <a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html">Sound</a>s will be put back into this <a href="https://www.fmod.com/docs/2.03/api/core-api-soundgroup.html">SoundGroup</a>.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSoundGroup#release}, {@link FModSound#setSoundGroup}
	 * <p>
	 * 
	 * @param system
	 * @param soundGroup <span>(<i>Out</i>) <dd>Master sound group. (<a href="https://www.fmod.com/docs/2.03/api/core-api-soundgroup.html">SoundGroup</a>)
	 */
	public static int getMasterSoundGroup(long system, org.lwjgl.PointerBuffer soundGroup)
	{
		return FMOD.FMOD_System_GetMasterSoundGroup(system, soundGroup);
	}

	/**
	 * Retrieves the handle of a nested plug-in.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_GetNestedPlugin(
	 *   FMOD_SYSTEM *system,
	 *   unsigned int handle,
	 *   int index,
	 *   unsigned int *nestedhandle
	 * );
	 * </code></pre>
	 * <p>
	 * This function is used to iterate handles for plug-ins that have a list of definitions.
	 * <p>
	 * Most plug-ins contain a single definition. If this is the case, only index 0 is valid, and the returned handle is the same as the handle passed in.
	 * <p>
	 * See the
	 * <a href="https://www.fmod.com/docs/2.03/api/dsp-plugin-api-guide.html#multiple-plug-ins-within-one-file">DSP Plug-in API guide</a> for more information.
	 * <p>
	 * 
	 * @param system
	 * @param handle <dd>Handle obtained from {@link FModSystem#loadPlugin}.
	 * @param index <dd><p>Index into the list of plug-in definitions.</p><ul><li><span class="label">Range:</span> [0, {@link FModSystem#getNumNestedPlugins})</li></ul>
	 * @param nestedhandle <span>(<i>Out</i>) <dd>Handle used to represent the nested plug-in.
	 */
	public static int getNestedPlugin(long system, int handle, int index, java.nio.IntBuffer nestedhandle)
	{
		return FMOD.FMOD_System_GetNestedPlugin(system, handle, index, nestedhandle);
	}

	/**
	 * Retrieves the URL of the proxy server used in internet streaming.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_GetNetworkProxy(
	 *   FMOD_SYSTEM *system,
	 *   char *proxy,
	 *   int proxylen
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#setNetworkProxy}
	 * <p>
	 * 
	 * @param system
	 * @param proxy <span>(<i>Out</i>) <dd>Proxy server URL. (<a href="https://www.fmod.com/docs/2.03/api/glossary.html#string-format">UTF-8 string</a>)
	 */
	public static int getNetworkProxy(long system, java.nio.ByteBuffer proxy)
	{
		return FMOD.FMOD_System_GetNetworkProxy(system, proxy);
	}

	/**
	 * Retrieve the timeout value for network streams.
	 * <p>
	 * @param system
	 * @param timeout <span>(<i>Out</i>) <dd><p>Timeout value.</p><p>Units: Milliseconds</p>
	 */
	public static int getNetworkTimeout(long system, java.nio.IntBuffer timeout)
	{
		return FMOD.FMOD_System_GetNetworkTimeout(system, timeout);
	}

	/**
	 * Retrieves the number of output drivers available for the selected output type.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_GetNumDrivers(
	 *   FMOD_SYSTEM *system,
	 *   int *numdrivers
	 * );
	 * </code></pre>
	 * <p>
	 * If
	 * {@link FModSystem#setOutput} has not been called, this function will return the number of drivers available for the default output type.<br>A possible use for this function is to iterate through available sound devices for the current output type, and use {@link FModSystem#getDriverInfo} to get the device's name and other attributes.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#getDriver}
	 * <p>
	 * 
	 * @param system
	 * @param numdrivers <span>(<i>Out</i>) <dd>Number of output drivers.
	 */
	public static int getNumDrivers(long system, java.nio.IntBuffer numdrivers)
	{
		return FMOD.FMOD_System_GetNumDrivers(system, numdrivers);
	}

	/**
	 * Retrieves the number of nested plug-ins from the selected plug-in.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_GetNumNestedPlugins(
	 *   FMOD_SYSTEM *system,
	 *   unsigned int handle,
	 *   int *count
	 * );
	 * </code></pre>
	 * <p>
	 * Most plug-ins contain a single definition, in which case the count is 1, however some have a list of definitions. This function returns the number of plug-ins that have been defined.
	 * <p>
	 * See the
	 * <a href="https://www.fmod.com/docs/2.03/api/dsp-plugin-api-guide.html#multiple-plug-ins-within-one-file">DSP Plug-in API guide</a> for more information.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#getNestedPlugin}
	 * <p>
	 * 
	 * @param system
	 * @param handle <dd>Handle obtained from {@link FModSystem#loadPlugin}.
	 * @param count <dd>Returned number of plug-ins.
	 */
	public static int getNumNestedPlugins(long system, int handle, java.nio.IntBuffer count)
	{
		return FMOD.FMOD_System_GetNumNestedPlugins(system, handle, count);
	}

	/**
	 * Retrieves the number of loaded plug-ins.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_GetNumPlugins(
	 *   FMOD_SYSTEM *system,
	 *   FMOD_PLUGINTYPE plugintype,
	 *   int *numplugins
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#loadPlugin}, {@link FModSystem#getPluginHandle}
	 * <p>
	 * 
	 * @param system
	 * @param plugintype <dd>Plugin type. (<a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_plugintype">FMOD_PLUGINTYPE</a>)
	 * @param numplugins <span>(<i>Out</i>) <dd>Number of loaded plug-ins for the selected <code>plugintype</code>.
	 */
	public static int getNumPlugins(long system, int plugintype, java.nio.IntBuffer numplugins)
	{
		return FMOD.FMOD_System_GetNumPlugins(system, plugintype, numplugins);
	}

	/**
	 * Retrieves the type of output interface used to run the mixer.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_GetOutput(
	 *   FMOD_SYSTEM *system,
	 *   FMOD_OUTPUTTYPE *output
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#setOutput}
	 * <p>
	 * 
	 * @param system
	 * @param output <span>(<i>Out</i>) <dd>Output type. (<a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_outputtype">FMOD_OUTPUTTYPE</a>)
	 */
	public static int getOutput(long system, java.nio.IntBuffer output)
	{
		return FMOD.FMOD_System_GetOutput(system, output);
	}

	/**
	 * Retrieves the plug-in handle for the currently selected output type.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_GetOutputByPlugin(
	 *   FMOD_SYSTEM *system,
	 *   unsigned int *handle
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#getNumPlugins}, {@link FModSystem#setOutputByPlugin}, {@link FModSystem#setOutput}
	 * <p>
	 * 
	 * @param system
	 * @param handle <span>(<i>Out</i>) <dd>Handle to a pre-existing output plug-in.
	 */
	public static int getOutputByPlugin(long system, java.nio.IntBuffer handle)
	{
		return FMOD.FMOD_System_GetOutputByPlugin(system, handle);
	}

	/**
	 * Retrieves an output type specific internal native interface.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_GetOutputHandle(
	 *   FMOD_SYSTEM *system,
	 *   void **handle
	 * );
	 * </code></pre>
	 * <p>
	 * Reinterpret the returned handle based on the selected output type, not all types return something.
	 * <p>
	 * <ul><li><a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_outputtype_wavwriter">FMOD_OUTPUTTYPE_WAVWRITER</a>     Pointer to stdio FILE is returned</li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_outputtype_wavwriter_nrt">FMOD_OUTPUTTYPE_WAVWRITER_NRT</a> Pointer to stdio FILE is returned</li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_outputtype_wasapi">FMOD_OUTPUTTYPE_WASAPI</a>        Pointer to type IAudioClient is returned.</li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_outputtype_alsa">FMOD_OUTPUTTYPE_ALSA</a>          Pointer to type snd_pcm_t is returned.</li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_outputtype_coreaudio">FMOD_OUTPUTTYPE_COREAUDIO</a>     Handle of type AudioUnit is returned.</li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_outputtype_audioout">FMOD_OUTPUTTYPE_AUDIOOUT</a>      Pointer to type int is returned.  Handle returned from sceAudioOutOpen.</li></ul>
	 * <p>
	 * <strong>See Also:</strong> <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_outputtype">FMOD_OUTPUTTYPE</a>, {@link FModSystem#setOutput}, {@link FModSystem#init}
	 * <p>
	 * 
	 * @param system
	 * @param handle <span>(<i>Out</i>) <dd>Internal native handle.
	 */
	public static int getOutputHandle(long system, org.lwjgl.PointerBuffer handle)
	{
		return FMOD.FMOD_System_GetOutputHandle(system, handle);
	}

	/**
	 * Retrieves the handle of a plug-in based on its type and relative index.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_GetPluginHandle(
	 *   FMOD_SYSTEM *system,
	 *   FMOD_PLUGINTYPE plugintype,
	 *   int index,
	 *   unsigned int *handle
	 * );
	 * </code></pre>
	 * <p>
	 * All plug-ins, whether built-in or loaded, can be enumerated using this and
	 * {@link FModSystem#getNumPlugins}.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#loadPlugin}
	 * <p>
	 * 
	 * @param system
	 * @param plugintype <dd>Plug-in type. (<a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_plugintype">FMOD_PLUGINTYPE</a>)
	 * @param index <dd>Index in the list of plug-ins for the given <code>plugintype</code>.
	 * @param handle <span>(<i>Out</i>) <dd>Handle used to represent the plugin.
	 */
	public static int getPluginHandle(long system, int plugintype, int index, java.nio.IntBuffer handle)
	{
		return FMOD.FMOD_System_GetPluginHandle(system, plugintype, index, handle);
	}

	/**
	 * Retrieves information for the selected plug-in.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_GetPluginInfo(
	 *   FMOD_SYSTEM *system,
	 *   unsigned int handle,
	 *   FMOD_PLUGINTYPE *plugintype,
	 *   char *name,
	 *   int namelen,
	 *   unsigned int *version
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#getNumPlugins}, {@link FModSystem#getPluginHandle}
	 * <p>
	 * 
	 * @param system
	 * @param handle <dd>Handle to an already loaded plug-in.
	 * @param plugintype <span>(<i>Opt, Out</i>) <dd>Plug-in type. (<a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_plugintype">FMOD_PLUGINTYPE</a>)
	 * @param name <span>(<i>Opt, Out</i>) <dd>Name of the plug-in. (<a href="https://www.fmod.com/docs/2.03/api/glossary.html#string-format">UTF-8 string</a>)
	 * @param version <span>(<i>Opt, Out</i>) <dd>Version number of the plug-in.
	 */
	public static int getPluginInfo(long system, int handle, java.nio.IntBuffer plugintype, java.nio.ByteBuffer name, java.nio.IntBuffer version)
	{
		return FMOD.FMOD_System_GetPluginInfo(system, handle, plugintype, name, version);
	}

	/**
	 * Retrieves identification information about an audio device specified by its index, and specific to the output mode.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_GetRecordDriverInfo(
	 *   FMOD_SYSTEM *system,
	 *   int id,
	 *   char *name,
	 *   int namelen,
	 *   FMOD_GUID *guid,
	 *   int *systemrate,
	 *   FMOD_SPEAKERMODE *speakermode,
	 *   int *speakermodechannels,
	 *   FMOD_DRIVER_STATE *state
	 * );
	 * </code></pre><ul><li>Units: Bytes</li></ul>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#setOutput}
	 * <p>
	 * 
	 * @param system
	 * @param id <dd><p>Index of the recording device.</p><ul><li><span class="label">Range:</span> [0, {@link FModSystem#getRecordNumDrivers}]</li></ul>
	 * @param name <span>(<i>Opt, Out</i>) <dd>The name of the device. (<a href="https://www.fmod.com/docs/2.03/api/glossary.html#string-format">UTF-8 string</a>)
	 * @param guid <span>(<i>Opt, Out</i>) <dd>A <a href="https://www.fmod.com/docs/2.03/api/glossary.html#guid">GUID</a> that uniquely identifies the device. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_guid">FMOD_GUID</a>)
	 * @param systemrate <span>(<i>Opt, Out</i>) <dd>The sample rate the device operates at.
	 * @param speakermode <span>(<i>Opt, Out</i>) <dd>The speaker configuration the device is currently using. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_speakermode">FMOD_SPEAKERMODE</a>)
	 * @param speakermodechannels <span>(<i>Opt, Out</i>) <dd>The number of channels in the current speaker setup.
	 * @param state <span>(<i>Opt, Out</i>) <dd>Flags that provide additional information about the driver. (<a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_driver_state">FMOD_DRIVER_STATE</a>)
	 */
	public static int getRecordDriverInfo(long system, int id, java.nio.ByteBuffer name, org.lwjgl.fmod.FMOD_GUID guid, java.nio.IntBuffer systemrate, java.nio.IntBuffer speakermode, java.nio.IntBuffer speakermodechannels, java.nio.IntBuffer state)
	{
		return FMOD.FMOD_System_GetRecordDriverInfo(system, id, name, guid, systemrate, speakermode, speakermodechannels, state);
	}

	/**
	 * Retrieves the number of recording devices available for this output mode. Use this to enumerate all recording devices possible so that the user can select one.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_GetRecordNumDrivers(
	 *   FMOD_SYSTEM *system,
	 *   int *numdrivers,
	 *   int *numconnected
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#getRecordDriverInfo}
	 * <p>
	 * 
	 * @param system
	 * @param numdrivers <span>(<i>Opt, Out</i>) <dd>Number of recording drivers available for this output mode.
	 * @param numconnected <span>(<i>Opt, Out</i>) <dd>Number of recording driver currently plugged in.
	 */
	public static int getRecordNumDrivers(long system, java.nio.IntBuffer numdrivers, java.nio.IntBuffer numconnected)
	{
		return FMOD.FMOD_System_GetRecordNumDrivers(system, numdrivers, numconnected);
	}

	/**
	 * Retrieves the current recording position of the record buffer in PCM samples.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_GetRecordPosition(
	 *   FMOD_SYSTEM *system,
	 *   int id,
	 *   unsigned int *position
	 * );
	 * </code></pre>
	 * <p>
	 * Will return
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_err_record_disconnected">FMOD_ERR_RECORD_DISCONNECTED</a> if the driver is unplugged.
	 * <p>
	 * The position will return to 0 when
	 * {@link FModSystem#recordStop} is called or when a non-looping recording reaches the end.
	 * <p>
	 * <em>PS4 specific note:</em> Record devices are virtual so 'position' will continue to update if the device is unplugged (the OS is generating silence). This function will still report <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_err_record_disconnected">FMOD_ERR_RECORD_DISCONNECTED</a> for your information though.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#recordStart}, {@link FModSystem#recordStop}
	 * <p>
	 * 
	 * @param system
	 * @param id <dd><p>Index of the recording device.</p><ul><li><span class="label">Range:</span> [0, {@link FModSystem#getRecordNumDrivers}]</li></ul>
	 * @param position <span>(<i>Out</i>) <dd><p>Current recording position.</p><p>Units: Samples</p>
	 */
	public static int getRecordPosition(long system, int id, java.nio.IntBuffer position)
	{
		return FMOD.FMOD_System_GetRecordPosition(system, id, position);
	}

	/**
	 * Retrieves the current reverb environment for the specified reverb instance.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_GetReverbProperties(
	 *   FMOD_SYSTEM *system,
	 *   int instance,
	 *   FMOD_REVERB_PROPERTIES *prop
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#setReverbProperties}
	 * <p>
	 * 
	 * @param system
	 * @param instance <dd>Index of the particular reverb instance to target.
	 * @param prop <span>(<i>Out</i>) <dd>Current reverb environment description. (<a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_reverb_properties">FMOD_REVERB_PROPERTIES</a>)
	 */
	public static int getReverbProperties(long system, int instance, org.lwjgl.fmod.FMOD_REVERB_PROPERTIES prop)
	{
		return FMOD.FMOD_System_GetReverbProperties(system, instance, prop);
	}

	/**
	 * Retrieves the maximum number of software mixed Channels possible.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_GetSoftwareChannels(
	 *   FMOD_SYSTEM *system,
	 *   int *numsoftwarechannels
	 * );
	 * </code></pre>
	 * <p>
	 * Software
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s refers to real voices that will play, with <code>numsoftwarechannels</code> refering to the maximum number of voices before successive voices start becoming virtual. For differences between real and virtual voices, see the <a href="https://www.fmod.com/docs/2.03/api/managing-resources-in-the-core-api.html#virtual-voice-system">Virtual Voice System</a> section of the <a href="https://www.fmod.com/docs/2.03/api/managing-resources-in-the-core-api.html">Managing Resources in the Core API</a> chapter.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#setSoftwareChannels}
	 * <p>
	 * 
	 * @param system
	 * @param numsoftwarechannels <span>(<i>Out</i>) <dd>Current maximum number of real voices (<a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s) available.
	 */
	public static int getSoftwareChannels(long system, java.nio.IntBuffer numsoftwarechannels)
	{
		return FMOD.FMOD_System_GetSoftwareChannels(system, numsoftwarechannels);
	}

	/**
	 * Retrieves the output format for the software mixer.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_GetSoftwareFormat(
	 *   FMOD_SYSTEM *system,
	 *   int *samplerate,
	 *   FMOD_SPEAKERMODE *speakermode,
	 *   int *numrawspeakers
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#setSoftwareFormat}
	 * <p>
	 * 
	 * @param system
	 * @param samplerate <span>(<i>Opt, Out</i>) <dd>Sample rate of the mixer.
	 * @param speakermode <span>(<i>Opt, Out</i>) <dd>Speaker setup of the mixer. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_speakermode">FMOD_SPEAKERMODE</a>)
	 * @param numrawspeakers <span>(<i>Opt, Out</i>) <dd>Number of speakers for <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_speakermode_raw">FMOD_SPEAKERMODE_RAW</a> mode.
	 */
	public static int getSoftwareFormat(long system, java.nio.IntBuffer samplerate, java.nio.IntBuffer speakermode, java.nio.IntBuffer numrawspeakers)
	{
		return FMOD.FMOD_System_GetSoftwareFormat(system, samplerate, speakermode, numrawspeakers);
	}

	/**
	 * Retrieves the channel count for a given speaker mode.
	 * <p>
	 * @param system
	 * @param mode <dd>Speaker mode to query. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_speakermode">FMOD_SPEAKERMODE</a>)
	 * @param channels <span>(<i>Out</i>) <dd>Number of channels.
	 */
	public static int getSpeakerModeChannels(long system, int mode, java.nio.IntBuffer channels)
	{
		return FMOD.FMOD_System_GetSpeakerModeChannels(system, mode, channels);
	}

	/**
	 * Retrieves the position of the specified speaker for the current speaker mode.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_GetSpeakerPosition(
	 *   FMOD_SYSTEM *system,
	 *   FMOD_SPEAKER speaker,
	 *   float *x,
	 *   float *y,
	 *   FMOD_BOOL *active
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#setSpeakerPosition}
	 * <p>
	 * 
	 * @param system
	 * @param speaker <dd>Speaker. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_speaker">FMOD_SPEAKER</a>)
	 * @param x <span>(<i>Opt, Out</i>) <dd><p>2D X position relative to the listener. -1 = left, 0 = middle, +1 = right.</p><ul><li><span class="label">Range:</span> [-1, 1]</li></ul>
	 * @param y <span>(<i>Opt, Out</i>) <dd><p>2D Y position relative to the listener. -1 = back, 0 = middle, +1 = front.</p><ul><li><span class="label">Range:</span> [-1, 1]</li></ul>
	 * @param active <span>(<i>Opt, Out</i>) <dd><p>Active state of a speaker.  True = included in 3D calculations, False = ignored.</p><ul><li><span class="label">Units:</span> Boolean</li></ul>
	 */
	public static int getSpeakerPosition(long system, int speaker, java.nio.FloatBuffer x, java.nio.FloatBuffer y, java.nio.IntBuffer active)
	{
		return FMOD.FMOD_System_GetSpeakerPosition(system, speaker, x, y, active);
	}

	/**
	 * Retrieves the default file buffer size for newly opened streams.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_GetStreamBufferSize(
	 *   FMOD_SYSTEM *system,
	 *   unsigned int *filebuffersize,
	 *   FMOD_TIMEUNIT *filebuffersizetype
	 * );
	 * </code></pre>
	 * <p>
	 * Valid units for
	 * <code>filebuffersizetype</code> are:
	 * <p>
	 * <ul><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_ms">FMOD_TIMEUNIT_MS</a></li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_pcm">FMOD_TIMEUNIT_PCM</a></li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_pcmbytes">FMOD_TIMEUNIT_PCMBYTES</a></li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_rawbytes">FMOD_TIMEUNIT_RAWBYTES</a></li></ul>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#setStreamBufferSize}
	 * <p>
	 * 
	 * @param system
	 * @param filebuffersize <span>(<i>Opt, Out</i>) <dd><p>Buffer size.</p><ul><li><span class="label">Default:</span> 16384</li></ul>
	 * @param filebuffersizetype <span>(<i>Opt, Out</i>) <dd><p>Type of units for <code>filebuffersize</code>. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit">FMOD_TIMEUNIT</a>)</p><ul><li><span class="label">Default:</span> <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_rawbytes">FMOD_TIMEUNIT_RAWBYTES</a></li></ul>
	 */
	public static int getStreamBufferSize(long system, java.nio.IntBuffer filebuffersize, java.nio.IntBuffer filebuffersizetype)
	{
		return FMOD.FMOD_System_GetStreamBufferSize(system, filebuffersize, filebuffersizetype);
	}

	/**
	 * Retrieves a user value associated with a System object.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_GetUserData(
	 *   FMOD_SYSTEM *system,
	 *   void **userdata
	 * );
	 * </code></pre>
	 * <p>
	 * This function allows arbitrary user data to be retrieved from this object. See the
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#user-data">User Data</a> section of the glossary for an example of how to get and set user data.
	 * <p>
	 * 
	 * @param system
	 * @param userdata <span>(<i>Out</i>) <dd>User data set by calling {@link FModSystem#setUserData}.
	 */
	public static int getUserData(long system, org.lwjgl.PointerBuffer userdata)
	{
		return FMOD.FMOD_System_GetUserData(system, userdata);
	}

	/**
	 * Retrieves the FMOD version and build number.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_GetVersion(
	 *   FMOD_SYSTEM *system,
	 *   unsigned int *version,
	 *   unsigned int *buildnumber
	 * );
	 * </code></pre>
	 * <p>
	 * The version is a 32 bit hexadecimal value formatted as 16:8:8, with the upper 16 bits being the product version, the middle 8 bits being the major version and the bottom 8 bits being the minor version. For example a value of 0x00010203 is equal to 1.02.03.
	 * <p>
	 * Compare against
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_version">FMOD_VERSION</a> to make sure header and runtime library versions match.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#create}
	 * <p>
	 * 
	 * @param system
	 * @param version <span>(<i>Out, Opt</i>) <dd>Version number.
	 * @param buildnumber <span>(<i>Out, Opt</i>) <dd>Build number.
	 */
	public static int getVersion(long system, java.nio.IntBuffer version, java.nio.IntBuffer buildnumber)
	{
		return FMOD.FMOD_System_GetVersion(system, version, buildnumber);
	}

	/**
	 * Initialize the
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#system">system</a> object and prepare FMOD for playback.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_Init(
	 *   FMOD_SYSTEM *system,
	 *   int maxchannels,
	 *   FMOD_INITFLAGS flags,
	 *   void *extradriverdata
	 * );
	 * </code></pre>
	 * <p>
	 * A system object must be created with
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#system_create">System_create</a>
	 * <p>
	 * Most API functions require an initialized System object before they will succeed, otherwise they will return
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_err_uninitialized">FMOD_ERR_UNINITIALIZED</a>. Some can only be called before initialization. These are:
	 * <p>
	 * <ul><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#memory_initialize">Memory_Initialize</a></li><li>{@link FModSystem#setSoftwareFormat}</li><li>{@link FModSystem#setSoftwareChannels}</li><li>{@link FModSystem#setDSPBufferSize}</li></ul>
	 * <p>
	 * {@link FModSystem#setOutput} / {@link FModSystem#setOutputByPlugin} can be called before or after {@link FModSystem#init} on Android, GameCore, UWP, Windows and Mac. Other platforms can only call this <strong>before</strong> {@link FModSystem#init}.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#close}, {@link FModSystem#release}
	 * <p>
	 * 
	 * @param system
	 * @param maxchannels <dd><p>Maximum number of <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a> objects available for playback, also known as virtual voices. Virtual voices will play with minimal overhead, with a subset of 'real' voices that are mixed, and selected based on priority and audibility. See the <a href="https://www.fmod.com/docs/2.03/api/managing-resources-in-the-core-api.html#virtual-voice-system">Virtual Voice System</a> section of the <a href="https://www.fmod.com/docs/2.03/api/managing-resources-in-the-core-api.html">Managing Resources in the Core API</a> chapter for more information.</p><ul><li><span class="label">Range:</span> [0, 4095]</li></ul>
	 * @param flags <dd>Initialization flags. More than one mode can be set at once by combining them with the OR operator. (<a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_initflags">FMOD_INITFLAGS</a>)
	 * @param extradriverdata <span>(<i>Opt</i>) <dd>Additional output specific initialization data. This will be passed to the output plug-in. See <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_outputtype">FMOD_OUTPUTTYPE</a> for descriptions of data that can be passed in, based on the selected output mode.
	 */
	public static int init(long system, int maxchannels, int flags, long extradriverdata)
	{
		return FMOD.FMOD_System_Init(system, maxchannels, flags, extradriverdata);
	}

	/**
	 * Retrieves the state of the FMOD recording API, ie if it is currently recording or not.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_IsRecording(
	 *   FMOD_SYSTEM *system,
	 *   int id,
	 *   FMOD_BOOL *recording
	 * );
	 * </code></pre>
	 * <p>
	 * Recording can be started with
	 * {@link FModSystem#recordStart} and stopped with {@link FModSystem#recordStop}.
	 * <p>
	 * Will return
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_err_record_disconnected">FMOD_ERR_RECORD_DISCONNECTED</a> if the driver is unplugged.
	 * <p>
	 * <em>PS4 specific note:</em> Record devices are virtual so 'recording' will continue to report true if the device is unplugged (the OS is generating silence). This function will still report <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_err_record_disconnected">FMOD_ERR_RECORD_DISCONNECTED</a> for your information though.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#getRecordPosition}
	 * <p>
	 * 
	 * @param system
	 * @param id <dd><p>Index of the recording device.</p><ul><li><span class="label">Range:</span> [0, {@link FModSystem#getRecordNumDrivers}].</li></ul>
	 * @param recording <span>(<i>Opt, Out</i>) <dd><p>Recording state. True = system is recording, False = system is not recording.</p><ul><li><span class="label">Units:</span> Boolean</li></ul>
	 */
	public static int isRecording(long system, int id, java.nio.IntBuffer recording)
	{
		return FMOD.FMOD_System_IsRecording(system, id, recording);
	}

	/**
	 * Creates a geometry object from a block of memory which contains pre-saved geometry data.
	 * <p>
	 * This function avoids the need to manually create and add geometry for faster start time.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_LoadGeometry(
	 *   FMOD_SYSTEM *system,
	 *   const void *data,
	 *   int datasize,
	 *   FMOD_GEOMETRY **geometry
	 * );
	 * </code></pre><ul><li>Units: Bytes</li></ul>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#createGeometry}
	 * <p>
	 * 
	 * @param system
	 * @param data <dd>Pre-saved geometry data from {@link FModGeometry#save}.
	 * @param geometry <span>(<i>Out</i>) <dd>Newly created geometry object. (<a href="https://www.fmod.com/docs/2.03/api/core-api-geometry.html">Geometry</a>)
	 */
	public static int loadGeometry(long system, java.nio.ByteBuffer data, org.lwjgl.PointerBuffer geometry)
	{
		return FMOD.FMOD_System_LoadGeometry(system, data, geometry);
	}

	/**
	 * Loads an FMOD plug-in (
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP</a>, output or codec) from a file.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_LoadPlugin(
	 *   FMOD_SYSTEM *system,
	 *   const char *filename,
	 *   unsigned int *handle,
	 *   unsigned int priority
	 * );
	 * </code></pre>
	 * <p>
	 * Once loaded, DSP plug-ins can be used via
	 * {@link FModSystem#createDSPByPlugin}, output plug-ins can be used via {@link FModSystem#setOutputByPlugin}, and codec plug-ins are used automatically.
	 * <p>
	 * When opening a file each codec tests whether it can support the file format in
	 * <code>priority</code> order.
	 * <p>
	 * The format of the plug-in is dependent on the operating system:
	 * <p>
	 * <ul><li>Windows / UWP / Xbox One: .dll</li><li>Linux / Android: .so</li><li>Macintosh: .dylib</li><li>PS4: .prx</li></ul>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#unloadPlugin}, {@link FModSystem#setPluginPath}, {@link FModSystem#getNumPlugins}, {@link FModSystem#getPluginHandle}, {@link FModSystem#getPluginInfo}
	 * <p>
	 * 
	 * @param system
	 * @param filename <dd>Path to the plug-in file. (<a href="https://www.fmod.com/docs/2.03/api/glossary.html#string-format">UTF-8 string</a>)
	 * @param handle <span>(<i>Out</i>) <dd>Handle used to represent the plug-in.
	 * @param priority <span>(<i>Opt</i>) <dd>Codec load priority where 0 represents most important and higher numbers represent less importance. <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_plugintype_codec">FMOD_PLUGINTYPE_CODEC</a> only.
	 */
	public static int loadPlugin(long system, java.nio.ByteBuffer filename, java.nio.IntBuffer handle, int priority)
	{
		return FMOD.FMOD_System_LoadPlugin(system, filename, handle, priority);
	}

	/**
	 * Loads an FMOD plug-in (
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP</a>, output or codec) from a file.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_LoadPlugin(
	 *   FMOD_SYSTEM *system,
	 *   const char *filename,
	 *   unsigned int *handle,
	 *   unsigned int priority
	 * );
	 * </code></pre>
	 * <p>
	 * Once loaded, DSP plug-ins can be used via
	 * {@link FModSystem#createDSPByPlugin}, output plug-ins can be used via {@link FModSystem#setOutputByPlugin}, and codec plug-ins are used automatically.
	 * <p>
	 * When opening a file each codec tests whether it can support the file format in
	 * <code>priority</code> order.
	 * <p>
	 * The format of the plug-in is dependent on the operating system:
	 * <p>
	 * <ul><li>Windows / UWP / Xbox One: .dll</li><li>Linux / Android: .so</li><li>Macintosh: .dylib</li><li>PS4: .prx</li></ul>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#unloadPlugin}, {@link FModSystem#setPluginPath}, {@link FModSystem#getNumPlugins}, {@link FModSystem#getPluginHandle}, {@link FModSystem#getPluginInfo}
	 * <p>
	 * 
	 * @param system
	 * @param filename <dd>Path to the plug-in file. (<a href="https://www.fmod.com/docs/2.03/api/glossary.html#string-format">UTF-8 string</a>)
	 * @param handle <span>(<i>Out</i>) <dd>Handle used to represent the plug-in.
	 * @param priority <span>(<i>Opt</i>) <dd>Codec load priority where 0 represents most important and higher numbers represent less importance. <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_plugintype_codec">FMOD_PLUGINTYPE_CODEC</a> only.
	 */
	public static int loadPlugin(long system, java.lang.CharSequence filename, java.nio.IntBuffer handle, int priority)
	{
		return FMOD.FMOD_System_LoadPlugin(system, filename, handle, priority);
	}

	/**
	 * Mutual exclusion function to lock the
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-engine">DSP engine</a> (which runs asynchronously in another thread), so that it will not execute.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_LockDSP(
	 * FMOD_SYSTEM *system
	 * );
	 * </code></pre>
	 * <p>
	 * If the DSP engine is already executing, this function is blocked until it has completed.
	 * <p>
	 * The function may be used to synchronize
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-graph">DSP graph</a> operations carried out by your code. For example, you could use this to construct a <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-sub-graph">DSP sub-graph</a> without the DSP engine executing in the background while the sub-graph is still under construction.
	 * <p>
	 * Once you no longer need the DSP engine to be locked, it can be unlocked with
	 * {@link FModSystem#unlockDSP}.
	 * <p>
	 * Note that if the DSP engine is locked for a significant amount of time, it may result in audible skipping or stuttering.
	 * <p>
	 * 
	 * @param system
	 */
	public static int lockDSP(long system)
	{
		return FMOD.FMOD_System_LockDSP(system);
	}

	/**
	 * Resume mixer thread and reacquire access to audio hardware.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_MixerResume(
	 * FMOD_SYSTEM *system
	 * );
	 * </code></pre>
	 * <p>
	 * Used on mobile platforms when entering the foreground after being suspended.
	 * <p>
	 * All internal state will resume, i.e. created
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html">Sound</a> and <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s are still valid and playback will continue.
	 * <p>
	 * Android specific: Must be called on the same thread as
	 * {@link FModSystem#mixerSuspend}.
	 * <p>
	 * HTML5 specific: Used to start audio from a user interaction event, like a mouse click or screen touch event. Without this call audio may not start on some browsers.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#mixerSuspend}
	 * <p>
	 * 
	 * @param system
	 */
	public static int mixerResume(long system)
	{
		return FMOD.FMOD_System_MixerResume(system);
	}

	/**
	 * Suspend mixer thread and relinquish usage of audio hardware while maintaining internal state.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_MixerSuspend(
	 * FMOD_SYSTEM *system
	 * );
	 * </code></pre>
	 * <p>
	 * Used on mobile platforms when entering a backgrounded state to reduce CPU to 0%.
	 * <p>
	 * All internal state will be maintained, i.e. created
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html">Sound</a> and <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s will stay available in memory.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#mixerResume}
	 * <p>
	 * 
	 * @param system
	 */
	public static int mixerSuspend(long system)
	{
		return FMOD.FMOD_System_MixerSuspend(system);
	}

	/**
	 * Plays a
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP</a> along with any of its inputs on a <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_PlayDSP(
	 *   FMOD_SYSTEM *system,
	 *   FMOD_DSP *dsp,
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   FMOD_BOOL paused,
	 *   FMOD_CHANNEL **channel
	 * );
	 * </code></pre>
	 * <p>
	 * Specifying a
	 * <code>channelgroup</code> as part of playDSP is more efficient than using {@link FModChannel#setChannelGroup} after playDSP, and could avoid audible glitches if the playDSP is not in a paused state.
	 * <p>
	 * If
	 * <code>dsp</code> returns <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_err_file_eof">FMOD_ERR_FILE_EOF</a> from its process or read callback, the channel will stop.
	 * <p>
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s are reference counted to handle dead or stolen <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a> handles.  See the <a href="https://www.fmod.com/docs/2.03/api/mixing-and-routing-in-the-core-api.html#channel-and-channel-group-handles">Channel and Channel Group Handles</a> section of the <a href="https://www.fmod.com/docs/2.03/api/mixing-and-routing-in-the-core-api.html">Mixing and Routing in the Core API</a> chapter for more information.
	 * <p>
	 * Playing more
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html">Sound</a>s or <a href="https://www.fmod.com/docs/2.03/api/core-api-dsp.html">DSP</a>s than physical <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s allow is handled with virtual voices. See the <a href="https://www.fmod.com/docs/2.03/api/managing-resources-in-the-core-api.html#virtual-voice-system">Virtual Voice System</a> section of the <a href="https://www.fmod.com/docs/2.03/api/managing-resources-in-the-core-api.html">Managing Resources in the Core API</a> chapter for more information.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#createDSP}, {@link FModSystem#createDSPByType}, {@link FModSystem#createDSPByPlugin}
	 * <p>
	 * 
	 * @param system
	 * @param dsp <dd>Unit to play. (<a href="https://www.fmod.com/docs/2.03/api/core-api-dsp.html">DSP</a>)
	 * @param channelgroup <span>(<i>Opt</i>) <dd>Group to output to instead of the master. (<a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a>)
	 * @param paused <dd><p>Whether to start in the paused state. Start a <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a> paused to allow altering attributes without it being audible, then follow it up with a call to {@link FModChannel#setPaused} with <code>paused</code> = False.</p><ul><li><span class="label">Units:</span> Boolean</li></ul>
	 * @param channel <span>(<i>Opt, Out</i>) <dd>Newly playing <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>. (<a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>)
	 */
	public static int playDSP(long system, long dsp, long channelgroup, int paused, org.lwjgl.PointerBuffer channel)
	{
		return FMOD.FMOD_System_PlayDSP(system, dsp, channelgroup, paused, channel);
	}

	/**
	 * Plays a Sound on a Channel.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_PlaySound(
	 *   FMOD_SYSTEM *system,
	 *   FMOD_SOUND *sound,
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   FMOD_BOOL paused,
	 *   FMOD_CHANNEL **channel
	 * );
	 * </code></pre>
	 * <p>
	 * When a sound is played, it will use the sound's default frequency and priority.  See
	 * {@link FModSound#setDefaults}.
	 * <p>
	 * A sound defined as
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d">FMOD_3D</a> will by default play at the 3D position of the listener. To set the 3D position of the <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a> before the sound is audible, start the <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a> paused by setting the <code>paused</code> parameter to true, and call {@link FModChannel#set3DAttributes}.
	 * <p>
	 * Specifying a
	 * <code>channelgroup</code> as part of playSound is more efficient than using {@link FModChannel#setChannelGroup} after playSound, and could avoid audible glitches if the playSound is not in a paused state.
	 * <p>
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s are reference counted to handle dead or stolen <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a> handles. For more information, see the <a href="https://www.fmod.com/docs/2.03/api/mixing-and-routing-in-the-core-api.html#channel-and-channel-group-handles">Channel and Channel Group Handles</a> section of the <a href="https://www.fmod.com/docs/2.03/api/mixing-and-routing-in-the-core-api.html">Mixing and Routing in the Core API</a> chapter.
	 * <p>
	 * Playing more
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html">Sound</a>s than physical <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s allow is handled with virtual voices.  See the <a href="https://www.fmod.com/docs/2.03/api/managing-resources-in-the-core-api.html#virtual-voice-system">Virtual Voice System</a> section of the <a href="https://www.fmod.com/docs/2.03/api/managing-resources-in-the-core-api.html">Managing Resources in the Core API</a> chapter for more information.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#createSound}, {@link FModSystem#createStream}
	 * <p>
	 * 
	 * @param system
	 * @param sound <dd>Sound to play. (<a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html">Sound</a>)
	 * @param channelgroup <span>(<i>Opt</i>) <dd>Group to output to instead of the master. (<a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a>)
	 * @param paused <dd><p>Whether to start in the paused state.  Start a <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a> paused to allow altering attributes without it being audible, then follow it up with a call to {@link FModChannel#setPaused} with <code>paused</code> = False.</p><ul><li><span class="label">Units:</span> Boolean</li></ul>
	 * @param channel <span>(<i>Opt, Out</i>) <dd>Newly playing <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>. (<a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>)
	 */
	public static int playSound(long system, long sound, long channelgroup, int paused, org.lwjgl.PointerBuffer channel)
	{
		return FMOD.FMOD_System_PlaySound(system, sound, channelgroup, paused, channel);
	}

	/**
	 * Starts the recording engine recording to a pre-created Sound object.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_RecordStart(
	 *   FMOD_SYSTEM *system,
	 *   int id,
	 *   FMOD_SOUND *sound,
	 *   FMOD_BOOL loop
	 * );
	 * </code></pre>
	 * <p>
	 * Will return
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_err_record_disconnected">FMOD_ERR_RECORD_DISCONNECTED</a> if the driver is unplugged.
	 * <p>
	 * Sound must be created as
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_createsample">FMOD_CREATESAMPLE</a>.  Raw PCM data can be accessed with {@link FModSound#lock}, {@link FModSound#unlock} and {@link FModSystem#getRecordPosition}.
	 * <p>
	 * Recording from the same driver a second time will stop the first recording.
	 * <p>
	 * For lowest latency set the FMOD::Sound sample rate to the rate returned by
	 * {@link FModSystem#getRecordDriverInfo}, otherwise a resampler will be allocated to handle the difference in frequencies, which adds latency.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#recordStop}
	 * <p>
	 * 
	 * @param system
	 * @param id <dd><p>Index of the recording device.</p><ul><li><span class="label">Range:</span> [0, {@link FModSystem#getRecordNumDrivers}]</li></ul>
	 * @param sound <dd>User created sound for the user to record to. (<a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html">Sound</a>)
	 * @param loop <dd><p>Flag to tell the recording engine whether to continue recording to the provided sound from the start again, after it has reached the end.  If this is set to true the data will be continually be overwritten once every loop.</p><ul><li><span class="label">Units:</span> Boolean</li></ul>
	 */
	public static int recordStart(long system, int id, long sound, int loop)
	{
		return FMOD.FMOD_System_RecordStart(system, id, sound, loop);
	}

	/**
	 * Stops the recording engine from recording to a pre-created Sound object.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_RecordStop(
	 *   FMOD_SYSTEM *system,
	 *   int id
	 * );
	 * </code></pre>
	 * <p>
	 * Returns no error if unplugged or already stopped.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#recordStart}
	 * <p>
	 * 
	 * @param system
	 * @param id <dd><p>Index of the recording device.</p><ul><li><span class="label">Range:</span> [0, {@link FModSystem#getRecordNumDrivers}]</li></ul>
	 */
	public static int recordStop(long system, int id)
	{
		return FMOD.FMOD_System_RecordStop(system, id);
	}

	/**
	 * Register a Codec plug-in description structure for later use.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_RegisterCodec(
	 *   FMOD_SYSTEM *system,
	 *   FMOD_CODEC_DESCRIPTION *description,
	 *   unsigned int *handle,
	 *   unsigned int priority
	 * );
	 * </code></pre>
	 * <p>
	 * To create an instances of this plug-in use
	 * {@link FModSystem#createSound} and {@link FModSystem#createStream}.
	 * <p>
	 * When opening a file each Codec tests whether it can support the file format in
	 * <code>priority</code> order. The priority for each <a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_sound_type">FMOD_SOUND_TYPE</a> are as follows:
	 * <p>
	 * <table><thead><tr><th>FMOD_SOUND_TYPE</th><th>Priority</th></tr></thead><tbody><tr><td><a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_sound_type_fsb">FMOD_SOUND_TYPE_FSB</a></td><td>250</td></tr><tr><td><a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_sound_type_xma">FMOD_SOUND_TYPE_XMA</a></td><td>250</td></tr><tr><td><a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_sound_type_at9">FMOD_SOUND_TYPE_AT9</a></td><td>250</td></tr><tr><td><a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_sound_type_vorbis">FMOD_SOUND_TYPE_VORBIS</a></td><td>250</td></tr><tr><td><a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_sound_type_opus">FMOD_SOUND_TYPE_OPUS</a></td><td>250</td></tr><tr><td><a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_sound_type_fadpcm">FMOD_SOUND_TYPE_FADPCM</a></td><td>250</td></tr><tr><td><a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_sound_type_wav">FMOD_SOUND_TYPE_WAV</a></td><td>600</td></tr><tr><td><a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_sound_type_oggvorbis">FMOD_SOUND_TYPE_OGGVORBIS</a></td><td>800</td></tr><tr><td><a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_sound_type_aiff">FMOD_SOUND_TYPE_AIFF</a></td><td>1000</td></tr><tr><td><a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_sound_type_flac">FMOD_SOUND_TYPE_FLAC</a></td><td>1100</td></tr><tr><td><a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_sound_type_mod">FMOD_SOUND_TYPE_MOD</a></td><td>1200</td></tr><tr><td><a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_sound_type_s3m">FMOD_SOUND_TYPE_S3M</a></td><td>1300</td></tr><tr><td><a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_sound_type_xm">FMOD_SOUND_TYPE_XM</a></td><td>1400</td></tr><tr><td><a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_sound_type_it">FMOD_SOUND_TYPE_IT</a></td><td>1500</td></tr><tr><td><a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_sound_type_midi">FMOD_SOUND_TYPE_MIDI</a></td><td>1600</td></tr><tr><td><a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_sound_type_dls">FMOD_SOUND_TYPE_DLS</a></td><td>1700</td></tr><tr><td><a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_sound_type_asf">FMOD_SOUND_TYPE_ASF</a></td><td>1900</td></tr><tr><td><a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_sound_type_audioqueue">FMOD_SOUND_TYPE_AUDIOQUEUE</a></td><td>2200</td></tr><tr><td><a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_sound_type_mediacodec">FMOD_SOUND_TYPE_MEDIACODEC</a></td><td>2250</td></tr><tr><td><a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_sound_type_mpeg">FMOD_SOUND_TYPE_MPEG</a></td><td>2400</td></tr><tr><td><a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_sound_type_playlist">FMOD_SOUND_TYPE_PLAYLIST</a></td><td>2450</td></tr><tr><td><a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_sound_type_raw">FMOD_SOUND_TYPE_RAW</a></td><td>2500</td></tr><tr><td><a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_sound_type_user">FMOD_SOUND_TYPE_USER</a></td><td>2600</td></tr><tr><td><a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_sound_type_media_foundation">FMOD_SOUND_TYPE_MEDIA_FOUNDATION</a></td><td>2600</td></tr></tbody></table>
	 * <p>
	 * XMA, AT9, Vorbis, Opus and FADPCM are supported through the FSB format, and therefore have the same priority as FSB.
	 * <p>
	 * Media Foundation is supported through the User codec, and therefore has the same priority as User.
	 * <p>
	 * Raw and User are only accesible if
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_openraw">FMOD_OPENRAW</a> or <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_openuser">FMOD_OPENUSER</a> is specified as the mode in {@link FModSystem#createSound}.
	 * <p>
	 * 
	 * @param system
	 * @param description <dd>Structure describing the Codec to register. (<a href="https://www.fmod.com/docs/2.03/api/plugin-api-codec.html#fmod_codec_description">FMOD_CODEC_DESCRIPTION</a>)
	 * @param handle <span>(<i>Out</i>) <dd>Handle used to represent the plug-in.
	 * @param priority <span>(<i>Opt</i>) <dd>Codec load priority where 0 represents most important and higher numbers represent less importance.
	 */
	public static int registerCodec(long system, org.lwjgl.fmod.FMOD_CODEC_DESCRIPTION description, java.nio.IntBuffer handle, int priority)
	{
		return FMOD.FMOD_System_RegisterCodec(system, description, handle, priority);
	}

	/**
	 * Register a
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP</a> plug-in description structure for later use.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_RegisterDSP(
	 *   FMOD_SYSTEM *system,
	 *   const FMOD_DSP_DESCRIPTION *description,
	 *   unsigned int *handle
	 * );
	 * </code></pre>
	 * <p>
	 * To create an instance of this plug-in, use
	 * {@link FModSystem#createDSPByPlugin}.
	 * <p>
	 * 
	 * @param system
	 * @param description <dd>Structure describing the DSP to register. (<a href="https://www.fmod.com/docs/2.03/api/plugin-api-dsp.html#fmod_dsp_description">FMOD_DSP_DESCRIPTION</a>)
	 * @param handle <span>(<i>Out</i>) <dd>Handle used to represent the plug-in.
	 */
	public static int registerDSP(long system, org.lwjgl.fmod.FMOD_DSP_DESCRIPTION description, java.nio.IntBuffer handle)
	{
		return FMOD.FMOD_System_RegisterDSP(system, description, handle);
	}

	/**
	 * Register an Output plug-in description structure for later use.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_RegisterOutput(
	 *   FMOD_SYSTEM *system,
	 *   const FMOD_OUTPUT_DESCRIPTION *description,
	 *   unsigned int *handle
	 * );
	 * </code></pre>
	 * <p>
	 * To select this plug-in for output use, use
	 * {@link FModSystem#setOutputByPlugin}.
	 * <p>
	 * 
	 * @param system
	 * @param description <dd>Structure describing the Output to register. (<a href="https://www.fmod.com/docs/2.03/api/plugin-api-output.html#fmod_output_description">FMOD_OUTPUT_DESCRIPTION</a>)
	 * @param handle <span>(<i>Out</i>) <dd>Handle used to represent the plug-in.
	 */
	public static int registerOutput(long system, org.lwjgl.fmod.FMOD_OUTPUT_DESCRIPTION description, java.nio.IntBuffer handle)
	{
		return FMOD.FMOD_System_RegisterOutput(system, description, handle);
	}

	/**
	 * Closes and frees this object and its resources.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_Release(
	 * FMOD_SYSTEM *system
	 * );
	 * </code></pre>
	 * <p>
	 * This will internally call
	 * {@link FModSystem#close}, so calling {@link FModSystem#close} before this function is not necessary.
	 * <p>
	 * <div class="admonition warning">
	 * <p>
	 * Calls to
	 * {@link FModSystem#create} and {@link FModSystem#release} are not thread-safe. Do not call these functions simultaneously from multiple threads at once.
	 * <p>
	 * </div>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#init}
	 * <p>
	 * 
	 * @param system
	 */
	public static int release(long system)
	{
		return FMOD.FMOD_System_Release(system);
	}

	/**
	 * Sets the position, velocity and orientation of the specified 3D sound listener.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_Set3DListenerAttributes(
	 *   FMOD_SYSTEM *system,
	 *   int listener,
	 *   const FMOD_VECTOR *pos,
	 *   const FMOD_VECTOR *vel,
	 *   const FMOD_VECTOR *forward,
	 *   const FMOD_VECTOR *up
	 * );
	 * </code></pre>
	 * <p>
	 * The
	 * <code>forward</code> and <code>up</code> vectors must be perpendicular and be of unit length (magnitude of each vector should be 1).
	 * <p>
	 * Vectors must be provided in the correct
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#handedness">handedness</a>.
	 * <p>
	 * For velocity, remember to use units per
	 * <strong>second</strong>, and not units per frame. This is a common mistake and will make the doppler effect sound wrong if velocity is based on movement per frame rather than a fixed time period.<br>If velocity per frame is calculated, it can be converted to velocity per second by dividing it by the time taken between frames as a fraction of a second.<br>i.e.
	 * <p>
	 * <pre><code>velocity_units_per_second = (position_currentframe - position_lastframe) / time_taken_since_last_frame_in_seconds.</code></pre>
	 * <p>
	 * At 60fps the formula would look like
	 * <em>velocity_units_per_second = (position_currentframe - position_lastframe) / 0.0166667</em>.
	 * <p>
	 * Users of the Studio API should call
	 * <a href="https://www.fmod.com/docs/2.03/api/studio-api-system.html#studio_system_setlistenerattributes">Studio::System::setListenerAttributes</a> instead of this function.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#get3DListenerAttributes}, {@link FModSystem#set3DSettings}, {@link FModSystem#get3DSettings}
	 * <p>
	 * 
	 * @param system
	 * @param listener <dd><p>Index of listener to set 3D attributes on. Listeners are indexed from 0, to <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_max_listeners">FMOD_MAX_LISTENERS</a> - 1, in a multi-listener environment.</p><ul><li><span class="label">Range:</span> [0, <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_max_listeners">FMOD_MAX_LISTENERS</a>)</li></ul>
	 * @param pos <span>(<i>Opt</i>) <dd><p>Position in 3D world space used for panning and attenuation. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_vector">FMOD_VECTOR</a>)</p><ul><li><span class="label">Units:</span> <a href="https://www.fmod.com/docs/2.03/api/glossary.html#distance-units">Distance units</a></li></ul>
	 * @param vel <span>(<i>Opt</i>) <dd><p>Velocity in 3D space used for doppler. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_vector">FMOD_VECTOR</a>)</p><ul><li><span class="label">Units:</span> <a href="https://www.fmod.com/docs/2.03/api/glossary.html#distance-units">Distance units</a> per second</li></ul>
	 * @param forward <span>(<i>Opt</i>) <dd>Forwards orientation. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_vector">FMOD_VECTOR</a>)
	 * @param up <span>(<i>Opt</i>) <dd>Upwards orientation. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_vector">FMOD_VECTOR</a>)
	 */
	public static int set3DListenerAttributes(long system, int listener, org.lwjgl.fmod.FMOD_VECTOR pos, org.lwjgl.fmod.FMOD_VECTOR vel, org.lwjgl.fmod.FMOD_VECTOR forward, org.lwjgl.fmod.FMOD_VECTOR up)
	{
		return FMOD.FMOD_System_Set3DListenerAttributes(system, listener, pos, vel, forward, up);
	}

	/**
	 * Sets the number of 3D 'listeners' in the 3D sound scene.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_Set3DNumListeners(
	 *   FMOD_SYSTEM *system,
	 *   int numlisteners
	 * );
	 * </code></pre>
	 * <p>
	 * This function is useful mainly for split-screen game purposes.
	 * <p>
	 * If the number of listeners is set to more than 1, then panning and doppler are turned off. All sound effects will be mono. FMOD uses a 'closest sound to the listener' method to determine what should be heard in this case.
	 * <p>
	 * Users of the Studio API should call
	 * <a href="https://www.fmod.com/docs/2.03/api/studio-api-system.html#studio_system_setnumlisteners">Studio::System::setNumListeners</a> instead of this function.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#get3DNumListeners}, {@link FModSystem#set3DListenerAttributes}
	 * <p>
	 * 
	 * @param system
	 * @param numlisteners <span>(<i>Out</i>) <dd><p>Number of listeners in the scene.</p><ul><li><span class="label">Range:</span> [1, <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_max_listeners">FMOD_MAX_LISTENERS</a>]</li><li><span class="label">Default:</span> 1</li></ul>
	 */
	public static int set3DNumListeners(long system, int numlisteners)
	{
		return FMOD.FMOD_System_Set3DNumListeners(system, numlisteners);
	}

	/**
	 * Sets a callback to allow custom calculation of distance attenuation.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_Set3DRolloffCallback(
	 *   FMOD_SYSTEM *system,
	 *   FMOD_3D_ROLLOFF_CALLBACK callback
	 * );
	 * </code></pre>
	 * <p>
	 * This function overrides
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d_inverserolloff">FMOD_3D_INVERSEROLLOFF</a>, <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d_linearrolloff">FMOD_3D_LINEARROLLOFF</a>, <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d_linearsquarerolloff">FMOD_3D_LINEARSQUAREROLLOFF</a>, <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d_inversetaperedrolloff">FMOD_3D_INVERSETAPEREDROLLOFF</a> and <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d_customrolloff">FMOD_3D_CUSTOMROLLOFF</a>.
	 * <p>
	 * <strong>See Also:</strong> <a href="https://www.fmod.com/docs/2.03/api/glossary.html#callback-behavior">Callback Behavior</a>
	 * <p>
	 * 
	 * @param system
	 * @param callback <dd>Custom callback. Set to 0 or null to return control of distance attenuation to FMOD. (<a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_3d_rolloff_callback">FMOD_3D_ROLLOFF_CALLBACK</a>)
	 */
	public static int set3DRolloffCallback(long system, org.lwjgl.fmod.FMOD_3D_ROLLOFF_CALLBACKI callback)
	{
		return FMOD.FMOD_System_Set3DRolloffCallback(system, callback);
	}

	/**
	 * Sets the global doppler scale, distance factor and log roll-off scale for all 3D sound in FMOD.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_Set3DSettings(
	 *   FMOD_SYSTEM *system,
	 *   float dopplerscale,
	 *   float distancefactor,
	 *   float rolloffscale
	 * );
	 * </code></pre>
	 * <p>
	 * The
	 * <code>dopplerscale</code> is a general scaling factor for how much the pitch varies due to doppler shifting in 3D sound. Doppler is the pitch bending effect when a sound comes towards the listener or moves away from it, much like the effect you hear when a train goes past you with its horn sounding. With "dopplerscale" you can exaggerate or diminish the effect. FMOD's effective speed of sound at a doppler factor of 1.0 is 340 m/s.
	 * <p>
	 * The
	 * <code>distancefactor</code> is the FMOD 3D engine relative distance factor, compared to 1.0 meters. Another way to put it is that it equates to "how many units per meter does your engine have". For example, if you are using feet then "scale" would equal 3.28.<br>This only affects doppler. If you keep your min/max distance, custom roll-off curves, and positions in scale relative to each other, the volume roll-off will not change.  If you set this, the mindistance of a sound will automatically set itself to this value when it is created in case the user forgets to set the mindistance to match the new distancefactor.
	 * <p>
	 * The
	 * <code>rolloffscale</code> is a global factor applied to the roll-off of sounds using roll-off modes other than FMOD_3D_CUSTOMROLLOFF. When a sound uses a roll-off mode other than FMOD_3D_CUSTOMROLLOFF and the distance is greater than the sound's minimum distance, the distance for the purposes of distance attenuation is calculated according to the formula <code>distance = (distance - minDistance) * rolloffscale + minDistance</code>.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#get3DSettings}, {@link FModSound#set3DMinMaxDistance}, {@link FModSound#get3DMinMaxDistance}, {@link FModChannel#set3DAttributes}
	 * <p>
	 * 
	 * @param system
	 * @param dopplerscale <dd><p>A scaling factor for doppler shift.</p><ul><li><span class="label">Default:</span> 1</li></ul>
	 * @param distancefactor <dd><p>A factor for converting game distance units to FMOD distance units.</p><ul><li><span class="label">Default:</span> 1</li></ul>
	 * @param rolloffscale <dd><p>A scaling factor for distance attenuation. When a sound uses a roll-off mode other than FMOD_3D_CUSTOMROLLOFF and the distance is greater than the sound's minimum distance, the distance is scaled by the roll-off scale.</p><ul><li><span class="label">Default:</span> 1</li></ul>
	 */
	public static int set3DSettings(long system, float dopplerscale, float distancefactor, float rolloffscale)
	{
		return FMOD.FMOD_System_Set3DSettings(system, dopplerscale, distancefactor, rolloffscale);
	}

	/**
	 * Sets advanced settings for the
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#system">system</a> object, typically to allow adjusting of settings related to resource usage or audio quality.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_SetAdvancedSettings(
	 *   FMOD_SYSTEM *system,
	 *   FMOD_ADVANCEDSETTINGS *settings
	 * );
	 * </code></pre>
	 * <p>
	 * To use, clear the
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_advancedsettings">FMOD_ADVANCEDSETTINGS</a> structure to zeroes first, then set the cbSize member to the size in bytes of the <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_advancedsettings">FMOD_ADVANCEDSETTINGS</a> structure.  From here settings can be set one at a time, as all defaults are null or 0 for all members.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#getAdvancedSettings}
	 * <p>
	 * 
	 * @param system
	 * @param settings <dd>Advanced settings (<a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_advancedsettings">FMOD_ADVANCEDSETTINGS</a>)
	 */
	public static int setAdvancedSettings(long system, org.lwjgl.fmod.FMOD_ADVANCEDSETTINGS settings)
	{
		return FMOD.FMOD_System_SetAdvancedSettings(system, settings);
	}

	/**
	 * Sets the callback for System level notifications.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_SetCallback(
	 *   FMOD_SYSTEM *system,
	 *   FMOD_SYSTEM_CALLBACK callback,
	 *   FMOD_SYSTEM_CALLBACK_TYPE callbackmask
	 * );
	 * </code></pre>
	 * <p>
	 * System callbacks can be called by a variety of FMOD threads, so make sure any code executed inside the callback is thread safe.
	 * <p>
	 * <strong>See Also:</strong> <a href="https://www.fmod.com/docs/2.03/api/glossary.html#callback-behavior">Callback Behavior</a>
	 * <p>
	 * 
	 * @param system
	 * @param callback <dd>Callback to invoke when system notification happens. (<a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_system_callback">FMOD_SYSTEM_CALLBACK</a>)
	 * @param callbackmask <dd>Bitfield specifying which callback types are required, to filter out unwanted callbacks. (<a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_system_callback_type">FMOD_SYSTEM_CALLBACK_TYPE</a>)
	 */
	public static int setCallback(long system, org.lwjgl.fmod.FMOD_SYSTEM_CALLBACKI callback, int callbackmask)
	{
		return FMOD.FMOD_System_SetCallback(system, callback, callbackmask);
	}

	/**
	 * Sets the buffer size for the FMOD software mixing engine.
	 * <p>
	 * This function is used if you need to control mixer latency or granularity. Smaller buffersizes lead to smaller latency, but can lead to stuttering/skipping/unstable sound on slower machines or soundcards with bad drivers.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_SetDSPBufferSize(
	 *   FMOD_SYSTEM *system,
	 *   unsigned int bufferlength,
	 *   int numbuffers
	 * );
	 * </code></pre>
	 * <p>
	 * To get the
	 * <code>bufferlength</code> in milliseconds, divide it by the output rate and multiply the result by 1000. For a <code>bufferlength</code> of 1024 and an output rate of 48khz (see {@link FModSystem#setSoftwareFormat}), milliseconds = 1024 / 48000 * 1000 = 21.33ms. This means the mixer updates every 21.33ms.
	 * <p>
	 * To get the total buffer size multiply the
	 * <code>bufferlength</code> by the <code>numbuffers</code> value. By default this would be 4 * 1024 = 4096 samples, or 4 * 21.33ms = 85.33ms. This would generally be the total latency of the software mixer, but in reality due to one of the buffers being written to constantly, and the cursor position of the buffer that is audible, the latency is typically more like the (number of buffers - 1.5) multiplied by the buffer length.
	 * <p>
	 * To convert from milliseconds back to 'samples', simply multiply the value in milliseconds by the sample rate of the output (ie 48000 if that is what it is set to), then divide by 1000.
	 * <p>
	 * The FMOD software mixer mixes to a ringbuffer. The size of this ringbuffer is determined here. It mixes a block of sound data every 'bufferlength' number of samples, and there are 'numbuffers' number of these blocks that make up the entire ringbuffer. Adjusting these values can lead to extremely low latency performance (smaller values), or greater stability in sound output (larger values).
	 * <p>
	 * Warning!  The 'buffersize' is generally best left alone. Making the granularity smaller will just increase CPU usage (cache misses and
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-graph">DSP graph</a> overhead). Making it larger affects how often you hear commands update such as volume/pitch/pan changes. Anything above 20ms will be noticeable and sound parameter changes will be obvious instead of smooth.
	 * <p>
	 * FMOD chooses the most optimal size by default for best stability, depending on the output type. It is not recommended changing this value unless you really need to. You may get worse performance than the default settings chosen by FMOD. If you do set the size manually, the
	 * <code>bufferlength</code> argument must be a multiple of four, typically 256, 480, 512, 1024 or 2048 depedning on your latency requirements.
	 * <p>
	 * The values in milliseconds and average latency expected from the settings can be calculated using the following code:
	 * <p>
	 * <div class="highlight language-text"><pre>FMOD_RESULT result;unsigned int blocksize;int numblocks;float ms;result = system-&gt;getDSPBufferSize(&amp;blocksize, &amp;numblocks);result = system-&gt;getSoftwareFormat(&amp;frequency, 0, 0);ms = (float)blocksize * 1000.0f / (float)frequency;printf("Mixer blocksize        = %.02f ms\n", ms);printf("Mixer Total buffersize = %.02f ms\n", ms * numblocks);printf("Mixer Average Latency  = %.02f ms\n", ms * ((float)numblocks - 1.5f));</pre></div><ul><li>This function cannot be called after FMOD is already activated with {@link FModSystem#init}.</li><li>It must be called before {@link FModSystem#init}, or after {@link FModSystem#close}.</li></ul>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#getDSPBufferSize}, {@link FModSystem#getSoftwareFormat}, {@link FModSystem#init}, {@link FModSystem#close}
	 * <p>
	 * 
	 * @param system
	 * @param bufferlength <dd><p>The mixer engine block size. Use this to adjust mixer update granularity. See below for more information on buffer length vs latency.</p><ul><li><span class="label">Units:</span> Samples</li><li><span class="label">Default:</span> 1024</li></ul>
	 * @param numbuffers <dd><p>The mixer engine number of buffers used. Use this to adjust mixer latency. See below for more information on number of buffers vs latency.</p><ul><li><span class="label">Default:</span> 4</li></ul>
	 */
	public static int setDSPBufferSize(long system, int bufferlength, int numbuffers)
	{
		return FMOD.FMOD_System_SetDSPBufferSize(system, bufferlength, numbuffers);
	}

	/**
	 * Sets the output driver for the selected output type.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_SetDriver(
	 *   FMOD_SYSTEM *system,
	 *   int driver
	 * );
	 * </code></pre>
	 * <p>
	 * When an output type has more than one driver available, this function can be used to select between them.
	 * <p>
	 * If this function is called after
	 * {@link FModSystem#init}, the current driver will be shutdown and the newly selected driver will be initialized / started.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#getDriver}, {@link FModSystem#getDriverInfo}, {@link FModSystem#setOutput}
	 * <p>
	 * 
	 * @param system
	 * @param driver <dd><p>Driver index where 0 represents the default for the output type.</p><ul><li><span class="label">Range:</span> [0, {@link FModSystem#getNumDrivers})</li></ul>
	 */
	public static int setDriver(long system, int driver)
	{
		return FMOD.FMOD_System_SetDriver(system, driver);
	}

	/**
	 * Set callbacks to implement all file I/O instead of using the platform native method.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_SetFileSystem(
	 *   FMOD_SYSTEM *system,
	 *   FMOD_FILE_OPEN_CALLBACK useropen,
	 *   FMOD_FILE_CLOSE_CALLBACK userclose,
	 *   FMOD_FILE_READ_CALLBACK userread,
	 *   FMOD_FILE_SEEK_CALLBACK userseek,
	 *   FMOD_FILE_ASYNCREAD_CALLBACK userasyncread,
	 *   FMOD_FILE_ASYNCCANCEL_CALLBACK userasynccancel,
	 *   int blockalign
	 * );
	 * </code></pre>
	 * <p>
	 * Setting these callbacks have no effect on sounds loaded with
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_openmemory">FMOD_OPENMEMORY</a> or <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_openuser">FMOD_OPENUSER</a>.
	 * <p>
	 * There are three valid configurations for this function:
	 * <p>
	 * <ol><li>Set <code>useropen</code>, <code>userclose</code>, <code>userread</code>, <code>userseek</code> and optionally <code>blockalign</code> for blocking file I/O.</li><li>Set <code>useropen</code>, <code>userclose</code>, <code>userasyncread</code>, <code>userasynccancel</code> and optionally <code>blockalign</code> for asynchronous file I/O.</li><li>Set <code>blockalign</code> by itself with everything else null, to change platform native file I/O buffering.</li></ol>
	 * <p>
	 * Setting
	 * <code>blockalign</code> to 0 will disable file buffering and cause every read to invoke the relevant callback (not recommended), current default is tuned for memory usage vs performance. Be mindful of the I/O capabilities of the platform before increasing this default.
	 * <p>
	 * <strong>Asynchronous file access via <code>userasyncread</code> / <code>userasynccanel</code>.</strong>
	 * <p>
	 * <ul><li>We recommended consulting the 'asyncio' example for reference implementation. For more information, see the <a href="https://www.fmod.com/docs/2.03/api/loading-and-playing-sounds-in-the-core-api.html#asynchronous-io-and-deferred-file-reading">Asynchronous I/O and Deferred Fire Reading</a> section of the <a href="https://www.fmod.com/docs/2.03/api/loading-and-playing-sounds-in-the-core-api.html">Loading and Playing Sounds in the Core API</a> chapter.</li><li><code>userasyncread</code> allows the user to return immediately before the data is ready. FMOD will either wait internally (see note below about thread safety), or continuously check in the streamer until data arrives. It is the user's responsibility to provide data in time in the stream case, or the stream may stutter. Data starvation can be detected with {@link FModSound#getOpenState}.</li><li><strong>Important:</strong> If <code>userasyncread</code> is processed in the main thread, then it will hang the application, because FMOD will wait internally until data is ready, and the main thread will not be able to supply the data. For this reason the user's file access should normally be from a separate thread.</li><li>A <code>userasynccancel</code> must either service or prevent an async read issued previously via <code>userasyncread</code> before returning.</li></ul>
	 * <p>
	 * <strong>Implementation tips to avoid hangs / crashes.</strong>
	 * <p>
	 * <ul><li>All Callbacks must be thread safe.</li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_err_file_eof">FMOD_ERR_FILE_EOF</a> must be returned if the number of bytes read is smaller than requested.</li></ul>
	 * <p>
	 * <strong>See Also:</strong> <a href="https://www.fmod.com/docs/2.03/api/glossary.html#callback-behavior">Callback Behavior</a>, {@link FModSystem#attachFileSystem}
	 * <p>
	 * 
	 * @param system
	 * @param useropen <span>(<i>Opt</i>) <dd>Callback for opening a file. (<a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_file_open_callback">FMOD_FILE_OPEN_CALLBACK</a>)
	 * @param userclose <span>(<i>Opt</i>) <dd>Callback for closing a file. (<a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_file_close_callback">FMOD_FILE_CLOSE_CALLBACK</a>)
	 * @param userread <span>(<i>Opt</i>) <dd>Callback for reading from a file, instead of <code>userasyncread</code> and <code>userasynccancel</code>. (<a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_file_read_callback">FMOD_FILE_READ_CALLBACK</a>)
	 * @param userseek <span>(<i>Opt</i>) <dd>Callback for seeking within a file, instead of <code>userasyncread</code> and <code>userasynccancel</code>. (<a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_file_seek_callback">FMOD_FILE_SEEK_CALLBACK</a>)
	 * @param userasyncread <span>(<i>Opt</i>) <dd>Callback for reading and seeking asynchronously, instead of <code>userread</code> and <code>userseek</code>. (<a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_file_asyncread_callback">FMOD_FILE_ASYNCREAD_CALLBACK</a>)
	 * @param userasynccancel <span>(<i>Opt</i>) <dd>Callback for cancelling a previous <code>userasyncread</code> call. (<a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_file_asynccancel_callback">FMOD_FILE_ASYNCCANCEL_CALLBACK</a>)
	 * @param blockalign <span>(<i>Opt</i>) <dd><p>File buffering chunk size, specify -1 to keep system default or previously set value. 0 = disable buffering, see notes below for more on this.</p><ul><li><span class="label">Default:</span> 2048</li><li><span class="label">Units:</span> Bytes</li></ul>
	 */
	public static int setFileSystem(long system, org.lwjgl.fmod.FMOD_FILE_OPEN_CALLBACKI useropen, org.lwjgl.fmod.FMOD_FILE_CLOSE_CALLBACKI userclose, org.lwjgl.fmod.FMOD_FILE_READ_CALLBACKI userread, org.lwjgl.fmod.FMOD_FILE_SEEK_CALLBACKI userseek, org.lwjgl.fmod.FMOD_FILE_ASYNCREAD_CALLBACKI userasyncread, org.lwjgl.fmod.FMOD_FILE_ASYNCCANCEL_CALLBACKI userasynccancel, int blockalign)
	{
		return FMOD.FMOD_System_SetFileSystem(system, useropen, userclose, userread, userseek, userasyncread, userasynccancel, blockalign);
	}

	/**
	 * Sets the maximum world size for the geometry engine for performance / precision reasons.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_SetGeometrySettings(
	 *   FMOD_SYSTEM *system,
	 *   float maxworldsize
	 * );
	 * </code></pre>
	 * <p>
	 * FMOD uses an efficient spatial partitioning system to store polygons for ray casting purposes.
	 * <br>The maximum size of the world should (<code>maxworldsize</code>) be set to allow processing within a known range.<br>Outside of this range, objects and polygons will not be processed as efficiently.<br>Excessive world size settings can also cause loss of precision and efficiency.
	 * <p>
	 * Setting
	 * <code>maxworldsize</code> should be done first before creating any geometry. It can be done any time afterwards but may be slow in this case.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#createGeometry}, {@link FModSystem#getGeometrySettings}, {@link FModSystem#set3DSettings}
	 * <p>
	 * 
	 * @param system
	 * @param maxworldsize <dd>Maximum size of the world from the centerpoint to the edge using the same units used in other 3D functions.
	 */
	public static int setGeometrySettings(long system, float maxworldsize)
	{
		return FMOD.FMOD_System_SetGeometrySettings(system, maxworldsize);
	}

	/**
	 * Set a proxy server to use for all subsequent internet connections.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_SetNetworkProxy(
	 *   FMOD_SYSTEM *system,
	 *   const char *proxy
	 * );
	 * </code></pre>
	 * <p>
	 * Specify the proxy in
	 * <code>host:port</code> format e.g. <code>www.fmod.com:8888</code> (defaults to port 80 if no port is specified).
	 * <p>
	 * Basic authentication is supported using
	 * <code>user:password@host:port</code> format e.g. <code>bob:sekrit123@www.fmod.com:8888</code>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#getNetworkProxy}
	 * <p>
	 * 
	 * @param system
	 * @param proxy <dd>Proxy server URL. (<a href="https://www.fmod.com/docs/2.03/api/glossary.html#string-format">UTF-8 string</a>)
	 */
	public static int setNetworkProxy(long system, java.lang.CharSequence proxy)
	{
		return FMOD.FMOD_System_SetNetworkProxy(system, proxy);
	}

	/**
	 * Set a proxy server to use for all subsequent internet connections.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_SetNetworkProxy(
	 *   FMOD_SYSTEM *system,
	 *   const char *proxy
	 * );
	 * </code></pre>
	 * <p>
	 * Specify the proxy in
	 * <code>host:port</code> format e.g. <code>www.fmod.com:8888</code> (defaults to port 80 if no port is specified).
	 * <p>
	 * Basic authentication is supported using
	 * <code>user:password@host:port</code> format e.g. <code>bob:sekrit123@www.fmod.com:8888</code>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#getNetworkProxy}
	 * <p>
	 * 
	 * @param system
	 * @param proxy <dd>Proxy server URL. (<a href="https://www.fmod.com/docs/2.03/api/glossary.html#string-format">UTF-8 string</a>)
	 */
	public static int setNetworkProxy(long system, java.nio.ByteBuffer proxy)
	{
		return FMOD.FMOD_System_SetNetworkProxy(system, proxy);
	}

	/**
	 * Set the timeout for network streams.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_SetNetworkTimeout(
	 *   FMOD_SYSTEM *system,
	 *   int timeout
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#getNetworkTimeout}
	 * <p>
	 * 
	 * @param system
	 * @param timeout <dd><p>Timeout value.</p><ul><li><span class="label">Units:</span> Milliseconds</li><li><span class="label">Default:</span> 5000</li></ul>
	 */
	public static int setNetworkTimeout(long system, int timeout)
	{
		return FMOD.FMOD_System_SetNetworkTimeout(system, timeout);
	}

	/**
	 * Sets the type of output interface used to run the mixer.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_SetOutput(
	 *   FMOD_SYSTEM *system,
	 *   FMOD_OUTPUTTYPE output
	 * );
	 * </code></pre>
	 * <p>
	 * This function is typically used to select between different OS specific audio APIs which may have different features.
	 * <p>
	 * It is only necessary to call this function if you want to specifically switch away from the default output mode for the operating system. The most optimal mode is selected by default for the operating system.
	 * <p>
	 * (Windows, UWP, GameCore, Android, MacOS, iOS, Linux Only) This function can be called after
	 * {@link FModSystem#init} to perform special handling of driver disconnections, see <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_system_callback_devicelistchanged">FMOD_SYSTEM_CALLBACK_DEVICELISTCHANGED</a>.
	 * <p>
	 * When using the Studio API, switching to an NRT (non-realtime) output type after FMOD is already initialized will not behave correctly unless the Studio API was initialized with
	 * <a href="https://www.fmod.com/docs/2.03/api/studio-api-system.html#fmod_studio_init_synchronous_update">FMOD_STUDIO_INIT_SYNCHRONOUS_UPDATE</a>.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#getOutput}
	 * <p>
	 * 
	 * @param system
	 * @param output <dd>Output type. (<a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_outputtype">FMOD_OUTPUTTYPE</a>)
	 */
	public static int setOutput(long system, int output)
	{
		return FMOD.FMOD_System_SetOutput(system, output);
	}

	/**
	 * Selects an output type given a plug-in handle.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_SetOutputByPlugin(
	 *   FMOD_SYSTEM *system,
	 *   unsigned int handle
	 * );
	 * </code></pre>
	 * <p>
	 * (Windows Only) This function can be called after FMOD is already initialized. You can use it to change the output mode at runtime. If
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_system_callback_devicelistchanged">FMOD_SYSTEM_CALLBACK_DEVICELISTCHANGED</a> is specified use the setOutput call to change to <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_outputtype_nosound">FMOD_OUTPUTTYPE_NOSOUND</a> if no more sound card drivers exist.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#getNumPlugins}, {@link FModSystem#getOutputByPlugin}, {@link FModSystem#setOutput}
	 * <p>
	 * 
	 * @param system
	 * @param handle <dd>Handle to an already loaded output plug-in.
	 */
	public static int setOutputByPlugin(long system, int handle)
	{
		return FMOD.FMOD_System_SetOutputByPlugin(system, handle);
	}

	/**
	 * Specify a base search path for plug-ins so they can be placed somewhere else than the directory of the main executable.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_SetPluginPath(
	 *   FMOD_SYSTEM *system,
	 *   const char *path
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#loadPlugin}
	 * <p>
	 * 
	 * @param system
	 * @param path <dd>A character string containing a correctly formatted path to load plug-ins from. (<a href="https://www.fmod.com/docs/2.03/api/glossary.html#string-format">UTF-8 string</a>)
	 */
	public static int setPluginPath(long system, java.nio.ByteBuffer path)
	{
		return FMOD.FMOD_System_SetPluginPath(system, path);
	}

	/**
	 * Specify a base search path for plug-ins so they can be placed somewhere else than the directory of the main executable.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_SetPluginPath(
	 *   FMOD_SYSTEM *system,
	 *   const char *path
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#loadPlugin}
	 * <p>
	 * 
	 * @param system
	 * @param path <dd>A character string containing a correctly formatted path to load plug-ins from. (<a href="https://www.fmod.com/docs/2.03/api/glossary.html#string-format">UTF-8 string</a>)
	 */
	public static int setPluginPath(long system, java.lang.CharSequence path)
	{
		return FMOD.FMOD_System_SetPluginPath(system, path);
	}

	/**
	 * Sets parameters for the global reverb environment.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_SetReverbProperties(
	 *   FMOD_SYSTEM *system,
	 *   int instance,
	 *   const FMOD_REVERB_PROPERTIES *prop
	 * );
	 * </code></pre>
	 * <p>
	 * To assist in defining reverb properties there are several presets available, see
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_reverb_presets">FMOD_REVERB_PRESETS</a>
	 * <p>
	 * When using each instance for the first time, FMOD will create an SFX reverb
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP unit</a> that takes up several hundred kilobytes of memory and some CPU.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#getReverbProperties}, {@link FModChannel#setReverbProperties}, {@link FModChannel#getReverbProperties}, <a href="https://www.fmod.com/docs/2.03/api/effects-reference.html#sfx-reverb">Effects Reference: SFX Reverb</a>
	 * <p>
	 * 
	 * @param system
	 * @param instance <dd><p>Index of the particular reverb instance to target.</p><ul><li><span class="label">Range:</span> [0, <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_reverb_maxinstances">FMOD_REVERB_MAXINSTANCES</a>].</li></ul>
	 * @param prop <dd>Reverb environment description. Passing 0 or NULL to this function will delete the reverb. (<a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_reverb_properties">FMOD_REVERB_PROPERTIES</a>)
	 */
	public static int setReverbProperties(long system, int instance, org.lwjgl.fmod.FMOD_REVERB_PROPERTIES prop)
	{
		return FMOD.FMOD_System_SetReverbProperties(system, instance, prop);
	}

	/**
	 * Sets the maximum number of software mixed Channels possible.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_SetSoftwareChannels(
	 *   FMOD_SYSTEM *system,
	 *   int numsoftwarechannels
	 * );
	 * </code></pre>
	 * <p>
	 * This function cannot be called after FMOD is already activated, it must be called before
	 * {@link FModSystem#init}, or after {@link FModSystem#close}.<br>'Software Channels' refers to real <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s that will play, with <code>numsoftwarechannels</code> refering to the maximum number of <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s before successive <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s start becoming virtual. For differences between real and virtual <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s see the <a href="https://www.fmod.com/docs/2.03/api/managing-resources-in-the-core-api.html#virtual-voice-system">Virtual Voice System</a> section of the <a href="https://www.fmod.com/docs/2.03/api/managing-resources-in-the-core-api.html">Managing Resources in the Core API</a> chapter.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#getSoftwareChannels}, {@link FModSystem#init}, {@link FModSystem#close}
	 * <p>
	 * 
	 * @param system
	 * @param numsoftwarechannels <dd><p>The maximum number of real <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s to be allocated by FMOD.</p><ul><li><span class="label">Default:</span> 64</li></ul>
	 */
	public static int setSoftwareChannels(long system, int numsoftwarechannels)
	{
		return FMOD.FMOD_System_SetSoftwareChannels(system, numsoftwarechannels);
	}

	/**
	 * Sets the output format for the software mixer.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_SetSoftwareFormat(
	 *   FMOD_SYSTEM *system,
	 *   int samplerate,
	 *   FMOD_SPEAKERMODE speakermode,
	 *   int numrawspeakers
	 * );
	 * </code></pre>
	 * <p>
	 * If loading
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#bank-file">banks</a> made in <a href="https://www.fmod.com/docs/2.03/api/glossary.html#fmod-studio">FMOD Studio</a>, this must be called with <code>speakermode</code> corresponding to the project output format if there is a possibility of the output audio device not matching the project format. Any differences between the project format and <code>speakermode</code> will cause the mix to sound wrong.
	 * <p>
	 * By default
	 * <code>speakermode</code> will assume the setup the OS / output prefers.
	 * <p>
	 * Altering the
	 * <code>samplerate</code> from the OS / output preferred rate may incur extra latency. Altering the <code>speakermode</code> from the OS / output preferred mode may cause an upmix/downmix which can alter the sound.
	 * <p>
	 * On lower power platforms such as mobile
	 * <code>samplerate</code> will default to 24 kHz to reduce CPU cost.
	 * <p>
	 * This function must be called before
	 * {@link FModSystem#init}, or after {@link FModSystem#close}.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#getSoftwareFormat}
	 * <p>
	 * 
	 * @param system
	 * @param samplerate <span>(<i>Opt</i>) <dd><p>Sample rate of the mixer.</p><ul><li><span class="label">Range:</span> [8000, 192000]</li><li><span class="label">Units:</span> Hertz</li><li><span class="label">Default:</span> 48000</li></ul>
	 * @param speakermode <span>(<i>Opt</i>) <dd>Speaker setup of the mixer. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_speakermode">FMOD_SPEAKERMODE</a>)
	 * @param numrawspeakers <span>(<i>Opt</i>) <dd><p>Number of speakers for <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_speakermode_raw">FMOD_SPEAKERMODE_RAW</a> mode.</p><ul><li><span class="label">Range:</span> [0, <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_max_channel_width">FMOD_MAX_CHANNEL_WIDTH</a>]</li></ul>
	 */
	public static int setSoftwareFormat(long system, int samplerate, int speakermode, int numrawspeakers)
	{
		return FMOD.FMOD_System_SetSoftwareFormat(system, samplerate, speakermode, numrawspeakers);
	}

	/**
	 * Sets the position of the specified speaker for the current speaker mode.
	 * <p>
	 * This function allows the user to specify the position of their speaker to account for non standard setups.
	 * <br>It also allows the user to disable speakers from 3D consideration in a game.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_SetSpeakerPosition(
	 *   FMOD_SYSTEM *system,
	 *   FMOD_SPEAKER speaker,
	 *   float x,
	 *   float y,
	 *   FMOD_BOOL active
	 * );
	 * </code></pre>
	 * <p>
	 * This allows you to customize the position of the speakers for the current
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_speakermode">FMOD_SPEAKERMODE</a> by giving X (left to right) and Y (front to back) coordinates.<br>When disabling a speaker, 3D spatialization is redistributed around the missing speaker so that <a href="https://www.fmod.com/docs/2.03/api/glossary.html#signal">signal</a> isn't lost.
	 * <p>
	 * Stereo setup would look like this:
	 * <p>
	 * <pre><code>
	 * FMOD_System_SetSpeakerPosition(
	 * system,
	 *  FMOD_SPEAKER_FRONT_LEFT,
	 *  -1.0f,
	 *   0.0f,
	 *  1
	 * );
	 * FMOD_System_SetSpeakerPosition(
	 * system,
	 *  FMOD_SPEAKER_FRONT_RIGHT,
	 *  1.0f,
	 *   0.0f,
	 *  1
	 * );
	 * </code></pre>
	 * <p>
	 * 7.1 setup would look like this:
	 * <p>
	 * <pre><code>
	 * FMOD_System_SetSpeakerPosition(
	 * system,
	 *  FMOD_SPEAKER_FRONT_LEFT,
	 *      sin(
	 * degtorad(
	 *  -30)), cos(
	 * degtorad(
	 *  -30)), 1
	 * );
	 * FMOD_System_SetSpeakerPosition(
	 * system,
	 *  FMOD_SPEAKER_FRONT_RIGHT,
	 *     sin(
	 * degtorad(
	 *   30)), cos(
	 * degtorad(
	 *   30)), 1
	 * );
	 * FMOD_System_SetSpeakerPosition(
	 * system,
	 *  FMOD_SPEAKER_FRONT_CENTER,
	 *    sin(
	 * degtorad(
	 *    0)), cos(
	 * degtorad(
	 *    0)), 1
	 * );
	 * FMOD_System_SetSpeakerPosition(
	 * system,
	 *  FMOD_SPEAKER_LOW_FREQUENCY,
	 *   sin(
	 * degtorad(
	 *    0)), cos(
	 * degtorad(
	 *    0)), 1
	 * );
	 * FMOD_System_SetSpeakerPosition(
	 * system,
	 *  FMOD_SPEAKER_SURROUND_LEFT,
	 *   sin(
	 * degtorad(
	 *  -90)), cos(
	 * degtorad(
	 *  -90)), 1
	 * );
	 * FMOD_System_SetSpeakerPosition(
	 * system,
	 *  FMOD_SPEAKER_SURROUND_RIGHT,
	 *  sin(
	 * degtorad(
	 *   90)), cos(
	 * degtorad(
	 *   90)), 1
	 * );
	 * FMOD_System_SetSpeakerPosition(
	 * system,
	 *  FMOD_SPEAKER_BACK_LEFT,
	 *       sin(
	 * degtorad(
	 * -150)), cos(
	 * degtorad(
	 * -150)), 1
	 * );
	 * FMOD_System_SetSpeakerPosition(
	 * system,
	 *  FMOD_SPEAKER_BACK_RIGHT,
	 *      sin(
	 * degtorad(
	 *  150)), cos(
	 * degtorad(
	 *  150)), 1
	 * );
	 * </code></pre>
	 * <p>
	 * Calling
	 * {@link FModSystem#setSoftwareFormat} will override any customization made with this function.
	 * <p>
	 * Users of the Studio API should be aware this function does not affect the speaker positions used by the spatializer
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSPs</a> such as the <a href="https://www.fmod.com/docs/2.03/api/effects-reference.html#pan">pan</a> and <a href="https://www.fmod.com/docs/2.03/api/effects-reference.html#object-panner">object panner</a> DSPs. It is purely for Core API spatialization via {@link FModChannel#set3DAttributes}.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#getSpeakerPosition}
	 * <p>
	 * 
	 * @param system
	 * @param speaker <dd>Speaker. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_speaker">FMOD_SPEAKER</a>)
	 * @param x <dd><p>2D X position relative to the listener. -1 = left, 0 = middle, +1 = right.</p><ul><li><span class="label">Range:</span> [-1, 1]</li></ul>
	 * @param y <dd><p>2D Y position relative to the listener. -1 = back, 0 = middle, +1 = front.</p><ul><li><span class="label">Range:</span> [-1, 1]</li></ul>
	 * @param active <dd><p>Active state of a speaker.  True = included in 3D calculations, False = ignored.</p><ul><li><span class="label">Units:</span> Boolean</li></ul>
	 */
	public static int setSpeakerPosition(long system, int speaker, float x, float y, int active)
	{
		return FMOD.FMOD_System_SetSpeakerPosition(system, speaker, x, y, active);
	}

	/**
	 * Sets the default file buffer size for newly opened streams.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_SetStreamBufferSize(
	 *   FMOD_SYSTEM *system,
	 *   unsigned int filebuffersize,
	 *   FMOD_TIMEUNIT filebuffersizetype
	 * );
	 * </code></pre>
	 * <p>
	 * Valid units for
	 * <code>filebuffersizetype</code> are:
	 * <p>
	 * <ul><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_ms">FMOD_TIMEUNIT_MS</a></li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_pcm">FMOD_TIMEUNIT_PCM</a></li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_pcmbytes">FMOD_TIMEUNIT_PCMBYTES</a></li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_rawbytes">FMOD_TIMEUNIT_RAWBYTES</a></li></ul>
	 * <p>
	 * Larger values will consume more memory, whereas smaller values may cause buffer under-run / starvation / stuttering caused by large delays in disk access (ie netstream), or CPU usage in slow machines, or by trying to play too many streams at once.
	 * <p>
	 * Does not affect streams created with
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_openuser">FMOD_OPENUSER</a>, as the buffer size is specified in {@link FModSystem#createSound}.
	 * <p>
	 * Does not affect latency of playback.  All streams are pre-buffered (unless opened with
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_openonly">FMOD_OPENONLY</a>), so they will always start immediately.
	 * <p>
	 * Seek and Play operations can sometimes cause a reflush of this buffer.
	 * <p>
	 * If
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_rawbytes">FMOD_TIMEUNIT_RAWBYTES</a> is used, the memory allocated is two times the size passed in, because fmod allocates a double buffer.
	 * <p>
	 * If
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_ms">FMOD_TIMEUNIT_MS</a>, <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_pcm">FMOD_TIMEUNIT_PCM</a> or <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_pcmbytes">FMOD_TIMEUNIT_PCMBYTES</a> is used, and the stream is infinite (such as a shoutcast netstream), or VBR, then FMOD cannot calculate an accurate compression ratio to work with when the file is opened. This means it will then base the buffersize on <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_pcmbytes">FMOD_TIMEUNIT_PCMBYTES</a>, or in other words the number of PCM bytes, but this will be incorrect for some compressed formats. Use <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_rawbytes">FMOD_TIMEUNIT_RAWBYTES</a> for these type (infinite / undetermined length) of streams for more accurate read sizes.
	 * <p>
	 * To determine the actual memory usage of a stream, including sound buffer and other overhead, use
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#memory_getstats">Memory_GetStats</a> before and after creating a sound.
	 * <p>
	 * Stream may still stutter if the codec uses a large amount of cpu time, which impacts the smaller, internal 'decode' buffer. The decode buffer size is changeable via
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_createsoundexinfo">FMOD_CREATESOUNDEXINFO</a>.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#getStreamBufferSize}, {@link FModSound#getOpenState}
	 * <p>
	 * 
	 * @param system
	 * @param filebuffersize <dd><p>Buffer size.</p><ul><li><span class="label">Default:</span> 16384</li></ul>
	 * @param filebuffersizetype <dd><p>Type of units for <code>filebuffersize</code>. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit">FMOD_TIMEUNIT</a>)</p><ul><li><span class="label">Default:</span> <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_rawbytes">FMOD_TIMEUNIT_RAWBYTES</a></li></ul>
	 */
	public static int setStreamBufferSize(long system, int filebuffersize, int filebuffersizetype)
	{
		return FMOD.FMOD_System_SetStreamBufferSize(system, filebuffersize, filebuffersizetype);
	}

	/**
	 * Sets a user value associated with a System object.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_SetUserData(
	 *   FMOD_SYSTEM *system,
	 *   void *userdata
	 * );
	 * </code></pre>
	 * <p>
	 * This function allows arbitrary user data to be attached to this object, which wll be passed through the
	 * <code>userdata</code> parameter in any <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_system_callback">FMOD_SYSTEM_CALLBACK</a>s. See the <a href="https://www.fmod.com/docs/2.03/api/glossary.html#user-data">User Data</a> section of the glossary for an example of how to get and set user data.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#getUserData}, {@link FModSystem#setCallback}
	 * <p>
	 * 
	 * @param system
	 * @param userdata <dd>User specified data to be stored within the System object.
	 */
	public static int setUserData(long system, long userdata)
	{
		return FMOD.FMOD_System_SetUserData(system, userdata);
	}

	/**
	 * Unloads an FMOD (
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP</a>, Output or Codec) plug-in.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_UnloadPlugin(
	 *   FMOD_SYSTEM *system,
	 *   unsigned int handle
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#loadPlugin}
	 * <p>
	 * 
	 * @param system
	 * @param handle <dd>Handle to an already loaded plug-in.
	 */
	public static int unloadPlugin(long system, int handle)
	{
		return FMOD.FMOD_System_UnloadPlugin(system, handle);
	}

	/**
	 * Mutual exclusion function to unlock the
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-engine">DSP engine</a> (which runs asynchronously in another thread) and let it continue executing.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_UnlockDSP(
	 * FMOD_SYSTEM *system
	 * );
	 * </code></pre>
	 * <p>
	 * The DSP engine must be locked with
	 * {@link FModSystem#lockDSP} before this function is called.
	 * <p>
	 * 
	 * @param system
	 */
	public static int unlockDSP(long system)
	{
		return FMOD.FMOD_System_UnlockDSP(system);
	}

	/**
	 * Updates the FMOD
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#system">system</a>.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_System_Update(
	 * FMOD_SYSTEM *system
	 * );
	 * </code></pre>
	 * <p>
	 * Should be called once per 'game' tick, or once per frame in your application to perform actions such as:
	 * <p>
	 * <ul><li>Panning and reverb from 3D attributes changes.</li><li>Virtualization of <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s based on their audibility.</li><li>Mixing for non-realtime output types. See comment below.</li><li>Streaming if using <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_init_stream_from_update">FMOD_INIT_STREAM_FROM_UPDATE</a>.</li><li>Mixing if using <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_init_mix_from_update">FMOD_INIT_MIX_FROM_UPDATE</a></li><li>Firing callbacks that are deferred until Update.</li><li><a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP</a> cleanup.</li></ul>
	 * <p>
	 * If
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_outputtype_nosound_nrt">FMOD_OUTPUTTYPE_NOSOUND_NRT</a> or <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_outputtype_wavwriter_nrt">FMOD_OUTPUTTYPE_WAVWRITER_NRT</a> output modes are used, this function also drives the software / <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-engine">DSP engine</a>, instead of it running asynchronously in a thread as is the default behavior.<br>This can be used for faster than realtime updates to the decoding or DSP engine which might be useful if the output is the wav writer for example.
	 * <p>
	 * If
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_init_stream_from_update">FMOD_INIT_STREAM_FROM_UPDATE</a> is used, this function will update the stream engine. Combining this with the non realtime output will mean smoother captured output.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#init}, <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_initflags">FMOD_INITFLAGS</a>, <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_outputtype">FMOD_OUTPUTTYPE</a>, <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_mode">FMOD_MODE</a>
	 * <p>
	 * @param system
	 */
	public static int update(long system)
	{
		return FMOD.FMOD_System_Update(system);
	}

}
