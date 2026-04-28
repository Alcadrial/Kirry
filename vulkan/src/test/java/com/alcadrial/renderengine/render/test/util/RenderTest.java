package com.alcadrial.renderengine.render.test.util;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWWindowRefreshCallbackI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.EXTDebugUtils;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;

import com.alcadrial.kector.angle.AngleUnit;
import com.alcadrial.kector.annotation.Angle;
import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.kroperties.Property;
import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.FrameBuffer;
import com.alcadrial.renderengine.render.RenderEngineProperties;
import com.alcadrial.renderengine.render.RenderEngineVulkan;
import com.alcadrial.renderengine.render.RenderEngineVulkan.Builder;
import com.alcadrial.renderengine.render.Renderer;
import com.alcadrial.renderengine.render.buffer.BufferWriter;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.commandbuffer.CommandBuffer;
import com.alcadrial.renderengine.render.pipeline.GraphicsPipeline;
import com.alcadrial.renderengine.render.pipeline.GraphicsPipelineTemplate;
import com.alcadrial.renderengine.render.pipeline.PipelineLayout;
import com.alcadrial.renderengine.render.renderpass.RenderPass;
import com.alcadrial.renderengine.render.shader.PreparedShader;
import com.alcadrial.renderengine.render.shader.Shader;
import com.alcadrial.renderengine.render.shader.ShaderInputType;
import com.alcadrial.renderengine.render.shader.ShaderProgram;
import com.alcadrial.renderengine.render.shader.ShaderType;
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.shader.binding.BindingInfo;
import com.alcadrial.renderengine.render.shader.descriptor.DescriptorPool;
import com.alcadrial.renderengine.render.shader.descriptor.DescriptorSet;
import com.alcadrial.renderengine.render.shader.descriptor.DescriptorSetLayout;
import com.alcadrial.renderengine.render.texture.Texture;
import com.alcadrial.renderengine.render.texture.TextureSampler;
import com.alcadrial.renderengine.render.util.Version;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.VulkanFuture;
import com.alcadrial.renderengine.render.util.exception.ExternalException;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.util.functional.VulkanBiConsumer;
import com.alcadrial.renderengine.render.util.functional.VulkanRunnable;
import com.alcadrial.renderengine.render.window.VulkanWindow;

public class RenderTest {
	
	protected static final Object MONITOR = new Object();
	private static final String[] SEVERITIES = new String[] {"UNKNOWN", "VERBOSE", "INFO", "WARNING", "ERROR"};
	private static final String[] TYPES = new String[] {"UNKNOWN", "GENERAL", "VALIDATION", "PERFORMANCE"};
	private static final AtomicBoolean hasValidationError = new AtomicBoolean(false);
	
	public static void mainTest(String[] args, VulkanRunnable start, VulkanRunnable loop, VulkanRunnable end)
	{
		mainTest(args, start, loop, end, _ -> {});
	}
	
	public static void mainTest(String[] args, VulkanRunnable start, VulkanRunnable loop, VulkanRunnable end, Consumer<RenderEngineVulkan.Builder> config)
	{
		try
		{
			TestProperties.init(args);
			//Properties.DEVICE_DESTRUCTION_LIST_THRESHOLD.setValue(0);
			
			if (RenderEngineProperties.DEBUG.getValue()) RenderEngineVulkan.enableDebug();
			
			VulkanRunnable init = () -> {
				RenderEngineVulkan.Builder builder = initRenderEngine();
				config.accept(builder);
				builder.initialize();
			};
			println("RenderEngine initialization", init, TestProperties.PRINT_LOADING_TIMES);
			if (!RenderEngineVulkan.isInitializated()) throw new RuntimeException("Cannot initialize RenderEngine", RenderEngineVulkan.getLastInitializationException());
			
			if (RenderEngineProperties.DEBUG.getValue())
			{
				Debug.setEnabledAllocationDebug(true);
				Debug.setEnabledDeallocationFail(true);
			}
			
			Thread mainThread = null;
			
			if (RenderEngineVulkan.isMultiThreading())
			{
				mainThread = Thread.ofPlatform().name("Main").start(RenderEngineVulkan::serveRenderEngine);
			}
			
			try
			{
				println("Start", start, TestProperties.PRINT_LOADING_TIMES);
				if (!RenderEngineVulkan.isInitializated()) return;
				
				println("Loop", loop, TestProperties.PRINT_LOADING_TIMES);
				if (!RenderEngineVulkan.isInitializated()) return;
				
				println("End", end, TestProperties.PRINT_LOADING_TIMES);
			}
			catch (Throwable t)
			{
				try
				{
					if (RenderEngineVulkan.isInitializated()) RenderEngineVulkan.terminate();
				}
				catch (Throwable e)
				{
					e.printStackTrace(System.out);
				}
				throw t;
			}
			finally
			{
				if (mainThread != null) mainThread.join();
			}
		}
		catch (Throwable t)
		{
			t.printStackTrace(System.out);
		}
	}
	
	public static void defaultLoop(VulkanBiConsumer<Long, Long> action) throws VulkanException
	{
		VulkanWindow window = RenderEngineVulkan.getVulkan().getWindow();
		String title = window.getTitle();
		
		VulkanException[] lastError = new VulkanException[1];
		
		int maxfps = TestProperties.MAX_FRAME_RATE.getValue();
		long firstHighMilestone = 1000000000;
		long firstMilestone = 1000000000 / maxfps;
		if (firstMilestone <= 0) firstMilestone = 1;
		
		long time = System.nanoTime();
		long[] lastCurrent = {time};
		long frame = 0;
		long milestone = firstMilestone;
		long highMilestone = firstHighMilestone;
		
		window.setCallback((GLFWWindowRefreshCallbackI) _ -> {
			try
			{
				long current = System.nanoTime();
				long enlapsed = current - time;
				long enlapsedFrame = current - lastCurrent[0];
				action.accept(enlapsed, enlapsedFrame);
			}
			catch (VulkanException e)
			{
				lastError[0] = e;
			}
		});
		
		while (!window.shouldClose() && !hasValidationError.get())
		{
			long current = System.nanoTime();
			long enlapsed = current - time;
			long enlapsedFrame = current - lastCurrent[0];
			long pauseTime = milestone - enlapsed;
			if (pauseTime > 0)
			{
				try
				{
					synchronized (window)
					{
						window.wait(pauseTime / 1000000, (int) (pauseTime % 1000000));
					}
				}
				catch (InterruptedException e)
				{
					e.printStackTrace(System.out);
				}
				continue;
			}
			milestone += (1 - pauseTime / firstMilestone) * firstMilestone;
			if (enlapsed >= highMilestone)
			{
				window.setTitle(title + " fps=" + frame);
				frame = 0;
				highMilestone += (enlapsed - highMilestone + firstHighMilestone) / firstHighMilestone * firstHighMilestone;
			}
			println("Frame " + frame, () -> action.accept(enlapsed, enlapsedFrame), TestProperties.PRINT_FRAME_TIMES);
			
			window.pollEvents();
			if (lastError[0] != null) throw lastError[0];
			frame++;
			lastCurrent[0] = current;
		}
		window.getDevice().waitIdle();
	}
	
	public static void staticLoop(VulkanBiConsumer<Long, Long> action) throws VulkanException
	{
		staticLoop(action, null);
	}
	
	public static void staticLoop(VulkanBiConsumer<Long, Long> action, @Nullable Consumer<Runnable> forceRender) throws VulkanException
	{
		VulkanWindow window = RenderEngineVulkan.getVulkan().getWindow();
		
		VulkanException[] lastError = new VulkanException[1];
		
		int maxfps = TestProperties.MAX_FRAME_RATE.getValue();
		long firstMilestone = 1000000000 / maxfps;
		if (firstMilestone <= 0) firstMilestone = 1;
		
		long time = System.nanoTime();
		long[] lastCurrent = {time};
		long milestone = firstMilestone;
		
		Runnable render = () -> {
			try
			{
				long current = System.nanoTime();
				long enlapsed = current - time;
				long enlapsedFrame = current - lastCurrent[0];
				action.accept(enlapsed, enlapsedFrame);
			}
			catch (VulkanException e)
			{
				lastError[0] = e;
			}
			catch (Throwable e)
			{
				lastError[0] = new ExternalException(e);
			}
		};
		
		window.setCallback((GLFWWindowRefreshCallbackI) _ -> render.run());
		if (forceRender != null) forceRender.accept(render);
		
		while (!window.shouldClose() && !hasValidationError.get())
		{
			long current = System.nanoTime();
			long enlapsed = current - time;
			long pauseTime = milestone - enlapsed;
			if (pauseTime > 0)
			{
				try
				{
					synchronized (window)
					{
						window.wait(pauseTime / 1000000, (int) (pauseTime % 1000000));
					}
				}
				catch (InterruptedException e)
				{
					e.printStackTrace(System.out);
				}
				continue;
			}
			milestone += (1 - pauseTime / firstMilestone) * firstMilestone;
			
			window.pollEvents();
			if (lastError[0] != null) throw lastError[0];
			lastCurrent[0] = current;
		}
		window.getDevice().waitIdle();
	}
	
	public static void noEventsloop(VulkanBiConsumer<Long, Long> action) throws VulkanException
	{
		VulkanWindow window = RenderEngineVulkan.getVulkan().getWindow();
		String title = window.getTitle();
		
		int maxfps = TestProperties.MAX_FRAME_RATE.getValue();
		long firstHighMilestone = 1000000000;
		long firstMilestone = 1000000000 / maxfps;
		
		long time = System.nanoTime();
		long lastCurrent = time;
		long frame = 0;
		long milestone = firstMilestone;
		long highMilestone = firstHighMilestone;
		
		while (!window.shouldClose() && !hasValidationError.get())
		{
			long current = System.nanoTime();
			long enlapsed = current - time;
			long enlapsedFrame = current - lastCurrent;
			long pauseTime = milestone - enlapsed;
			if (pauseTime > 0)
			{
				try
				{
					synchronized (window)
					{
						window.wait(pauseTime / 1000000, (int) (pauseTime % 1000000));
					}
				}
				catch (InterruptedException e)
				{
					e.printStackTrace(System.out);
				}
				continue;
			}
			milestone += (1 - pauseTime / firstMilestone) * firstMilestone;
			if (enlapsed >= highMilestone)
			{
				window.setTitle(title + " fps=" + frame);
				frame = 0;
				highMilestone += (enlapsed - highMilestone + firstHighMilestone) / firstHighMilestone * firstHighMilestone;
			}
			println("Frame " + frame, () -> action.accept(enlapsed, enlapsedFrame), TestProperties.PRINT_FRAME_TIMES);
			
			frame++;
			lastCurrent = current;
		}
		window.getDevice().waitIdle();
	}
	
	public static void defaultAnimationLoop(VulkanBiConsumer<Long, Long> action) throws VulkanException
	{
		VulkanWindow window = RenderEngineVulkan.getVulkan().getWindow();
		String title = window.getTitle();
		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		VulkanException[] lastError = new VulkanException[1];
		
		int maxfps = TestProperties.MAX_FRAME_RATE.getValue();
		long period = 1000000000 / maxfps;
		if (period <= 0) period = 1;
		
		long firstHighMilestone = 1000000000;
		long time = System.nanoTime();
		long[] lastCurrent = {time};
		long[] frame = {0};
		long[] highMilestone = {firstHighMilestone};
		
		service.scheduleAtFixedRate(() -> {
			try
			{
				long current = System.nanoTime();
				long enlapsed = current - time;
				long enlapsedFrame = current - lastCurrent[0];
				if (enlapsed >= highMilestone[0])
				{
					window.setTitle(title + " fps=" + frame[0]);
					frame[0] = 0;
					highMilestone[0] += (enlapsed - highMilestone[0] + firstHighMilestone) / firstHighMilestone * firstHighMilestone;
				}
				println("Frame " + frame, () -> action.accept(enlapsed, enlapsedFrame), TestProperties.PRINT_FRAME_TIMES);
				frame[0]++;
				lastCurrent[0] = current;
				if (window.shouldClose()) window.postEmptyEvent();
			}
			catch (VulkanException e)
			{
				lastError[0] = e;
				service.shutdown();
				window.setShouldClose(true);
				window.postEmptyEvent();
			}
			catch (Throwable e)
			{
				lastError[0] = new ExternalException(e);
				service.shutdown();
				window.setShouldClose(true);
				window.postEmptyEvent();
			}
		}, 0, period, TimeUnit.NANOSECONDS);
		
		while (!window.shouldClose() && !hasValidationError.get())
		{
			window.waitEvents();
		}
		try
		{
			service.shutdown();
			if (!service.awaitTermination(500, TimeUnit.MILLISECONDS)) service.shutdownNow();
		}
		catch (InterruptedException e)
		{
			service.shutdownNow();
			return;
		}
		finally
		{
			if (lastError[0] != null) throw lastError[0];
		}
		
		window.getDevice().waitIdle();
	}
	
	public static void registerDragging()
	{
		VulkanWindow window = RenderEngineVulkan.getVulkan().getWindow();
		Vector2i oldPos = Vector2i.of(0, 0);
		Vector2f oldMousePos = Vector2f.of(0, 0);
		boolean[] pressing = new boolean[1];
		window.setCallback((GLFWMouseButtonCallbackI) (_, button, action, _) -> {
			if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
			{
				if (pressing[0] = action == GLFW.GLFW_PRESS)
				{
					window.getMousePos(oldMousePos);
				}
			}
		});
		window.setCallback((GLFWCursorPosCallbackI) (_, x, y) -> {
			if (pressing[0])
			{
				window.getPos(oldPos);
				window.setPos(Vector2i.of((float) x - oldMousePos.x() + oldPos.x(), (float) y - oldMousePos.y() + oldPos.y(), RoundingMode.FLOOR));
				window.getMousePos(oldMousePos);
			}
		});
	}
	
	public static void registerRotating(BiConsumer<@Angle(AngleUnit.RADIANS) Float, @Angle(AngleUnit.RADIANS) Float> callback)
	{
		VulkanWindow window = RenderEngineVulkan.getVulkan().getWindow();
		Vector2f oldMousePos = Vector2f.of(0, 0);
		Vector2f rotation = Vector2f.of(0, 0);
		Vector2f oldRotation = Vector2f.of(0, 0);
		boolean[] pressing = new boolean[1];
		window.setCallback((GLFWMouseButtonCallbackI) (_, button, action, _) -> {
			if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
			{
				if (pressing[0] = action == GLFW.GLFW_PRESS)
				{
					window.getMousePos(oldMousePos);
				}
				else
				{
					oldRotation.add(rotation);
				}
			}
		});
		window.setCallback((GLFWCursorPosCallbackI) (_, x, y) -> {
			if (pressing[0])
			{
				float yRotation = (float) AngleUnit.RADIANS.fromTurns((x - oldMousePos.x()) / window.getWidth());
				float xRotation = (float) AngleUnit.RADIANS.fromTurns((y - oldMousePos.y()) / window.getHeight());
				rotation.setAll(xRotation, yRotation);
				callback.accept(rotation.x() + oldRotation.x(), -rotation.y() - oldRotation.y());
			}
		});
	}
	
	public static Builder initRenderEngine() throws VulkanException
	{
		return RenderEngineVulkan.create("Test", new Version(0, 0, 1, 0))
			.messengerCallback(RenderTest::debugCallback)
			.instanceLayersDebug("VK_LAYER_KHRONOS_validation")
			.instanceExtensionsDebug(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME)
			.multiThreading(true)
			.useWindowSystem(true);
	}
	
	public static int debugCallback(int messageSeverity, int messageType, long callbackDataId, long userDataId)
	{
		VkDebugUtilsMessengerCallbackDataEXT callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(callbackDataId);
		
		int i = switch (messageSeverity)
		{
			case 0x0001 -> 1;
			case 0x0010 -> 2;
			case 0x0100 -> 3;
			case 0x1000 -> 4;
			default -> 0;
		};
		int j = switch (messageType)
		{
			case 0b001 -> 1;
			case 0b010 -> 2;
			case 0b100 -> 3;
			default -> 0;
		};
		if (messageSeverity == EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT)
		{
			hasValidationError.set(true);
		}
		
		Debug.debug("[" + SEVERITIES[i] + " " + TYPES[j] + "] " + callbackData.pMessageString());
		
		return VK10.VK_FALSE;
	}
	
	public static Texture createDepthTexture(int width, int height) throws VulkanException
	{
		Texture depthTexture = Texture.create2dTexture(RenderEngineVulkan.getVulkan().getDevice(), width, height, VK10.VK_FORMAT_D24_UNORM_S8_UINT, VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT, VK10.VK_IMAGE_ASPECT_DEPTH_BIT | VK10.VK_IMAGE_ASPECT_STENCIL_BIT);
		depthTexture.changeLayout(VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL).join();
		return depthTexture;
	}
	
	public static File getFile(String path)
	{
		return new File(RenderEngineProperties.RESOURCE_PATH.getValue(), "assets/renderengine/" + path);
	}
	
	public static PreparedShader loadShader(String path) throws IOException
	{
		File file = getFile(path);
		String name = file.getName();
		Shader shader = new Shader(file);
		ShaderType type = ShaderType.fromExtension(name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : name);
		if (TestProperties.FORCE_RECOMPILE_SHADERS.getValue()) shader.compile(type);
		else
		{
			File binFile = getFile(path + ".bin");
			if (binFile.canRead()) shader.loadCompiled(binFile);
			else shader.compile(type);
		}
		if (!shader.isCompilationSuccessfull()) throw new IOException("Shader " + shader.getFile() + " has errors");
		return new PreparedShader(shader, type);
	}
	
	public static void saveShaderBinary(Shader shader) throws IOException
	{
		Files.write(new File(VulkanErrorUtils.nonNull(shader.getFile()).getPath() + ".bin").toPath(), shader.getLastCompilation().get().binary());
	}
	
	public static void println(String s, VulkanRunnable action, Property<Boolean> printProperty) throws VulkanException
	{
		if (printProperty.getValue()) System.out.println(s + " time = " + recordTime(action, TimeUnit.MICROSECONDS) / 1000D + "ms");
		else action.run();
	}
	
	public static long recordTime(VulkanRunnable runnable, TimeUnit unit) throws VulkanException
	{
		long startTime;
		TimeUnit localUnit;
		if (unit == TimeUnit.MICROSECONDS || unit == TimeUnit.NANOSECONDS)
		{
			startTime = System.nanoTime();
			localUnit = TimeUnit.NANOSECONDS;
		}
		else
		{
			startTime = System.currentTimeMillis();
			localUnit = TimeUnit.MILLISECONDS;
		}
		
		runnable.run();
		
		long endTime;
		if (localUnit == TimeUnit.NANOSECONDS) endTime = System.nanoTime();
		else endTime = System.currentTimeMillis();
		
		return unit.convert(endTime - startTime, localUnit);
	}
	
	public static double recordTime(VulkanRunnable runnable, TimeUnit unit, TimeUnit resolution) throws VulkanException
	{
		long startTime;
		TimeUnit localUnit;
		if (resolution == TimeUnit.MICROSECONDS || resolution == TimeUnit.NANOSECONDS)
		{
			startTime = System.nanoTime();
			localUnit = TimeUnit.NANOSECONDS;
		}
		else
		{
			startTime = System.currentTimeMillis();
			localUnit = TimeUnit.MILLISECONDS;
		}
		
		runnable.run();
		
		long endTime;
		if (localUnit == TimeUnit.NANOSECONDS) endTime = System.nanoTime();
		else endTime = System.currentTimeMillis();
		
		return (double) resolution.convert(endTime - startTime, localUnit) / resolution.convert(1, unit);
	}
	
	public static Matrix4f buildFullSceneMatrix(VulkanWindow window, @Angle(AngleUnit.RADIANS) float fieldOfView)
	{
		float sceneRay = 1;//ray of the sphere containing the entire scene after applying the model matrix
		float aspectRatio = (float) window.getWidth() / window.getHeight();
		float scale = (float) (1 / Math.tan(fieldOfView / 2));
		float cameraZ = (float) (sceneRay * Math.sqrt(1 + scale * scale) / scale);
		float maxDepth = cameraZ + sceneRay;
		float minDepth = cameraZ - sceneRay;
		
		Matrix4f matrix = new Matrix4f();
		matrix.projectPositiveZ(fieldOfView, maxDepth, minDepth, aspectRatio);
		matrix.multiply(new Matrix4f().translateZTo(-cameraZ));
		
		return matrix;
	}
	
	public static abstract class Logic {
		
		public abstract void load() throws IOException;
		public abstract void start() throws VulkanException;
		public abstract void loop() throws VulkanException;
		public abstract void end() throws VulkanException;
	}
	
	public static void renderTexture(VulkanWindow window, Texture texture) throws VulkanException
	{
		renderTexture(window, texture, 1);
	}
	
	public static void renderTexture(VulkanWindow window, Texture texture, int samples) throws VulkanException
	{
		final String shader = "shader4";
		
		VulkanFuture<ShaderProgram> shaderProgramFuture = RenderEngineVulkan.submitAsync(() -> {
			PreparedShader vertexShader = loadShader(shader + ".vert");
			PreparedShader fragmentShader = loadShader(shader + ".frag");
			return ShaderProgram.create().add(vertexShader, fragmentShader).build();
		});
		
		Device device = window.getDevice();
		
		VulkanBuffer vertexBuffer = new VulkanBuffer(device, 28 * 4, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT);
		VulkanBuffer indexBuffer = new VulkanBuffer(device, 6 * 4, VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT | VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT);
		VulkanBuffer uniformBuffer = VulkanBuffer.ofUniformsBindings(device, 1, 16 * 2 * 4);
		uniformBuffer.getMemory().mapMemory();
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VulkanBuffer localVertexBuffer = VulkanBuffer.asLocal(vertexBuffer);
			VulkanBuffer localIndexBuffer = VulkanBuffer.asLocal(indexBuffer);
			
			localVertexBuffer.getMemory().mapMemory();
			localIndexBuffer.getMemory().mapMemory();
			
			CommandBuffer commandBuffer = device.createSingleUseCommandBuffer();
			
			texture.changeLayout(commandBuffer, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
			BufferWriter vertexBufferWriter = new BufferWriter(localVertexBuffer, vertexBuffer);
			vertexBufferWriter.setCommandBuffer(commandBuffer);
			BufferWriter indexBufferWriter = new BufferWriter(localIndexBuffer, indexBuffer);
			indexBufferWriter.setCommandBuffer(commandBuffer);
			
			RenderingDataUtils.loadRectangleUv(vertexBufferWriter, indexBufferWriter, 0, -1, -1, 1, 1, 1, 0, 0, 1, 1);
			vertexBufferWriter.close();
			indexBufferWriter.close();
			commandBuffer.endRecording();
			
			device.submitSingleUseCommandBuffer(stack, commandBuffer).join();
			
			localVertexBuffer.getMemory().unmapMemory();
			localIndexBuffer.getMemory().unmapMemory();
			localVertexBuffer.close();
			localIndexBuffer.close();
			
			RenderPass renderPass = window.getRenderPass();
			
			VkDescriptorPoolCreateInfo descriptorPoolCreateInfo = VkDescriptorPoolCreateInfo.malloc(stack)
				.sType(VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
				.pNext(0)
				.flags(0)
				.maxSets(1)
				.pPoolSizes(VkDescriptorPoolSize.malloc(2, stack)
					.apply(b -> b
						.type(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
						.descriptorCount(1)
					)
					.apply(b -> b
						.type(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
						.descriptorCount(1)
					)
					.rewind()
				);
			
			VkDescriptorSetLayoutCreateInfo descriptorSetLayoutCreateInfo = VkDescriptorSetLayoutCreateInfo.malloc(stack)
				.sType(VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
				.pNext(0)
				.flags(0)
				.pBindings(VkDescriptorSetLayoutBinding.malloc(2, stack)
					.apply(b -> b
						.binding(0)
						.descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
						.descriptorCount(1)
						.stageFlags(VK10.VK_SHADER_STAGE_VERTEX_BIT)
						.pImmutableSamplers(null))
					.apply(b -> b
						.binding(1)
						.descriptorType(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
						.descriptorCount(1)
						.stageFlags(VK10.VK_SHADER_STAGE_FRAGMENT_BIT)
						.pImmutableSamplers(null))
					.rewind()
				);
			
			DescriptorPool descriptorPool = new DescriptorPool(device, descriptorPoolCreateInfo);
			DescriptorSetLayout descriptorSetLayout = new DescriptorSetLayout(device, descriptorSetLayoutCreateInfo);
			DescriptorSet descriptorSet = descriptorPool.allocate(descriptorSetLayout);
			VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack)
				.pSetLayouts(stack.longs(descriptorSetLayout.getVk()));
			PipelineLayout pipelineLayout = new PipelineLayout(device, pipelineLayoutInfo);
			
			GraphicsPipelineTemplate graphicsPipelineTemplate = GraphicsPipelineTemplate.createScreenRenderingPipeline()
				.configureVertexInputState(s -> s
					.addBinding(0, 20, VK10.VK_VERTEX_INPUT_RATE_VERTEX)
					.addAttribute(0, 0, ShaderInputType.VECTOR_FLOAT_3, 0)
					.addAttribute(1, 0, ShaderInputType.VECTOR_FLOAT_2, 12)
				)
				.configureMultisampleState(s -> s
					.rasterizationSamples(samples)
				)
				.build();
			ShaderProgram shaderProgram = shaderProgramFuture.forceGet();
			GraphicsPipeline graphicsPipeline = graphicsPipelineTemplate.createPipeline(device, shaderProgram, window, pipelineLayout, renderPass, 0);
			Renderer renderer = Renderer.builder()
				.graphicsPipeline(graphicsPipeline)
				.descriptorSets(descriptorSet)
				.bindings(
					BindingInfo.ofUniform(uniformBuffer, VK10.VK_PIPELINE_STAGE_VERTEX_SHADER_BIT, List.of(
						ShaderVariable.TRANSFORMATION,
						ShaderVariable.MODEL
					)),
					BindingInfo.ofTexture(texture, new TextureSampler(device), VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT, ShaderVariable.TEXTURE)
				)
				.build();
			
			renderer.setBuffer(0, vertexBuffer);
			renderer.setIndexBuffer(indexBuffer);
			
			float textureAspectRatio = (float) texture.getHeight() / texture.getWidth();
			float scale = Math.min(window.getWidth(), window.getHeight());
			Matrix4f transformation = new Matrix4f().scale(Vector3f.of(scale / window.getWidth() / textureAspectRatio, scale / window.getHeight(), 0.02F));
			Matrix4f model = new Matrix4f();
			
			renderer.useBufferLayoutWriter(0, 0, writer -> {
				writer.set(ShaderVariable.TRANSFORMATION, transformation);
				writer.set(ShaderVariable.MODEL, model);
				writer.flushAndInvalidate();
			});
			
			switch (window.startPresentation())
			{
				case EMPTY_SURFACE:
					return;
				case SURFACE_LOST, OUT_OF_MEMORY, FATAL_ERROR:
					throw new VulkanException("Presentaton error");
			}
			
			FrameBuffer frameBuffer = window.getCurrentFrameBuffer();
			commandBuffer = window.getCurrentCommandBuffer();
			
			VkRenderPassBeginInfo renderPassBeginInfo = VkRenderPassBeginInfo.malloc(stack);
			renderPassBeginInfo.sType(VK10.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
			renderPassBeginInfo.pNext(0);
			renderPassBeginInfo.renderPass(renderPass.getVk());
			renderPassBeginInfo.framebuffer(frameBuffer.getVk());
			
			VkRect2D renderArea = VkRect2D.calloc(stack);
			renderArea.extent(VkExtent2D.malloc(stack).set(frameBuffer.getWidth(), frameBuffer.getHeight()));
			renderPassBeginInfo.renderArea(renderArea);
			
			VkClearValue.Buffer clearValues = VkClearValue.calloc(2, stack);
			clearValues.get().color().float32(stack.floats(0, 0, 0, 0));
			clearValues.get().depthStencil().depth(1).stencil(0);
			renderPassBeginInfo.pClearValues(clearValues.rewind());
			
			commandBuffer.beginRecording();
			commandBuffer.beginRenderPass(renderPassBeginInfo, VK10.VK_SUBPASS_CONTENTS_INLINE);
			renderer.draw(commandBuffer, 0);
			commandBuffer.endRenderPass();
			commandBuffer.endRecording();
			
			window.endPresentation(commandBuffer, () -> {
				graphicsPipeline.close();
				graphicsPipelineTemplate.close();
				pipelineLayout.close();
			});
		}
	}
}
