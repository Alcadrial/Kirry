package com.alcadrial.renderengine.render.pipeline;

import java.util.List;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;

import com.alcadrial.renderengine.core.util.Pair;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.shader.PreparedShader;
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.shader.layouts.ShaderInputLayout;
import com.alcadrial.renderengine.render.shader.layouts.ShaderLayout.Type;
import com.alcadrial.renderengine.render.util.RawDeviceResource;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.VulkanUtils;

/**
 * A sequence of operation that must be executed in order.
 * 
 * @author Alcadrial
 */
public abstract class Pipeline extends RawDeviceResource {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	protected PipelineLayout layout;
	
	public Pipeline(Device device, PipelineLayout layout)
	{
		super(device);
		this.layout = layout;
	}
	
	@Override
	public void setName(String name)
	{
		device.setObjectDebugName(this, name);
		this.name = name;
	}
	
	public abstract int bindPoint();
	
	protected void fillShaderStageCreateInfo(MemoryStack stack, VkPipelineShaderStageCreateInfo shaderStageInfo, PreparedShader shader)
	{
		long[] p = new long[1];
		VulkanErrorUtils.assertNoError(VK10.vkCreateShaderModule(device.getVk(), VulkanUtils.shaderModuleInfo(stack, shader.shader().getLastCompilation().orElseThrow().getBinary()), null, p), "Failed to create shader module");
		
		shaderStageInfo.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
		shaderStageInfo.stage(shader.info().getType().getStageBit());
		shaderStageInfo.module(p[0]);
		shaderStageInfo.pName(stack.UTF8(shader.info().getEntryPoint()));
	}
	
	protected List<ShaderVariable> extractInputs(PreparedShader vertexShader)
	{
		ShaderInputLayout inputLayout = (ShaderInputLayout) vertexShader.info().getLayouts().stream().filter(l -> l.getType() == Type.INPUT).findFirst().orElseThrow(() -> new RuntimeException("Missing input layout"));
		return inputLayout.getVariables().stream().map(Pair::r).toList();
	}
	
	public PipelineLayout getLayout()
	{
		return layout;
	}
	
	@Override
	public void close()
	{
		if (vk != null) VK10.vkDestroyPipeline(device.getVk(), vk, null);
		super.close();
	}
}
