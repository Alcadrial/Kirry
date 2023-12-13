package com.alcadrial.renderengine.render;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import com.alcadrial.renderengine.core.util.Nullable;
import com.alcadrial.renderengine.core.util.color.Color;
import com.alcadrial.renderengine.render.buffer.IndexBuffer;
import com.alcadrial.renderengine.render.buffer.UniformBuffer;
import com.alcadrial.renderengine.render.buffer.VertexBuffer;
import com.alcadrial.renderengine.render.pipeline.GraphicsPipeline;
import com.alcadrial.renderengine.render.pipeline.PipelineLayout;
import com.alcadrial.renderengine.render.shader.Shader;
import com.alcadrial.renderengine.render.shader.ShaderDescriptorType;
import com.alcadrial.renderengine.render.shader.ShaderUniform;
import com.alcadrial.renderengine.render.shader.ShaderUniformBinding;
import com.alcadrial.renderengine.render.shader.ShaderUniformBlock;
import com.alcadrial.renderengine.render.texture.Texture;
import com.alcadrial.renderengine.render.util.DataAccumulator;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.VulkanUtils;
import com.alcadrial.renderengine.render.window.VulkanWindow;

public class Renderer implements AutoCloseable {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public static Builder create()
	{
		return new Builder();
	}
	
	public enum DrawResult {
		SUCCESS,
		FAILED,
		NO_DRAWING_AREA;
	}
	
	private ModificationPlan modificationPlan;
	private Device device;
	private VulkanWindow targetWindow;
	private Swapchain swapchain;
	private GraphicsPipeline graphicsPipeline;
	private Color clearColor;
	private float clearDepth;
	private int clearStencil;
	private VertexBuffer vertexBuffer;
	private IndexBuffer indexBuffer;
	private List<Long> descriptorSetLayouts;
	private long[][] descriptorSets;
	private long descriptorPool;
	private List<UniformBuffer> uniformBuffers;
	private List<Deque<Consumer<UniformBuffer>>> uniformBuffersUpdates;
	
	private Renderer(Builder builder, VulkanWindow targetWindow, Device device, Swapchain swapchain, GraphicsPipeline graphicsPipeline, List<Long> descriptorSetLayouts, long[][] descriptorSets, long descriptorPool, List<UniformBuffer> uniformBuffers)
	{
		modificationPlan = new ModificationPlan(builder);
		this.device = device;
		this.targetWindow = targetWindow;
		this.swapchain = swapchain;
		this.graphicsPipeline = graphicsPipeline;
		this.descriptorSetLayouts = descriptorSetLayouts;
		this.descriptorSets = descriptorSets;
		this.descriptorPool = descriptorPool;
		this.uniformBuffers = uniformBuffers;
		clearColor = Color.BLACK;
		clearDepth = 1;
		clearStencil = 0;
		uniformBuffersUpdates = new LinkedList<>();
		for (int i = 0; i < uniformBuffers.size(); i++)
		{
			LinkedList<Consumer<UniformBuffer>> l = new LinkedList<>();
			l.add(b -> {});
			uniformBuffersUpdates.add(l);
		}
	}
	
	public VertexAccumulator accumulate()
	{
		return new VertexAccumulator(graphicsPipeline, vertexBuffer);
	}
	
	public void updateUniformBuffers(Consumer<UniformBuffer> action)
	{
		for (Deque<Consumer<UniformBuffer>> queue : uniformBuffersUpdates) queue.add(action);
	}
	
	public synchronized DrawResult draw()
	{
		if (targetWindow.getWidth() == 0 || targetWindow.getHeight() == 0)
		{
			return DrawResult.NO_DRAWING_AREA;
		}
		
		while (VK10.vkWaitForFences(device.getVk(), swapchain.getActiveCommandBufferData().getFence(), true, Integer.MAX_VALUE) != VK10.VK_SUCCESS);
		
		Deque<Consumer<UniformBuffer>> queue = uniformBuffersUpdates.get(swapchain.getActiveCommandIndex());
		boolean update = queue.size() > 0;
		
		Consumer<UniformBuffer> action;
		while ((action = queue.poll()) != null) for (UniformBuffer buffer : uniformBuffers) action.accept(buffer);
		
		if (update) uniformBuffers.get(swapchain.getActiveCommandIndex()).updateDescriptorSets();
		
		CommandBufferData commandBuffer = swapchain.startRegisteringCommands(null, null, clearColor, clearDepth, clearStencil);
		
		commandBuffer.bindPipeline(graphicsPipeline);
		commandBuffer.bindVertexBuffers(0, vertexBuffer);
		commandBuffer.bindIndexBuffer(indexBuffer, 0);
		commandBuffer.bindDescriptorSets(graphicsPipeline, descriptorSets[swapchain.getActiveCommandIndex()], new int[0]);
		commandBuffer.drawIndexed((int) indexBuffer.getSize() / 4, 1, 0, 0, 0);
		swapchain.endRegisteringCommands();
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			IntBuffer p = stack.mallocInt(1);
			
			int result = KHRSwapchain.vkAcquireNextImageKHR(device.getVk(), swapchain.getVk(), Integer.MAX_VALUE, commandBuffer.getImageAvailableSemaphore(), 0, p);
			if (result == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR)
			{
				Debug.println("Swapchain out of date");
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
				Debug.printlnIf(!swapchain.recreate(device, targetWindow.getSurface(), null, createInfo), "Failed to recreate swapchain");
				return DrawResult.FAILED;
			}
			VulkanErrorUtils.assertNoError(result, "Error acquiring next image");
			
			LongBuffer p0 = stack.longs(commandBuffer.getImageAvailableSemaphore());
			LongBuffer p1 = stack.longs(commandBuffer.getRenderFinishedSemaphore());
			
			VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack);
			submitInfo.sType(VK10.VK_STRUCTURE_TYPE_SUBMIT_INFO);
			submitInfo.waitSemaphoreCount(1);
			submitInfo.pWaitSemaphores(p0);
			submitInfo.pWaitDstStageMask(stack.ints(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT));
			submitInfo.pSignalSemaphores(p1);
			submitInfo.pCommandBuffers(VulkanUtils.toAddressBuffer(stack, commandBuffer));
			
			commandBuffer.resetFence();
			device.getGraphicsQueue().waitIdle();
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
				Debug.println("Swapchain out of date");
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
				Debug.printlnIf(!swapchain.recreate(device, targetWindow.getSurface(), null, createInfo), "Failed to recreate swapchain");
				return DrawResult.FAILED;
			}
			VulkanErrorUtils.assertNoError(result, "Error presenting queue");
			
			swapchain.swapBuffer();
			
			return DrawResult.SUCCESS;
		}
	}
	
	public ModificationPlan startModificationPlan()
	{
		modificationPlan.pushStack();
		return modificationPlan;
	}
	
	public ModificationPlan getCurrentModificationPlan()
	{
		return modificationPlan;
	}
	
	public void setClearColor(Color clearColor)
	{
		this.clearColor = clearColor;
	}
	
	public void setClearDepth(float clearDepth)
	{
		this.clearDepth = clearDepth;
	}
	
	public void setClearStencil(int clearStencil)
	{
		this.clearStencil = clearStencil;
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
		
		private boolean isActive;
		
		private ModificationPlan(RendererDataAccumulator<?> defaultSource)
		{
			super(defaultSource);
			isActive = false;
		}
		
		@Override
		public ModificationPlan device(@Nullable Device device)
		{
			return super.device(device);
		}
		
		@Override
		public ModificationPlan targetWindow(@Nullable VulkanWindow targetWindow)
		{
			return super.targetWindow(targetWindow);
		}
		
		public void submit()
		{
			if (isActive)
			{
				synchronized (Renderer.this)
				{
					if (isActive) submit0();
				}
			}
		}
		
		private void pushStack()
		{
			stack.push();
			isActive = true;
		}
		
		private void submit0()
		{
			boolean deviceModified = device.isModified();
			boolean targetWindowModified = targetWindow.isModified();
			
			physicalDevice.set(device.require().getPhysicalDevice());
			surface.set(targetWindow.require().getSurface());
			
			if (deviceModified || targetWindowModified)
			{
				renderPass.set(createRenderPass());
				
				VkSurfaceCapabilitiesKHR capabilities = VkSurfaceCapabilitiesKHR.calloc(stack);
				VulkanErrorUtils.assertNoError(KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physicalDevice.get().getVk(), surface.get().getSurface(), capabilities), "Failed to create swap chain");
				VulkanErrorUtils.assertStatement(capabilities.maxImageCount() >= 2, "Insufficient images for double-buffer");
				
				swapchain.set(createSwapchain());
			}
			
			if (deviceModified || vertexShader.isModified() || tessellationControlShader.isModified() || tessellationEvaluationShader.isModified() || geometryShader.isModified() || fragmentShader.isModified() || computeShader.isModified())
			{
				createUniformBuffers();
				
				VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack);
				pipelineLayoutInfo.pSetLayouts(stack.longs(descriptorSetLayouts.get(0), descriptorSetLayouts.get(1)));
				
				pipelineLayout.set(new PipelineLayout(device.get(), pipelineLayoutInfo));
				
				List<Long> descriptorSetLayouts0 = Renderer.this.descriptorSetLayouts;
				long descriptorPool0 = Renderer.this.descriptorPool;
				
				device.get().waitAsyncIdle(device0 -> {
					for (long l : descriptorSetLayouts0) VK10.vkDestroyDescriptorSetLayout(device0.getVk(), l, null);
					VK10.vkDestroyDescriptorPool(device0.getVk(), descriptorPool0, null);
				});
				
				Renderer.this.descriptorSetLayouts = descriptorSetLayouts;
				Renderer.this.descriptorSets = descriptorSets;
				Renderer.this.descriptorPool = descriptorPool;
				
				Iterator<UniformBuffer> i = uniformBuffers.iterator();
				Iterator<UniformBuffer> j = Renderer.this.uniformBuffers.iterator();
				
				for (UniformBuffer u0, u1; i.hasNext();)
				{
					u0 = i.next();
					u1 = j.next();
					u0.asByteBuffer().put(u1.asByteBuffer());
					u0.updateDescriptorSets();
				}
				Renderer.this.uniformBuffers = uniformBuffers;
			}
			
			Renderer.this.graphicsPipeline.getDevice().waitAsyncIdle(Renderer.this.graphicsPipeline::close);
			graphicsPipeline.set(createGraphicsPipeline());
			
			Renderer.this.device = device.get();
			Renderer.this.targetWindow = targetWindow.get();
			Renderer.this.swapchain = swapchain.get();
			Renderer.this.graphicsPipeline = graphicsPipeline.get();
			
			isActive = false;
			stack.pop();
		}
	}
	
	public static class Builder extends RendererDataAccumulator<Builder> {
		
		private Builder()
		{}
		
		public Renderer build(VulkanWindow targetWindow, Device device)
		{
			try
			{
				this.device.setNonNull(device);
				this.targetWindow.setNonNull(targetWindow);
				physicalDevice.set(device.getPhysicalDevice());
				surface.set(targetWindow.getSurface());
				
				renderPass.set(createRenderPass());
				
				VkSurfaceCapabilitiesKHR capabilities = VkSurfaceCapabilitiesKHR.calloc(stack);
				VulkanErrorUtils.assertNoError(KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physicalDevice.get().getVk(), surface.get().getSurface(), capabilities), "Failed to create swap chain");
				VulkanErrorUtils.assertStatement(capabilities.maxImageCount() >= 2, "Insufficient images for double-buffer");
				
				swapchain.set(createSwapchain());
				
				createUniformBuffers();
				
				VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack);
				pipelineLayoutInfo.pSetLayouts(stack.longs(descriptorSetLayouts.get(0), descriptorSetLayouts.get(1)));
				
				pipelineLayout.set(new PipelineLayout(device, pipelineLayoutInfo));
				
				graphicsPipeline.set(createGraphicsPipeline());
				
				return new Renderer(this, targetWindow, device, swapchain.get(), graphicsPipeline.get(), descriptorSetLayouts, descriptorSets, descriptorPool, uniformBuffers);
			}
			finally
			{
				close();
			}
		}
	}
	
	private static class RendererDataAccumulator<RDA extends RendererDataAccumulator<RDA>> extends DataAccumulator<RDA> {
		
		protected List<Long> descriptorSetLayouts;
		protected long[][] descriptorSets;
		protected long descriptorPool;
		protected List<UniformBuffer> uniformBuffers;
		
		protected RendererDataAccumulator()
		{}
		
		protected RendererDataAccumulator(DataAccumulator<?> defaultSource)
		{
			super(defaultSource);
		}
		
		protected void createUniformBuffers()
		{
			Debug.println("Creating uniform buffers");
			device.require();
			
			VkDescriptorPoolCreateInfo poolInfo = VulkanUtils.descriptorPoolCreateInfo(stack, VulkanUtils.descriptorPoolSize(stack, VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 2));// static
			poolInfo.maxSets(2);
			
			long[] p0 = new long[1];
			
			VulkanErrorUtils.assertNoError(VK10.vkCreateDescriptorPool(device.get().getVk(), poolInfo, null, p0), "failed to create descriptor pool!");
			
			descriptorPool = p0[0];
			
			Map<Integer, ShaderUniformBinding> bindingFlags = new HashMap<>();
			extractData(vertexShader.require(), bindingFlags);
			extractData(tessellationControlShader.get(), bindingFlags);
			extractData(tessellationEvaluationShader.get(), bindingFlags);
			extractData(geometryShader.get(), bindingFlags);
			extractData(fragmentShader.get(), bindingFlags);
			extractData(computeShader.get(), bindingFlags);
			
			int minOffsetAlignment = (int) physicalDevice.get().getLimits().minUniformBufferOffsetAlignment();
			int uniformBufferSize = 0;
			VkDescriptorSetLayoutBinding.Buffer uniformLayoutBindings = VkDescriptorSetLayoutBinding.malloc(bindingFlags.size(), stack);// static
			for (Entry<Integer, ShaderUniformBinding> e : bindingFlags.entrySet())
			{
				if (uniformBufferSize % minOffsetAlignment != 0)
				{
					uniformBufferSize += minOffsetAlignment - uniformBufferSize % minOffsetAlignment;
				}
				VkDescriptorSetLayoutBinding uniformLayoutBinding = uniformLayoutBindings.get();
				uniformLayoutBinding.binding(e.getKey());
				uniformLayoutBinding.descriptorCount(1);
				uniformLayoutBinding.descriptorType(e.getValue().getDescriptorType().getType());
				uniformLayoutBinding.pImmutableSamplers(null);
				uniformLayoutBinding.stageFlags(e.getValue().getStages());
				uniformBufferSize += e.getValue().getUniforms().stream().collect(Collectors.summingInt(u -> u.getType().getLength()));
			}
			uniformLayoutBindings.rewind();
			
			LongBuffer p = stack.callocLong(2);
			
			descriptorSetLayouts = new ArrayList<>(2);
			for (int i = 0; i < 2; i++)
			{
				VulkanErrorUtils.assertNoError(VK10.vkCreateDescriptorSetLayout(device.get().getVk(), VulkanUtils.descriptorSetLayoutInfo(stack, 0, uniformLayoutBindings), null, p), "failed to create descriptor set layout!");
				
				descriptorSetLayouts.add(p.get());
			}
			p.rewind();
			
			long[] p1 = new long[2];
			
			descriptorSets = new long[2][1];
			
			VulkanErrorUtils.assertNoError(VK10.vkAllocateDescriptorSets(device.get().getVk(), VulkanUtils.descriptorSetAllocateInfo(stack, descriptorPool, p), p1), "failed to allocate descriptor sets!");
			
			uniformBuffers = new ArrayList<>(2);
			
			for (int i = 0; i < 2; i++)
			{
				VkWriteDescriptorSet.Buffer writeDescriptorSets = VulkanUtils.writeDescriptorSet(bindingFlags.size(), VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 1);// dynamic
				
				UniformBuffer buffer = new UniformBuffer(writeDescriptorSets, Math.max(4, uniformBufferSize));
				uniformBuffers.add(buffer);
				descriptorSets[i] = new long[] {p1[i]};
				
				int offset = 0;
				for (Entry<Integer, ShaderUniformBinding> e : bindingFlags.entrySet())
				{
					if (offset % minOffsetAlignment != 0)
					{
						offset += minOffsetAlignment - offset % minOffsetAlignment;
					}
					
					VkWriteDescriptorSet writeDescriptorSet = writeDescriptorSets.get();
					writeDescriptorSet.dstSet(p1[i]);
					writeDescriptorSet.dstBinding(e.getKey());
					writeDescriptorSet.dstArrayElement(0);
					writeDescriptorSet.descriptorType(e.getValue().getDescriptorType().getType());
					
					Set<ShaderUniform> uniforms = e.getValue().getUniforms();
					switch (e.getValue().getDescriptorType())
					{
						case UNIFORM_BUFFER ->
						{
							VkDescriptorBufferInfo.Buffer descriptorBufferInfo = VkDescriptorBufferInfo.calloc(1);
							descriptorBufferInfo.buffer(buffer.getVk());
							descriptorBufferInfo.offset(offset);
							for (ShaderUniform uniform : uniforms)
							{
								buffer.mark(uniform, offset / 4);
								offset += uniform.getType().getLength();
							}
							descriptorBufferInfo.range(offset - descriptorBufferInfo.offset());
							writeDescriptorSet.pBufferInfo(descriptorBufferInfo);
						}
						case COMBINED_IMAGE_SAMPLER ->
						{
							VkDescriptorImageInfo.Buffer descriptorImageInfos = VkDescriptorImageInfo.calloc(uniforms.size());
							for (ShaderUniform uniform : uniforms)
							{
								buffer.mark(uniform, writeDescriptorSets.position() - 1);
								VkDescriptorImageInfo descriptorImageInfo = descriptorImageInfos.get();
								descriptorImageInfo.imageLayout(VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
								descriptorImageInfo.imageView(0);
								descriptorImageInfo.sampler(0);
							}
							descriptorImageInfos.rewind();
							writeDescriptorSet.pImageInfo(descriptorImageInfos);
						}
					}
				}
				writeDescriptorSets.rewind();
			}
		}
		
		private void extractData(Shader shader, Map<Integer, ShaderUniformBinding> bindingFlags)
		{
			if (shader != null)
			{
				ShaderUniformBinding p = new ShaderUniformBinding(shader.getType().getStageBit(), null, null);
				for (ShaderUniformBlock uniformBlock : shader.getUniformsBlocks())
				{
					p.setUniforms(new LinkedHashSet<>(uniformBlock.getUniforms()));
					bindingFlags.merge(uniformBlock.getBinding(), p, (v, w) -> {
						v.getUniforms().addAll(w.getUniforms());
						v.setStages(v.getStages() | w.getStages());
						return v;
					});
				}
				for (Entry<Integer, ShaderUniformBinding> e : bindingFlags.entrySet())
				{
					ShaderDescriptorType descriptorType = shader.getBindingDescriptorTypeMap().get(e.getKey());
					if (descriptorType != null) e.getValue().setDescriptorType(descriptorType);
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
		public RDA depthTexture(@Nullable Texture value)
		{
			return super.depthTexture(value);
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
		
		@Override
		public RDA depthTestEnable(@Nullable Boolean value)
		{
			return super.depthTestEnable(value);
		}
		
		@Override
		public RDA depthWriteEnable(@Nullable Boolean value)
		{
			return super.depthWriteEnable(value);
		}
		
		@Override
		public RDA depthCompareOp(@Nullable Integer value)
		{
			return super.depthCompareOp(value);
		}
		
		@Override
		public RDA minDepth(@Nullable Float value)
		{
			return super.minDepth(value);
		}
		
		@Override
		public RDA maxDepth(@Nullable Float value)
		{
			return super.maxDepth(value);
		}
	}
}
