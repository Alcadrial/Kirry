package com.alcadrial.renderengine.render.window;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallbackI;
import org.lwjgl.glfw.GLFWCharModsCallbackI;
import org.lwjgl.glfw.GLFWCursorEnterCallbackI;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWDropCallbackI;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWScrollCallbackI;
import org.lwjgl.glfw.GLFWWindowCloseCallbackI;
import org.lwjgl.glfw.GLFWWindowContentScaleCallbackI;
import org.lwjgl.glfw.GLFWWindowFocusCallbackI;
import org.lwjgl.glfw.GLFWWindowIconifyCallbackI;
import org.lwjgl.glfw.GLFWWindowMaximizeCallbackI;
import org.lwjgl.glfw.GLFWWindowPosCallbackI;
import org.lwjgl.glfw.GLFWWindowRefreshCallbackI;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import org.lwjgl.system.CallbackI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSubpassDependency;
import org.lwjgl.vulkan.VkSubpassDescription;

import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.renderengine.core.Window;
import com.alcadrial.renderengine.core.util.Pair;
import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.FrameBuffer;
import com.alcadrial.renderengine.render.PhysicalDevice;
import com.alcadrial.renderengine.render.PhysicalDevice.PhysicalDevicePickFilter;
import com.alcadrial.renderengine.render.PhysicalDevice.PhysicalDevicePickStrategy;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.Renderer;
import com.alcadrial.renderengine.render.Surface;
import com.alcadrial.renderengine.render.Vulkan;
import com.alcadrial.renderengine.render.commandbuffer.CommandBuffer;
import com.alcadrial.renderengine.render.renderpass.RenderPass;
import com.alcadrial.renderengine.render.shader.ShaderProgram;
import com.alcadrial.renderengine.render.texture.Texture;
import com.alcadrial.renderengine.render.util.PhysicalDeviceNotFoundException;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.VulkanResource;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.util.functional.VulkanConsumer;
import com.alcadrial.renderengine.render.window.Presenter.Result;

public class VulkanWindow extends Window implements VulkanResource<Long> {
	
	private static final int BUFFERED_FRAMES_COUNT = 3;
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public static class Builder {
		
		private PhysicalDevicePickFilter filter;
		private PhysicalDevicePickStrategy strategy;
		private VkPhysicalDeviceFeatures deviceFeatures;
		private String deviceExtensions[];
		private boolean defaultPrimary;
		private boolean secondary;
		private int width;
		private int height;
		private @Nullable String title;
		private boolean useDepthTexture;
		private long vk;
		private @Nullable VulkanWindow window;
		
		public Builder()
		{
			filter = PhysicalDevice.Filter.any();
			strategy = PhysicalDevice.Strategy.findBest();
			deviceFeatures = VkPhysicalDeviceFeatures.calloc();
			deviceExtensions = new String[0];
			defaultPrimary = false;
			secondary = false;
			width = -1;
			height = -1;
			title = null;
			useDepthTexture = true;
			vk = 0;
			window = null;
		}
		
		public Builder physicalDeviceFilter(PhysicalDevicePickFilter filter)
		{
			this.filter = filter;
			return this;
		}
		
		public Builder physicalDeviceStrategy(PhysicalDevicePickStrategy strategy)
		{
			this.strategy = strategy;
			return this;
		}
		
		public Builder deviceFeatures(Consumer<VkPhysicalDeviceFeatures> consumer)
		{
			consumer.accept(deviceFeatures);
			return this;
		}
		
		public Builder deviceExtensions(String... deviceExtensions)
		{
			this.deviceExtensions = deviceExtensions;
			return this;
		}
		
		public Builder asDefaultPrimary()
		{
			defaultPrimary = true;
			return this;
		}
		
		public Builder asPrimary()
		{
			secondary = true;
			return this;
		}
		
		public Builder asSecondary()
		{
			secondary = false;
			return this;
		}
		
		public Builder size(int width, int height)
		{
			this.width = width;
			this.height = height;
			return this;
		}
		
		public Builder title(String title)
		{
			this.title = title;
			return this;
		}
		
		public void useDepthTexture(boolean useDepthTexture)
		{
			this.useDepthTexture = useDepthTexture;
		}
		
		/**
		 * <b>WARNING:</b> the window hint {@link GLFW#GLFW_CLIENT_API CLIENT_API} is set to {@link GLFW#GLFW_NO_API NO_API}
		 * using {@link RenderEngine#setWindowHint(int, int) RenderEngine#setWindowHint}
		 * before creating the window and is never restored to the previous value if different.
		 * <p>
		 * Must be called from the main thread if single threaded otherwise it will block waiting for the main thread.
		 * @throws VulkanException 
		 */
		public VulkanWindow build() throws VulkanException
		{
			String title = VulkanErrorUtils.nonNull(this.title, "The window's tile must be set");
			Vulkan vulkan = RenderEngine.getVulkan();
			
			RenderEngine.setWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
			vk = VulkanErrorUtils.nonNull(RenderEngine.awaitRequestMainThread(() -> GLFW.glfwCreateWindow(width, height, title, 0, 0)), "Cannot create window");
			
			Surface.Builder surface = Surface.builder().windowVk(vk).create();
			vulkan.setPhysicalDeviceFilter(p -> filter.filter(p, surface));
			try
			{
				vulkan.pickPhysicalDevices(strategy.pick(vulkan.getAvailablePhysicalDevices()));
			}
			catch (PhysicalDeviceNotFoundException e)
			{
				throw new VulkanException("A broken PhysicalDevicePickStrategy has been used", e);
			}
			Device device = vulkan.createDevice(deviceFeatures, deviceExtensions);
			VulkanWindow window = new VulkanWindow(this, vk, title, device, surface);
			if (defaultPrimary) RenderEngine.getVulkan().setDefaultWindow(window);
			if (!secondary) RenderEngine.getVulkan().setWindow(window);
			this.window = window;
			return window;
		}
		
		public long getVk()
		{
			return vk;
		}
		
		public @Nullable VulkanWindow getBuild()
		{
			return window;
		}
	}
	
	public static Builder builder()
	{
		return new Builder();
	}
	
	private final Device device;
	private final Surface surface;
	private int width;
	private int height;
	private String title;
	private final @Nullable Texture depthTexture;
	private boolean isIconified;
	private final List<VulkanConsumer<Surface>> surfaceUpdateActions;
	private Screen screen;
	private Presenter presenter;
	private Renderer renderer;
	private ShaderProgram shaderProgram;
	private RenderPass renderPass;
	private @Nullable Function<VulkanException, Boolean> exceptionHandler;
	private final List<CommandBuffer> commandBuffers;
	private int drawCount;
	private @Nullable String name;
	
	private VulkanWindow(Builder builder, long windowId, String title, Device device, Surface.Builder surfaceBuilder) throws VulkanException
	{
		VulkanErrorUtils.assertStatement(builder.width >= 0, "The window's sizes must not be negative");
		VulkanErrorUtils.assertStatement(builder.height >= 0, "The window's sizes must not be negative");
		
		this.id = windowId;
		if (builder.width == 0 || builder.height == 0)
		{
			int[] p0 = builder.width == 0 ? new int[1] : null;
			int[] p1 = builder.height == 0 ? new int[1] : null;
			GLFW.glfwGetWindowSize(id, p0, p1);
			if (builder.width == 0) this.width = ((int @NonNull []) p0)[0];
			if (builder.height == 0) this.height = ((int @NonNull []) p1)[0];
		}
		else
		{
			this.width = builder.width;
			this.height = builder.height;
		}
		
		this.title = title;
		this.device = device;
		surface = surfaceBuilder.window(this).build();
		surfaceUpdateActions = new LinkedList<>();
		resetCallbacks();
		shaderProgram = ShaderProgram.DEFAULT;
		drawCount = 0;
		depthTexture = builder.useDepthTexture ? createDepthTexture() : null;
		renderPass = createRenderPass();
		presenter = new Presenter(device, this, renderPass, BUFFERED_FRAMES_COUNT);
		renderer = Renderer.create()
			.shaderProgram(shaderProgram)
			.build(device, surface.getSize(), renderPass);
		commandBuffers = device.createMultiUseCommandBuffer(BUFFERED_FRAMES_COUNT);
		for (CommandBuffer commandBuffer : commandBuffers) commandBuffer.beginRecording();
		
		prepareFrameBuffers();
		presenter.startPresentation();
		
		screen = BlackScreen.BLACK_SCREEN;
	}
	
	public void setName(String name)
	{
		this.name = name;
		device.setObjectDebugName(surface, "Surface(" + name + ")");
		presenter.setName("Presenter(" + name + ")");
		renderer.setName("Renderer(" + name + ")");
		if (depthTexture != null) depthTexture.setName("DepthTexture(" + name + ")");
		for (CommandBuffer commandBuffer : commandBuffers) commandBuffer.setName("CommandBuffer (" + name + ")");
	}
	
	public void update() throws VulkanException
	{
		Screen screen = this.screen;
		CommandBuffer commandBuffer = commandBuffers.get(getFrameIndex());
		/*
		if (!screen.isInitialized()) screen.initializeRendering(null);//TODO
		if (!screen.isRendered()) screen.render(new Matrix4f().orthographicPositive(width, height), renderer);
		if (drawCount++ < 3)//WIP
		{
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				VkRenderPassBeginInfo renderPassBeginInfo = VkRenderPassBeginInfo.malloc(stack);
				renderPassBeginInfo.sType(VK10.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
				renderPassBeginInfo.pNext(0);
				
				renderPassBeginInfo.renderPass(screen.getRenderPass().getVk());
				
				VkClearValue.Buffer clearValues = VkClearValue.calloc(2, stack);
				clearValues.get().color().float32(stack.floats(0, 0, 0, 0));
				clearValues.get().depthStencil().depth(1).stencil(0);
				renderPassBeginInfo.pClearValues(clearValues.rewind());
				
				FrameBuffer frameBuffer = presenter.getFrameBuffer(getFrameIndex());
				renderPassBeginInfo.framebuffer(frameBuffer.getVk());
				
				VkRect2D renderArea = VkRect2D.calloc(stack);
				renderArea.extent(VkExtent2D.malloc(stack).set(frameBuffer.getWidth(), frameBuffer.getHeight()));
				renderPassBeginInfo.renderArea(renderArea);
				
				commandBuffer.beginRenderPass(renderPassBeginInfo, VK10.VK_SUBPASS_CONTENTS_INLINE);
			}
			
			commandBuffer.setScissor(0, 0, getWidth(), getHeight());
			commandBuffer.setViewport(0, getHeight(), getWidth(), -getHeight(), -1000, 1000);
			
			screen.draw(Matrix4f.IDENTITY, commandBuffer, renderer, 0);
			commandBuffer.endRenderPass();
		}
		*/
		commandBuffer.endRecording();
		swapBuffers(commandBuffer);
		commandBuffer.beginRecording();
	}
	
	public void setShouldClose(boolean value)
	{
		GLFW.glfwSetWindowShouldClose(id, value);
	}
	
	public void swapBuffers(CommandBuffer commandBuffer) throws VulkanException
	{
		Result r = presenter.endPresentation(commandBuffer);
		if (r == Result.FAILED) throw new VulkanException("Failed to end presentation");
		if (r != Result.SUCCESS) Debug.debug("Swap buffers status: " + r);
		r = presenter.startPresentation();
		if (r == Result.FAILED) throw new VulkanException("Failed to start presentation");
		if (r != Result.SUCCESS) Debug.debug("Swap buffers status: " + r);
	}
	
	public void swapBuffers(CommandBuffer commandBuffer, Runnable onCommandBufferEndAction) throws VulkanException
	{
		presenter.endPresentation(commandBuffer, onCommandBufferEndAction);
		presenter.startPresentation();
	}
	
	public void addSurfaceUpdateActions(VulkanConsumer<Surface> action)
	{
		surfaceUpdateActions.add(action);
	}
	
	public void removeSurfaceUpdateActions(VulkanConsumer<Surface> action)
	{
		surfaceUpdateActions.remove(action);
	}
	
	private RenderPass createRenderPass() throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkAttachmentDescription.Buffer attachmentDescriptions = VkAttachmentDescription.calloc(depthTexture != null ? 2 : 1, stack);
			
			VkAttachmentDescription colorAttachment = attachmentDescriptions.get();
			colorAttachment.format(surface.getFormat().format());
			colorAttachment.samples(VK10.VK_SAMPLE_COUNT_1_BIT);
			colorAttachment.loadOp(VK10.VK_ATTACHMENT_LOAD_OP_CLEAR);
			colorAttachment.storeOp(VK10.VK_ATTACHMENT_STORE_OP_STORE);
			colorAttachment.stencilLoadOp(VK10.VK_ATTACHMENT_LOAD_OP_DONT_CARE);
			colorAttachment.stencilStoreOp(VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE);
			colorAttachment.initialLayout(VK10.VK_IMAGE_LAYOUT_UNDEFINED);
			colorAttachment.finalLayout(VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
			
			VkAttachmentReference.Buffer colorAttachmentRef = VkAttachmentReference.calloc(1, stack);
			colorAttachmentRef.attachment(0);
			colorAttachmentRef.layout(VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
			
			VkSubpassDescription.Buffer subpassDescription = VkSubpassDescription.calloc(1, stack);
			subpassDescription.pipelineBindPoint(VK10.VK_PIPELINE_BIND_POINT_GRAPHICS);
			subpassDescription.colorAttachmentCount(1);
			subpassDescription.pColorAttachments(colorAttachmentRef);
			
			int srcStageMask = VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
			int srcAccessMask = 0;
			int dstStageMask = VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
			int dstAccessMask = VK10.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT;
			
			if (depthTexture != null)
			{
				VkAttachmentDescription depthAttachment = attachmentDescriptions.get();
				depthAttachment.format(VK10.VK_FORMAT_D24_UNORM_S8_UINT);//dynamic
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
				
				subpassDescription.pDepthStencilAttachment(depthAttachmentRef);
				
				srcStageMask |= VK10.VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT;
				dstStageMask |= VK10.VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT;
				dstAccessMask |= VK10.VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT;
			}
			
			VkSubpassDependency.Buffer subpassDependency = VkSubpassDependency.calloc(1, stack);
			subpassDependency.srcSubpass(VK10.VK_SUBPASS_EXTERNAL);
			subpassDependency.dstSubpass(0);
			subpassDependency.srcStageMask(srcStageMask);
			subpassDependency.srcAccessMask(srcAccessMask);
			subpassDependency.dstStageMask(dstStageMask);
			subpassDependency.dstAccessMask(dstAccessMask);
			
			VkRenderPassCreateInfo renderPassCreateInfo = VkRenderPassCreateInfo.calloc(stack);
			renderPassCreateInfo.sType(VK10.VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO);
			renderPassCreateInfo.pAttachments(attachmentDescriptions.rewind());
			renderPassCreateInfo.pSubpasses(subpassDescription);
			renderPassCreateInfo.pDependencies(subpassDependency);
			
			return new RenderPass(device, renderPassCreateInfo);
		}
	}
	
	private @Nullable Texture createDepthTexture() throws VulkanException
	{
		VkExtent2D extent = surface.getExtent();
		Texture depthTexture = Texture.create2dTexture(device, extent.width(), extent.height(), VK10.VK_FORMAT_D24_UNORM_S8_UINT, VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT, VK10.VK_IMAGE_ASPECT_DEPTH_BIT | VK10.VK_IMAGE_ASPECT_STENCIL_BIT);
		depthTexture.changeLayout(VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL).join();
		if (name != null) depthTexture.setName("DepthTexture (" + name + ")");
		return depthTexture;
	}
	
	private void prepareFrameBuffers() throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			presenter.initialize(depthTexture);
			VkRenderPassBeginInfo renderPassBeginInfo = VkRenderPassBeginInfo.malloc(stack);
			renderPassBeginInfo.sType(VK10.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
			renderPassBeginInfo.pNext(0);
			
			renderPassBeginInfo.renderPass(renderPass.getVk());
			
			VkClearValue.Buffer clearValues = VkClearValue.calloc(2, stack);
			clearValues.get().color().float32(stack.floats(0, 0, 0, 0));
			clearValues.get().depthStencil().depth(1).stencil(0);
			renderPassBeginInfo.pClearValues(clearValues.rewind());
			
			for (int i = 0; i < commandBuffers.size(); i++)
			{
				CommandBuffer commandBuffer = commandBuffers.get(i);
				FrameBuffer frameBuffer = presenter.getFrameBuffer(i);
				renderPassBeginInfo.framebuffer(frameBuffer.getVk());
				
				VkRect2D renderArea = VkRect2D.calloc(stack);
				renderArea.extent(VkExtent2D.malloc(stack).set(frameBuffer.getWidth(), frameBuffer.getHeight()));
				renderPassBeginInfo.renderArea(renderArea);
				
				commandBuffer.beginRenderPass(renderPassBeginInfo, VK10.VK_SUBPASS_CONTENTS_INLINE);
				commandBuffer.endRenderPass();
			}
		}
	}
	
	@Override
	public void pollEvents()
	{
		RenderEngine.requestMainThread(GLFW::glfwPollEvents);
	}
	
	public final void resetCallbacks()
	{
		RenderEngine.awaitRequestMainThread(() -> {
			GLFW.glfwSetCharCallback(id, null);
			GLFW.glfwSetCharModsCallback(id, null);
			GLFW.glfwSetCursorEnterCallback(id, null);
			GLFW.glfwSetCursorPosCallback(id, null);
			GLFW.glfwSetDropCallback(id, null);
			GLFW.glfwSetKeyCallback(id, null);
			GLFW.glfwSetMouseButtonCallback(id, null);
			GLFW.glfwSetScrollCallback(id, null);
			GLFW.glfwSetWindowCloseCallback(id, w -> onHide());
			GLFW.glfwSetWindowContentScaleCallback(id, null);
			GLFW.glfwSetWindowFocusCallback(id, (w, f) -> onFocus());
			GLFW.glfwSetWindowIconifyCallback(id, (w, c) -> {
				isIconified = c;
				if (c) onIconify();
				else onRestore();
			});
			GLFW.glfwSetWindowMaximizeCallback(id, (w, m) -> {
				if (m) onMaximize();
				else onRestore();
			});
			GLFW.glfwSetWindowPosCallback(id, null);
			GLFW.glfwSetWindowRefreshCallback(id, null);
			GLFW.glfwSetWindowSizeCallback(id, (p, w, h) -> {
				width = w;
				height = h;
			});
			GLFW.glfwSetFramebufferSizeCallback(id, (p, w, h) -> {
				if (w != 0)
				{
					try
					{
						surface.reinit();
						prepareFrameBuffers();
					}
					catch (VulkanException e)
					{
						if (exceptionHandler != null && exceptionHandler.apply(e))
						{
							RenderEngine.terminate();
							return;
						}
					}
				}
				for (VulkanConsumer<Surface> action : surfaceUpdateActions)
				{
					try
					{
						action.accept(surface);
					}
					catch (VulkanException e)
					{
						if (exceptionHandler != null && exceptionHandler.apply(e))
						{
							RenderEngine.terminate();
							return;
						}
					}
				}
			});
		});
	}
	
	public final void setCallback(CallbackI callback)
	{
		RenderEngine.awaitRequestMainThread(() -> {
			if (callback instanceof GLFWCharCallbackI i) GLFW.glfwSetCharCallback(id, i);
			if (callback instanceof GLFWCharModsCallbackI i) GLFW.glfwSetCharModsCallback(id, i);
			if (callback instanceof GLFWCursorEnterCallbackI i) GLFW.glfwSetCursorEnterCallback(id, i);
			if (callback instanceof GLFWCursorPosCallbackI i) GLFW.glfwSetCursorPosCallback(id, i);
			if (callback instanceof GLFWDropCallbackI i) GLFW.glfwSetDropCallback(id, i);
			if (callback instanceof GLFWKeyCallbackI i) GLFW.glfwSetKeyCallback(id, i);
			if (callback instanceof GLFWMouseButtonCallbackI i) GLFW.glfwSetMouseButtonCallback(id, i);
			if (callback instanceof GLFWScrollCallbackI i) GLFW.glfwSetScrollCallback(id, i);
			if (callback instanceof GLFWWindowCloseCallbackI i) GLFW.glfwSetWindowCloseCallback(id, w -> {
				onHide();
				i.invoke(w);
			});
			if (callback instanceof GLFWWindowContentScaleCallbackI i) GLFW.glfwSetWindowContentScaleCallback(id, i);
			if (callback instanceof GLFWWindowFocusCallbackI i) GLFW.glfwSetWindowFocusCallback(id, (w, f) -> {
				onFocus();
				i.invoke(w, f);
			});
			if (callback instanceof GLFWWindowIconifyCallbackI i) GLFW.glfwSetWindowIconifyCallback(id, (w, c) -> {
				isIconified = c;
				if (c) onIconify();
				else onRestore();
				i.invoke(w, c);
			});
			if (callback instanceof GLFWWindowMaximizeCallbackI i) GLFW.glfwSetWindowMaximizeCallback(id, (w, m) -> {
				if (m) onMaximize();
				else onRestore();
				i.invoke(w, m);
			});
			if (callback instanceof GLFWWindowPosCallbackI i) GLFW.glfwSetWindowPosCallback(id, i);
			if (callback instanceof GLFWWindowRefreshCallbackI i) GLFW.glfwSetWindowRefreshCallback(id, i);
			if (callback instanceof GLFWWindowSizeCallbackI i) GLFW.glfwSetWindowSizeCallback(id, (p, w, h) -> {
				width = w;
				height = h;
				i.invoke(p, w, h);
			});
			if (callback instanceof GLFWFramebufferSizeCallbackI i) GLFW.glfwSetFramebufferSizeCallback(id, (p, w, h) -> {
				try
				{
					surface.reinit();
				}
				catch (VulkanException e)
				{
					if (exceptionHandler != null && exceptionHandler.apply(e))
					{
						RenderEngine.terminate();
						return;
					}
				}
				for (VulkanConsumer<Surface> action : surfaceUpdateActions)
				{
					try
					{
						action.accept(surface);
					}
					catch (VulkanException e)
					{
						if (exceptionHandler != null && exceptionHandler.apply(e))
						{
							RenderEngine.terminate();
							return;
						}
					}
				}
				i.invoke(p, w, h);
			});
		});
	}
	
	public void setExceptionHandler(Function<VulkanException, Boolean> exceptionHandler)
	{
		this.exceptionHandler = exceptionHandler;
	}
	
	public void setScreen(Screen screen)
	{
		this.screen = Objects.requireNonNull(screen);
	}
	
	public Screen getScreen()
	{
		return screen;
	}
	
	public final Vector2f getMousePos(Vector2f holder)
	{
		double[][] p = new double[2][1];
		RenderEngine.awaitRequestMainThread(() -> GLFW.glfwGetCursorPos(id, p[0], p[1]));
		return holder.setAll((float) p[0][0], (float) p[1][0]);
	}
	
	public final Vector2f getMousePos()
	{
		double[][] p = new double[2][1];
		RenderEngine.awaitRequestMainThread(() -> GLFW.glfwGetCursorPos(id, p[0], p[1]));
		return Vector2f.of((float) p[0][0], (float) p[1][0]);
	}
	
	public final Pair<Double, Double> getHighPrecisionMousePos(Pair<Double, Double> holder)
	{
		double[][] p = new double[2][1];
		RenderEngine.awaitRequestMainThread(() -> GLFW.glfwGetCursorPos(id, p[0], p[1]));
		holder.setLeft(p[0][0]);
		holder.setRight(p[1][0]);
		return holder;
	}
	
	public final Pair<Double, Double> getHighPrecisionMousePos()
	{
		double[][] p = new double[2][1];
		RenderEngine.awaitRequestMainThread(() -> GLFW.glfwGetCursorPos(id, p[0], p[1]));
		return new Pair<>(p[0][0], p[1][0]);
	}
	
	public final void setMousePos(Vector2f pos)
	{
		RenderEngine.requestMainThread(() -> GLFW.glfwSetCursorPos(id, pos.x(), pos.y()));
	}
	
	public final void setMousePos(Pair<Double, Double> pos)
	{
		RenderEngine.requestMainThread(() -> GLFW.glfwSetCursorPos(id, pos.l(), pos.r()));
	}
	
	public final Vector2i getPos(Vector2i holder)
	{
		int[][] p = new int[2][1];
		RenderEngine.awaitRequestMainThread(() -> GLFW.glfwGetWindowPos(id, p[0], p[1]));
		return holder.setAll(p[0][0], p[1][0]);
	}
	
	public final Vector2i getPos()
	{
		int[][] p = new int[2][1];
		RenderEngine.awaitRequestMainThread(() -> GLFW.glfwGetWindowPos(id, p[0], p[1]));
		return Vector2i.of(p[0][0], p[1][0]);
	}
	
	public final void setPos(Vector2i pos)
	{
		RenderEngine.requestMainThread(() -> GLFW.glfwSetWindowPos(id, pos.x(), pos.y()));
	}
	
	@Override
	public Long getVk()
	{
		return id;
	}
	
	public Device getDevice()
	{
		return device;
	}
	
	public boolean isIconified()
	{
		return isIconified;
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
	
	public String getTitle()
	{
		return title;
	}
	
	public void setTitle(String title)
	{
		RenderEngine.requestMainThread(() -> GLFW.glfwSetWindowTitle(id, title));
		this.title = title;
	}
	
	public int getFrameIndex()
	{
		return presenter.getFrameIndex();
	}
	
	public FrameBuffer getCurrentFrameBuffer()
	{
		return presenter.getFrameBuffer(getFrameIndex());
	}
	
	public void setShaderProgram(ShaderProgram shaderProgram)
	{
		this.shaderProgram = shaderProgram;
	}
	
	@Override
	public void close()
	{
		if (id != 0)
		{
			renderer.close();
			presenter.close();
			device.waitIdle();
			for (CommandBuffer commandBuffer : commandBuffers) commandBuffer.close();
			if (depthTexture != null) depthTexture.close();
			
			surface.close();
			RenderEngine.awaitRequestMainThread(() -> GLFW.glfwDestroyWindow(id));
			id = 0L;
		}
	}
	
	@Override
	public void debug()
	{
		Debug.print("Vk: " + Debug.address(id));
		Debug.print("Size: " + width + "x" + height);
		Debug.print("Surface: ", surface);
		Debug.print("Presenter: ", presenter);
		Debug.print("Renderer: ", renderer);
		Debug.print("ShaderProgram: " + shaderProgram.getshaders());
	}
}
