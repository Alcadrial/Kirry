package com.alcadrial.renderengine.render.renderer;

import com.alcadrial.renderengine.render.shader.ShaderInput;
import com.alcadrial.renderengine.render.util.Tracker;

public interface IVertexResult {
	
	Tracker<int[]> track(ShaderInput input);
}
