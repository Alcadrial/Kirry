package com.alcadrial.renderengine.audio.bindings;

import org.lwjgl.fmod.FMOD;

/**
 * <div class="manual-content api">
<h1>7. Core API Reference | ChannelControl</h1>
<p>An interface that represents the shared APIs between <a class="apilink" href="core-api-channel.html">Channel</a> and <a class="apilink" href="core-api-channelgroup.html">ChannelGroup</a>.</p>
<p><strong>Playback:</strong></p>
<ul>
<li><span>{@link #isPlaying(long, java.nio.IntBuffer)} Retrieves the playing state.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_stop" title="Stops the Channel (or all Channels in nested ChannelGroups) from playing.">ChannelControl::stop</a> Stops the Channel (or all Channels in nested ChannelGroups) from playing.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_setpaused" title="Sets the paused state.">ChannelControl::setPaused</a> Sets the paused state.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_getpaused" title="Retrieves the paused state.">ChannelControl::getPaused</a> Retrieves the paused state.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_setmode" title="Sets the playback mode that controls how this object behaves.">ChannelControl::setMode</a> Sets the playback mode that controls how this object behaves.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_getmode" title="Retrieves the playback mode bits that control how this object behaves.">ChannelControl::getMode</a> Retrieves the playback mode bits that control how this object behaves.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_setpitch" title="Sets the relative pitch / playback rate.">ChannelControl::setPitch</a> Sets the relative pitch / playback rate.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_getpitch" title="Retrieves the relative pitch / playback rate.">ChannelControl::getPitch</a> Retrieves the relative pitch / playback rate.</span></li>
</ul>
<p><strong>Volume levels:</strong></p>
<ul>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_getaudibility" title="Retrieves an estimation of the output volume.">ChannelControl::getAudibility</a> Retrieves an estimation of the output volume.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_setvolume" title="Sets the volume level.">ChannelControl::setVolume</a> Sets the volume level.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_getvolume" title="Retrieves the volume level.">ChannelControl::getVolume</a> Retrieves the volume level.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_setvolumeramp" title="Sets whether volume changes are ramped or instantaneous.">ChannelControl::setVolumeRamp</a> Sets whether volume changes are ramped or instantaneous.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_getvolumeramp" title="Retrieves whether volume changes are ramped or instantaneous.">ChannelControl::getVolumeRamp</a> Retrieves whether volume changes are ramped or instantaneous.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_setmute" title="Sets the mute state.">ChannelControl::setMute</a> Sets the mute state.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_getmute" title="Retrieves the mute state.">ChannelControl::getMute</a> Retrieves the mute state.</span></li>
</ul>
<p><strong>Spatialization:</strong></p>
<ul>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_set3dattributes" title="Sets the 3D position and velocity used to apply panning, attenuation and doppler.">ChannelControl::set3DAttributes</a> Sets the 3D position and velocity used to apply panning, attenuation and doppler.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_get3dattributes" title="Retrieves the 3D position and velocity used to apply panning, attenuation and doppler.">ChannelControl::get3DAttributes</a> Retrieves the 3D position and velocity used to apply panning, attenuation and doppler.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_set3dconeorientation" title="Sets the orientation of a 3D cone shape, used for simulated occlusion.">ChannelControl::set3DConeOrientation</a> Sets the orientation of a 3D cone shape, used for simulated occlusion.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_get3dconeorientation" title="Retrieves the orientation of a 3D cone shape, used for simulated occlusion.">ChannelControl::get3DConeOrientation</a> Retrieves the orientation of a 3D cone shape, used for simulated occlusion.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_set3dconesettings" title="Sets the angles and attenuation levels of a 3D cone shape, for simulated occlusion which is based on direction.">ChannelControl::set3DConeSettings</a> Sets the angles and attenuation levels of a 3D cone shape, for simulated occlusion which is based on direction.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_get3dconesettings" title="Retrieves the angles and attenuation levels of a 3D cone shape, for simulated occlusion which is based on direction.">ChannelControl::get3DConeSettings</a> Retrieves the angles and attenuation levels of a 3D cone shape, for simulated occlusion which is based on direction.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_set3dcustomrolloff" title="Sets a custom roll-off shape for 3D distance attenuation.">ChannelControl::set3DCustomRolloff</a> Sets a custom roll-off shape for 3D distance attenuation.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_get3dcustomrolloff" title="Retrieves the current custom roll-off shape for 3D distance attenuation.">ChannelControl::get3DCustomRolloff</a> Retrieves the current custom roll-off shape for 3D distance attenuation.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_set3ddistancefilter" title="Sets an override value for the 3D distance filter.">ChannelControl::set3DDistanceFilter</a> Sets an override value for the 3D distance filter.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_get3ddistancefilter" title="Retrieves the override values for the 3D distance filter.">ChannelControl::get3DDistanceFilter</a> Retrieves the override values for the 3D distance filter.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_set3ddopplerlevel" title="Sets the amount by which doppler is scaled.">ChannelControl::set3DDopplerLevel</a> Sets the amount by which doppler is scaled.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_get3ddopplerlevel" title="Retrieves the amount by which doppler is scaled.">ChannelControl::get3DDopplerLevel</a> Retrieves the amount by which doppler is scaled.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_set3dlevel" title="Sets the blend between 3D panning and 2D panning.">ChannelControl::set3DLevel</a> Sets the blend between 3D panning and 2D panning.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_get3dlevel" title="Retrieves the blend between 3D panning and 2D panning.">ChannelControl::get3DLevel</a> Retrieves the blend between 3D panning and 2D panning.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_set3dminmaxdistance" title="Sets the minimum and maximum distances used to calculate the 3D roll-off attenuation.">ChannelControl::set3DMinMaxDistance</a> Sets the minimum and maximum distances used to calculate the 3D roll-off attenuation.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_get3dminmaxdistance" title="Retrieves the minimum and maximum distances used to calculate the 3D roll-off attenuation.">ChannelControl::get3DMinMaxDistance</a> Retrieves the minimum and maximum distances used to calculate the 3D roll-off attenuation.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_set3docclusion" title="Sets the 3D attenuation factors for the direct and reverb paths.">ChannelControl::set3DOcclusion</a> Sets the 3D attenuation factors for the direct and reverb paths.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_get3docclusion" title="Retrieves the 3D attenuation factors for the direct and reverb paths.">ChannelControl::get3DOcclusion</a> Retrieves the 3D attenuation factors for the direct and reverb paths.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_set3dspread" title="Sets the spread of a 3D sound in speaker space.">ChannelControl::set3DSpread</a> Sets the spread of a 3D sound in speaker space.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_get3dspread" title="Retrieves the spread of a 3D sound in speaker space.">ChannelControl::get3DSpread</a> Retrieves the spread of a 3D sound in speaker space.</span></li>
</ul>
<p><strong>Panning and level adjustment:</strong></p>
<ul>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_setpan" title="Sets the left/right pan level.">ChannelControl::setPan</a> Sets the left/right pan level.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_setmixlevelsinput" title="Sets the incoming volume level for each channel of a multi-channel signal.">ChannelControl::setMixLevelsInput</a> Sets the incoming volume level for each channel of a multi-channel signal.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_setmixlevelsoutput" title="Sets the outgoing volume levels for each speaker.">ChannelControl::setMixLevelsOutput</a> Sets the outgoing volume levels for each speaker.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_setmixmatrix" title="Sets a 2 dimensional pan matrix that maps the signal from input channels (columns) to output speakers (rows).">ChannelControl::setMixMatrix</a> Sets a 2 dimensional pan matrix that maps the signal from input channels (columns) to output speakers (rows).</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_getmixmatrix" title="Retrieves a 2 dimensional pan matrix that maps the signal from input channels (columns) to output speakers (rows).">ChannelControl::getMixMatrix</a> Retrieves a 2 dimensional pan matrix that maps the signal from input channels (columns) to output speakers (rows).</span></li>
</ul>
<p><strong>Filtering:</strong></p>
<ul>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_setreverbproperties" title="Sets the wet / send level for a particular reverb instance.">ChannelControl::setReverbProperties</a> Sets the wet / send level for a particular reverb instance.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_getreverbproperties" title="Retrieves the wet / send level for a particular reverb instance.">ChannelControl::getReverbProperties</a> Retrieves the wet / send level for a particular reverb instance.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_setlowpassgain" title="Sets the gain of the dry signal when built in lowpass / distance filtering is applied.">ChannelControl::setLowPassGain</a> Sets the gain of the dry signal when built in lowpass / distance filtering is applied.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_getlowpassgain" title="Retrieves the gain of the dry signal when built in lowpass / distance filtering is applied.">ChannelControl::getLowPassGain</a> Retrieves the gain of the dry signal when built in lowpass / distance filtering is applied.</span></li>
</ul>
<p><strong>DSP chain configuration:</strong></p>
<ul>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_adddsp" title="Adds a DSP unit to the specified index in the DSP chain.">ChannelControl::addDSP</a> Adds a DSP unit to the specified index in the DSP chain.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_removedsp" title="Removes the specified DSP unit from the DSP chain.">ChannelControl::removeDSP</a> Removes the specified DSP unit from the DSP chain.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_getnumdsps" title="Retrieves the number of DSP units in the DSP chain.">ChannelControl::getNumDSPs</a> Retrieves the number of DSP units in the DSP chain.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_getdsp" title="Retrieves the DSP unit at the specified index in the DSP chain.">ChannelControl::getDSP</a> Retrieves the DSP unit at the specified index in the DSP chain.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_setdspindex" title="Sets the index in the DSP chain of the specified DSP.">ChannelControl::setDSPIndex</a> Sets the index in the DSP chain of the specified DSP.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_getdspindex" title="Retrieves the index of a DSP inside the Channel or ChannelGroup's DSP chain.">ChannelControl::getDSPIndex</a> Retrieves the index of a DSP inside the Channel or ChannelGroup's DSP chain.</span></li>
</ul>
<hr>
<ul>
<li><span><a class="apilink" href="core-api-channelcontrol.html#fmod_channelcontrol_dsp_index" title="References to built in DSP positions that reside in a Channel or ChannelGroup DSP chain.">FMOD_CHANNELCONTROL_DSP_INDEX</a> References to built in DSP positions that reside in a Channel or ChannelGroup DSP chain.</span></li>
</ul>
<p><strong>Sample accurate scheduling:</strong></p>
<ul>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_getdspclock" title="Retrieves the DSP clock values at this point in time.">ChannelControl::getDSPClock</a> Retrieves the DSP clock values at this point in time.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_setdelay" title="Sets a sample accurate start (and/or stop) time relative to the parent ChannelGroup DSP clock.">ChannelControl::setDelay</a> Sets a sample accurate start (and/or stop) time relative to the parent ChannelGroup DSP clock.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_getdelay" title="Retrieves a sample accurate start (and/or stop) time relative to the parent ChannelGroup DSP clock.">ChannelControl::getDelay</a> Retrieves a sample accurate start (and/or stop) time relative to the parent ChannelGroup DSP clock.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_addfadepoint" title="Adds a sample accurate fade point at a time relative to the parent ChannelGroup DSP clock.">ChannelControl::addFadePoint</a> Adds a sample accurate fade point at a time relative to the parent ChannelGroup DSP clock.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_setfadepointramp" title="Adds a volume ramp at the specified time in the future using fade points.">ChannelControl::setFadePointRamp</a> Adds a volume ramp at the specified time in the future using fade points.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_removefadepoints" title="Removes all fade points between the two specified clock values (inclusive).">ChannelControl::removeFadePoints</a> Removes all fade points between the two specified clock values (inclusive).</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_getfadepoints" title="Retrieves information about stored fade points.">ChannelControl::getFadePoints</a> Retrieves information about stored fade points.</span></li>
</ul>
<p><strong>General:</strong></p>
<ul>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_setcallback" title="Sets the callback for ChannelControl level notifications.">ChannelControl::setCallback</a> Sets the callback for ChannelControl level notifications.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_setuserdata" title="Sets a user value associated with this object.">ChannelControl::setUserData</a> Sets a user value associated with this object.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_getuserdata" title="Retrieves a user value associated with this object.">ChannelControl::getUserData</a> Retrieves a user value associated with this object.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#channelcontrol_getsystemobject" title="Retrieves the System that created this object.">ChannelControl::getSystemObject</a> Retrieves the System that created this object.</span></li>
</ul>
<hr>
<ul>
<li><span><a class="apilink" href="core-api-channelcontrol.html#fmod_channelcontrol_callback" title="Callback for Channel or ChannelGroup notifications.">FMOD_CHANNELCONTROL_CALLBACK</a> Callback for Channel or ChannelGroup notifications.</span></li>
</ul>
<hr>
<ul>
<li><span><a class="apilink" href="core-api-channelcontrol.html#fmod_channelcontrol_type" title="Identifier used to distinguish between Channel and ChannelGroup in the ChannelControl callback.">FMOD_CHANNELCONTROL_TYPE</a> Identifier used to distinguish between Channel and ChannelGroup in the ChannelControl callback.</span></li>
<li><span><a class="apilink" href="core-api-channelcontrol.html#fmod_channelcontrol_callback_type" title="Types of callbacks called by Channels and ChannelGroups.">FMOD_CHANNELCONTROL_CALLBACK_TYPE</a> Types of callbacks called by Channels and ChannelGroups.</span></li>
</ul>
 *
 */
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
	 * <h2 api="function" id="channelcontrol_adddsp"><a href="#channelcontrol_adddsp">ChannelControl::addDSP</a></h2>
	<p>Adds a DSP unit to the specified index in the DSP chain.</p>
	<p>See <a href="glossary.html#dsp-chain">DSP chain</a></p>
	<p>
	</p><div class="language-selector">
	<div class="language-tab selected" data-language="language-c">C</div>
	<div class="language-tab" data-language="language-cpp">C++</div>
	<div class="language-tab" data-language="language-csharp">C#</div>
	<div class="language-tab" data-language="language-javascript">JS</div>
	</div>
	<p></p>
	<div class="highlight language-cpp" style="display: none;"><pre><span></span><span class="n">FMOD_RESULT</span> <span class="n">ChannelControl</span><span class="o">::</span><span class="n">addDSP</span><span class="p">(</span>
	<span class="kt">int</span> <span class="n">index</span><span class="p">,</span>
	<span class="n">DSP</span> <span class="o">*</span><span class="n">dsp</span>
	<span class="p">);</span>
	</pre></div>
	
	<div class="highlight language-c" style="display: block;"><pre><span></span><span class="n">FMOD_RESULT</span> <span class="nf">FMOD_Channel_AddDSP</span><span class="p">(</span>
	<span class="n">FMOD_CHANNEL</span> <span class="o">*</span><span class="n">channel</span><span class="p">,</span>
	<span class="kt">int</span> <span class="n">index</span><span class="p">,</span>
	<span class="n">FMOD_DSP</span> <span class="o">*</span><span class="n">dsp</span>
	<span class="p">);</span>
	<span class="n">FMOD_RESULT</span> <span class="nf">FMOD_ChannelGroup_AddDSP</span><span class="p">(</span>
	<span class="n">FMOD_CHANNELGROUP</span> <span class="o">*</span><span class="n">channelgroup</span><span class="p">,</span>
	<span class="kt">int</span> <span class="n">index</span><span class="p">,</span>
	<span class="n">FMOD_DSP</span> <span class="o">*</span><span class="n">dsp</span>
	<span class="p">);</span>
	</pre></div>
	
	<div class="highlight language-csharp" style="display: none;"><pre><span></span><span class="n">RESULT</span> <span class="n">ChannelControl</span><span class="p">.</span><span class="n">addDSP</span><span class="p">(</span>
	<span class="kt">int</span> <span class="n">index</span><span class="p">,</span>
	<span class="n">DSP</span> <span class="n">dsp</span>
	<span class="p">);</span>
	</pre></div>
	
	<div class="highlight language-javascript" style="display: none;"><pre><span></span><span class="nx">Channel</span><span class="p">.</span><span class="nx">addDSP</span><span class="p">(</span>
	<span class="nx">index</span><span class="p">,</span>
	<span class="nx">dsp</span>
	<span class="p">);</span>
	<span class="nx">ChannelGroup</span><span class="p">.</span><span class="nx">addDSP</span><span class="p">(</span>
	<span class="nx">index</span><span class="p">,</span>
	<span class="nx">dsp</span>
	<span class="p">);</span>
	</pre></div>
	
	<dl>
	<dt>index</dt>
	<dd>
	<p>Offset into the <a href="glossary.html#dsp-chain">DSP chain</a>, see <a class="apilink" href="core-api-channelcontrol.html#fmod_channelcontrol_dsp_index">FMOD_CHANNELCONTROL_DSP_INDEX</a> for special named offsets for 'head' and 'tail' and 'fader' units.</p>
	<ul>
	<li><span class="label">Range:</span> [0, <a class="apilink" href="core-api-channelcontrol.html#channelcontrol_getnumdsps">ChannelControl::getNumDSPs</a>)</li>
	</ul>
	</dd>
	<dt>dsp</dt>
	<dd>DSP unit to be added. (<a class="apilink" href="core-api-dsp.html">DSP</a>)</dd>
	</dl>
	<p>If <code>dsp</code> is already added to an existing object it will be removed and then added to this object.</p>
	<p>For detailed information on FMOD's <a class="apilink" href="core-api-dsp.html">DSP</a> network, read the <a href="white-papers-dsp-architecture.html">DSP Architecture and Usage</a> white paper.</p>
	<p><strong>See Also:</strong> <a class="apilink" href="core-api-channelcontrol.html#channelcontrol_removedsp">ChannelControl::removeDSP</a>, <a class="apilink" href="core-api-system.html#system_createdsp">System::createDSP</a>, <a class="apilink" href="core-api-system.html#system_createdspbytype">System::createDSPByType</a>, <a class="apilink" href="core-api-system.html#system_createdspbyplugin">System::createDSPByPlugin</a></p>
	
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
