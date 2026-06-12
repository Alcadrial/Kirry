package com.alcadrial.kirry.render.pipeline;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkComputePipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;

import com.alcadrial.kirry.render.Debug;
import com.alcadrial.kirry.render.Device;
import com.alcadrial.kirry.render.KirryVulkan;
import com.alcadrial.kirry.render.shader.PreparedShader;
import com.alcadrial.kirry.render.shader.ShaderUtils;
import com.alcadrial.kirry.render.util.VulkanErrorUtils;
import com.alcadrial.kirry.render.util.exception.VulkanException;

public class ComputePipeline extends Pipeline {
	
	static
	{
		KirryVulkan.assertLoaded();
	}
	
	public ComputePipeline(Device device, PipelineLayout layout, PreparedShader shader) throws VulkanException
	{
		super(device, layout);
		VkComputePipelineCreateInfo createInfo;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkPipelineShaderStageCreateInfo shaderCreateInfo = ShaderUtils.createPipelineShaderStageCreateInfoBuffer(stack, device, shader);
			createInfo = VkComputePipelineCreateInfo.malloc(stack)
				.sType(VK10.VK_STRUCTURE_TYPE_COMPUTE_PIPELINE_CREATE_INFO)
				.pNext(0)
				.flags(0)
				.stage(shaderCreateInfo)
				.layout(layout.getVk())
				.basePipelineHandle(0)
				.basePipelineIndex(0);
			long[] p = new long[1];
			int error = VK10.vkCreateComputePipelines(device.getVk(), 0, VkComputePipelineCreateInfo.create(createInfo.address(), 1), null, p);
			VK10.vkDestroyShaderModule(device.getVk(), shaderCreateInfo.module(), null);
			VulkanErrorUtils.assertNoError(error, "Failed to create compute pipeline");
			vk = p[0];
		}
	}
	
	public ComputePipeline(Device device, PipelineLayout layout, VkComputePipelineCreateInfo createInfo) throws VulkanException
	{
		super(device, layout);
		
		long[] p = new long[1];
		VulkanErrorUtils.assertNoError(VK10.vkCreateComputePipelines(device.getVk(), 0, VkComputePipelineCreateInfo.create(createInfo.address(), 1), null, p), "Failed to create compute pipeline");
		vk = p[0];
	}
	
	@Override
	public int bindPoint()
	{
		return VK10.VK_PIPELINE_BIND_POINT_COMPUTE;
	}
	
	@Override
	public void debug()
	{
		Debug.print("Vk: " + Debug.address(vk));
		Debug.print("Layout", layout);
	}
}
