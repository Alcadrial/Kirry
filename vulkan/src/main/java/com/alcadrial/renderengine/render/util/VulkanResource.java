package com.alcadrial.renderengine.render.util;

public interface VulkanResource<VK> extends AutoCloseable {
	
	// void recreate(Object... objects);
	
	VK getVk();
}
