package com.alcadrial.kirry.render.font;

import org.jspecify.annotations.NonNull;

import com.alcadrial.kirry.render.buffer.writer.BufferWriter;
import com.alcadrial.kirry.render.util.exception.VulkanException;

public interface TextFormatter<T> {
	
	int loadText(Font font, @NonNull T text, BufferWriter dst) throws VulkanException;
}
