package com.alcadrial.renderengine;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkSubpassDependency;
import org.lwjgl.vulkan.VkSubpassDescription;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR.Buffer;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;
import org.lwjgl.vulkan.VkViewport;

import com.alcadrial.renderengine.shader.CompiledShader;
import com.alcadrial.renderengine.shader.Shader;
import com.alcadrial.renderengine.shader.ShaderType;

public class Device implements AutoCloseable {
	
	private VkDevice device;
	private PhysicalDevice physicalDevice;
	private Queue graphicsQueue;
	private Queue presentQueue;
	private long surface;
	private SwapChain swapChain;
	private long renderPass;
	private Pipeline pipeline;
	private List<SwappingBuffer> swappingBuffers;
	private int currentFrame;
	private Map<Integer, SwappingBuffer> imagesInFlight;
	
	public Device(VkInstance instance, long window, long surface, VkDevice device, PhysicalDevice physicalDevice)
	{
		this.device = VulkanErrorUtils.nonNull(device);
		this.physicalDevice = VulkanErrorUtils.nonNull(physicalDevice);
		this.surface = VulkanErrorUtils.nonNull(surface);
		int[][] size = new int[2][1];
		GLFW.glfwGetWindowSize(VulkanErrorUtils.nonNull(window), size[0], size[1]);
		createSwapChain(size[0][0], size[1][0]);
	}
	
	private void createSwapChain(int width, int height)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkSurfaceCapabilitiesKHR capabilities = VkSurfaceCapabilitiesKHR.malloc(stack);
			VulkanErrorUtils.assertNoError(KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physicalDevice.getDevice(), surface, capabilities), "Failed to create swap chain");
			
			int[] p = new int[1];
			VulkanErrorUtils.assertNoError(KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice.getDevice(), surface, p, null), "Failed to create swap chain");
			
			Buffer formats = null;
			if (p[0] != 0)
			{
				formats = VkSurfaceFormatKHR.malloc(p[0], stack);
				VulkanErrorUtils.assertNoError(KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice.getDevice(), surface, p, formats), "Failed to create swap chain");
			}
			
			VulkanErrorUtils.assertNoError(KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice.getDevice(), surface, p, null), "Failed to create swap chain");
			
			int[] presentModes = null;
			if (p[0] != 0)
			{
				presentModes = new int[p[0]];
				VulkanErrorUtils.assertNoError(KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice.getDevice(), surface, p, presentModes), "Failed to create swap chain");
			}
			
			VkSurfaceFormatKHR format = formats.get(0);
			for (VkSurfaceFormatKHR format0 : formats) if (format0.format() == VK10.VK_FORMAT_B8G8R8_UNORM && format0.colorSpace() == KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)
			{
				format = format0;
				break;
			}
			
			int mode = KHRSurface.VK_PRESENT_MODE_FIFO_KHR;
			for (int i : presentModes) if (i == KHRSurface.VK_PRESENT_MODE_MAILBOX_KHR)
			{
				mode = i;
				break;
			}
			
			VkExtent2D extent;
			if (capabilities.currentExtent().width() != 0xFFFFFFFF)
			{
				extent = capabilities.currentExtent();
			}
			else
			{
				
				VkExtent2D actualExtent = VkExtent2D.malloc(stack).set(width, height);
				
				VkExtent2D minExtent = capabilities.minImageExtent();
				VkExtent2D maxExtent = capabilities.maxImageExtent();
				
				if (actualExtent.width() < minExtent.width()) actualExtent.width(minExtent.width());
				else if (actualExtent.width() > maxExtent.width()) actualExtent.width(maxExtent.width());
				
				if (actualExtent.height() < minExtent.height()) actualExtent.height(minExtent.height());
				else if (actualExtent.height() > maxExtent.height()) actualExtent.height(maxExtent.height());
				
				extent = actualExtent;
			}
			
			int[] images = new int[capabilities.minImageCount() + 1];
			
			if (capabilities.maxImageCount() > 0) images[0] = capabilities.maxImageCount();
			
			VkSwapchainCreateInfoKHR createInfo = VkSwapchainCreateInfoKHR.calloc(stack);
			
			createInfo.sType(KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR);
			createInfo.surface(surface);
			
			createInfo.minImageCount(images[0]);
			
			createInfo.imageFormat(format.format());
			createInfo.imageColorSpace(format.colorSpace());
			createInfo.imageExtent(extent);
			createInfo.imageArrayLayers(1);
			createInfo.imageUsage(VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);
			
			if (physicalDevice.getQueueGraphicsFamilyIndex() == physicalDevice.getQueueSurfaceFamilyIndex()) createInfo.imageSharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);
			else
			{
				createInfo.imageSharingMode(VK10.VK_SHARING_MODE_CONCURRENT);
				createInfo.pQueueFamilyIndices(stack.ints(physicalDevice.getQueueGraphicsFamilyIndex(), physicalDevice.getQueueSurfaceFamilyIndex()));
			}
			
			createInfo.preTransform(capabilities.currentTransform());
			createInfo.compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
			createInfo.presentMode(mode);
			createInfo.clipped(true);
			
			createInfo.oldSwapchain(VK10.VK_NULL_HANDLE);
			
			long[] p0 = new long[1];
			VulkanErrorUtils.assertNoError(KHRSwapchain.vkCreateSwapchainKHR(device, createInfo, null, p0), "Failed to create swap chain");
			swapChain = new SwapChain(this, p0[0], images, format, extent);
			
			VkAttachmentDescription.Buffer colorAttachment = VkAttachmentDescription.calloc(1, stack);
			colorAttachment.format(swapChain.getImageFormat());
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
			
			VkSubpassDescription.Buffer subpass = VkSubpassDescription.calloc(1, stack);
			subpass.pipelineBindPoint(VK10.VK_PIPELINE_BIND_POINT_GRAPHICS);
			subpass.colorAttachmentCount(1);
			subpass.pColorAttachments(colorAttachmentRef);
			
			VkSubpassDependency.Buffer dependency = VkSubpassDependency.calloc(1, stack);
			dependency.srcSubpass(VK10.VK_SUBPASS_EXTERNAL);
			dependency.dstSubpass(0);
			dependency.srcStageMask(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
			dependency.srcAccessMask(0);
			dependency.dstStageMask(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
			dependency.dstAccessMask(VK10.VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK10.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);
			
			VkRenderPassCreateInfo renderPassInfo = VkRenderPassCreateInfo.calloc(stack);
			renderPassInfo.sType(VK10.VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO);
			renderPassInfo.pAttachments(colorAttachment);
			renderPassInfo.pSubpasses(subpass);
			renderPassInfo.pDependencies(dependency);
			
			VulkanErrorUtils.assertNoError(VK10.vkCreateRenderPass(device, renderPassInfo, null, p0), "Failed to create render pass");
			
			renderPass = p0[0];
		}
	}
	
	public void buildGraphicPipeline(Shader vertexShader, Shader fragmentShader)
	{
		VulkanErrorUtils.match(vertexShader.getType(), ShaderType.VERTEX, "Not a vertex shader");
		VulkanErrorUtils.match(fragmentShader.getType(), ShaderType.FRAGMENT, "Not a fragment shader");
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			CompiledShader vertexCompiled = vertexShader.getOrCompile();
			CompiledShader fragmentCompiled = fragmentShader.getOrCompile();
			
			if (vertexCompiled.hasErrors()) throw new IllegalStateException("Shader " + vertexShader.getFile() + " cannot be used ");
			if (fragmentCompiled.hasErrors()) throw new IllegalStateException("Shader " + fragmentShader.getFile() + " cannot be used ");
			
			long vertShaderModule = createShaderModule(vertexCompiled.getBinary());
			long fragShaderModule = createShaderModule(fragmentCompiled.getBinary());
			
			ByteBuffer entryPoint = stack.UTF8("main");
			
			VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.calloc(2, stack);
			
			VkPipelineShaderStageCreateInfo vertShaderStageInfo = shaderStages.get(0);
			
			vertShaderStageInfo.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
			vertShaderStageInfo.stage(VK10.VK_SHADER_STAGE_VERTEX_BIT);
			vertShaderStageInfo.module(vertShaderModule);
			vertShaderStageInfo.pName(entryPoint);
			
			VkPipelineShaderStageCreateInfo fragShaderStageInfo = shaderStages.get(1);
			
			fragShaderStageInfo.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
			fragShaderStageInfo.stage(VK10.VK_SHADER_STAGE_FRAGMENT_BIT);
			fragShaderStageInfo.module(fragShaderModule);
			fragShaderStageInfo.pName(entryPoint);
			
			VkPipelineVertexInputStateCreateInfo vertexInputInfo = VkPipelineVertexInputStateCreateInfo.calloc(stack);
			vertexInputInfo.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
			
			VkPipelineInputAssemblyStateCreateInfo inputAssembly = VkPipelineInputAssemblyStateCreateInfo.calloc(stack);
			inputAssembly.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);
			inputAssembly.topology(VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
			inputAssembly.primitiveRestartEnable(false);
			
			VkExtent2D extent = swapChain.getExtent();
			
			VkViewport.Buffer viewport = VkViewport.calloc(1, stack);
			viewport.x(0);
			viewport.y(0);
			viewport.width(extent.width());
			viewport.height(extent.height());
			viewport.minDepth(0);
			viewport.maxDepth(1);
			
			VkRect2D.Buffer scissor = VkRect2D.calloc(1, stack);
			scissor.offset(VkOffset2D.calloc(stack).set(0, 0));
			scissor.extent(extent);
			
			VkPipelineViewportStateCreateInfo viewportState = VkPipelineViewportStateCreateInfo.calloc(stack);
			viewportState.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO);
			viewportState.pViewports(viewport);
			viewportState.pScissors(scissor);
			
			VkPipelineRasterizationStateCreateInfo rasterizer = VkPipelineRasterizationStateCreateInfo.calloc(stack);
			rasterizer.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO);
			rasterizer.depthClampEnable(false);
			rasterizer.rasterizerDiscardEnable(false);
			rasterizer.polygonMode(VK10.VK_POLYGON_MODE_FILL);
			rasterizer.lineWidth(1);
			rasterizer.cullMode(VK10.VK_CULL_MODE_BACK_BIT);
			rasterizer.frontFace(VK10.VK_FRONT_FACE_CLOCKWISE);
			rasterizer.depthBiasEnable(false);
			
			VkPipelineMultisampleStateCreateInfo multisampling = VkPipelineMultisampleStateCreateInfo.calloc(stack);
			multisampling.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO);
			multisampling.sampleShadingEnable(false);
			multisampling.rasterizationSamples(VK10.VK_SAMPLE_COUNT_1_BIT);
			
			VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachment = VkPipelineColorBlendAttachmentState.calloc(1, stack);
			colorBlendAttachment.colorWriteMask(VK10.VK_COLOR_COMPONENT_R_BIT | VK10.VK_COLOR_COMPONENT_G_BIT | VK10.VK_COLOR_COMPONENT_B_BIT | VK10.VK_COLOR_COMPONENT_A_BIT);
			colorBlendAttachment.blendEnable(false);
			
			VkPipelineColorBlendStateCreateInfo colorBlending = VkPipelineColorBlendStateCreateInfo.calloc(stack);
			colorBlending.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO);
			colorBlending.logicOpEnable(false);
			colorBlending.logicOp(VK10.VK_LOGIC_OP_COPY);
			colorBlending.pAttachments(colorBlendAttachment);
			colorBlending.blendConstants(stack.floats(0, 0, 0, 0));
			
			VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack);
			pipelineLayoutInfo.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
			
			long[] p = new long[1];
			
			VulkanErrorUtils.assertNoError(VK10.vkCreatePipelineLayout(device, pipelineLayoutInfo, null, p), "Failed to create pipeline layout");
			
			long pipelineLayout = p[0];
			
			VkGraphicsPipelineCreateInfo.Buffer pipelineInfo = VkGraphicsPipelineCreateInfo.calloc(1, stack);
			pipelineInfo.sType(VK10.VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO);
			pipelineInfo.pStages(shaderStages);
			pipelineInfo.pVertexInputState(vertexInputInfo);
			pipelineInfo.pInputAssemblyState(inputAssembly);
			pipelineInfo.pViewportState(viewportState);
			pipelineInfo.pRasterizationState(rasterizer);
			pipelineInfo.pMultisampleState(multisampling);
			pipelineInfo.pColorBlendState(colorBlending);
			pipelineInfo.layout(pipelineLayout);
			pipelineInfo.renderPass(renderPass);
			pipelineInfo.subpass(0);
			pipelineInfo.basePipelineHandle(VK10.VK_NULL_HANDLE);
			pipelineInfo.basePipelineIndex(-1);
			
			VulkanErrorUtils.assertNoError(VK10.vkCreateGraphicsPipelines(device, 0, pipelineInfo, null, p), "Failed to create graphics pipeline");
			
			pipeline = new Pipeline(this, pipelineLayout, p[0]);
			
			VK10.vkDestroyShaderModule(device, vertShaderModule, null);
			VK10.vkDestroyShaderModule(device, fragShaderModule, null);
		}
	}
	
	private long createShaderModule(byte[] binary)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkShaderModuleCreateInfo createInfo = VkShaderModuleCreateInfo.calloc(stack);
			
			createInfo.sType(VK10.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO);
			ByteBuffer bb = stack.malloc(binary.length);
			for (byte b : binary) bb.put(b);
			createInfo.pCode(bb.rewind());
			
			long[] p = new long[1];
			VulkanErrorUtils.assertNoError(VK10.vkCreateShaderModule(device, createInfo, null, p), "Failed to create shader module");
			
			return p[0];
		}
	}
	
	public void createSwappingBuffers(int buffers)
	{
		if (buffers < 1) throw new IllegalArgumentException("Required at least 1 buffer");
		
		swappingBuffers = new ArrayList<>(buffers);
		imagesInFlight = new HashMap<>(swapChain.getImages().size());
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkSemaphoreCreateInfo semaphoreInfo = VkSemaphoreCreateInfo.calloc(stack);
			semaphoreInfo.sType(VK10.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);
			
			VkFenceCreateInfo fenceInfo = VkFenceCreateInfo.calloc(stack);
			fenceInfo.sType(VK10.VK_STRUCTURE_TYPE_FENCE_CREATE_INFO);
			fenceInfo.flags(VK10.VK_FENCE_CREATE_SIGNALED_BIT);
			
			long[][] p = new long[3][1];
			
			for (int i = 0; i < buffers; i++)
			{
				VulkanErrorUtils.assertNoError(VK10.vkCreateSemaphore(device, semaphoreInfo, null, p[0]), "Failed to create swapping buffers for the frame " + i);
				VulkanErrorUtils.assertNoError(VK10.vkCreateSemaphore(device, semaphoreInfo, null, p[1]), "Failed to create swapping buffers for the frame " + i);
				VulkanErrorUtils.assertNoError(VK10.vkCreateFence(device, fenceInfo, null, p[2]), "Failed to create swapping buffers for the frame " + i);
				
				swappingBuffers.add(new SwappingBuffer(this, p[0][0], p[1][0], p[2][0]));
			}
		}
		currentFrame = 0;
	}
	
	public void draw()
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			SwappingBuffer buffer = swappingBuffers.get(currentFrame);
			
			VK10.vkWaitForFences(device, buffer.getFence(), true, -1);
			
			IntBuffer p = stack.callocInt(1);
			
			KHRSwapchain.vkAcquireNextImageKHR(device, swapChain.getSwapchain(), -1, buffer.getImageAvailable(), 0, p);
			int imageIndex = p.get(0);
			
			if (imagesInFlight.containsKey(imageIndex))
			{
				VK10.vkWaitForFences(device, imagesInFlight.get(imageIndex).getFence(), true, -1);
			}
			
			imagesInFlight.put(imageIndex, buffer);
			
			LongBuffer p0 = stack.callocLong(1);
			p0.put(0, buffer.getImageAvailable());
			LongBuffer p1 = stack.callocLong(1);
			p1.put(0, buffer.getRenderFinished());
			
			VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack);
			submitInfo.sType(VK10.VK_STRUCTURE_TYPE_SUBMIT_INFO);
			
			submitInfo.waitSemaphoreCount(1);
			submitInfo.pWaitSemaphores(p0);
			submitInfo.pWaitDstStageMask(stack.ints(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT));
			
			submitInfo.pSignalSemaphores(p1);
			
			submitInfo.pCommandBuffers(stack.pointers(swapChain.getCommands().get(imageIndex)));
			
			VK10.vkResetFences(device, buffer.getFence());
			
			VulkanErrorUtils.assertNoError(VK10.vkQueueSubmit(graphicsQueue.getQueue(), submitInfo, buffer.getFence()), "Failed to submit draw command buffer");// FAIL
			
			VkPresentInfoKHR presentInfo = VkPresentInfoKHR.calloc(stack);
			presentInfo.sType(KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);
			
			presentInfo.pWaitSemaphores(p1);
			
			presentInfo.swapchainCount(1);
			presentInfo.pSwapchains(stack.longs(swapChain.getSwapchain()));
			
			presentInfo.pImageIndices(p);
			
			KHRSwapchain.vkQueuePresentKHR(presentQueue.getQueue(), presentInfo);
			
			currentFrame = (currentFrame + 1) % swappingBuffers.size();
		}
	}
	
	public VkDevice getDevice()
	{
		return device;
	}
	
	public PhysicalDevice getPhysicalDevice()
	{
		return physicalDevice;
	}
	
	public Queue getGraphicsQueue()
	{
		return graphicsQueue;
	}
	
	public void setGraphicsQueue(Queue queue)
	{
		VulkanErrorUtils.match(this, queue.getDevice(), "Cannot set a queue of another device");
		graphicsQueue = queue;
	}
	
	public Queue getPresentQueue()
	{
		return presentQueue;
	}
	
	public void setPresentQueue(Queue queue)
	{
		VulkanErrorUtils.match(this, queue.getDevice(), "Cannot set a queue of another device");
		presentQueue = queue;
	}
	
	public long getSurface()
	{
		return surface;
	}
	
	public SwapChain getSwapChain()
	{
		return swapChain;
	}
	
	public long getRenderPass()
	{
		return renderPass;
	}
	
	public Pipeline getPipeline()
	{
		return pipeline;
	}
	
	@Override
	public void close()
	{
		if (swappingBuffers != null) for (SwappingBuffer swappingBuffer : swappingBuffers) swappingBuffer.close();
		if (pipeline != null) pipeline.close();
		VK10.vkDestroyRenderPass(device, renderPass, null);
		swapChain.close();
		KHRSwapchain.vkDestroySwapchainKHR(device, swapChain.getSwapchain(), null);
		VK10.vkDestroyDevice(device, null);
	}
}
