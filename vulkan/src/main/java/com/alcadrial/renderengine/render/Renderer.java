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
import java.util.function.BiConsumer;

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
import com.alcadrial.renderengine.core.util.Pair;
import com.alcadrial.renderengine.core.util.color.Color;
import com.alcadrial.renderengine.render.buffer.BufferLayout;
import com.alcadrial.renderengine.render.buffer.IndexBuffer;
import com.alcadrial.renderengine.render.buffer.VertexBuffer;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.pipeline.GraphicsPipeline;
import com.alcadrial.renderengine.render.pipeline.GraphicsPipeline.GraphicsPipelineBuildFunction;
import com.alcadrial.renderengine.render.pipeline.PipelineLayout;
import com.alcadrial.renderengine.render.shader.PreparedShader;
import com.alcadrial.renderengine.render.shader.Shader;
import com.alcadrial.renderengine.render.shader.ShaderInfo;
import com.alcadrial.renderengine.render.shader.ShaderLayoutBinding;
import com.alcadrial.renderengine.render.shader.ShaderType;
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.shader.layouts.ShaderCombinedImageSamplerLayout;
import com.alcadrial.renderengine.render.shader.layouts.ShaderLayout;
import com.alcadrial.renderengine.render.shader.layouts.ShaderStorageBufferLayout;
import com.alcadrial.renderengine.render.shader.layouts.ShaderUniformBufferLayout;
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
	
	public static final int SWAPPING_BUFFERS = 3;
	
	private ModificationPlan modificationPlan;
	private Device device;
	private VulkanWindow targetWindow;
	private Swapchain swapchain;
	private GraphicsPipeline graphicsPipeline;
	private Color clearColor;
	private float clearDepth;
	private int clearStencil;
	private Map<Integer, List<VertexBuffer>> vertexBuffers;
	private IndexBuffer indexBuffer;
	private List<Long> descriptorSetLayouts;
	private long[][] descriptorSets;
	private long descriptorPool;
	private List<Pair<VulkanBuffer, BufferLayout>> uniformBuffers;
	private List<Deque<BiConsumer<VulkanBuffer, BufferLayout>>> uniformBuffersUpdates;
	
	private Renderer(Builder builder, VulkanWindow targetWindow, Device device, Swapchain swapchain, GraphicsPipeline graphicsPipeline, List<Long> descriptorSetLayouts, long[][] descriptorSets, long descriptorPool, List<Pair<VulkanBuffer, BufferLayout>> uniformBuffers)
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
		vertexBuffers = new HashMap<>();
		
		for (int i = 0; i < uniformBuffers.size(); i++)
		{
			LinkedList<BiConsumer<VulkanBuffer, BufferLayout>> list = new LinkedList<>();
			list.add((b, l) -> {});
			uniformBuffersUpdates.add(list);
		}
	}
	
	public VertexAccumulator accumulate(VertexBuffer vertexBuffer)
	{
		return new VertexAccumulator(graphicsPipeline, vertexBuffer);
	}
	
	public void updateUniformBuffers(BiConsumer<VulkanBuffer, BufferLayout> action)
	{
		for (Deque<BiConsumer<VulkanBuffer, BufferLayout>> queue : uniformBuffersUpdates) queue.add(action);
	}
	
	public synchronized DrawResult draw()
	{
		if (targetWindow.getWidth() == 0 || targetWindow.getHeight() == 0)
		{
			return DrawResult.NO_DRAWING_AREA;
		}
		
		while (VK10.vkWaitForFences(device.getVk(), swapchain.getActiveCommandBufferData().getFence(), true, Integer.MAX_VALUE) != VK10.VK_SUCCESS);
		
		Deque<BiConsumer<VulkanBuffer, BufferLayout>> queue = uniformBuffersUpdates.get(swapchain.getActiveCommandIndex());
		boolean update = queue.size() > 0;
		
		BiConsumer<VulkanBuffer, BufferLayout> action;
		while ((action = queue.poll()) != null) for (Pair<VulkanBuffer, BufferLayout> p : uniformBuffers) action.accept(p.left, p.right);
		
		if (update)
		{
			Pair<VulkanBuffer, BufferLayout> p = uniformBuffers.get(swapchain.getActiveCommandIndex());
			p.right.updateDescriptorSets(p.left);
		}
		
		CommandBufferData commandBuffer = swapchain.startRegisteringCommands(null, null, clearColor, clearDepth, clearStencil);
		commandBuffer.bindPipeline(graphicsPipeline);
		for (Entry<Integer, List<VertexBuffer>> p : vertexBuffers.entrySet()) commandBuffer.bindVertexBuffers(p.getKey(), p.getValue());
		commandBuffer.bindIndexBuffer(indexBuffer, 0);
		commandBuffer.bindDescriptorSets(graphicsPipeline, descriptorSets[swapchain.getActiveCommandIndex()], new int[0]);
		commandBuffer.drawIndexed((int) (indexBuffer.getSize() / 4), 1, 0, 0, 0);
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
				createInfo.minImageCount(SWAPPING_BUFFERS);
				createInfo.imageArrayLayers(1);
				createInfo.imageUsage(VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);
				
				if (device.getGraphicsQueue().getFamilyIndex() == device.getPresentQueue().getFamilyIndex()) createInfo.imageSharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);
				else
				{
					createInfo.imageSharingMode(VK10.VK_SHARING_MODE_CONCURRENT);
					createInfo.pQueueFamilyIndices(stack.ints(device.getGraphicsQueue().getFamilyIndex(), device.getPresentQueue().getFamilyIndex()));
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
				createInfo.minImageCount(SWAPPING_BUFFERS);
				createInfo.imageArrayLayers(1);
				createInfo.imageUsage(VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);
				if (device.getGraphicsQueue().getFamilyIndex() == device.getPresentQueue().getFamilyIndex()) createInfo.imageSharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);
				else
				{
					createInfo.imageSharingMode(VK10.VK_SHARING_MODE_CONCURRENT);
					createInfo.pQueueFamilyIndices(stack.ints(device.getGraphicsQueue().getFamilyIndex(), device.getPresentQueue().getFamilyIndex()));
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
	
	public void addVertexBuffer(int binding, VertexBuffer vertexBuffer)
	{
		vertexBuffers.computeIfAbsent(binding, k -> new LinkedList<>()).add(vertexBuffer);
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
			submit(GraphicsPipeline::new);
		}
		
		public void submit(GraphicsPipelineBuildFunction<?> builderFunction)
		{
			if (isActive)
			{
				synchronized (Renderer.this)
				{
					if (isActive) submit0(builderFunction);
				}
			}
		}
		
		private void pushStack()
		{
			stack.push();
			isActive = true;
		}
		
		private void submit0(GraphicsPipelineBuildFunction<?> builderFunction)
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
				
				Iterator<Pair<VulkanBuffer, BufferLayout>> i = uniformBuffers.iterator();
				Iterator<Pair<VulkanBuffer, BufferLayout>> j = Renderer.this.uniformBuffers.iterator();
				
				for (Pair<VulkanBuffer, BufferLayout> u0, u1; i.hasNext();)
				{
					u0 = i.next();
					u1 = j.next();
					u0.left.asByteBuffer().put(u1.left.asByteBuffer());
					u0.right.updateDescriptorSets(u0.left);
				}
				Renderer.this.uniformBuffers = uniformBuffers;
			}
			
			Renderer.this.graphicsPipeline.getDevice().waitAsyncIdle(Renderer.this.graphicsPipeline::close);
			graphicsPipeline.set(createGraphicsPipeline(builderFunction));
			
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
			return build(targetWindow, device, GraphicsPipeline::new);
		}
		
		public Renderer build(VulkanWindow targetWindow, Device device, GraphicsPipelineBuildFunction<?> builderFunction)
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
				
				graphicsPipeline.set(createGraphicsPipeline(builderFunction));
				
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
		protected List<Pair<VulkanBuffer, BufferLayout>> uniformBuffers;
		
		protected RendererDataAccumulator()
		{}
		
		protected RendererDataAccumulator(DataAccumulator<?> defaultSource)
		{
			super(defaultSource);
		}
		
		@SuppressWarnings("null")
		protected void createUniformBuffers()
		{
			Debug.println("Creating uniform buffers");
			device.require();
			
			VkDescriptorPoolCreateInfo poolInfo = VulkanUtils.descriptorPoolCreateInfo(stack, VulkanUtils.descriptorPoolSize(stack, VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, SWAPPING_BUFFERS));// static
			poolInfo.maxSets(SWAPPING_BUFFERS);
			
			long[] p0 = new long[1];
			
			VulkanErrorUtils.assertNoError(VK10.vkCreateDescriptorPool(device.get().getVk(), poolInfo, null, p0), "Failed to create descriptor pool!");
			
			descriptorPool = p0[0];
			
			Map<Integer, ShaderLayoutBinding> bindingFlags = new HashMap<>();
			extractData(vertexShader.require(), bindingFlags);
			extractData(tessellationControlShader.get(), bindingFlags);
			extractData(tessellationEvaluationShader.get(), bindingFlags);
			extractData(geometryShader.get(), bindingFlags);
			extractData(fragmentShader.get(), bindingFlags);
			extractData(computeShader.get(), bindingFlags);
			
			boolean useUniform = false;
			boolean useStorage = false;
			
			VkDescriptorSetLayoutBinding.Buffer descriptorSetLayoutBindings = VkDescriptorSetLayoutBinding.malloc(bindingFlags.size(), stack);// static
			for (Entry<Integer, ShaderLayoutBinding> e : bindingFlags.entrySet())
			{
				ShaderLayout.Type type = e.getValue().getDescriptorType();
				VkDescriptorSetLayoutBinding descriptorSetLayoutBinding = descriptorSetLayoutBindings.get();
				descriptorSetLayoutBinding.binding(e.getKey());
				descriptorSetLayoutBinding.descriptorCount(1);
				descriptorSetLayoutBinding.descriptorType(type.getType());
				descriptorSetLayoutBinding.pImmutableSamplers(null);
				descriptorSetLayoutBinding.stageFlags(e.getValue().getStages());
				
				switch (type)
				{
					case UNIFORM_BUFFER, UNIFORM_BUFFER_DYNAMIC, INLINE_UNIFORM_BLOCK -> useUniform = true;
					case STORAGE_BUFFER, STORAGE_BUFFER_DYNAMIC -> useStorage = true;
				}
			}
			descriptorSetLayoutBindings.rewind();
			
			LongBuffer p = stack.callocLong(SWAPPING_BUFFERS);
			
			descriptorSetLayouts = new ArrayList<>(SWAPPING_BUFFERS);
			for (int i = 0; i < SWAPPING_BUFFERS; i++)
			{
				VulkanErrorUtils.assertNoError(VK10.vkCreateDescriptorSetLayout(device.get().getVk(), VulkanUtils.descriptorSetLayoutInfo(stack, 0, descriptorSetLayoutBindings), null, p), "Failed to create descriptor set layout!");
				descriptorSetLayouts.add(p.get());
			}
			p.rewind();
			
			long[] p1 = new long[SWAPPING_BUFFERS];
			
			descriptorSets = new long[SWAPPING_BUFFERS][1];
			VulkanErrorUtils.assertNoError(VK10.vkAllocateDescriptorSets(device.get().getVk(), VulkanUtils.descriptorSetAllocateInfo(stack, descriptorPool, p), p1), "Failed to allocate descriptor sets!");
			
			VulkanBuffer uniformBuffer = null;
			VulkanBuffer storageBuffer = null;
			if (useUniform)
			{
				uniformBuffers = new ArrayList<>(SWAPPING_BUFFERS);
				uniformBuffer = this.uniformBuffer.orElse(new VulkanBuffer(device.get(), 1, VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT));
			}
			else uniformBuffers = List.of();
			if (useStorage)
			{
				storageBuffer = this.storageBuffer.orElse(new VulkanBuffer(device.get(), 1, VK10.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT));
			}
			
			int minOffsetAlignment = (int) physicalDevice.get().getLimits().minUniformBufferOffsetAlignment();
			
			for (int i = 0; i < SWAPPING_BUFFERS; i++)
			{
				descriptorSets[i] = new long[] {p1[i]};
				
				VkWriteDescriptorSet.Buffer writeDescriptorSets = VkWriteDescriptorSet.malloc(bindingFlags.size(), stack);
				BufferLayout uniformBufferLayout = new BufferLayout(writeDescriptorSets, 0);
				BufferLayout storageBufferLayout = new BufferLayout(writeDescriptorSets, 0);
				if (useUniform) uniformBuffers.add(new Pair<>(uniformBuffer, uniformBufferLayout));
				
				int offset = 0;
				for (Entry<Integer, ShaderLayoutBinding> e : bindingFlags.entrySet())
				{
					if (offset % minOffsetAlignment != 0)
					{
						offset += minOffsetAlignment - offset % minOffsetAlignment;
					}
					System.out.println("Binding " + e.getKey() + " " + e.getValue().getDescriptorType());
					VkWriteDescriptorSet writeDescriptorSet = writeDescriptorSets.get();
					writeDescriptorSet.sType(VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
					writeDescriptorSet.pNext(0);
					writeDescriptorSet.dstSet(p1[i]);
					writeDescriptorSet.dstBinding(e.getKey());
					writeDescriptorSet.dstArrayElement(0);
					writeDescriptorSet.descriptorCount(1);
					writeDescriptorSet.descriptorType(e.getValue().getDescriptorType().getType());
					
					Set<ShaderVariable> variables = e.getValue().getLayoutsVariables();
					switch (e.getValue().getDescriptorType())
					{
						case UNIFORM_BUFFER ->
						{
							VkDescriptorBufferInfo.Buffer descriptorBufferInfo = VkDescriptorBufferInfo.calloc(1);
							descriptorBufferInfo.buffer(uniformBuffer.getVk());
							descriptorBufferInfo.offset(offset);
							for (ShaderVariable variable : variables)
							{
								uniformBufferLayout.mark(variable, offset / 4);
								offset += variable.getType().getLength();
							}
							descriptorBufferInfo.range(offset - descriptorBufferInfo.offset());
							writeDescriptorSet.pBufferInfo(descriptorBufferInfo);
							writeDescriptorSet.pImageInfo(null);
							writeDescriptorSet.pTexelBufferView(null);
						}
						case STORAGE_BUFFER ->
						{
							System.out.println("Storage");
							VkDescriptorBufferInfo.Buffer descriptorBufferInfo = VkDescriptorBufferInfo.calloc(1);
							descriptorBufferInfo.buffer(storageBuffer.getVk());
							descriptorBufferInfo.offset(offset);
							for (ShaderVariable variable : variables)
							{
								storageBufferLayout.mark(variable, offset / 4);
								offset += variable.getType().getLength();
							}
							descriptorBufferInfo.range(offset - descriptorBufferInfo.offset());
							writeDescriptorSet.pBufferInfo(descriptorBufferInfo);
							writeDescriptorSet.pImageInfo(null);
							writeDescriptorSet.pTexelBufferView(null);
						}
						case COMBINED_IMAGE_SAMPLER ->
						{
							VkDescriptorImageInfo.Buffer descriptorImageInfos = VkDescriptorImageInfo.calloc(variables.size());
							for (ShaderVariable variable : variables)
							{
								uniformBufferLayout.mark(variable, writeDescriptorSets.position() - 1);
								VkDescriptorImageInfo descriptorImageInfo = descriptorImageInfos.get();
								descriptorImageInfo.imageLayout(VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
								descriptorImageInfo.imageView(0);
								descriptorImageInfo.sampler(0);
							}
							descriptorImageInfos.rewind();
							writeDescriptorSet.pBufferInfo(null);
							writeDescriptorSet.pImageInfo(descriptorImageInfos);
							writeDescriptorSet.pTexelBufferView(null);
						}
					}
				}
				writeDescriptorSets.rewind();
			}
		}
		
		private void extractData(PreparedShader shader, Map<Integer, ShaderLayoutBinding> bindingFlags)
		{
			if (shader != null)
			{
				ShaderInfo info = shader.info();
				ShaderLayoutBinding layoutBinding = new ShaderLayoutBinding(info.getType().getStageBit(), null, null);
				for (ShaderLayout layout : info.getLayouts())
				{
					switch (layout.getType())
					{
						case COMBINED_IMAGE_SAMPLER ->
						{
							ShaderCombinedImageSamplerLayout combinedImageSamplerLayout = (ShaderCombinedImageSamplerLayout) layout;
							layoutBinding.setLayoutsVariables(new LinkedHashSet<>(combinedImageSamplerLayout.getVariables()));
							layoutBinding.setDescriptorType(layout.getType());
							
							bindingFlags.merge(combinedImageSamplerLayout.getBinding(), layoutBinding, (v, w) -> {
								v.getLayoutsVariables().addAll(w.getLayoutsVariables());
								v.setStages(v.getStages() | w.getStages());
								return v;
							});
						}
						case UNIFORM_BUFFER ->
						{
							ShaderUniformBufferLayout uniformBufferLayout = (ShaderUniformBufferLayout) layout;
							layoutBinding.setLayoutsVariables(new LinkedHashSet<>(uniformBufferLayout.getVariables()));
							layoutBinding.setDescriptorType(layout.getType());
							
							bindingFlags.merge(uniformBufferLayout.getBinding(), layoutBinding, (v, w) -> {
								v.getLayoutsVariables().addAll(w.getLayoutsVariables());
								v.setStages(v.getStages() | w.getStages());
								return v;
							});
						}
						case STORAGE_BUFFER ->
						{
							ShaderStorageBufferLayout storageBufferLayout = (ShaderStorageBufferLayout) layout;
							layoutBinding.setLayoutsVariables(new LinkedHashSet<>(storageBufferLayout.getVariables()));
							layoutBinding.setDescriptorType(layout.getType());
							
							bindingFlags.merge(storageBufferLayout.getBinding(), layoutBinding, (v, w) -> {
								v.getLayoutsVariables().addAll(w.getLayoutsVariables());
								v.setStages(v.getStages() | w.getStages());
								return v;
							});
						}
					}
				}
			}
		}
		
		@Override
		public RDA uniformBuffer(@Nullable VulkanBuffer value)
		{
			return super.uniformBuffer(value);
		}
		
		@Override
		public RDA storageBuffer(VulkanBuffer value)
		{
			return super.storageBuffer(value);
		}
		
		public RDA shader(@Nullable Shader shader, @Nullable ShaderInfo info, ShaderType type)
		{
			return super.genericShader(shader, info, type);
		}
		
		@Override
		public RDA vertexShader(@Nullable Shader shader, @Nullable ShaderInfo info)
		{
			return super.vertexShader(shader, info);
		}
		
		@Override
		public RDA tessellationControlShader(@Nullable Shader shader, @Nullable ShaderInfo info)
		{
			return super.tessellationControlShader(shader, info);
		}
		
		@Override
		public RDA tessellationEvaluationShader(@Nullable Shader shader, @Nullable ShaderInfo info)
		{
			return super.tessellationEvaluationShader(shader, info);
		}
		
		@Override
		public RDA geometryShader(@Nullable Shader shader, @Nullable ShaderInfo info)
		{
			return super.geometryShader(shader, info);
		}
		
		@Override
		public RDA fragmentShader(@Nullable Shader shader, @Nullable ShaderInfo info)
		{
			return super.fragmentShader(shader, info);
		}
		
		@Override
		public RDA computeShader(@Nullable Shader shader, @Nullable ShaderInfo info)
		{
			return super.computeShader(shader, info);
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
