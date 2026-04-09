package com.alcadrial.renderengine.render.shader;

import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public class ShaderUtils {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public static VkDescriptorSetLayoutBinding.Buffer createDescriptorSetLayoutBindingBuffer(MemoryStack stack, Map<Integer, ShaderLayoutBinding> bindingFlags)
	{
		VkDescriptorSetLayoutBinding.Buffer descriptorSetLayoutBindings = VkDescriptorSetLayoutBinding.malloc(bindingFlags.size(), stack);
		for (Entry<Integer, ShaderLayoutBinding> e : bindingFlags.entrySet())
		{
			ShaderLayoutBinding layout = e.getValue();
			descriptorSetLayoutBindings.get()
				.binding(e.getKey())
				.descriptorCount(layout.getDescriptorCount())
				.descriptorType(layout.getDescriptorType().getType())
				.pImmutableSamplers(null)
				.stageFlags(layout.getStages());
		}
		return descriptorSetLayoutBindings.rewind();
	}
	
	public static VkPipelineShaderStageCreateInfo.Buffer createPipelineShaderStageCreateInfoBuffer(MemoryStack stack, Device device, ShaderProgram program) throws VulkanException
	{
		return createPipelineShaderStageCreateInfoBuffer(stack, device, program, "main");
	}
	
	public static VkPipelineShaderStageCreateInfo.Buffer createPipelineShaderStageCreateInfoBuffer(MemoryStack stack, Device device, ShaderProgram program, String entryPoint) throws VulkanException
	{
		VkPipelineShaderStageCreateInfo.Buffer createInfoBuffer = VkPipelineShaderStageCreateInfo.malloc(program.getShaders().size(), stack);
		VkShaderModuleCreateInfo createInfo = VkShaderModuleCreateInfo.malloc(stack)
			.sType(VK10.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
			.pNext(0)
			.flags(0);
		long[] p = new long[1];
		for (PreparedShader shader : program.getShaders())
		{
			createInfo.pCode(stack.bytes(shader.shader().getLastCompilation().orElseThrow().binary()));
			VulkanErrorUtils.assertNoError(VK10.vkCreateShaderModule(device.getVk(), createInfo, null, p), "Failed to create shader module");
			
			createInfoBuffer.get()
				.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
				.pNext(0)
				.flags(0)
				.stage(shader.type().getStageBit())
				.module(p[0])
				.pName(stack.UTF8(entryPoint))
				.pSpecializationInfo(/*shader constants*/null);
		}
		return createInfoBuffer.rewind();
	}
	
	public static VkPipelineShaderStageCreateInfo createPipelineShaderStageCreateInfoBuffer(MemoryStack stack, Device device, PreparedShader shader) throws VulkanException
	{
		return createPipelineShaderStageCreateInfoBuffer(stack, device, shader, "main");
	}
	
	public static VkPipelineShaderStageCreateInfo createPipelineShaderStageCreateInfoBuffer(MemoryStack stack, Device device, PreparedShader shader, String entryPoint) throws VulkanException
	{
		VkShaderModuleCreateInfo createInfo = VkShaderModuleCreateInfo.malloc(stack)
			.sType(VK10.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
			.pNext(0)
			.flags(0);
		long[] p = new long[1];
		createInfo.pCode(stack.bytes(shader.shader().getLastCompilation().orElseThrow().binary()));
		VulkanErrorUtils.assertNoError(VK10.vkCreateShaderModule(device.getVk(), createInfo, null, p), "Failed to create shader module");
		
		VkPipelineShaderStageCreateInfo createInfoBuffer = VkPipelineShaderStageCreateInfo.malloc(stack)
			.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
			.pNext(0)
			.flags(0)
			.stage(shader.type().getStageBit())
			.module(p[0])
			.pName(stack.UTF8(entryPoint))
			.pSpecializationInfo(/*shader constants*/null);
		return createInfoBuffer;
	}
	
	public static VkPipelineShaderStageCreateInfo.Buffer createPipelineShaderStageCreateInfoBuffer(Device device, ShaderProgram program) throws VulkanException
	{
		return createPipelineShaderStageCreateInfoBuffer(device, program, "main");
	}
	
	public static VkPipelineShaderStageCreateInfo.Buffer createPipelineShaderStageCreateInfoBuffer(Device device, ShaderProgram program, String entryPoint) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkPipelineShaderStageCreateInfo.Buffer createInfoBuffer = VkPipelineShaderStageCreateInfo.malloc(program.getShaders().size());
			VkShaderModuleCreateInfo createInfo = VkShaderModuleCreateInfo.malloc(stack)
				.sType(VK10.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
				.pNext(0)
				.flags(0);
			long[] p = new long[1];
			for (PreparedShader shader : program.getShaders())
			{
				createInfo.pCode(stack.bytes(shader.shader().getLastCompilation().orElseThrow().binary()));
				VulkanErrorUtils.assertNoError(VK10.vkCreateShaderModule(device.getVk(), createInfo, null, p), "Failed to create shader module");
				
				createInfoBuffer.get()
					.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
					.pNext(0)
					.flags(0)
					.stage(shader.type().getStageBit())
					.module(p[0])
					.pName(stack.UTF8(entryPoint))
					.pSpecializationInfo(/*shader constants*/null);
			}
			return createInfoBuffer.rewind();
		}
	}
	
	public static VkPipelineShaderStageCreateInfo createPipelineShaderStageCreateInfoBuffer(Device device, PreparedShader shader) throws VulkanException
	{
		return createPipelineShaderStageCreateInfoBuffer(device, shader, "main");
	}
	
	public static VkPipelineShaderStageCreateInfo createPipelineShaderStageCreateInfoBuffer(Device device, PreparedShader shader, String entryPoint) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkShaderModuleCreateInfo createInfo = VkShaderModuleCreateInfo.malloc(stack)
				.sType(VK10.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
				.pNext(0)
				.flags(0);
			long[] p = new long[1];
			createInfo.pCode(stack.bytes(shader.shader().getLastCompilation().orElseThrow().binary()));
			VulkanErrorUtils.assertNoError(VK10.vkCreateShaderModule(device.getVk(), createInfo, null, p), "Failed to create shader module");
			
			VkPipelineShaderStageCreateInfo createInfoBuffer = VkPipelineShaderStageCreateInfo.malloc()
				.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
				.pNext(0)
				.flags(0)
				.stage(shader.type().getStageBit())
				.module(p[0])
				.pName(stack.UTF8(entryPoint))
				.pSpecializationInfo(/*shader constants*/null);
			return createInfoBuffer;
		}
	}
}
