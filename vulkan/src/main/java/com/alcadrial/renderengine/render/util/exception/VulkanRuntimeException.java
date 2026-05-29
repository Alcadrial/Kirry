package com.alcadrial.renderengine.render.util.exception;

import org.jspecify.annotations.NonNull;

public class VulkanRuntimeException extends RuntimeException {
	
	public VulkanRuntimeException(VulkanException cause)
	{
		super(cause);
	}
	@Override
	public @NonNull VulkanException getCause()
	{
		return (@NonNull VulkanException) super.getCause();
	}
}
