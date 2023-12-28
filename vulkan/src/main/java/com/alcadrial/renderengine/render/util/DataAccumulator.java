package com.alcadrial.renderengine.render.util;

import java.lang.reflect.Field;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSubpassDependency;
import org.lwjgl.vulkan.VkSubpassDescription;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;
import org.lwjgl.vulkan.VkViewport;

import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.renderengine.core.util.NotUsed;
import com.alcadrial.renderengine.core.util.Nullable;
import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.FrameBuffer;
import com.alcadrial.renderengine.render.PhysicalDevice;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.RenderPass;
import com.alcadrial.renderengine.render.Renderer;
import com.alcadrial.renderengine.render.Surface;
import com.alcadrial.renderengine.render.Swapchain;
import com.alcadrial.renderengine.render.buffer.IndexBuffer;
import com.alcadrial.renderengine.render.buffer.VertexBuffer;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.pipeline.GraphicsPipeline;
import com.alcadrial.renderengine.render.pipeline.GraphicsPipeline.GraphicsPipelineBuildFunction;
import com.alcadrial.renderengine.render.pipeline.PipelineLayout;
import com.alcadrial.renderengine.render.shader.PreparedShader;
import com.alcadrial.renderengine.render.shader.Shader;
import com.alcadrial.renderengine.render.shader.ShaderInfo;
import com.alcadrial.renderengine.render.shader.ShaderType;
import com.alcadrial.renderengine.render.texture.Texture;
import com.alcadrial.renderengine.render.window.VulkanWindow;

public abstract class DataAccumulator<SDA extends DataAccumulator<SDA>> {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	protected static final Field[] ELEMENTS;
	
	static
	{
		Field[] allFields = DataAccumulator.class.getDeclaredFields();
		Field[] fs = new Field[allFields.length];
		int i = 0;
		for (Field field : DataAccumulator.class.getDeclaredFields()) if (field.getType().equals(DataElement.class)) fs[i++] = field;
		ELEMENTS = new Field[i];
		for (int j = 0; j < i; j++) ELEMENTS[j] = fs[j];
	}
	
	protected final MemoryStack stack;
	protected DataElement<Boolean> autoCloseLayout;
	protected DataElement<Boolean> autoCloseRenderPass;
	protected DataElement<PreparedShader> shader;
	protected DataElement<PreparedShader> vertexShader;
	protected DataElement<PreparedShader> tessellationControlShader;
	protected DataElement<PreparedShader> tessellationEvaluationShader;
	protected DataElement<PreparedShader> geometryShader;
	protected DataElement<PreparedShader> fragmentShader;
	protected DataElement<PreparedShader> computeShader;
	protected DataElement<VulkanWindow> targetWindow;
	protected DataElement<Device> device;
	protected DataElement<PhysicalDevice> physicalDevice;
	protected DataElement<GraphicsPipeline> graphicsPipeline;
	protected DataElement<PipelineLayout> pipelineLayout;
	protected DataElement<FrameBuffer> frameBuffer;
	protected DataElement<RenderPass> renderPass;
	protected DataElement<Surface> surface;
	protected DataElement<Swapchain> swapchain;
	protected DataElement<VertexBuffer> vertexBuffer;
	protected DataElement<IndexBuffer> indexBuffer;
	protected DataElement<VulkanBuffer> uniformBuffer;
	protected DataElement<VulkanBuffer> storageBuffer;
	protected DataElement<VkDescriptorSetLayoutBinding> descriptorSetLayoutBinding;
	protected DataElement<Texture> depthTexture;
	// VkPipelineColorBlendAttachmentState {
	protected DataElement<Integer> colorWriteMask;
	protected DataElement<Boolean> blendEnable;
	// ...
	// }
	// VkPipelineColorBlendStateCreateInfo (n*VkPipelineColorBlendAttachmentState) {
	protected DataElement<Boolean> logicOpEnable;
	protected DataElement<Integer> logicOp;
	protected Float[] blendConstants;
	// }
	// VkPipelineMultisampleStateCreateInfo {
	protected DataElement<Integer> rasterizationSamples;// extra pixel in rasterization
	protected DataElement<Boolean> sampleShadingEnable;
	protected DataElement<Float> minSampleShading;
	protected DataElement<Boolean> alphaToOneEnable;
	protected DataElement<Boolean> alphaToCoverageEnable;
	protected DataElement<int[]> sampleMask;
	// ...
	// }
	// VkPipelineRasterizationStateCreateInfo {
	protected DataElement<Integer> polygonMode;
	protected DataElement<Integer> cullMode;
	protected DataElement<Integer> frontFace;
	protected DataElement<Boolean> depthClampEnable;
	protected DataElement<Boolean> rasterizerDiscardEnable;
	protected DataElement<Float> lineWidth;
	protected DataElement<Boolean> depthBiasEnable;
	protected DataElement<Float> depthBiasConstantFactor;
	protected DataElement<Float> depthBiasClamp;
	protected DataElement<Float> depthBiasSlopeFactor;
	// }
	// VkPipelineViewportStateCreateInfo (n*VkGraphicsPipelineCreateInfo) {
	
	protected @NotUsed Vector3f viewportPos;
	protected @NotUsed Vector3f viewportSize;
	protected @NotUsed Vector2f scissorPos;
	protected @NotUsed Vector2f scissorSize;
	protected List<VkViewport> viewports;
	protected List<VkRect2D> scissors;
	// }
	// VkPipelineInputAssemblyStateCreateInfo {
	protected DataElement<Integer> topology;
	protected DataElement<Boolean> primitiveRestartEnable;
	// }
	protected List<VkPipelineColorBlendAttachmentState> pipelineColorBlendAttachmentStates;
	protected DataElement<Boolean> depthTestEnable;
	protected DataElement<Boolean> depthWriteEnable;
	protected DataElement<Integer> depthCompareOp;
	protected DataElement<Float> minDepth;
	protected DataElement<Float> maxDepth;
	
	protected DataAccumulator()
	{
		stack = MemoryStack.stackPush();
		blendConstants = new Float[4];
		for (Field field : ELEMENTS)
		{
			try
			{
				field.set(this, new DataElement<>());
			}
			catch (IllegalArgumentException | IllegalAccessException e)
			{
				throw new RuntimeException("The field \"" + field.getName() + "\" cannot be initialized, this cannot happen!");
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	protected DataAccumulator(DataAccumulator<?> defaultSource)
	{
		this();
		for (Field field : ELEMENTS)
		{
			try
			{
				((DataElement<Object>) field.get(this)).data = ((DataElement<Object>) field.get(defaultSource)).data;
			}
			catch (IllegalArgumentException | IllegalAccessException e)
			{
				throw new RuntimeException("The field \"" + field.getName() + "\" cannot be copied, this cannot happen!");
			}
		}
	}
	
	protected <SSDA extends PipelineColorBlendAttachmentStateAccumulator<SSDA>> SSDA startPipelineColorBlendAttachmentState()
	{
		throw new UnsupportedOperationException();
	}
	
	/**
	 * 
	 * @author Alcadrial
	 *
	 * @param <SSDA>
	 * @renderengine.Requires
	 * 		{@link #pipelineColorBlendAttachmentStates}<br>
	 */
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
		autoCloseLayout.set(value);
		return myself();
	}
	
	protected SDA autoCloseRenderPass(@Nullable Boolean value)
	{
		autoCloseRenderPass.set(value);
		return myself();
	}
	
	private SDA shader(DataElement<PreparedShader> element, @Nullable Shader value, @Nullable ShaderInfo info)
	{
		if (value == null) element.set(null);
		else
		{
			Debug.printlnIf(!value.isCompilationSuccessfull(), "Shader " + value.getFile() + " cannot be used becouse of errors");
			element.set(value.isCompilationSuccessfull() ? new PreparedShader(value, info) : null);
		}
		return myself();
	}
	
	protected SDA shader(DataElement<PreparedShader> element, @Nullable PreparedShader value)
	{
		if (value == null) element.set(null);
		else
		{
			Shader shader = value.shader();
			Debug.printlnIf(!shader.isCompilationSuccessfull(), "Shader " + shader.getFile() + " cannot be used becouse of errors");
			element.set(shader.isCompilationSuccessfull() ? value : null);
		}
		return myself();
	}
	
	protected SDA shader(@Nullable PreparedShader value)
	{
		return shader(shader, value);
	}
	
	protected SDA shader(@Nullable Shader value, @Nullable ShaderInfo info)
	{
		return shader(shader, value, info);
	}
	
	protected SDA genericShader(@Nullable PreparedShader value, ShaderType type)
	{
		if (value != null)
		{
			Shader shader = value.shader();
			Debug.printlnIf(!shader.isCompilationSuccessfull(), "Shader " + shader + " cannot be used becouse of errors");
			DataElement<PreparedShader> element;
			element = switch (type)
			{
				case VERTEX -> vertexShader;
				case TESS_CONTROL -> tessellationControlShader;
				case TESS_EVALUATION -> tessellationEvaluationShader;
				case GEOMETRY -> geometryShader;
				case FRAGMENT -> fragmentShader;
				case COMPUTE -> computeShader;
				default -> null;
			};
			if (element != null) element.set(shader.isCompilationSuccessfull() ? value : null);
			else Debug.println("Shader " + shader.getFile() + " cannot be used becouse the type is unknown");
		}
		return myself();
	}
	
	/**
	 * Uses {@link Shader#getType()} to distinguish the type
	 * 
	 * @param value
	 * @param info
	 * @param type
	 * 
	 * @return this for chaining
	 */
	protected SDA genericShader(@Nullable Shader value, @Nullable ShaderInfo info, ShaderType type)
	{
		if (value != null)
		{
			Debug.printlnIf(!value.isCompilationSuccessfull(), "Shader " + value + " cannot be used becouse of errors");
			DataElement<PreparedShader> element;
			element = switch (type)
			{
				case VERTEX -> vertexShader;
				case TESS_CONTROL -> tessellationControlShader;
				case TESS_EVALUATION -> tessellationEvaluationShader;
				case GEOMETRY -> geometryShader;
				case FRAGMENT -> fragmentShader;
				case COMPUTE -> computeShader;
				default -> null;
			};
			if (element != null) element.set(value.isCompilationSuccessfull() ? new PreparedShader(value, null) : null);
			else Debug.println("Shader " + value.getFile() + " cannot be used becouse the type is unknown");
		}
		return myself();
	}
	
	protected SDA vertexShader(@Nullable PreparedShader value)
	{
		return shader(vertexShader, value);
	}
	
	protected SDA vertexShader(@Nullable Shader value, @Nullable ShaderInfo info)
	{
		return shader(vertexShader, value, info);
	}
	
	protected SDA tessellationControlShader(@Nullable PreparedShader value)
	{
		return shader(tessellationControlShader, value);
	}
	
	protected SDA tessellationControlShader(@Nullable Shader value, @Nullable ShaderInfo info)
	{
		return shader(tessellationControlShader, value, info);
	}
	
	protected SDA tessellationEvaluationShader(@Nullable PreparedShader value)
	{
		return shader(tessellationEvaluationShader, value);
	}
	
	protected SDA tessellationEvaluationShader(@Nullable Shader value, @Nullable ShaderInfo info)
	{
		return shader(tessellationEvaluationShader, value, info);
	}
	
	protected SDA geometryShader(@Nullable PreparedShader value)
	{
		return shader(geometryShader, value);
	}
	
	protected SDA geometryShader(@Nullable Shader value, @Nullable ShaderInfo info)
	{
		return shader(geometryShader, value, info);
	}
	
	protected SDA fragmentShader(@Nullable PreparedShader value)
	{
		return shader(fragmentShader, value);
	}
	
	protected SDA fragmentShader(@Nullable Shader value, @Nullable ShaderInfo info)
	{
		return shader(fragmentShader, value, info);
	}
	
	protected SDA computeShader(@Nullable PreparedShader value)
	{
		return shader(computeShader, value);
	}
	
	protected SDA computeShader(@Nullable Shader value, @Nullable ShaderInfo info)
	{
		return shader(computeShader, value, info);
	}
	
	protected SDA targetWindow(@Nullable VulkanWindow value)
	{
		targetWindow.set(value);
		return myself();
	}
	
	protected SDA device(@Nullable Device value)
	{
		device.set(value);
		return myself();
	}
	
	protected SDA physicalDevice(@Nullable PhysicalDevice value)
	{
		physicalDevice.set(value);
		return myself();
	}
	
	protected SDA graphicsPipeline(@Nullable GraphicsPipeline value)
	{
		graphicsPipeline.set(value);
		return myself();
	}
	
	protected SDA pipelineLayout(@Nullable PipelineLayout value)
	{
		pipelineLayout.set(value);
		return myself();
	}
	
	protected SDA frameBuffer(@Nullable FrameBuffer value)
	{
		frameBuffer.set(value);
		return myself();
	}
	
	protected SDA renderPass(@Nullable RenderPass value)
	{
		renderPass.set(value);
		return myself();
	}
	
	protected SDA surface(@Nullable Surface value)
	{
		surface.set(value);
		return myself();
	}
	
	protected SDA swapchain(@Nullable Swapchain value)
	{
		swapchain.set(value);
		return myself();
	}
	
	protected SDA vertexBuffer(@Nullable VertexBuffer value)
	{
		vertexBuffer.set(value);
		return myself();
	}
	
	protected SDA indexBuffer(@Nullable IndexBuffer value)
	{
		indexBuffer.set(value);
		return myself();
	}
	
	protected SDA uniformBuffer(@Nullable VulkanBuffer value)
	{
		uniformBuffer.set(value);
		return myself();
	}
	
	protected SDA storageBuffer(@Nullable VulkanBuffer value)
	{
		storageBuffer.set(value);
		return myself();
	}
	
	protected SDA depthTexture(@Nullable Texture value)
	{
		depthTexture.set(value);
		return myself();
	}
	
	protected SDA topology(@Nullable Integer value)
	{
		topology.set(value);
		return myself();
	}
	
	protected SDA primitiveRestartEnable(@Nullable Boolean value)
	{
		primitiveRestartEnable.set(value);
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
		depthClampEnable.set(value);
		return myself();
	}
	
	protected SDA rasterizerDiscardEnable(@Nullable Boolean value)
	{
		rasterizerDiscardEnable.set(value);
		return myself();
	}
	
	protected SDA polygonMode(@Nullable Integer value)
	{
		polygonMode.set(value);
		return myself();
	}
	
	protected SDA cullMode(@Nullable Integer value)
	{
		cullMode.set(value);
		return myself();
	}
	
	protected SDA frontFace(@Nullable Integer value)
	{
		frontFace.set(value);
		return myself();
	}
	
	protected SDA depthBiasEnable(@Nullable Boolean value)
	{
		depthBiasEnable.set(value);
		return myself();
	}
	
	protected SDA depthBiasConstantFactor(@Nullable Float value)
	{
		depthBiasConstantFactor.set(value);
		return myself();
	}
	
	protected SDA depthBiasClamp(@Nullable Float value)
	{
		depthBiasClamp.set(value);
		return myself();
	}
	
	protected SDA depthBiasSlopeFactor(@Nullable Float value)
	{
		depthBiasSlopeFactor.set(value);
		return myself();
	}
	
	protected SDA lineWidth(@Nullable Float value)
	{
		lineWidth.set(value);
		return myself();
	}
	
	protected SDA rasterizationSamples(@Nullable Integer value)
	{
		rasterizationSamples.set(value);
		return myself();
	}
	
	protected SDA sampleShadingEnable(@Nullable Boolean value)
	{
		sampleShadingEnable.set(value);
		return myself();
	}
	
	protected SDA minSampleShading(@Nullable Float value)
	{
		minSampleShading.set(value);
		return myself();
	}
	
	protected SDA pSampleMask(@Nullable int[] value)
	{
		sampleMask.set(value);
		return myself();
	}
	
	protected SDA alphaToCoverageEnable(@Nullable Boolean value)
	{
		alphaToCoverageEnable.set(value);
		return myself();
	}
	
	protected SDA alphaToOneEnable(@Nullable Boolean value)
	{
		alphaToOneEnable.set(value);
		return myself();
	}
	
	protected SDA blendConstants(@Nullable Float r, @Nullable Float g, @Nullable Float b, @Nullable Float a)
	{
		blendConstants = new Float[] {r, g, b, a};
		return myself();
	}
	
	protected SDA logicOpEnable(@Nullable Boolean value)
	{
		logicOpEnable.set(value);
		return myself();
	}
	
	protected SDA logicOp(@Nullable Integer value)
	{
		logicOp.set(value);
		return myself();
	}
	
	protected SDA depthTestEnable(@Nullable Boolean value)
	{
		depthTestEnable.set(value);
		return myself();
	}
	
	protected SDA depthWriteEnable(@Nullable Boolean value)
	{
		depthWriteEnable.set(value);
		return myself();
	}
	
	protected SDA depthCompareOp(@Nullable Integer value)
	{
		depthCompareOp.set(value);
		return myself();
	}
	
	protected SDA minDepth(@Nullable Float value)
	{
		minDepth.set(value);
		return myself();
	}
	
	protected SDA maxDepth(@Nullable Float value)
	{
		maxDepth.set(value);
		return myself();
	}
	
	/**
	 * Creates a new render pass using the required fields.
	 * 
	 * @return a new render pass
	 * @renderengine.Requires
	 * 		{@link #device(Device) device}<br>
	 * 		{@link #surface(Surface) surface}
	 * @renderengine.Optional
	 * 		{@link #depthTexture(Texture) depthTexture}
	 */
	protected RenderPass createRenderPass()
	{
		Device device = this.device.require();
		Surface surface = this.surface.require();
		VulkanErrorUtils.assertStatement(surface.isInitialized(), "Surface not initialized");
		
		VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.calloc(depthTexture.get() != null ? 2 : 1, stack);// static
		
		VkAttachmentDescription colorAttachment = attachments.get();
		colorAttachment.format(surface.getFormat().format());
		colorAttachment.samples(VK10.VK_SAMPLE_COUNT_1_BIT);
		colorAttachment.loadOp(VK10.VK_ATTACHMENT_LOAD_OP_CLEAR);
		colorAttachment.storeOp(VK10.VK_ATTACHMENT_STORE_OP_STORE);
		colorAttachment.stencilLoadOp(VK10.VK_ATTACHMENT_LOAD_OP_DONT_CARE);
		colorAttachment.stencilStoreOp(VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE);
		colorAttachment.initialLayout(VK10.VK_IMAGE_LAYOUT_UNDEFINED);
		colorAttachment.finalLayout(KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);
		
		VkAttachmentReference.Buffer colorAttachmentRef = VkAttachmentReference.calloc(1, stack);// static
		colorAttachmentRef.attachment(0);// Color image attachment
		colorAttachmentRef.layout(VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
		
		VkSubpassDescription.Buffer subpass = VkSubpassDescription.calloc(1, stack);// static
		subpass.pipelineBindPoint(VK10.VK_PIPELINE_BIND_POINT_GRAPHICS);
		subpass.colorAttachmentCount(1);
		subpass.pColorAttachments(colorAttachmentRef);// using color image[0]
		
		int srcStageMask = VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
		int srcAccessMask = 0;
		int dstStageMask = VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
		int dstAccessMask = VK10.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT;
		
		if (depthTexture.get() != null)
		{
			VkAttachmentDescription depthAttachment = attachments.get();
			depthAttachment.format(depthTexture.get().getFormat());
			depthAttachment.samples(VK10.VK_SAMPLE_COUNT_1_BIT);
			depthAttachment.loadOp(VK10.VK_ATTACHMENT_LOAD_OP_CLEAR);
			depthAttachment.storeOp(VK10.VK_ATTACHMENT_STORE_OP_STORE);
			depthAttachment.stencilLoadOp(VK10.VK_ATTACHMENT_LOAD_OP_DONT_CARE);
			depthAttachment.stencilStoreOp(VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE);
			depthAttachment.initialLayout(VK10.VK_IMAGE_LAYOUT_UNDEFINED);
			depthAttachment.finalLayout(VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
			
			VkAttachmentReference depthAttachmentRef = VkAttachmentReference.calloc(stack);
			depthAttachmentRef.attachment(1);
			depthAttachmentRef.layout(VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
			
			subpass.pDepthStencilAttachment(depthAttachmentRef);
			
			srcStageMask |= VK10.VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT;
			dstStageMask |= VK10.VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT;
			dstAccessMask |= VK10.VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT;
		}
		
		VkSubpassDependency.Buffer dependency = VkSubpassDependency.calloc(1, stack);
		dependency.srcSubpass(VK10.VK_SUBPASS_EXTERNAL);
		dependency.dstSubpass(0);
		dependency.srcStageMask(srcStageMask);
		dependency.srcAccessMask(srcAccessMask);
		dependency.dstStageMask(dstStageMask);
		dependency.dstAccessMask(dstAccessMask);
		
		VkRenderPassCreateInfo renderPassInfo = VkRenderPassCreateInfo.calloc(stack);
		renderPassInfo.sType(VK10.VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO);
		renderPassInfo.pAttachments(attachments.rewind());
		renderPassInfo.pSubpasses(subpass);
		renderPassInfo.pDependencies(dependency);
		
		return new RenderPass(device, renderPassInfo);
	}
	
	/**
	 * Creates a new swapchain.
	 * 
	 * @return a new render pass
	 * @renderengine.Requires
	 * 		{@link #device}<br>
	 * 		{@link #surface}<br>
	 * 		{@link #renderPass}
	 * @renderengine.Optional
	 * 		{@link #depthTexture(Texture) depthTexture}
	 */
	protected Swapchain createSwapchain()
	{
		Device device = this.device.require();
		Surface surface = this.surface.require();
		VulkanErrorUtils.assertStatement(surface.isInitialized(), "Surface not initialized");
		RenderPass renderPass = this.renderPass.require();
		
		VkSwapchainCreateInfoKHR createInfo = VkSwapchainCreateInfoKHR.calloc(stack);
		createInfo.sType(KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR);
		createInfo.minImageCount(Renderer.SWAPPING_BUFFERS);
		createInfo.imageArrayLayers(1);
		createInfo.imageUsage(VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);
		if (device.getGraphicsQueue().getFamilyIndex() == device.getPresentQueue().getFamilyIndex()) createInfo.imageSharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);
		else
		{
			createInfo.imageSharingMode(VK10.VK_SHARING_MODE_CONCURRENT);
			createInfo.pQueueFamilyIndices(stack.ints(device.getGraphicsQueue().getFamilyIndex(), device.getPresentQueue().getFamilyIndex()));
		}
		createInfo.compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
		createInfo.clipped(true);
		
		surface.createSwapchain(renderPass, createInfo);
		
		Swapchain swapchain = surface.getSwapchain();
		swapchain.initialize(surface, depthTexture.get());
		return swapchain;
	}
	
	/**
	 * Creates a new swapchain.
	 * 
	 * @return a new render pass
	 * @renderengine.Requires
	 * 		{@link #device}<br>
	 * 		{@link #surface}<br>
	 * 		{@link #renderPass}<br>
	 * 		{@link #pipelineLayout}
	 */
	protected <GP extends GraphicsPipeline> GP createGraphicsPipeline(GraphicsPipelineBuildFunction<GP> buildFunction)
	{
		VkPipelineInputAssemblyStateCreateInfo inputAssembly = VulkanUtils.pipelineInputAssemblyStateInfo(stack, topology.orElse(VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST), primitiveRestartEnable.orElse(false));
		
		VkExtent2D extent = surface.require().getExtent();
		VkViewport.Buffer viewport = load(c -> VkViewport.malloc(c, stack), orElse(viewports, VkViewport.malloc(stack).set(0, extent.height(), extent.width(), -extent.height(), 0, 1)));// static
		VkRect2D.Buffer scissor = load(c -> VkRect2D.malloc(c, stack), orElse(scissors, VkRect2D.malloc(stack).set(VulkanUtils.offset(stack, 0, 0), extent)));// static
		
		VkPipelineViewportStateCreateInfo viewportState = VulkanUtils.pipelineViewportStateInfo(stack, viewport, scissor);
		
		VkPipelineRasterizationStateCreateInfo pipelineRasterizationStateCreateInfo = VkPipelineRasterizationStateCreateInfo.calloc(stack);
		pipelineRasterizationStateCreateInfo.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO);
		pipelineRasterizationStateCreateInfo.polygonMode(polygonMode.orElse(VK10.VK_POLYGON_MODE_FILL));
		pipelineRasterizationStateCreateInfo.cullMode(cullMode.orElse(VK10.VK_CULL_MODE_BACK_BIT));
		pipelineRasterizationStateCreateInfo.frontFace(frontFace.orElse(VK10.VK_FRONT_FACE_CLOCKWISE));
		pipelineRasterizationStateCreateInfo.depthClampEnable(depthClampEnable.orElse(false));
		pipelineRasterizationStateCreateInfo.rasterizerDiscardEnable(rasterizerDiscardEnable.orElse(false));
		pipelineRasterizationStateCreateInfo.lineWidth(lineWidth.orElse(1F));
		pipelineRasterizationStateCreateInfo.depthBiasEnable(depthBiasEnable.orElse(false));
		
		VkPipelineMultisampleStateCreateInfo pipelineMultisampleStateCreateInfo = VkPipelineMultisampleStateCreateInfo.calloc(stack);
		pipelineMultisampleStateCreateInfo.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO);
		pipelineMultisampleStateCreateInfo.rasterizationSamples(rasterizationSamples.orElse(VK10.VK_SAMPLE_COUNT_1_BIT));
		pipelineMultisampleStateCreateInfo.sampleShadingEnable(sampleShadingEnable.orElse(false));
		pipelineMultisampleStateCreateInfo.minSampleShading(minSampleShading.orElse(0F));
		pipelineMultisampleStateCreateInfo.alphaToCoverageEnable(alphaToCoverageEnable.orElse(false));
		pipelineMultisampleStateCreateInfo.alphaToOneEnable(alphaToOneEnable.orElse(false));
		
		VkPipelineColorBlendStateCreateInfo pipelineColorBlendStateCreateInfo = VkPipelineColorBlendStateCreateInfo.calloc(stack);
		pipelineColorBlendStateCreateInfo.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO);
		pipelineColorBlendStateCreateInfo.logicOpEnable(logicOpEnable.orElse(false));
		pipelineColorBlendStateCreateInfo.logicOp(logicOp.orElse(VK10.VK_LOGIC_OP_COPY));
		pipelineColorBlendStateCreateInfo.pAttachments(load(c -> VkPipelineColorBlendAttachmentState.calloc(c, stack), orElse(pipelineColorBlendAttachmentStates, VkPipelineColorBlendAttachmentState.calloc(stack).colorWriteMask(VK10.VK_COLOR_COMPONENT_R_BIT | VK10.VK_COLOR_COMPONENT_G_BIT | VK10.VK_COLOR_COMPONENT_B_BIT | VK10.VK_COLOR_COMPONENT_A_BIT).blendEnable(false))));
		pipelineColorBlendStateCreateInfo.blendConstants(load(blendConstants, new float[] {0, 0, 0, 0}));
		
		VkPipelineDepthStencilStateCreateInfo pipelineDepthStencilStateCreateInfo = VkPipelineDepthStencilStateCreateInfo.calloc(stack);
		pipelineDepthStencilStateCreateInfo.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO);
		pipelineDepthStencilStateCreateInfo.depthTestEnable(depthTestEnable.orElse(false));
		pipelineDepthStencilStateCreateInfo.depthWriteEnable(depthWriteEnable.orElse(true));
		pipelineDepthStencilStateCreateInfo.depthCompareOp(depthCompareOp.orElse(VK10.VK_COMPARE_OP_LESS));
		pipelineDepthStencilStateCreateInfo.depthBoundsTestEnable(false);
		pipelineDepthStencilStateCreateInfo.minDepthBounds(minDepth.orElse(0F));
		pipelineDepthStencilStateCreateInfo.maxDepthBounds(maxDepth.orElse(1F));
		pipelineDepthStencilStateCreateInfo.stencilTestEnable(false);
		// pipelineDepthStencilStateCreateInfo.front();
		// pipelineDepthStencilStateCreateInfo.back();
		
		VkGraphicsPipelineCreateInfo.Buffer pipelineInfo = VkGraphicsPipelineCreateInfo.calloc(1, stack);
		pipelineInfo.sType(VK10.VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO);
		pipelineInfo.pInputAssemblyState(inputAssembly);
		pipelineInfo.pViewportState(viewportState);
		pipelineInfo.pRasterizationState(pipelineRasterizationStateCreateInfo);
		pipelineInfo.pMultisampleState(pipelineMultisampleStateCreateInfo);
		pipelineInfo.pColorBlendState(pipelineColorBlendStateCreateInfo);
		pipelineInfo.pDepthStencilState(pipelineDepthStencilStateCreateInfo);
		pipelineInfo.renderPass(renderPass.require().getVk());
		pipelineInfo.subpass(0);
		pipelineInfo.basePipelineHandle(VK10.VK_NULL_HANDLE);
		pipelineInfo.basePipelineIndex(-1);
		
		GraphicsPipeline.Builder builder = GraphicsPipeline.create();
		builder.autoCloseLayout(false);
		builder.autoCloseRenderPass(false);
		builder.vertexShader(vertexShader.get());
		builder.tessellationControlShader(tessellationControlShader.get());
		builder.tessellationEvaluationShader(tessellationEvaluationShader.get());
		builder.geometryShader(geometryShader.get());
		builder.fragmentShader(fragmentShader.get());
		builder.computeShader(computeShader.get());
		return builder.build(buildFunction, device.require(), pipelineLayout.require(), renderPass.require(), pipelineInfo);
	}
	
	protected <T> T orElseThrow(@Nullable T requestedValue)
	{
		if (requestedValue != null) return requestedValue;
		throw new NullPointerException();
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
	
	protected <S extends Struct<S>, B extends StructBuffer<S, B>> B load(Function<Integer, B> builder, List<S> list)
	{
		B buffer = builder.apply(list.size());
		for (S s : list) buffer.put(s);
		return buffer.rewind();
	}
	
	protected <S extends Struct<S>, B extends StructBuffer<S, B>> B load(Function<Integer, B> builder, List<S> list, S defaultValue)
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
		FloatBuffer buffer;
		if (array != null)
		{
			buffer = stack.callocFloat(array.length);
			for (int i = 0; i < array.length; i++) buffer.put(orElse(array[i], defaultValue[i]));
		}
		else
		{
			buffer = stack.callocFloat(defaultValue.length);
			for (float element : defaultValue) buffer.put(element);
		}
		return buffer.rewind();
	}
	
	protected boolean isValid(Shader shader)
	{
		return shader != null && shader.isCompilationSuccessfull();
	}
	
	protected void clearAllModificationMarks()
	{
		
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
	
	protected static class DataElement<T> {
		
		private T data;
		private boolean isModified;
		
		public T get()
		{
			return data;
		}
		
		public T require()
		{
			return Objects.requireNonNull(data);
		}
		
		public T require(String error)
		{
			return Objects.requireNonNull(data, error);
		}
		
		public T orElse(T data)
		{
			if (this.data == null) return data;
			return this.data;
		}
		
		public T newValueOrElse(T data)
		{
			if (isModified)
			{
				isModified = false;
				return this.data;
			}
			return data;
		}
		
		public void setNonNull(T data)
		{
			Objects.requireNonNull(data);
			isModified = Objects.equals(data, this.data);
			this.data = data;
		}
		
		public void setNonNull(T data, String error)
		{
			Objects.requireNonNull(data, error);
			isModified = Objects.equals(data, this.data);
			this.data = data;
		}
		
		public void set(T data)
		{
			isModified = Objects.equals(data, this.data);
			this.data = data;
		}
		
		public boolean isModified()
		{
			return isModified;
		}
		
		public Optional<T> asOptional()
		{
			return Optional.ofNullable(data);
		}
	}
}
