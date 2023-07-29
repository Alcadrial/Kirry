package com.alcadrial.renderengine.render;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkCopyDescriptorSet;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkSubpassDependency;
import org.lwjgl.vulkan.VkSubpassDescription;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;
import org.lwjgl.vulkan.VkViewport;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.renderengine.core.util.color.Color;
import com.alcadrial.renderengine.render.buffer.IndexBuffer;
import com.alcadrial.renderengine.render.buffer.UniformBuffer;
import com.alcadrial.renderengine.render.buffer.VertexBuffer;
import com.alcadrial.renderengine.render.pipeline.GraphicsPipeline;
import com.alcadrial.renderengine.render.pipeline.PipelineLayout;
import com.alcadrial.renderengine.render.shader.Shader;
import com.alcadrial.renderengine.render.util.DataAccumulator;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.VulkanUtils;
import com.alcadrial.renderengine.render.window.Window;

public class Renderer implements AutoCloseable {
	
	public static Builder create()
	{
		return new Builder();
	}
	
	private Device device;
	private GraphicsPipeline pipeline;
	private SwapChain swapChain;
	private Window targetWindow;
	private Color clearColor;
	private VertexBuffer vertexBuffer;
	private IndexBuffer indexBuffer;
	private List<Long> descriptorSetLayouts;
	private long[] descriptorSets;
	private long descriptorPool;
	
	
	private Renderer(Window targetWindow, Device device, GraphicsPipeline pipeline, SwapChain swapChain, List<Long> descriptorSetLayouts, long[] descriptorSets, long descriptorPool)
	{
		this.device = device;
		this.targetWindow = targetWindow;
		this.pipeline = pipeline;
		this.swapChain = swapChain;
		this.descriptorSetLayouts = descriptorSetLayouts;
		this.descriptorSets = descriptorSets;
		this.descriptorPool = descriptorPool;
		clearColor = Color.BLACK;
	}
	
	public VertexAccumulator accumulate()
	{
		return new VertexAccumulator(pipeline, vertexBuffer);
	}
	
	public void draw(Matrix4f matrix)
	{
		CommandBufferData commandBuffer = swapChain.startRegisteringCommands(null, null, clearColor);
		commandBuffer.bindPipeline(pipeline);
		commandBuffer.bindVertexBuffers(0, vertexBuffer);
		commandBuffer.bindIndexBuffer(indexBuffer, 0);
		commandBuffer.bindDescriptorSets(pipeline, descriptorSets, new int[0]);
		commandBuffer.drawIndexed((int) indexBuffer.getSize() / 4, 1, 0, 0, 0);
		swapChain.endRegisteringCommands();
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			IntBuffer p = stack.callocInt(1);
			
			int result = KHRSwapchain.vkAcquireNextImageKHR(device.getVk(), swapChain.getVk(), Integer.MAX_VALUE, commandBuffer.getImageAvailableSemaphore(), 0, p);
			if (result == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR)
			{
				VkSwapchainCreateInfoKHR createInfo = VkSwapchainCreateInfoKHR.calloc(stack);
				createInfo.sType(KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR);
				createInfo.minImageCount(2);
				createInfo.imageArrayLayers(1);
				createInfo.imageUsage(VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);
				PhysicalDevice physicalDevice = device.getPhysicalDevice();
				if (physicalDevice.getQueueGraphicsFamilyIndex() == physicalDevice.getQueueSurfaceFamilyIndex()) createInfo.imageSharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);
				else
				{
					createInfo.imageSharingMode(VK10.VK_SHARING_MODE_CONCURRENT);
					createInfo.pQueueFamilyIndices(stack.ints(physicalDevice.getQueueGraphicsFamilyIndex(), physicalDevice.getQueueSurfaceFamilyIndex()));
				}
				createInfo.compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
				createInfo.clipped(true);
				swapChain.recreate(device, targetWindow.getSurface(), createInfo);
				return;
			}
			VulkanErrorUtils.assertNoError(result, "Error acquiring next image");
			
			LongBuffer p0 = stack.callocLong(1);
			p0.put(0, commandBuffer.getImageAvailableSemaphore());
			LongBuffer p1 = stack.callocLong(1);
			p1.put(0, commandBuffer.getRenderFinishedSemaphore());
			
			VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack);
			submitInfo.sType(VK10.VK_STRUCTURE_TYPE_SUBMIT_INFO);
			submitInfo.waitSemaphoreCount(1);
			submitInfo.pWaitSemaphores(p0);
			submitInfo.pWaitDstStageMask(stack.ints(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT));
			submitInfo.pSignalSemaphores(p1);
			submitInfo.pCommandBuffers(VulkanUtils.toAddressBuffer(stack, commandBuffer));
			
			commandBuffer.resetFence();
			
			VulkanErrorUtils.assertNoError(VK10.vkQueueSubmit(device.getGraphicsQueue().getVk(), submitInfo, commandBuffer.getFence()), "Failed to submit draw command buffer");// FAIL
			
			VkPresentInfoKHR presentInfo = VkPresentInfoKHR.calloc(stack);
			presentInfo.sType(KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);
			presentInfo.pWaitSemaphores(p1);
			presentInfo.swapchainCount(1);
			presentInfo.pSwapchains(stack.longs(swapChain.getVk()));
			presentInfo.pImageIndices(p);
			
			result = KHRSwapchain.vkQueuePresentKHR(device.getPresentQueue().getVk(), presentInfo);
			if (result == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR)
			{
				VkSwapchainCreateInfoKHR createInfo = VkSwapchainCreateInfoKHR.calloc(stack);
				createInfo.sType(KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR);
				createInfo.minImageCount(2);
				createInfo.imageArrayLayers(1);
				createInfo.imageUsage(VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);
				PhysicalDevice physicalDevice = device.getPhysicalDevice();
				if (physicalDevice.getQueueGraphicsFamilyIndex() == physicalDevice.getQueueSurfaceFamilyIndex()) createInfo.imageSharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);
				else
				{
					createInfo.imageSharingMode(VK10.VK_SHARING_MODE_CONCURRENT);
					createInfo.pQueueFamilyIndices(stack.ints(physicalDevice.getQueueGraphicsFamilyIndex(), physicalDevice.getQueueSurfaceFamilyIndex()));
				}
				createInfo.compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
				createInfo.clipped(true);
				swapChain.recreate(device, targetWindow.getSurface(), createInfo);
				return;
			}
			VulkanErrorUtils.assertNoError(result, "Error presenting queue");
			
			swapChain.swapBuffer();
		}
	}
	
	public ModificationPlan startModificationPlan()
	{
		return new ModificationPlan();
	}
	
	public void setClearColor(Color clearColor)
	{
		this.clearColor = clearColor;
	}
	
	public void setVertexBuffer(VertexBuffer vertexBuffer)
	{
		this.vertexBuffer = vertexBuffer;
	}
	
	public void setIndexBuffer(IndexBuffer indexBuffer)
	{
		this.indexBuffer = indexBuffer;
	}
	
	@Override
	public void close()
	{
		device.waitIdle();
		pipeline.close();
		swapChain.close();
		for (long l : descriptorSetLayouts) VK10.vkDestroyDescriptorSetLayout(device.getVk(), l, null);
		VK10.vkDestroyDescriptorPool(device.getVk(), descriptorPool, null);
	}
	
	public class ModificationPlan extends RendererDataAccumulator<ModificationPlan> {
		
		@Override
		public ModificationPlan device(Device device)
		{
			return super.device(device);
		}
		
		@Override
		public ModificationPlan targetWindow(Window targetWindow)
		{
			return super.targetWindow(targetWindow);
		}
		
		public void submit()
		{
			
		}
	}
	
	public static class Builder extends RendererDataAccumulator<Builder> {
		
		public Renderer build(Window targetWindow, Device device)
		{
			Objects.requireNonNull(device);
			Objects.requireNonNull(targetWindow);
			Surface surface = targetWindow.getSurface();
			PhysicalDevice physicalDevice = device.getPhysicalDevice();
			
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				VkAttachmentDescription.Buffer colorAttachment = VkAttachmentDescription.calloc(1, stack);
				colorAttachment.format(surface.getFormat().format());
				colorAttachment.samples(VK10.VK_SAMPLE_COUNT_1_BIT);
				colorAttachment.loadOp(VK10.VK_ATTACHMENT_LOAD_OP_CLEAR);
				colorAttachment.storeOp(VK10.VK_ATTACHMENT_STORE_OP_STORE);
				colorAttachment.stencilLoadOp(VK10.VK_ATTACHMENT_LOAD_OP_DONT_CARE);
				colorAttachment.stencilStoreOp(VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE);
				colorAttachment.initialLayout(VK10.VK_IMAGE_LAYOUT_UNDEFINED);
				colorAttachment.finalLayout(KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);
				
				VkAttachmentReference.Buffer colorAttachmentRef = VkAttachmentReference.calloc(1, stack);
				colorAttachmentRef.attachment(0);//Color image attachment 
				colorAttachmentRef.layout(VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
				
				VkSubpassDescription.Buffer subpass = VkSubpassDescription.calloc(1, stack);
				subpass.pipelineBindPoint(VK10.VK_PIPELINE_BIND_POINT_GRAPHICS);
				subpass.colorAttachmentCount(1);
				subpass.pColorAttachments(colorAttachmentRef);// using color image[0]
				
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
				
				RenderPass renderPass = new RenderPass(device, renderPassInfo);
				
				VkSurfaceCapabilitiesKHR capabilities = VkSurfaceCapabilitiesKHR.calloc(stack);
				VulkanErrorUtils.assertNoError(KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physicalDevice.getVk(), surface.getSurface(), capabilities), "Failed to create swap chain");
				VulkanErrorUtils.assertStatement(capabilities.maxImageCount() >= 2, "Insufficient images for double-buffer");
				
				VkSwapchainCreateInfoKHR createInfo = VkSwapchainCreateInfoKHR.calloc(stack);
				createInfo.sType(KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR);
				createInfo.minImageCount(2);
				createInfo.imageArrayLayers(1);
				createInfo.imageUsage(VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);
				if (physicalDevice.getQueueGraphicsFamilyIndex() == physicalDevice.getQueueSurfaceFamilyIndex()) createInfo.imageSharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);
				else
				{
					createInfo.imageSharingMode(VK10.VK_SHARING_MODE_CONCURRENT);
					createInfo.pQueueFamilyIndices(stack.ints(physicalDevice.getQueueGraphicsFamilyIndex(), physicalDevice.getQueueSurfaceFamilyIndex()));
				}
				createInfo.compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
				createInfo.clipped(true);
				
				SwapChain swapChain = new SwapChain(device, targetWindow.getSurface(), renderPass, createInfo);
				
				GraphicsPipeline.Builder builder = GraphicsPipeline.create();
				builder.autoCloseLayout(true);
				builder.autoCloseRenderPass(true);
				builder.vertexShader(vertexShader);
				builder.tessellationControlShader(tessellationControlShader);
				builder.tessellationEvaluationShader(tessellationEvaluationShader);
				builder.geometryShader(geometryShader);
				builder.fragmentShader(fragmentShader);
				builder.computeShader(computeShader);
				
				VkPipelineInputAssemblyStateCreateInfo inputAssembly = VulkanUtils.pipelineInputAssemblyStateInfo(stack, VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST, false);
				
				VkExtent2D extent = surface.getExtent();
				VkViewport.Buffer viewport = VulkanUtils.viewport(stack, 0, 0, extent.width(), extent.height(), 0, 1);
				VkRect2D.Buffer scissor = VulkanUtils.rect2d(stack, VulkanUtils.offset(stack, 0, 0), extent);
				
				VkPipelineViewportStateCreateInfo viewportState = VulkanUtils.pipelineViewportStateInfo(stack, viewport, scissor);
				
				VkPipelineRasterizationStateCreateInfo rasterizer = VulkanUtils.pipelineRasterizationStateInfo(stack, VK10.VK_POLYGON_MODE_FILL, VK10.VK_CULL_MODE_BACK_BIT, VK10.VK_FRONT_FACE_CLOCKWISE);
				rasterizer.depthClampEnable(false);
				rasterizer.rasterizerDiscardEnable(false);
				rasterizer.lineWidth(1);
				rasterizer.depthBiasEnable(false);
				
				VkPipelineMultisampleStateCreateInfo multisampling = VulkanUtils.pipelineMultisampleStateInfo(stack, VK10.VK_SAMPLE_COUNT_1_BIT);
				multisampling.sampleShadingEnable(false);
				
				VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachment = VkPipelineColorBlendAttachmentState.calloc(1, stack);
				colorBlendAttachment.colorWriteMask(VK10.VK_COLOR_COMPONENT_R_BIT | VK10.VK_COLOR_COMPONENT_G_BIT | VK10.VK_COLOR_COMPONENT_B_BIT | VK10.VK_COLOR_COMPONENT_A_BIT);
				colorBlendAttachment.blendEnable(false);
				
				VkPipelineColorBlendStateCreateInfo colorBlending = VkPipelineColorBlendStateCreateInfo.calloc(stack);
				colorBlending.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO);
				colorBlending.logicOpEnable(false);
				colorBlending.logicOp(VK10.VK_LOGIC_OP_COPY);
				colorBlending.pAttachments(colorBlendAttachment);
				colorBlending.blendConstants(stack.floats(0, 0, 0, 0));
				
				VkDescriptorPoolCreateInfo poolInfo = VulkanUtils.descriptorPoolCreateInfo(stack, VulkanUtils.descriptorPoolSize(stack, VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 2));
				poolInfo.maxSets(2);
				
				long[] p0 = new long[1];
				
				VulkanErrorUtils.assertNoError(VK10.vkCreateDescriptorPool(device.getVk(), poolInfo, null, p0), "failed to create descriptor pool!");
				
				long descriptorPool = p0[0];
				
				VkDescriptorSetLayoutBinding.Buffer uniformLayoutBinding = VulkanUtils.descriptorSetLayoutBinding(stack, 2, VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
				VkDescriptorSetLayoutBinding vertexShaderUniformLayoutBinding = uniformLayoutBinding.get();
				vertexShaderUniformLayoutBinding.binding(0);
				vertexShaderUniformLayoutBinding.descriptorCount(1);
				vertexShaderUniformLayoutBinding.stageFlags(VK10.VK_SHADER_STAGE_VERTEX_BIT);
				VkDescriptorSetLayoutBinding fragmentShaderUniformLayoutBinding = uniformLayoutBinding.get();
				fragmentShaderUniformLayoutBinding.binding(1);
				fragmentShaderUniformLayoutBinding.descriptorCount(1);
				fragmentShaderUniformLayoutBinding.stageFlags(VK10.VK_SHADER_STAGE_FRAGMENT_BIT);
				uniformLayoutBinding.rewind();
				
				LongBuffer p = stack.callocLong(2);
				
				List<Long> descriptorSetLayouts = new ArrayList<>(2);
				for (int i = 0; i < 2; i++)
				{
					VulkanErrorUtils.assertNoError(VK10.vkCreateDescriptorSetLayout(device.getVk(), VulkanUtils.descriptorSetLayoutInfo(stack, 0, uniformLayoutBinding), null, p), "failed to create descriptor set layout!");
					
					descriptorSetLayouts.add(p.get());
				}
				p.rewind();
				
				long[] p1 = new long[2];
				
				VulkanErrorUtils.assertNoError(VK10.vkAllocateDescriptorSets(device.getVk(), VulkanUtils.descriptorSetAllocateInfo(stack, descriptorPool, p), p1), "failed to allocate descriptor sets!");
				
				List<UniformBuffer> uniformBuffers = new ArrayList<>(2);
				List<Long> descriptorSets = new ArrayList<>(2);
				
				for (int i = 0; i < 2; i++)
				{
					UniformBuffer buffer = new UniformBuffer(16 * 4 + 4 * 4 + 256);
					uniformBuffers.add(buffer);
					
					buffer.asFloatBuffer().put(new Matrix4f().orthographic(targetWindow.getWidth(), targetWindow.getHeight(), 100).toArray());
					
					VkWriteDescriptorSet.Buffer descriptorWrite = VulkanUtils.writeDescriptorSet(stack, 2, VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 1);
					VkWriteDescriptorSet vertexShaderDescriptorWrite = descriptorWrite.get();
					vertexShaderDescriptorWrite.dstSet(p1[i]);
					vertexShaderDescriptorWrite.dstBinding(0);
					vertexShaderDescriptorWrite.dstArrayElement(0);
					vertexShaderDescriptorWrite.pBufferInfo(VulkanUtils.descriptorBufferInfo(stack, buffer.getVk(), 0, 16 * 4));
					
					VkWriteDescriptorSet fragmentShaderDescriptorWrite = descriptorWrite.get();
					fragmentShaderDescriptorWrite.dstSet(p1[i]);
					fragmentShaderDescriptorWrite.dstBinding(1);
					fragmentShaderDescriptorWrite.dstArrayElement(0);
					fragmentShaderDescriptorWrite.pBufferInfo(VulkanUtils.descriptorBufferInfo(stack, buffer.getVk(), 256, 4 * 4));
					
					descriptorWrite.rewind();
					
					VK10.vkUpdateDescriptorSets(device.getVk(), descriptorWrite, VkCopyDescriptorSet.calloc(0, stack));
					
					descriptorSets.add(p1[i]);
				}
				
				VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack);
				pipelineLayoutInfo.pSetLayouts(p);
				
				PipelineLayout layout = new PipelineLayout(device, pipelineLayoutInfo);
				
				VkGraphicsPipelineCreateInfo.Buffer pipelineInfo = VkGraphicsPipelineCreateInfo.calloc(1, stack);
				pipelineInfo.sType(VK10.VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO);
				pipelineInfo.pInputAssemblyState(inputAssembly);
				pipelineInfo.pViewportState(viewportState);
				pipelineInfo.pRasterizationState(rasterizer);
				pipelineInfo.pMultisampleState(multisampling);
				pipelineInfo.pColorBlendState(colorBlending);
				pipelineInfo.renderPass(renderPass.getVk());
				pipelineInfo.subpass(0);
				pipelineInfo.basePipelineHandle(VK10.VK_NULL_HANDLE);
				pipelineInfo.basePipelineIndex(-1);
				
				GraphicsPipeline pipeline = builder.build(device, layout, renderPass, pipelineInfo);
				
				return new Renderer(targetWindow, device, pipeline, swapChain, descriptorSetLayouts, p1, descriptorPool);
			}
		}
	}
	
	private static class RendererDataAccumulator<RDA extends RendererDataAccumulator<RDA>> extends DataAccumulator<RDA> {
		
		/**
		 * Use {@link Shader#getType()} to distinguish the type
		 * @param shader
		 * @return
		 */
		@Override
		public RDA shader(Shader shader)
		{
			return super.genericShader(shader);
		}
		
		@Override
		public RDA vertexShader(Shader shader)
		{
			return super.vertexShader(shader);
		}
		
		@Override
		public RDA tessellationControlShader(Shader shader)
		{
			return super.tessellationControlShader(shader);
		}
		
		@Override
		public RDA tessellationEvaluationShader(Shader shader)
		{
			return super.tessellationEvaluationShader(shader);
		}
		
		@Override
		public RDA geometryShader(Shader shader)
		{
			return super.geometryShader(shader);
		}
		
		@Override
		public RDA fragmentShader(Shader shader)
		{
			return super.fragmentShader(shader);
		}
		
		@Override
		public RDA computeShader(Shader shader)
		{
			return super.computeShader(shader);
		}
	}
}
