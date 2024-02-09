package com.alcadrial.renderengine.render.renderer;

import java.util.Map;

import com.alcadrial.renderengine.render.buffer.FloatArrayBuffer;
import com.alcadrial.renderengine.render.shader.ShaderInput;
import com.alcadrial.renderengine.render.util.InputTracker;
import com.alcadrial.renderengine.render.util.Tracker;

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
		if (inputs.containsKey(input)) return new InputTracker(inputs.get(input), buffer, input.getType().getSize());
		return Tracker.nullTracker();
	}
}