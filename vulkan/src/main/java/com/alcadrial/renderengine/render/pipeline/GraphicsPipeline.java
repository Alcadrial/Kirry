package com.alcadrial.renderengine.render.pipeline;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.debug.Debug;
import com.alcadrial.renderengine.render.shader.Shader;
import com.alcadrial.renderengine.render.shader.ShaderInput;
import com.alcadrial.renderengine.render.shader.ShaderInputType;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.VulkanUtils;

public class GraphicsPipeline implements AutoCloseable {
	
	public static final class Builder {
		
		private Optional<Shader> vertexShader;
		private Optional<Shader> tessellationControlShader;
		private Optional<Shader> tessellationEvaluationShader;
		private Optional<Shader> geometryShader;
		private Optional<Shader> fragmentShader;
		private Optional<Shader> computeShader;
		
		private Builder()
		{
			vertexShader = Optional.empty();
			tessellationControlShader = Optional.empty();
			tessellationEvaluationShader = Optional.empty();
			geometryShader = Optional.empty();
			fragmentShader = Optional.empty();
			computeShader = Optional.empty();
		}
		
		/**
		 * Use {@link Shader#getType()} to distinguish the type
		 * @param shader
		 * @return
		 */
		public Builder shader(Shader shader)
		{
			Debug.printlnIf(shader.getOrCompile().hasErrors(), "Shader " + shader.getFile() + " cannot be used becouse of errors");
			switch (shader.getType())
			{
				case VERTEX -> vertexShader = shader.getOrCompile().hasErrors() ? Optional.empty() : Optional.ofNullable(shader);
				case TESS_CONTROL -> tessellationControlShader = shader.getOrCompile().hasErrors() ? Optional.empty() : Optional.ofNullable(shader);
				case TESS_EVALUATION -> tessellationEvaluationShader = shader.getOrCompile().hasErrors() ? Optional.empty() : Optional.ofNullable(shader);
				case GEOMETRY -> geometryShader = shader.getOrCompile().hasErrors() ? Optional.empty() : Optional.ofNullable(shader);
				case FRAGMENT -> fragmentShader = shader.getOrCompile().hasErrors() ? Optional.empty() : Optional.ofNullable(shader);
				case COMPUTE -> computeShader = shader.getOrCompile().hasErrors() ? Optional.empty() : Optional.ofNullable(shader);
				default -> Debug.println("Shader " + shader.getFile() + " cannot be used becouse the type is not known");
			}
			return this;
		}
		
		public Builder vertexShader(Shader shader)
		{
			Debug.printlnIf(shader.getOrCompile().hasErrors(), "Shader " + shader.getFile() + " cannot be used becouse of errors");
			vertexShader = shader.getOrCompile().hasErrors() ? Optional.empty() : Optional.ofNullable(shader);
			return this;
		}
		
		public Builder tessellationControlShader(Shader shader)
		{
			Debug.printlnIf(shader.getOrCompile().hasErrors(), "Shader " + shader.getFile() + " cannot be used becouse of errors");
			tessellationControlShader = shader.getOrCompile().hasErrors() ? Optional.empty() : Optional.ofNullable(shader);
			return this;
		}
		
		public Builder tessellationEvaluationShader(Shader shader)
		{
			Debug.printlnIf(shader.getOrCompile().hasErrors(), "Shader " + shader.getFile() + " cannot be used becouse of errors");
			tessellationEvaluationShader = shader.getOrCompile().hasErrors() ? Optional.empty() : Optional.ofNullable(shader);
			return this;
		}
		
		public Builder geometryShader(Shader shader)
		{
			Debug.printlnIf(shader.getOrCompile().hasErrors(), "Shader " + shader.getFile() + " cannot be used becouse of errors");
			geometryShader = shader.getOrCompile().hasErrors() ? Optional.empty() : Optional.ofNullable(shader);
			return this;
		}
		
		public Builder fragmentShader(Shader shader)
		{
			Debug.printlnIf(shader.getOrCompile().hasErrors(), "Shader " + shader.getFile() + " cannot be used becouse of errors");
			fragmentShader = shader.getOrCompile().hasErrors() ? Optional.empty() : Optional.ofNullable(shader);
			return this;
		}
		
		public Builder computeShader(Shader shader)
		{
			Debug.printlnIf(shader.getOrCompile().hasErrors(), "Shader " + shader.getFile() + " cannot be used becouse of errors");
			computeShader = shader.getOrCompile().hasErrors() ? Optional.empty() : Optional.ofNullable(shader);
			return this;
		}
		
		public GraphicsPipeline build(Device device, PipelineLayout layout, VkGraphicsPipelineCreateInfo.Buffer createInfo)
		{
			return new GraphicsPipeline(device, layout, createInfo, vertexShader, tessellationControlShader, tessellationEvaluationShader, geometryShader, fragmentShader, computeShader);
		}
	}
	
	public static Builder create()
	{
		return new Builder();
	}
	
	private Device device;
	private PipelineLayout layout;
	private long pipeline;
	
	private Optional<Shader> vertexShader;
	private Optional<Shader> tessellationControlShader;
	private Optional<Shader> tessellationEvaluationShader;
	private Optional<Shader> geometryShader;
	private Optional<Shader> fragmentShader;
	private Optional<Shader> computeShader;
	
	private List<ShaderInput> inputs;
	
	private GraphicsPipeline(Device device, PipelineLayout layout, VkGraphicsPipelineCreateInfo.Buffer createInfo, Optional<Shader> vertexShader, Optional<Shader> tessellationControlShader, Optional<Shader> tessellationEvaluationShader, Optional<Shader> geometryShader, Optional<Shader> fragmentShader, Optional<Shader> computeShader)
	{
		this.device = device;
		this.layout = layout;
		this.vertexShader = vertexShader;
		this.tessellationControlShader = tessellationControlShader;
		this.tessellationEvaluationShader = tessellationEvaluationShader;
		this.geometryShader = geometryShader;
		this.fragmentShader = fragmentShader;
		this.computeShader = computeShader;
		inputs = List.of();
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			if (vertexShader.isPresent())
			{
				VkPipelineVertexInputStateCreateInfo vertexInputInfo = VkPipelineVertexInputStateCreateInfo.calloc(stack);
				vertexInputInfo.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
				
				VkVertexInputBindingDescription.Buffer bindingDescription = VkVertexInputBindingDescription.calloc(1, stack);
				
				int size = 0;
				int locationSize = 0;
				Set<ShaderInput> inputs = vertexShader.get().getInputs();
				VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.calloc(inputs.size());
				for (ShaderInput input : inputs)
				{
					ShaderInputType type = input.getType();
					VkVertexInputAttributeDescription description = attributeDescriptions.get();
					description.binding(0);
					description.location(locationSize);
					description.format(type.getType());
					description.offset(size);
					size += type.getSize() * 4;
					locationSize += type.getLocationSize();
				}
				
				bindingDescription.binding(0);
				bindingDescription.stride(size);
				bindingDescription.inputRate(VK10.VK_VERTEX_INPUT_RATE_VERTEX);
				vertexInputInfo.pVertexBindingDescriptions(bindingDescription);
				vertexInputInfo.pVertexAttributeDescriptions(attributeDescriptions.rewind());
				createInfo.pVertexInputState(vertexInputInfo);
				
				this.inputs = List.copyOf(inputs);
			}
			
			int stages = 0;
			if (vertexShader.isPresent()) stages++;
			if (tessellationControlShader.isPresent()) stages++;
			if (tessellationEvaluationShader.isPresent()) stages++;
			if (geometryShader.isPresent()) stages++;
			if (fragmentShader.isPresent()) stages++;
			if (computeShader.isPresent()) stages++;
			
			VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.calloc(stages, stack);
			
			vertexShader.ifPresent(shader -> fillShaderStageCreateInfo(stack, shaderStages.get(), shader));
			tessellationControlShader.ifPresent(shader -> fillShaderStageCreateInfo(stack, shaderStages.get(), shader));
			tessellationEvaluationShader.ifPresent(shader -> fillShaderStageCreateInfo(stack, shaderStages.get(), shader));
			geometryShader.ifPresent(shader -> fillShaderStageCreateInfo(stack, shaderStages.get(), shader));
			fragmentShader.ifPresent(shader -> fillShaderStageCreateInfo(stack, shaderStages.get(), shader));
			computeShader.ifPresent(shader -> fillShaderStageCreateInfo(stack, shaderStages.get(), shader));
			
			createInfo.pStages(shaderStages.rewind());
			createInfo.layout(layout.getPipelineLayout());
			
			long[] p = new long[1];
			VulkanErrorUtils.assertNoError(VK10.vkCreateGraphicsPipelines(device.getDevice(), 0, createInfo, null, p), "Failed to create graphics pipeline");
			pipeline = p[0];
			
			for (VkPipelineShaderStageCreateInfo shaderStageCreateInfo : shaderStages)
			{
				VK10.vkDestroyShaderModule(device.getDevice(), shaderStageCreateInfo.module(), null);
			}
		}
	}
	
	private void fillShaderStageCreateInfo(MemoryStack stack, VkPipelineShaderStageCreateInfo shaderStageInfo, Shader shader)
	{
		shaderStageInfo.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
		shaderStageInfo.stage(shader.getType().getStageBit());
		
		VkShaderModuleCreateInfo createInfo = VkShaderModuleCreateInfo.calloc(stack);
		
		createInfo.sType(VK10.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO);
		createInfo.pCode(VulkanUtils.allocate(stack, shader.getOrCompile().getBinary()));
		
		long[] p = new long[1];
		VulkanErrorUtils.assertNoError(VK10.vkCreateShaderModule(device.getDevice(), createInfo, null, p), "Failed to create shader module");
		
		shaderStageInfo.module(p[0]);
		shaderStageInfo.pName(stack.UTF8(shader.getEntryPoint()));
	}
	
	public PipelineLayout getLayout()
	{
		return layout;
	}
	
	public long getPipeline()
	{
		return pipeline;
	}
	
	public List<ShaderInput> getInputs()
	{
		return inputs;
	}
	
	public Optional<Shader> getVertexShader()
	{
		return vertexShader;
	}
	
	public Optional<Shader> getTessellationControlShader()
	{
		return tessellationControlShader;
	}
	
	public Optional<Shader> getTessellationEvaluationShader()
	{
		return tessellationEvaluationShader;
	}
	
	public Optional<Shader> getGeometryShader()
	{
		return geometryShader;
	}
	
	public Optional<Shader> getFragmentShader()
	{
		return fragmentShader;
	}
	
	public Optional<Shader> getComputeShader()
	{
		return computeShader;
	}
	
	@Override
	public void close()
	{
		layout.close();
		VK10.vkDestroyPipeline(device.getDevice(), pipeline, null);
	}
}
