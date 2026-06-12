package com.alcadrial.kirry.render.renderer;

import com.alcadrial.kirry.render.shader.ShaderInput;
import com.alcadrial.kirry.render.util.Tracker;

public interface IVertexResult {
	
	Tracker<int[]> track(ShaderInput input);
}
