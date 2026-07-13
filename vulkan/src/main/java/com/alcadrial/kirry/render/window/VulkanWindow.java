package com.alcadrial.kirry.render.window;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

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
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.EXTFullScreenExclusive;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2d;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2fr;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector2ir;
import com.alcadrial.kirry.core.Window;
import com.alcadrial.kirry.core.resources.ResourceManager;
import com.alcadrial.kirry.core.util.Pair;
import com.alcadrial.kirry.render.Debug;
import com.alcadrial.kirry.render.Device;
import com.alcadrial.kirry.render.FrameBuffer;
import com.alcadrial.kirry.render.KirryVulkan;
import com.alcadrial.kirry.render.PhysicalDevice;
import com.alcadrial.kirry.render.PhysicalDevice.PhysicalDevicePickFilter;
import com.alcadrial.kirry.render.PhysicalDevice.PhysicalDevicePickStrategy;
import com.alcadrial.kirry.render.Surface;
import com.alcadrial.kirry.render.Surface.SurfaceData;
import com.alcadrial.kirry.render.Swapchain;
import com.alcadrial.kirry.render.Vulkan;
import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;
import com.alcadrial.kirry.render.memory.MemoryManager;
import com.alcadrial.kirry.render.queue.PresentQueue;
import com.alcadrial.kirry.render.renderpass.RenderPass;
import com.alcadrial.kirry.render.renderpass.RenderPassTemplate;
import com.alcadrial.kirry.render.texture.Texture;
import com.alcadrial.kirry.render.util.VulkanErrorUtils;
import com.alcadrial.kirry.render.util.VulkanResource;
import com.alcadrial.kirry.render.util.VulkanUtils;
import com.alcadrial.kirry.render.util.exception.PhysicalDeviceNotFoundException;
import com.alcadrial.kirry.render.util.exception.VulkanException;
import com.alcadrial.kirry.render.util.functional.VulkanConsumer;
import com.alcadrial.kirry.render.util.future.VulkanFuture;
import com.alcadrial.kirry.render.window.event.EventType;

public class VulkanWindow extends Window implements VulkanResource<Long> {
	
	private static final int BUFFERED_FRAMES_COUNT = 3;
	
	static
	{
		KirryVulkan.assertLoaded();
	}
	
	public static class Builder {
		
		private PhysicalDevicePickFilter filter;
		private PhysicalDevicePickStrategy strategy;
		private VkPhysicalDeviceFeatures deviceFeatures;
		private String deviceExtensions[];
		private boolean defaultPrimary;
		private boolean secondary;
		private boolean useDepthTexture;
		private int sampleCount;
		private long vk;
		private @Nullable VulkanWindow window;
		private @Nullable MemoryManager memoryManager;
		private @Nullable ResourceManager resourceManager;
		
		public Builder()
		{
			filter = PhysicalDevice.Filter.any();
			strategy = PhysicalDevice.Strategy.findBest();
			deviceFeatures = VkPhysicalDeviceFeatures.calloc();
			deviceExtensions = new String[0];
			defaultPrimary = false;
			secondary = false;
			useDepthTexture = true;
			sampleCount = VK10.VK_SAMPLE_COUNT_1_BIT;
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
		
		public Builder deviceExtensionsWithDefaults(String... deviceExtensions)
		{
			this.deviceExtensions = Arrays.copyOf(deviceExtensions, deviceExtensions.length + 1);
			this.deviceExtensions[deviceExtensions.length] = KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
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
		
		public Builder useDepthTexture(boolean useDepthTexture)
		{
			this.useDepthTexture = useDepthTexture;
			return this;
		}
		
		public Builder sampleCount(int sampleCount)
		{
			this.sampleCount = sampleCount;
			return this;
		}
		
		public Builder memoryManager(MemoryManager memoryManager)
		{
			this.memoryManager = memoryManager;
			return this;
		}
		
		public Builder resourceManager(ResourceManager resourceManager)
		{
			this.resourceManager = resourceManager;
			return this;
		}
		
		/**
		 * <b>WARNING:</b> the window hint {@link GLFW#GLFW_CLIENT_API CLIENT_API} is set to {@link GLFW#GLFW_NO_API NO_API}
		 * using {@link KirryVulkan#setWindowHint(int, int) KirryVulkan#setWindowHint}
		 * before creating the window and is never restored to the previous value if different.
		 * <p>
		 * Must be called from the main thread if single threaded otherwise it will block waiting for the main thread.
		 * @throws VulkanException
		 */
		public VulkanWindow build(int width, int height, String title) throws VulkanException
		{
			Vulkan vulkan = KirryVulkan.getVulkan();
			
			KirryVulkan.setWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
			KirryVulkan.setWindowHint(GLFW.GLFW_SAMPLES, sampleCount);
			vk = VulkanErrorUtils.nonNull(KirryVulkan.awaitRequestMainThread(() -> GLFW.glfwCreateWindow(width, height, title, 0, 0)), "Cannot create window");
			
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
			Device device = vulkan.createDevice(deviceFeatures, surface, deviceExtensions);
			VulkanWindow window = new VulkanWindow(this, vk, title, device, surface);
			if (defaultPrimary) vulkan.setDefaultWindow(window);
			if (!secondary) vulkan.setWindow(window);
			this.window = window;
			return window;
		}
		
		public VulkanWindow rebuild(VulkanWindow oldWindow) throws VulkanException
		{
			Vulkan vulkan = KirryVulkan.getVulkan();
			
			vk = oldWindow.id;
			
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
			Device device = vulkan.createDevice(deviceFeatures, surface, deviceExtensions);
			VulkanWindow window = new VulkanWindow(this, vk, oldWindow.getTitle(), device, surface);
			vulkan.replaceWindow(oldWindow, window);
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
	
	public static Builder defaultBuilder()
	{
		return new Builder()
			.physicalDeviceFilter(PhysicalDevice.Filter.withSurfaceGraphicsAndCompute())
			.physicalDeviceStrategy(PhysicalDevice.Strategy.findBest())
			.asDefaultPrimary()
			.deviceExtensionsWithDefaults();
	}
	
	public class FrameData implements AutoCloseable {
		
		private final long image;
		private final long imageView;
		private final FrameBuffer frameBuffer;
		
		private final long imageAvailable;
		private final long renderFinished;
		private final long fence;
		private final IntBuffer imageIndex;
		private Swapchain swapchain;
		
		public FrameData(long image, long imageView, FrameBuffer frameBuffer, long imageAvailable, long renderFinished, long fence, Swapchain swapchain)
		{
			this.image = image;
			this.imageView = imageView;
			this.frameBuffer = frameBuffer;
			this.imageAvailable = imageAvailable;
			this.renderFinished = renderFinished;
			this.fence = fence;
			imageIndex = MemoryUtil.memAllocInt(1);
			this.swapchain = swapchain;
		}
		
		public long getImage()
		{
			return image;
		}
		
		public long getImageView()
		{
			return imageView;
		}
		
		public FrameBuffer getFrameBuffer()
		{
			return frameBuffer;
		}
		
		public long getImageAvailable()
		{
			return imageAvailable;
		}
		
		public long getRenderFinished()
		{
			return renderFinished;
		}
		
		public long getFence()
		{
			return fence;
		}
		
		public IntBuffer getImageIndex()
		{
			return imageIndex;
		}
		
		public Swapchain getSwapchain()
		{
			return swapchain;
		}
		
		public void setSwapchain(Swapchain swapchain)
		{
			this.swapchain = swapchain;
		}
		
		@Override
		public void close()
		{
			VkDevice vk = device.getVk();
			VK10.vkDestroyImageView(vk, imageView, null);
			VK10.vkDestroySemaphore(vk, imageAvailable, null);
			VK10.vkDestroySemaphore(vk, renderFinished, null);
			VK10.vkDestroyFence(vk, fence, null);
			MemoryUtil.memFree(imageIndex);
			frameBuffer.close();
		}
	}
	
	private volatile int width;
	private volatile int height;
	private volatile String title;
	private volatile boolean isIconified;
	
	private final Device device;
	private final PresentQueue presentQueue;
	private final MemoryManager memoryManager;
	
	private final Surface surface;
	private final Lock surfaceSizeChangedLock;
	private volatile boolean isSurfaceSizeChanged;
	private final Condition swapchainRecreatedCondition;
	
	private @Nullable Texture depthTexture;
	private final RenderPass renderPass;
	private Swapchain swapchain;
	private FrameData[] frames;
	private Queue<VulkanConsumer<CommandBuffer>> queuedCommands;
	private List<Queue<VulkanConsumer<Boolean>>> presentationTasks;
	private List<VulkanConsumer<SurfaceData>> onSwapchainRecreationCallbacks;
	
	private final List<CommandBuffer> commandBuffers;
	private int frameIndex;
	
	private @Nullable Screen oldScreen;
	private Screen screen;
	private @Nullable Matrix4f screenMatrix;
	private EventRegistry eventRegistry;
	
	private @Nullable String name;
	private boolean isLost;
	private @Nullable ResourceManager resourceManager;
	
	private Vector2d mousePos;
	
	private VulkanWindow(Builder builder, long windowId, String title, Device device, Surface.Builder surfaceBuilder) throws VulkanException
	{
		VulkanErrorUtils.assertStatement(device.getPresentQueue() != null, "The device must be able to present to this window's surface");
		
		id = windowId;
		int[] p0 = new int[1];
		int[] p1 = new int[1];
		GLFW.glfwGetWindowSize(id, p0, p1);
		width = p0[0];
		height = p1[0];
		screenMatrix = null;
		
		this.title = title;
		this.device = device;
		memoryManager = builder.memoryManager != null ? builder.memoryManager : MemoryManager.getDefault(device);
		presentQueue = (@NonNull PresentQueue) device.getPresentQueue();
		surface = surfaceBuilder.window(this).build();
		surfaceSizeChangedLock = new ReentrantLock();
		swapchainRecreatedCondition = surfaceSizeChangedLock.newCondition();
		
		SurfaceData surfaceData = surface.getData();
		resetCallbacks();
		presentationTasks = new ArrayList<>();
		for (int i = 0; i < BUFFERED_FRAMES_COUNT; i++) presentationTasks.add(new LinkedList<>());
		queuedCommands = new LinkedList<>();
		//	drawCount = 0;
		
		int format = surfaceData.format().format();
		try (RenderPassTemplate renderPassTemplate = builder.useDepthTexture ? RenderPassTemplate.initColorDepthTextureTemplate(format) : RenderPassTemplate.initColorTextureTemplate(format))
		{
			renderPass = renderPassTemplate.createRenderPass(device);
		}
		frameIndex = 0;
		
		commandBuffers = device.createMultiUseCommandBuffer(BUFFERED_FRAMES_COUNT);
		
		onSwapchainRecreationCallbacks = new LinkedList<>();
		swapchain = createSwapchain(surfaceData, null);
		depthTexture = builder.useDepthTexture ? createDepthTexture(surfaceData) : null;
		frames = prepareFrameBuffers(surfaceData, swapchain, depthTexture, null);
		
		screen = BlackScreen.BLACK_SCREEN;
		eventRegistry = new EventRegistry();
		
		resourceManager = builder.resourceManager;
		mousePos = Vector2d.of(Double.NaN, Double.NaN);
	}
	
	private @Nullable Texture createDepthTexture(SurfaceData surfaceData) throws VulkanException
	{
		VkExtent2D extent = surfaceData.extent();
		Texture depthTexture = Texture.create2dTexture(device, extent.width(), extent.height(), VK10.VK_FORMAT_D24_UNORM_S8_UINT, VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT, VK10.VK_IMAGE_ASPECT_DEPTH_BIT | VK10.VK_IMAGE_ASPECT_STENCIL_BIT);
		queuedCommands.add(commandBuffer -> depthTexture.changeLayout(commandBuffer, VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL));
		
		if (name != null) depthTexture.setName("DepthTexture (" + name + ")");
		return depthTexture;
	}
	
	private Swapchain createSwapchain(SurfaceData surfaceData, @Nullable Swapchain oldSwapchain) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkSurfaceCapabilitiesKHR capabilities = VkSurfaceCapabilitiesKHR.calloc(stack);
			VulkanErrorUtils.assertNoError(KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device.getPhysicalDevice().getVk(), surface.getVk(), capabilities), "Failed to create swapchain");
			
			VkSwapchainCreateInfoKHR swapchainCreateInfo = VkSwapchainCreateInfoKHR.malloc(stack)
				.sType(KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
				.pNext(0)
				.flags(0)
				.surface(surface.getVk())
				.minImageCount(BUFFERED_FRAMES_COUNT)
				.imageFormat(surfaceData.format().format())
				.imageColorSpace(surfaceData.format().colorSpace())
				.imageExtent(surfaceData.extent())
				.imageArrayLayers(1)
				.imageUsage(VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);
			if (device.getGraphicsQueue().getFamilyIndex() == presentQueue.getFamilyIndex())
			{
				swapchainCreateInfo.imageSharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);
			}
			else
			{
				swapchainCreateInfo.imageSharingMode(VK10.VK_SHARING_MODE_CONCURRENT);
				swapchainCreateInfo.pQueueFamilyIndices(stack.ints(device.getGraphicsQueue().getFamilyIndex(), presentQueue.getFamilyIndex()));
			}
			swapchainCreateInfo.preTransform(capabilities.currentTransform())
				.compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
				.presentMode(surfaceData.mode())
				.clipped(true)
				.oldSwapchain(oldSwapchain != null ? oldSwapchain.getVk() : 0);
			
			return new Swapchain(device, swapchainCreateInfo);
		}
	}
	
	private FrameData[] prepareFrameBuffers(SurfaceData surfaceData, Swapchain swapchain, @Nullable Texture depthTexture, FrameData @Nullable [] oldFrames) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			long[] p0 = new long[BUFFERED_FRAMES_COUNT];
			VulkanErrorUtils.assertNoError(KHRSwapchain.vkGetSwapchainImagesKHR(device.getVk(), swapchain.getVk(), new int[] {BUFFERED_FRAMES_COUNT}, p0));
			
			FrameData[] frames = new FrameData[BUFFERED_FRAMES_COUNT];
			
			long[] p1 = new long[1];
			
			LongBuffer attachments = stack.mallocLong(depthTexture != null ? 2 : 1);
			if (depthTexture != null) attachments.put(1, depthTexture.getView().getVk());
			
			VkFramebufferCreateInfo framebufferCreateInfo = VkFramebufferCreateInfo.malloc(stack)
				.sType(VK10.VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
				.pNext(0)
				.flags(0)
				.renderPass(renderPass.getVk())
				.width(surfaceData.extent().width())
				.height(surfaceData.extent().height())
				.layers(1);
			
			VkImageViewCreateInfo imageViewCreateInfo = VkImageViewCreateInfo.malloc(stack)
				.sType(VK10.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
				.pNext(0)
				.flags(0)
				.viewType(VK10.VK_IMAGE_VIEW_TYPE_2D)
				.format(surfaceData.format().format());
			imageViewCreateInfo.components()
				.r(VK10.VK_COMPONENT_SWIZZLE_IDENTITY)
				.g(VK10.VK_COMPONENT_SWIZZLE_IDENTITY)
				.b(VK10.VK_COMPONENT_SWIZZLE_IDENTITY)
				.a(VK10.VK_COMPONENT_SWIZZLE_IDENTITY);
			imageViewCreateInfo.subresourceRange()
				.aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT)
				.baseMipLevel(0)
				.levelCount(1)
				.baseArrayLayer(0)
				.layerCount(1);
			
			VkSemaphoreCreateInfo semaphoreInfo = VkSemaphoreCreateInfo.malloc(stack)
				.sType(VK10.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO)
				.pNext(0)
				.flags(0);
			
			VkFenceCreateInfo fenceInfo = VkFenceCreateInfo.malloc(stack)
				.sType(VK10.VK_STRUCTURE_TYPE_FENCE_CREATE_INFO)
				.pNext(0)
				.flags(VK10.VK_FENCE_CREATE_SIGNALED_BIT);
			
			long[][] p2 = new long[3][1];
			for (int i = 0; i < BUFFERED_FRAMES_COUNT; i++)
			{
				imageViewCreateInfo.image(p0[i]);
				
				VulkanErrorUtils.assertNoError(VK10.vkCreateImageView(device.getVk(), imageViewCreateInfo, null, p1), "Failed to create image views");
				
				if (name != null) device.setObjectDebugName(VK10.VK_OBJECT_TYPE_IMAGE_VIEW, p1[0], "Frame" + i + " ImageView (" + name + ")");
				
				framebufferCreateInfo.pAttachments(attachments.put(0, p1[0]));
				FrameBuffer frameBuffer = new FrameBuffer(device, renderPass, framebufferCreateInfo);
				if (name != null) frameBuffer.setName("Frame" + i + " FrameBuffer (" + name + ")");
				
				if (oldFrames == null)
				{
					VulkanErrorUtils.assertNoError(VK10.vkCreateSemaphore(device.getVk(), semaphoreInfo, null, p2[0]), "Failed to create synchronization objects for the frame " + i);
					VulkanErrorUtils.assertNoError(VK10.vkCreateSemaphore(device.getVk(), semaphoreInfo, null, p2[1]), "Failed to create synchronization objects for the frame " + i);
					VulkanErrorUtils.assertNoError(VK10.vkCreateFence(device.getVk(), fenceInfo, null, p2[2]), "Failed to create synchronization objects for the frame " + i);
					
					frames[i] = new FrameData(p0[i], p1[0], frameBuffer, p2[0][0], p2[1][0], p2[2][0], swapchain);
				}
				else
				{
					frames[i] = new FrameData(p0[i], p1[0], frameBuffer, oldFrames[i].imageAvailable, oldFrames[i].renderFinished, oldFrames[i].fence, swapchain);
				}
			}
			
			VkClearValue.Buffer clearValues = VkClearValue.malloc(2, stack);
			clearValues.get().color().float32(stack.floats(0, 0, 0, 0));
			clearValues.get().depthStencil().depth(1).stencil(0);
			
			VkRenderPassBeginInfo renderPassBeginInfo = VkRenderPassBeginInfo.malloc(stack)
				.sType(VK10.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
				.pNext(0)
				.renderPass(renderPass.getVk())
				.pClearValues(clearValues.rewind());
			
			for (int i = 0; i < BUFFERED_FRAMES_COUNT; i++)
			{
				FrameBuffer frameBuffer = frames[i].frameBuffer;
				renderPassBeginInfo.framebuffer(frameBuffer.getVk());
				
				VkRect2D renderArea = VkRect2D.calloc(stack);
				renderArea.extent(VkExtent2D.malloc(stack).set(frameBuffer.getWidth(), frameBuffer.getHeight()));
				renderPassBeginInfo.renderArea(renderArea);
			}
			return frames;
		}
	}
	
	private boolean recreateSwapchain() throws VulkanException
	{
		SurfaceData surfaceData = surface.reinit();
		queuedCommands.clear();
		for (Queue<VulkanConsumer<Boolean>> queue : presentationTasks) while (!queue.isEmpty()) ((@NonNull VulkanConsumer<Boolean>) queue.poll()).accept(false);
		try
		{
			VkExtent2D extent = surfaceData.extent();
			if (extent.width() == 0 || extent.height() == 0) return false;
			
			Swapchain oldSwapchain = swapchain;
			FrameData[] oldFrames = frames;
			@Nullable
			Texture oldDepthTexture = depthTexture;
			
			Swapchain newSwapchain = createSwapchain(surfaceData, oldSwapchain);
			@Nullable
			Texture newDepthTexture = oldDepthTexture != null ? createDepthTexture(surfaceData) : null;
			FrameData[] newFrames = prepareFrameBuffers(surfaceData, newSwapchain, newDepthTexture, oldFrames);
			
			for (VulkanConsumer<SurfaceData> callback : onSwapchainRecreationCallbacks) callback.accept(surfaceData);
			frames = newFrames;
			if (name != null) newSwapchain.setName("Swapchain (" + name + ")");
			depthTexture = newDepthTexture;
			swapchain = newSwapchain;
			
			for (int i = 0; i < frames.length; i++)
			{
				int i0 = i;
				presentationTasks.get(i0).add(_ -> {
					FrameData frame = oldFrames[i0];
					frame.frameBuffer.close();
					VK10.vkDestroyImageView(device.getVk(), frame.imageView, null);
				});
			}
			presentationTasks.get(frameIndex).add(_ -> {
				oldSwapchain.close();
				if (oldDepthTexture != null) oldDepthTexture.close();
			});
			return true;
		}
		finally
		{
			isSurfaceSizeChanged = false;
			swapchainRecreatedCondition.signal();
		}
	}
	
	public void addOnSwapchainRecreationCallback(VulkanConsumer<SurfaceData> callback)
	{
		onSwapchainRecreationCallbacks.add(callback);
	}
	
	public void removeOnSwapchainRecreationCallback(VulkanConsumer<SurfaceData> callback)
	{
		onSwapchainRecreationCallbacks.remove(callback);
	}
	
	public enum PresentationResult {
		SUCCESS,
		FULL_SCREEN_LOST,
		EMPTY_SURFACE,
		SURFACE_LOST,
		OUT_OF_MEMORY,
		FATAL_ERROR;
	}
	
	/**
	 * Thread-unsafe
	 */
	public PresentationResult startPresentation() throws VulkanException
	{
		surfaceSizeChangedLock.lock();
		try
		{
			if (isSurfaceSizeChanged)
			{
				if (!recreateSwapchain()) return PresentationResult.EMPTY_SURFACE;
			}
			else
			{
				SurfaceData surfaceData = surface.getData();
				if (surfaceData.size().x() == 0 || surfaceData.size().y() == 0) return PresentationResult.EMPTY_SURFACE;
			}
			while (true)
			{
				Swapchain swapchain = this.swapchain;
				FrameData frame = frames[frameIndex];
				device.waitForFences(frame.getFence());
				
				for (Queue<VulkanConsumer<Boolean>> queue : presentationTasks) while (!queue.isEmpty()) ((@NonNull VulkanConsumer<Boolean>) queue.poll()).accept(true);
				
				switch (KHRSwapchain.vkAcquireNextImageKHR(device.getVk(), swapchain.getVk(), Integer.MAX_VALUE, frame.getImageAvailable(), 0, frame.getImageIndex()))
				{
					case VK10.VK_SUCCESS, VK10.VK_NOT_READY, VK10.VK_TIMEOUT, KHRSwapchain.VK_SUBOPTIMAL_KHR -> {
						frame.setSwapchain(swapchain);
						CommandBuffer commandBuffer = getCurrentCommandBuffer();
						commandBuffer.reset();
						commandBuffer.beginRecording();
						while (!queuedCommands.isEmpty()) ((@NonNull VulkanConsumer<CommandBuffer>) queuedCommands.poll()).accept(commandBuffer);
						
						return PresentationResult.SUCCESS;
					}
					case KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR -> {
						if (!recreateSwapchain()) return PresentationResult.EMPTY_SURFACE;
					}
					case EXTFullScreenExclusive.VK_ERROR_FULL_SCREEN_EXCLUSIVE_MODE_LOST_EXT -> {//not yet supported
						return PresentationResult.FULL_SCREEN_LOST;
					}
					case KHRSurface.VK_ERROR_SURFACE_LOST_KHR -> {
						isLost = true;
						return PresentationResult.SURFACE_LOST;
					}
					case VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY -> {
						return PresentationResult.OUT_OF_MEMORY;
					}
					default -> {
						return PresentationResult.FATAL_ERROR;
					}
				};
			}
		}
		finally
		{
			surfaceSizeChangedLock.unlock();
		}
	}
	
	/**
	 * @see #endPresentation(CommandBuffer, Runnable)
	 */
	public PresentationResult endPresentation(CommandBuffer commandBuffer) throws VulkanException
	{
		return endPresentation(commandBuffer, () -> {});
	}
	
	/**
	 * Thread-unsafe, must be called after {@link #startPresentation()} with a return value of {@link PresentationResult#SUCCESS}
	 */
	public PresentationResult endPresentation(CommandBuffer commandBuffer, Runnable onCommandBufferEndAction) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			FrameData frame = frames[frameIndex];
			
			LongBuffer p1 = stack.longs(frame.getRenderFinished());
			
			commandBuffer.endRecording();
			
			VkSubmitInfo submitInfo = VkSubmitInfo.malloc(stack)
				.sType(VK10.VK_STRUCTURE_TYPE_SUBMIT_INFO)
				.pNext(0)
				.waitSemaphoreCount(1)
				.pWaitSemaphores(stack.longs(frame.getImageAvailable()))
				.pWaitDstStageMask(stack.ints(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT))
				.pCommandBuffers(VulkanUtils.toAddressBuffer(stack, commandBuffer))
				.pSignalSemaphores(p1);
			
			device.resetFences(frame.getFence());
			VulkanFuture.composeAfter(device.getGraphicsQueue().submitAsync(submitInfo, frame.getFence()), _ -> {
				onCommandBufferEndAction.run();
			});
			
			VkPresentInfoKHR presentInfo = VkPresentInfoKHR.malloc(stack)
				.sType(KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
				.pNext(0)
				.pWaitSemaphores(p1)
				.swapchainCount(1)
				.pSwapchains(stack.longs(frame.getSwapchain().getVk()))
				.pImageIndices(frame.getImageIndex())
				.pResults(null);
			
			return switch (presentQueue.present(presentInfo))
			{
				case VK10.VK_SUCCESS -> {
					frameIndex = (frameIndex + 1) % BUFFERED_FRAMES_COUNT;
					yield PresentationResult.SUCCESS;
				}
				case KHRSwapchain.VK_SUBOPTIMAL_KHR, KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR -> {
					frameIndex = (frameIndex + 1) % BUFFERED_FRAMES_COUNT;
					
					surfaceSizeChangedLock.lock();
					try
					{
						recreateSwapchain();
					}
					finally
					{
						swapchainRecreatedCondition.signal();
						surfaceSizeChangedLock.unlock();
					}
					yield PresentationResult.SUCCESS;
				}
				case EXTFullScreenExclusive.VK_ERROR_FULL_SCREEN_EXCLUSIVE_MODE_LOST_EXT -> PresentationResult.FULL_SCREEN_LOST;
				case KHRSurface.VK_ERROR_SURFACE_LOST_KHR -> {
					isLost = true;
					yield PresentationResult.SURFACE_LOST;
				}
				case VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY -> PresentationResult.OUT_OF_MEMORY;
				default -> PresentationResult.FATAL_ERROR;
			};
		}
	}
	
	public PresentationResult renderScreenOnce(float elapsed) throws VulkanException
	{
		if (oldScreen != null)
		{
			//oldScreen.clearRenderingData();
			oldScreen = null;
		}
		if (!screen.isOnSurface() || isSurfaceSizeChanged)
		{
			PresentationResult r = startPresentation();
			if (r != PresentationResult.SUCCESS) return r;
			
			CommandBuffer commandBuffer = getCurrentCommandBuffer();
			
			Screen screen = this.screen;
			if (!screen.isInitialized())
			{
				screen.init(memoryManager, commandBuffer);
			}
			screen.update(commandBuffer, elapsed);
			
			Matrix4f matrix = screenMatrix;
			if (matrix == null) matrix = screenMatrix = new Matrix4f().orthographicPositive(width, height);
			screen.render(matrix, commandBuffer);
			
			screen.draw(matrix, commandBuffer);
			return endPresentation(commandBuffer);
		}
		return PresentationResult.SUCCESS;
	}
	
	public void awaitClose() throws InterruptedException
	{
		
	}
	
	public void setShouldClose(boolean value)
	{
		GLFW.glfwSetWindowShouldClose(id, value);
	}
	
	@Override
	public void pollEvents()
	{
		KirryVulkan.requestMainThread(GLFW::glfwPollEvents);
	}
	
	public void waitEvents()
	{
		KirryVulkan.awaitRequestMainThread(GLFW::glfwWaitEvents);
	}
	
	public void postEmptyEvent()
	{
		GLFW.glfwPostEmptyEvent();
	}
	
	public final void resetCallbacks()
	{
		KirryVulkan.awaitRequestMainThread(() -> {
			GLFW.glfwSetCharCallback(id, null);
			GLFW.glfwSetCharModsCallback(id, null);
			GLFW.glfwSetCursorEnterCallback(id, null);
			GLFW.glfwSetCursorPosCallback(id, this::cursorPosCallback);
			GLFW.glfwSetDropCallback(id, null);
			GLFW.glfwSetKeyCallback(id, this::keyCallback);
			GLFW.glfwSetMouseButtonCallback(id, this::mouseButtonCallback);
			GLFW.glfwSetScrollCallback(id, null);
			GLFW.glfwSetWindowCloseCallback(id, this::windowCloseCallback);
			GLFW.glfwSetWindowContentScaleCallback(id, null);
			GLFW.glfwSetWindowFocusCallback(id, this::windowFocusCallback);
			GLFW.glfwSetWindowIconifyCallback(id, this::windowIconifyCallback);
			GLFW.glfwSetWindowMaximizeCallback(id, this::windowMaximizeCallback);
			GLFW.glfwSetWindowPosCallback(id, null);
			GLFW.glfwSetWindowRefreshCallback(id, null);
			GLFW.glfwSetWindowSizeCallback(id, this::windowSizeCallback);
			GLFW.glfwSetFramebufferSizeCallback(id, this::framebufferSizeCallback);
		});
	}
	
	public final void setCallback(CallbackI callback)
	{
		KirryVulkan.awaitRequestMainThread(() -> {
			if (callback instanceof GLFWCharCallbackI i) GLFW.glfwSetCharCallback(id, i);
			if (callback instanceof GLFWCharModsCallbackI i) GLFW.glfwSetCharModsCallback(id, i);
			if (callback instanceof GLFWCursorEnterCallbackI i) GLFW.glfwSetCursorEnterCallback(id, i);
			if (callback instanceof GLFWCursorPosCallbackI i) GLFW.glfwSetCursorPosCallback(id, (w, x, y) -> {
				cursorPosCallback(w, x, y);
				i.invoke(w, x, y);
			});
			if (callback instanceof GLFWDropCallbackI i) GLFW.glfwSetDropCallback(id, i);
			if (callback instanceof GLFWKeyCallbackI i) GLFW.glfwSetKeyCallback(id, (w, k, s, a, m) -> {
				keyCallback(w, k, s, a, m);
				i.invoke(w, k, s, a, m);
			});
			if (callback instanceof GLFWMouseButtonCallbackI i) GLFW.glfwSetMouseButtonCallback(id, (w, b, a, m) -> {
				mouseButtonCallback(w, b, a, m);
				i.invoke(w, b, a, m);
			});
			if (callback instanceof GLFWScrollCallbackI i) GLFW.glfwSetScrollCallback(id, i);
			if (callback instanceof GLFWWindowCloseCallbackI i) GLFW.glfwSetWindowCloseCallback(id, w -> {
				windowCloseCallback(w);
				i.invoke(w);
			});
			if (callback instanceof GLFWWindowContentScaleCallbackI i) GLFW.glfwSetWindowContentScaleCallback(id, i);
			if (callback instanceof GLFWWindowFocusCallbackI i) GLFW.glfwSetWindowFocusCallback(id, (w, f) -> {
				windowFocusCallback(w, f);
				i.invoke(w, f);
			});
			if (callback instanceof GLFWWindowIconifyCallbackI i) GLFW.glfwSetWindowIconifyCallback(id, (w, c) -> {
				windowIconifyCallback(w, c);
				i.invoke(w, c);
			});
			if (callback instanceof GLFWWindowMaximizeCallbackI i) GLFW.glfwSetWindowMaximizeCallback(id, (w, m) -> {
				windowMaximizeCallback(w, m);
				i.invoke(w, m);
			});
			if (callback instanceof GLFWWindowPosCallbackI i) GLFW.glfwSetWindowPosCallback(id, i);
			if (callback instanceof GLFWWindowRefreshCallbackI i) GLFW.glfwSetWindowRefreshCallback(id, i);
			if (callback instanceof GLFWWindowSizeCallbackI i) GLFW.glfwSetWindowSizeCallback(id, (p, w, h) -> {
				windowSizeCallback(p, w, h);
				i.invoke(p, w, h);
			});
			if (callback instanceof GLFWFramebufferSizeCallbackI i) GLFW.glfwSetFramebufferSizeCallback(id, (p, w, h) -> {
				framebufferSizeCallback(p, w, h);
				i.invoke(p, w, h);
			});
		});
	}
	
	private void cursorPosCallback(long p, double x, double y)
	{
		if (!Double.isNaN(mousePos.x()) && !Double.isNaN(mousePos.y())) eventRegistry.runEvent(EventType.MOUSE_MOVE, mousePos.x(), mousePos.y(), x, y, (x - mousePos.x()), (y - mousePos.y()));
		mousePos.setAll(x, y);
	}
	
	private void keyCallback(long p, int k, int s, int a, int m)
	{
		eventRegistry.runEvent(EventType.KEYBOARD, k, s, a, m);
	}
	
	private void mouseButtonCallback(long p, int b, int a, int m)
	{
		eventRegistry.runEvent(EventType.CLICK, b, 1, mousePos);
	}
	
	private void windowCloseCallback(long p)
	{
		onHide();
	}
	
	private void windowFocusCallback(long p, boolean f)
	{
		onFocus();
	}
	
	private void windowIconifyCallback(long p, boolean c)
	{
		isIconified = c;
		if (c) onIconify();
		else onRestore();
	}
	
	private void windowMaximizeCallback(long p, boolean m)
	{
		if (m) onMaximize();
		else onRestore();
	}
	
	private void windowSizeCallback(long p, int w, int h)
	{
		width = w;
		height = h;
		eventRegistry.runEvent(EventType.WINDOW_RESIZE, w, h);
		screenMatrix = null;
	}
	
	private void framebufferSizeCallback(long p, int w, int h)
	{
		if (!KirryVulkan.isMultiThreading())
		{
			isSurfaceSizeChanged = true;
			return;
		}
		surfaceSizeChangedLock.lock();
		try
		{
			isSurfaceSizeChanged = true;
			while (isSurfaceSizeChanged) swapchainRecreatedCondition.awaitUninterruptibly();
		}
		finally
		{
			surfaceSizeChangedLock.unlock();
		}
	}
	
	public void setScreen(Screen screen)
	{
		if (screen.isInitialized()) oldScreen = this.screen;
		this.screen = Objects.requireNonNull(screen);
		screen.registerEvents(eventRegistry);
		screen.setWindow(this);
	}
	
	public Screen getScreen()
	{
		return screen;
	}
	
	public final Vector2f getMousePos(Vector2f holder)
	{
		double[][] p = new double[2][1];
		KirryVulkan.awaitRequestMainThread(() -> GLFW.glfwGetCursorPos(id, p[0], p[1]));
		return holder.setAll((float) p[0][0], (float) p[1][0]);
	}
	
	public final Vector2f getMousePos()
	{
		double[][] p = new double[2][1];
		KirryVulkan.awaitRequestMainThread(() -> GLFW.glfwGetCursorPos(id, p[0], p[1]));
		return Vector2f.of((float) p[0][0], (float) p[1][0]);
	}
	
	public final Pair<Double, Double> getHighPrecisionMousePos(Pair<Double, Double> holder)
	{
		double[][] p = new double[2][1];
		KirryVulkan.awaitRequestMainThread(() -> GLFW.glfwGetCursorPos(id, p[0], p[1]));
		holder.setLeft(p[0][0]);
		holder.setRight(p[1][0]);
		return holder;
	}
	
	public final Pair<Double, Double> getHighPrecisionMousePos()
	{
		double[][] p = new double[2][1];
		KirryVulkan.awaitRequestMainThread(() -> GLFW.glfwGetCursorPos(id, p[0], p[1]));
		return new Pair<>(p[0][0], p[1][0]);
	}
	
	public final void setMousePos(Vector2fr pos)
	{
		KirryVulkan.requestMainThread(() -> GLFW.glfwSetCursorPos(id, pos.x(), pos.y()));
	}
	
	public final void setMousePos(Pair<Double, Double> pos)
	{
		KirryVulkan.requestMainThread(() -> GLFW.glfwSetCursorPos(id, pos.l(), pos.r()));
	}
	
	public final Vector2i getPos(Vector2i holder)
	{
		int[][] p = new int[2][1];
		KirryVulkan.awaitRequestMainThread(() -> GLFW.glfwGetWindowPos(id, p[0], p[1]));
		return holder.setAll(p[0][0], p[1][0]);
	}
	
	public final Vector2i getPos()
	{
		int[][] p = new int[2][1];
		KirryVulkan.awaitRequestMainThread(() -> GLFW.glfwGetWindowPos(id, p[0], p[1]));
		return Vector2i.of(p[0][0], p[1][0]);
	}
	
	public final void setPos(Vector2ir pos)
	{
		KirryVulkan.requestMainThread(() -> GLFW.glfwSetWindowPos(id, pos.x(), pos.y()));
	}
	
	public final void setSize(int width, int height)
	{
		KirryVulkan.requestMainThread(() -> GLFW.glfwSetWindowSize(id, width, height));
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
	
	public @Nullable ResourceManager getResourceManager()
	{
		return resourceManager;
	}
	
	public EventRegistry getEventRegistry()
	{
		return eventRegistry;
	}
	
	public boolean isIconified()
	{
		return isIconified;
	}
	
	public Surface getSurface()
	{
		return surface;
	}
	
	public boolean hasDepthBuffer()
	{
		return depthTexture != null;
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
		KirryVulkan.requestMainThread(() -> {
			GLFW.glfwSetWindowTitle(id, title);
			this.title = title;
		});
	}
	
	public RenderPass getRenderPass()
	{
		return renderPass;
	}
	
	public int getFrameIndex()
	{
		return frameIndex;
	}
	
	public int getFramesCount()
	{
		return frames.length;
	}
	
	public FrameBuffer getCurrentFrameBuffer()
	{
		return frames[frameIndex].getFrameBuffer();
	}
	
	public CommandBuffer getCurrentCommandBuffer()
	{
		return commandBuffers.get(frameIndex);
	}
	
	public boolean isLost()
	{
		return isLost;
	}
	
	@Override
	public void close()
	{
		if (id != 0)
		{
			for (Queue<VulkanConsumer<Boolean>> queue : presentationTasks) while (!queue.isEmpty())
			{
				try
				{
					((@NonNull VulkanConsumer<Boolean>) queue.poll()).accept(false);
				}
				catch (VulkanException e)
				{}
			}
			try
			{
				screen.close();
			}
			catch (VulkanException e)
			{}
			device.waitIdle();
			for (FrameData frame : frames) frame.close();
			swapchain.close();
			for (CommandBuffer commandBuffer : commandBuffers) commandBuffer.close();
			if (depthTexture != null) depthTexture.close();
			memoryManager.close();
			
			surface.close();
			KirryVulkan.awaitRequestMainThread(() -> GLFW.glfwDestroyWindow(id));
			id = 0L;
		}
	}
	
	public void setName(String name)
	{
		this.name = name;
		device.setObjectDebugName(surface, "Surface(" + name + ")");
		if (depthTexture != null) depthTexture.setName("DepthTexture(" + name + ")");
		for (int i = 0; i < commandBuffers.size(); i++) commandBuffers.get(i).setName("CommandBuffer " + i + " (" + name + ")");
	}
	
	@Override
	public void debug()
	{
		Debug.print("Vk: " + Debug.address(id));
		Debug.print("Size: " + width + "x" + height);
		Debug.print("Surface: ", surface);
		Debug.print("Swapchain: ", swapchain);
	}
}
