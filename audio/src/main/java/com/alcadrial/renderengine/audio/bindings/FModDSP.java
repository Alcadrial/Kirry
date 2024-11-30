package com.alcadrial.renderengine.audio.bindings;

import org.lwjgl.fmod.FMOD;

/**
 * <div class="manual-content api"><h1>7. Core API Reference | DSP</h1>The Digital Signal Processor is one node within a graph that transforms input audio signals to an output stream.
 * <p>
 * Create with {@link FModSystem#createDSP}, {@link FModSystem#createDSPByType} or {@link FModSystem#createDSPByPlugin}.
 * <p>
 * <strong>Connections:</strong>
 * <p>
 * <ul>
 *     <li><span>{@link FModDSP#addInput} Adds a DSP unit as an input to this object.</span></li>
 *     <li><span>{@link FModDSP#getInput} Retrieves the DSP unit at the specified index in the input list.</span></li>
 *     <li><span>{@link FModDSP#getOutput} Retrieves the DSP unit at the specified index in the output list.</span></li>
 *     <li><span>{@link FModDSP#getNumInputs} Retrieves the number of DSP units in the input list.</span></li>
 *     <li><span>{@link FModDSP#getNumOutputs} Retrieves the number of DSP units in the output list.</span></li>
 *     <li><span>{@link FModDSP#disconnectAll} Disconnects all inputs and/or outputs.</span></li>
 *     <li><span>{@link FModDSP#disconnectFrom} Disconnect the specified input DSP.</span></li>
 * </ul>
 * <strong>Parameters:</strong>
 * <p>
 * <ul>
 *     <li><span>{@link FModDSP#getDataParameterIndex} Retrieve the index of the first data parameter of a particular data type.</span></li>
 *     <li><span>{@link FModDSP#getNumParameters} Retrieves the number of parameters exposed by this unit.</span></li>
 *     <li><span>{@link FModDSP#setParameterBool} Sets a boolean parameter by index.</span></li>
 *     <li><span>{@link FModDSP#getParameterBool} Retrieves a boolean parameter by index.</span></li>
 *     <li><span>{@link FModDSP#setParameterData} Sets a binary data parameter by index.</span></li>
 *     <li><span>{@link FModDSP#getParameterData} Retrieves a binary data parameter by index.</span></li>
 *     <li><span>{@link FModDSP#setParameterFloat} Sets a floating point parameter by index.</span></li>
 *     <li><span>{@link FModDSP#getParameterFloat} Retrieves a floating point parameter by index.</span></li>
 *     <li><span>{@link FModDSP#setParameterInt} Sets an integer parameter by index.</span></li>
 *     <li><span>{@link FModDSP#getParameterInt} Retrieves an integer parameter by index.</span></li>
 *     <li><span>{@link FModDSP#getParameterInfo} Retrieve information about a specified parameter.</span></li>
 * </ul>
 * <strong>Channel format:</strong>
 * <p>
 * <ul>
 *     <li><span>{@link FModDSP#setChannelFormat} Sets the PCM input format this DSP will receive when processing.</span></li>
 *     <li><span>{@link FModDSP#getChannelFormat} Retrieves the PCM input format this DSP will receive when processing.</span></li>
 *     <li><span>{@link FModDSP#getOutputChannelFormat} Retrieves the output format this DSP will produce when processing based on the input specified.</span></li>
 * </ul>
 * <strong>Metering:</strong>
 * <p>
 * <ul>
 *     <li><span>{@link FModDSP#getMeteringInfo} Retrieve the signal metering information.</span></li>
 *     <li><span>{@link FModDSP#setMeteringEnabled} Sets the input and output signal metering enabled states.</span></li>
 *     <li><span>{@link FModDSP#getMeteringEnabled} Retrieves the input and output signal metering enabled states.</span></li>
 * </ul>
 * <strong>Processing:</strong>
 * <p>
 * <ul>
 *     <li><span>{@link FModDSP#setActive} Sets the processing active state.</span></li>
 *     <li><span>{@link FModDSP#getActive} Retrieves the processing active state.</span></li>
 *     <li><span>{@link FModDSP#setBypass} Sets the processing bypass state.</span></li>
 *     <li><span>{@link FModDSP#getBypass} Retrieves the processing bypass state.</span></li>
 *     <li><span>{@link FModDSP#setWetDryMix} Sets the scale of the wet and dry signal components.</span></li>
 *     <li><span>{@link FModDSP#getWetDryMix} Retrieves the scale of the wet and dry signal components.</span></li>
 *     <li><span>{@link FModDSP#getIdle} Retrieves the idle state.</span></li>
 * </ul>
 * <strong>General:</strong>
 * <p>
 * <ul>
 *     <li><span>{@link FModDSP#showConfigDialog} Display or hide a DSP unit configuration dialog box inside the target window.</span></li>
 *     <li><span>{@link FModDSP#reset} Reset a DSPs internal state ready for new input signal.</span></li>
 *     <li><span>{@link FModDSP#release} Frees a DSP object.</span></li>
 *     <li><span>{@link FModDSP#getType} Retrieves the pre-defined type of a FMOD registered DSP unit.</span></li>
 *     <li><span>{@link FModDSP#getInfo} Retrieves information about this DSP unit.</span></li>
 *     <li><span>{@link FModDSP#getCPUUsage} Retrieves statistics on the mixer thread CPU usage for this unit.</span></li>
 *     <li><span>{@link FModDSP#setUserData} Sets a user value associated with this object.</span></li>
 *     <li><span>{@link FModDSP#getUserData} Retrieves a user value associated with this object.</span></li>
 *     <li><span>{@link FModDSP#setCallback} Sets the callback for DSP notifications.</span></li>
 *     <li><span>{@link FModDSP#getSystemObject} Retrieves the parent System object.</span></li>
 * </ul>
 * <hr><ul>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-dsp.html#fmod_dsp_callback">FMOD_DSP_CALLBACK</a> Callback for DSP notifications.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-dsp.html#fmod_dsp_callback_type">FMOD_DSP_CALLBACK_TYPE</a> Types of callbacks called by DSPs.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-dsp.html#fmod_dsp_data_parameter_info">FMOD_DSP_DATA_PARAMETER_INFO</a> Describes the value of a DSP data parameter.</span></li>
 * </ul>
 * 
 * @author FMOD team, autogenerated by Alcadrial
 */
public class FModDSP {
	
	private FModDSP()
	{
		
	}
	
	/**
	 * Adds a DSP unit as an input to this object.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_AddInput(
	 *   FMOD_DSP *dsp,
	 *   FMOD_DSP *input,
	 *   FMOD_DSPCONNECTION **connection,
	 *   FMOD_DSPCONNECTION_TYPE type
	 * );
	 * </code></pre>
	 * <p>
	 * When a 
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-dsp.html">DSP</a> has multiple inputs the signals are automatically mixed together, sent to the unit's output(s).
	 * <p>
	 * The returned 
	 * <code>connection</code> will remain valid until the units are disconnected.
	 * <p>
	 * <strong>See Also:</strong> {@link FModDSP#getInput}, {@link FModDSP#getNumInputs}, {@link FModDSP#getOutput}, {@link FModDSP#getNumOutputs}
	 * <p>
	 * 
	 * @param dSP
	 * @param input <dd>DSP unit to be added. (<a href="https://www.fmod.com/docs/2.03/api/core-api-dsp.html">DSP</a>)
	 * @param connection <span>(<i>Out, Opt</i>) <dd>Connection between the two units. (<a href="https://www.fmod.com/docs/2.03/api/core-api-dspconnection.html">DSPConnection</a>)
	 * @param type <dd><p>Type of connection between the two units. (<a href="https://www.fmod.com/docs/2.03/api/core-api-dspconnection.html#fmod_dspconnection_type">FMOD_DSPCONNECTION_TYPE</a>)</p><ul><li><span class="label">Default:</span> <a href="https://www.fmod.com/docs/2.03/api/core-api-dspconnection.html#fmod_dspconnection_type_standard">FMOD_DSPCONNECTION_TYPE_STANDARD</a></li></ul>
	 */
	public static int addInput(long dSP, long input, org.lwjgl.PointerBuffer connection, int type)
	{
		return FMOD.FMOD_DSP_AddInput(dSP, input, connection, type);
	}
	
	/**
	 * Disconnects all inputs and/or outputs.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_DisconnectAll(
	 *   FMOD_DSP *dsp,
	 *   FMOD_BOOL inputs,
	 *   FMOD_BOOL outputs
	 * );
	 * </code></pre>
	 * <p>
	 * This is a convenience function that is faster than disconnecting all inputs and outputs individually.
	 * <p>
	 * <strong>See Also:</strong> {@link FModDSP#disconnectFrom}
	 * <p>
	 * 
	 * @param dSP
	 * @param inputs <dd><p>Whether all inputs should be disconnected.</p><ul><li><span class="label">Units:</span> Boolean</li></ul>
	 * @param outputs <dd><p>Whether all outputs should be disconnected.</p><ul><li><span class="label">Units:</span> Boolean</li></ul>
	 */
	public static int disconnectAll(long dSP, int inputs, int outputs)
	{
		return FMOD.FMOD_DSP_DisconnectAll(dSP, inputs, outputs);
	}
	
	/**
	 * Disconnect the specified input DSP.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_DisconnectFrom(
	 *   FMOD_DSP *dsp,
	 *   FMOD_DSP *target,
	 *   FMOD_DSPCONNECTION *connection
	 * );
	 * </code></pre>
	 * <p>
	 * If 
	 * <code>target</code> had only one output, after this operation that entire sub graph will no longer be connected to the DSP network.
	 * <p>
	 * After this operation 
	 * <code>connection</code> is no longer valid.
	 * <p>
	 * <strong>See Also:</strong> {@link FModDSP#addInput}, {@link FModDSP#disconnectAll}
	 * <p>
	 * 
	 * @param dSP
	 * @param target <span>(<i>Opt</i>) <dd>Input unit to disconnect, if not specified all inputs and outputs are disconnected from this unit. (<a href="https://www.fmod.com/docs/2.03/api/core-api-dsp.html">DSP</a>)
	 * @param connection <span>(<i>Opt</i>) <dd>When there is more than one connection between two units this can be used to define which of the connections should be disconnected. (<a href="https://www.fmod.com/docs/2.03/api/core-api-dspconnection.html">DSPConnection</a>)
	 */
	public static int disconnectFrom(long dSP, long target, long connection)
	{
		return FMOD.FMOD_DSP_DisconnectFrom(dSP, target, connection);
	}
	
	/**
	 * Retrieves the processing active state.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_GetActive(
	 *   FMOD_DSP *dsp,
	 *   FMOD_BOOL *active
	 * );
	 * </code></pre>
	 * <p>
	 * If 
	 * <code>active</code> is False, processing of this unit and its inputs are stopped.
	 * <p>
	 * When created a DSP is inactive.   If 
	 * {@link FModChannel#addDSP} is used it will automatically be activated, otherwise it must be set to active manually.
	 * <p>
	 * <strong>See Also:</strong> {@link FModDSP#setActive}, {@link FModDSP#setBypass}
	 * <p>
	 * 
	 * @param dSP
	 * @param active <span>(<i>Out</i>) <dd><p>Active state.</p><ul><li><span class="label">Units:</span> Boolean</li><li><span class="label">Default:</span> False</li></ul>
	 */
	public static int getActive(long dSP, java.nio.IntBuffer active)
	{
		return FMOD.FMOD_DSP_GetActive(dSP, active);
	}
	
	/**
	 * Retrieves the processing bypass state.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_GetBypass(
	 *   FMOD_DSP *dsp,
	 *   FMOD_BOOL *bypass
	 * );
	 * </code></pre>
	 * <p>
	 * If 
	 * <code>bypass</code> is true, processing of this unit is skipped but it continues to process its inputs.
	 * <p>
	 * <strong>See Also:</strong> {@link FModDSP#setBypass}, {@link FModDSP#setActive}
	 * <p>
	 * 
	 * @param dSP
	 * @param bypass <span>(<i>Out</i>) <dd><p>Bypass state.</p><ul><li><span class="label">Units:</span> Boolean</li><li><span class="label">Default:</span> False</li></ul>
	 */
	public static int getBypass(long dSP, java.nio.IntBuffer bypass)
	{
		return FMOD.FMOD_DSP_GetBypass(dSP, bypass);
	}
	
	/**
	 * Retrieves statistics on the mixer thread CPU usage for this unit.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_GetCPUUsage(
	 *   FMOD_DSP *dsp,
	 *   unsigned int *exclusive,
	 *   unsigned int *inclusive
	 * );
	 * </code></pre>
	 * <p>
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_init_profile_enable">FMOD_INIT_PROFILE_ENABLE</a> with {@link FModSystem#init} is required to call this function.
	 * <p>
	 * 
	 * @param dSP
	 * @param exclusive <span>(<i>Out, Opt</i>) <dd><p>CPU time spent processing just this unit during the last mixer update.</p><ul><li><span class="label">Units:</span> Microseconds</li></ul>
	 * @param inclusive <span>(<i>Out, Opt</i>) <dd><p>CPU time spent processing this unit and all of its input during the last mixer update.</p><ul><li><span class="label">Units:</span> Microseconds</li></ul>
	 */
	public static int getCPUUsage(long dSP, java.nio.IntBuffer exclusive, java.nio.IntBuffer inclusive)
	{
		return FMOD.FMOD_DSP_GetCPUUsage(dSP, exclusive, inclusive);
	}
	
	/**
	 * Retrieves the PCM input format this DSP will receive when processing.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_GetChannelFormat(
	 *   FMOD_DSP *dsp,
	 *   FMOD_CHANNELMASK *channelmask,
	 *   int *numchannels,
	 *   FMOD_SPEAKERMODE *source_speakermode
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModDSP#setChannelFormat}
	 * <p>
	 * 
	 * @param dSP
	 * @param channelmask <span>(<i>Out, Opt</i>) <dd>Deprecated. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_channelmask">FMOD_CHANNELMASK</a>)
	 * @param numchannels <span>(<i>Out, Opt</i>) <dd>Number of channels to be processed.
	 * @param source_speakermode <span>(<i>Out, Opt</i>) <dd>Speaker mode to describe the channel mapping. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_speakermode">FMOD_SPEAKERMODE</a>)
	 */
	public static int getChannelFormat(long dSP, java.nio.IntBuffer channelmask, java.nio.IntBuffer numchannels, java.nio.IntBuffer source_speakermode)
	{
		return FMOD.FMOD_DSP_GetChannelFormat(dSP, channelmask, numchannels, source_speakermode);
	}
	
	/**
	 * Retrieve the index of the first data parameter of a particular data type.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_GetDataParameterIndex(
	 *   FMOD_DSP *dsp,
	 *   int datatype,
	 *   int *index
	 * );
	 * </code></pre>
	 * <p>
	 * This function returns 
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_ok">FMOD_OK</a> if a parmeter of matching type is found and <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_err_invalid_param">FMOD_ERR_INVALID_PARAM</a> if no matches were found.
	 * <p>
	 * The return code can be used to check whether the DSP supports specific functionality through data parameters of certain types without the need to provide 
	 * <code>index</code>.
	 * <p>
	 * 
	 * @param dSP
	 * @param datatype <dd>The type of data to find. Typically of type <a href="https://www.fmod.com/docs/2.03/api/plugin-api-dsp.html#fmod_dsp_parameter_data_type">FMOD_DSP_PARAMETER_DATA_TYPE</a>.
	 * @param index <span>(<i>Out, Opt</i>) <dd>Contains the index of the first data parameter of type <code>datatype</code> after the function is called. Will be -1 if no matches were found.
	 */
	public static int getDataParameterIndex(long dSP, int datatype, java.nio.IntBuffer index)
	{
		return FMOD.FMOD_DSP_GetDataParameterIndex(dSP, datatype, index);
	}
	
	/**
	 * Retrieves the idle state.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_GetIdle(
	 *   FMOD_DSP *dsp,
	 *   FMOD_BOOL *idle
	 * );
	 * </code></pre>
	 * <p>
	 * A DSP is considered idle when it stops receiving input signal and all internal processing of stored input has been exhausted.
	 * <p>
	 * Each DSP type has the potential to have differing idle behaviour based on the type of effect.  A reverb or echo may take a longer time to go idle after it stops receiving a valid signal, compared to an effect with a shorter tail length like an EQ filter.
	 * <p>
	 * 
	 * @param dSP
	 * @param idle <span>(<i>Out</i>) <dd><p>Idle state.</p><ul><li><span class="label">Units:</span> Boolean</li></ul>
	 */
	public static int getIdle(long dSP, java.nio.IntBuffer idle)
	{
		return FMOD.FMOD_DSP_GetIdle(dSP, idle);
	}
	
	/**
	 * Retrieves information about this DSP unit.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_GetInfo(
	 *   FMOD_DSP *dsp,
	 *   char *name,
	 *   unsigned int *version,
	 *   int *channels,
	 *   int *configwidth,
	 *   int *configheight
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModDSP#showConfigDialog}
	 * <p>
	 * 
	 * @param dSP
	 * @param name <span>(<i>Out, Opt</i>) <dd>The name of this unit will be written (null terminated) to the provided 32 byte buffer. (<a href="https://www.fmod.com/docs/2.03/api/glossary.html#string-format">UTF-8 string</a>)
	 * @param version <span>(<i>Out, Opt</i>) <dd>Version number of this unit, usually formated as hex AAAABBBB where the AAAA is the major version number and the BBBB is the minor version number.
	 * @param channels <span>(<i>Out, Opt</i>) <dd>Number of channels this unit processes where 0 represents "any".
	 * @param configwidth <span>(<i>Out, Opt</i>) <dd>Configuration dialog box width where 0 represents "no dialog box".
	 * @param configheight <span>(<i>Out, Opt</i>) <dd>Configuration dialog box height where 0 represents "no dialog box".
	 */
	public static int getInfo(long dSP, java.nio.ByteBuffer name, java.nio.IntBuffer version, java.nio.IntBuffer channels, java.nio.IntBuffer configwidth, java.nio.IntBuffer configheight)
	{
		return FMOD.FMOD_DSP_GetInfo(dSP, name, version, channels, configwidth, configheight);
	}
	
	/**
	 * Retrieves the DSP unit at the specified index in the input list.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_GetInput(
	 *   FMOD_DSP *dsp,
	 *   int index,
	 *   FMOD_DSP **input,
	 *   FMOD_DSPCONNECTION **inputconnection
	 * );
	 * </code></pre>
	 * <p>
	 * This will flush the DSP queue (which blocks against the mixer) to ensure the input list is correct, avoid this during time sensitive operations.
	 * <p>
	 * The returned 
	 * <code>connection</code> will remain valid until the units are disconnected.
	 * <p>
	 * <strong>See Also:</strong> {@link FModDSP#addInput}, {@link FModDSP#getOutput}
	 * <p>
	 * 
	 * @param dSP
	 * @param index <dd><p>Offset into this DSP's input list.</p><ul><li><span class="label">Range:</span> [0, {@link FModDSP#getNumInputs})</li></ul>
	 * @param input <span>(<i>Out, Opt</i>) <dd>DSP unit at the specified <code>index</code>. (<a href="https://www.fmod.com/docs/2.03/api/core-api-dsp.html">DSP</a>)
	 * @param inputconnection <span>(<i>Out, Opt</i>) <dd>Connection between this unit and <code>input</code>. (<a href="https://www.fmod.com/docs/2.03/api/core-api-dspconnection.html">DSPConnection</a>)
	 */
	public static int getInput(long dSP, int index, org.lwjgl.PointerBuffer input, org.lwjgl.PointerBuffer inputconnection)
	{
		return FMOD.FMOD_DSP_GetInput(dSP, index, input, inputconnection);
	}
	
	/**
	 * Retrieves the input and output signal metering enabled states.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_GetMeteringEnabled(
	 *   FMOD_DSP *dsp,
	 *   FMOD_BOOL *inputEnabled,
	 *   FMOD_BOOL *outputEnabled
	 * );
	 * </code></pre>
	 * <p>
	 * Input metering is pre processing, while output metering is post processing.
	 * <p>
	 * Enabled metering allows 
	 * {@link FModDSP#getMeteringInfo} to return metering information and allows FMOD profiling tools to visualize the levels.
	 * <p>
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_init_profile_meter_all">FMOD_INIT_PROFILE_METER_ALL</a> with {@link FModSystem#init} will automatically turn on metering for all DSP units inside the mixer graph.
	 * <p>
	 * <strong>See Also:</strong> {@link FModDSP#setMeteringEnabled}
	 * <p>
	 * 
	 * @param dSP
	 * @param inputEnabled <span>(<i>Out, Opt</i>) <dd><p>Metering enabled state for the input signal.</p><ul><li><span class="label">Units:</span> Boolean</li><li><span class="label">Default:</span> False</li></ul>
	 * @param outputEnabled <span>(<i>Out, Opt</i>) <dd><p>Metering enabled state for the output signal.</p><ul><li><span class="label">Units:</span> Boolean</li><li><span class="label">Default:</span> False</li></ul>
	 */
	public static int getMeteringEnabled(long dSP, java.nio.IntBuffer inputEnabled, java.nio.IntBuffer outputEnabled)
	{
		return FMOD.FMOD_DSP_GetMeteringEnabled(dSP, inputEnabled, outputEnabled);
	}
	
	/**
	 * Retrieve the signal metering information.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_GetMeteringInfo(
	 *   FMOD_DSP *dsp,
	 *   FMOD_DSP_METERING_INFO *inputInfo,
	 *   FMOD_DSP_METERING_INFO *outputInfo
	 * );
	 * </code></pre>
	 * <p>
	 * Requesting metering information when it hasn't been enabled will result in 
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_err_badcommand">FMOD_ERR_BADCOMMAND</a>.
	 * <p>
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_init_profile_meter_all">FMOD_INIT_PROFILE_METER_ALL</a> with {@link FModSystem#init} will automatically enable metering for all DSP units.
	 * <p>
	 * <strong>See Also:</strong> {@link FModDSP#setMeteringEnabled}, {@link FModDSP#getMeteringEnabled}
	 * <p>
	 * 
	 * @param dSP
	 * @param inputInfo <span>(<i>Out, Opt</i>) <dd>Input metering information before the DSP has processed. (<a href="https://www.fmod.com/docs/2.03/api/plugin-api-dsp.html#fmod_dsp_metering_info">FMOD_DSP_METERING_INFO</a>)
	 * @param outputInfo <span>(<i>Out, Opt</i>) <dd>Output metering information after the DSP has processed. (<a href="https://www.fmod.com/docs/2.03/api/plugin-api-dsp.html#fmod_dsp_metering_info">FMOD_DSP_METERING_INFO</a>)
	 */
	public static int getMeteringInfo(long dSP, org.lwjgl.fmod.FMOD_DSP_METERING_INFO inputInfo, org.lwjgl.fmod.FMOD_DSP_METERING_INFO outputInfo)
	{
		return FMOD.FMOD_DSP_GetMeteringInfo(dSP, inputInfo, outputInfo);
	}
	
	/**
	 * Retrieves the number of DSP units in the input list.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_GetNumInputs(
	 *   FMOD_DSP *dsp,
	 *   int *numinputs
	 * );
	 * </code></pre>
	 * <p>
	 * This will flush the DSP queue (which blocks against the mixer) to ensure the input list is correct, avoid this during time sensitive operations.
	 * <p>
	 * <strong>See Also:</strong> {@link FModDSP#getNumOutputs}, {@link FModDSP#getInput}
	 * <p>
	 * 
	 * @param dSP
	 * @param numinputs <span>(<i>Out</i>) <dd>Number of input DSPs.
	 */
	public static int getNumInputs(long dSP, java.nio.IntBuffer numinputs)
	{
		return FMOD.FMOD_DSP_GetNumInputs(dSP, numinputs);
	}
	
	/**
	 * Retrieves the number of DSP units in the output list.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_GetNumOutputs(
	 *   FMOD_DSP *dsp,
	 *   int *numoutputs
	 * );
	 * </code></pre>
	 * <p>
	 * This will flush the DSP queue (which blocks against the mixer) to ensure the output list is correct, avoid this during time sensitive operations.
	 * <p>
	 * <strong>See Also:</strong> {@link FModDSP#getNumInputs}, {@link FModDSP#getOutput}
	 * <p>
	 * 
	 * @param dSP
	 * @param numoutputs <span>(<i>Out</i>) <dd>Number of output DSPs.
	 */
	public static int getNumOutputs(long dSP, java.nio.IntBuffer numoutputs)
	{
		return FMOD.FMOD_DSP_GetNumOutputs(dSP, numoutputs);
	}
	
	/**
	 * Retrieves the number of parameters exposed by this unit.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_GetNumParameters(
	 *   FMOD_DSP *dsp,
	 *   int *numparams
	 * );
	 * </code></pre>
	 * <p>
	 * Use this to enumerate all parameters of a DSP unit with 
	 * {@link FModDSP#getParameterInfo}.
	 * <p>
	 * <strong>See Also:</strong> {@link FModDSP#setParameterFloat}, {@link FModDSP#setParameterInt}, {@link FModDSP#setParameterBool}, {@link FModDSP#setParameterData}
	 * <p>
	 * 
	 * @param dSP
	 * @param numparams <span>(<i>Out</i>) <dd>Number of parameters.
	 */
	public static int getNumParameters(long dSP, java.nio.IntBuffer numparams)
	{
		return FMOD.FMOD_DSP_GetNumParameters(dSP, numparams);
	}
	
	/**
	 * Retrieves the DSP unit at the specified index in the output list.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_GetOutput(
	 *   FMOD_DSP *dsp,
	 *   int index,
	 *   FMOD_DSP **output,
	 *   FMOD_DSPCONNECTION **outputconnection
	 * );
	 * </code></pre>
	 * <p>
	 * This will flush the DSP queue (which blocks against the mixer) to ensure the output list is correct, avoid this during time sensitive operations.
	 * <p>
	 * The returned 
	 * <code>connection</code> will remain valid until the units are disconnected.
	 * <p>
	 * <strong>See Also:</strong> {@link FModDSP#addInput}, {@link FModDSP#getInput}
	 * <p>
	 * 
	 * @param dSP
	 * @param index <dd><p>Offset into this DSP's output list.</p><ul><li><span class="label">Range:</span> [0, {@link FModDSP#getNumOutputs})</li></ul>
	 * @param output <span>(<i>Out, Opt</i>) <dd>DSP unit at the specified <code>index</code>. (<a href="https://www.fmod.com/docs/2.03/api/core-api-dsp.html">DSP</a>)
	 * @param outputconnection <span>(<i>Out, Opt</i>) <dd>Connection between this unit and <code>output</code>. (<a href="https://www.fmod.com/docs/2.03/api/core-api-dspconnection.html">DSPConnection</a>)
	 */
	public static int getOutput(long dSP, int index, org.lwjgl.PointerBuffer output, org.lwjgl.PointerBuffer outputconnection)
	{
		return FMOD.FMOD_DSP_GetOutput(dSP, index, output, outputconnection);
	}
	
	/**
	 * Retrieves the output format this DSP will produce when processing based on the input specified.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_GetOutputChannelFormat(
	 *   FMOD_DSP *dsp,
	 *   FMOD_CHANNELMASK inmask,
	 *   int inchannels,
	 *   FMOD_SPEAKERMODE inspeakermode,
	 *   FMOD_CHANNELMASK *outmask,
	 *   int *outchannels,
	 *   FMOD_SPEAKERMODE *outspeakermode
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModDSP#setChannelFormat}, {@link FModDSP#getChannelFormat}
	 * <p>
	 * 
	 * @param dSP
	 * @param inmask <dd>Deprecated. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_channelmask">FMOD_CHANNELMASK</a>)
	 * @param inchannels <dd>Number of channels for the input signal.
	 * @param inspeakermode <dd>Speaker mode for the input signal. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_speakermode">FMOD_SPEAKERMODE</a>)
	 * @param outmask <span>(<i>Out, Opt</i>) <dd>Deprecated. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_channelmask">FMOD_CHANNELMASK</a>)
	 * @param outchannels <span>(<i>Out, Opt</i>) <dd>Number of channels for the output signal.
	 * @param outspeakermode <span>(<i>Out, Opt</i>) <dd>Speaker mode for the output signal. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_speakermode">FMOD_SPEAKERMODE</a>)
	 */
	public static int getOutputChannelFormat(long dSP, int inmask, int inchannels, int inspeakermode, java.nio.IntBuffer outmask, java.nio.IntBuffer outchannels, java.nio.IntBuffer outspeakermode)
	{
		return FMOD.FMOD_DSP_GetOutputChannelFormat(dSP, inmask, inchannels, inspeakermode, outmask, outchannels, outspeakermode);
	}
	
	/**
	 * Retrieves a boolean parameter by index.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_GetParameterBool(
	 *   FMOD_DSP *dsp,
	 *   int index,
	 *   FMOD_BOOL *value,
	 *   char *valuestr,
	 *   int valuestrlen
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModDSP#setParameterBool}, {@link FModDSP#getParameterInfo}
	 * <p>
	 * 
	 * @param dSP
	 * @param index <dd><p>Parameter index.</p><ul><li><span class="label">Range:</span> [0, {@link FModDSP#getNumParameters})</li></ul>
	 * @param value <span>(<i>Out, Opt</i>) <dd><p>Parameter boolean data.</p><ul><li><span class="label">Units:</span> Boolean</li></ul>
	 * @param valuestr <span>(<i>Out, Opt</i>) <dd>String representation <code>value</code>. (<a href="https://www.fmod.com/docs/2.03/api/glossary.html#string-format">UTF-8 string</a>)
	 */
	public static int getParameterBool(long dSP, int index, java.nio.IntBuffer value, java.nio.ByteBuffer valuestr)
	{
		return FMOD.FMOD_DSP_GetParameterBool(dSP, index, value, valuestr);
	}
	
	/**
	 * Retrieves a binary data parameter by index.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_GetParameterData(
	 *   FMOD_DSP *dsp,
	 *   int index,
	 *   void **data,
	 *   unsigned int *length,
	 *   char *valuestr,
	 *   int valuestrlen
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModDSP#setParameterData}, {@link FModDSP#getParameterInfo}
	 * <p>
	 * 
	 * @param dSP
	 * @param index <dd><p>Parameter index.</p><ul><li><span class="label">Range:</span> [0, {@link FModDSP#getNumParameters})</li></ul>
	 * @param data <span>(<i>Out, Opt</i>) <dd>Parameter binary data.
	 * @param length <span>(<i>Out, Opt</i>) <dd><p>Length of <code>data</code>.</p><ul><li><span class="label">Units:</span> Bytes</li></ul>
	 * @param valuestr <span>(<i>Out, Opt</i>) <dd>String representation <code>data</code>. (<a href="https://www.fmod.com/docs/2.03/api/glossary.html#string-format">UTF-8 string</a>)
	 */
	public static int getParameterData(long dSP, int index, org.lwjgl.PointerBuffer data, java.nio.IntBuffer length, java.nio.ByteBuffer valuestr)
	{
		return FMOD.FMOD_DSP_GetParameterData(dSP, index, data, length, valuestr);
	}
	
	/**
	 * Retrieves a floating point parameter by index.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_GetParameterFloat(
	 *   FMOD_DSP *dsp,
	 *   int index,
	 *   float *value,
	 *   char *valuestr,
	 *   int valuestrlen
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModDSP#setParameterFloat}, {@link FModDSP#getParameterInfo}
	 * <p>
	 * 
	 * @param dSP
	 * @param index <dd><p>Parameter index.</p><ul><li><span class="label">Range:</span> [0, {@link FModDSP#getNumParameters})</li></ul>
	 * @param value <span>(<i>Out, Opt</i>) <dd>Parameter floating point data.
	 * @param valuestr <span>(<i>Out, Opt</i>) <dd>String representation <code>value</code>. (<a href="https://www.fmod.com/docs/2.03/api/glossary.html#string-format">UTF-8 string</a>)
	 */
	public static int getParameterFloat(long dSP, int index, java.nio.FloatBuffer value, java.nio.ByteBuffer valuestr)
	{
		return FMOD.FMOD_DSP_GetParameterFloat(dSP, index, value, valuestr);
	}
	
	/**
	 * Retrieve information about a specified parameter.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_GetParameterInfo(
	 *   FMOD_DSP *dsp,
	 *   int index,
	 *   FMOD_DSP_PARAMETER_DESC **desc
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModDSP#setParameterFloat}, {@link FModDSP#setParameterInt}, {@link FModDSP#setParameterBool}, {@link FModDSP#setParameterData}
	 * <p>
	 * 
	 * @param dSP
	 * @param index <dd><p>Parameter index.</p><ul><li><span class="label">Range:</span> [0, {@link FModDSP#getNumParameters})</li></ul>
	 * @param desc <span>(<i>Out</i>) <dd>Parameter description at the specified <code>index</code>. (<a href="https://www.fmod.com/docs/2.03/api/plugin-api-dsp.html#fmod_dsp_parameter_desc">FMOD_DSP_PARAMETER_DESC</a>)
	 */
	public static int getParameterInfo(long dSP, int index, org.lwjgl.PointerBuffer desc)
	{
		return FMOD.FMOD_DSP_GetParameterInfo(dSP, index, desc);
	}
	
	/**
	 * Retrieves an integer parameter by index.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_GetParameterInt(
	 *   FMOD_DSP *dsp,
	 *   int index,
	 *   int *value,
	 *   char *valuestr,
	 *   int valuestrlen
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModDSP#setParameterInt}, {@link FModDSP#getParameterInfo}
	 * <p>
	 * 
	 * @param dSP
	 * @param index <dd><p>Parameter index.</p><ul><li><span class="label">Range:</span> [0, {@link FModDSP#getNumParameters})</li></ul>
	 * @param value <span>(<i>Out, Opt</i>) <dd>Parameter integer data.
	 * @param valuestr <span>(<i>Out, Opt</i>) <dd>String representation <code>value</code>. (<a href="https://www.fmod.com/docs/2.03/api/glossary.html#string-format">UTF-8 string</a>)
	 */
	public static int getParameterInt(long dSP, int index, java.nio.IntBuffer value, java.nio.ByteBuffer valuestr)
	{
		return FMOD.FMOD_DSP_GetParameterInt(dSP, index, value, valuestr);
	}
	
	/**
	 * Retrieves the parent System object.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_GetSystemObject(
	 *   FMOD_DSP *dsp,
	 *   FMOD_SYSTEM **system
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#createDSP}, {@link FModSystem#createDSPByType}
	 * <p>
	 * 
	 * @param dSP
	 * @param system <span>(<i>Out</i>) <dd>System object. (<a href="https://www.fmod.com/docs/2.03/api/core-api-system.html">System</a>)
	 */
	public static int getSystemObject(long dSP, org.lwjgl.PointerBuffer system)
	{
		return FMOD.FMOD_DSP_GetSystemObject(dSP, system);
	}
	
	/**
	 * Retrieves the pre-defined type of a FMOD registered DSP unit.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_GetType(
	 *   FMOD_DSP *dsp,
	 *   FMOD_DSP_TYPE *type
	 * );
	 * </code></pre>
	 * <p>
	 * This is only valid for built in FMOD effects.  Any user plugins will simply return 
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common-dsp-effects.html#fmod_dsp_type_unknown">FMOD_DSP_TYPE_UNKNOWN</a>.
	 * <p>
	 * 
	 * @param dSP
	 * @param type <span>(<i>Out</i>) <dd>DSP type. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common-dsp-effects.html#fmod_dsp_type">FMOD_DSP_TYPE</a>)
	 */
	public static int getType(long dSP, java.nio.IntBuffer type)
	{
		return FMOD.FMOD_DSP_GetType(dSP, type);
	}
	
	/**
	 * Retrieves a user value associated with this object.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_GetUserData(
	 *   FMOD_DSP *dsp,
	 *   void **userdata
	 * );
	 * </code></pre>
	 * <p>
	 * This function allows arbitrary user data to be retrieved from this object. See the 
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#user-data">User Data</a> section of the glossary for an example of how to get and set user data.
	 * <p>
	 * 
	 * @param dSP
	 * @param userdata <span>(<i>Out</i>) <dd>User data set by calling {@link FModDSP#setUserData}.
	 */
	public static int getUserData(long dSP, org.lwjgl.PointerBuffer userdata)
	{
		return FMOD.FMOD_DSP_GetUserData(dSP, userdata);
	}
	
	/**
	 * Retrieves the scale of the wet and dry signal components.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_GetWetDryMix(
	 *   FMOD_DSP *dsp,
	 *   float *prewet,
	 *   float *postwet,
	 *   float *dry
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModDSP#setWetDryMix}
	 * <p>
	 * 
	 * @param dSP
	 * @param prewet <span>(<i>Out, Opt</i>) <dd><p>Level of the 'Dry' (pre-processed signal) mix that is processed by the DSP. 0 = silent, 1 = full. Negative level inverts the signal.  Values larger than 1 amplify the signal.</p><ul><li><span class="label">Units:</span> Linear</li><li><span class="label">Range:</span> (-inf, inf)</li><li><span class="label">Default:</span> 1</li></ul>
	 * @param postwet <span>(<i>Out, Opt</i>) <dd><p>Level of the 'Wet' (post-processed signal) mix that is output. 0 = silent, 1 = full. Negative level inverts the signal.  Values larger than 1 amplify the signal.</p><ul><li><span class="label">Units:</span> Linear</li><li><span class="label">Range:</span> (-inf, inf)</li><li><span class="label">Default:</span> 1</li></ul>
	 * @param dry <span>(<i>Out, Opt</i>) <dd><p>Level of the 'Dry' (pre-processed signal) mix that is output. 0 = silent, 1 = full. Negative level inverts the signal.  Values larger than 1 amplify the signal.</p><ul><li><span class="label">Units:</span> Linear</li><li><span class="label">Range:</span> (-inf, inf)</li><li><span class="label">Default:</span> 1</li></ul>
	 */
	public static int getWetDryMix(long dSP, java.nio.FloatBuffer prewet, java.nio.FloatBuffer postwet, java.nio.FloatBuffer dry)
	{
		return FMOD.FMOD_DSP_GetWetDryMix(dSP, prewet, postwet, dry);
	}
	
	/**
	 * Frees a DSP object.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_Release(
	 * FMOD_DSP *dsp
	 * );
	 * </code></pre>
	 * <p>
	 * If DSP is not removed from the network with 
	 * {@link FModChannel#removeDSP} after being added with {@link FModChannel#addDSP}, it will not release and will instead return <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_err_dsp_inuse">FMOD_ERR_DSP_INUSE</a>.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#createDSP}
	 * <p>
	 * 
	 * @param dSP
	 */
	public static int release(long dSP)
	{
		return FMOD.FMOD_DSP_Release(dSP);
	}
	
	/**
	 * Reset a DSPs internal state ready for new input signal.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_Reset(
	 * FMOD_DSP *dsp
	 * );
	 * </code></pre>
	 * <p>
	 * This will clear all internal state derived from input signal while retaining any set parameter values.  The intended use of the function is to avoid audible artifacts if moving the DSP from one part of the DSP network to another.
	 * <p>
	 * 
	 * @param dSP
	 */
	public static int reset(long dSP)
	{
		return FMOD.FMOD_DSP_Reset(dSP);
	}
	
	/**
	 * Sets the processing active state.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_SetActive(
	 *   FMOD_DSP *dsp,
	 *   FMOD_BOOL active
	 * );
	 * </code></pre>
	 * <p>
	 * If 
	 * <code>active</code> is false, processing of this unit and its inputs are stopped.
	 * <p>
	 * When created a DSP is inactive.   If 
	 * {@link FModChannel#addDSP} is used it will automatically be activated, otherwise it must be set to active manually.
	 * <p>
	 * <strong>See Also:</strong> {@link FModDSP#getActive}, {@link FModDSP#setBypass}, {@link FModDSP#getBypass}
	 * <p>
	 * 
	 * @param dSP
	 * @param active <dd><p>Active state.</p><ul><li><span class="label">Units:</span> Boolean</li><li><span class="label">Default:</span> False</li></ul>
	 */
	public static int setActive(long dSP, int active)
	{
		return FMOD.FMOD_DSP_SetActive(dSP, active);
	}
	
	/**
	 * Sets the processing bypass state.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_SetBypass(
	 *   FMOD_DSP *dsp,
	 *   FMOD_BOOL bypass
	 * );
	 * </code></pre>
	 * <p>
	 * If 
	 * <code>bypass</code> is true, processing of this unit is skipped but it continues to process its inputs.
	 * <p>
	 * <strong>See Also:</strong> {@link FModDSP#getBypass}, {@link FModDSP#setActive}, {@link FModDSP#getActive}
	 * <p>
	 * 
	 * @param dSP
	 * @param bypass <dd><p>Bypass state.</p><ul><li><span class="label">Units:</span> Boolean</li><li><span class="label">Default:</span> False</li></ul>
	 */
	public static int setBypass(long dSP, int bypass)
	{
		return FMOD.FMOD_DSP_SetBypass(dSP, bypass);
	}
	
	/**
	 * Sets the callback for DSP notifications.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_SetCallback(
	 *   FMOD_DSP *dsp,
	 *   FMOD_DSP_CALLBACK callback
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> <a href="https://www.fmod.com/docs/2.03/api/glossary.html#callback-behavior">Callback Behavior</a>, <a href="https://www.fmod.com/docs/2.03/api/core-api-dsp.html#fmod_dsp_callback_type">FMOD_DSP_CALLBACK_TYPE</a>
	 * <p>
	 * 
	 * @param dSP
	 * @param callback <dd>Callback to invoke. (<a href="https://www.fmod.com/docs/2.03/api/core-api-dsp.html#fmod_dsp_callback">FMOD_DSP_CALLBACK</a>)
	 */
	public static int setCallback(long dSP, org.lwjgl.fmod.FMOD_DSP_CALLBACKI callback)
	{
		return FMOD.FMOD_DSP_SetCallback(dSP, callback);
	}
	
	/**
	 * Sets the PCM input format this DSP will receive when processing.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_SetChannelFormat(
	 *   FMOD_DSP *dsp,
	 *   FMOD_CHANNELMASK channelmask,
	 *   int numchannels,
	 *   FMOD_SPEAKERMODE source_speakermode
	 * );
	 * </code></pre>
	 * <p>
	 * Setting the number of channels on a unit will force either a down or up mix to that channel count before processing the DSP read/process callback.
	 * <p>
	 * <strong>See Also:</strong> {@link FModDSP#getChannelFormat}
	 * <p>
	 * 
	 * @param dSP
	 * @param channelmask <dd>Deprecated. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_channelmask">FMOD_CHANNELMASK</a>)
	 * @param numchannels <dd><p>Number of channels to be processed.</p><ul><li><span class="label">Range:</span> [0, <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_max_channel_width">FMOD_MAX_CHANNEL_WIDTH</a>]</li></ul>
	 * @param source_speakermode <dd>Speaker mode to describe the channel mapping. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_speakermode">FMOD_SPEAKERMODE</a>)
	 */
	public static int setChannelFormat(long dSP, int channelmask, int numchannels, int source_speakermode)
	{
		return FMOD.FMOD_DSP_SetChannelFormat(dSP, channelmask, numchannels, source_speakermode);
	}
	
	/**
	 * Sets the input and output signal metering enabled states.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_SetMeteringEnabled(
	 *   FMOD_DSP *dsp,
	 *   FMOD_BOOL inputEnabled,
	 *   FMOD_BOOL outputEnabled
	 * );
	 * </code></pre>
	 * <p>
	 * Input metering is pre processing, while output metering is post processing.
	 * <p>
	 * Enabled metering allows 
	 * {@link FModDSP#getMeteringInfo} to return metering information and allows FMOD profiling tools to visualize the levels.
	 * <p>
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_init_profile_meter_all">FMOD_INIT_PROFILE_METER_ALL</a> with {@link FModSystem#init} will automatically turn on metering for all DSP units inside the mixer graph.
	 * <p>
	 * This function must have 
	 * <code>inputEnabled</code> and <code>outputEnabled</code> set to <code>true</code> if being used by the FMOD Studio API, such as in the Unity or Unreal Engine integrations, in order to avoid conflict with FMOD Studio's live update feature.
	 * <p>
	 * <strong>See Also:</strong> {@link FModDSP#getMeteringEnabled}
	 * <p>
	 * 
	 * @param dSP
	 * @param inputEnabled <dd><p>Metering enabled state for the input signal.</p><ul><li><span class="label">Units:</span> Boolean</li><li><span class="label">Default:</span> False</li></ul>
	 * @param outputEnabled <dd><p>Metering enabled state for the output signal.</p><ul><li><span class="label">Units:</span> Boolean</li><li><span class="label">Default:</span> False</li></ul>
	 */
	public static int setMeteringEnabled(long dSP, int inputEnabled, int outputEnabled)
	{
		return FMOD.FMOD_DSP_SetMeteringEnabled(dSP, inputEnabled, outputEnabled);
	}
	
	/**
	 * Sets a boolean parameter by index.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_SetParameterBool(
	 *   FMOD_DSP *dsp,
	 *   int index,
	 *   FMOD_BOOL value
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModDSP#getParameterInfo}, {@link FModDSP#getParameterBool}
	 * <p>
	 * 
	 * @param dSP
	 * @param index <dd><p>Parameter index.</p><ul><li><span class="label">Range:</span> [0, {@link FModDSP#getNumParameters})</li></ul>
	 * @param value <dd><p>Parameter value.</p><ul><li><span class="label">Units:</span> Boolean</li></ul>
	 */
	public static int setParameterBool(long dSP, int index, int value)
	{
		return FMOD.FMOD_DSP_SetParameterBool(dSP, index, value);
	}
	
	/**
	 * Sets a binary data parameter by index.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_SetParameterData(
	 *   FMOD_DSP *dsp,
	 *   int index,
	 *   void *data,
	 *   unsigned int length
	 * );
	 * </code></pre>
	 * <p>
	 * Certain data types are predefined by the system and can be specified via the 
	 * <a href="https://www.fmod.com/docs/2.03/api/plugin-api-dsp.html#fmod_dsp_parameter_desc_data">FMOD_DSP_PARAMETER_DESC_DATA</a>, see <a href="https://www.fmod.com/docs/2.03/api/plugin-api-dsp.html#fmod_dsp_parameter_data_type">FMOD_DSP_PARAMETER_DATA_TYPE</a>
	 * <p>
	 * <strong>See Also:</strong> {@link FModDSP#getParameterInfo}, {@link FModDSP#getParameterData}
	 * <p>
	 * 
	 * @param dSP
	 * @param index <dd><p>Parameter index.</p><ul><li><span class="label">Range:</span> [0, {@link FModDSP#getNumParameters})</li></ul>
	 * @param data <dd>Parameter binary data.
	 */
	public static int setParameterData(long dSP, int index, java.nio.ByteBuffer data)
	{
		return FMOD.FMOD_DSP_SetParameterData(dSP, index, data);
	}
	
	/**
	 * Sets a floating point parameter by index.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_SetParameterFloat(
	 *   FMOD_DSP *dsp,
	 *   int index,
	 *   float value
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModDSP#getParameterInfo}, {@link FModDSP#getParameterFloat}
	 * <p>
	 * 
	 * @param dSP
	 * @param index <dd><p>Parameter index.</p><ul><li><span class="label">Range:</span> [0, {@link FModDSP#getNumParameters})</li></ul>
	 * @param value <dd>Parameter floating point data.
	 */
	public static int setParameterFloat(long dSP, int index, float value)
	{
		return FMOD.FMOD_DSP_SetParameterFloat(dSP, index, value);
	}
	
	/**
	 * Sets an integer parameter by index.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_SetParameterInt(
	 *   FMOD_DSP *dsp,
	 *   int index,
	 *   int value
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModDSP#getParameterInfo}, {@link FModDSP#getParameterInt}
	 * <p>
	 * 
	 * @param dSP
	 * @param index <dd><p>Parameter index.</p><ul><li><span class="label">Range:</span> [0, {@link FModDSP#getNumParameters})</li></ul>
	 * @param value <dd>Parameter integer data.
	 */
	public static int setParameterInt(long dSP, int index, int value)
	{
		return FMOD.FMOD_DSP_SetParameterInt(dSP, index, value);
	}
	
	/**
	 * Sets a user value associated with this object.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_SetUserData(
	 *   FMOD_DSP *dsp,
	 *   void *userdata
	 * );
	 * </code></pre>
	 * <p>
	 * This function allows arbitrary user data to be attached to this object. See the 
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#user-data">User Data</a> section of the glossary for an example of how to get and set user data.
	 * <p>
	 * <strong>See Also:</strong> {@link FModDSP#getUserData}
	 * <p>
	 * 
	 * @param dSP
	 * @param userdata <dd>Value stored on this object.
	 */
	public static int setUserData(long dSP, long userdata)
	{
		return FMOD.FMOD_DSP_SetUserData(dSP, userdata);
	}
	
	/**
	 * Sets the scale of the wet and dry signal components.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_SetWetDryMix(
	 *   FMOD_DSP *dsp,
	 *   float prewet,
	 *   float postwet,
	 *   float dry
	 * );
	 * </code></pre>
	 * <p>
	 * The dry signal path is silent by default, because dsp effects transform the input and pass the newly processed result to the output.
	 * <p>
	 * <strong>See Also:</strong> {@link FModDSP#getWetDryMix}
	 * <p>
	 * 
	 * @param dSP
	 * @param prewet <dd><p>Level of the 'Dry' (pre-processed signal) mix that is processed by the DSP. 0 = silent, 1 = full. Negative level inverts the signal.  Values larger than 1 amplify the signal.</p><ul><li><span class="label">Units:</span> Linear</li><li><span class="label">Range:</span> (-inf, inf)</li><li><span class="label">Default:</span> 1</li></ul>
	 * @param postwet <dd><p>Level of the 'Wet' (post-processed signal) mix that is output. 0 = silent, 1 = full. Negative level inverts the signal.  Values larger than 1 amplify the signal.</p><ul><li><span class="label">Units:</span> Linear</li><li><span class="label">Range:</span> (-inf, inf)</li><li><span class="label">Default:</span> 1</li></ul>
	 * @param dry <dd><p>Level of the 'Dry' (pre-processed signal) mix that is output. 0 = silent, 1 = full. Negative level inverts the signal.  Values larger than 1 amplify the signal.</p><ul><li><span class="label">Units:</span> Linear</li><li><span class="label">Range:</span> (-inf, inf)</li><li><span class="label">Default:</span> 0</li></ul>
	 */
	public static int setWetDryMix(long dSP, float prewet, float postwet, float dry)
	{
		return FMOD.FMOD_DSP_SetWetDryMix(dSP, prewet, postwet, dry);
	}
	
	/**
	 * Display or hide a DSP unit configuration dialog box inside the target window.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_DSP_ShowConfigDialog(
	 *   FMOD_DSP *dsp,
	 *   void *hwnd,
	 *   FMOD_BOOL show
	 * );
	 * </code></pre>
	 * <p>
	 * Dialog boxes are used by DSP plugins that prefer to use a graphical user interface to modify their parameters rather than using the other method of enumerating the parameters and using 
	 * {@link FModDSP#setParameterFloat} / {@link FModDSP#setParameterInt} / {@link FModDSP#setParameterBool} / {@link FModDSP#setParameterData}.
	 * <p>
	 * FMOD Studio plugins do not have configuration dialog boxes. To find out what size window to create to store the configuration screen, use 
	 * {@link FModDSP#getInfo} where you can get the width and height.
	 * <p>
	 * @param dSP
	 * @param hwnd <dd>Target HWND in windows to display configuration dialog.
	 * @param show <dd><p>Whether to show or hide the dialog box inside target hwnd.</p><ul><li><span class="label">Units:</span> Boolean</li></ul>
	 */
	public static int showConfigDialog(long dSP, long hwnd, int show)
	{
		return FMOD.FMOD_DSP_ShowConfigDialog(dSP, hwnd, show);
	}
	
}
