package com.alcadrial.renderengine.render.renderer;

import java.util.HashMap;
import java.util.Map;

import com.alcadrial.renderengine.render.buffer.FloatArrayBuffer;
import com.alcadrial.renderengine.render.shader.ShaderInput;
import com.alcadrial.renderengine.render.shader.ShaderProgram;

public class VertexBuilder extends AbstractVertexBuilder<VertexBuilder, VertexResult> {
	
	private FloatArrayBuffer arrayBuffer;
	
	public VertexBuilder(ShaderProgram program, FloatArrayBuffer arrayBuffer, float x, float y, float z)
	{
		super(program, x, y, z);
		this.arrayBuffer = arrayBuffer;
	}
	
	@Override
	public VertexResult end()
	{
		int index = arrayBuffer.size();
		arrayBuffer.addAll(gatherProgramData());
		map.clear();
		Map<ShaderInput, Integer> inputs = new HashMap<>();
		for (ShaderInput input : program.getProgramInputs())
		{
			inputs.put(input, index);
			index += input.getType().getSize();
		}
		return new VertexResult(arrayBuffer, inputs);
	}
}