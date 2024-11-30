package com.alcadrial.renderengine.render.util;

import org.lwjgl.vulkan.EXTDebugUtils;

public enum DebugMessengerSeverity {
	
	ERROR(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT),
	WARNING(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT),
	INFO(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT),
	VERBOSE(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT);
	
	private int bit;
	
	DebugMessengerSeverity(int bit)
	{
		this.bit = bit;
	}
	
	public int getBit()
	{
		return bit;
	}
}
