package com.alcadrial.renderengine.render.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;

import com.alcadrial.kector.angle.AngleUnit;
import com.alcadrial.kector.annotation.Angle;
import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.renderengine.core.component.Entity;
import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.FrameBuffer;
import com.alcadrial.renderengine.render.PhysicalDevice;
import com.alcadrial.renderengine.render.RenderEngineVulkan;
import com.alcadrial.renderengine.render.buffer.StaticBigBuffer;
import com.alcadrial.renderengine.render.commandbuffer.CommandBuffer;
import com.alcadrial.renderengine.render.component.ColorTextureComponent;
import com.alcadrial.renderengine.render.component.EntityRenderer;
import com.alcadrial.renderengine.render.component.MeshComponent;
import com.alcadrial.renderengine.render.component.SpatialEngine;
import com.alcadrial.renderengine.render.shader.PreparedShader;
import com.alcadrial.renderengine.render.shader.ShaderInputType;
import com.alcadrial.renderengine.render.shader.ShaderProgram;
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.test.util.RenderTest;
import com.alcadrial.renderengine.render.test.util.TestProperties;
import com.alcadrial.renderengine.render.util.VulkanFuture;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.window.VulkanWindow;

public class ComponentTest extends RenderTest {
	
	private static final String SHADER = "shader5";
	
	public static void main(String[] args)
	{
		mainTest(args, ComponentTest::start, () -> defaultAnimationLoop(ComponentTest::loop), ComponentTest::end);
	}
	
	private static MutableList<Entity> entities;
	private static EntityRenderer entityRenderer;
	private static SpatialEngine spatialEngine;
	
	private static List<ShaderProgram> shaders;
	private static int currentShaderIndex;
	private static StaticBigBuffer staticBigBuffer;
	
	private static ShaderVariable lightPos = ShaderVariable.of(ShaderInputType.VECTOR_FLOAT_3, "LIGHT_POS");
	private static ShaderVariable lightIntensity = ShaderVariable.of(ShaderInputType.FLOAT, "LIGHT_INTENSITY");
	private static ShaderVariable spaceFogIntensity = ShaderVariable.of(ShaderInputType.FLOAT, "SPACE_FOG_INTENSITY");
	
	private static @Angle(AngleUnit.RADIANS) float xRotation;
	private static @Angle(AngleUnit.RADIANS) float yRotation;
	private static @Angle(AngleUnit.RADIANS) float zRotation;
	private static boolean isModifiedModel;
	private static boolean isModified;
	private static boolean reloadShaders;
	
	private static Matrix4f buildModelMatrix(@Angle(AngleUnit.RADIANS) float xRotation, @Angle(AngleUnit.RADIANS) float yRotation, @Angle(AngleUnit.RADIANS) float zRotation)
	{
		Matrix4f matrix = new Matrix4f();
		matrix.multiply(new Matrix4f().rotateX(xRotation));
		matrix.multiply(new Matrix4f().rotateY(yRotation));
		matrix.multiply(new Matrix4f().rotateZ(zRotation));
		matrix.multiply(new Matrix4f().scale(Vector3f.of(1, 1, 1).divideAll((float) Math.sqrt(3))));
		matrix.multiply(new Matrix4f().translate(Vector3f.of(-0.5F, -0.5F, -0.5F)));
		return matrix;
	}
	
	public static void start() throws VulkanException
	{
		shaders = Lists.mutable.empty();
		ShaderProgram shaderProgram;
		PreparedShader initComputeShader;
		PreparedShader transformComputeShader;
		try
		{
			PreparedShader vertexShader = loadShader(SHADER + ".vert");
			shaders.add(ShaderProgram.create().add(vertexShader, loadShader(SHADER + ".frag")).build());
			shaders.add(ShaderProgram.create().add(vertexShader, loadShader(SHADER + ".1.frag")).build());
			shaderProgram = shaders.getFirst();
			currentShaderIndex = 0;
			
			initComputeShader = loadShader("shaders/" + EntityRenderer.INIT_COMPUTE_SHADER_NAME);
			transformComputeShader = loadShader("shaders/" + EntityRenderer.TRANSFORM_COMPUTE_SHADER_NAME);
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
			RenderEngineVulkan.terminate();
			return;
		}
		
		RenderEngineVulkan.setWindowHint(GLFW.GLFW_RESIZABLE, true);
		RenderEngineVulkan.setWindowHint(GLFW.GLFW_DECORATED, true);
		RenderEngineVulkan.setWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, true);
		
		VulkanWindow window = VulkanWindow.defaultBuilder()
			.physicalDeviceStrategy(PhysicalDevice.Strategy.atIndexOrElse(TestProperties.DEVICE.getValue(), PhysicalDevice.Strategy.findBest()))
			.deviceFeatures(f -> f
				.samplerAnisotropy(true)
			)
			.build(1080, 720, "Test");
		Device device = window.getDevice();
		
		entities = Lists.mutable.of();
		for (int i = 0; i < 10; i++)
		{
			Entity e = Entity.of(ColorTextureComponent.Type.INSTANCE.getId());
			e.setComponent(MeshComponent.Type.INSTANCE, MeshComponent.ofFileObj(getFile("models/model" + i + ".obj")));
			e.setComponent(ColorTextureComponent.Type.INSTANCE, ColorTextureComponent.ofFile(device, getFile("textures/model" + i + ".png")));
			entities.add(e);
		}
		
		VulkanFuture<?> entitiesLoading = RenderEngineVulkan.submitAsync(() -> {
			FastList<CompletableFuture<?>> futures = new FastList<>();
			for (Entity e : entities)
			{
				futures.add(e.load().exceptionally(ex -> {
					System.out.println("Failed to load model " + e.getComponent(MeshComponent.Type.INSTANCE));
					if (!FileNotFoundException.class.isAssignableFrom(ex.getCause().getClass())) ex.getCause().printStackTrace();
					return null;
				}));
			}
			CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
		});
		
		window.setCallback((GLFWWindowSizeCallbackI) (_, _, _) -> {
			isModified = true;
		});
		window.setCallback((GLFWKeyCallbackI) (_, k, _, a, _) -> {
			if (a == GLFW.GLFW_PRESS)
			{
				switch (k)
				{
					case GLFW.GLFW_KEY_UP -> {
						currentShaderIndex = (currentShaderIndex == 0 ? shaders.size() : currentShaderIndex) - 1;
						reloadShaders = true;
					}
					case GLFW.GLFW_KEY_DOWN -> {
						currentShaderIndex = (currentShaderIndex == shaders.size() - 1 ? 0 : currentShaderIndex + 1);
						reloadShaders = true;
					}
					case GLFW.GLFW_KEY_F2 -> {
						List<ShaderProgram> newShaders = Lists.mutable.empty();
						try
						{
							PreparedShader vertexShader = loadShader(SHADER + ".vert");
							newShaders.add(ShaderProgram.create().add(vertexShader, loadShader(SHADER + ".frag")).build());
							newShaders.add(ShaderProgram.create().add(vertexShader, loadShader(SHADER + ".1.frag")).build());
						}
						catch (IOException e)
						{
							System.out.println("Failed to reload shaders: " + e.getLocalizedMessage());
							return;
						}
						shaders.clear();
						shaders = newShaders;
						reloadShaders = true;
					}
				}
			}
		});
		
		registerRotating((x, y) -> {
			xRotation = x;
			yRotation = y;
			isModifiedModel = true;
		});
		
		entityRenderer = new EntityRenderer(device, entities);
		spatialEngine = new SpatialEngine(entities);
		
		Matrix4f projectionMatrix = buildFullSceneMatrix(window, (float) (Math.PI * 2 / 3));
		Matrix4f modelMatrix = buildModelMatrix(xRotation, yRotation, zRotation);
		
		entitiesLoading.join();
		
		EntityRenderer.Properties entityRendererProperties = entityRenderer.getProperties();
		staticBigBuffer = new StaticBigBuffer(device, entityRendererProperties.bufferSize());
		entityRenderer.init(new EntityRenderer.InitParams(shaderProgram, initComputeShader, transformComputeShader, staticBigBuffer, new EntityRenderer.SurfaceTarget(window), Lists.mutable.of(ShaderVariable.PROJECTION, ShaderVariable.MODEL), Lists.mutable.of(lightPos, lightIntensity, spaceFogIntensity)));
		entityRenderer.setUniforms((i, writer) -> {
			switch (i)
			{
				case 0 -> {
					writer.set(ShaderVariable.MODEL, modelMatrix);
					writer.set(ShaderVariable.PROJECTION, projectionMatrix);
					writer.flushAndInvalidate();
				}
				case 1 -> {
					writer.set(lightPos, Vector3f.of(0.3F, 1, 0));
					writer.set(lightIntensity, 0.75F);
					writer.set(spaceFogIntensity, 0.1F);
					writer.flushAndInvalidate();
				}
			}
		});
		
		Debug.runOnDebug(() -> {
			staticBigBuffer.setName("Big buffer");
		});
	}
	
	public static void loop(long time, long enlapsed) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VulkanWindow window = RenderEngineVulkan.getVulkan().getWindow();
			switch (window.startPresentation())
			{
				case EMPTY_SURFACE:
					return;
				case SURFACE_LOST, OUT_OF_MEMORY, FATAL_ERROR:
					throw new VulkanException("Presentaton error");
			}
			if (isModified || reloadShaders)
			{
				entityRenderer.reinit(new EntityRenderer.ReinitParams(reloadShaders ? shaders.get(currentShaderIndex) : null, null, null, isModified ? new EntityRenderer.SurfaceTarget(window) : null));
				if (isModified)
				{
					Matrix4f projectionMatrix = buildFullSceneMatrix(window, (float) (Math.PI * 2 / 3));
					entityRenderer.setUniforms((i, writer) -> {
						switch (i)
						{
							case 0 -> {
								writer.set(ShaderVariable.PROJECTION, projectionMatrix);
								writer.flushAndInvalidate();
							}
						}
					});
				}
				reloadShaders = false;
				isModified = false;
			}
			if (isModifiedModel)
			{
				isModifiedModel = false;
				
				Matrix4f modelMatrix = buildModelMatrix(xRotation, yRotation, zRotation);
				entityRenderer.setUniforms((i, writer) -> {
					switch (i)
					{
						case 0 -> {
							writer.set(ShaderVariable.MODEL, modelMatrix);
							writer.flushAndInvalidate();
						}
					}
				});
			}
			
			CommandBuffer commandBuffer = window.getCurrentCommandBuffer();
			
			VkRenderPassBeginInfo renderPassBeginInfo = VkRenderPassBeginInfo.malloc(stack);
			renderPassBeginInfo.sType(VK10.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
			renderPassBeginInfo.pNext(0);
			renderPassBeginInfo.renderPass(window.getRenderPass().getVk());
			
			FrameBuffer frameBuffer = window.getCurrentFrameBuffer();
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
			entityRenderer.update(new EntityRenderer.UpdateParams(commandBuffer, window.getFrameIndex()));
			commandBuffer.endRenderPass();
			commandBuffer.endRecording();
			window.endPresentation(commandBuffer);
		}
	}
	
	public static void end()
	{
		staticBigBuffer.close();
		RenderEngineVulkan.terminate();
	}
}
