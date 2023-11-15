package com.alcadrial.renderengine.render.pipeline;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import com.alcadrial.renderengine.core.util.NotYetImplementedException;
import com.alcadrial.renderengine.core.util.Nullable;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.RenderPass;
import com.alcadrial.renderengine.render.shader.Shader;
import com.alcadrial.renderengine.render.shader.ShaderInput;
import com.alcadrial.renderengine.render.shader.ShaderInputType;
import com.alcadrial.renderengine.render.util.DataAccumulator;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.VulkanUtils;

/**
 * A {@link Pipeline} used for rendering.
 * 
 * @author Alcadrial
 */
public class GraphicsPipeline extends Pipeline {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public static final class Builder extends DataAccumulator<Builder> {
		
		@Override
		public Builder autoCloseLayout(@Nullable Boolean value)
		{
			return super.autoCloseLayout(value);
		}
		
		@Override
		public Builder autoCloseRenderPass(@Nullable Boolean value)
		{
			return super.autoCloseRenderPass(value);
		}
		
		/**
		 * Uses {@link Shader#getType()} to distinguish the type
		 * @param shader
		 * @return this for chaining
		 */
		@Override
		public Builder shader(@Nullable Shader shader)
		{
			return super.genericShader(shader);
		}
		
		@Override
		public Builder vertexShader(@Nullable Shader shader)
		{
			return super.vertexShader(shader);
		}
		
		@Override
		public Builder tessellationControlShader(@Nullable Shader shader)
		{
			return super.tessellationControlShader(shader);
		}
		
		@Override
		public Builder tessellationEvaluationShader(@Nullable Shader shader)
		{
			return super.tessellationEvaluationShader(shader);
		}
		
		@Override
		public Builder geometryShader(@Nullable Shader shader)
		{
			return super.geometryShader(shader);
		}
		
		@Override
		public Builder fragmentShader(@Nullable Shader shader)
		{
			return super.fragmentShader(shader);
		}
		
		@Override
		public Builder computeShader(@Nullable Shader shader)
		{
			return super.computeShader(shader);
		}
		
		public GraphicsPipeline build(Device device, PipelineLayout layout, RenderPass renderPass, VkGraphicsPipelineCreateInfo.Buffer createInfo)
		{
			try
			{
				return new GraphicsPipeline(device, layout, autoCloseLayout.orElse(true), renderPass, autoCloseRenderPass.orElse(true), createInfo, vertexShader.require("The vertex shader is required"), tessellationControlShader.asOptional(), tessellationEvaluationShader.asOptional(), geometryShader.asOptional(), fragmentShader.asOptional(), computeShader.asOptional());
			}
			finally
			{
				close();
			}
		}
	}
	
	public static Builder create()
	{
		return new Builder();
	}
	
	private boolean autoCloseLayout;
	private RenderPass renderPass;
	private boolean autoCloseRenderPass;
	
	private Shader vertexShader;
	private Optional<Shader> tessellationControlShader;
	private Optional<Shader> tessellationEvaluationShader;
	private Optional<Shader> geometryShader;
	private Optional<Shader> fragmentShader;
	private Optional<Shader> computeShader;
	
	private List<ShaderInput> inputs;
	
	private GraphicsPipeline(Device device, PipelineLayout layout, boolean autoCloseLayout, RenderPass renderPass, boolean autoCloseRenderPass, VkGraphicsPipelineCreateInfo.Buffer createInfo, Shader vertexShader, Optional<Shader> tessellationControlShader, Optional<Shader> tessellationEvaluationShader, Optional<Shader> geometryShader, Optional<Shader> fragmentShader, Optional<Shader> computeShader)
	{
		super(device, layout);
		this.autoCloseLayout = autoCloseLayout;
		this.renderPass = renderPass;
		this.autoCloseRenderPass = autoCloseRenderPass;
		this.vertexShader = vertexShader;
		this.tessellationControlShader = tessellationControlShader;
		this.tessellationEvaluationShader = tessellationEvaluationShader;
		this.geometryShader = geometryShader;
		this.fragmentShader = fragmentShader;
		this.computeShader = computeShader;
		inputs = List.of();
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkPipelineVertexInputStateCreateInfo vertexInputInfo = VkPipelineVertexInputStateCreateInfo.calloc(stack);
			vertexInputInfo.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
			
			int size = 0;
			int locationSize = 0;
			Set<ShaderInput> inputs = vertexShader.getInputs();
			VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.calloc(inputs.size());
			for (ShaderInput input : inputs)
			{
				ShaderInputType type = input.getType();
				attributeDescriptions.get().set(locationSize, 0, type.getType(), size);
				size += type.getLength();
				locationSize += type.getLocationSize();
			}
			
			VkVertexInputBindingDescription.Buffer bindingDescription = VkVertexInputBindingDescription.calloc(1, stack);
			bindingDescription.binding(0);
			bindingDescription.stride(size);
			bindingDescription.inputRate(VK10.VK_VERTEX_INPUT_RATE_VERTEX);
			vertexInputInfo.pVertexBindingDescriptions(bindingDescription);
			vertexInputInfo.pVertexAttributeDescriptions(attributeDescriptions.rewind());
			
			this.inputs = List.copyOf(inputs);
			
			int stages = 1;
			if (tessellationControlShader.isPresent()) stages++;
			if (tessellationEvaluationShader.isPresent()) stages++;
			if (geometryShader.isPresent()) stages++;
			if (fragmentShader.isPresent()) stages++;
			if (computeShader.isPresent()) stages++;
			
			VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.calloc(stages, stack);
			
			fillShaderStageCreateInfo(stack, shaderStages.get(), vertexShader);
			tessellationControlShader.ifPresent(shader -> fillShaderStageCreateInfo(stack, shaderStages.get(), shader));
			tessellationEvaluationShader.ifPresent(shader -> fillShaderStageCreateInfo(stack, shaderStages.get(), shader));
			geometryShader.ifPresent(shader -> fillShaderStageCreateInfo(stack, shaderStages.get(), shader));
			fragmentShader.ifPresent(shader -> fillShaderStageCreateInfo(stack, shaderStages.get(), shader));
			computeShader.ifPresent(shader -> fillShaderStageCreateInfo(stack, shaderStages.get(), shader));
			
			createInfo.pVertexInputState(vertexInputInfo);
			createInfo.pStages(shaderStages.rewind());
			createInfo.layout(layout.getVk());
			createInfo.renderPass(renderPass.getVk());
			
			long[] p = new long[1];
			VulkanErrorUtils.assertNoError(VK10.vkCreateGraphicsPipelines(device.getVk(), 0, createInfo, null, p), "Failed to create graphics pipeline");
			vk = p[0];
			
			for (VkPipelineShaderStageCreateInfo shaderStageCreateInfo : shaderStages)
			{
				VK10.vkDestroyShaderModule(device.getVk(), shaderStageCreateInfo.module(), null);
			}
		}
	}
	
	private void fillShaderStageCreateInfo(MemoryStack stack, VkPipelineShaderStageCreateInfo shaderStageInfo, Shader shader)
	{
		shaderStageInfo.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
		shaderStageInfo.stage(shader.getType().getStageBit());
		
		long[] p = new long[1];
		VulkanErrorUtils.assertNoError(VK10.vkCreateShaderModule(device.getVk(), VulkanUtils.shaderModuleInfo(stack, shader.getOrCompile().getBinary()), null, p), "Failed to create shader module");
		
		shaderStageInfo.module(p[0]);
		shaderStageInfo.pName(stack.UTF8(shader.getEntryPoint()));
	}
	
	public void recreate()
	{
		throw new NotYetImplementedException();
	}
	
	@Override
	public PipelineLayout getLayout()
	{
		return layout;
	}
	
	public RenderPass getRenderPass()
	{
		return renderPass;
	}
	
	@Override
	public int bindPoint()
	{
		return VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;
	}
	
	public List<ShaderInput> getInputs()
	{
		return inputs;
	}
	
	public Shader getVertexShader()
	{
		return vertexShader;
	}
	
	public Optional<Shader> getTessellationControlShader()
	{
		return tessellationControlShader;
	}
	
	public Optional<Shader> getTessellationEvaluationShader()
	{
		return tessellationEvaluationShader;
	}
	
	public Optional<Shader> getGeometryShader()
	{
		return geometryShader;
	}
	
	public Optional<Shader> getFragmentShader()
	{
		return fragmentShader;
	}
	
	public Optional<Shader> getComputeShader()
	{
		return computeShader;
	}
	
	@Override
	public void close()
	{
		if (autoCloseLayout) layout.close();
		if (autoCloseRenderPass) renderPass.close();
		super.close();
	}
}
