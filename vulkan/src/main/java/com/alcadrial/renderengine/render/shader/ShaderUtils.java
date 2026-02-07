package com.alcadrial.renderengine.render.shader;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.shader.layouts.ShaderCombinedImageSamplerLayout;
import com.alcadrial.renderengine.render.shader.layouts.ShaderLayout;
import com.alcadrial.renderengine.render.shader.layouts.ShaderStorageBufferLayout;
import com.alcadrial.renderengine.render.shader.layouts.ShaderUniformBufferLayout;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public class ShaderUtils {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public static Map<Integer, ShaderLayoutBinding> extractData(ShaderProgram shaderProgram)
	{
		Map<Integer, ShaderLayoutBinding> bindingFlags = Maps.mutable.empty();
		for (PreparedShader shader : shaderProgram.getShaders()) extractData(shader, bindingFlags);
		return bindingFlags;
	}
	
	public static void extractData(ShaderProgram shaderProgram, Map<Integer, ShaderLayoutBinding> bindingFlags)
	{
		for (PreparedShader shader : shaderProgram.getShaders()) extractData(shader, bindingFlags);
	}
	
	public static Map<Integer, ShaderLayoutBinding> extractData(PreparedShader shader)
	{
		Map<Integer, ShaderLayoutBinding> bindingFlags = Maps.mutable.empty();
		extractData(shader, bindingFlags);
		return bindingFlags;
	}
	
	public static void extractData(PreparedShader shader, Map<Integer, ShaderLayoutBinding> bindingFlags)
	{
		ShaderInfo info = shader.info();
		for (ShaderLayout layout : info.getLayouts())
		{
			int stageBit = info.getType().getStageBit();
			switch (layout.getType())
			{
				case COMBINED_IMAGE_SAMPLER -> {
					ShaderCombinedImageSamplerLayout combinedImageSamplerLayout = (ShaderCombinedImageSamplerLayout) layout;
					bindingFlags.merge(combinedImageSamplerLayout.getBinding(), new ShaderLayoutBinding(stageBit, layout.getType(), layout.getDescriptorCount(), Sets.mutable.ofAll(combinedImageSamplerLayout.getVariables())), ShaderUtils::merge);
				}
				case UNIFORM_BUFFER -> {
					ShaderUniformBufferLayout uniformBufferLayout = (ShaderUniformBufferLayout) layout;
					bindingFlags.merge(uniformBufferLayout.getBinding(), new ShaderLayoutBinding(stageBit, layout.getType(), layout.getDescriptorCount(), Sets.mutable.ofAll(uniformBufferLayout.getVariables())), ShaderUtils::merge);
				}
				case STORAGE_BUFFER -> {
					ShaderStorageBufferLayout storageBufferLayout = (ShaderStorageBufferLayout) layout;
					bindingFlags.merge(storageBufferLayout.getBinding(), new ShaderLayoutBinding(stageBit, layout.getType(), layout.getDescriptorCount(), Sets.mutable.ofAll(storageBufferLayout.getVariables())), ShaderUtils::merge);
				}
			}
		}
	}
	
	private static ShaderLayoutBinding merge(ShaderLayoutBinding v, ShaderLayoutBinding w)
	{
		v.getLayoutsVariables().addAll(w.getLayoutsVariables());
		v.setStages(v.getStages() | w.getStages());
		return v;
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
				.stage(shader.info().getType().getStageBit())
				.module(p[0])
				.pName(stack.UTF8(shader.info().getEntryPoint()))
				.pSpecializationInfo(/*shader constants*/null);
		}
		return createInfoBuffer;
	}
	
	public static VkPipelineShaderStageCreateInfo.Buffer createPipelineShaderStageCreateInfoBuffer(Device device, ShaderProgram program) throws VulkanException
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
					.stage(shader.info().getType().getStageBit())
					.module(p[0])
					.pName(MemoryUtil.memUTF8(shader.info().getEntryPoint()))
					.pSpecializationInfo(/*shader constants*/null);
			}
			return createInfoBuffer;
		}
	}
}
