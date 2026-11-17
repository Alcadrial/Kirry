package com.alcadrial.kirry.render.font;

import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;

public interface FontRenderer extends AutoCloseable {
	
	void draw(CommandBuffer commandBuffer, int count);
	
	@Override
	void close();
}
