package com.alcadrial.renderengine.render.shader;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.collections.api.factory.Sets;
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
		ShaderInfo info = shader.info();
		for (ShaderLayout layout : info.getLayouts())
		{
			int stageBit = info.getType().getStageBit();
			switch (layout.getType())
			{
				case COMBINED_IMAGE_SAMPLER -> {
					ShaderCombinedImageSamplerLayout combinedImageSamplerLayout = (ShaderCombinedImageSamplerLayout) layout;
					bindingFlags.merge(combinedImageSamplerLayout.getBinding(), new ShaderLayoutBinding(stageBit, layout.getType(), Sets.mutable.ofAll(combinedImageSamplerLayout.getVariables())), ShaderUtils::merge);
				}
				case UNIFORM_BUFFER -> {
					ShaderUniformBufferLayout uniformBufferLayout = (ShaderUniformBufferLayout) layout;
					bindingFlags.merge(uniformBufferLayout.getBinding(), new ShaderLayoutBinding(stageBit, layout.getType(), Sets.mutable.ofAll(uniformBufferLayout.getVariables())), ShaderUtils::merge);
				}
				case STORAGE_BUFFER -> {
					ShaderStorageBufferLayout storageBufferLayout = (ShaderStorageBufferLayout) layout;
					bindingFlags.merge(storageBufferLayout.getBinding(), new ShaderLayoutBinding(stageBit, layout.getType(), Sets.mutable.ofAll(storageBufferLayout.getVariables())), ShaderUtils::merge);
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
			descriptorSetLayoutBindings.get()
				.binding(e.getKey())
				.descriptorCount(1)
				.descriptorType(e.getValue().getDescriptorType().getType())
				.pImmutableSamplers(null)
				.stageFlags(e.getValue().getStages());
		}
		return descriptorSetLayoutBindings.rewind();
	}
}
