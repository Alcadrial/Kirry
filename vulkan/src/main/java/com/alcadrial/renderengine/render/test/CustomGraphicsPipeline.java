package com.alcadrial.renderengine.render.test;

import java.util.Arrays;
import java.util.Optional;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo.Buffer;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.RenderPass;
import com.alcadrial.renderengine.render.pipeline.GraphicsPipeline;
import com.alcadrial.renderengine.render.pipeline.PipelineLayout;
import com.alcadrial.renderengine.render.shader.PreparedShader;
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;

public class CustomGraphicsPipeline extends GraphicsPipeline {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public CustomGraphicsPipeline(Device device, PipelineLayout layout, boolean autoCloseLayout, RenderPass renderPass, boolean autoCloseRenderPass, Buffer createInfo, PreparedShader vertexShader, Optional<PreparedShader> tessellationControlShader, Optional<PreparedShader> tessellationEvaluationShader, Optional<PreparedShader> geometryShader, Optional<PreparedShader> fragmentShader)
	{
		super(device, layout, autoCloseLayout, renderPass, autoCloseRenderPass, createInfo, vertexShader, tessellationControlShader, tessellationEvaluationShader, geometryShader, fragmentShader);
	}
	
	@Override
	protected void createVk(Buffer createInfo)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkPipelineVertexInputStateCreateInfo vertexInputInfo = VkPipelineVertexInputStateCreateInfo.calloc(stack);
			vertexInputInfo.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
			
			VkVertexInputBindingDescription.Buffer bindingDescription = VkVertexInputBindingDescription.calloc(2, stack);
			bindingDescription.get().set(0, 16, VK10.VK_VERTEX_INPUT_RATE_VERTEX);
			bindingDescription.get().set(1, 40, VK10.VK_VERTEX_INPUT_RATE_INSTANCE);
			
			VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.calloc(2, stack);
			attributeDescriptions.get().set(1, 0, ShaderVariable.VERTEX_POSITION.getType().getType(), 0);
			attributeDescriptions.get().set(0, 1, ShaderVariable.COLOR.getType().getType(), 24);
			
			vertexInputInfo.pVertexBindingDescriptions(bindingDescription.rewind());
			vertexInputInfo.pVertexAttributeDescriptions(attributeDescriptions.rewind());
			
			inputs = Arrays.asList(ShaderVariable.COLOR);
			
			int stages = 2;
			
			VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.calloc(stages, stack);
			
			fillShaderStageCreateInfo(stack, shaderStages.get(), vertexShader);
			fillShaderStageCreateInfo(stack, shaderStages.get(), fragmentShader.orElseThrow());
			
			createInfo.pVertexInputState(vertexInputInfo);
			createInfo.pStages(shaderStages.rewind());
			createInfo.layout(layout.getVk());
			createInfo.renderPass(renderPass.getVk());
			
			long[] p = new long[1];
			VulkanErrorUtils.assertNoError(VK10.vkCreateGraphicsPipelines(device.getVk(), 0, createInfo, null, p), "Failed to create graphics pipeline");
			vk = p[0];
			
			for (VkPipelineShaderStageCreateInfo stage : shaderStages) VK10.vkDestroyShaderModule(device.getVk(), stage.module(), null);
		}
	}
}
