package com.alcadrial.renderengine.render.test.animation;

import java.util.Arrays;
import java.util.List;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo.Buffer;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.pipeline.GraphicsPipeline;
import com.alcadrial.renderengine.render.pipeline.PipelineLayout;
import com.alcadrial.renderengine.render.renderpass.RenderPass;
import com.alcadrial.renderengine.render.shader.PreparedShader;
import com.alcadrial.renderengine.render.shader.ShaderInputType;
import com.alcadrial.renderengine.render.shader.ShaderProgram;
import com.alcadrial.renderengine.render.shader.ShaderType;
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public class AnimationInstancingGraphicsPipeline extends GraphicsPipeline {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public AnimationInstancingGraphicsPipeline(Device device, PipelineLayout layout, boolean autoCloseLayout, RenderPass renderPass, boolean autoCloseRenderPass, Buffer createInfo, ShaderProgram shaderProgram) throws VulkanException
	{
		super(device, layout, autoCloseLayout, renderPass, autoCloseRenderPass, createInfo, shaderProgram);
	}
	
	@Override
	protected void createVk(Buffer createInfo) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkPipelineVertexInputStateCreateInfo vertexInputInfo = VkPipelineVertexInputStateCreateInfo.calloc(stack);
			vertexInputInfo.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
			
			VkVertexInputBindingDescription.Buffer bindingDescription = VkVertexInputBindingDescription.calloc(2, stack);
			bindingDescription.get().set(0, 40, VK10.VK_VERTEX_INPUT_RATE_VERTEX);
			bindingDescription.get().set(1, 16, VK10.VK_VERTEX_INPUT_RATE_INSTANCE);
			
			VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.calloc(5, stack);
			attributeDescriptions.get().set(0, 0, ShaderInputType.VECTOR_FLOAT_3.getVulkanType(), 0);
			attributeDescriptions.get().set(1, 0, ShaderInputType.VECTOR_FLOAT_4.getVulkanType(), 12);
			attributeDescriptions.get().set(2, 0, ShaderInputType.VECTOR_FLOAT_3.getVulkanType(), 28);
			attributeDescriptions.get().set(3, 1, ShaderInputType.VECTOR_FLOAT_3.getVulkanType(), 0);
			attributeDescriptions.get().set(4, 1, ShaderInputType.INT.getVulkanType(), 12);
			
			vertexInputInfo.pVertexBindingDescriptions(bindingDescription.rewind());
			vertexInputInfo.pVertexAttributeDescriptions(attributeDescriptions.rewind());
			
			inputs = Arrays.asList(ShaderVariable.VERTEX_POSITION, ShaderVariable.COLOR, ShaderVariable.NORMAL_POSITION);
			
			VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.calloc(2, stack);
			{
				List<PreparedShader> vertexShaders = shaderProgram.getShaders().stream().filter(ps -> ps.info().getType() == ShaderType.VERTEX).toList();
				List<PreparedShader> fragmentShaders = shaderProgram.getShaders().stream().filter(ps -> ps.info().getType() == ShaderType.FRAGMENT).toList();
				VkPipelineShaderStageCreateInfo stage = shaderStages.get();
				for (PreparedShader prepared : vertexShaders)
				{
					fillShaderStageCreateInfo(stack, stage, prepared);
				}
				stage = shaderStages.get();
				for (PreparedShader prepared : fragmentShaders)
				{
					fillShaderStageCreateInfo(stack, stage, prepared);
				}
			}
			
			createInfo.pVertexInputState(vertexInputInfo);
			createInfo.pStages(shaderStages.rewind());
			createInfo.layout(layout.getVk());
			createInfo.renderPass(renderPass.getVk());
			
			long[] p = new long[1];
			VulkanErrorUtils.assertNoError(VK10.vkCreateGraphicsPipelines(device.getVk(), 0, createInfo, null, p), "Failed to create graphics pipeline");
			vk = p[0];
			
			for (VkPipelineShaderStageCreateInfo stage : shaderStages)
			{
				VK10.vkDestroyShaderModule(device.getVk(), stage.module(), null);
			}
		}
	}
}
