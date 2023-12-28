package com.alcadrial.renderengine.render;

import com.alcadrial.renderengine.render.buffer.VertexBuffer;
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.util.Tracker;

public interface IVertexResult {
	
	Tracker<int[]> track(ShaderVariable input);
	
	VertexBuffer getBuffer();
	
	int getIndex();
}
