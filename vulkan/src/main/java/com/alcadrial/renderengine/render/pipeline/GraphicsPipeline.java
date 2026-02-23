package com.alcadrial.renderengine.render.pipeline;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public class GraphicsPipeline extends Pipeline {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private ImmutableSet<Integer> activeBindings;
	
	public GraphicsPipeline(Device device, PipelineLayout layout, VkGraphicsPipelineCreateInfo createInfo) throws VulkanException
	{
		super(device, layout);

		long[] p = new long[1];
		VulkanErrorUtils.assertNoError(VK10.vkCreateGraphicsPipelines(device.getVk(), 0, VkGraphicsPipelineCreateInfo.create(createInfo.address(), 1), null, p), "Failed to create graphics pipeline");
		vk = p[0];
		
		MutableSet<Integer> bindings = Sets.mutable.empty();
		VkPipelineVertexInputStateCreateInfo pVertexInputState = createInfo.pVertexInputState();
		if (pVertexInputState != null)
		{
			VkVertexInputBindingDescription.Buffer pVertexBindingDescriptions = pVertexInputState.pVertexBindingDescriptions();
			if (pVertexBindingDescriptions != null)
			{
				for (VkVertexInputBindingDescription vertexInputBindingDescription : pVertexBindingDescriptions) bindings.add(vertexInputBindingDescription.binding());
			}
		}
		activeBindings = bindings.toImmutable();
	}
	
	@Override
	public int bindPoint()
	{
		return VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;
	}
	
	public ImmutableSet<Integer> getActiveBindings()
	{
		return activeBindings;
	}
	
	@Override
	public void debug()
	{
		Debug.print("Vk: " + Debug.address(vk));
		Debug.print("Layout", layout);
	}
}
