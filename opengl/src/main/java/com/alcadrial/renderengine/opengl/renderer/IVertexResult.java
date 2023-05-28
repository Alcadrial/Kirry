package com.alcadrial.renderengine.opengl.renderer;

import com.alcadrial.renderengine.opengl.shader.ShaderInput;
import com.alcadrial.renderengine.opengl.util.Tracker;

public interface IVertexResult {
	
	Tracker<int[]> track(ShaderInput input);
}
