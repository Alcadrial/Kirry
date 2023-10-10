package com.alcadrial.renderengine.render;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.renderengine.core.util.Nullable;
import com.alcadrial.renderengine.core.util.color.Color;
import com.alcadrial.renderengine.render.buffer.IndexBuffer;
import com.alcadrial.renderengine.render.buffer.UniformBuffer;
import com.alcadrial.renderengine.render.buffer.VertexBuffer;
import com.alcadrial.renderengine.render.pipeline.GraphicsPipeline;
import com.alcadrial.renderengine.render.pipeline.PipelineLayout;
import com.alcadrial.renderengine.render.shader.Shader;
import com.alcadrial.renderengine.render.shader.ShaderUniform;
import com.alcadrial.renderengine.render.shader.ShaderUniformBlock;
import com.alcadrial.renderengine.render.util.DataAccumulator;
import com.alcadrial.renderengine.render.util.Pair;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.VulkanUtils;
import com.alcadrial.renderengine.render.window.Window;

public class Renderer implements AutoCloseable {
	
	public static Builder create()
	{
		return new Builder();
	}
	
	private Device device;
	private Window targetWindow;
	private Swapchain swapchain;
	private GraphicsPipeline graphicsPipeline;
	private Color clearColor;
	private VertexBuffer vertexBuffer;
	private IndexBuffer indexBuffer;
	private List<Long> descriptorSetLayouts;
	private long[] descriptorSets;
	private long descriptorPool;
	private List<UniformBuffer> uniformBuffers;
	
	private Renderer(Window targetWindow, Device device, Swapchain swapchain, GraphicsPipeline graphicsPipeline, List<Long> descriptorSetLayouts, long[] descriptorSets, long descriptorPool, List<UniformBuffer> uniformBuffers)
	{
		this.device = device;
		this.targetWindow = targetWindow;
		this.swapchain = swapchain;
		this.graphicsPipeline = graphicsPipeline;
		this.descriptorSetLayouts = descriptorSetLayouts;
		this.descriptorSets = descriptorSets;
		this.descriptorPool = descriptorPool;
		this.uniformBuffers = uniformBuffers;
		clearColor = Color.BLACK;
	}
	
	public VertexAccumulator accumulate()
	{
		return new VertexAccumulator(graphicsPipeline, vertexBuffer);
	}
	
	public List<UniformBuffer> getUniformBuffers()
	{
		return uniformBuffers;
	}
	
	public synchronized void draw(Matrix4f matrix)
	{
		CommandBufferData commandBuffer = swapchain.startRegisteringCommands(null, null, clearColor);
		commandBuffer.bindPipeline(graphicsPipeline);
		commandBuffer.bindVertexBuffers(0, vertexBuffer);
		commandBuffer.bindIndexBuffer(indexBuffer, 0);
		commandBuffer.bindDescriptorSets(graphicsPipeline, descriptorSets, new int[0]);
		commandBuffer.drawIndexed((int) indexBuffer.getSize() / 4, 1, 0, 0, 0);
		swapchain.endRegisteringCommands();
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			IntBuffer p = stack.callocInt(1);
			
			int result = KHRSwapchain.vkAcquireNextImageKHR(device.getVk(), swapchain.getVk(), Integer.MAX_VALUE, commandBuffer.getImageAvailableSemaphore(), 0, p);
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
				swapchain.recreate(device, targetWindow.getSurface(), createInfo);
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
			presentInfo.pSwapchains(stack.longs(swapchain.getVk()));
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
				swapchain.recreate(device, targetWindow.getSurface(), createInfo);
				return;
			}
			VulkanErrorUtils.assertNoError(result, "Error presenting queue");
			
			swapchain.swapBuffer();
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
		graphicsPipeline.close();
		swapchain.close();
		for (long l : descriptorSetLayouts) VK10.vkDestroyDescriptorSetLayout(device.getVk(), l, null);
		VK10.vkDestroyDescriptorPool(device.getVk(), descriptorPool, null);
	}
	
	public class ModificationPlan extends RendererDataAccumulator<ModificationPlan> {
		
		private boolean isSubmitted;
		
		@Override
		public ModificationPlan device(@Nullable Device device)
		{
			return super.device(device);
		}
		
		@Override
		public ModificationPlan targetWindow(@Nullable Window targetWindow)
		{
			return super.targetWindow(targetWindow);
		}
		
		public void submit()
		{
			if (!isSubmitted)
			{
				synchronized (Renderer.this)
				{
					if (!isSubmitted)
					{
						isSubmitted = true;
						submit0();
					}
				}
			}
		}
		
		private void submit0()
		{
			Device newDevice = device;
			Window newTargetWindow = targetWindow;
			if (device == null) device = Renderer.this.device;
			if (targetWindow == null) targetWindow = Renderer.this.targetWindow;
			
			physicalDevice = device.getPhysicalDevice();
			surface = orElse(newTargetWindow, targetWindow).getSurface();
			
			if (newDevice != null || newTargetWindow != null)
			{
				renderPass = createRenderPass();
				
				VkSurfaceCapabilitiesKHR capabilities = VkSurfaceCapabilitiesKHR.calloc(stack);
				VulkanErrorUtils.assertNoError(KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physicalDevice.getVk(), surface.getSurface(), capabilities), "Failed to create swap chain");
				VulkanErrorUtils.assertStatement(capabilities.maxImageCount() >= 2, "Insufficient images for double-buffer");
				
				swapchain = createSwapchain();
			}
			else
			{
				swapchain = Renderer.this.swapchain;
				renderPass = swapchain.getRenderPass();
			}
			
			if (newDevice != null || vertexShader != null || tessellationControlShader != null || tessellationEvaluationShader != null || geometryShader != null || fragmentShader != null || computeShader != null)
			{
				vertexShader = orElse(vertexShader, Renderer.this.graphicsPipeline.getVertexShader());
				tessellationControlShader = orElse(tessellationControlShader, Renderer.this.graphicsPipeline.getTessellationControlShader().orElse(null));
				tessellationEvaluationShader = orElse(tessellationEvaluationShader, Renderer.this.graphicsPipeline.getTessellationEvaluationShader().orElse(null));
				geometryShader = orElse(geometryShader, Renderer.this.graphicsPipeline.getGeometryShader().orElse(null));
				fragmentShader = orElse(fragmentShader, Renderer.this.graphicsPipeline.getFragmentShader().orElse(null));
				computeShader = orElse(computeShader, Renderer.this.graphicsPipeline.getComputeShader().orElse(null));
				
				createUniformBuffers();
				
				VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack);
				pipelineLayoutInfo.pSetLayouts(stack.longs(descriptorSetLayouts.get(0), descriptorSetLayouts.get(1)));
				
				pipelineLayout = new PipelineLayout(device, pipelineLayoutInfo);
				
				Renderer.this.descriptorSetLayouts = descriptorSetLayouts;
				Renderer.this.descriptorSets = descriptorSets;
				Renderer.this.descriptorPool = descriptorPool;
				Renderer.this.uniformBuffers = uniformBuffers;
			}
			else
			{
				vertexShader = orElse(vertexShader, Renderer.this.graphicsPipeline.getVertexShader());
				pipelineLayout = Renderer.this.graphicsPipeline.getLayout();
			}
			
			Renderer.this.graphicsPipeline.getDevice().waitAsyncIdle(Renderer.this.graphicsPipeline::close);
			graphicsPipeline = createGraphicsPipeline();
			
			Renderer.this.device = device;
			Renderer.this.targetWindow = targetWindow;
			Renderer.this.swapchain = swapchain;
			Renderer.this.graphicsPipeline = graphicsPipeline;
			close();
		}
	}
	
	public static class Builder extends RendererDataAccumulator<Builder> {
		
		public Renderer build(Window targetWindow, Device device)
		{
			this.device = orElseThrow(device);
			this.targetWindow = orElseThrow(targetWindow);
			physicalDevice = device.getPhysicalDevice();
			surface = targetWindow.getSurface();
			
			renderPass = createRenderPass();
			
			VkSurfaceCapabilitiesKHR capabilities = VkSurfaceCapabilitiesKHR.calloc(stack);
			VulkanErrorUtils.assertNoError(KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physicalDevice.getVk(), surface.getSurface(), capabilities), "Failed to create swap chain");
			VulkanErrorUtils.assertStatement(capabilities.maxImageCount() >= 2, "Insufficient images for double-buffer");
			
			swapchain = createSwapchain();
			
			createUniformBuffers();
			
			VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack);
			pipelineLayoutInfo.pSetLayouts(stack.longs(descriptorSetLayouts.get(0), descriptorSetLayouts.get(1)));
			
			pipelineLayout = new PipelineLayout(device, pipelineLayoutInfo);
			
			graphicsPipeline = createGraphicsPipeline();
			
			close();
			
			return new Renderer(targetWindow, device, swapchain, graphicsPipeline, descriptorSetLayouts, descriptorSets, descriptorPool, uniformBuffers);
		}
	}
	
	private static class RendererDataAccumulator<RDA extends RendererDataAccumulator<RDA>> extends DataAccumulator<RDA> {
		
		protected List<Long> descriptorSetLayouts;
		protected long[] descriptorSets;
		protected long descriptorPool;
		protected List<UniformBuffer> uniformBuffers;
		
		protected void createUniformBuffers()
		{
			System.out.println("Creating uniform buffers");
			
			VkDescriptorPoolCreateInfo poolInfo = VulkanUtils.descriptorPoolCreateInfo(stack, VulkanUtils.descriptorPoolSize(stack, VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 2));//static
			poolInfo.maxSets(2);
			
			long[] p0 = new long[1];
			
			VulkanErrorUtils.assertNoError(VK10.vkCreateDescriptorPool(device.getVk(), poolInfo, null, p0), "failed to create descriptor pool!");
			
			descriptorPool = p0[0];
			
			Map<Integer, Pair<Integer, Set<ShaderUniform>>> bindingFlags = new HashMap<>();
			extractData(vertexShader, bindingFlags);
			extractData(tessellationControlShader, bindingFlags);
			extractData(tessellationEvaluationShader, bindingFlags);
			extractData(geometryShader, bindingFlags);
			extractData(fragmentShader, bindingFlags);
			extractData(computeShader, bindingFlags);
			
			int uniformBufferSize = 0;
			VkDescriptorSetLayoutBinding.Buffer uniformLayoutBindings = VulkanUtils.descriptorSetLayoutBinding(stack, bindingFlags.size(), VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);//static
			for (Entry<Integer, Pair<Integer, Set<ShaderUniform>>> e : bindingFlags.entrySet())
			{
				VkDescriptorSetLayoutBinding uniformLayoutBinding = uniformLayoutBindings.get();
				uniformLayoutBinding.binding(e.getKey());
				uniformLayoutBinding.descriptorCount(1);
				uniformLayoutBinding.stageFlags(e.getValue().getLeft());
				uniformBufferSize += e.getValue().getRigth().stream().collect(Collectors.summingInt(u -> u.getType().getLength()));
			}
			uniformLayoutBindings.rewind();
			
			LongBuffer p = stack.callocLong(2);
			
			descriptorSetLayouts = new ArrayList<>(2);
			for (int i = 0; i < 2; i++)
			{
				VulkanErrorUtils.assertNoError(VK10.vkCreateDescriptorSetLayout(device.getVk(), VulkanUtils.descriptorSetLayoutInfo(stack, 0, uniformLayoutBindings), null, p), "failed to create descriptor set layout!");
				
				descriptorSetLayouts.add(p.get());
			}
			p.rewind();
			
			descriptorSets = new long[2];
			
			VulkanErrorUtils.assertNoError(VK10.vkAllocateDescriptorSets(device.getVk(), VulkanUtils.descriptorSetAllocateInfo(stack, descriptorPool, p), descriptorSets), "failed to allocate descriptor sets!");
			
			uniformBuffers = new ArrayList<>(2);
			
			for (int i = 0; i < 2; i++)
			{
				VkWriteDescriptorSet.Buffer writeDescriptorSets = VulkanUtils.writeDescriptorSet(bindingFlags.size(), VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 1);//dynamic
				
				UniformBuffer buffer = new UniformBuffer(writeDescriptorSets, Math.max(4, uniformBufferSize));
				uniformBuffers.add(buffer);
				
				int offset = 0;
				for (Entry<Integer, Pair<Integer, Set<ShaderUniform>>> e : bindingFlags.entrySet())
				{
					VkWriteDescriptorSet writeDescriptorSet = writeDescriptorSets.get();
					writeDescriptorSet.dstSet(descriptorSets[i]);
					writeDescriptorSet.dstBinding(e.getKey());
					writeDescriptorSet.dstArrayElement(0);
					
					Set<ShaderUniform> uniforms = e.getValue().getRigth();
					VkDescriptorBufferInfo.Buffer descriptorBufferInfos = VkDescriptorBufferInfo.calloc(uniforms.size());
					for (ShaderUniform uniform : uniforms)
					{
						buffer.mark(uniform, offset);
						VkDescriptorBufferInfo descriptorBufferInfo = descriptorBufferInfos.get();
						descriptorBufferInfo.buffer(buffer.getVk());
						descriptorBufferInfo.offset(offset);
						descriptorBufferInfo.range(offset += uniform.getType().getLength());
					}
					descriptorBufferInfos.rewind();
					writeDescriptorSet.pBufferInfo(descriptorBufferInfos);
				}
				writeDescriptorSets.rewind();
			}
		}
		
		private void extractData(Shader shader, Map<Integer, Pair<Integer, Set<ShaderUniform>>> bindingFlags)
		{
			if (shader != null)
			{
				Pair<Integer, Set<ShaderUniform>> p = new Pair<>(shader.getType().getStageBit(), null);
				for (ShaderUniformBlock uniformBlock : shader.getUniformsBlocks())
				{
					p.setRigth(new HashSet<>(uniformBlock.getUniforms()));
					bindingFlags.merge(uniformBlock.getBinding(), p, (v, w) -> {
						v.getRigth().addAll(w.getRigth());
						v.setLeft(v.getLeft() | w.getLeft());
						return v;
					});
				}
			}
		}
		
		/**
		 * Uses {@link Shader#getType()} to distinguish the type
		 * @param shader
		 * @return this for chaining
		 */
		@Override
		public RDA shader(@Nullable Shader shader)
		{
			return super.genericShader(shader);
		}
		
		@Override
		public RDA vertexShader(@Nullable Shader shader)
		{
			return super.vertexShader(shader);
		}
		
		@Override
		public RDA tessellationControlShader(@Nullable Shader shader)
		{
			return super.tessellationControlShader(shader);
		}
		
		@Override
		public RDA tessellationEvaluationShader(@Nullable Shader shader)
		{
			return super.tessellationEvaluationShader(shader);
		}
		
		@Override
		public RDA geometryShader(@Nullable Shader shader)
		{
			return super.geometryShader(shader);
		}
		
		@Override
		public RDA fragmentShader(@Nullable Shader shader)
		{
			return super.fragmentShader(shader);
		}
		
		@Override
		public RDA computeShader(@Nullable Shader shader)
		{
			return super.computeShader(shader);
		}
		
		@Override
		public RDA alphaToCoverageEnable(@Nullable Boolean value)
		{
			return super.alphaToCoverageEnable(value);
		}
		
		@Override
		public RDA alphaToOneEnable(@Nullable Boolean value)
		{
			return super.alphaToOneEnable(value);
		}
		
		@Override
		public RDA cullMode(@Nullable Integer value)
		{
			return super.cullMode(value);
		}
		
		@Override
		public RDA depthBiasClamp(@Nullable Float value)
		{
			return super.depthBiasClamp(value);
		}
		
		@Override
		public RDA depthBiasConstantFactor(@Nullable Float value)
		{
			return super.depthBiasConstantFactor(value);
		}
		
		@Override
		public RDA depthBiasEnable(@Nullable Boolean value)
		{
			return super.depthBiasEnable(value);
		}
		
		@Override
		public RDA depthBiasSlopeFactor(@Nullable Float value)
		{
			return super.depthBiasSlopeFactor(value);
		}
		
		@Override
		public RDA depthClampEnable(@Nullable Boolean value)
		{
			return super.depthClampEnable(value);
		}
		
		@Override
		public RDA frontFace(@Nullable Integer value)
		{
			return super.frontFace(value);
		}
		
		@Override
		public RDA lineWidth(@Nullable Float value)
		{
			return super.lineWidth(value);
		}
		
		@Override
		public RDA logicOp(@Nullable Integer value)
		{
			return super.logicOp(value);
		}
		
		@Override
		public RDA logicOpEnable(@Nullable Boolean value)
		{
			return super.logicOpEnable(value);
		}
		
		@Override
		public RDA minSampleShading(@Nullable Float value)
		{
			return super.minSampleShading(value);
		}
		
		@Override
		public RDA polygonMode(@Nullable Integer value)
		{
			return super.polygonMode(value);
		}
		
		@Override
		public RDA primitiveRestartEnable(@Nullable Boolean value)
		{
			return super.primitiveRestartEnable(value);
		}
		
		@Override
		public RDA rasterizationSamples(@Nullable Integer value)
		{
			return super.rasterizationSamples(value);
		}
		
		@Override
		public RDA rasterizerDiscardEnable(@Nullable Boolean value)
		{
			return super.rasterizerDiscardEnable(value);
		}
		
		@Override
		public RDA sampleShadingEnable(@Nullable Boolean value)
		{
			return super.sampleShadingEnable(value);
		}
		
		@Override
		public RDA topology(@Nullable Integer value)
		{
			return super.topology(value);
		}
		
		@Override
		public <SSDA extends DataAccumulator<RDA>.PipelineColorBlendAttachmentStateAccumulator<SSDA>> SSDA startPipelineColorBlendAttachmentState()
		{
			return super.startPipelineColorBlendAttachmentState();
		}
	}
}
