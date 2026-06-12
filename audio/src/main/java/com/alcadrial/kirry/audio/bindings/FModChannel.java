package com.alcadrial.kirry.audio.bindings;

import org.lwjgl.fmod.FMOD;

/**
 * <div class="manual-content api"><strong>11. Core API Reference | Channel</strong><br>A source of <a href="https://www.fmod.com/docs/2.03/api/glossary.html#signal">audio signal</a> that connects to the <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a> mixing hierarchy.
 * <p>
 * Create with {@link FModSystem#playSound} or {@link FModSystem#playDSP}.
 * <p>
 * <strong>Playback control:</strong>
 * <p>
 * <ul>
 *     <li><span>{@link FModChannel#setFrequency} Sets the frequency or playback rate.</span></li>
 *     <li><span>{@link FModChannel#getFrequency} Retrieves the playback frequency or playback rate.</span></li>
 *     <li><span>{@link FModChannel#setPriority} Sets the priority used for virtual voice ordering.</span></li>
 *     <li><span>{@link FModChannel#getPriority} Retrieves the priority used for virtual voice ordering.</span></li>
 *     <li><span>{@link FModChannel#setPosition} Sets the current playback position.</span></li>
 *     <li><span>{@link FModChannel#getPosition} Retrieves the current playback position.</span></li>
 *     <li><span>{@link FModChannel#setChannelGroup} Sets the ChannelGroup this object outputs to.</span></li>
 *     <li><span>{@link FModChannel#getChannelGroup} Retrieves the ChannelGroup this object outputs to.</span></li>
 *     <li><span>{@link FModChannel#setLoopCount} Sets the number of times to loop before stopping.</span></li>
 *     <li><span>{@link FModChannel#getLoopCount} Retrieves the number of times to loop before stopping.</span></li>
 *     <li><span>{@link FModChannel#setLoopPoints} Sets the loop start and end points.</span></li>
 *     <li><span>{@link FModChannel#getLoopPoints} Retrieves the loop start and end points.</span></li>
 * </ul>
 * <strong>Information:</strong>
 * <p>
 * <ul>
 *     <li><span>{@link FModChannel#isVirtual} Retrieves whether the Channel is being emulated by the virtual voice system.</span></li>
 *     <li><span>{@link FModChannel#getCurrentSound} Retrieves the currently playing Sound.</span></li>
 *     <li><span>{@link FModChannel#getIndex} Retrieves the index of this object in the System Channel pool.</span></li>
 * </ul>
 * The following APIs are inherited from <a href="https://www.fmod.com/docs/2.03/api/core-api-channelcontrol.html">ChannelControl</a>:
 * <p>
 * <strong>Playback:</strong>
 * <p>
 * <ul>
 *     <li><span>{@link FModChannel#isPlaying} Retrieves the playing state.</span></li>
 *     <li><span>{@link FModChannel#stop} Stops the Channel (or all Channels in nested ChannelGroups) from playing.</span></li>
 *     <li><span>{@link FModChannel#setPaused} Sets the paused state.</span></li>
 *     <li><span>{@link FModChannel#getPaused} Retrieves the paused state.</span></li>
 *     <li><span>{@link FModChannel#setMode} Sets the playback mode that controls how this object behaves.</span></li>
 *     <li><span>{@link FModChannel#getMode} Retrieves the playback mode bits that control how this object behaves.</span></li>
 *     <li><span>{@link FModChannel#setPitch} Sets the relative pitch / playback rate.</span></li>
 *     <li><span>{@link FModChannel#getPitch} Retrieves the relative pitch / playback rate.</span></li>
 * </ul>
 * <strong>Volume levels:</strong>
 * <p>
 * <ul>
 *     <li><span>{@link FModChannel#getAudibility} Gets the calculated audibility based on all attenuation factors which contribute to the final output volume.</span></li>
 *     <li><span>{@link FModChannel#setVolume} Sets the volume level.</span></li>
 *     <li><span>{@link FModChannel#getVolume} Retrieves the volume level.</span></li>
 *     <li><span>{@link FModChannel#setVolumeRamp} Sets whether volume changes are ramped or instantaneous.</span></li>
 *     <li><span>{@link FModChannel#getVolumeRamp} Retrieves whether volume changes are ramped or instantaneous.</span></li>
 *     <li><span>{@link FModChannel#setMute} Sets the mute state.</span></li>
 *     <li><span>{@link FModChannel#getMute} Retrieves the mute state.</span></li>
 * </ul>
 * <strong>Spatialization:</strong>
 * <p>
 * <ul>
 *     <li><span>{@link FModChannel#set3DAttributes} Sets the 3D position and velocity used to apply panning, attenuation and doppler.</span></li>
 *     <li><span>{@link FModChannel#get3DAttributes} Retrieves the 3D position and velocity used to apply panning, attenuation and doppler.</span></li>
 *     <li><span>{@link FModChannel#set3DConeOrientation} Sets the orientation of a 3D cone shape, used for simulated occlusion.</span></li>
 *     <li><span>{@link FModChannel#get3DConeOrientation} Retrieves the orientation of a 3D cone shape, used for simulated occlusion.</span></li>
 *     <li><span>{@link FModChannel#set3DConeSettings} Sets the angles and attenuation levels of a 3D cone shape, for simulated occlusion which is based on direction.</span></li>
 *     <li><span>{@link FModChannel#get3DConeSettings} Retrieves the angles and attenuation levels of a 3D cone shape, for simulated occlusion which is based on direction.</span></li>
 *     <li><span>{@link FModChannel#set3DCustomRolloff} Sets a custom roll-off shape for 3D distance attenuation.</span></li>
 *     <li><span>{@link FModChannel#get3DCustomRolloff} Retrieves the current custom roll-off shape for 3D distance attenuation.</span></li>
 *     <li><span>{@link FModChannel#set3DDistanceFilter} Sets an override value for the 3D distance filter.</span></li>
 *     <li><span>{@link FModChannel#get3DDistanceFilter} Retrieves the override values for the 3D distance filter.</span></li>
 *     <li><span>{@link FModChannel#set3DDopplerLevel} Sets the amount by which doppler is scaled.</span></li>
 *     <li><span>{@link FModChannel#get3DDopplerLevel} Retrieves the amount by which doppler is scaled.</span></li>
 *     <li><span>{@link FModChannel#set3DLevel} Sets the blend between 3D panning and 2D panning.</span></li>
 *     <li><span>{@link FModChannel#get3DLevel} Retrieves the blend between 3D panning and 2D panning.</span></li>
 *     <li><span>{@link FModChannel#set3DMinMaxDistance} Sets the minimum and maximum distances used to calculate the 3D roll-off attenuation.</span></li>
 *     <li><span>{@link FModChannel#get3DMinMaxDistance} Retrieves the minimum and maximum distances used to calculate the 3D roll-off attenuation.</span></li>
 *     <li><span>{@link FModChannel#set3DOcclusion} Sets the 3D attenuation factors for the direct and reverb paths.</span></li>
 *     <li><span>{@link FModChannel#get3DOcclusion} Retrieves the 3D attenuation factors for the direct and reverb paths.</span></li>
 *     <li><span>{@link FModChannel#set3DSpread} Sets the spread of a 3D sound in speaker space.</span></li>
 *     <li><span>{@link FModChannel#get3DSpread} Retrieves the spread of a 3D sound in speaker space.</span></li>
 * </ul>
 * <strong>Panning and level adjustment:</strong>
 * <p>
 * <ul>
 *     <li><span>{@link FModChannel#setPan} Sets the left/right pan level.</span></li>
 *     <li><span>{@link FModChannel#setMixLevelsInput} Sets the incoming volume level for each channel of a multi-channel <a href="https://www.fmod.com/docs/2.03/api/glossary.html#signal">signal</a>.</span></li>
 *     <li><span>{@link FModChannel#setMixLevelsOutput} Sets the outgoing volume levels for each speaker.</span></li>
 *     <li><span>{@link FModChannel#setMixMatrix} Sets a two-dimensional pan matrix that maps the <a href="https://www.fmod.com/docs/2.03/api/glossary.html#signal">signal</a> from input channels (columns) to output speakers (rows).</span></li>
 *     <li><span>{@link FModChannel#getMixMatrix} Retrieves a 2 dimensional pan matrix that maps the <a href="https://www.fmod.com/docs/2.03/api/glossary.html#signal">signal</a> from input channels (columns) to output speakers (rows).</span></li>
 * </ul>
 * <strong>Filtering:</strong>
 * <p>
 * <ul>
 *     <li><span>{@link FModChannel#setReverbProperties} Sets the wet / send level for a particular reverb instance.</span></li>
 *     <li><span>{@link FModChannel#getReverbProperties} Retrieves the wet / send level for a particular reverb instance.</span></li>
 *     <li><span>{@link FModChannel#setLowPassGain} Sets the gain of the dry <a href="https://www.fmod.com/docs/2.03/api/glossary.html#signal">signal</a> when built in lowpass / distance filtering is applied.</span></li>
 *     <li><span>{@link FModChannel#getLowPassGain} Retrieves the gain of the dry <a href="https://www.fmod.com/docs/2.03/api/glossary.html#signal">signal</a> when built in lowpass / distance filtering is applied.</span></li>
 * </ul>
 * <strong>DSP chain configuration:</strong>
 * <p>
 * <ul>
 *     <li><span>{@link FModChannel#addDSP} Adds a <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP unit</a> to the specified index in the <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-chain">DSP chain</a>.</span></li>
 *     <li><span>{@link FModChannel#removeDSP} Removes the specified <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP unit</a> from the <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-chain">DSP chain</a>.</span></li>
 *     <li><span>{@link FModChannel#getNumDSPs} Retrieves the number of <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP units</a> in the <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-chain">DSP chain</a>.</span></li>
 *     <li><span>{@link FModChannel#getDSP} Retrieves the <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP unit</a> at the specified index in the <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-chain">DSP chain</a>.</span></li>
 *     <li><span>{@link FModChannel#setDSPIndex} Sets the index in the <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-chain">DSP chain</a> of the specified <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP</a>.</span></li>
 *     <li><span>{@link FModChannel#getDSPIndex} Retrieves the index of a <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP</a> inside the <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a> or <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a>'s <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-chain">DSP chain</a>.</span></li>
 * </ul>
 * <strong>Sample accurate scheduling:</strong>
 * <p>
 * <ul>
 *     <li><span>{@link FModChannel#getDSPClock} Retrieves the <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-clock">DSP clock</a> values at this point in time.</span></li>
 *     <li><span>{@link FModChannel#setDelay} Sets a sample accurate start (and/or stop) time relative to the parent ChannelGroup <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-clock">DSP clock</a>.</span></li>
 *     <li><span>{@link FModChannel#getDelay} Retrieves a sample accurate start (and/or stop) time relative to the parent ChannelGroup <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-clock">DSP clock</a>.</span></li>
 *     <li><span>{@link FModChannel#addFadePoint} Adds a sample accurate fade point at a time relative to the parent ChannelGroup <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-clock">DSP clock</a>.</span></li>
 *     <li><span>{@link FModChannel#setFadePointRamp} Adds a volume ramp at the specified time in the future using fade points.</span></li>
 *     <li><span>{@link FModChannel#removeFadePoints} Removes all fade points between the two specified clock values (inclusive).</span></li>
 *     <li><span>{@link FModChannel#getFadePoints} Retrieves information about stored fade points.</span></li>
 * </ul>
 * <strong>General:</strong>
 * <p>
 * <ul>
 *     <li><span>{@link FModChannel#setCallback} Sets the callback for ChannelControl level notifications.</span></li>
 *     <li><span>{@link FModChannel#setUserData} Sets a user value associated with this object.</span></li>
 *     <li><span>{@link FModChannel#getUserData} Retrieves a user value associated with this object.</span></li>
 *     <li><span>{@link FModChannel#getSystemObject} Retrieves the System that created this object.</span></li>
 * </ul>
 * </div>
 * 
 * @author FMOD team, autogenerated by Alcadrial
 */
public class FModChannel {

	private FModChannel()
	{

	}

	/**
	 * Adds a
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP unit</a> to the specified index in the <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-chain">DSP chain</a>.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_AddDSP(
	 *   FMOD_CHANNEL *channel,
	 *   int index,
	 *   FMOD_DSP *dsp
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_AddDSP(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   int index,
	 *   FMOD_DSP *dsp
	 * );
	 * </code></pre>
	 * <p>
	 * If
	 * <code>dsp</code> is already added to an existing object it will be removed and then added to this object.
	 * <p>
	 * For detailed information on FMOD's
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-graph">DSP graph</a>, see the <a href="https://www.fmod.com/docs/2.03/api/using-dsp-effects-in-the-core-api.html#dsp-architecture-and-usage">DSP Architecture and Usage</a> section of the <a href="https://www.fmod.com/docs/2.03/api/using-dsp-effects-in-the-core-api.html">Using DSP Effects in the Core API</a> chapter.
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#removeDSP}, {@link FModSystem#createDSP}, {@link FModSystem#createDSPByType}, {@link FModSystem#createDSPByPlugin}
	 * <p>
	 * 
	 * @param channel
	 * @param index <dd><p>Offset into the <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-chain">DSP chain</a>, see <a href="https://www.fmod.com/docs/2.03/api/core-api-channelcontrol.html#fmod_channelcontrol_dsp_index">FMOD_CHANNELCONTROL_DSP_INDEX</a> for special named offsets for 'head' and 'tail' and 'fader' units.</p><ul><li><span class="label">Range:</span> [0, {@link FModChannel#getNumDSPs})</li></ul>
	 * @param dsp <dd>DSP unit to be added. (<a href="https://www.fmod.com/docs/2.03/api/core-api-dsp.html">DSP</a>)
	 */
	public static int addDSP(long channel, int index, long dsp)
	{
		return FMOD.FMOD_Channel_AddDSP(channel, index, dsp);
	}

	/**
	 * Adds a sample accurate fade point at a time relative to the parent ChannelGroup
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-clock">DSP clock</a>.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_AddFadePoint(
	 *   FMOD_CHANNEL *channel,
	 *   unsigned long long dspclock,
	 *   float volume
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_AddFadePoint(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   unsigned long long dspclock,
	 *   float volume
	 * );
	 * </code></pre>
	 * <p>
	 * Fade points are scaled against other volume settings and in-between each fade point the volume will be linearly ramped.
	 * <p>
	 * To perform sample accurate fading use
	 * {@link FModChannel#getDSPClock} to query the parent clock value. If a parent <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a> changes its pitch, the fade points will still be correct as the parent clock rate is adjusted by that pitch.
	 * <p>
	 * <pre><code>
	 * /* Example. Ramp from full volume to half volume over the next 4096 samples {@literal *}/unsigned long long parentclock;FMOD_ChannelControl_GetDSPClock(
	 * target,
	 *  NULL,
	 *  &amp;parentclock
	 * );
	 * FMOD_ChannelControl_AddFadePoint(
	 * target,
	 *  parentclock,
	 *         1.0f
	 * );
	 * FMOD_ChannelControl_AddFadePoint(
	 * target,
	 *  parentclock + 4096,
	 *  0.5f
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#removeFadePoints}, {@link FModChannel#setFadePointRamp}, {@link FModChannel#getFadePoints}
	 * <p>
	 * 
	 * @param channel
	 * @param dspclock <dd><p><a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-clock">DSP clock</a> of the parent <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a> to set the fade point volume.</p><ul><li><span class="label">Units:</span> Samples</li></ul>
	 * @param volume <dd><p>Volume level at the given <code>dspclock</code>.  Values above 1.0 amplify the <a href="https://www.fmod.com/docs/2.03/api/glossary.html#signal">signal</a>.</p><ul><li><span class="label">Units:</span> Linear</li><li><span class="label">Range:</span> [0, inf)</li></ul>
	 */
	public static int addFadePoint(long channel, long dspclock, float volume)
	{
		return FMOD.FMOD_Channel_AddFadePoint(channel, dspclock, volume);
	}

	/**
	 * Retrieves the 3D position and velocity used to apply panning, attenuation and doppler.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_Get3DAttributes(
	 *   FMOD_CHANNEL *channel,
	 *   FMOD_VECTOR *pos,
	 *   FMOD_VECTOR *vel
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_Get3DAttributes(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   FMOD_VECTOR *pos,
	 *   FMOD_VECTOR *vel
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#set3DAttributes}
	 * <p>
	 * 
	 * @param channel
	 * @param pos <span>(<i>Out, Opt</i>) <dd><p>Position in 3D space used for panning and attenuation. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_vector">FMOD_VECTOR</a>)</p><ul><li><span class="label">Units:</span> <a href="https://www.fmod.com/docs/2.03/api/glossary.html#distance-units">Distance units</a></li></ul>
	 * @param vel <span>(<i>Out, Opt</i>) <dd><p>Velocity in 3D space used for doppler. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_vector">FMOD_VECTOR</a>)</p><ul><li><span class="label">Units:</span> <a href="https://www.fmod.com/docs/2.03/api/glossary.html#distance-units">Distance units</a> per second</li></ul>
	 */
	public static int get3DAttributes(long channel, org.lwjgl.fmod.FMOD_VECTOR pos, org.lwjgl.fmod.FMOD_VECTOR vel)
	{
		return FMOD.FMOD_Channel_Get3DAttributes(channel, pos, vel);
	}

	/**
	 * Retrieves the orientation of a 3D cone shape, used for simulated occlusion.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_Get3DConeOrientation(
	 *   FMOD_CHANNEL *channel,
	 *   FMOD_VECTOR *orientation
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_Get3DConeOrientation(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   FMOD_VECTOR *orientation
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#set3DConeOrientation}
	 * <p>
	 * 
	 * @param channel
	 * @param orientation <span>(<i>Out</i>) <dd><p>Normalized orientation vector, which represents the direction of the sound cone. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_vector">FMOD_VECTOR</a>)</p><ul><li><span class="label">Default:</span> (0, 0, 1)</li></ul>
	 */
	public static int get3DConeOrientation(long channel, org.lwjgl.fmod.FMOD_VECTOR orientation)
	{
		return FMOD.FMOD_Channel_Get3DConeOrientation(channel, orientation);
	}

	/**
	 * Retrieves the angles and attenuation levels of a 3D cone shape, for simulated occlusion which is based on direction.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_Get3DConeSettings(
	 *   FMOD_CHANNEL *channel,
	 *   float *insideconeangle,
	 *   float *outsideconeangle,
	 *   float *outsidevolume
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_Get3DConeSettings(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   float *insideconeangle,
	 *   float *outsideconeangle,
	 *   float *outsidevolume
	 * );
	 * </code></pre>
	 * <p>
	 * When
	 * {@link FModChannel#set3DConeOrientation} is used and a 3D 'cone' is set up, attenuation will automatically occur for a sound based on the relative angle of the direction the cone is facing, vs the angle between the sound and the listener.
	 * <p>
	 * <ul><li>If the relative angle is within the <code>insideconeangle</code>, the sound will not have any attenuation applied.</li><li>If the relative angle is between the <code>insideconeangle</code> and <code>outsideconeangle</code>, linear volume attenuation (between 1 and <code>outsidevolume</code>) is applied between the two angles until it reaches the <code>outsideconeangle</code>.</li><li>If the relative angle is outside of the <code>outsideconeangle</code> the volume does not attenuate any further.</li></ul>
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#set3DConeSettings}
	 * <p>
	 * 
	 * @param channel
	 * @param insideConeAngle <span>(<i>Out, Opt</i>) <dd><p>Inside cone angle. This is the angle spread within which the sound is unattenuated.</p><ul><li><span class="label">Units:</span> Degrees</li><li><span class="label">Range:</span> [0, 360]</li><li><span class="label">Default:</span> 360</li></ul>
	 * @param outsideConeAngle <span>(<i>Out, Opt</i>) <dd><p>Outside cone angle. This is the angle spread outside of which the sound is attenuated to its <code>outsidevolume</code>.</p><ul><li><span class="label">Units:</span> Degrees</li><li><span class="label">Range:</span> [0, 360]</li><li><span class="label">Default:</span> 360</li></ul>
	 * @param outsideVolume <span>(<i>Out, Opt</i>) <dd><p>Cone outside volume.</p><ul><li><span class="label">Units:</span> Linear</li><li><span class="label">Range:</span> [0, 1]</li><li><span class="label">Default:</span> 1</li></ul>
	 */
	public static int get3DConeSettings(long channel, java.nio.FloatBuffer insideConeAngle, java.nio.FloatBuffer outsideConeAngle, java.nio.FloatBuffer outsideVolume)
	{
		return FMOD.FMOD_Channel_Get3DConeSettings(channel, insideConeAngle, outsideConeAngle, outsideVolume);
	}

	/**
	 * Retrieves the current custom roll-off shape for 3D distance attenuation.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_Get3DCustomRolloff(
	 *   FMOD_CHANNEL *channel,
	 *   FMOD_VECTOR **points,
	 *   int *numpoints
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_Get3DCustomRolloff(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   FMOD_VECTOR **points,
	 *   int *numpoints
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#set3DCustomRolloff}
	 * <p>
	 * 
	 * @param channel
	 * @param points <span>(<i>Out, Opt</i>) <dd><p>Array of vectors sorted by distance, where <code>x</code> = distance and <code>y</code> = volume from 0 to 1. <code>z</code> is unused. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_vector">FMOD_VECTOR</a>)</p><ul><li><span class="label">Units:</span> <a href="https://www.fmod.com/docs/2.03/api/glossary.html#distance-units">Distance units</a></li></ul>
	 * @param numPoints <span>(<i>Out, Opt</i>) <dd>Number of entries in the <code>points</code> array.
	 */
	public static int get3DCustomRolloff(long channel, org.lwjgl.PointerBuffer points, java.nio.IntBuffer numPoints)
	{
		return FMOD.FMOD_Channel_Get3DCustomRolloff(channel, points, numPoints);
	}

	/**
	 * Retrieves the override values for the 3D distance filter.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_Get3DDistanceFilter(
	 *   FMOD_CHANNEL *channel,
	 *   FMOD_BOOL *custom,
	 *   float *customLevel,
	 *   float *centerFreq
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_Get3DDistanceFilter(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   FMOD_BOOL *custom,
	 *   float *customLevel,
	 *   float *centerFreq
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#set3DDistanceFilter}
	 * <p>
	 * 
	 * @param channel
	 * @param custom <span>(<i>Out, Opt</i>) <dd><p>Override automatic distance filtering and use <code>customLevel</code> instead.</p><ul><li><span class="label">Units:</span> Boolean</li><li><span class="label">Default:</span> False</li></ul>
	 * @param customLevel <span>(<i>Out, Opt</i>) <dd><p>Attenuation factor where 1 represents no attenuation and 0 represents complete attenuation.</p><ul><li><span class="label">Range:</span> [0, 1]</li><li><span class="label">Default:</span> 1</li></ul>
	 * @param centerFreq <span>(<i>Out, Opt</i>) <dd><p>Center frequency of the band-pass filter used to simulate distance attenuation, 0 for default of <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_advancedsettings_distancefiltercenterfreq">FMOD_ADVANCEDSETTINGS::distanceFilterCenterFreq</a>.</p><ul><li><span class="label">Units:</span> Hertz</li><li><span class="label">Range:</span> [10, 22050]</li><li><span class="label">Default:</span> <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_advancedsettings_distancefiltercenterfreq">FMOD_ADVANCEDSETTINGS::distanceFilterCenterFreq</a></li></ul>
	 */
	public static int get3DDistanceFilter(long channel, java.nio.IntBuffer custom, java.nio.FloatBuffer customLevel, java.nio.FloatBuffer centerFreq)
	{
		return FMOD.FMOD_Channel_Get3DDistanceFilter(channel, custom, customLevel, centerFreq);
	}

	/**
	 * Retrieves the amount by which doppler is scaled.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_Get3DDopplerLevel(
	 *   FMOD_CHANNEL *channel,
	 *   float *level
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_Get3DDopplerLevel(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   float *level
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#set3DDopplerLevel}
	 * <p>
	 * 
	 * @param channel
	 * @param level <span>(<i>Out</i>) <dd><p>Doppler scale where 0 represents no doppler, 1 represents natural doppler and 5 represents exaggerated doppler.</p><ul><li><span class="label">Range:</span> [0, 5]</li><li><span class="label">Default:</span> 1</li></ul>
	 */
	public static int get3DDopplerLevel(long channel, java.nio.FloatBuffer level)
	{
		return FMOD.FMOD_Channel_Get3DDopplerLevel(channel, level);
	}

	/**
	 * Retrieves the blend between 3D panning and 2D panning.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_Get3DLevel(
	 *   FMOD_CHANNEL *channel,
	 *   float *level
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_Get3DLevel(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   float *level
	 * );
	 * </code></pre>
	 * <p>
	 * The
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d">FMOD_3D</a> flag must be set on this object otherwise <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_err_needs3d">FMOD_ERR_NEEDS3D</a> is returned.
	 * <p>
	 * 2D functions include:
	 * <p>
	 * <ul><li>{@link FModChannel#setPan}</li><li>{@link FModChannel#setMixLevelsOutput}</li><li>{@link FModChannel#setMixLevelsInput}</li><li>{@link FModChannel#setMixMatrix}</li></ul>
	 * <p>
	 * 3D functions include:
	 * <p>
	 * <ul><li>{@link FModChannel#set3DAttributes}</li><li>{@link FModChannel#set3DConeOrientation}</li><li>{@link FModChannel#set3DCustomRolloff}</li></ul>
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#set3DLevel}
	 * <p>
	 * 
	 * @param channel
	 * @param level <span>(<i>Out</i>) <dd><p>3D pan level where 0 represents panning/attenuating solely with 2D panning functions and 1 represents solely 3D.</p><ul><li><span class="label">Units:</span> Linear</li><li><span class="label">Range:</span> [0, 1]</li><li><span class="label">Default:</span> 1</li></ul>
	 */
	public static int get3DLevel(long channel, java.nio.FloatBuffer level)
	{
		return FMOD.FMOD_Channel_Get3DLevel(channel, level);
	}

	/**
	 * Retrieves the minimum and maximum distances used to calculate the 3D roll-off attenuation.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_Get3DMinMaxDistance(
	 *   FMOD_CHANNEL *channel,
	 *   float *mindistance,
	 *   float *maxdistance
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_Get3DMinMaxDistance(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   float *mindistance,
	 *   float *maxdistance
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#set3DMinMaxDistance}, {@link FModSystem#set3DSettings}
	 * <p>
	 * 
	 * @param channel
	 * @param mindistance <span>(<i>Out, Opt</i>) <dd><p>Distance from the source where attenuation begins.</p><ul><li><span class="label">Units:</span> <a href="https://www.fmod.com/docs/2.03/api/glossary.html#distance-units">Distance units</a></li><li><span class="label">Range:</span> [0, inf)</li><li><span class="label">Default:</span> 1</li></ul>
	 * @param maxdistance <span>(<i>Out, Opt</i>) <dd><p>Distance from the source where attenuation ends.</p><ul><li><span class="label">Units:</span> <a href="https://www.fmod.com/docs/2.03/api/glossary.html#distance-units">Distance units</a></li><li><span class="label">Range:</span> [<code>mindistance</code>, inf)</li><li><span class="label">Default:</span> 1</li></ul>
	 */
	public static int get3DMinMaxDistance(long channel, java.nio.FloatBuffer mindistance, java.nio.FloatBuffer maxdistance)
	{
		return FMOD.FMOD_Channel_Get3DMinMaxDistance(channel, mindistance, maxdistance);
	}

	/**
	 * Retrieves the 3D attenuation factors for the direct and reverb paths.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_Get3DOcclusion(
	 *   FMOD_CHANNEL *channel,
	 *   float *directocclusion,
	 *   float *reverbocclusion
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_Get3DOcclusion(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   float *directocclusion,
	 *   float *reverbocclusion
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#set3DOcclusion}
	 * <p>
	 * 
	 * @param channel
	 * @param directocclusion <span>(<i>Out, Opt</i>) <dd><p>Occlusion factor for the direct path where 0 represents no occlusion and 1 represents full occlusion.</p><ul><li><span class="label">Range:</span> [0, 1]</li><li><span class="label">Default:</span> 0</li></ul>
	 * @param reverbocclusion <span>(<i>Out, Opt</i>) <dd><p>Occlusion factor for the reverb path where 0 represents no occlusion and 1 represents full occlusion.</p><ul><li><span class="label">Range:</span> [0, 1]</li><li><span class="label">Default:</span> 0</li></ul>
	 */
	public static int get3DOcclusion(long channel, java.nio.FloatBuffer directocclusion, java.nio.FloatBuffer reverbocclusion)
	{
		return FMOD.FMOD_Channel_Get3DOcclusion(channel, directocclusion, reverbocclusion);
	}

	/**
	 * Retrieves the spread of a 3D sound in speaker space.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_Get3DSpread(
	 *   FMOD_CHANNEL *channel,
	 *   float *angle
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_Get3DSpread(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   float *angle
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#set3DSpread}
	 * <p>
	 * 
	 * @param channel
	 * @param angle <span>(<i>Out</i>) <dd><p>Angle over which the sound is spread.</p><ul><li><span class="label">Units:</span> Degrees</li><li><span class="label">Range:</span> [0, 360]</li><li><span class="label">Default:</span> 0</li></ul>
	 */
	public static int get3DSpread(long channel, java.nio.FloatBuffer angle)
	{
		return FMOD.FMOD_Channel_Get3DSpread(channel, angle);
	}

	/**
	 * Gets the calculated audibility based on all attenuation factors which contribute to the final output volume.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_GetAudibility(
	 *   FMOD_CHANNEL *channel,
	 *   float *audibility
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_GetAudibility(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   float *audibility
	 * );
	 * </code></pre>
	 * <p>
	 * The estimated volume is calculated using a number of factors detailed in the
	 * <a href="https://www.fmod.com/docs/2.03/api/managing-resources-in-the-core-api.html#audibility-calculation">Audibility Calculation</a> section of the <a href="https://www.fmod.com/docs/2.03/api/managing-resources-in-the-core-api.html">Core API Managing Resources</a> chapter.
	 * <p>
	 * While this does not represent the actual waveform,
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s playing FSB files will take into consideration the overall peak level of the file (if available).
	 * <p>
	 * This value is used to determine which
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s should be audible and which <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s to virtualize when resources are limited.
	 * <p>
	 * See the
	 * <a href="https://www.fmod.com/docs/2.03/api/managing-resources-in-the-core-api.html#virtual-voice-system">Virtual Voice System</a> section of the <a href="https://www.fmod.com/docs/2.03/api/managing-resources-in-the-core-api.html">Managing Resources in the Core API</a> chapter for more details about how audibility is calculated.
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#isVirtual}, {@link FModChannel#getVolume}, {@link FModChannel#get3DOcclusion}, {@link FModChannel#get3DAttributes}, <a href="https://www.fmod.com/docs/2.03/api/plugin-api-dsp.html#fmod_dsp_parameter_overallgain">FMOD_DSP_PARAMETER_OVERALLGAIN</a>
	 * <p>
	 * 
	 * @param channel
	 * @param audibility <span>(<i>Out</i>) <dd>Estimated audibility.
	 */
	public static int getAudibility(long channel, java.nio.FloatBuffer audibility)
	{
		return FMOD.FMOD_Channel_GetAudibility(channel, audibility);
	}

	/**
	 * Retrieves the ChannelGroup this object outputs to.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_GetChannelGroup(
	 *   FMOD_CHANNEL *channel,
	 *   FMOD_CHANNELGROUP **channelgroup
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#setChannelGroup}
	 * <p>
	 * 
	 * @param channel
	 * @param channelgroup <span>(<i>Out</i>) <dd>Output group. (<a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a>)
	 */
	public static int getChannelGroup(long channel, org.lwjgl.PointerBuffer channelgroup)
	{
		return FMOD.FMOD_Channel_GetChannelGroup(channel, channelgroup);
	}

	/**
	 * Retrieves the currently playing Sound.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_GetCurrentSound(
	 *   FMOD_CHANNEL *channel,
	 *   FMOD_SOUND **sound
	 * );
	 * </code></pre>
	 * <p>
	 * May return NULL or equivalent if no
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html">Sound</a> is playing.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#playSound}
	 * <p>
	 * 
	 * @param channel
	 * @param sound <span>(<i>Out</i>) <dd>Currently playing sound. (<a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html">Sound</a>)
	 */
	public static int getCurrentSound(long channel, org.lwjgl.PointerBuffer sound)
	{
		return FMOD.FMOD_Channel_GetCurrentSound(channel, sound);
	}

	/**
	 * Retrieves the
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP unit</a> at the specified index in the <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-chain">DSP chain</a>.
	 * <p>
	 * @param channel
	 * @param index <dd><p>Offset into the <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-chain">DSP chain</a>, see <a href="https://www.fmod.com/docs/2.03/api/core-api-channelcontrol.html#fmod_channelcontrol_dsp_index">FMOD_CHANNELCONTROL_DSP_INDEX</a> for special named offsets for 'head' and 'tail' and 'fader' units.</p><ul><li><span class="label">Range:</span> [0, {@link FModChannel#getNumDSPs})</li></ul>
	 * @param dsp <span>(<i>Out</i>) <dd><a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP unit</a> at the specified <code>index</code>. (<a href="https://www.fmod.com/docs/2.03/api/core-api-dsp.html">DSP</a>)
	 */
	public static int getDSP(long channel, int index, org.lwjgl.PointerBuffer dsp)
	{
		return FMOD.FMOD_Channel_GetDSP(channel, index, dsp);
	}

	/**
	 * Retrieves the
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-clock">DSP clock</a> values at this point in time.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_GetDSPClock(
	 *   FMOD_CHANNEL *channel,
	 *   unsigned long long *dspclock,
	 *   unsigned long long *parentclock
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_GetDSPClock(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   unsigned long long *dspclock,
	 *   unsigned long long *parentclock
	 * );
	 * </code></pre>
	 * <p>
	 * To perform sample accurate scheduling in conjunction with
	 * {@link FModChannel#setDelay} and {@link FModChannel#addFadePoint} query the <code>parentclock</code> value.
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#getDelay}
	 * <p>
	 * 
	 * @param channel
	 * @param dspclock <span>(<i>Out, Opt</i>) <dd><p><a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-clock">DSP clock</a> value for the tail <a href="https://www.fmod.com/docs/2.03/api/core-api-dsp.html">DSP</a> (<a href="https://www.fmod.com/docs/2.03/api/core-api-channelcontrol.html#fmod_channelcontrol_dsp_tail">FMOD_CHANNELCONTROL_DSP_TAIL</a>) node.</p><ul><li><span class="label">Units:</span> Samples</li></ul>
	 * @param parentclock <span>(<i>Out, Opt</i>) <dd><p><a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-clock">DSP clock</a> value for the tail <a href="https://www.fmod.com/docs/2.03/api/core-api-dsp.html">DSP</a> (<a href="https://www.fmod.com/docs/2.03/api/core-api-channelcontrol.html#fmod_channelcontrol_dsp_tail">FMOD_CHANNELCONTROL_DSP_TAIL</a>) node of the parent ChannelGroup.</p><ul><li><span class="label">Units:</span> Samples</li></ul>
	 */
	public static int getDSPClock(long channel, java.nio.LongBuffer dspclock, java.nio.LongBuffer parentclock)
	{
		return FMOD.FMOD_Channel_GetDSPClock(channel, dspclock, parentclock);
	}

	/**
	 * Retrieves the index of a
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP</a> inside the <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a> or <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a>'s <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-chain">DSP chain</a>.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_GetDSPIndex(
	 *   FMOD_CHANNEL *channel,
	 *   FMOD_DSP *dsp,
	 *   int *index
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_GetDSPIndex(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   FMOD_DSP *dsp,
	 *   int *index
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#setDSPIndex}
	 * <p>
	 * 
	 * @param channel
	 * @param dsp <dd><a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP unit</a> that exists in the <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-chain">DSP chain</a>. (<a href="https://www.fmod.com/docs/2.03/api/core-api-dsp.html">DSP</a>)
	 * @param index <span>(<i>Out</i>) <dd>Offset into the <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-chain">DSP chain</a>.
	 */
	public static int getDSPIndex(long channel, long dsp, java.nio.IntBuffer index)
	{
		return FMOD.FMOD_Channel_GetDSPIndex(channel, dsp, index);
	}

	/**
	 * Retrieves a sample accurate start (and/or stop) time relative to the parent ChannelGroup
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-clock">DSP clock</a>.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_GetDelay(
	 *   FMOD_CHANNEL *channel,
	 *   unsigned long long *dspclock_start,
	 *   unsigned long long *dspclock_end,
	 *   FMOD_BOOL *stopchannels
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_GetDelay(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   unsigned long long *dspclock_start,
	 *   unsigned long long *dspclock_end,
	 *   FMOD_BOOL *stopchannels
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#setDelay},{@link FModChannel#getDSPClock}
	 * <p>
	 * 
	 * @param channel
	 * @param dspclock_start <span>(<i>Out, Opt</i>) <dd><p><a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-clock">DSP clock</a> of the parent <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a> to audibly start playing sound at.</p><ul><li><span class="label">Units:</span> Samples</li><li><span class="label">Default:</span> 0</li></ul>
	 * @param dspclock_end <span>(<i>Out, Opt</i>) <dd><p><a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-clock">DSP clock</a> of the parent <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a> to audibly stop playing sound at.</p><ul><li><span class="label">Units:</span> Samples</li><li><span class="label">Default:</span> 0</li></ul>
	 * @param stopchannels <span>(<i>Out, Opt</i>) <dd><p>True: When <code>dspclock_end</code> is reached, behaves like {@link FModChannel#stop} has been called.<br>False: When <code>dspclock_end</code> is reached, behaves like {@link FModChannel#setPaused} has been called, a subsequent <code>dspclock_start</code> allows it to resume.</p><ul><li><span class="label">Units:</span> Boolean</li></ul></dd>
	 */
	public static int getDelay(long channel, java.nio.LongBuffer dspclock_start, java.nio.LongBuffer dspclock_end, java.nio.IntBuffer stopchannels)
	{
		return FMOD.FMOD_Channel_GetDelay(channel, dspclock_start, dspclock_end, stopchannels);
	}

	/**
	 * Retrieves information about stored fade points.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_GetFadePoints(
	 *   FMOD_CHANNEL *channel,
	 *   unsigned int *numpoints,
	 *   unsigned long long *point_dspclock,
	 *   float *point_volume
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_GetFadePoints(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   unsigned int *numpoints,
	 *   unsigned long long *point_dspclock,
	 *   float *point_volume
	 * );
	 * </code></pre>
	 * <p>
	 * Passing NULL for
	 * <code>point_dspclock</code> and <code>point_volume</code> will allow you to query the number of fade points stored, with result stored in <code>numpoints</code>. <br>If <code>numpoints</code> is specified by the user, the value will be used as an input for the maximum number of fade points to output. It will then output as the number of fadepoints retrieved.
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#addFadePoint}, {@link FModChannel#setFadePointRamp}, {@link FModChannel#removeFadePoints}
	 * <p>
	 * 
	 * @param channel
	 * @param numPoints <span>(<i>Out</i>) <dd>Number of fade points.
	 * @param point_dspclock <span>(<i>Out, Opt</i>) <dd><p>Array of <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-clock">DSP clock</a> values that represent the fade point times.</p><ul><li><span class="label">Units:</span> Samples</li></ul>
	 * @param point_volume <span>(<i>Out, Opt</i>) <dd><p>Array of volume levels that represent the fade point values.</p><ul><li><span class="label">Units:</span> Linear</li><li><span class="label">Range:</span> [0, inf)</li></ul>
	 */
	public static int getFadePoints(long channel, java.nio.IntBuffer numPoints, java.nio.LongBuffer point_dspclock, java.nio.FloatBuffer point_volume)
	{
		return FMOD.FMOD_Channel_GetFadePoints(channel, numPoints, point_dspclock, point_volume);
	}

	/**
	 * Retrieves the playback frequency or playback rate.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_GetFrequency(
	 *   FMOD_CHANNEL *channel,
	 *   float *frequency
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#setFrequency}
	 * <p>
	 * 
	 * @param channel
	 * @param frequency <span>(<i>Out</i>) <dd><p>Playback frequency.</p><ul><li><span class="label">Units:</span> Hertz</li></ul>
	 */
	public static int getFrequency(long channel, java.nio.FloatBuffer frequency)
	{
		return FMOD.FMOD_Channel_GetFrequency(channel, frequency);
	}

	/**
	 * Retrieves the index of this object in the System Channel pool.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_GetIndex(
	 *   FMOD_CHANNEL *channel,
	 *   int *index
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#getChannel}
	 * <p>
	 * 
	 * @param channel
	 * @param index <span>(<i>Out</i>) <dd><p>Index within the <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html">System</a> <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a> pool.</p><ul><li><span class="label">Range:</span> [0, 'maxchannels' value from {@link FModSystem#init} - 1]</li></ul>
	 */
	public static int getIndex(long channel, java.nio.IntBuffer index)
	{
		return FMOD.FMOD_Channel_GetIndex(channel, index);
	}

	/**
	 * Retrieves the number of times to loop before stopping.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_GetLoopCount(
	 *   FMOD_CHANNEL *channel,
	 *   int *loopcount
	 * );
	 * </code></pre>
	 * <p>
	 * This is the
	 * <em>current</em> loop countdown value that will decrement as it plays until reaching 0. Reset with {@link FModChannel#setLoopCount}.
	 * <p>
	 * 
	 * @param channel
	 * @param loopCount <span>(<i>Out</i>) <dd>Times to loop before stopping where 0 represents "oneshot", 1 represents "loop once then stop" and -1 represents "loop forever".
	 */
	public static int getLoopCount(long channel, java.nio.IntBuffer loopCount)
	{
		return FMOD.FMOD_Channel_GetLoopCount(channel, loopCount);
	}

	/**
	 * Retrieves the loop start and end points.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_GetLoopPoints(
	 *   FMOD_CHANNEL *channel,
	 *   unsigned int *loopstart,
	 *   FMOD_TIMEUNIT loopstarttype,
	 *   unsigned int *loopend,
	 *   FMOD_TIMEUNIT loopendtype
	 * );
	 * </code></pre>
	 * <p>
	 * Valid
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit">FMOD_TIMEUNIT</a> types are <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_pcm">FMOD_TIMEUNIT_PCM</a>, <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_ms">FMOD_TIMEUNIT_MS</a>, <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_pcmbytes">FMOD_TIMEUNIT_PCMBYTES</a>.  Any other time units return <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_err_format">FMOD_ERR_FORMAT</a>.<br>If <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_ms">FMOD_TIMEUNIT_MS</a> or <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_pcmbytes">FMOD_TIMEUNIT_PCMBYTES</a> are used, the value is internally converted from <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_pcm">FMOD_TIMEUNIT_PCM</a>, so the retrieved value may not exactly match the set value.
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#setLoopPoints}
	 * <p>
	 * 
	 * @param channel
	 * @param loopStart <span>(<i>Out, Opt</i>) <dd><p>Loop start point.</p><ul><li><span class="label">Range:</span> [0, {@link FModSound#getLength} - 1]. </li></ul>
	 * @param loopStartType <dd>Time units for <code>loopstart</code>. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit">FMOD_TIMEUNIT</a>)
	 * @param loopEnd <span>(<i>Out, Opt</i>) <dd><p>Loop end point.</p><ul><li><span class="label">Range:</span> [0, {@link FModSound#getLength} - 1]. </li></ul>
	 * @param loopEndType <dd>Time units for <code>loopend</code>. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit">FMOD_TIMEUNIT</a>)
	 */
	public static int getLoopPoints(long channel, java.nio.IntBuffer loopStart, int loopStartType, java.nio.IntBuffer loopEnd, int loopEndType)
	{
		return FMOD.FMOD_Channel_GetLoopPoints(channel, loopStart, loopStartType, loopEnd, loopEndType);
	}

	/**
	 * Retrieves the gain of the dry
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#signal">signal</a> when built in lowpass / distance filtering is applied.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_GetLowPassGain(
	 *   FMOD_CHANNEL *channel,
	 *   float *gain
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_GetLowPassGain(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   float *gain
	 * );
	 * </code></pre>
	 * <p>
	 * Requires the built in lowpass to be created with
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_init_channel_lowpass">FMOD_INIT_CHANNEL_LOWPASS</a> or <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_init_channel_distancefilter">FMOD_INIT_CHANNEL_DISTANCEFILTER</a>.
	 * <p>
	 * <div class="admonition warning">
	 * <p>
	 * Currently only supported for Channel, not ChannelGroup.
	 * <p>
	 * </div>
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#setLowPassGain}
	 * <p>
	 * 
	 * @param channel
	 * @param gain <span>(<i>Out</i>) <dd><p>gain level where 0 represents silent (full filtering) and 1 represents full volume (no filtering).</p><ul><li><span class="label">Units:</span> Linear</li><li><span class="label">Range:</span> [0, 1]</li><li><span class="label">Default:</span> 1</li></ul>
	 */
	public static int getLowPassGain(long channel, java.nio.FloatBuffer gain)
	{
		return FMOD.FMOD_Channel_GetLowPassGain(channel, gain);
	}

	/**
	 * Retrieves a 2 dimensional pan matrix that maps the
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#signal">signal</a> from input channels (columns) to output speakers (rows).
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_GetMixMatrix(
	 *   FMOD_CHANNEL *channel,
	 *   float *matrix,
	 *   int *outchannels,
	 *   int *inchannels,
	 *   int inchannel_hop
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_GetMixMatrix(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   float *matrix,
	 *   int *outchannels,
	 *   int *inchannels,
	 *   int inchannel_hop
	 * );
	 * </code></pre><ul><li>Range: [0, <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_max_channel_width">FMOD_MAX_CHANNEL_WIDTH</a>]</li></ul><ul><li>Range: [0, <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_max_channel_width">FMOD_MAX_CHANNEL_WIDTH</a>]</li></ul>
	 * <p>
	 * Passing NULL for
	 * <code>matrix</code> will allow you to query <code>outchannels</code> and <code>inchannels</code> otherwise they are not optional.
	 * <p>
	 * Matrix element values can be below 0 to invert a signal and above 1 to amplify the signal. Note that increasing the signal level too far may cause audible distortion.
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#setMixMatrix}
	 * <p>
	 * 
	 * @param channel
	 * @param matrix <span>(<i>Out, Opt</i>) <dd><p>Two dimensional array of volume levels in row-major order. Each row represents an output speaker, each column represents an input channel.</p><ul><li><span class="label">Units:</span> Linear</li><li><span class="label">Range:</span> (-inf, inf)</li></ul>
	 * @param outchannels <span>(<i>Out, Opt</i>) <dd>Number of valid output channels (rows) in <code>matrix</code>.
	 * @param inchannels <span>(<i>Out, Opt</i>) <dd>Number of valid input channels (columns) in <code>matrix</code>.
	 * @param inchannel_hop <span>(<i>Out, Opt</i>) <dd>Width (total number of columns) in destination <code>matrix</code>. A matrix element is referenced as 'outchannel * inchannel_hop + inchannel'.
	 */
	public static int getMixMatrix(long channel, java.nio.FloatBuffer matrix, java.nio.IntBuffer outchannels, java.nio.IntBuffer inchannels, int inchannel_hop)
	{
		return FMOD.FMOD_Channel_GetMixMatrix(channel, matrix, outchannels, inchannels, inchannel_hop);
	}

	/**
	 * Retrieves the playback mode bits that control how this object behaves.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_GetMode(
	 *   FMOD_CHANNEL *channel,
	 *   FMOD_MODE *mode
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_GetMode(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   FMOD_MODE *mode
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#setMode}
	 * <p>
	 * 
	 * @param channel
	 * @param mode <span>(<i>Out</i>) <dd>Playback mode bitfield.  Test against a specific mode with the AND operator. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_mode">FMOD_MODE</a>)
	 */
	public static int getMode(long channel, java.nio.IntBuffer mode)
	{
		return FMOD.FMOD_Channel_GetMode(channel, mode);
	}

	/**
	 * Retrieves the mute state.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_GetMute(
	 *   FMOD_CHANNEL *channel,
	 *   FMOD_BOOL *mute
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_GetMute(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   FMOD_BOOL *mute
	 * );
	 * </code></pre>
	 * <p>
	 * An individual mute state is kept for each object, a parent
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a> being muted will effectively mute this object however when queried the individual mute state is returned.  {@link FModChannel#getAudibility} can be used to calculate overall audibility for a <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a> or <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a>.
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#setMute}
	 * <p>
	 * 
	 * @param channel
	 * @param mute <span>(<i>Out</i>) <dd><p>Mute state. True = silent.  False = audible.</p><ul><li><span class="label">Units:</span> Boolean</li></ul>
	 */
	public static int getMute(long channel, java.nio.IntBuffer mute)
	{
		return FMOD.FMOD_Channel_GetMute(channel, mute);
	}

	/**
	 * Retrieves the number of
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP units</a> in the <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-chain">DSP chain</a>.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_GetNumDSPs(
	 *   FMOD_CHANNEL *channel,
	 *   int *numdsps
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_GetNumDSPs(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   int *numdsps
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#addDSP}, {@link FModChannel#removeDSP}, {@link FModChannel#getDSP}
	 * <p>
	 * 
	 * @param channel
	 * @param numdsps <span>(<i>Out</i>) <dd>Number of <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP units</a> in the <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-chain">DSP chain</a>.
	 */
	public static int getNumDSPs(long channel, java.nio.IntBuffer numdsps)
	{
		return FMOD.FMOD_Channel_GetNumDSPs(channel, numdsps);
	}

	/**
	 * Retrieves the paused state.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_GetPaused(
	 *   FMOD_CHANNEL *channel,
	 *   FMOD_BOOL *paused
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_GetPaused(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   FMOD_BOOL *paused
	 * );
	 * </code></pre>
	 * <p>
	 * An individual pause state is kept for each object, a parent
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a> being paused will effectively pause this object however when queried the individual pause state is returned.
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#setPaused}
	 * <p>
	 * 
	 * @param channel
	 * @param paused <span>(<i>Out</i>) <dd><p>Paused state.  True = playback halted.  False = playback active.</p><ul><li><span class="label">Units:</span> Boolean</li></ul>
	 */
	public static int getPaused(long channel, java.nio.IntBuffer paused)
	{
		return FMOD.FMOD_Channel_GetPaused(channel, paused);
	}

	/**
	 * Retrieves the relative pitch / playback rate.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_GetPitch(
	 *   FMOD_CHANNEL *channel,
	 *   float *pitch
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_GetPitch(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   float *pitch
	 * );
	 * </code></pre>
	 * <p>
	 * An individual pitch value is kept for each object, a parent
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a> pitch will effectively scale the pitch of this object however when queried the individual pitch value is returned.
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#setPitch}
	 * <p>
	 * 
	 * @param channel
	 * @param pitch <span>(<i>Out</i>) <dd>Pitch value where 0.5 represents half pitch (one octave down), 1 represents unmodified pitch and 2 represents double pitch (one octave up).
	 */
	public static int getPitch(long channel, java.nio.FloatBuffer pitch)
	{
		return FMOD.FMOD_Channel_GetPitch(channel, pitch);
	}

	/**
	 * Retrieves the current playback position.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_GetPosition(
	 *   FMOD_CHANNEL *channel,
	 *   unsigned int *position,
	 *   FMOD_TIMEUNIT postype
	 * );
	 * </code></pre>
	 * <p>
	 * Certain
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit">FMOD_TIMEUNIT</a> types are always available: <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_pcm">FMOD_TIMEUNIT_PCM</a>, <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_pcmbytes">FMOD_TIMEUNIT_PCMBYTES</a> and <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_ms">FMOD_TIMEUNIT_MS</a>. The others are format specific such as <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_modorder">FMOD_TIMEUNIT_MODORDER</a> / <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_modrow">FMOD_TIMEUNIT_MODROW</a> / <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_modpattern">FMOD_TIMEUNIT_MODPATTERN</a> which is specific to files of type MOD / S3M / XM / IT.
	 * <p>
	 * If
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_ms">FMOD_TIMEUNIT_MS</a> or <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_pcmbytes">FMOD_TIMEUNIT_PCMBYTES</a> are used, the value is internally converted from <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_pcm">FMOD_TIMEUNIT_PCM</a>, so the retrieved value may not exactly match the set value.
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#setPosition}
	 * <p>
	 * 
	 * @param channel
	 * @param position <span>(<i>Out</i>) <dd>Playback position.
	 * @param postype <dd>Time units for <code>position</code>. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit">FMOD_TIMEUNIT</a>)
	 */
	public static int getPosition(long channel, java.nio.IntBuffer position, int postype)
	{
		return FMOD.FMOD_Channel_GetPosition(channel, position, postype);
	}

	/**
	 * Retrieves the priority used for virtual voice ordering.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_GetPriority(
	 *   FMOD_CHANNEL *channel,
	 *   int *priority
	 * );
	 * </code></pre>
	 * <p>
	 * Priority is used as a coarse grain control for the virtual voice system, lower priority
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s will always be stolen before higher ones. For <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s of equal priority, those with the quietest {@link FModChannel#getAudibility} value will be stolen first.
	 * <p>
	 * For more information, see the
	 * <a href="https://www.fmod.com/docs/2.03/api/managing-resources-in-the-core-api.html#virtual-voice-system">Virtual Voice System</a> section of the <a href="https://www.fmod.com/docs/2.03/api/managing-resources-in-the-core-api.html">Managing Resources in the Core API</a> chapter.
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#setPriority}
	 * <p>
	 * 
	 * @param channel
	 * @param priority <span>(<i>Out</i>) <dd><p>Priority where 0 represents most important and 256 represents least important.</p><ul><li><span class="label">Range:</span> [0, 256]</li><li><span class="label">Default:</span> 128</li></ul>
	 */
	public static int getPriority(long channel, java.nio.IntBuffer priority)
	{
		return FMOD.FMOD_Channel_GetPriority(channel, priority);
	}

	/**
	 * Retrieves the wet / send level for a particular reverb instance.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_GetReverbProperties(
	 *   FMOD_CHANNEL *channel,
	 *   int instance,
	 *   float *wet
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_GetReverbProperties(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   int instance,
	 *   float *wet
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#setReverbProperties}
	 * <p>
	 * 
	 * @param channel
	 * @param instance <dd><p>Reverb instance index.</p><ul><li><span class="label">Range:</span> [0, <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_reverb_maxinstances">FMOD_REVERB_MAXINSTANCES</a>]</li></ul>
	 * @param wet <span>(<i>Out</i>) <dd><p>Send level for the <a href="https://www.fmod.com/docs/2.03/api/glossary.html#signal">signal</a> to the reverb. 0 = none, 1 = full.</p><ul><li><span class="label">Units:</span> Linear</li><li><span class="label">Range:</span> (-inf, inf)</li><li><span class="label">Default:</span> 1 for <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>, 0 for <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a></li></ul>
	 */
	public static int getReverbProperties(long channel, int instance, java.nio.FloatBuffer wet)
	{
		return FMOD.FMOD_Channel_GetReverbProperties(channel, instance, wet);
	}

	/**
	 * Retrieves the System that created this object.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_GetSystemObject(
	 *   FMOD_CHANNEL *channel,
	 *   FMOD_SYSTEM **system
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_GetSystemObject(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   FMOD_SYSTEM **system
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#createChannelGroup}, {@link FModSystem#getMasterChannelGroup}, {@link FModSystem#playSound}
	 * <p>
	 * 
	 * @param channel
	 * @param system <span>(<i>Out</i>) <dd>System object. (<a href="https://www.fmod.com/docs/2.03/api/core-api-system.html">System</a>)
	 */
	public static int getSystemObject(long channel, org.lwjgl.PointerBuffer system)
	{
		return FMOD.FMOD_Channel_GetSystemObject(channel, system);
	}

	/**
	 * Retrieves a user value associated with this object.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_GetUserData(
	 *   FMOD_CHANNEL *channel,
	 *   void **userdata
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_GetUserData(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   void **userdata
	 * );
	 * </code></pre>
	 * <p>
	 * This function allows arbitrary user data to be retrieved from this object. See the
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#user-data">User Data</a> section of the glossary for an example of how to get and set user data.
	 * <p>
	 * 
	 * @param channel
	 * @param userdata <span>(<i>Out</i>) <dd>User data set by calling {@link FModChannel#setUserData}.
	 */
	public static int getUserData(long channel, org.lwjgl.PointerBuffer userdata)
	{
		return FMOD.FMOD_Channel_GetUserData(channel, userdata);
	}

	/**
	 * Retrieves the volume level.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_GetVolume(
	 *   FMOD_CHANNEL *channel,
	 *   float *volume
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_GetVolume(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   float *volume
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#setVolume}
	 * <p>
	 * 
	 * @param channel
	 * @param volume <span>(<i>Out</i>) <dd><p>Volume level.</p><ul><li><span class="label">Units:</span> Linear</li><li><span class="label">Range:</span> (-inf, inf)</li><li><span class="label">Default:</span> 1</li></ul>
	 */
	public static int getVolume(long channel, java.nio.FloatBuffer volume)
	{
		return FMOD.FMOD_Channel_GetVolume(channel, volume);
	}

	/**
	 * Retrieves whether volume changes are ramped or instantaneous.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_GetVolumeRamp(
	 *   FMOD_CHANNEL *channel,
	 *   FMOD_BOOL *ramp
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_GetVolumeRamp(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   FMOD_BOOL *ramp
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#setVolumeRamp}
	 * <p>
	 * 
	 * @param channel
	 * @param ramp <span>(<i>Out</i>) <dd><p>Ramp state.  True = volume change is ramped.  False = volume change is instantaeous.</p><ul><li><span class="label">Units:</span> Boolean</li><li><span class="label">Default:</span> True</li></ul>
	 */
	public static int getVolumeRamp(long channel, java.nio.IntBuffer ramp)
	{
		return FMOD.FMOD_Channel_GetVolumeRamp(channel, ramp);
	}

	/**
	 * Retrieves the playing state.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_IsPlaying(
	 *   FMOD_CHANNEL *channel,
	 *   FMOD_BOOL *isplaying
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_IsPlaying(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   FMOD_BOOL *isplaying
	 * );
	 * </code></pre>
	 * <p>
	 * A
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a> is considered playing after {@link FModSystem#playSound} or {@link FModSystem#playDSP}, even if it is paused.
	 * <p>
	 * A
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a> is considered playing if it has any playing Channels.
	 * <p>
	 * 
	 * @param channel
	 * @param isplaying <span>(<i>Out</i>) <dd><p>Playing state.</p><ul><li><span class="label">Units:</span> Boolean</li></ul>
	 */
	public static int isPlaying(long channel, java.nio.IntBuffer isplaying)
	{
		return FMOD.FMOD_Channel_IsPlaying(channel, isplaying);
	}

	/**
	 * Retrieves whether the Channel is being emulated by the virtual voice system.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_IsVirtual(
	 *   FMOD_CHANNEL *channel,
	 *   FMOD_BOOL *isvirtual
	 * );
	 * </code></pre>
	 * <p>
	 * See the
	 * <a href="https://www.fmod.com/docs/2.03/api/managing-resources-in-the-core-api.html#virtual-voice-system">Virtual Voice System</a> section of the <a href="https://www.fmod.com/docs/2.03/api/managing-resources-in-the-core-api.html">Managing Resources in the Core API</a> chapter for more information.
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#getAudibility}
	 * <p>
	 * 
	 * @param channel
	 * @param isvirtual <span>(<i>Out</i>) <dd><p>Virtual state. True = silent / emulated. False = audible / real.</p><ul><li><span class="label">Units:</span> Boolean</li></ul>
	 */
	public static int isVirtual(long channel, java.nio.IntBuffer isvirtual)
	{
		return FMOD.FMOD_Channel_IsVirtual(channel, isvirtual);
	}

	/**
	 * Removes the specified
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP unit</a> from the <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-chain">DSP chain</a>.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_RemoveDSP(
	 *   FMOD_CHANNEL *channel,
	 *   FMOD_DSP *dsp
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_RemoveDSP(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   FMOD_DSP *dsp
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#addDSP}, {@link FModChannel#getDSP}, {@link FModChannel#getNumDSPs}
	 * <p>
	 * 
	 * @param channel
	 * @param dsp <dd><a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP unit</a> to be removed. (<a href="https://www.fmod.com/docs/2.03/api/core-api-dsp.html">DSP</a>)
	 */
	public static int removeDSP(long channel, long dsp)
	{
		return FMOD.FMOD_Channel_RemoveDSP(channel, dsp);
	}

	/**
	 * Removes all fade points between the two specified clock values (inclusive).
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_RemoveFadePoints(
	 *   FMOD_CHANNEL *channel,
	 *   unsigned long long dspclock_start,
	 *   unsigned long long dspclock_end
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_RemoveFadePoints(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   unsigned long long dspclock_start,
	 *   unsigned long long dspclock_end
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#addFadePoint}, {@link FModChannel#setFadePointRamp}, {@link FModChannel#getFadePoints}
	 * <p>
	 * 
	 * @param channel
	 * @param dspclock_start <dd><p><a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-clock">DSP clock</a> of the parent <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a> at which to begin removing fade points.</p><ul><li><span class="label">Units:</span> Samples</li></ul>
	 * @param dspclock_end <dd><p><a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-clock">DSP clock</a> of the parent <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a> at which to stop removing fade points.</p><ul><li><span class="label">Units:</span> Samples</li></ul>
	 */
	public static int removeFadePoints(long channel, long dspclock_start, long dspclock_end)
	{
		return FMOD.FMOD_Channel_RemoveFadePoints(channel, dspclock_start, dspclock_end);
	}

	/**
	 * Sets the 3D position and velocity used to apply panning, attenuation and doppler.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_Set3DAttributes(
	 *   FMOD_CHANNEL *channel,
	 *   const FMOD_VECTOR *pos,
	 *   const FMOD_VECTOR *vel
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_Set3DAttributes(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   const FMOD_VECTOR *pos,
	 *   const FMOD_VECTOR *vel
	 * );
	 * </code></pre>
	 * <p>
	 * The
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d">FMOD_3D</a> flag must be set on this object otherwise <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_err_needs3d">FMOD_ERR_NEEDS3D</a> is returned.
	 * <p>
	 * Vectors must be provided in the correct
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#handedness">handedness</a>.
	 * <p>
	 * For a stereo 3D sound, you can set the spread of the left/right parts in speaker space by using
	 * {@link FModChannel#set3DSpread}.
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#get3DAttributes}
	 * <p>
	 * 
	 * @param channel
	 * @param pos <span>(<i>Opt</i>) <dd><p>Position in 3D space used for panning and attenuation. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_vector">FMOD_VECTOR</a>)</p><ul><li><span class="label">Units:</span> <a href="https://www.fmod.com/docs/2.03/api/glossary.html#distance-units">Distance units</a></li></ul>
	 * @param vel <span>(<i>Opt</i>) <dd><p>Velocity in 3D space used for doppler. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_vector">FMOD_VECTOR</a>)</p><ul><li><span class="label">Units:</span> <a href="https://www.fmod.com/docs/2.03/api/glossary.html#distance-units">Distance units</a> per second</li></ul>
	 */
	public static int set3DAttributes(long channel, org.lwjgl.fmod.FMOD_VECTOR pos, org.lwjgl.fmod.FMOD_VECTOR vel)
	{
		return FMOD.FMOD_Channel_Set3DAttributes(channel, pos, vel);
	}

	/**
	 * Sets the orientation of a 3D cone shape, used for simulated occlusion.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_Set3DConeOrientation(
	 *   FMOD_CHANNEL *channel,
	 *   FMOD_VECTOR *orientation
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_Set3DConeOrientation(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   FMOD_VECTOR *orientation
	 * );
	 * </code></pre>
	 * <p>
	 * The
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d">FMOD_3D</a> flag must be set on this object otherwise <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_err_needs3d">FMOD_ERR_NEEDS3D</a> is returned.
	 * <p>
	 * This function has no effect unless
	 * {@link FModChannel#set3DConeSettings} has been used to change the cone inside/outside angles from the default.
	 * <p>
	 * Vectors must be provided in the correct
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#handedness">handedness</a>.
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#get3DConeOrientation}
	 * <p>
	 * 
	 * @param channel
	 * @param orientation <dd><p>Normalized orientation vector, which represents the direction of the sound cone. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_vector">FMOD_VECTOR</a>)</p><ul><li><span class="label">Default:</span> (0, 0, 1)</li></ul>
	 */
	public static int set3DConeOrientation(long channel, org.lwjgl.fmod.FMOD_VECTOR orientation)
	{
		return FMOD.FMOD_Channel_Set3DConeOrientation(channel, orientation);
	}

	/**
	 * Sets the angles and attenuation levels of a 3D cone shape, for simulated occlusion which is based on direction.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_Set3DConeSettings(
	 *   FMOD_CHANNEL *channel,
	 *   float insideconeangle,
	 *   float outsideconeangle,
	 *   float outsidevolume
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_Set3DConeSettings(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   float insideconeangle,
	 *   float outsideconeangle,
	 *   float outsidevolume
	 * );
	 * </code></pre>
	 * <p>
	 * The
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d">FMOD_3D</a> flag must be set on this object otherwise <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_err_needs3d">FMOD_ERR_NEEDS3D</a> is returned.
	 * <p>
	 * When
	 * {@link FModChannel#set3DConeOrientation} is used and a 3D 'cone' is set up, attenuation will automatically occur for a sound based on the relative angle of the direction the cone is facing, vs the angle between the sound and the listener.
	 * <p>
	 * <ul><li>If the relative angle is within the <code>insideconeangle</code>, the sound will not have any attenuation applied.</li><li>If the relative angle is between the <code>insideconeangle</code> and <code>outsideconeangle</code>, linear volume attenuation (between 1 and <code>outsidevolume</code>) is applied between the two angles until it reaches the <code>outsideconeangle</code>.</li><li>If the relative angle is outside of the <code>outsideconeangle</code> the volume does not attenuate any further.</li></ul>
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#get3DConeSettings}, {@link FModSound#set3DConeSettings}
	 * <p>
	 * 
	 * @param channel
	 * @param insideConeAngle <dd><p>Inside cone angle. This is the angle spread within which the sound is unattenuated.</p><ul><li><span class="label">Units:</span> Degrees</li><li><span class="label">Range:</span> [0, <code>outsideconeangle</code>]</li><li><span class="label">Default:</span> 360</li></ul>
	 * @param outsideConeAngle <dd><p>Outside cone angle. This is the angle spread outside of which the sound is attenuated to its <code>outsidevolume</code>.</p><ul><li><span class="label">Units:</span> Degrees</li><li><span class="label">Range:</span> [<code>insideconeangle</code>, 360]</li><li><span class="label">Default:</span> 360</li></ul>
	 * @param outsideVolume <dd><p>Cone outside volume.</p><ul><li><span class="label">Units:</span> Linear</li><li><span class="label">Range:</span> [0, 1]</li><li><span class="label">Default:</span> 1</li></ul>
	 */
	public static int set3DConeSettings(long channel, float insideConeAngle, float outsideConeAngle, float outsideVolume)
	{
		return FMOD.FMOD_Channel_Set3DConeSettings(channel, insideConeAngle, outsideConeAngle, outsideVolume);
	}

	/**
	 * Sets a custom roll-off shape for 3D distance attenuation.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_Set3DCustomRolloff(
	 *   FMOD_CHANNEL *channel,
	 *   FMOD_VECTOR *points,
	 *   int numpoints
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_Set3DCustomRolloff(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   FMOD_VECTOR *points,
	 *   int numpoints
	 * );
	 * </code></pre>
	 * <p>
	 * Must be used in conjunction with
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d_customrolloff">FMOD_3D_CUSTOMROLLOFF</a> flag to be activated.
	 * <p>
	 * This function does not duplicate the memory for the points internally. The memory you pass to FMOD must remain valid while in use.
	 * <p>
	 * If
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d_customrolloff">FMOD_3D_CUSTOMROLLOFF</a> is set and the roll-off shape is not set, FMOD will revert to <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d_inverserolloff">FMOD_3D_INVERSEROLLOFF</a> roll-off mode.
	 * <p>
	 * When a custom roll-off is specified a
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a> or <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a>'s 3D 'minimum' and 'maximum' distances are ignored.
	 * <p>
	 * The distance in-between point values is linearly interpolated until the final point where the last value is held.
	 * <p>
	 * If the points are not sorted by distance, an error will result.
	 * <p>
	 * <div class="highlight language-c-cpp" style="display: block;"><pre>// Defining a custom array of pointsFMOD_VECTOR curve[3] ={    { 0.0f,  1.0f, 0.0f },    { 2.0f,  0.2f, 0.0f },    { 20.0f, 0.0f, 0.0f }};</pre></div>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSound#set3DCustomRolloff}, {@link FModChannel#get3DCustomRolloff}
	 * <p>
	 * 
	 * @param channel
	 * @param points <dd><p>Array of vectors sorted by distance, where <code>x</code> = distance and <code>y</code> = volume from 0 to 1. <code>z</code> should be set to 0.  Pass null or equivalen to disable custom rolloff. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_vector">FMOD_VECTOR</a>)</p><ul><li><span class="label">Units:</span> <a href="https://www.fmod.com/docs/2.03/api/glossary.html#distance-units">Distance units</a></li></ul>
	 */
	public static int set3DCustomRolloff(long channel, org.lwjgl.fmod.FMOD_VECTOR.Buffer points)
	{
		return FMOD.FMOD_Channel_Set3DCustomRolloff(channel, points);
	}

	/**
	 * Sets an override value for the 3D distance filter.
	 * <p>
	 * If distance filtering is enabled, by default the 3D engine will automatically attenuate frequencies using a lowpass and a highpass filter, based on 3D distance.
	 * <br>This function allows the distance filter effect to be set manually, or to be set back to 'automatic' mode.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_Set3DDistanceFilter(
	 *   FMOD_CHANNEL *channel,
	 *   FMOD_BOOL custom,
	 *   float customLevel,
	 *   float centerFreq
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_Set3DDistanceFilter(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   FMOD_BOOL custom,
	 *   float customLevel,
	 *   float centerFreq
	 * );
	 * </code></pre>
	 * <p>
	 * The
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d">FMOD_3D</a> flag must be set on this object otherwise <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_err_needs3d">FMOD_ERR_NEEDS3D</a> is returned.
	 * <p>
	 * The
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html">System</a> must be initialized with <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_init_channel_distancefilter">FMOD_INIT_CHANNEL_DISTANCEFILTER</a> for this feature to work.
	 * <p>
	 * <div class="admonition warning">
	 * <p>
	 * Currently only supported for Channel, not ChannelGroup.
	 * <p>
	 * </div>
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#get3DDistanceFilter}
	 * <p>
	 * 
	 * @param channel
	 * @param custom <dd><p>Override automatic distance filtering and use <code>customLevel</code> instead.</p><ul><li><span class="label">Units:</span> Boolean</li><li><span class="label">Default:</span> False</li></ul>
	 * @param customLevel <dd><p>Attenuation factor where 1 represents no attenuation and 0 represents complete attenuation.</p><ul><li><span class="label">Range:</span> [0, 1]</li><li><span class="label">Default:</span> 1</li></ul>
	 * @param centerFreq <span>(<i>Opt</i>) <dd><p>Center frequency of the band-pass filter used to simulate distance attenuation, 0 for default.</p><ul><li><span class="label">Units:</span> Hertz</li><li><span class="label">Range:</span> [10, 22050]</li><li><span class="label">Default:</span> <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_advancedsettings_distancefiltercenterfreq">FMOD_ADVANCEDSETTINGS::distanceFilterCenterFreq</a></li></ul>
	 */
	public static int set3DDistanceFilter(long channel, int custom, float customLevel, float centerFreq)
	{
		return FMOD.FMOD_Channel_Set3DDistanceFilter(channel, custom, customLevel, centerFreq);
	}

	/**
	 * Sets the amount by which doppler is scaled.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_Set3DDopplerLevel(
	 *   FMOD_CHANNEL *channel,
	 *   float level
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_Set3DDopplerLevel(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   float level
	 * );
	 * </code></pre>
	 * <p>
	 * The
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d">FMOD_3D</a> flag must be set on this object otherwise <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_err_needs3d">FMOD_ERR_NEEDS3D</a> is returned.
	 * <p>
	 * The doppler effect will disabled if
	 * {@link FModSystem#set3DNumListeners} is given a value greater than 1.
	 * <p>
	 * <div class="admonition warning">
	 * <p>
	 * Currently only supported for Channel, not ChannelGroup.
	 * <p>
	 * </div>
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#get3DDopplerLevel}, {@link FModSystem#set3DSettings}
	 * <p>
	 * 
	 * @param channel
	 * @param level <dd><p>Doppler scale where 0 represents no doppler, 1 represents natural doppler and 5 represents exaggerated doppler.</p><ul><li><span class="label">Range:</span> [0, 5]</li><li><span class="label">Default:</span> 1</li></ul>
	 */
	public static int set3DDopplerLevel(long channel, float level)
	{
		return FMOD.FMOD_Channel_Set3DDopplerLevel(channel, level);
	}

	/**
	 * Sets the blend between 3D panning and 2D panning.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_Set3DLevel(
	 *   FMOD_CHANNEL *channel,
	 *   float level
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_Set3DLevel(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   float level
	 * );
	 * </code></pre>
	 * <p>
	 * The
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d">FMOD_3D</a> flag must be set on this object otherwise <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_err_needs3d">FMOD_ERR_NEEDS3D</a> is returned.
	 * <p>
	 * 2D functions include:
	 * <p>
	 * <ul><li>{@link FModChannel#setPan}</li><li>{@link FModChannel#setMixLevelsOutput}</li><li>{@link FModChannel#setMixLevelsInput}</li><li>{@link FModChannel#setMixMatrix}</li></ul>
	 * <p>
	 * 3D functions include:
	 * <p>
	 * <ul><li>{@link FModChannel#set3DAttributes}</li><li>{@link FModChannel#set3DConeOrientation}</li><li>{@link FModChannel#set3DCustomRolloff}</li></ul>
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#get3DLevel}
	 * <p>
	 * 
	 * @param channel
	 * @param level <dd><p>3D pan level where 0 represents panning/attenuating solely with 2D panning functions and 1 represents solely 3D.</p><ul><li><span class="label">Units:</span> Linear</li><li><span class="label">Range:</span> [0, 1]</li><li><span class="label">Default:</span> 1</li></ul>
	 */
	public static int set3DLevel(long channel, float level)
	{
		return FMOD.FMOD_Channel_Set3DLevel(channel, level);
	}

	/**
	 * Sets the minimum and maximum distances used to calculate the 3D roll-off attenuation.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_Set3DMinMaxDistance(
	 *   FMOD_CHANNEL *channel,
	 *   float mindistance,
	 *   float maxdistance
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_Set3DMinMaxDistance(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   float mindistance,
	 *   float maxdistance
	 * );
	 * </code></pre>
	 * <p>
	 * When the listener is within the minimum distance of the sound source the 3D volume will be at its maximum. As the listener moves from the minimum distance to the maximum distance the sound will attenuate following the roll-off curve set. When outside the maximum distance the sound will no longer attenuate.
	 * <p>
	 * Attenuation in 3D space is controlled by the roll-off mode, these are
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d_inverserolloff">FMOD_3D_INVERSEROLLOFF</a>, <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d_linearrolloff">FMOD_3D_LINEARROLLOFF</a>, <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d_linearsquarerolloff">FMOD_3D_LINEARSQUAREROLLOFF</a>, <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d_inversetaperedrolloff">FMOD_3D_INVERSETAPEREDROLLOFF</a>, <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d_customrolloff">FMOD_3D_CUSTOMROLLOFF</a>.
	 * <p>
	 * Minimum distance is useful to give the impression that the sound is loud or soft in 3D space.
	 * <br>A sound with a small 3D minimum distance in a typical (non custom) roll-off mode will make the sound appear small, and the sound will attenuate quickly.<br>A sound with a large minimum distance will make the sound appear larger.
	 * <p>
	 * The
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d">FMOD_3D</a> flag must be set on this object otherwise <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_err_needs3d">FMOD_ERR_NEEDS3D</a> is returned.
	 * <p>
	 * To define the min and max distance per
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html">Sound</a> instead of <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a> or <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a> use {@link FModSound#set3DMinMaxDistance}.
	 * <p>
	 * If
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d_customrolloff">FMOD_3D_CUSTOMROLLOFF</a> has been set on this object these values are stored, but ignored in 3D processing.
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#get3DMinMaxDistance}
	 * <p>
	 * 
	 * @param channel
	 * @param mindistance <dd><p>Distance from the source where attenuation begins.</p><ul><li><span class="label">Units:</span> <a href="https://www.fmod.com/docs/2.03/api/glossary.html#distance-units">Distance units</a></li><li><span class="label">Range:</span> [0, <code>maxdistance</code>]</li><li><span class="label">Default:</span> 1</li></ul>
	 * @param maxdistance <dd><p>Distance from the source where attenuation ends.</p><ul><li><span class="label">Units:</span> <a href="https://www.fmod.com/docs/2.03/api/glossary.html#distance-units">Distance units</a></li><li><span class="label">Range:</span> [<code>mindistance</code>, inf)</li><li><span class="label">Default:</span> 10000</li></ul>
	 */
	public static int set3DMinMaxDistance(long channel, float mindistance, float maxdistance)
	{
		return FMOD.FMOD_Channel_Set3DMinMaxDistance(channel, mindistance, maxdistance);
	}

	/**
	 * Sets the 3D attenuation factors for the direct and reverb paths.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_Set3DOcclusion(
	 *   FMOD_CHANNEL *channel,
	 *   float directocclusion,
	 *   float reverbocclusion
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_Set3DOcclusion(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   float directocclusion,
	 *   float reverbocclusion
	 * );
	 * </code></pre>
	 * <p>
	 * There is a reverb path/send when
	 * {@link FModChannel#setReverbProperties} has been used, <code>reverbocclusion</code> controls its attenuation.
	 * <p>
	 * If the
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html">System</a> has been initialized with <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_init_channel_distancefilter">FMOD_INIT_CHANNEL_DISTANCEFILTER</a> or <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_init_channel_lowpass">FMOD_INIT_CHANNEL_LOWPASS</a> the <code>directocclusion</code> is applied as frequency filtering rather than volume attenuation.
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#get3DOcclusion}
	 * <p>
	 * 
	 * @param channel
	 * @param directocclusion <dd><p>Occlusion factor for the direct path where 0 represents no occlusion and 1 represents full occlusion.</p><ul><li><span class="label">Range:</span> [0, 1]</li><li><span class="label">Default:</span> 0</li></ul>
	 * @param reverbocclusion <dd><p>Occlusion factor for the reverb path where 0 represents no occlusion and 1 represents full occlusion.</p><ul><li><span class="label">Range:</span> [0, 1]</li><li><span class="label">Default:</span> 0</li></ul>
	 */
	public static int set3DOcclusion(long channel, float directocclusion, float reverbocclusion)
	{
		return FMOD.FMOD_Channel_Set3DOcclusion(channel, directocclusion, reverbocclusion);
	}

	/**
	 * Sets the spread of a 3D sound in speaker space.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_Set3DSpread(
	 *   FMOD_CHANNEL *channel,
	 *   float angle
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_Set3DSpread(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   float angle
	 * );
	 * </code></pre>
	 * <p>
	 * When the spread angle is 0 (default) a multi-channel
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#signal">signal</a> will collapse to mono and be spatialized to a single point based on {@link FModChannel#set3DAttributes} calculations. As the angle is increased, each channel within a multi-channel signal will be rotated away from that point. For 2, 4, 6, 8, and 12 channel signals, the spread is arranged from leftmost speaker to rightmost speaker intelligently, for example in 5.1 the leftmost speaker is rear left, followed by front left, center, front right then finally rear right as the rightmost speaker (LFE is not spread). For other channel counts the individual channels are spread evenly in the order of the signal. As the signal is spread the power will be preserved.
	 * <p>
	 * For a stereo signal given different spread angles:
	 * <p>
	 * <ul><li>0:   Sound is collapsed to mono and spatialized to a single point.</li><li>90:  Left channel is rotated 45 degrees to the left compared with angle=0 and the right channel 45 degrees to the right.</li><li>180: Left channel is rotated 90 degrees to the left compared with angle=0 and the right channel 90 degrees to the right.</li><li>360: Left channel is rotated 180 degrees to the left and the right channel 180 degrees to the right. This means the sound is collapsed to mono and spatialized to a single point in the opposite direction compared with (angle=0).</li></ul>
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#get3DSpread}
	 * <p>
	 * 
	 * @param channel
	 * @param angle <dd><p>Angle over which the sound is spread.</p><ul><li><span class="label">Units:</span> Degrees</li><li><span class="label">Range:</span> [0, 360]</li><li><span class="label">Default:</span> 0</li></ul>
	 */
	public static int set3DSpread(long channel, float angle)
	{
		return FMOD.FMOD_Channel_Set3DSpread(channel, angle);
	}

	/**
	 * Sets the callback for ChannelControl level notifications.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_SetCallback(
	 *   FMOD_CHANNEL *channel,
	 *   FMOD_CHANNELCONTROL_CALLBACK callback
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_SetCallback(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   FMOD_CHANNELCONTROL_CALLBACK callback
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> <a href="https://www.fmod.com/docs/2.03/api/glossary.html#callback-behavior">Callback Behavior</a>, <a href="https://www.fmod.com/docs/2.03/api/core-api-channelcontrol.html#fmod_channelcontrol_callback_type">FMOD_CHANNELCONTROL_CALLBACK_TYPE</a>
	 * <p>
	 * 
	 * @param channel
	 * @param callback <dd>Callback to invoke. (<a href="https://www.fmod.com/docs/2.03/api/core-api-channelcontrol.html#fmod_channelcontrol_callback">FMOD_CHANNELCONTROL_CALLBACK</a>)
	 */
	public static int setCallback(long channel, org.lwjgl.fmod.FMOD_CHANNELCONTROL_CALLBACKI callback)
	{
		return FMOD.FMOD_Channel_SetCallback(channel, callback);
	}

	/**
	 * Sets the ChannelGroup this object outputs to.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_SetChannelGroup(
	 *   FMOD_CHANNEL *channel,
	 *   FMOD_CHANNELGROUP *channelgroup
	 * );
	 * </code></pre>
	 * <p>
	 * A
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a> may contain many <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s.
	 * <p>
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s may only output to a single <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a>.  This operation will remove it from the previous group first.
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#getChannelGroup}
	 * <p>
	 * 
	 * @param channel
	 * @param channelgroup <dd>Output group. (<a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a>)
	 */
	public static int setChannelGroup(long channel, long channelgroup)
	{
		return FMOD.FMOD_Channel_SetChannelGroup(channel, channelgroup);
	}

	/**
	 * Sets the index in the
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-chain">DSP chain</a> of the specified <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP</a>.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_SetDSPIndex(
	 *   FMOD_CHANNEL *channel,
	 *   FMOD_DSP *dsp,
	 *   int index
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_SetDSPIndex(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   FMOD_DSP *dsp,
	 *   int index
	 * );
	 * </code></pre>
	 * <p>
	 * This will move a
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-dsp.html">DSP</a> already in the <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-chain">DSP chain</a> to a new offset.
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#getDSPIndex}
	 * <p>
	 * 
	 * @param channel
	 * @param dsp <dd>A <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP unit</a> that exists in the <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-chain">DSP chain</a>. (<a href="https://www.fmod.com/docs/2.03/api/core-api-dsp.html">DSP</a>)
	 * @param index <dd>Offset into the <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-chain">DSP chain</a> to move the <a href="https://www.fmod.com/docs/2.03/api/core-api-dsp.html">DSP</a> to, see <a href="https://www.fmod.com/docs/2.03/api/core-api-channelcontrol.html#fmod_channelcontrol_dsp_index">FMOD_CHANNELCONTROL_DSP_INDEX</a> for special named offsets.
	 */
	public static int setDSPIndex(long channel, long dsp, int index)
	{
		return FMOD.FMOD_Channel_SetDSPIndex(channel, dsp, index);
	}

	/**
	 * Sets a sample accurate start (and/or stop) time relative to the parent ChannelGroup
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-clock">DSP clock</a>.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_SetDelay(
	 *   FMOD_CHANNEL *channel,
	 *   unsigned long long dspclock_start,
	 *   unsigned long long dspclock_end,
	 *   FMOD_BOOL stopchannels
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_SetDelay(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   unsigned long long dspclock_start,
	 *   unsigned long long dspclock_end,
	 *   FMOD_BOOL stopchannels
	 * );
	 * </code></pre>
	 * <p>
	 * To perform sample accurate scheduling use
	 * {@link FModChannel#getDSPClock} to query the parent clock value. If a parent <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a> changes its pitch, the start and stop times will still be correct as the parent clock rate is adjusted by that pitch.
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#getDelay}
	 * <p>
	 * 
	 * @param channel
	 * @param dspclock_start <span>(<i>Opt</i>) <dd><p><a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-clock">DSP clock</a> of the parent <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a> to audibly start playing sound at.</p><ul><li><span class="label">Units:</span> Samples</li><li><span class="label">Default:</span> 0</li></ul>
	 * @param dspclock_end <span>(<i>Opt</i>) <dd><p><a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-clock">DSP clock</a> of the parent <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a> to audibly stop playing sound at.</p><ul><li><span class="label">Units:</span> Samples</li><li><span class="label">Default:</span> 0</li></ul>
	 * @param stopchannels <dd><p>True: When <code>dspclock_end</code> is reached, behaves like {@link FModChannel#stop} has been called.<br>False: When <code>dspclock_end</code> is reached, behaves like {@link FModChannel#setPaused} has been called, a subsequent <code>dspclock_start</code> allows it to resume.</p><ul><li><span class="label">Units:</span> Boolean</li></ul></dd>
	 */
	public static int setDelay(long channel, long dspclock_start, long dspclock_end, int stopchannels)
	{
		return FMOD.FMOD_Channel_SetDelay(channel, dspclock_start, dspclock_end, stopchannels);
	}

	/**
	 * Adds a volume ramp at the specified time in the future using fade points.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_SetFadePointRamp(
	 *   FMOD_CHANNEL *channel,
	 *   unsigned long long dspclock,
	 *   float volume
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_SetFadePointRamp(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   unsigned long long dspclock,
	 *   float volume
	 * );
	 * </code></pre>
	 * <p>
	 * This is a convenience function that creates a scheduled 64 sample fade point ramp from the current volume level to
	 * <code>volume</code> arriving at <code>dspclock</code> time.
	 * <p>
	 * Can be use in conjunction with
	 * {@link FModChannel#setDelay}.
	 * <p>
	 * All fade points after
	 * <code>dspclock</code> will be removed.
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#removeFadePoints}, {@link FModChannel#addFadePoint}, {@link FModChannel#getFadePoints}
	 * <p>
	 * 
	 * @param channel
	 * @param dspclock <dd><p>Time at which the ramp will end, as measured by the <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp-clock">DSP clock</a> of the parent <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a>.</p><ul><li><span class="label">Units:</span> Samples</li></ul>
	 * @param volume <dd><p>Volume level at the given <code>dspclock</code>.  0 = silent, 1 = full.</p><ul><li><span class="label">Units:</span> Linear</li><li><span class="label">Range:</span> [0, inf)</li></ul>
	 */
	public static int setFadePointRamp(long channel, long dspclock, float volume)
	{
		return FMOD.FMOD_Channel_SetFadePointRamp(channel, dspclock, volume);
	}

	/**
	 * Sets the frequency or playback rate.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_SetFrequency(
	 *   FMOD_CHANNEL *channel,
	 *   float frequency
	 * );
	 * </code></pre>
	 * <p>
	 * Default frequency is determined by the audio format of the
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html">Sound</a> or <a href="https://www.fmod.com/docs/2.03/api/core-api-dsp.html">DSP</a>.
	 * <p>
	 * Sounds opened as
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_createsample">FMOD_CREATESAMPLE</a> (not <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_createstream">FMOD_CREATESTREAM</a> or <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_createcompressedsample">FMOD_CREATECOMPRESSEDSAMPLE</a>) can be played backwards by giving a negative frequency.
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#getFrequency}, {@link FModSound#setDefaults}
	 * <p>
	 * 
	 * @param channel
	 * @param frequency <dd><p>Playback rate.</p><ul><li><span class="label">Units:</span> Hertz</li></ul>
	 */
	public static int setFrequency(long channel, float frequency)
	{
		return FMOD.FMOD_Channel_SetFrequency(channel, frequency);
	}

	/**
	 * Sets the number of times to loop before stopping.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_SetLoopCount(
	 *   FMOD_CHANNEL *channel,
	 *   int loopcount
	 * );
	 * </code></pre>
	 * <p>
	 * The 'mode' of the
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html">Sound</a> or <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a> must be <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_loop_normal">FMOD_LOOP_NORMAL</a> or <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_loop_bidi">FMOD_LOOP_BIDI</a> for this function to work.
	 * <p>
	 * <strong>See Also:</strong> <a href="https://www.fmod.com/docs/2.03/api/glossary.html#streaming-issues">Streaming Issues</a>, {@link FModChannel#getLoopCount}, {@link FModChannel#setMode}, {@link FModSound#setMode}, {@link FModSystem#createSound}
	 * <p>
	 * 
	 * @param channel
	 * @param loopCount <dd>Times to loop before stopping where 0 represents "oneshot", 1 represents "loop once then stop" and -1 represents "loop forever".
	 */
	public static int setLoopCount(long channel, int loopCount)
	{
		return FMOD.FMOD_Channel_SetLoopCount(channel, loopCount);
	}

	/**
	 * Sets the loop start and end points.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_SetLoopPoints(
	 *   FMOD_CHANNEL *channel,
	 *   unsigned int loopstart,
	 *   FMOD_TIMEUNIT loopstarttype,
	 *   unsigned int loopend,
	 *   FMOD_TIMEUNIT loopendtype
	 * );
	 * </code></pre>
	 * <p>
	 * Loop points may only be set on a
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a> playing a <a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html">Sound</a>, not a <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a> playing a <a href="https://www.fmod.com/docs/2.03/api/core-api-dsp.html">DSP</a> (See {@link FModSystem#playDSP}).
	 * <p>
	 * Valid
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit">FMOD_TIMEUNIT</a> types are <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_pcm">FMOD_TIMEUNIT_PCM</a>, <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_ms">FMOD_TIMEUNIT_MS</a>, <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_pcmbytes">FMOD_TIMEUNIT_PCMBYTES</a>.  Any other time units return <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_err_format">FMOD_ERR_FORMAT</a>.<br>If <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_ms">FMOD_TIMEUNIT_MS</a> or <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_pcmbytes">FMOD_TIMEUNIT_PCMBYTES</a>, the value is internally converted to <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_pcm">FMOD_TIMEUNIT_PCM</a>.
	 * <p>
	 * The
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>'s mode must be set to <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_loop_normal">FMOD_LOOP_NORMAL</a> or <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_loop_bidi">FMOD_LOOP_BIDI</a> for loop points to affect playback.
	 * <p>
	 * <strong>See Also:</strong> <a href="https://www.fmod.com/docs/2.03/api/glossary.html#streaming-issues">Streaming Issues</a>, {@link FModChannel#getLoopPoints}, {@link FModChannel#setMode}
	 * <p>
	 * 
	 * @param channel
	 * @param loopStart <dd><p>Loop start point.</p><ul><li><span class="label">Range:</span> [0, {@link FModSound#getLength} - 1].</li></ul>
	 * @param loopStartType <dd>Time units for <code>loopstart</code>. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit">FMOD_TIMEUNIT</a>)
	 * @param loopEnd <dd><p>Loop end point.</p><ul><li>Range; [<code>loopstart</code> + 1, {@link FModSound#getLength} - 1].</li></ul>
	 * @param loopEndType <dd>Time units for <code>loopend</code>. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit">FMOD_TIMEUNIT</a>)
	 */
	public static int setLoopPoints(long channel, int loopStart, int loopStartType, int loopEnd, int loopEndType)
	{
		return FMOD.FMOD_Channel_SetLoopPoints(channel, loopStart, loopStartType, loopEnd, loopEndType);
	}

	/**
	 * Sets the gain of the dry
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#signal">signal</a> when built in lowpass / distance filtering is applied.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_SetLowPassGain(
	 *   FMOD_CHANNEL *channel,
	 *   float gain
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_SetLowPassGain(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   float gain
	 * );
	 * </code></pre>
	 * <p>
	 * Requires the built in lowpass to be created with
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_init_channel_lowpass">FMOD_INIT_CHANNEL_LOWPASS</a> or <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_init_channel_distancefilter">FMOD_INIT_CHANNEL_DISTANCEFILTER</a>.
	 * <p>
	 * <div class="admonition warning">
	 * <p>
	 * Currently only supported for Channel, not ChannelGroup.
	 * <p>
	 * </div>
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#getLowPassGain}
	 * <p>
	 * 
	 * @param channel
	 * @param gain <dd><p>gain level where 0 represents silent (full filtering) and 1 represents full volume (no filtering).</p><ul><li><span class="label">Units:</span> Linear</li><li><span class="label">Range:</span> [0, 1]</li><li><span class="label">Default:</span> 1</li></ul>
	 */
	public static int setLowPassGain(long channel, float gain)
	{
		return FMOD.FMOD_Channel_SetLowPassGain(channel, gain);
	}

	/**
	 * Sets the incoming volume level for each channel of a multi-channel
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#signal">signal</a>.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_SetMixLevelsInput(
	 *   FMOD_CHANNEL *channel,
	 *   float *levels,
	 *   int numlevels
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_SetMixLevelsInput(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   float *levels,
	 *   int numlevels
	 * );
	 * </code></pre>
	 * <p>
	 * This is a convenience function to avoid passing a matrix, it will overwrite values set via
	 * {@link FModChannel#setPan}, {@link FModChannel#setMixLevelsOutput} and {@link FModChannel#setMixMatrix}.
	 * <p>
	 * <div class="admonition warning">
	 * <p>
	 * Currently only supported for
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>, not <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a>.
	 * <p>
	 * </div>
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#getMixMatrix}
	 * <p>
	 * 
	 * @param channel
	 * @param levels <dd><p>Array of volume levels for each incoming channel. Volume level. 0 = silent, 1 = full. Negative level inverts the signal.  Values larger than 1 amplify the signal.</p><ul><li><span class="label">Units:</span> Linear</li><li><span class="label">Range:</span> (-inf, inf)</li></ul>
	 */
	public static int setMixLevelsInput(long channel, java.nio.FloatBuffer levels)
	{
		return FMOD.FMOD_Channel_SetMixLevelsInput(channel, levels);
	}

	/**
	 * Sets the outgoing volume levels for each speaker.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_SetMixLevelsOutput(
	 *   FMOD_CHANNEL *channel,
	 *   float frontleft,
	 *   float frontright,
	 *   float center,
	 *   float lfe,
	 *   float surroundleft,
	 *   float surroundright,
	 *   float backleft,
	 *   float backright
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_SetMixLevelsOutput(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   float frontleft,
	 *   float frontright,
	 *   float center,
	 *   float lfe,
	 *   float surroundleft,
	 *   float surroundright,
	 *   float backleft,
	 *   float backright
	 * );
	 * </code></pre>
	 * <p>
	 * Specify the level for a given output speaker, if the channel count of the input and output do not match, channels will be up/down mixed as appropriate to approximate the given speaker values. For example stereo input with 5.1 output will use the
	 * <code>center</code> parameter to distribute signal to the center speaker from front left and front right channels.
	 * <p>
	 * This is a convenience function to avoid passing a matrix, it will overwrite values set via
	 * {@link FModChannel#setPan}, {@link FModChannel#setMixLevelsInput} and {@link FModChannel#setMixMatrix}.
	 * <p>
	 * The output channel count will always match the System speaker mode set via
	 * {@link FModSystem#setSoftwareFormat}.
	 * <p>
	 * If the System is initialized with
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_speakermode_raw">FMOD_SPEAKERMODE_RAW</a> calling this function will produce silence.
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#getMixMatrix}
	 * <p>
	 * 
	 * @param channel
	 * @param frontleft <dd><p>Volume level for <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_speaker_front_left">FMOD_SPEAKER_FRONT_LEFT</a>. Volume level. 0 = silent, 1 = full. Negative level inverts the <a href="https://www.fmod.com/docs/2.03/api/glossary.html#signal">signal</a>.  Values larger than 1 amplify the signal.</p><ul><li><span class="label">Units:</span> Linear</li><li><span class="label">Range:</span> (-inf, inf)</li></ul>
	 * @param frontright <dd><p>Volume level for <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_speaker_front_right">FMOD_SPEAKER_FRONT_RIGHT</a>. Volume level. 0 = silent, 1 = full. Negative level inverts the signal.  Values larger than 1 amplify the signal.</p><ul><li><span class="label">Units:</span> Linear</li><li><span class="label">Range:</span> (-inf, inf)</li></ul>
	 * @param center <dd><p>Volume level for <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_speaker_front_center">FMOD_SPEAKER_FRONT_CENTER</a>. Volume level. 0 = silent, 1 = full. Negative level inverts the signal.  Values larger than 1 amplify the signal.</p><ul><li><span class="label">Units:</span> Linear</li><li><span class="label">Range:</span> (-inf, inf)</li></ul>
	 * @param lfe <dd><p>Volume level for <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_speaker_low_frequency">FMOD_SPEAKER_LOW_FREQUENCY</a>. Volume level. 0 = silent, 1 = full. Negative level inverts the signal.  Values larger than 1 amplify the signal.</p><ul><li><span class="label">Units:</span> Linear</li><li><span class="label">Range:</span> (-inf, inf)</li></ul>
	 * @param surroundleft <dd><p>Volume level for <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_speaker_surround_left">FMOD_SPEAKER_SURROUND_LEFT</a>. Volume level. 0 = silent, 1 = full. Negative level inverts the signal.  Values larger than 1 amplify the signal.</p><ul><li><span class="label">Units:</span> Linear</li><li><span class="label">Range:</span> (-inf, inf)</li></ul>
	 * @param surroundright <dd><p>Volume level for <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_speaker_surround_right">FMOD_SPEAKER_SURROUND_RIGHT</a>. Volume level. 0 = silent, 1 = full. Negative level inverts the signal.  Values larger than 1 amplify the signal.</p><ul><li><span class="label">Units:</span> Linear</li><li><span class="label">Range:</span> (-inf, inf)</li></ul>
	 * @param backleft <dd><p>Volume level for <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_speaker_back_left">FMOD_SPEAKER_BACK_LEFT</a>. Volume level. 0 = silent, 1 = full. Negative level inverts the signal.  Values larger than 1 amplify the signal.</p><ul><li><span class="label">Units:</span> Linear</li><li><span class="label">Range:</span> (-inf, inf)</li></ul>
	 * @param backright <dd><p>Volume level for <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_speaker_back_right">FMOD_SPEAKER_BACK_RIGHT</a>. Volume level. 0 = silent, 1 = full. Negative level inverts the signal.  Values larger than 1 amplify the signal.</p><ul><li><span class="label">Units:</span> Linear</li><li><span class="label">Range:</span> (-inf, inf)</li></ul>
	 */
	public static int setMixLevelsOutput(long channel, float frontleft, float frontright, float center, float lfe, float surroundleft, float surroundright, float backleft, float backright)
	{
		return FMOD.FMOD_Channel_SetMixLevelsOutput(channel, frontleft, frontright, center, lfe, surroundleft, surroundright, backleft, backright);
	}

	/**
	 * Sets a two-dimensional pan matrix that maps the
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#signal">signal</a> from input channels (columns) to output speakers (rows).
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_SetMixMatrix(
	 *   FMOD_CHANNEL *channel,
	 *   float *matrix,
	 *   int outchannels,
	 *   int inchannels,
	 *   int inchannel_hop
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_SetMixMatrix(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   float *matrix,
	 *   int outchannels,
	 *   int inchannels,
	 *   int inchannel_hop
	 * );
	 * </code></pre><ul><li>Range: [0, <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_max_channel_width">FMOD_MAX_CHANNEL_WIDTH</a>]</li><li>Default: <code>inchannels</code></li></ul>
	 * <p>
	 * This will overwrite values set via
	 * {@link FModChannel#setPan}, {@link FModChannel#setMixLevelsInput} and {@link FModChannel#setMixLevelsOutput}.
	 * <p>
	 * If no matrix is passed in via
	 * <code>matrix</code> a default upmix, downmix, or unit matrix will take its place.  A unit matrix allows a signal to pass through unchanged.
	 * <p>
	 * Example 5.1 unit matrix:
	 * <p>
	 * <pre><code>1 0 0 0 0 0 0 1 0 0 0 0 0 0 1 0 0 0 0 0 0 1 0 0 0 0 0 0 1 0 0 0 0 0 0 1</code></pre>
	 * <p>
	 * Matrix element values can be below 0 to invert a signal and above 1 to amplify the signal. Note that increasing the signal level too far may cause audible distortion.
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#getMixMatrix}
	 * <p>
	 * 
	 * @param channel
	 * @param matrix <span>(<i>Opt</i>) <dd><p>Two dimensional array of volume levels in row-major order. Each row represents an output speaker, each column represents an input channel.</p><ul><li><span class="label">Units:</span> Linear</li><li><span class="label">Range:</span> (-inf, inf)</li></ul>
	 * @param outchannels <dd><p>Number of output channels (rows) in <code>matrix</code>.</p><ul><li><span class="label">Range:</span> [0, <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_max_channel_width">FMOD_MAX_CHANNEL_WIDTH</a>]</li></ul>
	 * @param inchannels <dd><p>Number of input channels (columns) in <code>matrix</code>.</p><ul><li><span class="label">Range:</span> [0, <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_max_channel_width">FMOD_MAX_CHANNEL_WIDTH</a>]</li></ul>
	 * @param inchannel_hop <span>(<i>Opt</i>) <dd>Width (total number of columns) in source <code>matrix</code>. A matrix element is referenced as 'outchannel * inchannel_hop + inchannel'.
	 */
	public static int setMixMatrix(long channel, java.nio.FloatBuffer matrix, int outchannels, int inchannels, int inchannel_hop)
	{
		return FMOD.FMOD_Channel_SetMixMatrix(channel, matrix, outchannels, inchannels, inchannel_hop);
	}

	/**
	 * Sets the playback mode that controls how this object behaves.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_SetMode(
	 *   FMOD_CHANNEL *channel,
	 *   FMOD_MODE mode
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_SetMode(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   FMOD_MODE mode
	 * );
	 * </code></pre>
	 * <p>
	 * Modes supported:
	 * <p>
	 * <ul><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_loop_off">FMOD_LOOP_OFF</a></li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_loop_normal">FMOD_LOOP_NORMAL</a></li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_loop_bidi">FMOD_LOOP_BIDI</a></li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_2d">FMOD_2D</a></li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d">FMOD_3D</a></li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d_headrelative">FMOD_3D_HEADRELATIVE</a></li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d_worldrelative">FMOD_3D_WORLDRELATIVE</a></li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d_inverserolloff">FMOD_3D_INVERSEROLLOFF</a></li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d_linearrolloff">FMOD_3D_LINEARROLLOFF</a></li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d_linearsquarerolloff">FMOD_3D_LINEARSQUAREROLLOFF</a></li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d_inversetaperedrolloff">FMOD_3D_INVERSETAPEREDROLLOFF</a></li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d_customrolloff">FMOD_3D_CUSTOMROLLOFF</a></li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d_ignoregeometry">FMOD_3D_IGNOREGEOMETRY</a></li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_virtual_playfromstart">FMOD_VIRTUAL_PLAYFROMSTART</a></li></ul>
	 * <p>
	 * When changing the loop mode, sounds created with
	 * {@link FModSystem#createStream} or <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_createstream">FMOD_CREATESTREAM</a> may have already been pre-buffered and executed their loop logic ahead of time before this call was even made. This is dependent on the size of the sound versus the size of the stream <em>decode</em> buffer (see <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_createsoundexinfo">FMOD_CREATESOUNDEXINFO</a>). If this happens, you may need to reflush the stream buffer by calling {@link FModChannel#setPosition}. Note this will usually only happen if you have sounds or loop points that are smaller than the stream decode buffer size.
	 * <p>
	 * When changing the loop mode of sounds created with with
	 * {@link FModSystem#createSound} or <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_createsample">FMOD_CREATESAMPLE</a>, if the sound was set up as <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_loop_off">FMOD_LOOP_OFF</a>, then set to <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_loop_normal">FMOD_LOOP_NORMAL</a> with this function, the sound may click when playing the end of the sound. This is because the sound needs to be prepared for looping using {@link FModSound#setMode}, by modifying the content of the PCM data (i.e. data past the end of the actual sample data) to allow the interpolators to read ahead without clicking. If you use {@link FModChannel#setMode} it will not do this (because different <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s may have different loop modes for the same sound) and may click if you try to set it to looping on an unprepared sound. If you want to change the loop mode at runtime it may be better to load the sound as looping first (or use {@link FModSound#setMode}), to let it prepare the data as if it was looping so that it does not click whenever {@link FModChannel#setMode} is used to turn looping on.
	 * <p>
	 * If
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d_ignoregeometry">FMOD_3D_IGNOREGEOMETRY</a> or <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_virtual_playfromstart">FMOD_VIRTUAL_PLAYFROMSTART</a> is not specified, the flag will be cleared if it was specified previously.
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#getMode}
	 * <p>
	 * 
	 * @param channel
	 * @param mode <dd><p>Playback mode. More than one mode can be set at once by combining them with the OR operator. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_mode">FMOD_MODE</a>)</p><ul><li><span class="label">Default:</span> <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_default">FMOD_DEFAULT</a></li></ul>
	 */
	public static int setMode(long channel, int mode)
	{
		return FMOD.FMOD_Channel_SetMode(channel, mode);
	}

	/**
	 * Sets the mute state.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_SetMute(
	 *   FMOD_CHANNEL *channel,
	 *   FMOD_BOOL mute
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_SetMute(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   FMOD_BOOL mute
	 * );
	 * </code></pre>
	 * <p>
	 * Mute is an additional control for volume, the effect of which is equivalent to setting the volume to zero.
	 * <p>
	 * An individual mute state is kept for each object, muting a parent
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a> will effectively mute this object however when queried the individual mute state is returned.  {@link FModChannel#getAudibility} can be used to calculate overall audibility for a <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a> or <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a>.
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#getMute}
	 * <p>
	 * 
	 * @param channel
	 * @param mute <dd><p>Mute state. True = silent.  False = audible.</p><ul><li><span class="label">Units:</span> Boolean</li></ul>
	 */
	public static int setMute(long channel, int mute)
	{
		return FMOD.FMOD_Channel_SetMute(channel, mute);
	}

	/**
	 * Sets the left/right pan level.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_SetPan(
	 *   FMOD_CHANNEL *channel,
	 *   float pan
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_SetPan(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   float pan
	 * );
	 * </code></pre>
	 * <p>
	 * This is a convenience function to avoid passing a matrix, it will overwrite values set via
	 * {@link FModChannel#setMixLevelsInput}, {@link FModChannel#setMixLevelsOutput} and {@link FModChannel#setMixMatrix}.
	 * <p>
	 * Mono inputs are panned from left to right using constant power panning (non linear fade). Stereo and greater inputs will isolate the front left and right input channels and fade them up and down based on the pan value (silencing other channels). The output channel count will always match the System speaker mode set via
	 * {@link FModSystem#setSoftwareFormat}.
	 * <p>
	 * If the System is initialized with
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_speakermode_raw">FMOD_SPEAKERMODE_RAW</a> calling this function will produce silence.
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#getMixMatrix}
	 * <p>
	 * 
	 * @param channel
	 * @param pan <dd><p>Pan level where -1 represents full left, 0 represents center and 1 represents full right.</p><ul><li><span class="label">Range:</span> [-1, 1]</li><li><span class="label">Default:</span> 0</li></ul>
	 */
	public static int setPan(long channel, float pan)
	{
		return FMOD.FMOD_Channel_SetPan(channel, pan);
	}

	/**
	 * Sets the paused state.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_SetPaused(
	 *   FMOD_CHANNEL *channel,
	 *   FMOD_BOOL paused
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_SetPaused(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   FMOD_BOOL paused
	 * );
	 * </code></pre>
	 * <p>
	 * Pause halts playback which effectively freezes
	 * {@link FModChannel#getPosition} and {@link FModChannel#getDSPClock} values.
	 * <p>
	 * An individual pause state is kept for each object, pausing a parent
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a> will effectively pause this object however when queried the individual pause state is returned.
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#getPaused}
	 * <p>
	 * 
	 * @param channel
	 * @param paused <dd><p>Paused state.  True = playback halted.  False = playback active.</p><ul><li><span class="label">Units:</span> Boolean</li></ul>
	 */
	public static int setPaused(long channel, int paused)
	{
		return FMOD.FMOD_Channel_SetPaused(channel, paused);
	}

	/**
	 * Sets the relative pitch / playback rate.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_SetPitch(
	 *   FMOD_CHANNEL *channel,
	 *   float pitch
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_SetPitch(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   float pitch
	 * );
	 * </code></pre>
	 * <p>
	 * Scales playback frequency of
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a> object or if issued on a <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a> it scales the frequencies of all <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s contained in the <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a>.
	 * <p>
	 * An individual pitch value is kept for each object, changing the pitch of a parent
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a> will effectively alter the pitch of this object however when queried the individual pitch value is returned.
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#getPitch}, {@link FModChannel#setFrequency}, {@link FModChannel#getFrequency}
	 * <p>
	 * 
	 * @param channel
	 * @param pitch <dd>Pitch value where 0.5 represents half pitch (one octave down), 1.0 represents unmodified pitch and 2.0 represents double pitch (one octave up).
	 */
	public static int setPitch(long channel, float pitch)
	{
		return FMOD.FMOD_Channel_SetPitch(channel, pitch);
	}

	/**
	 * Sets the current playback position.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_SetPosition(
	 *   FMOD_CHANNEL *channel,
	 *   unsigned int position,
	 *   FMOD_TIMEUNIT postype
	 * );
	 * </code></pre>
	 * <p>
	 * Certain
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit">FMOD_TIMEUNIT</a> types are always available: <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_pcm">FMOD_TIMEUNIT_PCM</a>, <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_pcmbytes">FMOD_TIMEUNIT_PCMBYTES</a> and <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_ms">FMOD_TIMEUNIT_MS</a>. The others are format specific such as <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_modorder">FMOD_TIMEUNIT_MODORDER</a> / <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_modrow">FMOD_TIMEUNIT_MODROW</a> / <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_modpattern">FMOD_TIMEUNIT_MODPATTERN</a> which is specific to files of type MOD / S3M / XM / IT.
	 * <p>
	 * If playing a
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html">Sound</a> created with {@link FModSystem#createStream} or <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_createstream">FMOD_CREATESTREAM</a> changing the position may cause a slow reflush operation while the file seek and decode occurs. You can avoid this by creating the stream with <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_nonblocking">FMOD_NONBLOCKING</a>. This will cause the stream to go into <a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_openstate_setposition">FMOD_OPENSTATE_SETPOSITION</a> state (see {@link FModSound#getOpenState}) and Sound commands will return <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_err_notready">FMOD_ERR_NOTREADY</a>. {@link FModChannel#getPosition} will also not update until this non-blocking set position operation has completed.
	 * <p>
	 * Using a VBR source that does not have an associated seek table or seek information (such as MP3 or MOD/S3M/XM/IT) may cause inaccurate seeking if you specify
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_ms">FMOD_TIMEUNIT_MS</a> or <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_pcm">FMOD_TIMEUNIT_PCM</a>. If you want FMOD to create a PCM vs bytes seek table so that seeking is accurate, you will have to specify <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_accuratetime">FMOD_ACCURATETIME</a> when loading or opening the sound. This means there is a slight delay as FMOD scans the whole file when loading the sound to create this table.
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#getPosition}
	 * <p>
	 * 
	 * @param channel
	 * @param position <dd>Playback position.
	 * @param postype <dd>Time units for <code>position</code>. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit">FMOD_TIMEUNIT</a>)
	 */
	public static int setPosition(long channel, int position, int postype)
	{
		return FMOD.FMOD_Channel_SetPosition(channel, position, postype);
	}

	/**
	 * Sets the priority used for virtual voice ordering.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_SetPriority(
	 *   FMOD_CHANNEL *channel,
	 *   int priority
	 * );
	 * </code></pre>
	 * <p>
	 * Priority is used as a coarse grain control for the virtual voice system, lower priority
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s will always be stolen before higher ones. For <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s of equal priority, those with the quietest {@link FModChannel#getAudibility} value will be stolen first.
	 * <p>
	 * See the
	 * <a href="https://www.fmod.com/docs/2.03/api/managing-resources-in-the-core-api.html#virtual-voice-system">Virtual Voice System</a> section of the <a href="https://www.fmod.com/docs/2.03/api/managing-resources-in-the-core-api.html">Managing Resources in the Core API</a> chapter for more information.
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#getPriority}
	 * <p>
	 * @param channel
	 * @param priority <dd><p>Priority where 0 represents most important and 256 represents least important.</p><ul><li><span class="label">Range:</span> [0, 256]</li><li><span class="label">Default:</span> 128</li></ul>
	 */
	public static int setPriority(long channel, int priority)
	{
		return FMOD.FMOD_Channel_SetPriority(channel, priority);
	}

	/**
	 * Sets the wet / send level for a particular reverb instance.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_SetReverbProperties(
	 *   FMOD_CHANNEL *channel,
	 *   int instance,
	 *   float wet
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_SetReverbProperties(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   int instance,
	 *   float wet
	 * );
	 * </code></pre>
	 * <p>
	 * Channels are automatically connected to all existing reverb instances due to the default wet level of 1.
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a>s however will not send to any reverb by default requiring an explicit call to this function.
	 * <p>
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a> reverb is optimal for the case where you want to send 1 mixed signal to the reverb, rather than a lot of individual <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a> reverb sends. It is advisable to do this to reduce CPU if you have many <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s inside a <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a>.
	 * <p>
	 * When setting a wet level for a
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a>, any <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s under that <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a> will still have their existing sends to the reverb. To avoid this doubling up you should explicitly set the <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a> wet levels to 0.
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#getReverbProperties}
	 * <p>
	 * 
	 * @param channel
	 * @param instance <dd><p>Reverb instance index.</p><ul><li><span class="label">Range:</span> [0, <a href="https://www.fmod.com/docs/2.03/api/core-api-system.html#fmod_reverb_maxinstances">FMOD_REVERB_MAXINSTANCES</a>]</li></ul>
	 * @param wet <dd><p>Send level for the <a href="https://www.fmod.com/docs/2.03/api/glossary.html#signal">signal</a> to the reverb. 0 = none, 1 = full. Negative level inverts the signal.</p><ul><li><span class="label">Units:</span> Linear</li><li><span class="label">Range:</span> (-inf, inf)</li><li><span class="label">Default:</span> 1 for <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>, 0 for <a href="https://www.fmod.com/docs/2.03/api/core-api-channelgroup.html">ChannelGroup</a></li></ul>
	 */
	public static int setReverbProperties(long channel, int instance, float wet)
	{
		return FMOD.FMOD_Channel_SetReverbProperties(channel, instance, wet);
	}

	/**
	 * Sets a user value associated with this object.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_SetUserData(
	 *   FMOD_CHANNEL *channel,
	 *   void *userdata
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_SetUserData(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   void *userdata
	 * );
	 * </code></pre>
	 * <p>
	 * This function allows arbitrary user data to be attached to this object. See the
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#user-data">User Data</a> section of the glossary for an example of how to get and set user data.
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#getUserData}, {@link FModChannel#setCallback}
	 * <p>
	 * 
	 * @param channel
	 * @param userdata <dd>Value stored on this object.
	 */
	public static int setUserData(long channel, long userdata)
	{
		return FMOD.FMOD_Channel_SetUserData(channel, userdata);
	}

	/**
	 * Sets the volume level.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_SetVolume(
	 *   FMOD_CHANNEL *channel,
	 *   float volume
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_SetVolume(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   float volume
	 * );
	 * </code></pre>
	 * <p>
	 * To define the volume per Sound use
	 * {@link FModSound#setDefaults}.
	 * <p>
	 * Setting
	 * <code>volume</code> at a level higher than 1 can lead to distortion/clipping.
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#getVolume}
	 * <p>
	 * 
	 * @param channel
	 * @param volume <dd><p>Volume level. 0 = silent, 1 = full. Negative level inverts the <a href="https://www.fmod.com/docs/2.03/api/glossary.html#signal">signal</a>.  Values larger than 1 amplify the signal.</p><ul><li><span class="label">Units:</span> Linear</li><li><span class="label">Range:</span> (-inf, inf)</li><li><span class="label">Default:</span> 1</li></ul>
	 */
	public static int setVolume(long channel, float volume)
	{
		return FMOD.FMOD_Channel_SetVolume(channel, volume);
	}

	/**
	 * Sets whether volume changes are ramped or instantaneous.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_SetVolumeRamp(
	 *   FMOD_CHANNEL *channel,
	 *   FMOD_BOOL ramp
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_SetVolumeRamp(
	 *   FMOD_CHANNELGROUP *channelgroup,
	 *   FMOD_BOOL ramp
	 * );
	 * </code></pre>
	 * <p>
	 * Volume changes when not paused will be ramped to the target value to avoid a pop sound, this function allows that setting to be overridden and volume changes to be applied immediately.
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#getVolumeRamp}
	 * <p>
	 * 
	 * @param channel
	 * @param ramp <dd><p>Ramp state.  True = volume change is ramped.  False = volume change is instantaeous.</p><ul><li><span class="label">Units:</span> Boolean</li><li><span class="label">Default:</span> True</li></ul>
	 */
	public static int setVolumeRamp(long channel, int ramp)
	{
		return FMOD.FMOD_Channel_SetVolumeRamp(channel, ramp);
	}

	/**
	 * Stops the Channel (or all Channels in nested ChannelGroups) from playing.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Channel_Stop(
	 * FMOD_CHANNEL *channel
	 * );
	 * FMOD_RESULT FMOD_ChannelGroup_Stop(
	 * FMOD_CHANNELGROUP *channelgroup
	 * );
	 * </code></pre>
	 * <p>
	 * This will free up internal resources for reuse by the virtual voice system.
	 * <p>
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>s are stopped automatically when their playback position reaches the length of the <a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html">Sound</a> being played. This is not the case however if the <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a> is playing a <a href="https://www.fmod.com/docs/2.03/api/glossary.html#dsp">DSP</a> or the <a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html">Sound</a> is looping, in which case the <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a> will continue playing until stop is called. Once stopped, the <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a> handle will become invalid and can be discarded and any API calls made with it will return <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_err_invalid_handle">FMOD_ERR_INVALID_HANDLE</a>.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#playSound}, {@link FModChannel#getPosition}, {@link FModSound#getLength}, {@link FModSystem#playDSP}
	 * <p>
	 * 
	 * @param channel
	 */
	public static int stop(long channel)
	{
		return FMOD.FMOD_Channel_Stop(channel);
	}

}
