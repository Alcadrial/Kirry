package com.alcadrial.kirry.render.test;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
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

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.kector.vector.Vector3fr;
import com.alcadrial.kirry.core.component.Entity;
import com.alcadrial.kirry.render.Debug;
import com.alcadrial.kirry.render.Device;
import com.alcadrial.kirry.render.FrameBuffer;
import com.alcadrial.kirry.render.KirryVulkan;
import com.alcadrial.kirry.render.PhysicalDevice;
import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;
import com.alcadrial.kirry.render.component.MeshComponent;
import com.alcadrial.kirry.render.component.RenderEngine;
import com.alcadrial.kirry.render.component.ShaderComponent;
import com.alcadrial.kirry.render.component.SpatialComponent;
import com.alcadrial.kirry.render.memory.MemoryManager;
import com.alcadrial.kirry.render.resources.FileManager;
import com.alcadrial.kirry.render.resources.Mesh;
import com.alcadrial.kirry.render.resources.ShaderResource;
import com.alcadrial.kirry.render.resources.TextureResource;
import com.alcadrial.kirry.render.shader.ShaderInputType;
import com.alcadrial.kirry.render.shader.ShaderVariable;
import com.alcadrial.kirry.render.test.util.RecompileShaderResource;
import com.alcadrial.kirry.render.test.util.RenderTest;
import com.alcadrial.kirry.render.test.util.TestProperties;
import com.alcadrial.kirry.render.util.exception.VulkanException;
import com.alcadrial.kirry.render.util.future.VulkanFuture;
import com.alcadrial.kirry.render.window.VulkanWindow;

public class ComponentTest extends RenderTest {
	
	private static final String SHADER = SHADERS + "shader5";
	
	public static void main(String[] args)
	{
		mainTest(args, ComponentTest::start, () -> defaultAnimationLoop(ComponentTest::loop), ComponentTest::end);
	}
	
	private static MutableList<Mesh> models;
	private static MutableList<Entity> entities;
	private static RenderEngine renderEngine;
	
	private static ShaderResource vertexShaderResource;
	private static ShaderResource fragmentShaderResource;
	private static ShaderResource fragment1ShaderResource;
	private static ShaderResource spatialMoveComputeShaderResource;
	private static ShaderResource initComputeShaderResource;
	private static ShaderResource transformComputeShaderResource;
	private static int currentShaderIndex;
	private static MemoryManager memoryManager;
	
	private static ShaderVariable lightPos = ShaderVariable.of(ShaderInputType.VECTOR_FLOAT_3, "LIGHT_POS");
	private static ShaderVariable lightIntensity = ShaderVariable.of(ShaderInputType.FLOAT, "LIGHT_INTENSITY");
	private static ShaderVariable spaceFogIntensity = ShaderVariable.of(ShaderInputType.FLOAT, "SPACE_FOG_INTENSITY");
	
	private static boolean reloadShaders;
	private static SpatialMovementData spatialMovementData;
	
	public static void start() throws VulkanException
	{
		try
		{
			fileManager = FileManager.of(
				"src/test/resources",
				"src/main/resources",
				FileManager.loaderJarResourceUri(ASSETS)
			);
			fileManager.registerResourceType(ShaderResource.TYPE, RecompileShaderResource::new);
			fileManager.registerResourceType(TextureResource.TYPE, TextureResource::new);
			fileManager.registerResourceType(Mesh.TYPE, (p, m) -> new Mesh(p, m.getPath(MATERIALS), m.getPath(TEXTURES), m));
			fileManager.startWatchingResources();
			
			vertexShaderResource = fileManager.getResource(SHADER + ".vert", ShaderResource.TYPE);
			fragmentShaderResource = fileManager.getResource(SHADER + ".frag", ShaderResource.TYPE);
			fragment1ShaderResource = fileManager.getResource(SHADER + ".1.frag", ShaderResource.TYPE);
			spatialMoveComputeShaderResource = fileManager.getResource(RenderEngine.SPATIAL_MOVE_COMPUTE_SHADER_PATHNAME, ShaderResource.TYPE);
			initComputeShaderResource = fileManager.getResource(RenderEngine.INIT_COMPUTE_SHADER_PATHNAME, ShaderResource.TYPE);
			transformComputeShaderResource = fileManager.getResource(RenderEngine.TRANSFORM_COMPUTE_SHADER_PATHNAME, ShaderResource.TYPE);
			
			currentShaderIndex = 0;
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
			end();
			return;
		}
		
		KirryVulkan.setWindowHint(GLFW.GLFW_RESIZABLE, true);
		KirryVulkan.setWindowHint(GLFW.GLFW_DECORATED, true);
		KirryVulkan.setWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, true);
		
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
			
			int maxId = Lists.mutable.of(
				MeshComponent.TYPE.getId(),
				ShaderComponent.TYPE.getId(),
				SpatialComponent.TYPE.getId()
			).max();
			int j = 0;
			for (Mesh mesh : models)
			{
				for (int i = 0; i < 5; i++)
				{
					Entity e = Entity.of(maxId);
					e.setComponent(MeshComponent.TYPE, MeshComponent.ofMesh(mesh));
					e.setComponent(ShaderComponent.TYPE, ShaderComponent.ofResource(vertexShaderResource, i == 0 ? fragmentShaderResource : fragment1ShaderResource));
					e.setComponent(SpatialComponent.TYPE, SpatialComponent.of(new SpatialComponent.State(Vector3f.of(i * 1.1F, j * 1.1F, 0), Vector3f.of(0, 0, 0))));
					e.listenComponents();
					entities.add(e);
				}
				j++;
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
			rebuildProjectionMatrix = true;
		});
		
		window.setCallback((GLFWKeyCallbackI) (_, k, _, a, _) -> {
			if (a == GLFW.GLFW_PRESS)
			{
				switch (k)
				{
					case GLFW.GLFW_KEY_UP -> {
						currentShaderIndex = (currentShaderIndex == 0 ? 2 : currentShaderIndex) - 1;
						reloadShaders = true;
					}
					case GLFW.GLFW_KEY_DOWN -> {
						currentShaderIndex = (currentShaderIndex == 2 - 1 ? 0 : currentShaderIndex + 1);
						reloadShaders = true;
					}
				}
			}
		});
		
		spatialMovementData = registerSpatialMovement(window, YRotationSensivity, XRotationSensivity);
		
		renderEngine = new RenderEngine(device);
		
		memoryManager = MemoryManager.getDefault(device);
		
		Debug.runOnDebug(() -> {
			memoryManager.setName("MemoryManager");
		});
		
		VulkanFuture<?> entityRendererInitFuture = VulkanFuture.composeAfter(modelsLoading, () -> {
			device.configureAndSubmitSingleUseCommandBuffer(device.getGraphicsQueue(), commandBuffer -> {
				renderEngine.init(null, spatialMoveComputeShaderResource, initComputeShaderResource, transformComputeShaderResource, memoryManager, commandBuffer, new RenderEngine.SurfaceTarget(window), Lists.mutable.of(ShaderVariable.PROJECTION, ShaderVariable.MODEL), Lists.mutable.of(lightPos, lightIntensity, spaceFogIntensity), entities);
			}).join();
		});

		Matrix4f projectionMatrix = new Matrix4f().projectPositiveZ((float) (Math.PI * 2 / 3), 100, 0.1F, (float) window.getWidth() / window.getHeight());
		projectionMatrix.multiply(new Matrix4f().scale(Vector3fr.of(1, -1, 1)));
		Matrix4f modelMatrix = new Matrix4f().view(spatialMovementData.position, spatialMovementData.xRotation, spatialMovementData.yRotation);
		
		renderEngine.setUniforms((i, writer) -> {
			switch (i)
			{
				case 0 -> {
					writer.set(ShaderVariable.PROJECTION, projectionMatrix);
					writer.set(ShaderVariable.MODEL, modelMatrix);
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
		rebuildProjectionMatrix = false;
		rebuildViewMatrix = false;
		entityRendererInitFuture.join();
	}
	
	public static void loop(long time, long enlapsed) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VulkanWindow window = KirryVulkan.getVulkan().getWindow();
			switch (window.startPresentation())
			{
				case EMPTY_SURFACE:
					return;
				case SURFACE_LOST, OUT_OF_MEMORY, FATAL_ERROR:
					throw new VulkanException("Presentaton error");
			}
			CommandBuffer commandBuffer = window.getCurrentCommandBuffer();
			
			calculatePosition(spatialMovementData, enlapsed);
			
			if (reloadShaders)
			{
				Entity entity = entities.getFirst();
				ShaderComponent oldShader = entity.getComponent(ShaderComponent.TYPE);
				if (oldShader != null)
				{
					oldShader.close();
					entity.setComponent(ShaderComponent.TYPE, ShaderComponent.ofResource(vertexShaderResource, switch (currentShaderIndex)
					{
						case 0 -> fragmentShaderResource;
						case 1 -> fragment1ShaderResource;
						default -> throw new IllegalArgumentException("Unexpected value: " + currentShaderIndex);
					}));
					entity.load().join();
				}
				reloadShaders = false;
			}
			if (rebuildProjectionMatrix)
			{
				renderEngine.reinit(commandBuffer, null, null, null, new RenderEngine.SurfaceTarget(window), null, null);
			}
			if (rebuildProjectionMatrix || rebuildViewMatrix)
			{
				Matrix4f projectionMatrix = new Matrix4f().projectPositiveZ((float) (Math.PI * 2 / 3), 100, 0.1F, (float) window.getWidth() / window.getHeight());
				Matrix4f modelMatrix = new Matrix4f().view(spatialMovementData.position, spatialMovementData.xRotation, spatialMovementData.yRotation);
				boolean b0 = rebuildProjectionMatrix;
				boolean b1 = rebuildViewMatrix;
				renderEngine.setUniforms((i, writer) -> {
					switch (i)
					{
						case 0 -> {
							if (b0) writer.set(ShaderVariable.PROJECTION, projectionMatrix);
							if (b1) writer.set(ShaderVariable.MODEL, modelMatrix);
							writer.flush();
						}
					}
				});
				rebuildProjectionMatrix = false;
				rebuildViewMatrix = false;
			}
			
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
			
			renderEngine.update(commandBuffer, renderPassBeginInfo, window.getFrameIndex());
			window.endPresentation(commandBuffer);
		}
	}
	
	public static void end()
	{
		if (renderEngine != null) renderEngine.close();
		if (entities != null) for (Entity entity : entities) entity.close();
		if (memoryManager != null) memoryManager.close();
		try
		{
			fileManager.stopWatchingResources();
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
		}
		KirryVulkan.terminate();
	}
	
	public static void handleFileSystemEvent(Path path, Kind<?> kind)
	{}
}
