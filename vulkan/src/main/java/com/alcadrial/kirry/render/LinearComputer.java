package com.alcadrial.kirry.render;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2dr;
import com.alcadrial.kector.vector.Vector2fr;
import com.alcadrial.kector.vector.Vector3dr;
import com.alcadrial.kector.vector.Vector3fr;
import com.alcadrial.kector.vector.Vector3ir;
import com.alcadrial.kector.vector.Vector4dr;
import com.alcadrial.kector.vector.Vector4fr;
import com.alcadrial.kirry.render.buffer.BufferLayoutWriter;
import com.alcadrial.kirry.render.buffer.VulkanBuffer;
import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;
import com.alcadrial.kirry.render.pipeline.ComputePipeline;
import com.alcadrial.kirry.render.pipeline.PipelineLayout;
import com.alcadrial.kirry.render.shader.PreparedShader;
import com.alcadrial.kirry.render.shader.ShaderInputType;
import com.alcadrial.kirry.render.shader.ShaderVariable;
import com.alcadrial.kirry.render.shader.binding.BindingInfo;
import com.alcadrial.kirry.render.shader.descriptor.DescriptorPool;
import com.alcadrial.kirry.render.shader.descriptor.DescriptorSet;
import com.alcadrial.kirry.render.shader.descriptor.DescriptorSetLayout;
import com.alcadrial.kirry.render.util.VulkanErrorUtils;
import com.alcadrial.kirry.render.util.exception.VulkanException;

public class LinearComputer implements AutoCloseable {
	
	public static final String COMPUTE2X2_SHADER_NAME = "linearShader2x2.comp";
	public static final String COMPUTE2X2_SHADER_PATHNAME = KirryVulkan.SHADERS_PATH + COMPUTE2X2_SHADER_NAME;
	public static final String COMPUTE3X3_SHADER_NAME = "linearShader3x3.comp";
	public static final String COMPUTE3X3_SHADER_PATHNAME = KirryVulkan.SHADERS_PATH + COMPUTE3X3_SHADER_NAME;
	public static final String COMPUTE4X4_SHADER_NAME = "linearShader4x4.comp";
	public static final String COMPUTE4X4_SHADER_PATHNAME = KirryVulkan.SHADERS_PATH + COMPUTE4X4_SHADER_NAME;
	
	public static LinearComputer ofCopy(Device device, PreparedShader shader, @Nullable VulkanBuffer uniformBuffer, ShaderVariable matrixVariable, @Nullable VulkanBuffer inputBuffer, ShaderVariable inputVariable, @Nullable VulkanBuffer outputBuffer, ShaderVariable outputVariable) throws VulkanException
	{
		if (!matrixVariable.getType().isMatrix()) throw new IllegalArgumentException("LinearComputer for copy must work with matrixes");
		if (!inputVariable.getType().isFloatVector()) throw new IllegalArgumentException("LinearComputer for copy must work with floating point vectors");
		if (!outputVariable.getType().isFloatVector()) throw new IllegalArgumentException("LinearComputer for copy must work with floating point vectors");
		if (matrixVariable.getType().getSize() / inputVariable.getType().getSize() != outputVariable.getType().getSize()) throw new IllegalArgumentException("LinearComputer matrix and vectors are not compatible");
		
		return new LinearComputer(device, shader, uniformBuffer, matrixVariable, inputBuffer, inputVariable, outputBuffer, outputVariable);
	}
	
	public static LinearComputer ofUpdate(Device device, PreparedShader shader, @Nullable VulkanBuffer uniformBuffer, ShaderVariable matrixVariable, @Nullable VulkanBuffer inputBuffer, ShaderVariable inputVariable) throws VulkanException
	{
		if (!matrixVariable.getType().isSquareMatrix()) throw new IllegalArgumentException("LinearComputer for update must work with square matrixes");
		if (!inputVariable.getType().isFloatVector()) throw new IllegalArgumentException("LinearComputer for update must work with floating point vectors");
		if (matrixVariable.getType().getSize() / inputVariable.getType().getSize() != inputVariable.getType().getSize()) throw new IllegalArgumentException("LinearComputer matrix and vectors are not compatible");
		
		return new LinearComputer(device, shader, uniformBuffer, matrixVariable, inputBuffer, inputVariable, null, null);
	}
	
	public static LinearComputer ofComputer(LinearComputer linearComputer) throws VulkanException
	{
		return new LinearComputer(linearComputer);
	}
	
	private PreparedShader shader;
	private @Nullable VulkanBuffer uniformBuffer;
	private ShaderVariable matrixVariable;
	private @Nullable VulkanBuffer inputBuffer;
	private ShaderVariable inputVariable;
	private @Nullable VulkanBuffer outputBuffer;
	private @Nullable ShaderVariable outputVariable;
	
	private Computer computer;
	
	private LinearComputer(Device device, PreparedShader shader, @Nullable VulkanBuffer uniformBuffer, ShaderVariable matrixVariable, @Nullable VulkanBuffer inputBuffer, ShaderVariable inputVariable, @Nullable VulkanBuffer outputBuffer, @Nullable ShaderVariable outputVariable) throws VulkanException
	{
		this.shader = shader;
		this.uniformBuffer = uniformBuffer;
		this.matrixVariable = matrixVariable;
		this.inputBuffer = inputBuffer;
		this.inputVariable = inputVariable;
		this.outputBuffer = outputBuffer;
		this.outputVariable = outputVariable;
		computer = preInit(device);
	}
	
	private LinearComputer(LinearComputer other) throws VulkanException
	{
		shader = other.shader;
		uniformBuffer = other.uniformBuffer;
		matrixVariable = other.matrixVariable;
		inputBuffer = other.inputBuffer;
		inputVariable = other.inputVariable;
		outputBuffer = other.outputBuffer;
		outputVariable = other.outputVariable;
		computer = other.computer;
	}
	
	private Computer preInit(Device device) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			int maxSets = 1;
			DescriptorPool descriptorPool = new DescriptorPool(device, VkDescriptorPoolCreateInfo.malloc(stack)
				.sType(VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
				.pNext(0)
				.flags(0)
				.maxSets(maxSets)
				.pPoolSizes(VkDescriptorPoolSize.malloc(2, stack)
					.apply(b -> b
						.type(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
						.descriptorCount(1 * maxSets)
					)
					.apply(b -> b
						.type(VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER)
						.descriptorCount(1 * maxSets)
					)
					.rewind()
				));
			
			VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack);
			
			DescriptorSetLayout computeDescriptorSetLayout = new DescriptorSetLayout(device, VkDescriptorSetLayoutCreateInfo.malloc(stack)
				.sType(VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
				.pNext(0)
				.flags(0)
				.pBindings(VkDescriptorSetLayoutBinding.malloc(2, stack)
					.apply(b -> b
						.binding(0)
						.descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
						.descriptorCount(1)
						.stageFlags(VK10.VK_SHADER_STAGE_COMPUTE_BIT)
						.pImmutableSamplers(null))
					.apply(b -> b
						.binding(1)
						.descriptorType(VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER)
						.descriptorCount(1)
						.stageFlags(VK10.VK_SHADER_STAGE_COMPUTE_BIT)
						.pImmutableSamplers(null))
					.rewind()
				));
			DescriptorSet[] computingDescriptorSets = descriptorPool.allocates(computeDescriptorSetLayout, 1);
			pipelineLayoutInfo.pSetLayouts(stack.longs(computeDescriptorSetLayout.getVk()));
			PipelineLayout computeLayout = new PipelineLayout(device, pipelineLayoutInfo);
			
			ComputePipeline computePipeline = new ComputePipeline(device, computeLayout, shader);
			Debug.runOnDebug(() -> {
				computePipeline.setName("Linear Compute Pipeline");
			});
			return Computer.builder()
				.computePipeline(computePipeline)
				.descriptorSets(computingDescriptorSets)
				.groupCount(Vector3ir.of(256, 1, 1))
				.buffersCount(1)
				.build();
		}
	}
	
	public void init() throws VulkanException
	{
		computer.loadDescriptors(outputVariable != null ? new BindingInfo[] {
			BindingInfo.ofUniform(uniformBuffer, VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, List.of(
				matrixVariable,
				ShaderVariable.OFFSET,
				ShaderVariable.STRIDE,
				ShaderVariable.SIZE
			)),
			BindingInfo.ofStorage(inputBuffer, VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, List.of(
				inputVariable
			)),
			BindingInfo.ofStorage(outputBuffer, VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, List.of(
				cast(outputVariable)
			))
		} : new BindingInfo[] {
			BindingInfo.ofUniform(uniformBuffer, VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, List.of(
				matrixVariable,
				ShaderVariable.OFFSET,
				ShaderVariable.STRIDE,
				ShaderVariable.SIZE
			)),
			BindingInfo.ofStorage(inputBuffer, VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, List.of(
				inputVariable
			))
		});
		computer.updateDescriptorSet(0);
	}
	
	@SuppressWarnings("unchecked")
	private <T, X> X cast(T t)
	{
		return (X) t;
	}
	
	public Computer getComputer()
	{
		return computer;
	}
	
	public void dispatch(CommandBuffer commandBuffer, int count)
	{
		getComputer().dispatch(commandBuffer, 0, count, 1, 1);
	}
	
	public void setUniformBuffer(VulkanBuffer uniformBuffer) throws VulkanException
	{
		getComputer().updateDescriptor(0, 0, uniformBuffer);
	}
	
	public void setInputBuffer(VulkanBuffer dataBuffer) throws VulkanException
	{
		getComputer().updateDescriptor(0, 1, dataBuffer);
	}
	
	public void setOutputBuffer(VulkanBuffer dataBuffer) throws VulkanException
	{
		if (outputVariable == null) throw new IllegalStateException("Cannot specify an output becouse it is forced to be equal to the input");
		getComputer().updateDescriptor(0, 2, dataBuffer);
	}
	
	public void setOffset(int offset) throws VulkanException
	{
		getComputer().getBufferLayoutWriter(0, 0).set(ShaderVariable.OFFSET, offset);
	}
	
	public void setStride(int stride) throws VulkanException
	{
		getComputer().getBufferLayoutWriter(0, 0).set(ShaderVariable.STRIDE, stride);
	}
	
	public void setSize(int size) throws VulkanException
	{
		getComputer().getBufferLayoutWriter(0, 0).set(ShaderVariable.SIZE, size);
	}
	
	public void setMatrix(Matrix4f matrix) throws VulkanException
	{
		getComputer().getBufferLayoutWriter(0, 0).set(matrixVariable, matrix);
	}
	
	public void setMatrix(float m00, float m01, float m10, float m11) throws VulkanException
	{
		if (matrixVariable.getType() != ShaderInputType.MATRIX_FLOAT_2X2) throw new IllegalArgumentException("Provided a mat2 value for a " + matrixVariable.getName());
		BufferLayoutWriter layoutWriter = getComputer().getBufferLayoutWriter(0, 0);
		Integer offset = VulkanErrorUtils.nonNull(layoutWriter.getLayout().getOffset(matrixVariable));
		layoutWriter.getWriter().setCursor(offset).put(m00, m01, m10, m11);
	}
	
	public void setSparseMatrix(Vector2fr matrix) throws VulkanException
	{
		getComputer().getBufferLayoutWriter(0, 0).set(matrixVariable, matrix);
	}
	
	public void setSparseMatrix(Vector3fr matrix) throws VulkanException
	{
		getComputer().getBufferLayoutWriter(0, 0).set(matrixVariable, matrix);
	}
	
	public void setSparseMatrix(Vector4fr matrix) throws VulkanException
	{
		getComputer().getBufferLayoutWriter(0, 0).set(matrixVariable, matrix);
	}
	
	public void setSparseMatrix(Vector2dr matrix) throws VulkanException
	{
		getComputer().getBufferLayoutWriter(0, 0).set(matrixVariable, matrix);
	}
	
	public void setSparseMatrix(Vector3dr matrix) throws VulkanException
	{
		getComputer().getBufferLayoutWriter(0, 0).set(matrixVariable, matrix);
	}
	
	public void setSparseMatrix(Vector4dr matrix) throws VulkanException
	{
		getComputer().getBufferLayoutWriter(0, 0).set(matrixVariable, matrix);
	}
	
	public void setName(String name)
	{
		
	}
	
	@Override
	public void close()
	{
		
	}
}
