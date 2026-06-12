package com.alcadrial.kirry.render.util.future;


public interface VulkanDeferredFuture<V> extends VulkanFuture<V> {
	
	
	void start();
}
