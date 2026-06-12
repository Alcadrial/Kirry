package com.alcadrial.kirry.render.util;

public interface VulkanResource<VK> extends AutoCloseable, VulkanDebug {
	
	// void recreate(Object... objects);
	
	VK getVk();
	@Override
	void close();
}
