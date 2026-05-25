package com.alcadrial.renderengine.render.util.future;


public interface VulkanDeferredFuture<V> extends VulkanFuture<V> {
	
	
	void start();
}
