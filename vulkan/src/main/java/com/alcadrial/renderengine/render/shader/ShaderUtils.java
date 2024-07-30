package com.alcadrial.renderengine.render.shader;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding.Buffer;

import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.shader.layouts.ShaderCombinedImageSamplerLayout;
import com.alcadrial.renderengine.render.shader.layouts.ShaderLayout;
import com.alcadrial.renderengine.render.shader.layouts.ShaderStorageBufferLayout;
import com.alcadrial.renderengine.render.shader.layouts.ShaderUniformBufferLayout;

public class ShaderUtils {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public static void extractData(PreparedShader shader, Map<Integer, ShaderLayoutBinding> bindingFlags)
	{
		if (shader != null)
		{
			ShaderInfo info = shader.info();
			for (ShaderLayout layout : info.getLayouts())
			{
				ShaderLayoutBinding layoutBinding = new ShaderLayoutBinding(info.getType().getStageBit(), null, null);
				switch (layout.getType())
				{
					case COMBINED_IMAGE_SAMPLER -> {
						ShaderCombinedImageSamplerLayout combinedImageSamplerLayout = (ShaderCombinedImageSamplerLayout) layout;
						layoutBinding.setLayoutsVariables(new LinkedHashSet<>(combinedImageSamplerLayout.getVariables()));
						layoutBinding.setDescriptorType(layout.getType());
						
						bindingFlags.merge(combinedImageSamplerLayout.getBinding(), layoutBinding, ShaderUtils::merge);
					}
					case UNIFORM_BUFFER -> {
						ShaderUniformBufferLayout uniformBufferLayout = (ShaderUniformBufferLayout) layout;
						layoutBinding.setLayoutsVariables(new LinkedHashSet<>(uniformBufferLayout.getVariables()));
						layoutBinding.setDescriptorType(layout.getType());
						bindingFlags.merge(uniformBufferLayout.getBinding(), layoutBinding, ShaderUtils::merge);
					}
					case STORAGE_BUFFER -> {
						ShaderStorageBufferLayout storageBufferLayout = (ShaderStorageBufferLayout) layout;
						layoutBinding.setLayoutsVariables(new LinkedHashSet<>(storageBufferLayout.getVariables()));
						layoutBinding.setDescriptorType(layout.getType());
						
						bindingFlags.merge(storageBufferLayout.getBinding(), layoutBinding, ShaderUtils::merge);
					}
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
	
	public static Buffer createDescriptorSetLayoutBindingBuffer(MemoryStack stack, Map<Integer, ShaderLayoutBinding> bindingFlags)
	{
		VkDescriptorSetLayoutBinding.Buffer descriptorSetLayoutBindings = VkDescriptorSetLayoutBinding.malloc(bindingFlags.size(), stack);// static
		for (Entry<Integer, ShaderLayoutBinding> e : bindingFlags.entrySet())
		{
			ShaderLayout.Type type = e.getValue().getDescriptorType();
			VkDescriptorSetLayoutBinding descriptorSetLayoutBinding = descriptorSetLayoutBindings.get();
			descriptorSetLayoutBinding.binding(e.getKey());
			descriptorSetLayoutBinding.descriptorCount(1);
			descriptorSetLayoutBinding.descriptorType(type.getType());
			descriptorSetLayoutBinding.pImmutableSamplers(null);
			descriptorSetLayoutBinding.stageFlags(e.getValue().getStages());
		}
		return descriptorSetLayoutBindings.rewind();
	}
}
