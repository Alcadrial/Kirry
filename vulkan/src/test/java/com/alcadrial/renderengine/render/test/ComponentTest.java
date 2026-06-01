package com.alcadrial.renderengine.render.test;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

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
import com.alcadrial.renderengine.render.commandbuffer.CommandBuffer;
import com.alcadrial.renderengine.render.component.EntityRenderer;
import com.alcadrial.renderengine.render.component.MeshComponent;
import com.alcadrial.renderengine.render.component.SpatialEngine;
import com.alcadrial.renderengine.render.memory.MemoryManager;
import com.alcadrial.renderengine.render.resources.FileManager;
import com.alcadrial.renderengine.render.resources.Mesh;
import com.alcadrial.renderengine.render.resources.ShaderResource;
import com.alcadrial.renderengine.render.resources.TextureResource;
import com.alcadrial.renderengine.render.shader.PreparedShader;
import com.alcadrial.renderengine.render.shader.ShaderInputType;
import com.alcadrial.renderengine.render.shader.ShaderProgram;
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.test.util.RenderTest;
import com.alcadrial.renderengine.render.test.util.TestProperties;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.util.future.VulkanFuture;
import com.alcadrial.renderengine.render.window.VulkanWindow;

public class ComponentTest extends RenderTest {
	
	private static final String SHADER = SHADERS + "shader5";
	
	public static void main(String[] args)
	{
		mainTest(args, ComponentTest::start, () -> defaultAnimationLoop(ComponentTest::loop), ComponentTest::end);
	}
	
	private static MutableList<Mesh> models;
	private static MutableList<Entity> entities;
	private static EntityRenderer entityRenderer;
	private static SpatialEngine spatialEngine;
	
	private static MutableList<ShaderProgram> shaderPrograms;
	private static MutableList<PreparedShader> shaders;
	private static int currentShaderIndex;
	private static MemoryManager memoryManager;
	
	private static ShaderVariable lightPos = ShaderVariable.of(ShaderInputType.VECTOR_FLOAT_3, "LIGHT_POS");
	private static ShaderVariable lightIntensity = ShaderVariable.of(ShaderInputType.FLOAT, "LIGHT_INTENSITY");
	private static ShaderVariable spaceFogIntensity = ShaderVariable.of(ShaderInputType.FLOAT, "SPACE_FOG_INTENSITY");
	
	private static @Angle(AngleUnit.RADIANS) float xRotation;
	private static @Angle(AngleUnit.RADIANS) float yRotation;
	private static @Angle(AngleUnit.RADIANS) float zRotation;
	private static boolean isModifiedModel;
	private static boolean isModified;
	private static boolean reloadShaders;
	private static boolean reloadModels;
	
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
		try
		{
			fileManager = FileManager.of(
				"src/test/resources",
				"src/main/resources",
				FileManager.loaderJarResourceUri(ASSETS)
				);
			fileManager.registerResourceType(ShaderResource.TYPE, ShaderResource::new);
			fileManager.registerResourceType(TextureResource.TYPE, TextureResource::new);
			fileManager.registerResourceType(Mesh.TYPE, (p, m) -> new Mesh(p, m.getPath(MATERIALS), m.getPath(TEXTURES), m));
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
			end();
			return;
		}
		
		shaderPrograms = Lists.mutable.empty();
		shaders = Lists.mutable.empty();
		try
		{
			ShaderResource vertexShaderResource = fileManager.getResource(SHADER + ".vert", ShaderResource.TYPE);
			ShaderResource fragmentShaderResource = fileManager.getResource(SHADER + ".frag", ShaderResource.TYPE);
			ShaderResource fragment1ShaderResource = fileManager.getResource(SHADER + ".1.frag", ShaderResource.TYPE);
			ShaderResource initComputeShaderResource = fileManager.getResource(EntityRenderer.INIT_COMPUTE_SHADER_PATHNAME, ShaderResource.TYPE);
			ShaderResource transformComputeShaderResource = fileManager.getResource(EntityRenderer.TRANSFORM_COMPUTE_SHADER_PATHNAME, ShaderResource.TYPE);
			
			vertexShaderResource.setForceRecompile(TestProperties.FORCE_RECOMPILE_SHADERS.getValue());
			fragmentShaderResource.setForceRecompile(TestProperties.FORCE_RECOMPILE_SHADERS.getValue());
			fragment1ShaderResource.setForceRecompile(TestProperties.FORCE_RECOMPILE_SHADERS.getValue());
			initComputeShaderResource.setForceRecompile(TestProperties.FORCE_RECOMPILE_SHADERS.getValue());
			transformComputeShaderResource.setForceRecompile(TestProperties.FORCE_RECOMPILE_SHADERS.getValue());
			
			try
			{
				CompletableFuture.allOf(
					vertexShaderResource.load(),
					fragmentShaderResource.load(),
					fragment1ShaderResource.load(),
					initComputeShaderResource.load(),
					transformComputeShaderResource.load()
					).join();
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
			
			PreparedShader vertexShader = vertexShaderResource.getShader();
			shaderPrograms.add(ShaderProgram.create().add(vertexShader, fragmentShaderResource.getShader()).build());
			shaderPrograms.add(ShaderProgram.create().add(vertexShader, fragment1ShaderResource.getShader()).build());
			currentShaderIndex = 0;
			
			shaders.add(initComputeShaderResource.getShader());
			shaders.add(transformComputeShaderResource.getShader());
			
			fileManager.registerForChanges(vertexShaderResource, r -> {
				MutableList<ShaderProgram> newShaders = Lists.mutable.empty();
				
				r.reload().join();
				PreparedShader shader = r.getShader();
				newShaders.add(ShaderProgram.create().add(shader, shaderPrograms.get(0).getShaders().get(1)).build());
				newShaders.add(ShaderProgram.create().add(shader, shaderPrograms.get(1).getShaders().get(1)).build());
				
				MutableList<ShaderProgram> oldShaders = shaderPrograms;
				shaderPrograms = newShaders;
				oldShaders.clear();
				reloadShaders = true;
			});
			fileManager.registerForChanges(fragmentShaderResource, r -> {
				MutableList<ShaderProgram> newShaders = Lists.mutable.empty();
				
				r.reload().join();
				PreparedShader shader = r.getShader();
				newShaders.add(ShaderProgram.create().add(shaderPrograms.get(0).getShaders().get(0), shader).build());
				newShaders.add(shaderPrograms.get(1));
				
				MutableList<ShaderProgram> oldShaders = shaderPrograms;
				shaderPrograms = newShaders;
				oldShaders.clear();
				reloadShaders = true;
			});
			fileManager.registerForChanges(fragment1ShaderResource, r -> {
				MutableList<ShaderProgram> newShaders = Lists.mutable.empty();
				
				r.reload().join();
				PreparedShader shader = r.getShader();
				newShaders.add(shaderPrograms.get(0));
				newShaders.add(ShaderProgram.create().add(shaderPrograms.get(0).getShaders().get(0), shader).build());
				
				MutableList<ShaderProgram> oldShaders = shaderPrograms;
				shaderPrograms = newShaders;
				oldShaders.clear();
				reloadShaders = true;
			});
			fileManager.startWatchingResources();
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
			end();
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
		
		models = Lists.mutable.empty();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(fileManager.getPath(ASSETS + "models"), "*.obj"))
		{
			for (Path path : stream)
			{
				models.add(fileManager.getResource(path, Mesh.TYPE));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace(System.out);
			end();
			return;
		}
		
		if (models.isEmpty())
		{
			System.out.println("0 models found");
			end();
			return;
		}
		
		entities = Lists.mutable.of();
		
		VulkanFuture<?> modelsLoading = VulkanFuture.submitAsync(() -> {
			
			for (Mesh mesh : models)
			{
				for (int i = 0; i < 2; i++)
				{
					Entity e = Entity.of(MeshComponent.TYPE.getId());
					e.setComponent(MeshComponent.TYPE, MeshComponent.ofMesh(mesh));
					entities.add(e);
				}
			}
			
			FastList<CompletableFuture<?>> futures = new FastList<>();
			
			for (Entity e : entities)
			{
				futures.add(e.load().exceptionally(ex -> {
					System.out.println("Failed to load entity: " + ex.getLocalizedMessage());
					return null;
				}));
			}
			
			CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
		});
		
		window.setCallback((GLFWWindowSizeCallbackI) (_, _, _) -> {
			isModified = true;
		});
		window.setCallback((GLFWKeyCallbackI) (_, k, _, a, m) -> {
			if (a == GLFW.GLFW_PRESS)
			{
				switch (k)
				{
					case GLFW.GLFW_KEY_UP -> {
						currentShaderIndex = (currentShaderIndex == 0 ? shaderPrograms.size() : currentShaderIndex) - 1;
						reloadShaders = true;
					}
					case GLFW.GLFW_KEY_DOWN -> {
						currentShaderIndex = (currentShaderIndex == shaderPrograms.size() - 1 ? 0 : currentShaderIndex + 1);
						reloadShaders = true;
					}
					case GLFW.GLFW_KEY_F2 -> {
						MutableList<ShaderProgram> newShaders = Lists.mutable.empty();
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
						shaderPrograms.clear();
						shaderPrograms = newShaders;
						reloadShaders = true;
					}
					case GLFW.GLFW_KEY_F3 -> {
						
						if ((m & GLFW.GLFW_MOD_CONTROL) != 0)
						{
							MutableList<Mesh> newModels = Lists.mutable.empty();
							try (DirectoryStream<Path> stream = Files.newDirectoryStream(fileManager.getPath(ASSETS + "models"), "*.obj"))
							{
								for (Path path : stream)
								{
									models.add(fileManager.getResource(path, Mesh.TYPE));
								}
							}
							catch (Exception e)
							{
								System.out.println("Failed to reload models: " + e.getLocalizedMessage());
								return;
							}
							models.clear();
							models = newModels;
						}
						
						List<CompletableFuture<?>> futures = Lists.mutable.empty();
						for (Mesh mesh : models)
						{
							futures.add(mesh.load().exceptionally(ex -> {
								String name = mesh.getName();
								System.out.println("Failed to load model " + (name != null ? name : mesh.getPath()) + ": " + ex.getLocalizedMessage());
								return null;
							}));
						}
						
						reloadModels = true;
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
		
		modelsLoading.join();
		
		Matrix4f projectionMatrix = buildFullSceneMatrix(window, (float) (Math.PI * 2 / 3));
		Matrix4f modelMatrix = buildModelMatrix(xRotation, yRotation, zRotation);
		
		memoryManager = MemoryManager.getDefault(device);
		
		Debug.runOnDebug(() -> {
			memoryManager.setName("MemoryManager");
		});
		
		VulkanFuture<?> entityRendererInitFuture = device.configureAndSubmitSingleUseCommandBuffer(device.getGraphicsQueue(), commandBuffer -> {
			entityRenderer.init(new EntityRenderer.InitParams(shaderPrograms.getFirst(), shaders.get(0), shaders.get(1), memoryManager, commandBuffer, new EntityRenderer.SurfaceTarget(window), Lists.mutable.of(ShaderVariable.PROJECTION, ShaderVariable.MODEL), Lists.mutable.of(lightPos, lightIntensity, spaceFogIntensity)));
		});
		entityRenderer.setUniforms((i, writer) -> {
			switch (i)
			{
				case 0 -> {
					writer.set(ShaderVariable.MODEL, modelMatrix);
					writer.set(ShaderVariable.PROJECTION, projectionMatrix);
					writer.flush();
				}
				case 1 -> {
					writer.set(lightPos, Vector3f.of(0.3F, 1, 0));
					writer.set(lightIntensity, 0.75F);
					writer.set(spaceFogIntensity, 0.1F);
					writer.flush();
				}
			}
		});
		entityRendererInitFuture.join();
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
				entityRenderer.reinit(new EntityRenderer.ReinitParams(reloadShaders ? shaderPrograms.get(currentShaderIndex) : null, null, null, isModified ? new EntityRenderer.SurfaceTarget(window) : null));
				if (isModified)
				{
					Matrix4f projectionMatrix = buildFullSceneMatrix(window, (float) (Math.PI * 2 / 3));
					entityRenderer.setUniforms((i, writer) -> {
						switch (i)
						{
							case 0 -> {
								writer.set(ShaderVariable.PROJECTION, projectionMatrix);
								writer.flush();
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
							writer.flush();
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
		if (memoryManager != null) memoryManager.close();
		try
		{
			fileManager.stopWatchingResources();
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
		}
		RenderEngineVulkan.terminate();
	}
	
	public static void handleFileSystemEvent(Path path, Kind<?> kind)
	{}
}
