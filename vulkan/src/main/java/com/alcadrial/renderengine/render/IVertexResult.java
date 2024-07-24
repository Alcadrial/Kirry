package com.alcadrial.renderengine.render;

import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.util.Tracker;

public interface IVertexResult {
	
	Tracker<int[]> track(ShaderVariable input);
	
	VulkanBuffer getBuffer();
	
	int getIndex();
}
