package com.alcadrial.kirry.render.util;

import org.lwjgl.vulkan.EXTDebugUtils;

public enum DebugMessengerType {
	
	GENERAL(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT),
	PERFORMANCE(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT),
	VALIDATION(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT);
	
	private int bit;
	
	DebugMessengerType(int bit)
	{
		this.bit = bit;
	}
	
	public int getBit()
	{
		return bit;
	}
}
