package com.alcadrial.renderengine.render.pipeline;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import com.alcadrial.renderengine.core.util.NotYetImplementedException;
import com.alcadrial.renderengine.core.util.Nullable;
import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.renderpass.RenderPass;
import com.alcadrial.renderengine.render.shader.PreparedShader;
import com.alcadrial.renderengine.render.shader.ShaderInputType;
import com.alcadrial.renderengine.render.shader.ShaderProgram;
import com.alcadrial.renderengine.render.shader.ShaderType;
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.util.DataAccumulator;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

/**
 * A {@link Pipeline} used for rendering.
 * 
 * @author Alcadrial
 */
public class GraphicsPipeline extends Pipeline {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public static final class Builder extends DataAccumulator<Builder> {
		
		@Override
		public Builder autoCloseLayout(@Nullable Boolean value)
		{
			return super.autoCloseLayout(value);
		}
		
		@Override
		public Builder autoCloseRenderPass(@Nullable Boolean value)
		{
			return super.autoCloseRenderPass(value);
		}
		
		@Override
		public Builder shaderProgram(@Nullable ShaderProgram value)
		{
			return super.shaderProgram(value);
		}
		
		public GraphicsPipeline build(Device device, PipelineLayout layout, RenderPass renderPass, VkGraphicsPipelineCreateInfo.Buffer createInfo) throws VulkanException
		{
			try
			{
				return new GraphicsPipeline(device, layout, autoCloseLayout.orElse(true), renderPass, autoCloseRenderPass.orElse(true), createInfo, shaderProgram.require());
			}
			finally
			{
				close();
			}
		}
		
		public <GP extends GraphicsPipeline> GP build(GraphicsPipelineBuildFunction<GP> buildFunction, Device device, PipelineLayout layout, RenderPass renderPass, VkGraphicsPipelineCreateInfo.Buffer createInfo) throws VulkanException
		{
			GP graphicsPipeline = buildFunction.build(device, layout, autoCloseLayout.orElse(true), renderPass, autoCloseRenderPass.orElse(true), createInfo, shaderProgram.require());
			close();
			return graphicsPipeline;
		}
	}
	
	@FunctionalInterface
	public interface GraphicsPipelineBuildFunction<GP extends GraphicsPipeline> {
		
		GP build(Device device, PipelineLayout layout, boolean autoCloseLayout, RenderPass renderPass, boolean autoCloseRenderPass, VkGraphicsPipelineCreateInfo.Buffer createInfo, ShaderProgram program) throws VulkanException;
	}
	
	public static Builder create()
	{
		return new Builder();
	}
	
	protected boolean autoCloseLayout;
	protected RenderPass renderPass;
	protected boolean autoCloseRenderPass;
	
	protected ShaderProgram shaderProgram;
	
	protected List<ShaderVariable> inputs;
	
	public GraphicsPipeline(Device device, PipelineLayout layout, boolean autoCloseLayout, RenderPass renderPass, boolean autoCloseRenderPass, VkGraphicsPipelineCreateInfo.Buffer createInfo, ShaderProgram program) throws VulkanException
	{
		super(device, layout);
		this.autoCloseLayout = autoCloseLayout;
		this.renderPass = renderPass;
		this.autoCloseRenderPass = autoCloseRenderPass;
		shaderProgram = program;
		inputs = List.of();
		
		createVk(createInfo);
	}
	
	protected void createVk(VkGraphicsPipelineCreateInfo.Buffer createInfo) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkPipelineVertexInputStateCreateInfo vertexInputInfo = VkPipelineVertexInputStateCreateInfo.calloc(stack);
			vertexInputInfo.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
			
			inputs = shaderProgram.getshaders().stream().filter(ps -> ps.info().getType() == ShaderType.VERTEX).map(this::extractInputs).collect(LinkedList::new, List::addAll, List::addAll);
			
			int size = 0;
			int locationSize = 0;
			VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.calloc(inputs.size());
			for (ShaderVariable input : inputs)
			{
				ShaderInputType type = input.getType();
				attributeDescriptions.get().set(locationSize, 0, type.getVulkanType(), size);
				size += type.getLength();
				locationSize += type.getLocationSize();
			}
			
			VkVertexInputBindingDescription.Buffer bindingDescription = VkVertexInputBindingDescription.calloc(1, stack);
			bindingDescription.binding(0);
			bindingDescription.stride(size);
			bindingDescription.inputRate(VK10.VK_VERTEX_INPUT_RATE_VERTEX);
			vertexInputInfo.pVertexBindingDescriptions(bindingDescription);
			vertexInputInfo.pVertexAttributeDescriptions(attributeDescriptions.rewind());
			
			Map<ShaderType, List<PreparedShader>> shaderStagesMap = shaderProgram.getshaders().stream().collect(Collectors.groupingBy(ps -> ps.info().getType()));
			
			VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.calloc(shaderStagesMap.size(), stack);
			try
			{
				for (Entry<ShaderType, List<PreparedShader>> e : shaderStagesMap.entrySet())
				{
					VkPipelineShaderStageCreateInfo stage = shaderStages.get();
					for (PreparedShader shader : e.getValue()) fillShaderStageCreateInfo(stack, stage, shader);
				}
			}
			catch (OutOfMemoryError e)
			{
				throw e;
			}
			
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
	
	public void recreate()
	{
		throw new NotYetImplementedException();
	}
	
	@Override
	public PipelineLayout getLayout()
	{
		return layout;
	}
	
	public RenderPass getRenderPass()
	{
		return renderPass;
	}
	
	@Override
	public int bindPoint()
	{
		return VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;
	}
	
	public List<ShaderVariable> getInputs()
	{
		return inputs;
	}
	
	public ShaderProgram getShaderProgram()
	{
		return shaderProgram;
	}
	
	@Override
	public void close()
	{
		if (autoCloseLayout) layout.close();
		if (autoCloseRenderPass) renderPass.close();
		super.close();
	}
	
	@Override
	public void debug()
	{
		Debug.print("Vk: " + Debug.address(vk));
		Debug.print("Layout", layout);
		Debug.print("RenderPass", renderPass);
		Debug.print("Inputs", inputs);
		Debug.print("Shaders {");
		Debug.indent();
		for (Entry<ShaderType, List<PreparedShader>> e : shaderProgram.getshaders().stream().collect(Collectors.groupingBy(ps -> ps.info().getType())).entrySet())
		{
			Debug.print(e.getKey() + " {");
			Debug.indent();
			for (PreparedShader prepared : e.getValue())
			{
				File file = prepared.shader().getFile();
				if (file == null) Debug.print("Internal Shader");
				else Debug.print(prepared.shader().getFile());
			}
			Debug.deindent();
			Debug.print("}");
		}
		Debug.deindent();
		Debug.print("}");
	}
}
