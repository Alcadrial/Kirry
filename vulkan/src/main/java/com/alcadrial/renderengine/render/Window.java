package com.alcadrial.renderengine.render;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSubpassDependency;
import org.lwjgl.vulkan.VkSubpassDescription;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;
import org.lwjgl.vulkan.VkViewport;

import com.alcadrial.renderengine.core.util.color.Color;
import com.alcadrial.renderengine.render.pipeline.GraphicsPipeline;
import com.alcadrial.renderengine.render.pipeline.PipelineLayout;
import com.alcadrial.renderengine.render.shader.Shader;
import com.alcadrial.renderengine.render.shader.ShaderType;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;

public class Window implements AutoCloseable {
	
	static
	{
		if (!RenderEngine.isInitializated()) throw new RuntimeException("Use of RenderEngine Vulkan without initialization, use RenderEngine#initialize before anything");
	}
	
	private Vulkan vulkan;
	private long id;
	private Surface surface;
	private int width;
	private int height;
	private SwapChain swapChain;
	private long renderPass;
	private GraphicsPipeline pipeline;
	private VertexBuffer vertexBuffer;
	
	private int cullMode;
	private Color clearColor;
	private int clockwiseFrontOrientation;
	private boolean rebuildPipeline;
	
	public Window(int width, int height, String title)
	{
		vulkan = RenderEngine.getVulkan();
		id = VulkanErrorUtils.nonNull(GLFW.glfwCreateWindow(width, height, title, 0, 0), "Cannot create window");
		this.width = width;
		this.height = height;
		surface = new Surface(vulkan, this);
		cullMode = VK10.VK_CULL_MODE_BACK_BIT;
		clearColor = Color.BLACK;
		clockwiseFrontOrientation = VK10.VK_FRONT_FACE_CLOCKWISE;
		rebuildPipeline = false;
	}
	
	public void initialize()
	{
		VulkanErrorUtils.nonNull(vulkan.getPhysicalDevice(), "Cannot initialize a window without a physical device, choose one with Vulkan#setPhysicalDeviceFilter and Vulkan#pickPhysicalDevices");
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			PhysicalDevice physicalDevice = vulkan.getPhysicalDevice();
			surface.init(physicalDevice, stack);
			
			VkAttachmentDescription.Buffer colorAttachment = VkAttachmentDescription.calloc(1, stack);
			colorAttachment.format(surface.getFormat().format());
			colorAttachment.samples(VK10.VK_SAMPLE_COUNT_1_BIT);
			colorAttachment.loadOp(VK10.VK_ATTACHMENT_LOAD_OP_CLEAR);
			colorAttachment.storeOp(VK10.VK_ATTACHMENT_STORE_OP_STORE);
			colorAttachment.stencilLoadOp(VK10.VK_ATTACHMENT_LOAD_OP_DONT_CARE);
			colorAttachment.stencilStoreOp(VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE);
			colorAttachment.initialLayout(VK10.VK_IMAGE_LAYOUT_UNDEFINED);
			colorAttachment.finalLayout(KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);
			
			VkAttachmentReference.Buffer colorAttachmentRef = VkAttachmentReference.calloc(1, stack);
			colorAttachmentRef.attachment(0);
			colorAttachmentRef.layout(VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
			
			VkSubpassDescription.Buffer subpass = VkSubpassDescription.calloc(1, stack);
			subpass.pipelineBindPoint(VK10.VK_PIPELINE_BIND_POINT_GRAPHICS);
			subpass.colorAttachmentCount(1);
			subpass.pColorAttachments(colorAttachmentRef);
			
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
			
			long[] p = new long[1];
			VulkanErrorUtils.assertNoError(VK10.vkCreateRenderPass(vulkan.getDevice().getDevice(), renderPassInfo, null, p), "Failed to create render pass");
			renderPass = p[0];
			
			vertexBuffer = new VertexBuffer(new float[] {
				-0.5F, -0.5F, 0, 1, 1, 1, 1,
				0.5F, -0.5F, 0, 1, 0, 0, 1,
				0.5F, 0.5F, 0, 0, 1, 0, 1,
				-0.5F, 0.5F, 0, 0, 0, 1, 1,
			});
			
			VkSurfaceCapabilitiesKHR capabilities = VkSurfaceCapabilitiesKHR.calloc(stack);
			VulkanErrorUtils.assertNoError(KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physicalDevice.getDevice(), surface.getSurface(), capabilities), "Failed to create swap chain");
			
			VulkanErrorUtils.assertStatement(capabilities.maxImageCount() >= 2, "Insufficient images for double-buffer");
			
			VkSwapchainCreateInfoKHR createInfo = VkSwapchainCreateInfoKHR.calloc(stack);
			createInfo.sType(KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR);
			createInfo.surface(surface.getSurface());
			createInfo.minImageCount(2);
			createInfo.imageFormat(surface.getFormat().format());
			createInfo.imageColorSpace(surface.getFormat().colorSpace());
			createInfo.imageExtent(surface.getExtent());
			createInfo.imageArrayLayers(1);
			createInfo.imageUsage(VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);
			if (physicalDevice.getQueueGraphicsFamilyIndex() == physicalDevice.getQueueSurfaceFamilyIndex()) createInfo.imageSharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);
			else
			{
				createInfo.imageSharingMode(VK10.VK_SHARING_MODE_CONCURRENT);
				createInfo.pQueueFamilyIndices(stack.ints(physicalDevice.getQueueGraphicsFamilyIndex(), physicalDevice.getQueueSurfaceFamilyIndex()));
			}
			createInfo.preTransform(capabilities.currentTransform());
			createInfo.compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
			createInfo.presentMode(surface.getMode());
			createInfo.clipped(true);
			createInfo.oldSwapchain(VK10.VK_NULL_HANDLE);
			
			VulkanErrorUtils.assertNoError(KHRSwapchain.vkCreateSwapchainKHR(vulkan.getDevice().getDevice(), createInfo, null, p), "Failed to create swap chain");
			swapChain = new SwapChain(surface, p[0]);
		}
	}
	
	public void buildGraphicsPipeline(Shader vertexShader, Shader fragmentShader)
	{
		VulkanErrorUtils.nonNull(swapChain, "Cannot build the graphics pipeline without being initialized, use Window#initialize");
		VulkanErrorUtils.match(vertexShader.getType(), ShaderType.VERTEX, "Not a vertex shader");
		VulkanErrorUtils.match(fragmentShader.getType(), ShaderType.FRAGMENT, "Not a fragment shader");
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkPipelineInputAssemblyStateCreateInfo inputAssembly = VkPipelineInputAssemblyStateCreateInfo.calloc(stack);
			inputAssembly.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);
			inputAssembly.topology(VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_FAN);//MONO QUAD
			inputAssembly.primitiveRestartEnable(false);
			
			VkViewport.Buffer viewport = VkViewport.calloc(1, stack);
			viewport.x(0);
			viewport.y(0);
			viewport.width(surface.getExtent().width());
			viewport.height(surface.getExtent().height());
			viewport.minDepth(0);
			viewport.maxDepth(1);
			
			VkRect2D.Buffer scissor = VkRect2D.calloc(1, stack);
			scissor.offset(VkOffset2D.calloc(stack).set(0, 0));
			scissor.extent(surface.getExtent());
			
			VkPipelineViewportStateCreateInfo viewportState = VkPipelineViewportStateCreateInfo.calloc(stack);
			viewportState.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO);
			viewportState.pViewports(viewport);
			viewportState.pScissors(scissor);
			
			VkPipelineRasterizationStateCreateInfo rasterizer = VkPipelineRasterizationStateCreateInfo.calloc(stack);
			rasterizer.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO);
			rasterizer.depthClampEnable(false);
			rasterizer.rasterizerDiscardEnable(false);
			rasterizer.polygonMode(VK10.VK_POLYGON_MODE_FILL);
			rasterizer.lineWidth(1);
			rasterizer.cullMode(cullMode);
			rasterizer.frontFace(clockwiseFrontOrientation);
			rasterizer.depthBiasEnable(false);
			
			VkPipelineMultisampleStateCreateInfo multisampling = VkPipelineMultisampleStateCreateInfo.calloc(stack);
			multisampling.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO);
			multisampling.sampleShadingEnable(false);
			multisampling.rasterizationSamples(VK10.VK_SAMPLE_COUNT_1_BIT);
			
			VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachment = VkPipelineColorBlendAttachmentState.calloc(1, stack);
			colorBlendAttachment.colorWriteMask(VK10.VK_COLOR_COMPONENT_R_BIT | VK10.VK_COLOR_COMPONENT_G_BIT | VK10.VK_COLOR_COMPONENT_B_BIT | VK10.VK_COLOR_COMPONENT_A_BIT);
			colorBlendAttachment.blendEnable(false);
			
			VkPipelineColorBlendStateCreateInfo colorBlending = VkPipelineColorBlendStateCreateInfo.calloc(stack);
			colorBlending.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO);
			colorBlending.logicOpEnable(false);
			colorBlending.logicOp(VK10.VK_LOGIC_OP_COPY);
			colorBlending.pAttachments(colorBlendAttachment);
			colorBlending.blendConstants(stack.floats(0, 0, 0, 0));
			
			PipelineLayout pipelineLayout = new PipelineLayout(vulkan.getDevice(), VkPipelineLayoutCreateInfo.calloc(stack));
			
			VkGraphicsPipelineCreateInfo.Buffer pipelineInfo = VkGraphicsPipelineCreateInfo.calloc(1, stack);
			pipelineInfo.sType(VK10.VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO);
			pipelineInfo.pInputAssemblyState(inputAssembly);
			pipelineInfo.pViewportState(viewportState);
			pipelineInfo.pRasterizationState(rasterizer);
			pipelineInfo.pMultisampleState(multisampling);
			pipelineInfo.pColorBlendState(colorBlending);
			pipelineInfo.renderPass(renderPass);
			pipelineInfo.subpass(0);
			pipelineInfo.basePipelineHandle(VK10.VK_NULL_HANDLE);
			pipelineInfo.basePipelineIndex(-1);
			
			pipeline = GraphicsPipeline.create().shader(vertexShader).shader(fragmentShader).build(vulkan.getDevice(), pipelineLayout, pipelineInfo);
			
			swapChain.startRegisteringCommands();
		}
	}
	
	public void update()
	{
		if (rebuildPipeline)
		{
			
		}
	}
	
	public void enablePipelineBackCulling()
	{
		cullMode |= VK10.VK_CULL_MODE_BACK_BIT;
	}
	
	public void enablePipelineFrontCulling()
	{
		cullMode |= VK10.VK_CULL_MODE_FRONT_BIT;
	}
	
	public void disablePipelineBackCulling()
	{
		cullMode &= VK10.VK_CULL_MODE_FRONT_BIT;
	}
	
	public void disablePipelineFrontCulling()
	{
		cullMode &= VK10.VK_CULL_MODE_BACK_BIT;
	}
	
	public void setPipelineFrontOrientation(boolean clockwise)
	{
		clockwiseFrontOrientation = clockwise ? VK10.VK_FRONT_FACE_CLOCKWISE : VK10.VK_FRONT_FACE_COUNTER_CLOCKWISE;
	}
	
	public void invalidatePipeline()
	{
		rebuildPipeline = true;
	}
	
	public final boolean shouldClose()
	{
		return GLFW.glfwWindowShouldClose(id);
	}
	
	public final void show()
	{
		GLFW.glfwShowWindow(id);
		onShow();
	}
	
	protected void onShow()
	{
		
	}
	
	public final void hide()
	{
		GLFW.glfwHideWindow(id);
		onHide();
	}
	
	protected void onHide()
	{
		
	}
	
	public final void iconify()
	{
		GLFW.glfwIconifyWindow(id);
		onIconify();
	}
	
	protected void onIconify()
	{
		
	}
	
	public final void maximize()
	{
		GLFW.glfwMaximizeWindow(id);
		onMaximize();
	}
	
	protected void onMaximize()
	{
		
	}
	
	public final void restore()
	{
		GLFW.glfwRestoreWindow(id);
		onRestore();
	}
	
	protected void onRestore()
	{
		
	}
	
	public final void focus()
	{
		GLFW.glfwFocusWindow(id);
	}
	
	protected void onFocus()
	{
		
	}
	
	public final void requestAttention()
	{
		GLFW.glfwRequestWindowAttention(id);
	}
	
	public final void pollEvents()
	{
		GLFW.glfwPollEvents();
	}
	
	public long getId()
	{
		return id;
	}
	
	public Surface getSurface()
	{
		return surface;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public VertexBuffer getVertexBuffer()
	{
		return vertexBuffer;
	}
	
	public SwapChain getSwapChain()
	{
		return swapChain;
	}
	
	public long getRenderPass()
	{
		return renderPass;
	}
	
	public GraphicsPipeline getPipeline()
	{
		return pipeline;
	}
	
	public Color getClearColor()
	{
		return clearColor;
	}
	
	public void setClearColor(Color clearColor)
	{
		this.clearColor = clearColor;
		swapChain.setClearColor(clearColor);
	}
	
	@Override
	public void close()
	{
		swapChain.close();
		vertexBuffer.close();
		if (pipeline != null) pipeline.close();
		VK10.vkDestroyRenderPass(vulkan.getDevice().getDevice(), renderPass, null);
		surface.close();
		GLFW.glfwDestroyWindow(id);
	}
}
