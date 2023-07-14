package com.alcadrial.renderengine.render.pipeline;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.shader.CompiledShader;
import com.alcadrial.renderengine.render.shader.Shader;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;

public class GraphicsPipeline implements AutoCloseable {
	
	private Device device;
	private PipelineLayout layout;
	private long pipeline;
	
	private Shader vertexShader;
	private Shader fragmentShader;
	
	public GraphicsPipeline(Device device, PipelineLayout layout, VkGraphicsPipelineCreateInfo.Buffer createInfo, Shader vertexShader, Shader fragmentShader)
	{
		this.device = device;
		this.layout = layout;
		this.vertexShader = vertexShader;
		this.fragmentShader = fragmentShader;
		
		long[] p = new long[1];
		
		CompiledShader vertexCompiled = vertexShader.getOrCompile();
		CompiledShader fragmentCompiled = fragmentShader.getOrCompile();
		
		if (vertexCompiled.hasErrors()) throw new IllegalStateException("Shader " + vertexShader.getFile() + " cannot be used becouse of errors");
		if (fragmentCompiled.hasErrors()) throw new IllegalStateException("Shader " + fragmentShader.getFile() + " cannot be used becouse of errors");
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			long vertShaderModule = createShaderModule(stack, vertexCompiled.getBinary());
			long fragShaderModule = createShaderModule(stack, fragmentCompiled.getBinary());
			
			VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.calloc(2, stack);
			
			VkPipelineShaderStageCreateInfo vertShaderStageInfo = shaderStages.get(0);
			vertShaderStageInfo.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
			vertShaderStageInfo.stage(VK10.VK_SHADER_STAGE_VERTEX_BIT);
			vertShaderStageInfo.module(vertShaderModule);
			vertShaderStageInfo.pName(stack.UTF8(vertexShader.getEntryPoint()));
			
			VkPipelineShaderStageCreateInfo fragShaderStageInfo = shaderStages.get(1);
			fragShaderStageInfo.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
			fragShaderStageInfo.stage(VK10.VK_SHADER_STAGE_FRAGMENT_BIT);
			fragShaderStageInfo.module(fragShaderModule);
			fragShaderStageInfo.pName(stack.UTF8(fragmentShader.getEntryPoint()));
			
			createInfo.pStages(shaderStages);
			createInfo.layout(layout.getPipelineLayout());
			VulkanErrorUtils.assertNoError(VK10.vkCreateGraphicsPipelines(device.getDevice(), 0, createInfo, null, p), "Failed to create graphics pipeline");
			
			VK10.vkDestroyShaderModule(device.getDevice(), vertShaderModule, null);
			VK10.vkDestroyShaderModule(device.getDevice(), fragShaderModule, null);
		}
		pipeline = p[0];
	}
	
	private long createShaderModule(MemoryStack stack, byte[] binary)
	{
		VkShaderModuleCreateInfo createInfo = VkShaderModuleCreateInfo.calloc(stack);
		
		createInfo.sType(VK10.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO);
		ByteBuffer bb = stack.malloc(binary.length);
		for (byte b : binary) bb.put(b);
		createInfo.pCode(bb.rewind());
		
		long[] p = new long[1];
		VulkanErrorUtils.assertNoError(VK10.vkCreateShaderModule(device.getDevice(), createInfo, null, p), "Failed to create shader module");
		
		return p[0];
	}
	
	public PipelineLayout getLayout()
	{
		return layout;
	}
	
	public long getPipeline()
	{
		return pipeline;
	}
	
	public Shader getVertexShader()
	{
		return vertexShader;
	}
	
	public Shader getFragmentShader()
	{
		return fragmentShader;
	}
	
	@Override
	public void close()
	{
		layout.close();
		VK10.vkDestroyPipeline(device.getDevice(), pipeline, null);
	}
}
