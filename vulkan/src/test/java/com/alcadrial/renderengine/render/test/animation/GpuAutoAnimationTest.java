package com.alcadrial.renderengine.render.test.animation;

import java.io.IOException;
import java.math.RoundingMode;
import java.nio.FloatBuffer;
import java.util.function.Function;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWWindowCloseCallbackI;
import org.lwjgl.glfw.GLFWWindowIconifyCallbackI;
import org.lwjgl.glfw.GLFWWindowMaximizeCallbackI;
import org.lwjgl.glfw.GLFWWindowRefreshCallbackI;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;

import com.alcadrial.kector.Math;
import com.alcadrial.kector.angle.Angle;
import com.alcadrial.kector.angle.AngleUnit;
import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.kector.vector.Vector3i;
import com.alcadrial.kector.vector.axis.Axis;
import com.alcadrial.renderengine.core.util.color.Color;
import com.alcadrial.renderengine.render.Computer;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.Renderer;
import com.alcadrial.renderengine.render.Renderer.Builder;
import com.alcadrial.renderengine.render.Vulkan;
import com.alcadrial.renderengine.render.buffer.StaticBigBuffer;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.renderpass.RenderPass;
import com.alcadrial.renderengine.render.renderpass.RenderPassTemplate;
import com.alcadrial.renderengine.render.shader.PreparedShader;
import com.alcadrial.renderengine.render.shader.ShaderProgram;
import com.alcadrial.renderengine.render.test.RenderTest;
import com.alcadrial.renderengine.render.texture.Texture;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.window.VulkanWindow;

public class GpuAutoAnimationTest extends RenderTest {
	
	private static final String SHADER = "shader5";
	private static final String PROJECTION = "PROJECTION";
	private static final String MODEL = "MODEL";
	private static final String LIGHT_POS = "LIGHT_POS";
	private static final String LIGHT_INTENSITY = "LIGHT_INTENSITY";
	private static final String SPACE_FOG_INTENSITY = "SPACE_FOG_INTENSITY";
	
	private static final int PRECISION = 4096;
	private static final int CUBES = 1024;
	
	public static void main(String[] args)
	{
		mainTest(args, GpuAutoAnimationTest::start, () -> defaultLoop(GpuAutoAnimationTest::loop), GpuAutoAnimationTest::end);
	}
	
	private static RenderPass renderPass;
	private static Renderer[] renderers;
	private static Computer computer;
	private static StaticBigBuffer staticBigBuffer;
	private static Texture depthTexture;
	private static Vector3f lightPos;
	private static double f;
	private static int instanceCount;
	
	private static Matrix4f buildProjectionMatrix(VulkanWindow window)
	{
		Matrix4f matrix = new Matrix4f();
		matrix.multiply(new Matrix4f().perspective(Angle.of(90, AngleUnit.DEGREES), 0.09F, -0.1F, (float) window.getWidth() / window.getHeight()));
		matrix.multiply(new Matrix4f().view(Vector3f.of(0, 0, -1), Angle.V0_DEG, Angle.V0_DEG));
		return matrix.transpose();
	}
	
	private static Matrix4f buildModelMatrix()
	{
		Matrix4f matrix = new Matrix4f();
		matrix.multiply(new Matrix4f().rotate(Axis.Z, Angle.of(f / 11, AngleUnit.DEGREES)));
		matrix.multiply(new Matrix4f().rotate(Axis.Y, Angle.of(f / 17, AngleUnit.DEGREES)));
		matrix.multiply(new Matrix4f().rotate(Axis.X, Angle.of(f / 13, AngleUnit.DEGREES)));
		matrix.multiply(buildScaleMatrix());
		return matrix;
	}
	
	private static Matrix4f buildScaleMatrix()
	{
		return new Matrix4f().scale(Vector3f.of(1, 1, 1).divideAll(16));
	}
	
	public static void start() throws VulkanException
	{
		ShaderProgram shaderProgram;
		PreparedShader computeShader;
		try
		{
			shaderProgram = ShaderProgram.create().add(loadShader(SHADER + ".vert"), loadShader(SHADER + ".frag")).build();
			computeShader = loadShader(SHADER + ".comp");
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
			RenderEngine.terminate();
			return;
		}
		
		Vulkan vulkan = RenderEngine.getVulkan();
		
		RenderEngine.setWindowHint(GLFW.GLFW_RESIZABLE, true);
		RenderEngine.setWindowHint(GLFW.GLFW_DECORATED, true);
		RenderEngine.setWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, true);
		
		VulkanWindow window = new VulkanWindow(1080, 720, "Gpu Auto Animation Test");
		vulkan.setWindow(window);
		
		if (!pickPhysicalDevice(window))
		{
			return;
		}
		
		VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.calloc();
		deviceFeatures.wideLines(true);
		deviceFeatures.samplerAnisotropy(true);
		Device device = vulkan.createDevice(deviceFeatures, KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME);
		window.init(device);
		
		window.setCallback((GLFWWindowIconifyCallbackI) (w, iconified) -> {
			System.out.println(iconified ? "Iconified" : "Restored");
		});
		window.setCallback((GLFWWindowSizeCallbackI) (w, width, height) -> {
			System.out.println("Resized " + width + "x" + height);
			for (Renderer renderer : renderers)
			{/*
				renderer.updateUniforms(deviceUniformBuffer, l -> {
					try (PhysicalDeviceTranferState transfert = staticBigBuffer.startLocalBuffering())
					{
						VulkanBuffer localUniformBuffer = transfert.mapLocal(deviceUniformBuffer);
						l.set(PROJECTION, localUniformBuffer, buildProjectionMatrix(window));
						localUniformBuffer.copyToBuffer(deviceUniformBuffer);
					}
					catch (OutOfSpaceException e)
					{
						System.out.println("Cannot update uniforms");
						e.printStackTrace(System.out);
					}
				});*/
			}
		});
		window.setCallback((GLFWWindowRefreshCallbackI) w -> {
			System.out.println("Refresh");
		});
		window.setCallback((GLFWWindowCloseCallbackI) w -> {
			System.out.println("Closed");
		});
		window.setCallback((GLFWWindowMaximizeCallbackI) (w, maximized) -> {
			System.out.println(maximized ? "Maximized" : "Restored");
		});
		
		Vector2i[] oldPos = new Vector2i[] {Vector2i.of(0, 0)};
		Vector2f[] oldMousePos = new Vector2f[] {Vector2f.of(0, 0), Vector2f.of(0, 0)};
		boolean[] pressing = new boolean[2];
		window.setCallback((GLFWMouseButtonCallbackI) (w, button, action, mods) -> {
			if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
			{
				if (pressing[0] = action == GLFW.GLFW_PRESS)
				{
					window.getMousePos(oldMousePos[0]);
				}
			}
		});
		window.setCallback((GLFWCursorPosCallbackI) (w, x, y) -> {
			if (pressing[0] && !pressing[1])
			{
				pressing[1] = true;
				window.getPos(oldPos[0]);
				window.setPos(Vector2i.of((float) x - oldMousePos[0].x() + oldPos[0].x(), (float) y - oldMousePos[0].y() + oldPos[0].y(), RoundingMode.FLOOR));
				window.getMousePos(oldMousePos[0]);
				pressing[1] = false;
			}
		});
		
		window.setCallback((GLFWKeyCallbackI) (w, k, s, a, m) -> {
			if (k == GLFW.GLFW_KEY_R)
			{
				for (Renderer renderer : renderers)
				{/*
					renderer.updateUniforms(deviceUniformBuffer, l -> {
					
						try (PhysicalDeviceTranferState transfert = staticBigBuffer.startLocalBuffering())
						{
							VulkanBuffer localUniformBuffer = transfert.mapLocal(deviceUniformBuffer);
							l.set(PROJECTION, localUniformBuffer, buildProjectionMatrix(window));
							l.set(MODEL, localUniformBuffer, buildModelMatrix());
							l.set(LIGHT_POS, localUniformBuffer, buildScaleMatrix().multiply(lightPos).xyz());
							l.set(LIGHT_INTENSITY, localUniformBuffer, 0.75F);
							l.set(SPACE_FOG_INTENSITY, localUniformBuffer, 0.1F);
							localUniformBuffer.copyToBuffer(deviceUniformBuffer);
						}
						catch (OutOfSpaceException e)
						{
							System.out.println("Cannot update uniforms");
							e.printStackTrace(System.out);
						}
					});*/
				}
			}
		});
		
		depthTexture = createDepthTexture(window.getWidth(), window.getHeight());
		
		renderPass = RenderPassTemplate.initColorDepthTextureTemplate(window.getSurface().getFormat().format()).createRenderPass(device);
		
		instanceCount = CUBES * 2;
		staticBigBuffer = new StaticBigBuffer(device, PRECISION * 2 * 4 * 4 + CUBES * 4 * 4 * 5 + 4096);
		
		VulkanBuffer deviceVertexBuffer = staticBigBuffer.allocate(240 * 4, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
		deviceInstancingVertexBuffer = staticBigBuffer.allocate(instanceCount * 4 * 4, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
		VulkanBuffer deviceIndexBuffer = staticBigBuffer.allocate(36 * 4, VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT);
		VulkanBuffer devicePathBuffer = staticBigBuffer.allocate(PRECISION * 2 * 4 * 4, VK10.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT);
		VulkanBuffer deviceAnimationBuffer = staticBigBuffer.allocate(CUBES * 4 * 4 * 3, VK10.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT);
		VulkanBuffer deviceAnimationBuffer0 = new VulkanBuffer(deviceAnimationBuffer, 0, CUBES * 4 * 4);
		VulkanBuffer deviceAnimationBuffer1 = new VulkanBuffer(deviceAnimationBuffer, CUBES * 4 * 4, CUBES * 4 * 4);
		VulkanBuffer deviceAnimationBuffer2 = new VulkanBuffer(deviceAnimationBuffer, CUBES * 4 * 4 * 2, CUBES * 4 * 4);
		deviceUniformBuffer = staticBigBuffer.allocate(2048, VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT);
		
		lightPos = Vector3f.of(5, 14, 0);
		f = 0;
		
		Color[] colors = new Color[] {
			Color.VERY_LIGHT_YELLOW,
			Color.VERY_LIGHT_RED,
			Color.VERY_LIGHT_MAGENTA,
			Color.VERY_LIGHT_GREEN,
			Color.VERY_LIGHT_CYAN,
			Color.VERY_LIGHT_BLUE
		};
		/*
				try (PhysicalDeviceTranferState transfert = staticBigBuffer.startLocalBuffering())
				{
					VulkanBuffer localVertexBuffer = transfert.mapLocal(deviceVertexBuffer);
					VulkanBuffer localInstancingVertexBuffer = transfert.mapLocal(deviceInstancingVertexBuffer);
					VulkanBuffer localIndexBuffer = transfert.mapLocal(deviceIndexBuffer);
					VulkanBuffer localPathBuffer = transfert.mapLocal(devicePathBuffer);
					VulkanBuffer localAnimationBuffer = transfert.mapLocal(deviceAnimationBuffer0);
		
					RenderingDataUtils.loadOrthoCube(localVertexBuffer, 0, localIndexBuffer, 0, 0, Vector3f.of(-1, -1, -1), Vector3f.of(0, 0, 0), (i, j) -> colors[i]);
					{
						FloatBuffer buffer = localInstancingVertexBuffer.asFloatBuffer();
						IntBuffer buffer0 = localInstancingVertexBuffer.asIntBuffer();
		
						float step = 1F / CUBES;
						for (float i = 0; i < 1; i += step)
						{
							buffer0
								.put(Float.floatToIntBits(i * 1000)).put(0).put(0)// instancePosition
								.put(0);             // animationIndex
							buffer0
								.put(Float.floatToIntBits(-i * 1000)).put(0).put(0)
								.put(1);
						}
		
						buffer.rewind();
						buffer0.rewind();
					}
					/*
					{
						FloatBuffer buffer = localPathBuffer.asFloatBuffer();
						renderCurve(buffer, Vector2f.of(-50, 0), Vector2f.of(100, 5), x -> Math.cos(8 * x * Math.PIF), PRECISION);
						renderCurve(buffer, Vector2f.of(-50, 0), Vector2f.of(100, 5), x -> -Math.cos(8 * x * Math.PIF), PRECISION);
						buffer.rewind();
					}
					*//*
						
						{
						IntBuffer buffer = localAnimationBuffer.asIntBuffer();
						for (int i = 0; i < CUBES; i++)
						{
							buffer.put(0).put(PRECISION);
							buffer.put(0).put(PRECISION);
						}
						buffer.rewind();
						}
						
						localVertexBuffer.copyToBuffer(deviceVertexBuffer);
						localInstancingVertexBuffer.copyToBuffer(deviceInstancingVertexBuffer);
						localIndexBuffer.copyToBuffer(deviceIndexBuffer);
						localPathBuffer.copyToBuffer(devicePathBuffer);
						//	localAnimationBuffer.copyToBuffer(deviceAnimationBuffer0);
						//	localAnimationBuffer.copyToBuffer(deviceAnimationBuffer1);
						//	localAnimationBuffer.copyToBuffer(deviceAnimationBuffer2);
						}
						catch (OutOfSpaceException e)
						{
						e.printStackTrace(System.out);
						RenderEngine.terminate();
						return;
						}*/
		
		computer = new Computer(device, computeShader);
		computer.setGroupCount(Vector3i.of(256, 1, 1));
		
		renderers = new Renderer[3];
		
		Builder builder = Renderer.create();
		builder.shaderProgram(shaderProgram);
		builder.depthTestEnable(true);
		builder.cullMode(VK10.VK_CULL_MODE_BACK_BIT);
		builder.topology(VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);/*
																	builder.bufferBindings(List.of(
																	new VulkanBufferRange(deviceUniformBuffer, 0, 256),
																	new VulkanBufferRange(deviceUniformBuffer, 256, 256),
																	new VulkanBufferRange(devicePathBuffer, 0),
																	new VulkanBufferRange(deviceAnimationBuffer0, 0)
																	));
																	renderers[0] = builder.build(device, window.getSurface().getSize(), renderPass, AnimationInstancingGraphicsPipeline::new);
																	builder.bufferBindings(List.of(
																	new VulkanBufferRange(deviceUniformBuffer, 512, 256),
																	new VulkanBufferRange(deviceUniformBuffer, 768, 256),
																	new VulkanBufferRange(devicePathBuffer, 0),
																	new VulkanBufferRange(deviceAnimationBuffer1, 0)
																	));
																	renderers[1] = builder.build(device, window.getSurface().getSize(), renderPass, AnimationInstancingGraphicsPipeline::new);
																	builder.bufferBindings(List.of(
																	new VulkanBufferRange(deviceUniformBuffer, 1024, 256),
																	new VulkanBufferRange(deviceUniformBuffer, 1280, 256),
																	new VulkanBufferRange(devicePathBuffer, 0),
																	new VulkanBufferRange(deviceAnimationBuffer2, 0)
																	));
																	renderers[2] = builder.build(device, window.getSurface().getSize(), renderPass, AnimationInstancingGraphicsPipeline::new);
																	*/
		for (Renderer renderer : renderers)
		{/*
			renderer.addVertexBuffer(0, deviceVertexBuffer);
			renderer.addVertexBuffer(1, deviceInstancingVertexBuffer);
			renderer.setIndexBuffer(deviceIndexBuffer);
			renderer.updateUniforms(deviceUniformBuffer, l -> {
			
				try (PhysicalDeviceTranferState transfert = staticBigBuffer.startLocalBuffering())
				{
					VulkanBuffer localUniformBuffer = transfert.mapLocal(deviceUniformBuffer);
					l.set(PROJECTION, localUniformBuffer, buildProjectionMatrix(window));
					l.set(MODEL, localUniformBuffer, buildModelMatrix());
					l.set(LIGHT_POS, localUniformBuffer, buildScaleMatrix().multiply(lightPos).xyz());
					l.set(LIGHT_INTENSITY, localUniformBuffer, 0.75F);
					l.set(SPACE_FOG_INTENSITY, localUniformBuffer, 0.1F);
					localUniformBuffer.copyToBuffer(deviceUniformBuffer);
				}
				catch (OutOfSpaceException e)
				{
					System.out.println("Cannot update uniforms");
					e.printStackTrace(System.out);
				}
			});*/
		}
	}
	
	public static void renderCurve(FloatBuffer buffer, Vector2f pos, Vector2f size, Function<Float, Float> interpolationFunction, int precision)
	{
		if (size.x() == 0 || size.y() == 0)
		{
			return;
		}
		
		float step = 1F / precision;
		Vector2f point = Vector2f.copyOf(pos);
		for (float i = 0; i < 1; i += step)
		{
			point.setAll(pos.x(), pos.y()).add(i * size.x(), Math.interpolation(0F, size.y(), i, interpolationFunction));
			buffer.put(point.x()).put(point.y()).put(0).put(0);
		}
		point.setAll(pos.x(), pos.y()).add(size.x(), Math.interpolation(0F, size.y(), 1F, interpolationFunction));
		buffer.put(point.x()).put(point.y()).put(0).put(0);
	}
	
	private static VulkanBuffer deviceInstancingVertexBuffer;
	private static VulkanBuffer deviceUniformBuffer;
	
	public static void loop(long time, long enlapsed)
	{
		VulkanWindow window = RenderEngine.getVulkan().getWindow();
		Device device = window.getDevice();
		/*
				try (PhysicalDeviceTranferState transfert = staticBigBuffer.startLocalBuffering())
				{
					VulkanBuffer localInstancingVertexBuffer = transfert.mapLocal(deviceInstancingVertexBuffer);
					{
						IntBuffer buffer = localInstancingVertexBuffer.asIntBuffer();
						float speed = 1F;
						float pass = (time * PRECISION * speed / 10000000000L) % PRECISION;
						int i = (int) pass;
						float extra = pass * 25 / PRECISION;
						float extraI = pass / PRECISION;
						float step = 1F / CUBES;
						for (float j = 0; j < 1; j += step)
						{
							buffer.put(Float.floatToIntBits(extra)).put(0).put(0).put((int) (PRECISION * j + i + extraI) % (2 * PRECISION));
							buffer.put(Float.floatToIntBits(extra)).put(0).put(0).put((int) (PRECISION * j + i + extraI + PRECISION) % (2 * PRECISION));
						}
						buffer.rewind();
					}
					localInstancingVertexBuffer.copyToBuffer(deviceInstancingVertexBuffer);
				}
				catch (OutOfSpaceException e)
				{
					e.printStackTrace(System.out);
					RenderEngine.terminate();
					return;
				}*/
		/*
				try (MemoryStack stack = MemoryStack.stackPush())
				{
					device.configureSingleUseCommandBuffer(stack, device.getGraphicsQueue(), commandBuffer -> {
		
						VkRenderPassBeginInfo renderPassBeginInfo = VkRenderPassBeginInfo.malloc(stack);
						renderPassBeginInfo.sType(VK10.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
						renderPassBeginInfo.pNext(0);
		
						renderPassBeginInfo.renderPass(renderPass.getVk());
		
						FrameBuffer frameBuffer = window.getCurrentFrameBuffer();
						renderPassBeginInfo.framebuffer(frameBuffer.getVk());
		
						VkRect2D renderArea = VkRect2D.calloc(stack);
						renderArea.extent(VkExtent2D.malloc(stack).set(frameBuffer.getWidth(), frameBuffer.getHeight()));
						renderPassBeginInfo.renderArea(renderArea);
		
						VkClearValue.Buffer clearValues = VkClearValue.calloc(2, stack);
						clearValues.get().color().float32(stack.floats(0, 0, 0, 0));
						clearValues.get().depthStencil().depth(1).stencil(0);
						renderPassBeginInfo.pClearValues(clearValues.rewind());
		
						commandBuffer.setScissor(0, 0, window.getWidth(), window.getHeight());
						commandBuffer.setViewport(0, window.getHeight(), window.getWidth(), -window.getHeight(), 0, 1);
						commandBuffer.beginRenderPass(renderPassBeginInfo, VK10.VK_SUBPASS_CONTENTS_INLINE);
						renderers[0].draw(commandBuffer, instanceCount);
						commandBuffer.endRenderPass();
						commandBuffer.endRecording();
		
						window.swapBuffers(commandBuffer);
						window.pollEvents();
						window.update();
					});
				}
				renderers[0].updateUniforms(deviceUniformBuffer, l -> {
					try (PhysicalDeviceTranferState transfert = staticBigBuffer.startLocalBuffering())
					{
						VulkanBuffer localUniformBuffer = transfert.mapLocal(deviceUniformBuffer);
						l.set(MODEL, localUniformBuffer, buildModelMatrix());
						localUniformBuffer.copyToBuffer(l.getOffset(MODEL).right * 4, 64, deviceUniformBuffer);
					}
					catch (OutOfSpaceException e)
					{
						System.out.println("Cannot update uniforms");
						e.printStackTrace(System.out);
					}
				});*/
		f = time / 10000000D;
	}
	
	public static void end()
	{
		for (Renderer renderer : renderers)
		{
			if (renderer != null)
			{
				renderer.close();
			}
		}
		RenderEngine.getVulkan().getWindow().hide();
		synchronized (MONITOR)
		{
			MONITOR.notify();
		}
		RenderEngine.terminate();
	}
}
