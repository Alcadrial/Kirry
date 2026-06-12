package com.alcadrial.kirry.render.renderer;

import java.util.Map;
import java.util.Objects;

import com.alcadrial.kirry.render.buffer.FloatArrayBuffer;
import com.alcadrial.kirry.render.shader.ShaderInput;
import com.alcadrial.kirry.render.util.InputTracker;
import com.alcadrial.kirry.render.util.Tracker;

public class VertexResult implements IVertexResult {
	
	private FloatArrayBuffer buffer;
	private Map<ShaderInput, Integer> inputs;
	
	public VertexResult(FloatArrayBuffer buffer, Map<ShaderInput, Integer> inputs)
	{
		this.buffer = buffer;
		this.inputs = inputs;
	}
	
	@Override
	public Tracker<int[]> track(ShaderInput input)
	{
		if (inputs.containsKey(input)) return new InputTracker(Objects.requireNonNull(inputs.get(input), "Unknown input " + input), buffer, input.getType().getSize());
		return Tracker.nullTracker();
	}
}