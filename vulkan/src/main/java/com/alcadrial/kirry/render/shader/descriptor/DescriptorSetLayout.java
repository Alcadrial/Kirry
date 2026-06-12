package com.alcadrial.kirry.render.shader.descriptor;

import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;

import com.alcadrial.kirry.render.Debug;
import com.alcadrial.kirry.render.Device;
import com.alcadrial.kirry.render.KirryVulkan;
import com.alcadrial.kirry.render.shader.ShaderLayoutBinding;
import com.alcadrial.kirry.render.shader.layout.ShaderLayout;
import com.alcadrial.kirry.render.util.RawDeviceResource;
import com.alcadrial.kirry.render.util.VulkanErrorUtils;
import com.alcadrial.kirry.render.util.exception.VulkanException;

public class DescriptorSetLayout extends RawDeviceResource {
	
	static
	{
		KirryVulkan.assertLoaded();
	}
	/*
	public static DescriptorSetLayout ofShaderProgram(Device device, ShaderProgram shaderProgram) throws VulkanException
	{
		return ofBindings(device, ShaderUtils.extractData(shaderProgram));
	}
	
	public static DescriptorSetLayout ofShader(Device device, PreparedShader preparedShader) throws VulkanException
	{
		return ofBindings(device, ShaderUtils.extractData(preparedShader));
	}
	*/
	public static DescriptorSetLayout ofBindings(Device device, Map<Integer, ShaderLayoutBinding> bindings) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkDescriptorSetLayoutBinding.Buffer descriptorSetLayoutBindings = VkDescriptorSetLayoutBinding.malloc(bindings.size(), stack);
			for (Entry<Integer, ShaderLayoutBinding> e : bindings.entrySet())
			{
				ShaderLayoutBinding layout = e.getValue();
				ShaderLayout.Type type = layout.getDescriptorType();
				descriptorSetLayoutBindings.get()
					.binding(e.getKey())
					.descriptorCount(layout.getDescriptorCount())
					.descriptorType(type.getType())
					.pImmutableSamplers(null)
					.stageFlags(layout.getStages());
			}
			
			VkDescriptorSetLayoutCreateInfo createInfo = VkDescriptorSetLayoutCreateInfo.calloc(stack)
				.sType(VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
				.pBindings(descriptorSetLayoutBindings.rewind());
			
			return new DescriptorSetLayout(device, createInfo);
		}
	}
	
	public DescriptorSetLayout(Device device, VkDescriptorSetLayoutCreateInfo createInfo) throws VulkanException
	{
		super(device);
		
		long[] p = new long[1];
		VulkanErrorUtils.assertNoError(VK10.vkCreateDescriptorSetLayout(device.getVk(), createInfo, null, p), "Failed to create descriptor set layout");
		vk = p[0];
	}
	
	@Override
	public void close()
	{
		VK10.vkDestroyDescriptorSetLayout(device.getVk(), vk, null);
		super.close();
	}
	
	@Override
	public void debug()
	{
		Debug.debug("DescriptorSetLayout: " + name);
	}
	
	@Override
	public void setName(String name)
	{
		device.setObjectDebugName(this, name);
		this.name = name;
	}
}
