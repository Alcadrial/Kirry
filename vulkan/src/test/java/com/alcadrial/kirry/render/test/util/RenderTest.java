package com.alcadrial.kirry.render.test.util;

import java.io.IOException;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;
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

import com.alcadrial.kector.AngleUnit;
import com.alcadrial.kector.Math;
import com.alcadrial.kector.annotation.Angle;
import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.kector.vector.Vector3fr;
import com.alcadrial.kector.vector.axis.Axisf;
import com.alcadrial.kirry.core.cursor.CursorInputMode;
import com.alcadrial.kirry.render.Debug;
import com.alcadrial.kirry.render.Device;
import com.alcadrial.kirry.render.FrameBuffer;
import com.alcadrial.kirry.render.KirryProperties;
import com.alcadrial.kirry.render.KirryVulkan;
import com.alcadrial.kirry.render.KirryVulkan.Builder;
import com.alcadrial.kirry.render.PhysicalDevice;
import com.alcadrial.kirry.render.Renderer;
import com.alcadrial.kirry.render.buffer.VulkanBuffer;
import com.alcadrial.kirry.render.buffer.writer.DeferredBufferWriter;
import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;
import com.alcadrial.kirry.render.pipeline.GraphicsPipeline;
import com.alcadrial.kirry.render.pipeline.GraphicsPipelineTemplate;
import com.alcadrial.kirry.render.pipeline.PipelineLayout;
import com.alcadrial.kirry.render.renderpass.RenderPass;
import com.alcadrial.kirry.render.resources.FileManager;
import com.alcadrial.kirry.render.resources.ShaderResource;
import com.alcadrial.kirry.render.shader.PreparedShader;
import com.alcadrial.kirry.render.shader.Shader;
import com.alcadrial.kirry.render.shader.ShaderInputType;
import com.alcadrial.kirry.render.shader.ShaderProgram;
import com.alcadrial.kirry.render.shader.ShaderType;
import com.alcadrial.kirry.render.shader.ShaderVariable;
import com.alcadrial.kirry.render.shader.binding.BindingInfo;
import com.alcadrial.kirry.render.shader.descriptor.DescriptorPool;
import com.alcadrial.kirry.render.shader.descriptor.DescriptorSet;
import com.alcadrial.kirry.render.shader.descriptor.DescriptorSetLayout;
import com.alcadrial.kirry.render.texture.Texture;
import com.alcadrial.kirry.render.texture.TextureSampler;
import com.alcadrial.kirry.render.util.Version;
import com.alcadrial.kirry.render.util.VulkanErrorUtils;
import com.alcadrial.kirry.render.util.exception.ExternalException;
import com.alcadrial.kirry.render.util.exception.VulkanException;
import com.alcadrial.kirry.render.util.functional.VulkanBiConsumer;
import com.alcadrial.kirry.render.util.functional.VulkanRunnable;
import com.alcadrial.kirry.render.util.future.VulkanFuture;
import com.alcadrial.kirry.render.window.VulkanWindow;
import com.alcadrial.kroperties.Property;

public class RenderTest {
	
	public static final String ASSETS = "assets/kirry/";
	public static final String SHADERS = ASSETS + "shaders/";
	public static final String MODELS = ASSETS + "models/";
	public static final String TEXTURES = ASSETS + "textures/";
	public static final String FONTS = ASSETS + "fonts/";
	public static final String MATERIALS = ASSETS + "materials/";
	public static final String IMAGES = ASSETS + "images/";
	
	protected static final Object MONITOR = new Object();
	private static final String[] SEVERITIES = new String[] {"UNKNOWN", "VERBOSE", "INFO", "WARNING", "ERROR"};
	private static final String[] TYPES = new String[] {"UNKNOWN", "GENERAL", "VALIDATION", "PERFORMANCE"};
	private static final AtomicBoolean hasValidationError = new AtomicBoolean(false);
	
	protected static FileManager fileManager;
	
	public static final Vector3fr SOUTH = Axisf.Z;
	public static final Vector3fr NORTH = Axisf.NEGATIVE_Z;
	public static final Vector3fr EAST = Axisf.X;
	public static final Vector3fr WEST = Axisf.NEGATIVE_X;
	public static final Vector3fr UP = Axisf.Y;
	public static final Vector3fr DOWN = Axisf.NEGATIVE_Y;
	
	protected static boolean rebuildProjectionMatrix;
	protected static boolean rebuildViewMatrix;
	
	public interface TestRunnable {
		
		void run() throws VulkanException, InterruptedException;
	}
	
	public static void mainTest(String[] args, TestRunnable start, TestRunnable loop, TestRunnable end)
	{
		mainTest(args, start, loop, end, _ -> {});
	}
	
	public static void mainTest(String[] args, TestRunnable start, TestRunnable loop, TestRunnable end, Consumer<KirryVulkan.Builder> config)
	{
		try
		{
			TestProperties.init(args);
			//Properties.DEVICE_DESTRUCTION_LIST_THRESHOLD.setValue(0);
			
			if (KirryProperties.DEBUG.getValue()) KirryVulkan.enableDebug();
			
			TestRunnable init = () -> {
				KirryVulkan.Builder builder = initKirry();
				config.accept(builder);
				builder.initialize();
			};
			println("Kirry initialization", init, TestProperties.PRINT_LOADING_TIMES);
			if (!KirryVulkan.isInitializated()) throw new RuntimeException("Cannot initialize kirry", KirryVulkan.getLastInitializationException());
			
			if (KirryProperties.DEBUG.getValue())
			{
				Debug.setEnabledAllocationDebug(true);
				Debug.setEnabledDeallocationFail(true);
				GLFW.glfwSetErrorCallback((e, m) -> {
					try (MemoryStack stack = MemoryStack.stackPush())
					{
						Debug.error("GLFW Error " + e + ": " + stack.pointers(m).getStringUTF8());
					}
				});
			}
			if (TestProperties.LIST_DEVICES.getValue())
			{
				int i = 0;
				for (PhysicalDevice physicalDevice : KirryVulkan.getVulkan().getAvailablePhysicalDevices())
				{
					VkPhysicalDeviceProperties properties = physicalDevice.getProperties();
					System.out.println("Device " + i++ + ":");
					System.out.println("\tName: " + properties.deviceNameString());
					System.out.println("\tType: " + PhysicalDevice.typeToString(properties.deviceType()));
					System.out.println("\tId: " + properties.deviceID());
			
					int l = properties.apiVersion();
					System.out.println("\tapiVersion: " + ((l >>> 22) & 0x7F) + "." + ((l >>> 12) & 0x3FF) + "." + (l & 0xFFF) + "." + ((l >>> 29) & 0x8));
					l = properties.driverVersion();
					System.out.println("\tdriverVersion: " + ((l >>> 22) & 0x3FF) + "." + ((l >>> 12) & 0x3FF) + "." + (l & 0xFFF));
				}
			}
			
			Thread mainThread = null;
			
			if (KirryVulkan.isMultiThreading())
			{
				mainThread = Thread.ofPlatform().name("Main").start(KirryVulkan::serveKirry);
			}
			
			try
			{
				println("Start", start, TestProperties.PRINT_LOADING_TIMES);
				if (!KirryVulkan.isInitializated()) return;
				
				println("Loop", loop, TestProperties.PRINT_LOADING_TIMES);
				if (!KirryVulkan.isInitializated()) return;
				
				println("End", end, TestProperties.PRINT_LOADING_TIMES);
			}
			catch (Throwable t)
			{
				try
				{
					if (KirryVulkan.isInitializated()) KirryVulkan.terminate();
				}
				catch (Throwable e)
				{
					e.printStackTrace(System.out);
				}
				if (!(t instanceof InterruptedException)) throw t;
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
		finally
		{
			if (KirryVulkan.isInitializated())
			{
				System.err.println("Kirry was not terminated");
				KirryVulkan.terminate();
			}
		}
	}
	
	public static void defaultLoop(VulkanBiConsumer<Long, Long> action) throws VulkanException, InterruptedException
	{
		VulkanWindow window = KirryVulkan.getVulkan().getWindow();
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
	
	public interface TestLoop {
		
		void run(long enlapsed, long enlapsedFrame) throws VulkanException, InterruptedException;
	}
	
	public static void staticLoop(TestLoop action) throws VulkanException
	{
		staticLoop(action, null);
	}
	
	public static void staticLoop(TestLoop action, @Nullable Consumer<Runnable> forceRender) throws VulkanException
	{
		VulkanWindow window = KirryVulkan.getVulkan().getWindow();
		
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
				action.run(enlapsed, enlapsedFrame);
			}
			catch (VulkanException e)
			{
				lastError[0] = e;
			}
			catch (InterruptedException _)
			{}
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
	
	public static void noEventsloop(TestLoop action) throws VulkanException, InterruptedException
	{
		VulkanWindow window = KirryVulkan.getVulkan().getWindow();
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
			println("Frame " + frame, () -> action.run(enlapsed, enlapsedFrame), TestProperties.PRINT_FRAME_TIMES);
			
			frame++;
			lastCurrent = current;
		}
		window.getDevice().waitIdle();
	}
	
	public static void defaultAnimationLoop(TestLoop action) throws VulkanException, InterruptedException
	{
		VulkanWindow window = KirryVulkan.getVulkan().getWindow();
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
				println("Frame " + frame[0], () -> action.run(enlapsed, enlapsedFrame), TestProperties.PRINT_FRAME_TIMES);
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
			catch (InterruptedException _)
			{
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
		VulkanWindow window = KirryVulkan.getVulkan().getWindow();
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
	
	public static void registerRotating(BiConsumer<@Angle Float, @Angle Float> callback)
	{
		VulkanWindow window = KirryVulkan.getVulkan().getWindow();
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
				callback.accept(-rotation.x() - oldRotation.x(), rotation.y() + oldRotation.y());
			}
		});
	}
	
	public static SpatialMovementData registerSpatialMovement(VulkanWindow window, float yRotationSensivity, float xRotationSensivity)
	{
		return registerSpatialMovement(window, yRotationSensivity, xRotationSensivity, null);
	}
	
	public static SpatialMovementData registerSpatialMovement(VulkanWindow window, float yRotationSensivity, float xRotationSensivity, @Nullable GLFWKeyCallbackI callback)
	{
		SpatialMovementData data = new SpatialMovementData(Vector3f.of(0, 0, 1), Vector3f.of(0, 0, 0), 0, 0, false);
		registerSpatialMovement(window, yRotationSensivity, xRotationSensivity, callback, () -> data);
		return data;
	}
	
	/**
	 * <pre><code>
	 * x-axis mouse movement -> yRotation
	 * y-axis mouse movement -> xRotation
	 * 
	 * Key W -> movement += -Z
	 * Key A -> movement += -X
	 * Key S -> movement += Z
	 * Key D -> movement += X
	 * Key LEFT_SHIFT/LEFT_CONTROL -> movement += -Y
	 * Key SPACE -> movement += Y
	 * Key Q -> speed *= 1.1
	 * Key Z -> speed /= 1.1
	 * 
	 * Final movement = normalized((cos(yRotation) * movement.x() - sin(yRotation) * movement.z(), movement.y(), sin(yRotation) * movement.x() + cos(yRotation) * movement.z())) * speed
	 * </code></pre>
	 * 
	 * @param window
	 * @param yRotationSensivity
	 * @param xRotationSensivity
	 * @param callback
	 * @param data
	 */
	public static void registerSpatialMovement(VulkanWindow window, float yRotationSensivity, float xRotationSensivity, @Nullable GLFWKeyCallbackI callback, Supplier<SpatialMovementData> data)
	{
		Vector2f oldMousePos = Vector2f.of(0, 0);
		window.setCallback((GLFWCursorPosCallbackI) (_, x, y) -> {
			SpatialMovementData d = data.get();
			if (d.isCameraUnlocked)
			{
				d.xRotation += (y - oldMousePos.y()) * yRotationSensivity;
				if (d.xRotation > AngleUnit.RADIANS.fromDegrees(90)) d.xRotation = AngleUnit.RADIANS.fromDegrees(90);
				else if (d.xRotation < AngleUnit.RADIANS.fromDegrees(-90)) d.xRotation = -AngleUnit.RADIANS.fromDegrees(90);
				d.yRotation += (x - oldMousePos.x()) * xRotationSensivity;
				RenderTest.rebuildViewMatrix = true;
			}
			window.getMousePos(oldMousePos);
		});
		
		window.setCallback((GLFWKeyCallbackI) (w, k, s, a, m) -> {
			SpatialMovementData d = data.get();
			if (d.isCameraUnlocked)
			{
				Vector3fr v = switch (k)
				{
					case GLFW.GLFW_KEY_W -> NORTH;
					case GLFW.GLFW_KEY_A -> WEST;
					case GLFW.GLFW_KEY_S -> SOUTH;
					case GLFW.GLFW_KEY_D -> EAST;
					case GLFW.GLFW_KEY_SPACE -> UP;
					case GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_LEFT_CONTROL -> DOWN;
					default -> Vector3fr.of();
				};
				
				switch (a)
				{
					case GLFW.GLFW_PRESS -> d.movement.add(v);
					case GLFW.GLFW_RELEASE -> d.movement.subtract(v);
				}
			}
			
			if (a == GLFW.GLFW_RELEASE)
			{
				switch (k)
				{
					case GLFW.GLFW_KEY_U -> {
						d.isCameraUnlocked = !d.isCameraUnlocked;
						window.setCursorInputMode(d.isCameraUnlocked ? CursorInputMode.DISABLED : CursorInputMode.NORMAL);
					}
					case GLFW.GLFW_KEY_P -> {
						d.position.setAll(0, 0, 0);
						RenderTest.rebuildViewMatrix = true;
					}
				}
			}
			if (a == GLFW.GLFW_REPEAT || a == GLFW.GLFW_PRESS)
			{
				switch (k)
				{
					case GLFW.GLFW_KEY_Y -> {
						SpatialMovementData dd = data.get();
						if (dd.isCameraUnlocked)
						{
							dd.xRotation += (0 - oldMousePos.y()) * yRotationSensivity;
							if (dd.xRotation > AngleUnit.RADIANS.fromDegrees(90)) dd.xRotation = AngleUnit.RADIANS.fromDegrees(90);
							else if (dd.xRotation < AngleUnit.RADIANS.fromDegrees(-90)) dd.xRotation = -AngleUnit.RADIANS.fromDegrees(90);
							dd.yRotation += (0.1F - oldMousePos.x()) * xRotationSensivity;
							RenderTest.rebuildViewMatrix = true;
						}
						window.getMousePos(oldMousePos);
					}
					case GLFW.GLFW_KEY_H -> {
						SpatialMovementData dd = data.get();
						if (dd.isCameraUnlocked)
						{
							dd.xRotation += (0.1F - oldMousePos.y()) * yRotationSensivity;
							if (dd.xRotation > AngleUnit.RADIANS.fromDegrees(90)) dd.xRotation = AngleUnit.RADIANS.fromDegrees(90);
							else if (dd.xRotation < AngleUnit.RADIANS.fromDegrees(-90)) dd.xRotation = -AngleUnit.RADIANS.fromDegrees(90);
							dd.yRotation += (0 - oldMousePos.x()) * xRotationSensivity;
							RenderTest.rebuildViewMatrix = true;
						}
						window.getMousePos(oldMousePos);
					}
					case GLFW.GLFW_KEY_T -> {
						SpatialMovementData dd = data.get();
						if (dd.isCameraUnlocked)
						{
							dd.xRotation += (0 - oldMousePos.y()) * yRotationSensivity;
							if (dd.xRotation > AngleUnit.RADIANS.fromDegrees(90)) dd.xRotation = AngleUnit.RADIANS.fromDegrees(90);
							else if (dd.xRotation < AngleUnit.RADIANS.fromDegrees(-90)) dd.xRotation = -AngleUnit.RADIANS.fromDegrees(90);
							dd.yRotation += (-0.1F - oldMousePos.x()) * xRotationSensivity;
							RenderTest.rebuildViewMatrix = true;
						}
						window.getMousePos(oldMousePos);
					}
					case GLFW.GLFW_KEY_G -> {
						SpatialMovementData dd = data.get();
						if (dd.isCameraUnlocked)
						{
							dd.xRotation += (-0.1F - oldMousePos.y()) * yRotationSensivity;
							if (dd.xRotation > AngleUnit.RADIANS.fromDegrees(90)) dd.xRotation = AngleUnit.RADIANS.fromDegrees(90);
							else if (dd.xRotation < AngleUnit.RADIANS.fromDegrees(-90)) dd.xRotation = -AngleUnit.RADIANS.fromDegrees(90);
							dd.yRotation += (0 - oldMousePos.x()) * xRotationSensivity;
							RenderTest.rebuildViewMatrix = true;
						}
						window.getMousePos(oldMousePos);
					}
				}
			}
			if (a == GLFW.GLFW_REPEAT)
			{
				switch (k)
				{
					case GLFW.GLFW_KEY_Q -> {
						d.speed *= 1.1F;
					}
					case GLFW.GLFW_KEY_Z -> {
						d.speed /= 1.1F;
					}
				}
			}
			if (callback != null) callback.invoke(w, k, s, a, m);
		});
	}
	
	public static boolean calculatePosition(SpatialMovementData data, float enlapsed)
	{
		if (!data.movement.equals(Vector3fr.of()))
		{
			float sin = Math.sin(data.yRotation);
			float cos = Math.cos(data.yRotation);
			Vector3f movement = Vector3f.of(0, 0, 0);
			movement.add(cos * data.movement.x(), 0, sin * data.movement.x());
			movement.add(-sin * data.movement.z(), 0, cos * data.movement.z());
			movement.add(0, data.movement.y(), 0);
			
			data.position.add(movement.normalize().multiplyAll(enlapsed / 1000000000F * data.speed));
			RenderTest.rebuildViewMatrix = true;
			return true;
		}
		return false;
	}
	
	public static Builder initKirry() throws VulkanException
	{
		return KirryVulkan.create("Test", new Version(0, 0, 1, 0))
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
	
	public static Texture createDepthTexture(CommandBuffer commandBuffer, int width, int height) throws VulkanException
	{
		Texture depthTexture = Texture.create2dTexture(KirryVulkan.getVulkan().getDevice(), width, height, VK10.VK_FORMAT_D24_UNORM_S8_UINT, VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT, VK10.VK_IMAGE_ASPECT_DEPTH_BIT | VK10.VK_IMAGE_ASPECT_STENCIL_BIT);
		depthTexture.changeLayout(commandBuffer, VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
		return depthTexture;
	}
	
	public static Path getPath(String path)
	{
		if (fileManager != null)
		{
			return fileManager.getPath(path);
		}
		URL url = ClassLoader.getSystemClassLoader().getResource(ASSETS + path);
		if (url != null)
		{
			try
			{
				return Path.of(url.toURI());
			}
			catch (URISyntaxException _)
			{}
		}
		if (KirryProperties.RESOURCE_PATH.isPresent()) return Path.of(KirryProperties.RESOURCE_PATH.getValue(), ASSETS + path);
		return Path.of(ASSETS + path);
	}
	
	public static PreparedShader loadShader(String pathname) throws IOException
	{
		if (fileManager != null)
		{
			ShaderResource resource = fileManager.getResource(pathname, ShaderResource.TYPE);
			resource.setForceRecompile(TestProperties.FORCE_RECOMPILE_SHADERS.getValue());
			try
			{
				resource.load().join();
			}
			catch (CompletionException e)
			{
				Throwable cause = e.getCause();
				if (cause instanceof IOException ioe) throw ioe;
				throw new IOException(cause);
			}
			catch (Exception e)
			{
				throw new IOException(e);
			}
		}
		Path path = getPath(pathname);
		Shader shader = new Shader(path);
		ShaderType type = ShaderType.fromPath(path);
		if (TestProperties.FORCE_RECOMPILE_SHADERS.getValue()) shader.compile(type);
		else
		{
			Path binPath = getPath(pathname + ".bin");
			if (Files.isReadable(binPath)) shader.loadCompiled(binPath);
			else shader.compile(type);
		}
		if (!shader.isCompilationSuccessfull()) throw new IOException("Shader " + shader.getPath() + " has errors");
		return new PreparedShader(shader, type);
	}
	
	public static void saveShaderBinary(Shader shader) throws IOException
	{
		Files.write(Path.of(VulkanErrorUtils.nonNull(shader.getPath()) + ".bin"), shader.getLastCompilation().get().binary());
	}
	
	public static void println(String s, TestRunnable action, Property<Boolean> printProperty) throws VulkanException, InterruptedException
	{
		if (printProperty.getValue()) System.out.println(s + " time = " + recordTime(action, TimeUnit.MICROSECONDS) / 1000D + "ms");
		else action.run();
	}
	
	public static long recordTime(TestRunnable runnable, TimeUnit unit) throws VulkanException, InterruptedException
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
	
	public static Matrix4f buildFullSceneMatrix(VulkanWindow window, @Angle float fieldOfView)
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
	
	public static class SpatialMovementData {
		
		public Vector3f position;
		public Vector3f movement;
		public float speed;
		public @Angle float xRotation;
		public @Angle float yRotation;
		public boolean isCameraUnlocked;
		
		public SpatialMovementData(Vector3f position, Vector3f movement, @Angle float xRotation, @Angle float yRotation, boolean isCameraUnlocked)
		{
			this.position = position;
			this.movement = movement;
			this.xRotation = xRotation;
			this.yRotation = yRotation;
			this.isCameraUnlocked = isCameraUnlocked;
			speed = 1;
		}
		
		public SpatialMovementData(Vector3f position)
		{
			this.position = position;
			movement = Vector3f.of();
			xRotation = 0;
			yRotation = 0;
			isCameraUnlocked = false;
			speed = 1;
		}
	}
	
	public static void renderTexture(VulkanWindow window, Texture texture) throws VulkanException, InterruptedException
	{
		renderTexture(window, texture, 1);
	}
	
	public static void renderTexture(VulkanWindow window, Texture texture, int samples) throws VulkanException, InterruptedException
	{
		final String shader = "shaders/shader4";
		
		VulkanFuture<ShaderProgram> shaderProgramFuture = VulkanFuture.submitAsync(() -> {
			PreparedShader vertexShader = loadShader(shader + ".vert");
			PreparedShader fragmentShader = loadShader(shader + ".frag");
			return ShaderProgram.create().add(vertexShader, fragmentShader).build();
		});
		
		Device device = window.getDevice();
		
		VulkanBuffer vertexBuffer = new VulkanBuffer(device, 28 * 4, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT);
		vertexBuffer.setName("Vertex buffer");
		VulkanBuffer indexBuffer = new VulkanBuffer(device, 6 * 4, VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT | VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT);
		vertexBuffer.setName("Index buffer");
		VulkanBuffer uniformBuffer = VulkanBuffer.ofUniformsBindings(device, 1, 16 * 2 * 4);
		vertexBuffer.setName("Uniform buffer");
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VulkanBuffer localVertexBuffer = VulkanBuffer.asLocal(vertexBuffer);
			VulkanBuffer localIndexBuffer = VulkanBuffer.asLocal(indexBuffer);
			
			CommandBuffer commandBuffer = device.createSingleUseCommandBuffer();
			
			texture.changeLayout(commandBuffer, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
			DeferredBufferWriter vertexBufferWriter = new DeferredBufferWriter(localVertexBuffer, vertexBuffer);
			DeferredBufferWriter indexBufferWriter = new DeferredBufferWriter(localIndexBuffer, indexBuffer);
			
			RenderingDataUtils.loadRectangleUv(vertexBufferWriter, indexBufferWriter, 0, -1, -1, 1, 1, 1, 0, 0, 1, 1);
			vertexBufferWriter.transferAll(commandBuffer);
			indexBufferWriter.transferAll(commandBuffer);
			commandBuffer.addCompletionAction(() -> {
				localVertexBuffer.close();
				localIndexBuffer.close();
			});
			commandBuffer.endRecording();
			
			VulkanFuture<?> transferFuture = device.submitSingleUseCommandBuffer(stack, commandBuffer);
			
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
			ShaderProgram shaderProgram = shaderProgramFuture.get();
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
			Matrix4f transformation = new Matrix4f().scale(Vector3fr.of(scale / window.getWidth() / textureAspectRatio, scale / window.getHeight(), 0.02F));
			Matrix4f model = new Matrix4f();
			
			renderer.useBufferLayoutWriter(0, 0, writer -> {
				writer.set(ShaderVariable.TRANSFORMATION, transformation);
				writer.set(ShaderVariable.MODEL, model);
				writer.flush();
			});
			
			transferFuture.join();
			
			
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
			
			commandBuffer.beginRenderPass(renderPassBeginInfo, VK10.VK_SUBPASS_CONTENTS_INLINE);
			renderer.draw(commandBuffer, 0);
			commandBuffer.endRenderPass();

			window.show();
			window.endPresentation(commandBuffer, () -> {
				graphicsPipeline.close();
				graphicsPipelineTemplate.close();
				pipelineLayout.close();
				vertexBuffer.close();
				indexBuffer.close();
				uniformBuffer.close();
			});
		}
	}

	public static final float XRotationSensivity = 0.005F;
	public static final float YRotationSensivity = 0.01F;
}
