package com.alcadrial.renderengine.renderer;

import com.alcadrial.renderengine.shader.ShaderInput;
import com.alcadrial.renderengine.util.Tracker;

public interface IVertexResult {
	
	Tracker<int[]> track(ShaderInput input);
}
