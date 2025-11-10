package com.alcadrial.renderengine.render.pipeline;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkComputePipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;

import com.alcadrial.renderengine.core.util.NotYetImplementedException;
import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.shader.PreparedShader;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

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
	
	private boolean autoCloseLayout;
	
	private PreparedShader computeShader;
	
	public ComputePipeline(Device device, PipelineLayout layout, boolean autoCloseLayout, PreparedShader computeShader) throws VulkanException
	{
		super(device, layout);
		this.autoCloseLayout = autoCloseLayout;
		this.computeShader = computeShader;
		
		//	ShaderStorageBufferLayout storageBufferLayout = computeShader.info().getLayouts().stream().filter(l -> l.getType() == Type.STORAGE_BUFFER).map(l -> (ShaderStorageBufferLayout) l).filter(l -> l.getUsage() == Usage.INPUT).findFirst().orElseThrow(() -> new RuntimeException("Missing input layout"));
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkPipelineShaderStageCreateInfo stage = VkPipelineShaderStageCreateInfo.calloc(stack);
			
			fillShaderStageCreateInfo(stack, stage, computeShader);
			
			VkComputePipelineCreateInfo.Buffer createInfo = VkComputePipelineCreateInfo.calloc(1, stack);
			createInfo.sType(VK10.VK_STRUCTURE_TYPE_COMPUTE_PIPELINE_CREATE_INFO);
			createInfo.stage(stage);
			createInfo.layout(layout.getVk());
			
			long[] p = new long[1];
			VulkanErrorUtils.assertNoError(VK10.vkCreateComputePipelines(device.getVk(), 0, createInfo, null, p), "Failed to create compute pipeline");
			vk = p[0];
			
			VK10.vkDestroyShaderModule(device.getVk(), stage.module(), null);
		}
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
	
	@Override
	public void debug()
	{
		Debug.print("Vk: " + Debug.address(vk));
		Debug.print("Layout", layout);
	}
}
