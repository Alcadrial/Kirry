package com.alcadrial.renderengine.render.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.EXTDebugReport;
import org.lwjgl.vulkan.EXTDebugUtils;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSubpassDependency;
import org.lwjgl.vulkan.VkSubpassDescription;

import com.alcadrial.kroperties.Property;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.PhysicalDevice;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.RenderPass;
import com.alcadrial.renderengine.render.Vulkan;
import com.alcadrial.renderengine.render.shader.PreparedShader;
import com.alcadrial.renderengine.render.shader.Shader;
import com.alcadrial.renderengine.render.shader.ShaderInfo;
import com.alcadrial.renderengine.render.texture.Texture;
import com.alcadrial.renderengine.render.util.PhysicalDeviceNotFoundException;
import com.alcadrial.renderengine.render.util.Properties;
import com.alcadrial.renderengine.render.util.Version;
import com.alcadrial.renderengine.render.window.VulkanWindow;

public class RenderTest {
	
	protected static final Object MONITOR = new Object();
	private static final String[] SEVERITIES = new String[] {"UNKNOWN", "VERBOSE", "INFO", "WARNING", "ERROR"};
	private static final String[] TYPES = new String[] {"UNKNOWN", "GENERAL", "VALIDATION", "PERFORMANCE"};
	
	public static void initRenderEngine()
	{
		RenderEngine.Builder builder = RenderEngine.create("Test", new Version(0, 0, 1, 0));
		builder.messengerCallback(RenderTest::debugCallback);
		builder.instanceLayersDebug("VK_LAYER_KHRONOS_validation");
		builder.instanceExtensionsDebug(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME, EXTDebugReport.VK_EXT_DEBUG_REPORT_EXTENSION_NAME);
		builder.multiThreading(false);
		builder.initialize();
	}
	
	public static void initMultiThreadRenderEngine()
	{
		RenderEngine.Builder builder = RenderEngine.create("Test", new Version(0, 0, 1, 0));
		builder.messengerCallback(RenderTest::debugCallback);
		builder.instanceLayersDebug("VK_LAYER_KHRONOS_validation");
		builder.instanceExtensionsDebug(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME, EXTDebugReport.VK_EXT_DEBUG_REPORT_EXTENSION_NAME);
		builder.multiThreading(true);
		builder.initialize();
	}
	
	public static int debugCallback(int messageSeverity, int messageType, long callbackDataId, long userDataId)
	{
		VkDebugUtilsMessengerCallbackDataEXT callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(callbackDataId);
		
		int i = switch (messageSeverity)
		{
			case 0x0001 -> 1;
			case 0x0010 -> 2;
			case 0x0100 -> 3;
			case 0x1000 -> 4;
			default -> 0;
		};
		int j = switch (messageType)
		{
			case 0b001 -> 1;
			case 0b010 -> 2;
			case 0b100 -> 3;
			default -> 0;
		};
		
		System.out.println("[Vulkan debug/" + SEVERITIES[i] + " " + TYPES[j] + "] " + callbackData.pMessageString());
		
		return VK10.VK_FALSE;
	}
	
	public static boolean pickPhysicalDevice(VulkanWindow window)
	{
		try
		{
			Vulkan vulkan = RenderEngine.getVulkan();
			vulkan.setPhysicalDeviceFilter(physicalDevice -> physicalDevice.getQueueFamily(VK10.VK_QUEUE_GRAPHICS_BIT | VK10.VK_QUEUE_COMPUTE_BIT, window.getSurface()) != null);
			if (TestProperties.DEVICE.isDefault()) vulkan.pickPhysicalDevices(PhysicalDevice.typeBestMatcher());
			else vulkan.pickPhysicalDevices(TestProperties.DEVICE.getValue());
			return true;
		}
		catch (PhysicalDeviceNotFoundException e)
		{
			e.printStackTrace(System.out);
			RenderEngine.terminate();
			return false;
		}
	}
	
	public static Texture createDepthTexture(int width, int height)
	{
		Texture depthTexture = Texture.create2dTexture(RenderEngine.getVulkan().getDevice(), width, height, VK10.VK_FORMAT_D24_UNORM_S8_UINT, VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT, VK10.VK_IMAGE_ASPECT_DEPTH_BIT | VK10.VK_IMAGE_ASPECT_STENCIL_BIT);
		depthTexture.changeLayout(VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
		return depthTexture;
	}
	
	public static RenderPass createRenderPass(Device device, int colorFormat)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkAttachmentDescription.Buffer attachmentDescriptions = VkAttachmentDescription.calloc(1, stack);
			
			VkAttachmentDescription colorAttachment = attachmentDescriptions.get();
			colorAttachment.format(colorFormat);
			colorAttachment.samples(VK10.VK_SAMPLE_COUNT_1_BIT);
			colorAttachment.loadOp(VK10.VK_ATTACHMENT_LOAD_OP_CLEAR);
			colorAttachment.storeOp(VK10.VK_ATTACHMENT_STORE_OP_STORE);
			colorAttachment.stencilLoadOp(VK10.VK_ATTACHMENT_LOAD_OP_DONT_CARE);
			colorAttachment.stencilStoreOp(VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE);
			colorAttachment.initialLayout(VK10.VK_IMAGE_LAYOUT_UNDEFINED);
			colorAttachment.finalLayout(KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);
			
			VkAttachmentReference.Buffer colorAttachmentRef = VkAttachmentReference.calloc(1, stack);
			colorAttachmentRef.attachment(0);
			colorAttachmentRef.layout(VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
			
			VkSubpassDescription.Buffer subpassDescription = VkSubpassDescription.calloc(1, stack);
			subpassDescription.pipelineBindPoint(VK10.VK_PIPELINE_BIND_POINT_GRAPHICS);
			subpassDescription.colorAttachmentCount(1);
			subpassDescription.pColorAttachments(colorAttachmentRef);
			
			int srcStageMask = VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
			int srcAccessMask = 0;
			int dstStageMask = VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
			int dstAccessMask = VK10.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT;
			
			VkSubpassDependency.Buffer subpassDependency = VkSubpassDependency.calloc(1, stack);
			subpassDependency.srcSubpass(VK10.VK_SUBPASS_EXTERNAL);
			subpassDependency.dstSubpass(0);
			subpassDependency.srcStageMask(srcStageMask);
			subpassDependency.srcAccessMask(srcAccessMask);
			subpassDependency.dstStageMask(dstStageMask);
			subpassDependency.dstAccessMask(dstAccessMask);
			
			VkRenderPassCreateInfo renderPassCreateInfo = VkRenderPassCreateInfo.calloc(stack);
			renderPassCreateInfo.sType(VK10.VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO);
			renderPassCreateInfo.pAttachments(attachmentDescriptions.rewind());
			renderPassCreateInfo.pSubpasses(subpassDescription);
			renderPassCreateInfo.pDependencies(subpassDependency);
			
			return new RenderPass(device, renderPassCreateInfo);
		}
	}
	
	public static RenderPass createRenderPassWithDepth(Device device, int colorFormat)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkAttachmentDescription.Buffer attachmentDescriptions = VkAttachmentDescription.calloc(2, stack);
			
			VkAttachmentDescription colorAttachment = attachmentDescriptions.get();
			colorAttachment.format(colorFormat);
			colorAttachment.samples(VK10.VK_SAMPLE_COUNT_1_BIT);
			colorAttachment.loadOp(VK10.VK_ATTACHMENT_LOAD_OP_CLEAR);
			colorAttachment.storeOp(VK10.VK_ATTACHMENT_STORE_OP_STORE);
			colorAttachment.stencilLoadOp(VK10.VK_ATTACHMENT_LOAD_OP_DONT_CARE);
			colorAttachment.stencilStoreOp(VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE);
			colorAttachment.initialLayout(VK10.VK_IMAGE_LAYOUT_UNDEFINED);
			colorAttachment.finalLayout(KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);
			
			VkAttachmentReference.Buffer colorAttachmentRef = VkAttachmentReference.calloc(1, stack);
			colorAttachmentRef.attachment(0);
			colorAttachmentRef.layout(VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
			
			VkAttachmentDescription depthAttachment = attachmentDescriptions.get();
			depthAttachment.format(VK10.VK_FORMAT_D24_UNORM_S8_UINT);//dynamic
			depthAttachment.samples(VK10.VK_SAMPLE_COUNT_1_BIT);
			depthAttachment.loadOp(VK10.VK_ATTACHMENT_LOAD_OP_CLEAR);
			depthAttachment.storeOp(VK10.VK_ATTACHMENT_STORE_OP_STORE);
			depthAttachment.stencilLoadOp(VK10.VK_ATTACHMENT_LOAD_OP_DONT_CARE);
			depthAttachment.stencilStoreOp(VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE);
			depthAttachment.initialLayout(VK10.VK_IMAGE_LAYOUT_UNDEFINED);
			depthAttachment.finalLayout(VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
			
			VkAttachmentReference depthAttachmentRef = VkAttachmentReference.calloc(stack);
			depthAttachmentRef.attachment(1);
			depthAttachmentRef.layout(VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
			
			VkSubpassDescription.Buffer subpassDescription = VkSubpassDescription.calloc(1, stack);
			subpassDescription.pipelineBindPoint(VK10.VK_PIPELINE_BIND_POINT_GRAPHICS);
			subpassDescription.colorAttachmentCount(1);
			subpassDescription.pColorAttachments(colorAttachmentRef);
			subpassDescription.pDepthStencilAttachment(depthAttachmentRef);
			
			int srcStageMask = VK10.VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT | VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
			int srcAccessMask = 0;
			int dstStageMask = VK10.VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT | VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
			int dstAccessMask = VK10.VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT | VK10.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT;
			
			VkSubpassDependency.Buffer subpassDependency = VkSubpassDependency.calloc(1, stack);
			subpassDependency.srcSubpass(VK10.VK_SUBPASS_EXTERNAL);
			subpassDependency.dstSubpass(0);
			subpassDependency.srcStageMask(srcStageMask);
			subpassDependency.srcAccessMask(srcAccessMask);
			subpassDependency.dstStageMask(dstStageMask);
			subpassDependency.dstAccessMask(dstAccessMask);
			
			VkRenderPassCreateInfo renderPassCreateInfo = VkRenderPassCreateInfo.calloc(stack);
			renderPassCreateInfo.sType(VK10.VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO);
			renderPassCreateInfo.pAttachments(attachmentDescriptions.rewind());
			renderPassCreateInfo.pSubpasses(subpassDescription);
			renderPassCreateInfo.pDependencies(subpassDependency);
			
			return new RenderPass(device, renderPassCreateInfo);
		}
	}
	
	public static File getFile(String path)
	{
		return new File(Properties.RESOURCE_PATH.getValue(), "assets/renderengine/" + path);
	}
	
	public static PreparedShader loadShader(String path) throws IOException
	{
		Shader shader = new Shader(getFile(path));
		ShaderInfo info = ShaderInfo.fromJson(getFile(path + ".json"));
		if (TestProperties.FORCE_RECOMPILE_SHADERS.getValue()) shader.compile(info);
		else
		{
			File binFile = getFile(path + ".bin");
			if (binFile.canRead()) shader.loadCompiled(binFile);
			else shader.compile(info);
		}
		if (!shader.isCompilationSuccessfull()) throw new IOException("Shader " + shader.getFile() + " has errors");
		return new PreparedShader(shader, info);
	}
	
	public static void saveShaderBinary(Shader shader) throws IOException
	{
		Files.write(new File(shader.getFile().getPath() + ".bin").toPath(), shader.getLastCompilation().get().getBinary());
	}
	
	public static void println(String s, Runnable action, Property<Boolean> printProperty)
	{
		if (printProperty.getValue()) System.out.println(s + " time = " + recordTime(action, TimeUnit.MICROSECONDS) / 1000D + "ms");
		else action.run();
	}
	
	public static long recordTime(Runnable runnable, TimeUnit unit)
	{
		long startTime;
		TimeUnit localUnit;
		if (unit == TimeUnit.MICROSECONDS || unit == TimeUnit.NANOSECONDS)
		{
			startTime = System.nanoTime();
			localUnit = TimeUnit.NANOSECONDS;
		}
		else
		{
			startTime = System.currentTimeMillis();
			localUnit = TimeUnit.MILLISECONDS;
		}
		
		runnable.run();
		
		long endTime;
		if (localUnit == TimeUnit.NANOSECONDS) endTime = System.nanoTime();
		else endTime = System.currentTimeMillis();
		
		return unit.convert(endTime - startTime, localUnit);
	}
	
	public static double recordTime(Runnable runnable, TimeUnit unit, TimeUnit resolution)
	{
		long startTime;
		TimeUnit localUnit;
		if (resolution == TimeUnit.MICROSECONDS || resolution == TimeUnit.NANOSECONDS)
		{
			startTime = System.nanoTime();
			localUnit = TimeUnit.NANOSECONDS;
		}
		else
		{
			startTime = System.currentTimeMillis();
			localUnit = TimeUnit.MILLISECONDS;
		}
		
		runnable.run();
		
		long endTime;
		if (localUnit == TimeUnit.NANOSECONDS) endTime = System.nanoTime();
		else endTime = System.currentTimeMillis();
		
		return (double) resolution.convert(endTime - startTime, localUnit) / resolution.convert(1, unit);
	}
}
