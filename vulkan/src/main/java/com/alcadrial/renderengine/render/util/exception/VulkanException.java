package com.alcadrial.renderengine.render.util.exception;

import org.lwjgl.vulkan.VK10;

public class VulkanException extends Exception {
	
	private int vkError;
	
	public VulkanException()
	{
		vkError = VK10.VK_ERROR_UNKNOWN;
	}
	
	public VulkanException(String message)
	{
		super(message);
		vkError = VK10.VK_ERROR_UNKNOWN;
	}
	
	public VulkanException(Throwable cause)
	{
		super(cause);
		vkError = VK10.VK_ERROR_UNKNOWN;
	}
	
	public VulkanException(String message, Throwable cause)
	{
		super(message, cause);
		vkError = VK10.VK_ERROR_UNKNOWN;
	}
	
	public VulkanException(int vkError)
	{
		this.vkError = vkError;
	}
	
	public VulkanException(int vkError, String message)
	{
		super(message);
		this.vkError = vkError;
	}
	
	public VulkanException(int vkError, Throwable cause)
	{
		super(cause);
		this.vkError = vkError;
	}
	
	public VulkanException(int vkError, String message, Throwable cause)
	{
		super(message, cause);
		vkError = VK10.VK_ERROR_UNKNOWN;
	}
	
	public int getVkError()
	{
		return vkError;
	}
}
