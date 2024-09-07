package com.alcadrial.renderengine.render.test;

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
import com.alcadrial.renderengine.render.shader.ShaderProgram;
import com.alcadrial.renderengine.render.shader.ShaderType;
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;

public class ComputeCustomGraphicsPipeline extends GraphicsPipeline {

	static
	{
		RenderEngine.assertLoaded();
	}

	public ComputeCustomGraphicsPipeline(Device device, PipelineLayout layout, boolean autoCloseLayout, RenderPass renderPass, boolean autoCloseRenderPass, Buffer createInfo, ShaderProgram shaderProgram)
	{
		super(device, layout, autoCloseLayout, renderPass, autoCloseRenderPass, createInfo, shaderProgram);
	}

	@Override
	protected void createVk(Buffer createInfo)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkPipelineVertexInputStateCreateInfo vertexInputInfo = VkPipelineVertexInputStateCreateInfo.calloc(stack);
			vertexInputInfo.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);

			VkVertexInputBindingDescription.Buffer bindingDescription = VkVertexInputBindingDescription.calloc(2, stack);
			bindingDescription.get().set(0, 12, VK10.VK_VERTEX_INPUT_RATE_VERTEX);
			bindingDescription.get().set(1, 48, VK10.VK_VERTEX_INPUT_RATE_INSTANCE);

			VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.calloc(3, stack);
			attributeDescriptions.get().set(0, 0, ShaderVariable.VERTEX_POSITION.getType().getType(), 0);
			attributeDescriptions.get().set(1, 1, ShaderVariable.COLOR.getType().getType(), 32);
			attributeDescriptions.get().set(2, 1, ShaderVariable.POSITION.getType().getType(), 0);

			vertexInputInfo.pVertexBindingDescriptions(bindingDescription.rewind());
			vertexInputInfo.pVertexAttributeDescriptions(attributeDescriptions.rewind());

			inputs = Arrays.asList(ShaderVariable.VERTEX_POSITION);

			VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.calloc(2, stack);
			{
				List<PreparedShader> vertexShaders = shaderProgram.getshaders().stream().filter(ps -> ps.info().getType() == ShaderType.VERTEX).toList();
				List<PreparedShader> fragmentShaders = shaderProgram.getshaders().stream().filter(ps -> ps.info().getType() == ShaderType.FRAGMENT).toList();
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
