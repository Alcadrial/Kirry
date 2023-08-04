package com.alcadrial.renderengine.render.util;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.function.Function;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkViewport;

import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.renderengine.core.util.NotUsed;
import com.alcadrial.renderengine.core.util.Nullable;
import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.FrameBuffer;
import com.alcadrial.renderengine.render.PhysicalDevice;
import com.alcadrial.renderengine.render.RenderPass;
import com.alcadrial.renderengine.render.Surface;
import com.alcadrial.renderengine.render.SwapChain;
import com.alcadrial.renderengine.render.buffer.IndexBuffer;
import com.alcadrial.renderengine.render.buffer.UniformBuffer;
import com.alcadrial.renderengine.render.buffer.VertexBuffer;
import com.alcadrial.renderengine.render.pipeline.GraphicsPipeline;
import com.alcadrial.renderengine.render.pipeline.PipelineLayout;
import com.alcadrial.renderengine.render.shader.Shader;
import com.alcadrial.renderengine.render.window.Window;

public abstract class DataAccumulator<SDA extends DataAccumulator<SDA>> {
	
	protected MemoryStack stack;
	protected Boolean autoCloseLayout;
	protected Boolean autoCloseRenderPass;
	protected Shader shader;
	protected Shader vertexShader;
	protected Shader tessellationControlShader;
	protected Shader tessellationEvaluationShader;
	protected Shader geometryShader;
	protected Shader fragmentShader;
	protected Shader computeShader;
	protected Window targetWindow;
	protected Device device;
	protected PhysicalDevice physicalDevice;
	protected GraphicsPipeline graphicsPipeline;
	protected PipelineLayout pipelineLayout;
	protected FrameBuffer frameBuffer;
	protected RenderPass renderPass;
	protected Surface surface;
	protected SwapChain swapChain;
	protected VertexBuffer vertexBuffer;
	protected IndexBuffer indexBuffer;
	protected UniformBuffer uniformBuffer;
	// VkPipelineColorBlendAttachmentState {
	protected Integer colorWriteMask;
	protected Boolean blendEnable;
	// ...
	// }
	// VkPipelineColorBlendStateCreateInfo (n*VkPipelineColorBlendAttachmentState) {
	protected Boolean logicOpEnable;
	protected Integer logicOp;
	protected Float[] blendConstants;
	// }
	// VkPipelineMultisampleStateCreateInfo {
	protected Integer rasterizationSamples;//extra pixel in rasterization
	protected Boolean sampleShadingEnable;
	protected Float minSampleShading;
	protected Boolean alphaToOneEnable;
	protected Boolean alphaToCoverageEnable;
	protected int[] sampleMask;
	// ...
	// }
	// VkPipelineRasterizationStateCreateInfo {
	protected Integer polygonMode;
	protected Integer cullMode;
	protected Integer frontFace;
	protected Boolean depthClampEnable;
	protected Boolean rasterizerDiscardEnable;
	protected Float lineWidth;
	protected Boolean depthBiasEnable;
	protected Float depthBiasConstantFactor;
	protected Float depthBiasClamp;
	protected Float depthBiasSlopeFactor;
	// }
	// VkPipelineViewportStateCreateInfo (n*VkGraphicsPipelineCreateInfo) {
	@NotUsed
	protected Vector3f viewportPos;
	@NotUsed
	protected Vector3f viewportSize;
	@NotUsed
	protected Vector2f scissorPos;
	@NotUsed
	protected Vector2f scissorSize;
	protected List<VkViewport> viewports;
	protected List<VkRect2D> scissors;
	// }
	// VkPipelineInputAssemblyStateCreateInfo {
	protected Integer topology;
	protected Boolean primitiveRestartEnable;
	// }
	protected List<VkPipelineColorBlendAttachmentState> pipelineColorBlendAttachmentStates;
	
	protected DataAccumulator()
	{
		stack = MemoryStack.stackPush();
		blendConstants = new Float[4];
	}
	
	protected <SSDA extends PipelineColorBlendAttachmentStateAccumulator<SSDA>> SSDA startPipelineColorBlendAttachmentState()
	{
		throw new UnsupportedOperationException();
	}
	
	protected class PipelineColorBlendAttachmentStateAccumulator<SSDA extends PipelineColorBlendAttachmentStateAccumulator<SSDA>> {
		
		protected VkPipelineColorBlendAttachmentState vk;
		
		protected PipelineColorBlendAttachmentStateAccumulator()
		{
			vk = VkPipelineColorBlendAttachmentState.calloc(stack);
		}
		
		protected SSDA blendEnable(Boolean value)
		{
			vk.blendEnable(value);
			return myself();
		}
		
		protected SSDA srcColorBlendFactor(Integer value)
		{
			vk.srcColorBlendFactor(value);
			return myself();
		}
		
		protected SSDA dstColorBlendFactor(Integer value)
		{
			vk.dstColorBlendFactor(value);
			return myself();
		}
		
		protected SSDA colorBlendOp(Integer value)
		{
			vk.colorBlendOp(value);
			return myself();
		}
		
		protected SSDA srcAlphaBlendFactor(Integer value)
		{
			vk.srcAlphaBlendFactor(value);
			return myself();
		}
		
		protected SSDA dstAlphaBlendFactor(Integer value)
		{
			vk.dstAlphaBlendFactor(value);
			return myself();
		}
		
		protected SSDA alphaBlendOp(Integer value)
		{
			vk.alphaBlendOp(value);
			return myself();
		}
		
		protected SSDA colorWriteMask(Integer value)
		{
			vk.colorWriteMask(value);
			return myself();
		}
		
		@SuppressWarnings("unchecked")
		protected SSDA myself()
		{
			return (SSDA) this;
		}
		
		public SDA end()
		{
			pipelineColorBlendAttachmentStates.add(vk);
			return DataAccumulator.this.myself();
		}
	}
	
	protected SDA autoCloseLayout(@Nullable Boolean value)
	{
		autoCloseLayout = value;
		return myself();
	}
	
	protected SDA autoCloseRenderPass(@Nullable Boolean value)
	{
		autoCloseRenderPass = value;
		return myself();
	}
	
	protected SDA shader(@Nullable Shader value)
	{
		if (value == null) shader = null;
		else
		{
			Debug.printlnIf(value.getOrCompile().hasErrors(), "Shader " + value.getFile() + " cannot be used becouse of errors");
			shader = value.getOrCompile().hasErrors() ? null : value;
		}
		return myself();
	}
	
	/**
	 * Use {@link Shader#getType()} to distinguish the type
	 * @param value
	 * @return
	 */
	protected SDA genericShader(@Nullable Shader value)
	{
		if (value != null)
		{
			Debug.printlnIf(value.getOrCompile().hasErrors(), "Shader " + value.getFile() + " cannot be used becouse of errors");
			switch (value.getType())
			{
				case VERTEX -> vertexShader = value.getOrCompile().hasErrors() ? null : value;
				case TESS_CONTROL -> tessellationControlShader = value.getOrCompile().hasErrors() ? null : value;
				case TESS_EVALUATION -> tessellationEvaluationShader = value.getOrCompile().hasErrors() ? null : value;
				case GEOMETRY -> geometryShader = value.getOrCompile().hasErrors() ? null : value;
				case FRAGMENT -> fragmentShader = value.getOrCompile().hasErrors() ? null : value;
				case COMPUTE -> computeShader = value.getOrCompile().hasErrors() ? null : value;
				default -> Debug.println("Shader " + value.getFile() + " cannot be used becouse the type is unknown");
			}
		}
		return myself();
	}
	
	protected SDA vertexShader(@Nullable Shader value)
	{
		if (value == null) vertexShader = null;
		else
		{
			Debug.printlnIf(value.getOrCompile().hasErrors(), "Shader " + value.getFile() + " cannot be used becouse of errors");
			vertexShader = value.getOrCompile().hasErrors() ? null : value;
		}
		return myself();
	}
	
	protected SDA tessellationControlShader(@Nullable Shader value)
	{
		if (value == null) tessellationControlShader = null;
		else
		{
			Debug.printlnIf(value.getOrCompile().hasErrors(), "Shader " + value.getFile() + " cannot be used becouse of errors");
			tessellationControlShader = value.getOrCompile().hasErrors() ? null : value;
		}
		return myself();
	}
	
	protected SDA tessellationEvaluationShader(@Nullable Shader value)
	{
		if (value == null) tessellationEvaluationShader = null;
		else
		{
			Debug.printlnIf(value.getOrCompile().hasErrors(), "Shader " + value.getFile() + " cannot be used becouse of errors");
			tessellationEvaluationShader = value.getOrCompile().hasErrors() ? null : value;
		}
		return myself();
	}
	
	protected SDA geometryShader(@Nullable Shader value)
	{
		if (value == null) geometryShader = null;
		else
		{
			Debug.printlnIf(value.getOrCompile().hasErrors(), "Shader " + value.getFile() + " cannot be used becouse of errors");
			geometryShader = value.getOrCompile().hasErrors() ? null : value;
		}
		return myself();
	}
	
	protected SDA fragmentShader(@Nullable Shader value)
	{
		if (value == null) fragmentShader = null;
		else
		{
			Debug.printlnIf(value.getOrCompile().hasErrors(), "Shader " + value.getFile() + " cannot be used becouse of errors");
			fragmentShader = value.getOrCompile().hasErrors() ? null : value;
		}
		return myself();
	}
	
	protected SDA computeShader(@Nullable Shader value)
	{
		if (value == null) computeShader = null;
		else
		{
			Debug.printlnIf(value.getOrCompile().hasErrors(), "Shader " + value.getFile() + " cannot be used becouse of errors");
			computeShader = value.getOrCompile().hasErrors() ? null : value;
		}
		return myself();
	}
	
	protected SDA targetWindow(@Nullable Window value)
	{
		targetWindow = value;
		return myself();
	}
	
	protected SDA device(@Nullable Device value)
	{
		device = value;
		return myself();
	}
	
	protected SDA physicalDevice(@Nullable PhysicalDevice value)
	{
		physicalDevice = value;
		return myself();
	}
	
	protected SDA graphicsPipeline(@Nullable GraphicsPipeline value)
	{
		graphicsPipeline = value;
		return myself();
	}
	
	protected SDA pipelineLayout(@Nullable PipelineLayout value)
	{
		pipelineLayout = value;
		return myself();
	}
	
	protected SDA frameBuffer(@Nullable FrameBuffer value)
	{
		frameBuffer = value;
		return myself();
	}
	
	protected SDA renderPass(@Nullable RenderPass value)
	{
		renderPass = value;
		return myself();
	}
	
	protected SDA surface(@Nullable Surface value)
	{
		surface = value;
		return myself();
	}
	
	protected SDA swapChain(@Nullable SwapChain value)
	{
		swapChain = value;
		return myself();
	}
	
	protected SDA vertexBuffer(@Nullable VertexBuffer value)
	{
		vertexBuffer = value;
		return myself();
	}
	
	protected SDA indexBuffer(@Nullable IndexBuffer value)
	{
		indexBuffer = value;
		return myself();
	}
	
	protected SDA uniformBuffer(@Nullable UniformBuffer value)
	{
		uniformBuffer = value;
		return myself();
	}
	
	protected SDA topology(@Nullable Integer value)
	{
		topology = value;
		return myself();
	}
	
	protected SDA primitiveRestartEnable(@Nullable Boolean value)
	{
		primitiveRestartEnable = value;
		return myself();
	}
	
	protected SDA pViewports(@Nullable List<VkViewport> value)//////////////////////////////////////////////////////////
	{
		viewports = value;
		return myself();
	}
	
	protected SDA pScissors(@Nullable List<VkRect2D> value)
	{
		scissors = value;
		return myself();
	}
	
	protected SDA depthClampEnable(@Nullable Boolean value)
	{
		depthClampEnable = value;
		return myself();
	}
	
	protected SDA rasterizerDiscardEnable(@Nullable Boolean value)
	{
		rasterizerDiscardEnable = value;
		return myself();
	}
	
	protected SDA polygonMode(@Nullable Integer value)
	{
		polygonMode = value;
		return myself();
	}
	
	protected SDA cullMode(@Nullable Integer value)
	{
		cullMode = value;
		return myself();
	}
	
	protected SDA frontFace(@Nullable Integer value)
	{
		frontFace = value;
		return myself();
	}
	
	protected SDA depthBiasEnable(@Nullable Boolean value)
	{
		depthBiasEnable = value;
		return myself();
	}
	
	protected SDA depthBiasConstantFactor(@Nullable Float value)
	{
		depthBiasConstantFactor = value;
		return myself();
	}
	
	protected SDA depthBiasClamp(@Nullable Float value)
	{
		depthBiasClamp = value;
		return myself();
	}
	
	protected SDA depthBiasSlopeFactor(@Nullable Float value)
	{
		depthBiasSlopeFactor = value;
		return myself();
	}
	
	protected SDA lineWidth(@Nullable Float value)
	{
		lineWidth = value;
		return myself();
	}
	
	protected SDA rasterizationSamples(@Nullable Integer value)
	{
		rasterizationSamples = value;
		return myself();
	}
	
	protected SDA sampleShadingEnable(@Nullable Boolean value)
	{
		sampleShadingEnable = value;
		return myself();
	}
	
	protected SDA minSampleShading(@Nullable Float value)
	{
		minSampleShading = value;
		return myself();
	}
	
	protected SDA pSampleMask(@Nullable int[] value)
	{
		sampleMask = value;
		return myself();
	}
	
	protected SDA alphaToCoverageEnable(@Nullable Boolean value)
	{
		alphaToCoverageEnable = value;
		return myself();
	}
	
	protected SDA alphaToOneEnable(@Nullable Boolean value)
	{
		alphaToOneEnable = value;
		return myself();
	}
	
	protected SDA blendConstants(@Nullable Float r, @Nullable Float g, @Nullable Float b, @Nullable Float a)
	{
		blendConstants = new Float[] {r, g, b, a};
		return myself();
	}
	
	protected SDA logicOpEnable(@Nullable Boolean value)
	{
		logicOpEnable = value;
		return myself();
	}
	
	protected SDA logicOp(@Nullable Integer value)
	{
		logicOp = value;
		return myself();
	}
	
	protected <T> T orElse(@Nullable T requestedValue, T defaultValue)
	{
		if (requestedValue != null) return requestedValue;
		return defaultValue;
	}
	
	protected <T> List<T> orElse(@Nullable List<T> requestedValue, T defaultValue)
	{
		if (requestedValue != null) return requestedValue;
		return List.of(defaultValue);
	}
	
	protected <T> T[] orElse(@Nullable T[] requestedValue, T defaultValue)
	{
		if (requestedValue != null) return requestedValue;
		return varargs(defaultValue);
	}
	
	@SafeVarargs
	private <T> T[] varargs(T... varargs)
	{
		return varargs;
	}
	
	protected <S extends Struct, B extends StructBuffer<S, B>> B load(Function<Integer, B> builder, List<S> list)
	{
		B buffer = builder.apply(list.size());
		for (S s : list) buffer.put(s);
		return buffer.rewind();
	}
	
	protected <S extends Struct, B extends StructBuffer<S, B>> B load(Function<Integer, B> builder, List<S> list, S defaultValue)
	{
		B buffer = builder.apply(list.size());
		for (S s : list) buffer.put(orElse(s, defaultValue));
		return buffer.rewind();
	}
	
	protected IntBuffer load(Integer[] array)
	{
		IntBuffer buffer = stack.callocInt(array.length);
		for (Integer i : array) buffer.put(i);
		return buffer.rewind();
	}
	
	protected IntBuffer load(Integer[] array, int defaultValue)
	{
		IntBuffer buffer = stack.callocInt(array.length);
		for (Integer i : array) buffer.put(orElse(i, defaultValue));
		return buffer.rewind();
	}
	
	protected IntBuffer load(Integer[] array, int[] defaultValue)
	{
		IntBuffer buffer = stack.callocInt(array.length);
		for (int i = 0; i < array.length; i++) buffer.put(orElse(array[i], defaultValue[i]));
		return buffer.rewind();
	}
	
	protected FloatBuffer load(Float[] array)
	{
		FloatBuffer buffer = stack.callocFloat(array.length);
		for (Float f : array) buffer.put(f);
		return buffer.rewind();
	}
	
	protected FloatBuffer load(Float[] array, float defaultValue)
	{
		FloatBuffer buffer = stack.callocFloat(array.length);
		for (Float f : array) buffer.put(orElse(f, defaultValue));
		return buffer.rewind();
	}
	
	protected FloatBuffer load(Float[] array, float[] defaultValue)
	{
		FloatBuffer buffer = stack.callocFloat(array.length);
		for (int i = 0; i < array.length; i++) buffer.put(orElse(array[i], defaultValue[i]));
		return buffer.rewind();
	}
	
	@SuppressWarnings("unchecked")
	protected SDA myself()
	{
		return (SDA) this;
	}
	
	protected void close()
	{
		stack.close();
	}
}
