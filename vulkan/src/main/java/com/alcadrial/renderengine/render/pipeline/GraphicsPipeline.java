package com.alcadrial.renderengine.render.pipeline;

import java.util.List;
import java.util.Optional;

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
import com.alcadrial.renderengine.render.RenderPass;
import com.alcadrial.renderengine.render.shader.PreparedShader;
import com.alcadrial.renderengine.render.shader.Shader;
import com.alcadrial.renderengine.render.shader.ShaderInfo;
import com.alcadrial.renderengine.render.shader.ShaderInputType;
import com.alcadrial.renderengine.render.shader.ShaderType;
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.util.DataAccumulator;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;

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
		
		public Builder shader(@Nullable Shader shader, @Nullable ShaderInfo info, ShaderType type)
		{
			return super.genericShader(shader, info, type);
		}
		
		@Override
		public Builder vertexShader(@Nullable Shader shader, @Nullable ShaderInfo info)
		{
			return super.vertexShader(shader, info);
		}
		
		@Override
		public Builder tessellationControlShader(@Nullable Shader shader, @Nullable ShaderInfo info)
		{
			return super.tessellationControlShader(shader, info);
		}
		
		@Override
		public Builder tessellationEvaluationShader(@Nullable Shader shader, @Nullable ShaderInfo info)
		{
			return super.tessellationEvaluationShader(shader, info);
		}
		
		@Override
		public Builder geometryShader(@Nullable Shader shader, @Nullable ShaderInfo info)
		{
			return super.geometryShader(shader, info);
		}
		
		@Override
		public Builder fragmentShader(@Nullable Shader shader, @Nullable ShaderInfo info)
		{
			return super.fragmentShader(shader, info);
		}
		
		public GraphicsPipeline build(Device device, PipelineLayout layout, RenderPass renderPass, VkGraphicsPipelineCreateInfo.Buffer createInfo)
		{
			try
			{
				return new GraphicsPipeline(device, layout, autoCloseLayout.orElse(true), renderPass, autoCloseRenderPass.orElse(true), createInfo, vertexShader.require("The vertex shader is required"), tessellationControlShader.asOptional(), tessellationEvaluationShader.asOptional(), geometryShader.asOptional(), fragmentShader.asOptional());
			}
			finally
			{
				close();
			}
		}
		
		public <GP extends GraphicsPipeline> GP build(GraphicsPipelineBuildFunction<GP> buildFunction, Device device, PipelineLayout layout, RenderPass renderPass, VkGraphicsPipelineCreateInfo.Buffer createInfo)
		{
			return buildFunction.build(device, layout, autoCloseLayout.orElse(true), renderPass, autoCloseRenderPass.orElse(true), createInfo, vertexShader.require("The vertex shader is required"), tessellationControlShader.asOptional(), tessellationEvaluationShader.asOptional(), geometryShader.asOptional(), fragmentShader.asOptional());
		}
	}
	
	@FunctionalInterface
	public interface GraphicsPipelineBuildFunction<GP extends GraphicsPipeline> {
		
		GP build(Device device, PipelineLayout layout, boolean autoCloseLayout, RenderPass renderPass, boolean autoCloseRenderPass, VkGraphicsPipelineCreateInfo.Buffer createInfo, PreparedShader vertexShader, Optional<PreparedShader> tessellationControlShader, Optional<PreparedShader> tessellationEvaluationShader, Optional<PreparedShader> geometryShader, Optional<PreparedShader> fragmentShader);
	}
	
	public static Builder create()
	{
		return new Builder();
	}
	
	protected boolean autoCloseLayout;
	protected RenderPass renderPass;
	protected boolean autoCloseRenderPass;
	
	protected PreparedShader vertexShader;
	protected Optional<PreparedShader> tessellationControlShader;
	protected Optional<PreparedShader> tessellationEvaluationShader;
	protected Optional<PreparedShader> geometryShader;
	protected Optional<PreparedShader> fragmentShader;
	
	protected List<ShaderVariable> inputs;
	
	public GraphicsPipeline(Device device, PipelineLayout layout, boolean autoCloseLayout, RenderPass renderPass, boolean autoCloseRenderPass, VkGraphicsPipelineCreateInfo.Buffer createInfo, PreparedShader vertexShader, Optional<PreparedShader> tessellationControlShader, Optional<PreparedShader> tessellationEvaluationShader, Optional<PreparedShader> geometryShader, Optional<PreparedShader> fragmentShader)
	{
		super(device, layout);
		this.autoCloseLayout = autoCloseLayout;
		this.renderPass = renderPass;
		this.autoCloseRenderPass = autoCloseRenderPass;
		this.vertexShader = vertexShader;
		this.tessellationControlShader = tessellationControlShader;
		this.tessellationEvaluationShader = tessellationEvaluationShader;
		this.geometryShader = geometryShader;
		this.fragmentShader = fragmentShader;
		inputs = List.of();
		
		createVk(createInfo);
	}
	
	protected void createVk(VkGraphicsPipelineCreateInfo.Buffer createInfo)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkPipelineVertexInputStateCreateInfo vertexInputInfo = VkPipelineVertexInputStateCreateInfo.calloc(stack);
			vertexInputInfo.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
			
			inputs = extractInputs(vertexShader);
			
			int size = 0;
			int locationSize = 0;
			VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.calloc(inputs.size());
			for (ShaderVariable input : inputs)
			{
				ShaderInputType type = input.getType();
				attributeDescriptions.get().set(locationSize, 0, type.getType(), size);
				size += type.getLength();
				locationSize += type.getLocationSize();
			}
			
			VkVertexInputBindingDescription.Buffer bindingDescription = VkVertexInputBindingDescription.calloc(1, stack);
			bindingDescription.binding(0);
			bindingDescription.stride(size);
			bindingDescription.inputRate(VK10.VK_VERTEX_INPUT_RATE_VERTEX);
			vertexInputInfo.pVertexBindingDescriptions(bindingDescription);
			vertexInputInfo.pVertexAttributeDescriptions(attributeDescriptions.rewind());
			
			int stages = 1;
			if (tessellationControlShader.isPresent()) stages++;
			if (tessellationEvaluationShader.isPresent()) stages++;
			if (geometryShader.isPresent()) stages++;
			if (fragmentShader.isPresent()) stages++;
			
			VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.calloc(stages, stack);
			
			fillShaderStageCreateInfo(stack, shaderStages.get(), vertexShader);
			tessellationControlShader.ifPresent(shader -> fillShaderStageCreateInfo(stack, shaderStages.get(), shader));
			tessellationEvaluationShader.ifPresent(shader -> fillShaderStageCreateInfo(stack, shaderStages.get(), shader));
			geometryShader.ifPresent(shader -> fillShaderStageCreateInfo(stack, shaderStages.get(), shader));
			fragmentShader.ifPresent(shader -> fillShaderStageCreateInfo(stack, shaderStages.get(), shader));
			
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
	
	public PreparedShader getVertexShader()
	{
		return vertexShader;
	}
	
	public Optional<PreparedShader> getTessellationControlShader()
	{
		return tessellationControlShader;
	}
	
	public Optional<PreparedShader> getTessellationEvaluationShader()
	{
		return tessellationEvaluationShader;
	}
	
	public Optional<PreparedShader> getGeometryShader()
	{
		return geometryShader;
	}
	
	public Optional<PreparedShader> getFragmentShader()
	{
		return fragmentShader;
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
		Debug.print("Inputs: " + inputs);
		Debug.print("VertexShader: " + vertexShader.shader().getFile());
		tessellationControlShader.ifPresent(s -> Debug.print("TessellationControlShader: " + s.shader().getFile()));
		tessellationEvaluationShader.ifPresent(s -> Debug.print("TessellationEvaluationShader: " + s.shader().getFile()));
		geometryShader.ifPresent(s -> Debug.print("GeometryShader: " + s.shader().getFile()));
		fragmentShader.ifPresent(s -> Debug.print("FragmentShader: " + s.shader().getFile()));
		
	}
}
