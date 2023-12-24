package com.alcadrial.renderengine.render.pipeline;

import java.util.List;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkComputePipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;

import com.alcadrial.renderengine.core.util.NotYetImplementedException;
import com.alcadrial.renderengine.core.util.Nullable;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.shader.PreparedShader;
import com.alcadrial.renderengine.render.shader.Shader;
import com.alcadrial.renderengine.render.shader.ShaderInfo;
import com.alcadrial.renderengine.render.shader.ShaderInput;
import com.alcadrial.renderengine.render.util.DataAccumulator;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.VulkanUtils;

/**
 * A {@link Pipeline} used for rendering.
 * 
 * @author Alcadrial
 */
public class ComputePipeline extends Pipeline {
	
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
		public Builder computeShader(@Nullable Shader shader, @Nullable ShaderInfo info)
		{
			return super.computeShader(shader, info);
		}
		
		public ComputePipeline build(Device device, PipelineLayout layout, VkComputePipelineCreateInfo.Buffer createInfo)
		{
			try
			{
				return new ComputePipeline(device, layout, autoCloseLayout.orElse(true), createInfo, computeShader.require("The compute shader is required"));
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
	
	private PreparedShader computeShader;
	private List<ShaderInput> inputs;
	
	private ComputePipeline(Device device, PipelineLayout layout, boolean autoCloseLayout, VkComputePipelineCreateInfo.Buffer createInfo, PreparedShader computeShader)
	{
		super(device, layout);
		this.autoCloseLayout = autoCloseLayout;
		this.computeShader = computeShader;
		inputs = List.copyOf(computeShader.info().getInputs());
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkPipelineShaderStageCreateInfo stage = VkPipelineShaderStageCreateInfo.calloc(stack);
			
			fillShaderStageCreateInfo(stack, stage, computeShader);
			
			createInfo.stage(stage);
			createInfo.layout(layout.getVk());
			
			long[] p = new long[1];
			VulkanErrorUtils.assertNoError(VK10.vkCreateComputePipelines(device.getVk(), 0, createInfo, null, p), "Failed to create compute pipeline");
			vk = p[0];
			
			VK10.vkDestroyShaderModule(device.getVk(), stage.module(), null);
		}
	}
	
	private void fillShaderStageCreateInfo(MemoryStack stack, VkPipelineShaderStageCreateInfo shaderStageInfo, PreparedShader shader)
	{
		long[] p = new long[1];
		VulkanErrorUtils.assertNoError(VK10.vkCreateShaderModule(device.getVk(), VulkanUtils.shaderModuleInfo(stack, shader.shader().getLastCompilation().orElseThrow().getBinary()), null, p), "Failed to create shader module");
		
		shaderStageInfo.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
		shaderStageInfo.stage(shader.info().getType().getStageBit());
		shaderStageInfo.module(p[0]);
		shaderStageInfo.pName(stack.UTF8(shader.info().getEntryPoint()));
	}
	
	void recreate()
	{
		throw new NotYetImplementedException();
	}
	
	@Override
	public PipelineLayout getLayout()
	{
		return layout;
	}
	
	@Override
	public int bindPoint()
	{
		return VK10.VK_PIPELINE_BIND_POINT_COMPUTE;
	}
	
	public List<ShaderInput> getInputs()
	{
		return inputs;
	}
	
	public PreparedShader getComputeShader()
	{
		return computeShader;
	}
	
	@Override
	public void close()
	{
		if (autoCloseLayout) layout.close();
		super.close();
	}
}
