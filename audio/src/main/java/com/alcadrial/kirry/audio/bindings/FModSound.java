package com.alcadrial.kirry.audio.bindings;

import org.lwjgl.fmod.FMOD;

/**
 * <div class="manual-content api"><strong>11. Core API Reference | Sound</strong>Container for <a href="https://www.fmod.com/docs/2.03/api/glossary.html#sample-data">sample data</a> that can be played on a <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a>.
 * <p>
 * Create with {@link FModSystem#createSound} or {@link FModSystem#createStream}.
 * <p>
 * <strong>Format information:</strong>
 * <p>
 * <ul>
 *     <li><span>{@link FModSound#getName} Retrieves the name of a sound.</span></li>
 *     <li><span>{@link FModSound#getFormat} Returns format information about the sound.</span></li>
 *     <li><span>{@link FModSound#getLength} Retrieves the length using the specified time unit.</span></li>
 *     <li><span>{@link FModSound#getNumTags} Retrieves the number of metadata tags.</span></li>
 *     <li><span>{@link FModSound#getTag} Retrieves a metadata tag.</span></li>
 * </ul>
 * <hr><ul>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_tag">FMOD_TAG</a> Tag data / metadata description.</span></li>
 * </ul>
 * <hr><ul>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_sound_type">FMOD_SOUND_TYPE</a> Recognized audio formats that can be loaded into a Sound.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_sound_format">FMOD_SOUND_FORMAT</a> These definitions describe the native format of the hardware or software buffer that will be used.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_tagtype">FMOD_TAGTYPE</a> List of tag data / metadata types that could be stored within a sound.  These include id3 tags, metadata from netstreams and vorbis/asf data.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_tagdatatype">FMOD_TAGDATATYPE</a> List of tag data / metadata types.</span></li>
 * </ul>
 * <strong>Defaults when played:</strong>
 * <p>
 * <ul>
 *     <li><span>{@link FModSound#set3DConeSettings} Sets the angles and attenuation levels of a 3D cone shape, for simulated occlusion which is based on direction.</span></li>
 *     <li><span>{@link FModSound#get3DConeSettings} Retrieves the inside and outside angles of the 3D projection cone and the outside volume.</span></li>
 *     <li><span>{@link FModSound#set3DCustomRolloff} Sets a custom roll-off shape for 3D distance attenuation.</span></li>
 *     <li><span>{@link FModSound#get3DCustomRolloff} Retrieves the current custom roll-off shape for 3D distance attenuation.</span></li>
 *     <li><span>{@link FModSound#set3DMinMaxDistance} Sets the minimum and maximum audible distance for a 3D sound.</span></li>
 *     <li><span>{@link FModSound#get3DMinMaxDistance} Retrieve the minimum and maximum audible distance for a 3D sound.</span></li>
 *     <li><span>{@link FModSound#setDefaults} Sets a sound's default playback attributes.</span></li>
 *     <li><span>{@link FModSound#getDefaults} Retrieves a sound's default playback attributes.</span></li>
 *     <li><span>{@link FModSound#setMode} Sets or alters the mode of a sound.</span></li>
 *     <li><span>{@link FModSound#getMode} Retrieves the mode of a sound.</span></li>
 *     <li><span>{@link FModSound#setLoopCount} Sets the sound to loop a specified number of times before stopping if the playback mode is set to looping.</span></li>
 *     <li><span>{@link FModSound#getLoopCount} Retrieves the sound's loop count.</span></li>
 *     <li><span>{@link FModSound#setLoopPoints} Sets the loop points within a sound.</span></li>
 *     <li><span>{@link FModSound#getLoopPoints} Retrieves the loop points for a sound.</span></li>
 * </ul>
 * <strong>Relationship management:</strong>
 * <p>
 * <ul>
 *     <li><span>{@link FModSound#setSoundGroup} Moves the sound from its existing SoundGroup to the specified sound group.</span></li>
 *     <li><span>{@link FModSound#getSoundGroup} Retrieves the sound's current sound group.</span></li>
 *     <li><span>{@link FModSound#getNumSubSounds} Retrieves the number of subsounds stored within a sound.</span></li>
 *     <li><span>{@link FModSound#getSubSound} Retrieves a handle to a Sound object that is contained within the parent sound.</span></li>
 *     <li><span>{@link FModSound#getSubSoundParent} Retrieves the parent Sound object that contains this subsound.</span></li>
 * </ul>
 * <strong>Data reading:</strong>
 * <p>
 * <ul>
 *     <li><span>{@link FModSound#getOpenState} Retrieves the state a sound is in after being opened with the non blocking flag, or the current state of the streaming buffer.</span></li>
 *     <li><span>{@link FModSound#readData} Reads data from an opened sound to a specified buffer, using FMOD's internal codecs.</span></li>
 *     <li><span>{@link FModSound#seekData} Seeks a sound for use with data reading, using FMOD's internal codecs.</span></li>
 *     <li><span>{@link FModSound#lock} Gives access to a portion or all the sample data of a sound for direct manipulation.</span></li>
 *     <li><span>{@link FModSound#unlock} Finalizes a previous sample data lock and submits it back to the Sound object.</span></li>
 * </ul>
 * <hr><ul>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_openstate">FMOD_OPENSTATE</a> These values describe what state a sound is in after FMOD_NONBLOCKING has been used to open it.</span></li>
 * </ul>
 * <strong>Music:</strong>
 * <p>
 * <ul>
 *     <li><span>{@link FModSound#getMusicNumChannels} Gets the number of music channels inside a MOD/S3M/XM/IT/MIDI file.</span></li>
 *     <li><span>{@link FModSound#setMusicChannelVolume} Sets the volume of a MOD/S3M/XM/IT/MIDI music channel volume.</span></li>
 *     <li><span>{@link FModSound#getMusicChannelVolume} Retrieves the volume of a MOD/S3M/XM/IT/MIDI music channel volume.</span></li>
 *     <li><span>{@link FModSound#setMusicSpeed} Sets the relative speed of MOD/S3M/XM/IT/MIDI music.</span></li>
 *     <li><span>{@link FModSound#getMusicSpeed} Gets the relative speed of MOD/S3M/XM/IT/MIDI music.</span></li>
 * </ul>
 * <strong>Synchronization / markers:</strong>
 * <p>
 * <ul>
 *     <li><span>{@link FModSound#getSyncPoint} Retrieve a sync point.</span></li>
 *     <li><span>{@link FModSound#getSyncPointInfo} Retrieves information on an embedded sync point.</span></li>
 *     <li><span>{@link FModSound#getNumSyncPoints} Retrieves the number of sync points stored within a sound.</span></li>
 *     <li><span>{@link FModSound#addSyncPoint} Adds a sync point at a specific time within the sound.</span></li>
 *     <li><span>{@link FModSound#deleteSyncPoint} Deletes a sync point within the sound.</span></li>
 * </ul>
 * <strong>General:</strong>
 * <p>
 * <ul>
 *     <li><span>{@link FModSound#release} Frees a sound object.</span></li>
 *     <li><span>{@link FModSound#setUserData} Sets a user value associated with this object.</span></li>
 *     <li><span>{@link FModSound#getUserData} Retrieves a user value associated with this object.</span></li>
 *     <li><span>{@link FModSound#getSystemObject} Retrieves the parent System object.</span></li>
 * </ul>
 * <hr><ul>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_sound_pcmread_callback">FMOD_SOUND_PCMREAD_CALLBACK</a> Read callback used for user created sounds or to intercept FMOD's decoder during a normal sound open.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_sound_pcmsetpos_callback">FMOD_SOUND_PCMSETPOS_CALLBACK</a> Set position callback for user created sounds or to intercept FMOD's decoder during an API setPositon call.</span></li>
 *     <li><span><a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_sound_nonblock_callback">FMOD_SOUND_NONBLOCK_CALLBACK</a> Callback to be called when a sound has finished loading, or a non blocking seek is occuring.</span></li>
 * </ul>
 * 
 * @author FMOD team, autogenerated by Alcadrial
 */
public class FModSound {

	private FModSound()
	{

	}

	/**
	 * Adds a sync point at a specific time within the sound.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_AddSyncPoint(
	 *   FMOD_SOUND *sound,
	 *   unsigned int offset,
	 *   FMOD_TIMEUNIT offsettype,
	 *   const char *name,
	 *   FMOD_SYNCPOINT **point
	 * );
	 * </code></pre>
	 * <p>
	 * For more information on sync points see
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#sync-points">Sync Points</a>.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSound#getNumSyncPoints}, {@link FModSound#getSyncPoint}, {@link FModSound#getSyncPointInfo}, {@link FModSound#deleteSyncPoint}
	 * <p>
	 * 
	 * @param sound
	 * @param offset <dd>Offset value.
	 * @param offsetType <dd><code>offset</code> unit type. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit">FMOD_TIMEUNIT</a>)
	 * @param name <span>(<i>Opt</i>) <dd>Sync point name. (<a href="https://www.fmod.com/docs/2.03/api/glossary.html#string-format">UTF-8 string</a>)
	 * @param point <span>(<i>Out, Opt</i>) <dd>Sync point. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_syncpoint">FMOD_SYNCPOINT</a>)
	 */
	public static int addSyncPoint(long sound, int offset, int offsetType, java.lang.CharSequence name, org.lwjgl.PointerBuffer point)
	{
		return FMOD.FMOD_Sound_AddSyncPoint(sound, offset, offsetType, name, point);
	}

	/**
	 * Adds a sync point at a specific time within the sound.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_AddSyncPoint(
	 *   FMOD_SOUND *sound,
	 *   unsigned int offset,
	 *   FMOD_TIMEUNIT offsettype,
	 *   const char *name,
	 *   FMOD_SYNCPOINT **point
	 * );
	 * </code></pre>
	 * <p>
	 * For more information on sync points see
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#sync-points">Sync Points</a>.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSound#getNumSyncPoints}, {@link FModSound#getSyncPoint}, {@link FModSound#getSyncPointInfo}, {@link FModSound#deleteSyncPoint}
	 * <p>
	 * 
	 * @param sound
	 * @param offset <dd>Offset value.
	 * @param offsetType <dd><code>offset</code> unit type. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit">FMOD_TIMEUNIT</a>)
	 * @param name <span>(<i>Opt</i>) <dd>Sync point name. (<a href="https://www.fmod.com/docs/2.03/api/glossary.html#string-format">UTF-8 string</a>)
	 * @param point <span>(<i>Out, Opt</i>) <dd>Sync point. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_syncpoint">FMOD_SYNCPOINT</a>)
	 */
	public static int addSyncPoint(long sound, int offset, int offsetType, java.nio.ByteBuffer name, org.lwjgl.PointerBuffer point)
	{
		return FMOD.FMOD_Sound_AddSyncPoint(sound, offset, offsetType, name, point);
	}

	/**
	 * Deletes a sync point within the sound.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_DeleteSyncPoint(
	 *   FMOD_SOUND *sound,
	 *   FMOD_SYNCPOINT *point
	 * );
	 * </code></pre>
	 * <p>
	 * For for more information on sync points see
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#sync-points">Sync Points</a>.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSound#addSyncPoint}, {@link FModSound#getNumSyncPoints}, {@link FModSound#getSyncPoint}
	 * <p>
	 * 
	 * @param sound
	 * @param point <dd>Sync point. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_syncpoint">FMOD_SYNCPOINT</a>)
	 */
	public static int deleteSyncPoint(long sound, long point)
	{
		return FMOD.FMOD_Sound_DeleteSyncPoint(sound, point);
	}

	/**
	 * Retrieves the inside and outside angles of the 3D projection cone and the outside volume.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_Get3DConeSettings(
	 *   FMOD_SOUND *sound,
	 *   float *insideconeangle,
	 *   float *outsideconeangle,
	 *   float *outsidevolume
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSound#set3DConeSettings}
	 * <p>
	 * 
	 * @param sound
	 * @param insideConeAngle <span>(<i>Out, Opt</i>) <dd><p>Inside cone angle. This is the angle within which the sound is unattenuated.</p><ul><li><span class="label">Units:</span> Degrees</li><li><span class="label">Range:</span> [0, <code>outsideconeangle</code>]</li><li><span class="label">Default:</span> 360</li></ul>
	 * @param outsideConeAngle <span>(<i>Out, Opt</i>) <dd><p>Outside cone angle. This is the angle outside of which the sound is attenuated to its outside volume.</p><ul><li><span class="label">Units:</span> Degrees</li><li><span class="label">Range:</span> [<code>insideconeangle</code>, 360]</li><li><span class="label">Default:</span> 360</li></ul>
	 * @param outsideVolume <span>(<i>Out, Opt</i>) <dd><p>Cone outside volume.</p><ul><li><span class="label">Units:</span> Linear</li><li><span class="label">Range:</span> [0, 1]</li><li><span class="label">Default:</span> 1</li></ul>
	 */
	public static int get3DConeSettings(long sound, java.nio.FloatBuffer insideConeAngle, java.nio.FloatBuffer outsideConeAngle, java.nio.FloatBuffer outsideVolume)
	{
		return FMOD.FMOD_Sound_Get3DConeSettings(sound, insideConeAngle, outsideConeAngle, outsideVolume);
	}

	/**
	 * Retrieves the current custom roll-off shape for 3D distance attenuation.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_Get3DCustomRolloff(
	 *   FMOD_SOUND *sound,
	 *   FMOD_VECTOR **points,
	 *   int *numpoints
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSound#set3DCustomRolloff}
	 * <p>
	 * 
	 * @param sound
	 * @param points <span>(<i>Out, Opt</i>) <dd>Array of points sorted by distance where x = distance and y = volume from 0 to 1. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_vector">FMOD_VECTOR</a>)
	 * @param numPoints <span>(<i>Out, Opt</i>) <dd>Number of <code>points</code>.
	 */
	public static int get3DCustomRolloff(long sound, org.lwjgl.PointerBuffer points, java.nio.IntBuffer numPoints)
	{
		return FMOD.FMOD_Sound_Get3DCustomRolloff(sound, points, numPoints);
	}

	/**
	 * Retrieve the minimum and maximum audible distance for a 3D sound.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_Get3DMinMaxDistance(
	 *   FMOD_SOUND *sound,
	 *   float *min,
	 *   float *max
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSound#set3DMinMaxDistance}
	 * <p>
	 * 
	 * @param sound
	 * @param min <span>(<i>Out, Opt</i>) <dd><p>Minimum volume distance for the sound.</p><ul><li><span class="label">Units:</span> <a href="https://www.fmod.com/docs/2.03/api/glossary.html#distance-units">Distance units</a></li><li><span class="label">Default:</span> 1</li></ul>
	 * @param max <span>(<i>Out, Opt</i>) <dd><p>Maximum volume distance for the sound.</p><ul><li><span class="label">Units:</span> <a href="https://www.fmod.com/docs/2.03/api/glossary.html#distance-units">Distance units</a></li><li><span class="label">Default:</span> 10000</li></ul>
	 */
	public static int get3DMinMaxDistance(long sound, java.nio.FloatBuffer min, java.nio.FloatBuffer max)
	{
		return FMOD.FMOD_Sound_Get3DMinMaxDistance(sound, min, max);
	}

	/**
	 * Retrieves a sound's default playback attributes.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_GetDefaults(
	 *   FMOD_SOUND *sound,
	 *   float *frequency,
	 *   int *priority
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSound#setDefaults}
	 * <p>
	 * 
	 * @param sound
	 * @param frequency <span>(<i>Out, Opt</i>) <dd><p>Default playback frequency.</p><ul><li><span class="label">Units:</span> Hertz</li><li><span class="label">Default:</span> 48000</li></ul>
	 * @param priority <span>(<i>Out, Opt</i>) <dd><p>Default priority where 0 is the highest priority.</p><ul><li><span class="label">Range:</span> [0, 256]</li><li><span class="label">Default:</span> 128</li></ul>
	 */
	public static int getDefaults(long sound, java.nio.FloatBuffer frequency, java.nio.IntBuffer priority)
	{
		return FMOD.FMOD_Sound_GetDefaults(sound, frequency, priority);
	}

	/**
	 * Returns format information about the sound.
	 * <p>
	 * @param sound
	 * @param type <span>(<i>Out, Opt</i>) <dd>Type of sound. (<a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_sound_type">FMOD_SOUND_TYPE</a>)
	 * @param format <span>(<i>Out, Opt</i>) <dd>Format of the sound. (<a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_sound_format">FMOD_SOUND_FORMAT</a>)
	 * @param channels <span>(<i>Out, Opt</i>) <dd>Number of channels.
	 * @param bits <span>(<i>Out, Opt</i>) <dd>Number of bits per sample, corresponding to <code>format</code>.
	 */
	public static int getFormat(long sound, java.nio.IntBuffer type, java.nio.IntBuffer format, java.nio.IntBuffer channels, java.nio.IntBuffer bits)
	{
		return FMOD.FMOD_Sound_GetFormat(sound, type, format, channels, bits);
	}

	/**
	 * Retrieves the length using the specified time unit.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_GetLength(
	 *   FMOD_SOUND *sound,
	 *   unsigned int *length,
	 *   FMOD_TIMEUNIT lengthtype
	 * );
	 * </code></pre>
	 * <p>
	 * <code>lengthtype</code> must be valid for the file format. For example, an MP3 file does not support <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit_modorder">FMOD_TIMEUNIT_MODORDER</a>.
	 * <p>
	 * A
	 * <code>length</code> of 0xFFFFFFFF means it is of unlimited length, such as an internet radio stream or MOD/S3M/XM/IT file which may loop forever.
	 * <p>
	 * <strong>Note:</strong>  Using a VBR (Variable Bit Rate) source that does not have metadata containing its accurate length (such as un-tagged MP3 or MOD/S3M/XM/IT) may return inaccurate length values.<br>For these formats, use <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_accuratetime">FMOD_ACCURATETIME</a> when creating the sound.  This will cause a slight delay and memory increase, as FMOD will scan the whole during creation to find the correct length.  This flag also creates a seek table to enable sample accurate seeking.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#createSound}
	 * <p>
	 * 
	 * @param sound
	 * @param length <span>(<i>Out</i>) <dd>Sound length in units specified by <code>lengthtype</code>.
	 * @param lengthType <dd>Time unit type to retrieve into <code>length</code>. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit">FMOD_TIMEUNIT</a>)
	 */
	public static int getLength(long sound, java.nio.IntBuffer length, int lengthType)
	{
		return FMOD.FMOD_Sound_GetLength(sound, length, lengthType);
	}

	/**
	 * Retrieves the sound's loop count.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_GetLoopCount(
	 *   FMOD_SOUND *sound,
	 *   int *loopcount
	 * );
	 * </code></pre>
	 * <p>
	 * Unlike the
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-channel.html">Channel</a> loop count function, this function simply returns the value set with {@link FModSound#setLoopCount}.  It does not decrement as it plays (especially seeing as one sound can be played multiple times).
	 * <p>
	 * <strong>See Also:</strong> {@link FModSound#setLoopCount}, {@link FModChannel#setLoopCount}
	 * <p>
	 * 
	 * @param sound
	 * @param loopCount <span>(<i>Out</i>) <dd><p>Number of times to loop before final playback where -1 is always loop.  0 means no loop.</p><ul><li><span class="label">Range:</span> [-1, inf)</li><li><span class="label">Default:</span> -1</li></ul>
	 */
	public static int getLoopCount(long sound, java.nio.IntBuffer loopCount)
	{
		return FMOD.FMOD_Sound_GetLoopCount(sound, loopCount);
	}

	/**
	 * Retrieves the loop points for a sound.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_GetLoopPoints(
	 *   FMOD_SOUND *sound,
	 *   unsigned int *loopstart,
	 *   FMOD_TIMEUNIT loopstarttype,
	 *   unsigned int *loopend,
	 *   FMOD_TIMEUNIT loopendtype
	 * );
	 * </code></pre>
	 * <p>
	 * The values from
	 * <code>loopstart</code> and <code>loopend</code> are inclusive, which means these positions will be played.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSound#setLoopPoints}
	 * <p>
	 * 
	 * @param sound
	 * @param loopStart <span>(<i>Out, Opt</i>) <dd>Loop start point.
	 * @param loopStartType <dd>Time format of <code>loopstart</code>. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit">FMOD_TIMEUNIT</a>)
	 * @param loopEnd <span>(<i>Out, Opt</i>) <dd>Loop end point.
	 * @param loopEndType <dd>Time format of <code>loopend</code>. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit">FMOD_TIMEUNIT</a>)
	 */
	public static int getLoopPoints(long sound, java.nio.IntBuffer loopStart, int loopStartType, java.nio.IntBuffer loopEnd, int loopEndType)
	{
		return FMOD.FMOD_Sound_GetLoopPoints(sound, loopStart, loopStartType, loopEnd, loopEndType);
	}

	/**
	 * Retrieves the mode of a sound.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_GetMode(
	 *   FMOD_SOUND *sound,
	 *   FMOD_MODE *mode
	 * );
	 * </code></pre>
	 * <p>
	 * The mode will be dependent on the mode set by a call to
	 * {@link FModSystem#createSound}, {@link FModSystem#createStream} or {@link FModSound#setMode}.
	 * <p>
	 * <strong>See Also:</strong> {@link FModChannel#setMode}, {@link FModChannel#getMode}
	 * <p>
	 * 
	 * @param sound
	 * @param mode <span>(<i>Out</i>) <dd>Current mode. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_mode">FMOD_MODE</a>)
	 */
	public static int getMode(long sound, java.nio.IntBuffer mode)
	{
		return FMOD.FMOD_Sound_GetMode(sound, mode);
	}

	/**
	 * Retrieves the volume of a MOD/S3M/XM/IT/MIDI music channel volume.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_GetMusicChannelVolume(
	 *   FMOD_SOUND *sound,
	 *   int channel,
	 *   float *volume
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSound#setMusicChannelVolume}
	 * <p>
	 * 
	 * @param sound
	 * @param channel <dd>MOD/S3M/XM/IT/MIDI music subchannel to retrieve the volume for.
	 * @param volume <span>(<i>Out</i>) <dd><p>Volume of the channel.</p><ul><li><span class="label">Units:</span> Linear</li><li><span class="label">Range:</span> [0, 1]</li><li><span class="label">Default:</span> 1</li></ul>
	 */
	public static int getMusicChannelVolume(long sound, int channel, java.nio.FloatBuffer volume)
	{
		return FMOD.FMOD_Sound_GetMusicChannelVolume(sound, channel, volume);
	}

	/**
	 * Gets the number of music channels inside a MOD/S3M/XM/IT/MIDI file.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_GetMusicNumChannels(
	 *   FMOD_SOUND *sound,
	 *   int *numchannels
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSound#setMusicChannelVolume}, {@link FModSound#getMusicChannelVolume}
	 * <p>
	 * 
	 * @param sound
	 * @param numchannels <span>(<i>Out</i>) <dd>Number of music channels used in the song.
	 */
	public static int getMusicNumChannels(long sound, java.nio.IntBuffer numchannels)
	{
		return FMOD.FMOD_Sound_GetMusicNumChannels(sound, numchannels);
	}

	/**
	 * Gets the relative speed of MOD/S3M/XM/IT/MIDI music.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_GetMusicSpeed(
	 *   FMOD_SOUND *sound,
	 *   float *speed
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSound#setMusicSpeed}
	 * <p>
	 * 
	 * @param sound
	 * @param speed <span>(<i>Out</i>) <dd>Speed of the song.
	 */
	public static int getMusicSpeed(long sound, java.nio.FloatBuffer speed)
	{
		return FMOD.FMOD_Sound_GetMusicSpeed(sound, speed);
	}

	/**
	 * Retrieves the name of a sound.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_GetName(
	 *   FMOD_SOUND *sound,
	 *   char *name,
	 *   int namelen
	 * );
	 * </code></pre>
	 * <p>
	 * if
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_lowmem">FMOD_LOWMEM</a> has been specified in {@link FModSystem#createSound}, this function will return "(null)".
	 * <p>
	 * 
	 * @param sound
	 * @param name <span>(<i>Out</i>) <dd>Name of the sound. (<a href="https://www.fmod.com/docs/2.03/api/glossary.html#string-format">UTF-8 string</a>)
	 */
	public static int getName(long sound, java.nio.ByteBuffer name)
	{
		return FMOD.FMOD_Sound_GetName(sound, name);
	}

	/**
	 * Retrieves the number of subsounds stored within a sound.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_GetNumSubSounds(
	 *   FMOD_SOUND *sound,
	 *   int *numsubsounds
	 * );
	 * </code></pre>
	 * <p>
	 * A format that has subsounds is a container format, such as FSB, DLS, MOD, S3M, XM, IT.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSound#getSubSound}
	 * <p>
	 * 
	 * @param sound
	 * @param numSubSounds <span>(<i>Out</i>) <dd>Number of subsounds.
	 */
	public static int getNumSubSounds(long sound, java.nio.IntBuffer numSubSounds)
	{
		return FMOD.FMOD_Sound_GetNumSubSounds(sound, numSubSounds);
	}

	/**
	 * Retrieves the number of sync points stored within a sound.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_GetNumSyncPoints(
	 *   FMOD_SOUND *sound,
	 *   int *numsyncpoints
	 * );
	 * </code></pre>
	 * <p>
	 * For for more information on sync points see
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#sync-points">Sync Points</a>.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSound#getSyncPoint}, {@link FModSound#getSyncPointInfo}, {@link FModSound#addSyncPoint}, {@link FModSound#deleteSyncPoint}
	 * <p>
	 * 
	 * @param sound
	 * @param numSyncPoints <span>(<i>Out</i>) <dd>Number of sync points.
	 */
	public static int getNumSyncPoints(long sound, java.nio.IntBuffer numSyncPoints)
	{
		return FMOD.FMOD_Sound_GetNumSyncPoints(sound, numSyncPoints);
	}

	/**
	 * Retrieves the number of metadata tags.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_GetNumTags(
	 *   FMOD_SOUND *sound,
	 *   int *numtags,
	 *   int *numtagsupdated
	 * );
	 * </code></pre>
	 * <p>
	 * 'Tags' are metadata stored within a sound file. These can be things like a song's name, composer etc.
	 * <p>
	 * <code>numtagsupdated</code> could be periodically checked to see if new tags are available in certain circumstances.  This might be the case with internet based streams (i.e. shoutcast or icecast) where the name of the song or other attributes might change.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSound#getTag}
	 * <p>
	 * 
	 * @param sound
	 * @param numTags <span>(<i>Out, Opt</i>) <dd>Number of tags.
	 * @param numTagsUpdated <span>(<i>Out, Opt</i>) <dd>Number of tags updated since this function was last called.
	 */
	public static int getNumTags(long sound, java.nio.IntBuffer numTags, java.nio.IntBuffer numTagsUpdated)
	{
		return FMOD.FMOD_Sound_GetNumTags(sound, numTags, numTagsUpdated);
	}

	/**
	 * Retrieves the state a sound is in after being opened with the non blocking flag, or the current state of the streaming buffer.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_GetOpenState(
	 *   FMOD_SOUND *sound,
	 *   FMOD_OPENSTATE *openstate,
	 *   unsigned int *percentbuffered,
	 *   FMOD_BOOL *starving,
	 *   FMOD_BOOL *diskbusy
	 * );
	 * </code></pre>
	 * <p>
	 * When a sound is opened with
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_nonblocking">FMOD_NONBLOCKING</a>, it is opened and prepared in the background, or asynchronously.  This allows the main application to execute without stalling on audio loads.<br>This function will describe the state of the asynchronous load routine i.e. whether it has succeeded, failed or is still in progress.
	 * <p>
	 * If 'starving' is true, then you will most likely hear a stuttering/repeating sound as the decode buffer loops on itself and replays old data.
	 * <br>With the ability to detect stream starvation, muting the sound with {@link FModChannel#setMute} will keep the stream quiet until it is not starving any more.
	 * <p>
	 * <strong>Note:</strong> Always check 'openstate' to determine the state of the sound. Do not assume that if this function returns <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_ok">FMOD_OK</a> then the sound has finished loading.
	 * <p>
	 * <strong>See Also:</strong> <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_mode">FMOD_MODE</a>
	 * <p>
	 * 
	 * @param sound
	 * @param openState <span>(<i>Out, Opt</i>) <dd>Open state of a sound. (<a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_openstate">FMOD_OPENSTATE</a>)
	 * @param percentBuffered <span>(<i>Out, Opt</i>) <dd><p>Filled percentage of a stream's file buffer.</p><ul><li><span class="label">Units:</span> Percent</li><li><span class="label">Range:</span> [0, 100]</li></ul>
	 * @param starving <span>(<i>Out, Opt</i>) <dd><p>Starving state.  true if a stream has decoded more than the stream file buffer has ready.</p><ul><li><span class="label">Units:</span> Boolean</li></ul>
	 * @param diskBusy <span>(<i>Out, Opt</i>) <dd><p>Disk is currently being accessed for this sound.</p><ul><li><span class="label">Units:</span> Boolean</li></ul>
	 */
	public static int getOpenState(long sound, java.nio.IntBuffer openState, java.nio.IntBuffer percentBuffered, java.nio.IntBuffer starving, java.nio.IntBuffer diskBusy)
	{
		return FMOD.FMOD_Sound_GetOpenState(sound, openState, percentBuffered, starving, diskBusy);
	}

	/**
	 * Retrieves the sound's current sound group.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_GetSoundGroup(
	 *   FMOD_SOUND *sound,
	 *   FMOD_SOUNDGROUP **soundgroup
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSound#setSoundGroup}, {@link FModSystem#getMasterSoundGroup}
	 * <p>
	 * 
	 * @param sound
	 * @param soundGroup <span>(<i>Out</i>) <dd>Sound's current sound group. (<a href="https://www.fmod.com/docs/2.03/api/core-api-soundgroup.html">SoundGroup</a>)
	 */
	public static int getSoundGroup(long sound, org.lwjgl.PointerBuffer soundGroup)
	{
		return FMOD.FMOD_Sound_GetSoundGroup(sound, soundGroup);
	}

	/**
	 * Retrieves a handle to a Sound object that is contained within the parent sound.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_GetSubSound(
	 *   FMOD_SOUND *sound,
	 *   int index,
	 *   FMOD_SOUND **subsound
	 * );
	 * </code></pre>
	 * <p>
	 * If the sound is a stream and
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_nonblocking">FMOD_NONBLOCKING</a> was not used, then this call will perform a blocking seek/flush to the specified subsound.
	 * <p>
	 * If
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_nonblocking">FMOD_NONBLOCKING</a> was used to open this sound and the sound is a stream, FMOD will do a non blocking seek/flush and set the state of the subsound to <a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_openstate_seeking">FMOD_OPENSTATE_SEEKING</a>.
	 * <p>
	 * The sound won't be ready to be used when
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_nonblocking">FMOD_NONBLOCKING</a> is used, until the state of the sound becomes <a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_openstate_ready">FMOD_OPENSTATE_READY</a> or <a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_openstate_error">FMOD_OPENSTATE_ERROR</a>.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSound#getSubSoundParent}, {@link FModSystem#createSound}
	 * <p>
	 * 
	 * @param sound
	 * @param index <dd><p>Index of the subsound.</p><ul><li><span class="label">Range:</span> [0, {@link FModSound#getNumSubSounds})</li></ul>
	 * @param subSound <span>(<i>Out</i>) <dd>Subsound object. (<a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html">Sound</a>)
	 */
	public static int getSubSound(long sound, int index, org.lwjgl.PointerBuffer subSound)
	{
		return FMOD.FMOD_Sound_GetSubSound(sound, index, subSound);
	}

	/**
	 * Retrieves the parent Sound object that contains this subsound.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_GetSubSoundParent(
	 *   FMOD_SOUND *sound,
	 *   FMOD_SOUND **parentsound
	 * );
	 * </code></pre>
	 * <p>
	 * If the sound is not a subsound, the
	 * <code>parentsound</code> will be null.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSound#getNumSubSounds}, {@link FModSound#getSubSound}
	 * <p>
	 * 
	 * @param sound
	 * @param parentSound <span>(<i>Out</i>) <dd>Parent sound object. (<a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html">Sound</a>)
	 */
	public static int getSubSoundParent(long sound, org.lwjgl.PointerBuffer parentSound)
	{
		return FMOD.FMOD_Sound_GetSubSoundParent(sound, parentSound);
	}

	/**
	 * Retrieve a sync point.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_GetSyncPoint(
	 *   FMOD_SOUND *sound,
	 *   int index,
	 *   FMOD_SYNCPOINT **point
	 * );
	 * </code></pre>
	 * <p>
	 * For for more information on sync points see
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#sync-points">Sync Points</a>.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSound#getNumSyncPoints}, {@link FModSound#getSyncPointInfo}, {@link FModSound#addSyncPoint}, {@link FModSound#deleteSyncPoint}
	 * <p>
	 * 
	 * @param sound
	 * @param index <dd><p>Index of the sync point.</p><ul><li><span class="label">Range:</span> [0, {@link FModSound#getNumSyncPoints})</li></ul>
	 * @param point <span>(<i>Out</i>) <dd>Sync point. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_syncpoint">FMOD_SYNCPOINT</a>)
	 */
	public static int getSyncPoint(long sound, int index, org.lwjgl.PointerBuffer point)
	{
		return FMOD.FMOD_Sound_GetSyncPoint(sound, index, point);
	}

	/**
	 * Retrieves information on an embedded sync point.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_GetSyncPointInfo(
	 *   FMOD_SOUND *sound,
	 *   FMOD_SYNCPOINT *point,
	 *   char *name,
	 *   int namelen,
	 *   unsigned int *offset,
	 *   FMOD_TIMEUNIT offsettype
	 * );
	 * </code></pre>
	 * <p>
	 * For for more information on sync points see
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#sync-points">Sync Points</a>.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSound#getNumSyncPoints}, {@link FModSound#getSyncPoint}, {@link FModSound#addSyncPoint}, {@link FModSound#deleteSyncPoint}
	 * <p>
	 * 
	 * @param sound
	 * @param point <dd>Sync point. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_syncpoint">FMOD_SYNCPOINT</a>)
	 * @param name <span>(<i>Out, Opt</i>) <dd>Name of the syncpoint. (<a href="https://www.fmod.com/docs/2.03/api/glossary.html#string-format">UTF-8 string</a>)
	 * @param nameLen <dd><p>Size of <code>name</code>.</p><ul><li><span class="label">Units:</span> Bytes</li></ul>
	 * @param offset <span>(<i>Out, Opt</i>) <dd>Offset of the syncpoint.
	 */
	public static int getSyncPointInfo(long sound, long point, java.nio.ByteBuffer name, java.nio.IntBuffer nameLen, int offset)
	{
		return FMOD.FMOD_Sound_GetSyncPointInfo(sound, point, name, nameLen, offset);
	}

	/**
	 * Retrieves the parent System object.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_GetSystemObject(
	 *   FMOD_SOUND *sound,
	 *   FMOD_SYSTEM **system
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#createSound}
	 * <p>
	 * 
	 * @param sound
	 * @param system <span>(<i>Out</i>) <dd>System object. (<a href="https://www.fmod.com/docs/2.03/api/core-api-system.html">System</a>)
	 */
	public static int getSystemObject(long sound, org.lwjgl.PointerBuffer system)
	{
		return FMOD.FMOD_Sound_GetSystemObject(sound, system);
	}

	/**
	 * Retrieves a metadata tag.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_GetTag(
	 *   FMOD_SOUND *sound,
	 *   const char *name,
	 *   int index,
	 *   FMOD_TAG *tag
	 * );
	 * </code></pre>
	 * <p>
	 * 'Tags' are metadata stored within a sound file.  These can be things like a song's name, composer etc.
	 * <p>
	 * The number of tags available can be found with
	 * {@link FModSound#getNumTags}.
	 * <p>
	 * The way to display or retrieve tags can be done in 3 different ways:
	 * <p>
	 * <ul><li>All tags can be continuously retrieved by looping from 0 to the numtags value in {@link FModSound#getNumTags} - 1.  Updated tags will refresh automatically, and the 'updated' member of the <a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_tag">FMOD_TAG</a> structure will be set to true if a tag has been updated, due to something like a netstream changing the song name for example.</li><li>Tags can be retrieved by specifying -1 as the index and only updating tags that are returned.  If all tags are retrieved and this function is called the function will return an error of <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_err_tagnotfound">FMOD_ERR_TAGNOTFOUND</a>.</li><li>Specific tags can be retrieved by specifying a name parameter.  The index can be 0 based or -1 in the same fashion as described previously.</li></ul>
	 * <p>
	 * Note with netstreams an important consideration must be made between songs, a tag may occur that changes the playback rate of the song.  It is up to the user to catch this and reset the playback rate with
	 * {@link FModChannel#setFrequency}.<br>A sample rate change will be signalled with a tag of type <a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_tagtype_fmod">FMOD_TAGTYPE_FMOD</a>.
	 * <p>
	 * @param sound
	 * @param name <dd>Name of a type of tag to retrieve. (<a href="https://www.fmod.com/docs/2.03/api/glossary.html#string-format">UTF-8 string</a>).  Specify null to retrieve all types of tags.
	 * @param index <span>(<i>Opt</i>) <dd><p>Index into the tag list as restricted by <code>name</code>.</p><ul><li><span class="label">Range:</span> [0, {@link FModSound#getNumTags})</li></ul>
	 * @param tag <span>(<i>Out</i>) <dd>Tag info structure. (<a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_tag">FMOD_TAG</a>)
	 */
	public static int getTag(long sound, java.nio.ByteBuffer name, int index, org.lwjgl.fmod.FMOD_TAG tag)
	{
		return FMOD.FMOD_Sound_GetTag(sound, name, index, tag);
	}

	/**
	 * Retrieves a metadata tag.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_GetTag(
	 *   FMOD_SOUND *sound,
	 *   const char *name,
	 *   int index,
	 *   FMOD_TAG *tag
	 * );
	 * </code></pre>
	 * <p>
	 * 'Tags' are metadata stored within a sound file.  These can be things like a song's name, composer etc.
	 * <p>
	 * The number of tags available can be found with
	 * {@link FModSound#getNumTags}.
	 * <p>
	 * The way to display or retrieve tags can be done in 3 different ways:
	 * <p>
	 * <ul><li>All tags can be continuously retrieved by looping from 0 to the numtags value in {@link FModSound#getNumTags} - 1.  Updated tags will refresh automatically, and the 'updated' member of the <a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_tag">FMOD_TAG</a> structure will be set to true if a tag has been updated, due to something like a netstream changing the song name for example.</li><li>Tags can be retrieved by specifying -1 as the index and only updating tags that are returned.  If all tags are retrieved and this function is called the function will return an error of <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_err_tagnotfound">FMOD_ERR_TAGNOTFOUND</a>.</li><li>Specific tags can be retrieved by specifying a name parameter.  The index can be 0 based or -1 in the same fashion as described previously.</li></ul>
	 * <p>
	 * Note with netstreams an important consideration must be made between songs, a tag may occur that changes the playback rate of the song.  It is up to the user to catch this and reset the playback rate with
	 * {@link FModChannel#setFrequency}.<br>A sample rate change will be signalled with a tag of type <a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_tagtype_fmod">FMOD_TAGTYPE_FMOD</a>.
	 * <p>
	 * @param sound
	 * @param name <dd>Name of a type of tag to retrieve. (<a href="https://www.fmod.com/docs/2.03/api/glossary.html#string-format">UTF-8 string</a>).  Specify null to retrieve all types of tags.
	 * @param index <span>(<i>Opt</i>) <dd><p>Index into the tag list as restricted by <code>name</code>.</p><ul><li><span class="label">Range:</span> [0, {@link FModSound#getNumTags})</li></ul>
	 * @param tag <span>(<i>Out</i>) <dd>Tag info structure. (<a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_tag">FMOD_TAG</a>)
	 */
	public static int getTag(long sound, java.lang.CharSequence name, int index, org.lwjgl.fmod.FMOD_TAG tag)
	{
		return FMOD.FMOD_Sound_GetTag(sound, name, index, tag);
	}

	/**
	 * Retrieves a user value associated with this object.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_GetUserData(
	 *   FMOD_SOUND *sound,
	 *   void **userdata
	 * );
	 * </code></pre>
	 * <p>
	 * This function allows arbitrary user data to be retrieved from this object. See the
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#user-data">User Data</a> section of the glossary for an example of how to get and set user data.
	 * <p>
	 * 
	 * @param sound
	 * @param userdata <span>(<i>Out</i>) <dd>User data set by calling {@link FModSound#setUserData}
	 */
	public static int getUserData(long sound, org.lwjgl.PointerBuffer userdata)
	{
		return FMOD.FMOD_Sound_GetUserData(sound, userdata);
	}

	/**
	 * Gives access to a portion or all the sample data of a sound for direct manipulation.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_Lock(
	 *   FMOD_SOUND *sound,
	 *   unsigned int offset,
	 *   unsigned int length,
	 *   void **ptr1,
	 *   void **ptr2,
	 *   unsigned int *len1,
	 *   unsigned int *len2
	 * );
	 * </code></pre>
	 * <p>
	 * You must always unlock the data again after you have finished with it, using
	 * {@link FModSound#unlock}.
	 * <p>
	 * With this function you get access to the raw audio data.  If the data is 8, 16, 24 or 32bit PCM data, mono or stereo data, you must take this into consideration when processing the data.  See
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#sample-data">Sample Data</a> for more information.
	 * <p>
	 * If the sound is created with
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_createcompressedsample">FMOD_CREATECOMPRESSEDSAMPLE</a> the data retrieved will be the compressed bitstream.
	 * <p>
	 * It is not possible to lock the following:
	 * <p>
	 * <ul><li>A parent sound containing subsounds.  A parent sound has no audio data and <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_err_subsounds">FMOD_ERR_SUBSOUNDS</a> will be returned.</li><li>A stream / sound created with <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_createstream">FMOD_CREATESTREAM</a>.  <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_err_badcommand">FMOD_ERR_BADCOMMAND</a> will be returned in this case.</li></ul>
	 * <p>
	 * The names 'lock'/'unlock' are a legacy reference to older Operating System APIs that used to cause a mutex lock on the data, so that it could not be written to while the 'lock' was in place.   This is no longer the case with FMOD and data can be 'locked' multiple times from different places/threads at once.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#createSound}
	 * <p>
	 * 
	 * @param sound
	 * @param offset <dd><p>Offset into the sound's buffer to be retrieved.</p><ul><li><span class="label">Units:</span> Bytes</li></ul>
	 * @param length <dd><p>Length of the data required to be retrieved.  If <code>offset</code> + <code>length</code> exceeds the length of the sample buffer, <code>ptr2</code> and <code>len2</code> will be valid.</p><ul><li><span class="label">Units:</span> Bytes</li></ul>
	 * @param ptr1 <span>(<i>Out</i>) <dd><p>First part of the locked data.</p><ul><li><span class="label">Units:</span> <a href="https://www.fmod.com/docs/2.03/api/glossary.html#sample-data">Sample Data</a></li></ul>
	 * @param ptr2 <span>(<i>Out</i>) <dd><p>Second part of the locked data if the <code>offset</code> + <code>length</code> has exceeded the length of the sample buffer.</p><ul><li><span class="label">Units:</span> <a href="https://www.fmod.com/docs/2.03/api/glossary.html#sample-data">Sample Data</a></li></ul>
	 * @param len1 <span>(<i>Out</i>) <dd><p>Length of <code>ptr1</code>.</p><ul><li><span class="label">Units:</span> Bytes</li></ul>
	 * @param len2 <span>(<i>Out</i>) <dd><p>Length of <code>ptr2</code></p><ul><li><span class="label">Units:</span> Bytes</li></ul>
	 */
	public static int lock(long sound, int offset, int length, org.lwjgl.PointerBuffer ptr1, org.lwjgl.PointerBuffer ptr2, java.nio.IntBuffer len1, java.nio.IntBuffer len2)
	{
		return FMOD.FMOD_Sound_Lock(sound, offset, length, ptr1, ptr2, len1, len2);
	}

	/**
	 * Reads data from an opened sound to a specified buffer, using FMOD's internal codecs.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_ReadData(
	 *   FMOD_SOUND *sound,
	 *   void *buffer,
	 *   unsigned int length,
	 *   unsigned int *read
	 * );
	 * </code></pre>
	 * <p>
	 * This can be used for decoding data offline in small pieces (or big pieces), rather than playing and capturing it, or loading the whole file at once and having to
	 * {@link FModSound#lock} / {@link FModSound#unlock} the data.
	 * <p>
	 * If too much data is read, it is possible
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_err_file_eof">FMOD_ERR_FILE_EOF</a> will be returned, meaning it is out of data.  The 'read' parameter will reflect this by returning a smaller number of bytes read than was requested.
	 * <p>
	 * As a non streaming sound reads and decodes the whole file then closes it upon calling
	 * {@link FModSystem#createSound}, {@link FModSound#readData} will then not work because the file handle is closed.  Use <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_openonly">FMOD_OPENONLY</a> to stop FMOD reading/decoding the file.<br>If <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_openonly">FMOD_OPENONLY</a> flag is used when opening a sound, it will leave the file handle open, and FMOD will not read/decode any data internally, so the read cursor will stay at position 0.  This will allow the user to read the data from the start.
	 * <p>
	 * For streams, the streaming engine will decode a small chunk of data and this will advance the read cursor.  You need to either use
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_openonly">FMOD_OPENONLY</a> to stop the stream pre-buffering or call {@link FModSound#seekData} to reset the read cursor back to the start of the file, otherwise it will appear as if the start of the stream is missing.<br>{@link FModChannel#setPosition} will have the same result.  These functions will flush the stream buffer and read in a chunk of audio internally.  This is why if you want to read from an absolute position you should use {@link FModSound#seekData} and not the previously mentioned functions.
	 * <p>
	 * If you are calling
	 * {@link FModSound#readData} and {@link FModSound#seekData} on a stream, information functions such as {@link FModChannel#getPosition} may give misleading results.  Calling {@link FModChannel#setPosition} will cause the streaming engine to reset and flush the stream, leading to the time values returning to their correct position.
	 * <p>
	 * NOTE!  Thread safety.  If you call this from another stream callback, or any other thread besides the main thread, make sure to mutex the callback with
	 * {@link FModSound#release} in case the sound is still being read from while releasing.
	 * <p>
	 * This function is thread safe to call from a stream callback or different thread as long as it doesnt conflict with a call to
	 * {@link FModSound#release}.
	 * <p>
	 * <strong>See Also:</strong> <a href="https://www.fmod.com/docs/2.03/api/advanced-core-api-topics.html#extracting-pcm-data-from-a-sound">Extracting PCM Data from a Sound</a>, {@link FModSound#lock}, {@link FModSound#unlock}
	 * <p>
	 * 
	 * @param sound
	 * @param buffer <dd><p>Buffer to read decoded data into.</p><ul><li><span class="label">Units:</span> <a href="https://www.fmod.com/docs/2.03/api/glossary.html#sample-data">Sample Data</a></li></ul>
	 * @param length <dd><p>Amount of data to read into <code>buffer</code>.</p><ul><li><span class="label">Units:</span> Bytes</li></ul>
	 */
	public static int readData(long sound, java.nio.ByteBuffer buffer, java.nio.IntBuffer length)
	{
		return FMOD.FMOD_Sound_ReadData(sound, buffer, length);
	}

	/**
	 * Frees a sound object.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_Release(
	 * FMOD_SOUND *sound
	 * );
	 * </code></pre>
	 * <p>
	 * This will stop any instances of this sound, and free the sound object and its children if it is a multi-sound object.
	 * <p>
	 * If the sound was opened with
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_nonblocking">FMOD_NONBLOCKING</a> and hasn't finished opening yet, it will block. Additionally, if the sound is still playing or has recently been stopped, the release may stall, as the mixer may still be using the sound. Using {@link FModSound#getOpenState} and checking the open state for <a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_openstate_ready">FMOD_OPENSTATE_READY</a> and <a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html#fmod_openstate_error">FMOD_OPENSTATE_ERROR</a> is a good way to avoid stalls.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSystem#createSound}, {@link FModSound#getSubSound}
	 * <p>
	 * 
	 * @param sound
	 */
	public static int release(long sound)
	{
		return FMOD.FMOD_Sound_Release(sound);
	}

	/**
	 * Seeks a sound for use with data reading, using FMOD's internal codecs.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_SeekData(
	 *   FMOD_SOUND *sound,
	 *   unsigned int pcm
	 * );
	 * </code></pre>
	 * <p>
	 * For use in conjunction with
	 * {@link FModSound#readData} and <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_openonly">FMOD_OPENONLY</a>.
	 * <p>
	 * For streaming sounds, if this function is called, it will advance the internal file pointer but not update the streaming engine.  This can lead to de-synchronization of position information for the stream and audible playback.
	 * <p>
	 * A stream can reset its stream buffer and position synchronization by calling
	 * {@link FModChannel#setPosition}.  This causes reset and flush of the stream buffer.
	 * <p>
	 * 
	 * @param sound
	 * @param pcm <dd><p>Seek Offset.</p><ul><li><span class="label">Units:</span> Samples</li></ul>
	 */
	public static int seekData(long sound, int pcm)
	{
		return FMOD.FMOD_Sound_SeekData(sound, pcm);
	}

	/**
	 * Sets the angles and attenuation levels of a 3D cone shape, for simulated occlusion which is based on direction.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_Set3DConeSettings(
	 *   FMOD_SOUND *sound,
	 *   float insideconeangle,
	 *   float outsideconeangle,
	 *   float outsidevolume
	 * );
	 * </code></pre>
	 * <p>
	 * When
	 * {@link FModChannel#set3DConeOrientation} is used and a 3D 'cone' is set up, attenuation will automatically occur for a sound based on the relative angle of the direction the cone is facing, vs the angle between the sound and the listener.
	 * <p>
	 * <ul><li>If the relative angle is within the <code>insideconeangle</code>, the sound will not have any attenuation applied.</li><li>If the relative angle is between the <code>insideconeangle</code> and <code>outsideconeangle</code>, linear volume attenuation (between 1 and <code>outsidevolume</code>) is applied between the two angles until it reaches the <code>outsideconeangle</code>.</li><li>If the relative angle is outside of the <code>outsideconeangle</code> the volume does not attenuate any further.</li></ul>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSound#get3DConeSettings}, {@link FModChannel#set3DConeSettings}
	 * <p>
	 * 
	 * @param sound
	 * @param insideConeAngle <dd><p>Inside cone angle. This is the angle spread within which the sound is unattenuated.</p><ul><li><span class="label">Units:</span> Degrees</li><li><span class="label">Range:</span> [0, <code>outsideconeangle</code>]</li><li><span class="label">Default:</span> 360</li></ul>
	 * @param outsideConeAngle <dd><p>Outside cone angle. This is the angle spread outside of which the sound is attenuated to its <code>outsidevolume</code>.</p><ul><li><span class="label">Units:</span> Degrees</li><li><span class="label">Range:</span> [<code>insideconeangle</code>, 360]</li><li><span class="label">Default:</span> 360</li></ul>
	 * @param outsideVolume <dd><p>Cone outside volume.</p><ul><li><span class="label">Units:</span> Linear</li><li><span class="label">Range:</span> [0, 1]</li><li><span class="label">Default:</span> 1</li></ul>
	 */
	public static int set3DConeSettings(long sound, float insideConeAngle, float outsideConeAngle, float outsideVolume)
	{
		return FMOD.FMOD_Sound_Set3DConeSettings(sound, insideConeAngle, outsideConeAngle, outsideVolume);
	}

	/**
	 * Sets a custom roll-off shape for 3D distance attenuation.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_Set3DCustomRolloff(
	 *   FMOD_SOUND *sound,
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
	 * When a custom roll-off is specified a sound's 3D 'minimum' and 'maximum' distances are ignored.
	 * <p>
	 * The distance in-between point values is linearly interpolated until the final point where the last value is held.
	 * <p>
	 * If the points are not sorted by distance, an error will result.
	 * <p>
	 * <div class="highlight language-c-cpp" style="display: block;"><pre>// Defining a custom array of pointsFMOD_VECTOR curve[3] ={    { 0.0f,  1.0f, 0.0f },    { 2.0f,  0.2f, 0.0f },    { 20.0f, 0.0f, 0.0f }};</pre></div>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSound#get3DCustomRolloff}, {@link FModChannel#set3DCustomRolloff}
	 * <p>
	 * 
	 * @param sound
	 * @param points <dd>Array of points sorted by distance, where <code>x</code> = distance and <code>y</code> = volume from 0 to 1. <code>z</code> should be set to 0.  Pass null or equivalent to disable custom roll-off. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_vector">FMOD_VECTOR</a>)
	 */
	public static int set3DCustomRolloff(long sound, org.lwjgl.fmod.FMOD_VECTOR.Buffer points)
	{
		return FMOD.FMOD_Sound_Set3DCustomRolloff(sound, points);
	}

	/**
	 * Sets the minimum and maximum audible distance for a 3D sound.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_Set3DMinMaxDistance(
	 *   FMOD_SOUND *sound,
	 *   float min,
	 *   float max
	 * );
	 * </code></pre>
	 * <p>
	 * The distances are meant to simulate the 'size' of a sound.  Reducing the
	 * <code>min</code> distance will mean the sound appears smaller in the world, and in some modes makes the volume attenuate faster as the listener moves away from the sound.<br>Increasing the <code>min</code> distance simulates a larger sound in the world, and in some modes makes the volume attenuate slower as the listener moves away from the sound.
	 * <p>
	 * <code>max</code> will affect attenuation differently based on roll-off mode set in the <code>mode</code> parameter of {@link FModSystem#createSound}, {@link FModSystem#createStream}, {@link FModSound#setMode} or {@link FModChannel#setMode}.
	 * <p>
	 * For these modes the volume will attenuate to 0 volume (silence), when the distance from the sound is equal to or further than the
	 * <code>max</code> distance:
	 * <p>
	 * <ul><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d_linearrolloff">FMOD_3D_LINEARROLLOFF</a></li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d_linearsquarerolloff">FMOD_3D_LINEARSQUAREROLLOFF</a></li></ul>
	 * <p>
	 * For these modes the volume will stop attenuating at the point of the
	 * <code>max</code> distance, without affecting the <em>rate</em> of attenuation:
	 * <p>
	 * <ul><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d_inverserolloff">FMOD_3D_INVERSEROLLOFF</a></li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d_inversetaperedrolloff">FMOD_3D_INVERSETAPEREDROLLOFF</a></li></ul>
	 * <p>
	 * For this mode the
	 * <code>max</code> distance is ignored:
	 * <p>
	 * <ul><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d_customrolloff">FMOD_3D_CUSTOMROLLOFF</a></li></ul>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSound#get3DMinMaxDistance}, {@link FModChannel#set3DMinMaxDistance}, {@link FModChannel#get3DMinMaxDistance}, {@link FModSystem#set3DSettings}
	 * <p>
	 * 
	 * @param sound
	 * @param min <dd><p>The sound's minimum volume distance, or the distance that the sound has no attenuation due to 3d positioning.</p><ul><li><span class="label">Units:</span> <a href="https://www.fmod.com/docs/2.03/api/glossary.html#distance-units">Distance units</a></li><li><span class="label">Default:</span> 1</li></ul>
	 * @param max <dd><p>The sound's maximum volume distance, or the distance that no additional attenuation will occur.  See below for notes on different maxdistance behaviors.</p><ul><li><span class="label">Units:</span> <a href="https://www.fmod.com/docs/2.03/api/glossary.html#distance-units">Distance units</a></li><li><span class="label">Default:</span> 10000</li></ul>
	 */
	public static int set3DMinMaxDistance(long sound, float min, float max)
	{
		return FMOD.FMOD_Sound_Set3DMinMaxDistance(sound, min, max);
	}

	/**
	 * Sets a sound's default playback attributes.
	 * <p>
	 * When the Sound is played it will use these values without having to specify them later on a per Channel basis.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_SetDefaults(
	 *   FMOD_SOUND *sound,
	 *   float frequency,
	 *   int priority
	 * );
	 * </code></pre>
	 * <p>
	 * If you only want to change one of the default values, you will need to call
	 * {@link FModSound#getDefaults} to get the other existing default value first. e.g.:
	 * <p>
	 * <pre><code>
	 * int priority;FMOD_Sound_GetDefaults(
	 * sound,
	 *  NULL,
	 *  &amp;priority
	 * );
	 * FMOD_Sound_SetDefaults(
	 * 48000,
	 *  priority
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSound#getDefaults}, {@link FModSystem#playSound}, {@link FModSystem#createSound}
	 * <p>
	 * 
	 * @param sound
	 * @param frequency <dd><p>Default playback frequency. Default value is determined by the sound source's sample rate.</p><ul><li><span class="label">Units:</span> Hertz</li><li><span class="label">Default:</span> 44100</li></ul>
	 * @param priority <dd><p>Default priority where 0 is the highest priority.</p><ul><li><span class="label">Range:</span> [0, 256]</li><li><span class="label">Default:</span> 128</li></ul>
	 */
	public static int setDefaults(long sound, float frequency, int priority)
	{
		return FMOD.FMOD_Sound_SetDefaults(sound, frequency, priority);
	}

	/**
	 * Sets the sound to loop a specified number of times before stopping if the playback mode is set to looping.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_SetLoopCount(
	 *   FMOD_SOUND *sound,
	 *   int loopcount
	 * );
	 * </code></pre>
	 * <p>
	 * Changing loop count on an already buffered stream may not produced desired output.  See
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#streaming-issues">Streaming Issues</a>.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSound#setLoopPoints}, {@link FModSound#getLoopCount}
	 * <p>
	 * 
	 * @param sound
	 * @param loopCount <dd><p>Number of times to loop before final playback where -1 is always loop.  0 means no loop.</p><ul><li><span class="label">Range:</span> [-1, inf)</li><li><span class="label">Default:</span> -1</li></ul>
	 */
	public static int setLoopCount(long sound, int loopCount)
	{
		return FMOD.FMOD_Sound_SetLoopCount(sound, loopCount);
	}

	/**
	 * Sets the loop points within a sound.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_SetLoopPoints(
	 *   FMOD_SOUND *sound,
	 *   unsigned int loopstart,
	 *   FMOD_TIMEUNIT loopstarttype,
	 *   unsigned int loopend,
	 *   FMOD_TIMEUNIT loopendtype
	 * );
	 * </code></pre>
	 * <p>
	 * The values used for
	 * <code>loopstart</code> and <code>loopend</code> are inclusive, which means these positions will be played.
	 * <p>
	 * If a
	 * <code>loopend</code> is smaller or equal to <code>loopstart</code> an error will be returned.  The same will happen for any values that are equal or greater than the length of the sound.
	 * <p>
	 * Changing loop points on an already buffered stream may not produced desired output.  See
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#streaming-issues">Streaming Issues</a>.
	 * <p>
	 * The
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-sound.html">Sound</a>'s mode must be set to <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_loop_normal">FMOD_LOOP_NORMAL</a> or <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_loop_bidi">FMOD_LOOP_BIDI</a> for loop points to affect playback.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSound#getLoopPoints}, {@link FModSound#setLoopCount}, {@link FModSound#setMode}
	 * <p>
	 * 
	 * @param sound
	 * @param loopStart <dd><p>Loop start point.</p><ul><li><span class="label">Range:</span> [0, loopend)</li></ul>
	 * @param loopStartType <dd>Time format of <code>loopstart</code>. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit">FMOD_TIMEUNIT</a>)
	 * @param loopEnd <dd><p>Loop end point.</p><ul><li><span class="label">Range:</span> [loopstart, Sound::getLength)</li></ul>
	 * @param loopEndType <dd>Time format of <code>loopend</code>. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_timeunit">FMOD_TIMEUNIT</a>)
	 */
	public static int setLoopPoints(long sound, int loopStart, int loopStartType, int loopEnd, int loopEndType)
	{
		return FMOD.FMOD_Sound_SetLoopPoints(sound, loopStart, loopStartType, loopEnd, loopEndType);
	}

	/**
	 * Sets or alters the mode of a sound.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_SetMode(
	 *   FMOD_SOUND *sound,
	 *   FMOD_MODE mode
	 * );
	 * </code></pre>
	 * <p>
	 * When calling this function, note that it will only take effect when the sound is played again with
	 * {@link FModSystem#playSound}.<br>This is the default for when the sound next plays, not a mode that will suddenly change all currently playing instances of this sound.
	 * <p>
	 * Flags supported:
	 * <p>
	 * <ul><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_loop_off">FMOD_LOOP_OFF</a></li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_loop_normal">FMOD_LOOP_NORMAL</a></li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_loop_bidi">FMOD_LOOP_BIDI</a></li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d_headrelative">FMOD_3D_HEADRELATIVE</a></li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d_worldrelative">FMOD_3D_WORLDRELATIVE</a></li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_2d">FMOD_2D</a></li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d">FMOD_3D</a></li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d_inverserolloff">FMOD_3D_INVERSEROLLOFF</a></li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d_linearrolloff">FMOD_3D_LINEARROLLOFF</a></li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d_linearsquarerolloff">FMOD_3D_LINEARSQUAREROLLOFF</a></li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d_inversetaperedrolloff">FMOD_3D_INVERSETAPEREDROLLOFF</a></li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d_customrolloff">FMOD_3D_CUSTOMROLLOFF</a></li><li><a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d_ignoregeometry">FMOD_3D_IGNOREGEOMETRY</a></li></ul>
	 * <p>
	 * If
	 * <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_3d_ignoregeometry">FMOD_3D_IGNOREGEOMETRY</a> is not specified, the flag will be cleared if it was specified previously.
	 * <p>
	 * Changing mode on an already buffered stream may not produced desired output.  See
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#streaming-issues">Streaming Issues</a>.
	 * <p>
	 * <strong>See Also:</strong>  <a href="https://www.fmod.com/docs/2.03/api/glossary.html#streaming-issues">Streaming Issues</a>, {@link FModSound#getMode}, {@link FModSound#setLoopPoints}, {@link FModSystem#playSound}
	 * <p>
	 * 
	 * @param sound
	 * @param mode <dd><p>Mode bits to set. (<a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_mode">FMOD_MODE</a>)</p><ul><li><span class="label">Default:</span> <a href="https://www.fmod.com/docs/2.03/api/core-api-common.html#fmod_default">FMOD_DEFAULT</a></li></ul>
	 */
	public static int setMode(long sound, int mode)
	{
		return FMOD.FMOD_Sound_SetMode(sound, mode);
	}

	/**
	 * Sets the volume of a MOD/S3M/XM/IT/MIDI music channel volume.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_SetMusicChannelVolume(
	 *   FMOD_SOUND *sound,
	 *   int channel,
	 *   float volume
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSound#getMusicNumChannels}, {@link FModSound#getMusicChannelVolume}
	 * <p>
	 * 
	 * @param sound
	 * @param channel <dd>MOD/S3M/XM/IT/MIDI music subchannel to set a linear volume for.
	 * @param volume <dd><p>Volume of the channel.</p><ul><li><span class="label">Units:</span> Linear</li><li><span class="label">Range:</span> [0, 1]</li><li><span class="label">Default:</span> 1</li></ul>
	 */
	public static int setMusicChannelVolume(long sound, int channel, float volume)
	{
		return FMOD.FMOD_Sound_SetMusicChannelVolume(sound, channel, volume);
	}

	/**
	 * Sets the relative speed of MOD/S3M/XM/IT/MIDI music.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_SetMusicSpeed(
	 *   FMOD_SOUND *sound,
	 *   float speed
	 * );
	 * </code></pre>
	 * <p>
	 * <strong>See Also:</strong> {@link FModSound#getMusicSpeed}
	 * <p>
	 * 
	 * @param sound
	 * @param speed <dd><p>Speed of the song.</p><ul><li><span class="label">Units:</span> Linear</li><li><span class="label">Range:</span> [0.01, 100]</li><li><span class="label">Default:</span> 1</li></ul>
	 */
	public static int setMusicSpeed(long sound, float speed)
	{
		return FMOD.FMOD_Sound_SetMusicSpeed(sound, speed);
	}

	/**
	 * Moves the sound from its existing SoundGroup to the specified sound group.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_SetSoundGroup(
	 *   FMOD_SOUND *sound,
	 *   FMOD_SOUNDGROUP *soundgroup
	 * );
	 * </code></pre>
	 * <p>
	 * By default, a sound is located in the 'master sound group'.  This can be retrieved with
	 * {@link FModSystem#getMasterSoundGroup}.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSound#getSoundGroup}, {@link FModSystem#createSoundGroup}, {@link FModSoundGroup#setMaxAudible}
	 * <p>
	 * 
	 * @param sound
	 * @param soundGroup <dd>Sound group to move the sound to. (<a href="https://www.fmod.com/docs/2.03/api/core-api-soundgroup.html">SoundGroup</a>)
	 */
	public static int setSoundGroup(long sound, long soundGroup)
	{
		return FMOD.FMOD_Sound_SetSoundGroup(sound, soundGroup);
	}

	/**
	 * Sets a user value associated with this object.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_SetUserData(
	 *   FMOD_SOUND *sound,
	 *   void *userdata
	 * );
	 * </code></pre>
	 * <p>
	 * This function allows arbitrary user data to be attached to this object. See the
	 * <a href="https://www.fmod.com/docs/2.03/api/glossary.html#user-data">User Data</a> section of the glossary for an example of how to get and set user data.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSound#getUserData}
	 * <p>
	 * 
	 * @param sound
	 * @param userdata <dd>Value stored on this object.
	 */
	public static int setUserData(long sound, long userdata)
	{
		return FMOD.FMOD_Sound_SetUserData(sound, userdata);
	}

	/**
	 * Finalizes a previous sample data lock and submits it back to the Sound object.
	 * <p>
	 * <pre><code>
	 * FMOD_RESULT FMOD_Sound_Unlock(
	 *   FMOD_SOUND *sound,
	 *   void *ptr1,
	 *   void *ptr2,
	 *   unsigned int len1,
	 *   unsigned int len2
	 * );
	 * </code></pre>
	 * <p>
	 * The data being 'unlocked' must first have been locked with
	 * {@link FModSound#lock}.
	 * <p>
	 * If an unlock is not performed on PCM data, then sample loops may produce audible clicks.
	 * <p>
	 * The names 'lock'/'unlock' are a legacy reference to older Operating System APIs that used to cause a mutex lock on the data, so that it could not be written to while the 'lock' was in place.   This is no longer the case with FMOD and data can be 'locked' multiple times from different places/threads at once.
	 * <p>
	 * <strong>See Also:</strong> {@link FModSound#lock}
	 * <p>
	 * 
	 * @param sound
	 * @param ptr1 <dd><p>First part of the locked data.</p><ul><li><span class="label">Units:</span> <a href="https://www.fmod.com/docs/2.03/api/glossary.html#sample-data">Sample Data</a></li></ul>
	 * @param ptr2 <dd><p>Second part of the locked data.</p><ul><li><span class="label">Units:</span> <a href="https://www.fmod.com/docs/2.03/api/glossary.html#sample-data">Sample Data</a></li></ul>
	 */
	public static int unlock(long sound, java.nio.ByteBuffer ptr1, java.nio.ByteBuffer ptr2)
	{
		return FMOD.FMOD_Sound_Unlock(sound, ptr1, ptr2);
	}

}
