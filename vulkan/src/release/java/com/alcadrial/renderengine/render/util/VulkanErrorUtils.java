package com.alcadrial.renderengine.render.util;

import java.util.function.Supplier;

import org.lwjgl.vulkan.EXTDebugReport;
import org.lwjgl.vulkan.EXTFullScreenExclusive;
import org.lwjgl.vulkan.EXTImageCompressionControl;
import org.lwjgl.vulkan.EXTShaderObject;
import org.lwjgl.vulkan.KHRDisplaySwapchain;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.KHRVideoEncodeQueue;
import org.lwjgl.vulkan.KHRVideoQueue;
import org.lwjgl.vulkan.NVGLSLShader;
import org.lwjgl.vulkan.VK13;

public class VulkanErrorUtils {
	
	public static <O> void match(O obj, O other)
	{
		
	}
	
	public static <O> void match(O obj, O other, String error)
	{
		
	}
	
	public static <O> O nonNull(O obj)
	{
		return obj;
	}
	
	public static <O> O nonNull(O obj, String error)
	{
		return obj;
	}
	
	public static int nonNull(int id)
	{
		return id;
	}
	
	public static int nonNull(int id, String error)
	{
		return id;
	}
	
	public static long nonNull(long pointer)
	{
		return pointer;
	}
	
	public static long nonNull(long pointer, String error)
	{
		return pointer;
	}
	
	public static void assertStatement(boolean flag, String details)
	{
		if (!flag) System.err.println("[ASSERT] " + details);
	}
	
	public static void assertNoError(int error, Supplier<String> details)
	{
		if (error < 0) throw new AssertionError(details.get() + ": " + getErrorString(error));
	}
	
	public static void assertNoError(int error, String details)
	{
		if (error < 0) System.err.println("[ASSERT] " + getErrorString(error) + " " + details);
	}
	
	public static void assertNoError(int error)
	{
		if (error < 0) System.err.println("[ASSERT] " + getErrorString(error));
	}
	
	public static String getErrorString(int error)
	{
		return switch (error)
		{
			case VK13.VK_ERROR_OUT_OF_HOST_MEMORY -> "ERROR OUT OF HOST MEMORY";
			case VK13.VK_ERROR_OUT_OF_DEVICE_MEMORY -> "ERROR OUT OF DEVICE MEMORY";
			case VK13.VK_ERROR_INITIALIZATION_FAILED -> "ERROR INITIALIZATION FAILED";
			case VK13.VK_ERROR_DEVICE_LOST -> "ERROR DEVICE LOST";
			case VK13.VK_ERROR_MEMORY_MAP_FAILED -> "ERROR MEMORY MAP FAILED";
			case VK13.VK_ERROR_LAYER_NOT_PRESENT -> "ERROR LAYER NOT PRESENT";
			case VK13.VK_ERROR_EXTENSION_NOT_PRESENT -> "ERROR EXTENSION NOT PRESENT";
			case VK13.VK_ERROR_FEATURE_NOT_PRESENT -> "ERROR FEATURE NOT PRESENT";
			case VK13.VK_ERROR_INCOMPATIBLE_DRIVER -> "ERROR INCOMPATIBLE DRIVER";
			case VK13.VK_ERROR_TOO_MANY_OBJECTS -> "ERROR TOO MANY OBJECTS";
			case VK13.VK_ERROR_FORMAT_NOT_SUPPORTED -> "ERROR FORMAT NOT SUPPORTED";
			case VK13.VK_ERROR_FRAGMENTED_POOL -> "ERROR FRAGMENTED POOL";
			case VK13.VK_ERROR_OUT_OF_POOL_MEMORY -> "VK_ERROR_OUT_OF_POOL_MEMORY";
			case VK13.VK_ERROR_INVALID_EXTERNAL_HANDLE -> "VK_ERROR_INVALID_EXTERNAL_HANDLE";
			case VK13.VK_ERROR_FRAGMENTATION -> "VK_ERROR_FRAGMENTATION";
			case VK13.VK_ERROR_INVALID_OPAQUE_CAPTURE_ADDRESS -> "VK_ERROR_INVALID_OPAQUE_CAPTURE_ADDRESS";
		
			case KHRSurface.VK_ERROR_SURFACE_LOST_KHR -> "VK_ERROR_SURFACE_LOST_KHR";
			case KHRSurface.VK_ERROR_NATIVE_WINDOW_IN_USE_KHR -> "VK_ERROR_NATIVE_WINDOW_IN_USE_KHR";
		
			case KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR -> "ERROR OUT OF DATE KHR";
		
			case KHRDisplaySwapchain.VK_ERROR_INCOMPATIBLE_DISPLAY_KHR -> "VK_ERROR_INCOMPATIBLE_DISPLAY_KHR";
		
			case NVGLSLShader.VK_ERROR_INVALID_SHADER_NV -> "VK_ERROR_INVALID_SHADER_NV";
		
			case EXTFullScreenExclusive.VK_ERROR_FULL_SCREEN_EXCLUSIVE_MODE_LOST_EXT -> "VK_ERROR_FULL_SCREEN_EXCLUSIVE_MODE_LOST_EXT";
		
			case EXTDebugReport.VK_ERROR_VALIDATION_FAILED_EXT -> "VK_ERROR_VALIDATION_FAILED_EXT";
			case EXTImageCompressionControl.VK_ERROR_COMPRESSION_EXHAUSTED_EXT -> "VK_ERROR_COMPRESSION_EXHAUSTED_EXT";
		
			case KHRVideoQueue.VK_ERROR_IMAGE_USAGE_NOT_SUPPORTED_KHR -> "VK_ERROR_IMAGE_USAGE_NOT_SUPPORTED_KHR";
			case KHRVideoQueue.VK_ERROR_VIDEO_PICTURE_LAYOUT_NOT_SUPPORTED_KHR -> "VK_ERROR_VIDEO_PICTURE_LAYOUT_NOT_SUPPORTED_KHR";
			case KHRVideoQueue.VK_ERROR_VIDEO_PROFILE_OPERATION_NOT_SUPPORTED_KHR -> "VK_ERROR_VIDEO_PROFILE_OPERATION_NOT_SUPPORTED_KHR";
			case KHRVideoQueue.VK_ERROR_VIDEO_PROFILE_FORMAT_NOT_SUPPORTED_KHR -> "VK_ERROR_VIDEO_PROFILE_FORMAT_NOT_SUPPORTED_KHR";
			case KHRVideoQueue.VK_ERROR_VIDEO_PROFILE_CODEC_NOT_SUPPORTED_KHR -> "VK_ERROR_VIDEO_PROFILE_CODEC_NOT_SUPPORTED_KHR";
			case KHRVideoQueue.VK_ERROR_VIDEO_STD_VERSION_NOT_SUPPORTED_KHR -> "VK_ERROR_VIDEO_STD_VERSION_NOT_SUPPORTED_KHR";
			case KHRVideoEncodeQueue.VK_ERROR_INVALID_VIDEO_STD_PARAMETERS_KHR -> "VK_ERROR_INVALID_VIDEO_STD_PARAMETERS_KHR";
		
			case EXTShaderObject.VK_ERROR_INCOMPATIBLE_SHADER_BINARY_EXT -> "VK_ERROR_INCOMPATIBLE_SHADER_BINARY_EXT";
		
			case VK13.VK_ERROR_UNKNOWN -> "UNKNOWN ERROR";
			default -> "UNKNOWN ERROR " + error;
		};
	}
}
