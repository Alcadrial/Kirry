package com.alcadrial.kirry.render.window;

import com.alcadrial.kirry.render.FrameBuffer;

public interface FrameScreen extends Screen {
	
	void setOutput(FrameBuffer frameBuffer);
	FrameBuffer getOutput();
}
