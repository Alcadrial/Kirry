package com.alcadrial.kirry.render.util.functional;

import com.alcadrial.kirry.render.util.exception.VulkanException;

@FunctionalInterface
public interface VulkanRunnable {
	
	static VulkanRunnable of(Runnable runnable)
	{
		return runnable::run;
	}
	
	void run() throws VulkanException;
}
