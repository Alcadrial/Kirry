package com.alcadrial.renderengine.render.util;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Objects;
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
import com.alcadrial.renderengine.render.RenderPass;
import com.alcadrial.renderengine.render.Surface;
import com.alcadrial.renderengine.render.Swapchain;
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
	protected Swapchain swapchain;
	protected VertexBuffer vertexBuffer;
	protected IndexBuffer indexBuffer;
	protected UniformBuffer uniformBuffer;
	protected VkDescriptorSetLayoutBinding descriptorSetLayoutBinding;
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
	
	protected @NotUsed Vector3f viewportPos;
	protected @NotUsed Vector3f viewportSize;
	protected @NotUsed Vector2f scissorPos;
	protected @NotUsed Vector2f scissorSize;
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
			Debug.printlnIf(!value.isCompilationSuccessfull(), "Shader " + value.getFile() + " cannot be used becouse of errors");
			shader = value.isCompilationSuccessfull() ? value : null;
		}
		return myself();
	}
	
	/**
	 * Uses {@link Shader#getType()} to distinguish the type
	 * @param value
	 * @return this for chaining
	 */
	protected SDA genericShader(@Nullable Shader value)
	{
		if (value != null)
		{
			Debug.printlnIf(!value.isCompilationSuccessfull(), "Shader " + value.getFile() + " cannot be used becouse of errors");
			switch (value.getType())
			{
				case VERTEX -> vertexShader = value.isCompilationSuccessfull() ? value : null;
				case TESS_CONTROL -> tessellationControlShader = value.isCompilationSuccessfull() ? value : null;
				case TESS_EVALUATION -> tessellationEvaluationShader = value.isCompilationSuccessfull() ? value : null;
				case GEOMETRY -> geometryShader = value.isCompilationSuccessfull() ? value : null;
				case FRAGMENT -> fragmentShader = value.isCompilationSuccessfull() ? value : null;
				case COMPUTE -> computeShader = value.isCompilationSuccessfull() ? value : null;
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
			Debug.printlnIf(!value.isCompilationSuccessfull(), "Shader " + value.getFile() + " cannot be used becouse of errors");
			vertexShader = value.isCompilationSuccessfull() ? value : null;
		}
		return myself();
	}
	
	protected SDA tessellationControlShader(@Nullable Shader value)
	{
		if (value == null) tessellationControlShader = null;
		else
		{
			Debug.printlnIf(!value.isCompilationSuccessfull(), "Shader " + value.getFile() + " cannot be used becouse of errors");
			tessellationControlShader = value.isCompilationSuccessfull() ? value : null;
		}
		return myself();
	}
	
	protected SDA tessellationEvaluationShader(@Nullable Shader value)
	{
		if (value == null) tessellationEvaluationShader = null;
		else
		{
			Debug.printlnIf(!value.isCompilationSuccessfull(), "Shader " + value.getFile() + " cannot be used becouse of errors");
			tessellationEvaluationShader = value.isCompilationSuccessfull() ? value : null;
		}
		return myself();
	}
	
	protected SDA geometryShader(@Nullable Shader value)
	{
		if (value == null) geometryShader = null;
		else
		{
			Debug.printlnIf(!value.isCompilationSuccessfull(), "Shader " + value.getFile() + " cannot be used becouse of errors");
			geometryShader = value.isCompilationSuccessfull() ? value : null;
		}
		return myself();
	}
	
	protected SDA fragmentShader(@Nullable Shader value)
	{
		if (value == null) fragmentShader = null;
		else
		{
			Debug.printlnIf(!value.isCompilationSuccessfull(), "Shader " + value.getFile() + " cannot be used becouse of errors");
			fragmentShader = value.isCompilationSuccessfull() ? value : null;
		}
		return myself();
	}
	
	protected SDA computeShader(@Nullable Shader value)
	{
		if (value == null) computeShader = null;
		else
		{
			Debug.printlnIf(!value.isCompilationSuccessfull(), "Shader " + value.getFile() + " cannot be used becouse of errors");
			computeShader = value.isCompilationSuccessfull() ? value : null;
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
	
	protected SDA swapchain(@Nullable Swapchain value)
	{
		swapchain = value;
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
	
	/**
	 * Creates a new render pass using the required fields.
	 * 
	 * @param stack
	 * @return a new render pass
	 * @renderengine.Requires
	 * 		{@link #device(Device) device}<br>
	 * 		{@link #surface(Surface) surface}
	 */
	protected RenderPass createRenderPass()
	{
		Device device = Objects.requireNonNull(this.device, "Device missing");
		Surface surface = Objects.requireNonNull(this.surface, "Surface missing");
		VulkanErrorUtils.assertStatement(surface.isInitialized(), "Surface not initialized");
		
		VkAttachmentDescription.Buffer colorAttachment = VkAttachmentDescription.calloc(1, stack);//static
		colorAttachment.format(surface.getFormat().format());
		colorAttachment.samples(VK10.VK_SAMPLE_COUNT_1_BIT);
		colorAttachment.loadOp(VK10.VK_ATTACHMENT_LOAD_OP_CLEAR);
		colorAttachment.storeOp(VK10.VK_ATTACHMENT_STORE_OP_STORE);
		colorAttachment.stencilLoadOp(VK10.VK_ATTACHMENT_LOAD_OP_DONT_CARE);
		colorAttachment.stencilStoreOp(VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE);
		colorAttachment.initialLayout(VK10.VK_IMAGE_LAYOUT_UNDEFINED);
		colorAttachment.finalLayout(KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);
		
		VkAttachmentReference.Buffer colorAttachmentRef = VkAttachmentReference.calloc(1, stack);//static
		colorAttachmentRef.attachment(0);//Color image attachment 
		colorAttachmentRef.layout(VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
		
		VkSubpassDescription.Buffer subpass = VkSubpassDescription.calloc(1, stack);//static
		subpass.pipelineBindPoint(VK10.VK_PIPELINE_BIND_POINT_GRAPHICS);
		subpass.colorAttachmentCount(1);
		subpass.pColorAttachments(colorAttachmentRef);// using color image[0]
		
		VkSubpassDependency.Buffer dependency = VkSubpassDependency.calloc(1, stack);
		dependency.srcSubpass(VK10.VK_SUBPASS_EXTERNAL);
		dependency.dstSubpass(0);
		dependency.srcStageMask(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
		dependency.srcAccessMask(0);
		dependency.dstStageMask(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
		dependency.dstAccessMask(VK10.VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK10.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);
		
		VkRenderPassCreateInfo renderPassInfo = VkRenderPassCreateInfo.calloc(stack);
		renderPassInfo.sType(VK10.VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO);
		renderPassInfo.pAttachments(colorAttachment);
		renderPassInfo.pSubpasses(subpass);
		renderPassInfo.pDependencies(dependency);
		
		return new RenderPass(device, renderPassInfo);
	}
	
	/**
	 * Creates a new swapchain.
	 * 
	 * @param stack
	 * @return a new render pass
	 * @renderengine.Requires
	 * 		{@link #physicalDevice}<br>
	 * 		{@link #surface}<br>
	 * 		{@link #renderPass}
	 */
	protected Swapchain createSwapchain()
	{
		PhysicalDevice physicalDevice = Objects.requireNonNull(this.physicalDevice, "Device missing");
		Surface surface = Objects.requireNonNull(this.surface, "Surface missing");
		VulkanErrorUtils.assertStatement(surface.isInitialized(), "Surface not initialized");
		RenderPass renderPass = Objects.requireNonNull(this.renderPass, "Renderpass missing");
		
		VkSwapchainCreateInfoKHR createInfo = VkSwapchainCreateInfoKHR.calloc(stack);
		createInfo.sType(KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR);
		createInfo.minImageCount(2);
		createInfo.imageArrayLayers(1);
		createInfo.imageUsage(VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);
		if (physicalDevice.getQueueGraphicsFamilyIndex() == physicalDevice.getQueueSurfaceFamilyIndex()) createInfo.imageSharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);
		else
		{
			createInfo.imageSharingMode(VK10.VK_SHARING_MODE_CONCURRENT);
			createInfo.pQueueFamilyIndices(stack.ints(physicalDevice.getQueueGraphicsFamilyIndex(), physicalDevice.getQueueSurfaceFamilyIndex()));
		}
		createInfo.compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
		createInfo.clipped(true);
		
		surface.createSwapchain(renderPass, createInfo);
		
		return surface.getSwapchain();
	}
	
	/**
	 * Creates a new swapchain.
	 * 
	 * @param stack
	 * @return a new render pass
	 * @renderengine.Requires
	 * 		{@link #device}<br>
	 * 		{@link #surface}<br>
	 * 		{@link #renderPass}<br>
	 * 		{@link #pipelineLayout}
	 */
	protected GraphicsPipeline createGraphicsPipeline()
	{
		VkPipelineInputAssemblyStateCreateInfo inputAssembly = VulkanUtils.pipelineInputAssemblyStateInfo(stack, orElse(topology, VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST), orElse(primitiveRestartEnable, false));
		
		VkExtent2D extent = surface.getExtent();
		VkViewport.Buffer viewport = load(c -> VkViewport.calloc(c, stack), orElse(viewports, VkViewport.calloc(stack).set(0, extent.height(), extent.width(), -extent.height(), 0, 1)));//static
		VkRect2D.Buffer scissor = load(c -> VkRect2D.calloc(c, stack), orElse(scissors, VkRect2D.calloc(stack).set(VulkanUtils.offset(stack, 0, 0), extent)));//static
		
		VkPipelineViewportStateCreateInfo viewportState = VulkanUtils.pipelineViewportStateInfo(stack, viewport, scissor);
		
		VkPipelineRasterizationStateCreateInfo pipelineRasterizationStateCreateInfo = VkPipelineRasterizationStateCreateInfo.calloc(stack);
		pipelineRasterizationStateCreateInfo.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO);
		pipelineRasterizationStateCreateInfo.polygonMode(orElse(polygonMode, VK10.VK_POLYGON_MODE_FILL));
		pipelineRasterizationStateCreateInfo.cullMode(orElse(cullMode, VK10.VK_CULL_MODE_BACK_BIT));
		pipelineRasterizationStateCreateInfo.frontFace(orElse(frontFace, VK10.VK_FRONT_FACE_CLOCKWISE));
		pipelineRasterizationStateCreateInfo.depthClampEnable(orElse(depthClampEnable, false));
		pipelineRasterizationStateCreateInfo.rasterizerDiscardEnable(orElse(rasterizerDiscardEnable, false));
		pipelineRasterizationStateCreateInfo.lineWidth(orElse(lineWidth, 1F));
		pipelineRasterizationStateCreateInfo.depthBiasEnable(orElse(depthBiasEnable, false));
		
		VkPipelineMultisampleStateCreateInfo pipelineMultisampleStateCreateInfo = VkPipelineMultisampleStateCreateInfo.calloc(stack);
		pipelineMultisampleStateCreateInfo.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO);
		pipelineMultisampleStateCreateInfo.rasterizationSamples(orElse(rasterizationSamples, VK10.VK_SAMPLE_COUNT_1_BIT));
		pipelineMultisampleStateCreateInfo.sampleShadingEnable(orElse(sampleShadingEnable, false));
		pipelineMultisampleStateCreateInfo.minSampleShading(orElse(minSampleShading, 0F));
		pipelineMultisampleStateCreateInfo.alphaToCoverageEnable(orElse(alphaToCoverageEnable, false));
		pipelineMultisampleStateCreateInfo.alphaToOneEnable(orElse(alphaToOneEnable, false));
		
		VkPipelineColorBlendStateCreateInfo pipelineColorBlendStateCreateInfo = VkPipelineColorBlendStateCreateInfo.calloc(stack);
		pipelineColorBlendStateCreateInfo.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO);
		pipelineColorBlendStateCreateInfo.logicOpEnable(orElse(logicOpEnable, false));
		pipelineColorBlendStateCreateInfo.logicOp(orElse(logicOp, VK10.VK_LOGIC_OP_COPY));
		pipelineColorBlendStateCreateInfo.pAttachments(load(c -> VkPipelineColorBlendAttachmentState.calloc(c, stack), orElse(pipelineColorBlendAttachmentStates, VkPipelineColorBlendAttachmentState.calloc(stack).colorWriteMask(VK10.VK_COLOR_COMPONENT_R_BIT | VK10.VK_COLOR_COMPONENT_G_BIT | VK10.VK_COLOR_COMPONENT_B_BIT | VK10.VK_COLOR_COMPONENT_A_BIT).blendEnable(false))));
		pipelineColorBlendStateCreateInfo.blendConstants(load(blendConstants, new float[] {0, 0, 0, 0}));
		
		VkGraphicsPipelineCreateInfo.Buffer pipelineInfo = VkGraphicsPipelineCreateInfo.calloc(1, stack);
		pipelineInfo.sType(VK10.VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO);
		pipelineInfo.pInputAssemblyState(inputAssembly);
		pipelineInfo.pViewportState(viewportState);
		pipelineInfo.pRasterizationState(pipelineRasterizationStateCreateInfo);
		pipelineInfo.pMultisampleState(pipelineMultisampleStateCreateInfo);
		pipelineInfo.pColorBlendState(pipelineColorBlendStateCreateInfo);
		pipelineInfo.renderPass(renderPass.getVk());
		pipelineInfo.subpass(0);
		pipelineInfo.basePipelineHandle(VK10.VK_NULL_HANDLE);
		pipelineInfo.basePipelineIndex(-1);
		
		GraphicsPipeline.Builder builder = GraphicsPipeline.create();
		builder.autoCloseLayout(false);
		builder.autoCloseRenderPass(false);
		builder.vertexShader(vertexShader);
		builder.tessellationControlShader(tessellationControlShader);
		builder.tessellationEvaluationShader(tessellationEvaluationShader);
		builder.geometryShader(geometryShader);
		builder.fragmentShader(fragmentShader);
		builder.computeShader(computeShader);
		return builder.build(device, pipelineLayout, renderPass, pipelineInfo);
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
		FloatBuffer buffer = stack.callocFloat(array.length);
		for (int i = 0; i < array.length; i++) buffer.put(orElse(array[i], defaultValue[i]));
		return buffer.rewind();
	}
	
	protected boolean isValid(Shader shader)
	{
		return shader != null && shader.isCompilationSuccessfull();
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
