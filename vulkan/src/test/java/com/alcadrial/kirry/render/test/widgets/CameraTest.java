package com.alcadrial.kirry.render.test.widgets;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.vulkan.VK10;

import com.alcadrial.kector.AngleUnit;
import com.alcadrial.kector.vector.Vector2ir;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.kector.vector.Vector3ir;
import com.alcadrial.kirry.core.component.Entity;
import com.alcadrial.kirry.core.cursor.CursorInputMode;
import com.alcadrial.kirry.render.FrameBuffer;
import com.alcadrial.kirry.render.KirryVulkan;
import com.alcadrial.kirry.render.PhysicalDevice;
import com.alcadrial.kirry.render.component.MeshComponent;
import com.alcadrial.kirry.render.component.RenderEngine;
import com.alcadrial.kirry.render.component.ShaderComponent;
import com.alcadrial.kirry.render.component.SpatialComponent;
import com.alcadrial.kirry.render.renderpass.RenderPassTemplate;
import com.alcadrial.kirry.render.resources.FileManager;
import com.alcadrial.kirry.render.resources.Mesh;
import com.alcadrial.kirry.render.resources.ShaderResource;
import com.alcadrial.kirry.render.resources.TextureResource;
import com.alcadrial.kirry.render.scene.Camera;
import com.alcadrial.kirry.render.scene.CameraControllerTarget;
import com.alcadrial.kirry.render.scene.Controller;
import com.alcadrial.kirry.render.scene.ControllerTarget;
import com.alcadrial.kirry.render.scene.EntityControllerTarget;
import com.alcadrial.kirry.render.scene.JoystickController;
import com.alcadrial.kirry.render.scene.MouseKeyboardController;
import com.alcadrial.kirry.render.shader.ShaderInputType;
import com.alcadrial.kirry.render.shader.ShaderProgram;
import com.alcadrial.kirry.render.shader.ShaderType;
import com.alcadrial.kirry.render.shader.ShaderVariable;
import com.alcadrial.kirry.render.test.util.RecompileShaderResource;
import com.alcadrial.kirry.render.test.util.RenderTest;
import com.alcadrial.kirry.render.test.util.TestProperties;
import com.alcadrial.kirry.render.util.exception.ExternalException;
import com.alcadrial.kirry.render.util.exception.VulkanException;
import com.alcadrial.kirry.render.util.future.VulkanFuture;
import com.alcadrial.kirry.render.window.VulkanWindow;
import com.alcadrial.kirry.render.window.event.KeyboardEvent;
import com.alcadrial.kirry.render.window.screen.WidgetTextureScreen;
import com.alcadrial.kirry.render.window.widget.CameraView;
import com.alcadrial.kirry.render.window.widget.CameraView.UniformState;
import com.alcadrial.kirry.render.window.widget.ScreenWidget;

public class CameraTest extends RenderTest {
	
	public static void main(String[] args)
	{
		mainTest(args, CameraTest::start, CameraTest::loop, CameraTest::end, b -> b.useFont(true));
	}
	
	private static final String SHADER4 = SHADERS + "shader4";
	private static final String SHADER5 = SHADERS + "shader5";
	
	private static Mesh lightModel;
	private static Entity lightEntity;
	private static MutableList<Mesh> models;
	private static MutableList<Entity> entities;
	
	private static ShaderResource vertexShaderResource;
	private static ShaderResource fragmentShaderResource;
	
	private static boolean showBackground;
	private static boolean showGui;
	
	public static void start() throws VulkanException
	{
		ShaderProgram screenShaderProgram;
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
			
			vertexShaderResource = fileManager.getResource(SHADER5 + ".vert", ShaderResource.TYPE);
			fragmentShaderResource = fileManager.getResource(SHADER5 + ".1.frag", ShaderResource.TYPE);
			
			screenShaderProgram = ShaderProgram.create().add(loadShader(SHADER4 + ".vert"), loadShader(SHADER4 + ".frag")).build();
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
			.resourceManager(fileManager)
			.build(1080, 720, "Test");
		
		models = Lists.mutable.of();
		
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(fileManager.getPath(ASSETS + "models"), "*.obj"))
		{
			for (Path path : stream)
			{
				System.out.println("Found model " + path);
				if (path.endsWith("light.obj")) lightModel = fileManager.getResource(path, Mesh.TYPE);
				else models.add(fileManager.getResource(path, Mesh.TYPE));
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
		if (lightModel == null)
		{
			System.out.println("Missing light model (" + fileManager.getPath(ASSETS + "models/light.obj") + ")");
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
			{
				int j = -1;
				for (Mesh mesh : models)
				{
					for (int i = -2; i < 2; i++)
					{
						Entity e = Entity.of(maxId);
						e.setComponent(MeshComponent.TYPE, MeshComponent.ofMesh(mesh));
						e.setComponent(ShaderComponent.TYPE, ShaderComponent.ofResource(vertexShaderResource, fragmentShaderResource));
						e.setComponent(SpatialComponent.TYPE, SpatialComponent.of(Vector3f.of(i * 1.1F, j * 1.1F, 0)));
						e.listenComponents();
						entities.add(e);
					}
					j++;
				}
			}
			//light entity
			{
				Entity e = Entity.of(maxId);
				e.setComponent(MeshComponent.TYPE, MeshComponent.ofMesh(lightModel));
				e.setComponent(ShaderComponent.TYPE, ShaderComponent.ofResource(vertexShaderResource, fragmentShaderResource));
				e.setComponent(SpatialComponent.TYPE, SpatialComponent.of(Vector3f.of(), Vector3f.of()));
				e.listenComponents();
				entities.add(lightEntity = e);
			}
			
			FastList<CompletableFuture<Throwable>> futures = new FastList<>();
			
			for (Entity e : entities)
			{
				futures.add(e.load().handle((_, ex) -> ex));
			}
			
			CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
			
			for (int i = 0, j = 0; j < futures.size(); j++)
			{
				Throwable ex = futures.get(j).join();
				if (ex != null)
				{
					if (i < entities.size())
					{
						System.out.println("Failed to load entity (" + entities.remove(i).getComponent(MeshComponent.TYPE) + "): " + ex.getLocalizedMessage());
					}
					else
					{
						System.out.println("Failed to load overlay: " + ex.getLocalizedMessage());
					}
				}
				else i++;
			}
		});
		modelsLoading.join();
		
		Camera camera0 = new Camera(AngleUnit.RADIANS.fromDegrees(120), 100);
		Camera camera1 = new Camera(AngleUnit.RADIANS.fromDegrees(120), 100);
		Camera camera2 = new Camera(AngleUnit.RADIANS.fromDegrees(120), 100);
		Camera camera3 = new Camera(AngleUnit.RADIANS.fromDegrees(120), 100);
		CameraControllerTarget cameraTarget = new CameraControllerTarget(camera0);
		EntityControllerTarget lightTarget = new EntityControllerTarget(lightEntity);
		MouseKeyboardController mouseKeyboardController = new MouseKeyboardController(cameraTarget);
		JoystickController joystickController = new JoystickController(cameraTarget);
		
		CameraView cameraView;
		BasicScreen s;
		
		HiddenSprite background0;
		HiddenSprite background1;
		HiddenSprite background2;
		HiddenSprite background3;
		HiddenSprite gui;
		HiddenSprite activeGui;
		try
		{
			//@formatter:off
			cameraView = CameraView.ofFull(window, camera0, new UniformState() {{
				Var<Vector3f> lightPosVar = new Var<>(ShaderVariable.of(ShaderInputType.VECTOR_FLOAT_3, "LIGHT_POS"), lightTarget.getPos(), (w, v, t) -> w.set(v, t));
				Var<Float> lightIntensityVar = new Var<>(ShaderVariable.of(ShaderInputType.FLOAT, "LIGHT_INTENSITY"), 1F, (w, v, t) -> w.set(v, t));
				Var<Float> spaceFogIntensityVar = new Var<>(ShaderVariable.of(ShaderInputType.FLOAT, "SPACE_FOG_INTENSITY"), 0.1F, (w, v, t) -> w.set(v, t));
				add(lightPosVar, ShaderType.FRAGMENT, 1);
				add(lightIntensityVar, ShaderType.FRAGMENT, 1);
				add(spaceFogIntensityVar, ShaderType.FRAGMENT, 1);
				lightTarget.onUpdate(_ -> lightPosVar.markDirty());
			}});
			//@formatter:on
			cameraView.setCamera(camera0);
			cameraView.setController(mouseKeyboardController);
			cameraView.setEntities(entities);
			cameraView.setRenderEngineConfig(new RenderEngine.Config(true));
			cameraView.setFocus(true);
			
			background0 = HiddenSprite.ofWindowed(window, Vector3ir.of(20, 20, 0), Vector2ir.of(256, 256), IMAGES + "icon0.png");
			background1 = HiddenSprite.ofWindowed(window, Vector3ir.of(296, 20, 0), Vector2ir.of(256, 256), IMAGES + "icon.png");
			background2 = HiddenSprite.ofWindowed(window, Vector3ir.of(20, 296, 0), Vector2ir.of(256, 256), IMAGES + "icon.png");
			background3 = HiddenSprite.ofWindowed(window, Vector3ir.of(296, 296, 0), Vector2ir.of(256, 256), IMAGES + "icon0.png");
			
			float scale = 3;
			Vector2ir overlaySize = Vector2ir.of((int) (32 * scale), (int) (32 * scale));
			gui = HiddenSprite.ofWindowed(window, Vector3ir.of((window.getWidth() - overlaySize.x()) / 2, (window.getHeight() - overlaySize.y()) / 2, 0), overlaySize, TEXTURES + "overlay.png");
			activeGui = HiddenSprite.ofWindowed(window, gui.getPos(), gui.getSize(), TEXTURES + "overlay_active.png");
			activeGui.hide();
			showBackground = true;
			showGui = true;
			
			ScreenWidget screenWidget = ScreenWidget.ofWindowed(window, Vector3ir.of((window.getWidth() - 572) / 2, (window.getHeight() - 572) / 2, 0), Vector2ir.of(572, 572));
			
			WidgetTextureScreen s0 = new WidgetTextureScreen(window.getDevice());
			int format = window.getSurface().getData().format().format();
			s0.setOutput(FrameBuffer.ofColor(RenderPassTemplate.colorTextureWriteReadTemplate(format).createRenderPassAndClose(window.getDevice()), 316, 316, format, VK10.VK_IMAGE_USAGE_SAMPLED_BIT));
			s0.setShaderProgram(screenShaderProgram);
			s0.add(background0);
			s0.add(background1);
			s0.add(background2);
			s0.add(background3);
			screenWidget.setScreen(s0);

			s = new BasicScreen(window);
			s.setShaderProgram(screenShaderProgram);
			s.add(screenWidget);
			s.add(cameraView);
			s.add(gui);
			s.add(activeGui);
		}
		catch (IOException e)
		{
			throw new ExternalException(e);
		}
		
		window.setScreen(s);
		mouseKeyboardController.registerEvents(window.getEventRegistry());
		joystickController.registerEvents(window.getEventRegistry());
		boolean[] holdController = new boolean[] {true};
		window.getEventRegistry().register((KeyboardEvent) (key, _, action, _) -> {
			if (action == GLFW.GLFW_PRESS)
			{
				switch (key)
				{
					case GLFW.GLFW_KEY_U -> {
						Controller c = cameraView.getController();
						if (c == null) break;
						c.setActive(!c.isActive());
						if (c.isActive())
						{
							gui.setShown(false);
							activeGui.setShown(true);
						}
						else
						{
							gui.setShown(true);
							activeGui.setShown(false);
						}
						window.setCursorInputMode(c.isActive() ? CursorInputMode.DISABLED : CursorInputMode.NORMAL);
					}
					case GLFW.GLFW_KEY_1 -> {
						cameraView.setCamera(camera0);
						if (holdController[0]) cameraTarget.setCamera(camera0);
					}
					case GLFW.GLFW_KEY_2 -> {
						cameraView.setCamera(camera1);
						if (holdController[0]) cameraTarget.setCamera(camera1);
					}
					case GLFW.GLFW_KEY_3 -> {
						cameraView.setCamera(camera2);
						if (holdController[0]) cameraTarget.setCamera(camera2);
					}
					case GLFW.GLFW_KEY_4 -> {
						cameraView.setCamera(camera3);
						if (holdController[0]) cameraTarget.setCamera(camera3);
					}
					case GLFW.GLFW_KEY_7 -> {
						cameraTarget.setCamera(camera0);
					}
					case GLFW.GLFW_KEY_8 -> {
						cameraTarget.setCamera(camera1);
					}
					case GLFW.GLFW_KEY_9 -> {
						cameraTarget.setCamera(camera2);
					}
					case GLFW.GLFW_KEY_0 -> {
						cameraTarget.setCamera(camera3);
					}
					case GLFW.GLFW_KEY_L -> {
						Controller c = cameraView.getController();
						if (c == null) break;
						ControllerTarget n = c.getTarget() == lightTarget ? cameraTarget : lightTarget;
						c.setTarget(n);
					}
					case GLFW.GLFW_KEY_J -> {
						Controller c = cameraView.getController();
						if (c == null) break;
						Controller n = c == mouseKeyboardController ? joystickController : mouseKeyboardController;
						n.setActive(c.isActive());
						c.setActive(false);
						cameraView.setController(n);
					}
					case GLFW.GLFW_KEY_H -> {
						holdController[0] = !holdController[0];
					}
					case GLFW.GLFW_KEY_G -> {
						Controller c = cameraView.getController();
						if (c == null) break;
						showGui = !showGui;
						if (showGui)
						{
							if (c.isActive())
							{
								activeGui.setShown(true);
							}
							else
							{
								gui.setShown(true);
							}
						}
						else
						{
							gui.setShown(false);
							activeGui.setShown(false);
						}
					}
					case GLFW.GLFW_KEY_B -> {
						showBackground = !showBackground;
						background0.setShown(showBackground);
						background1.setShown(showBackground);
						background2.setShown(showBackground);
						background3.setShown(showBackground);
					}
				}
			}
		});
	}
	
	public static void loop() throws VulkanException
	{
		VulkanWindow window = KirryVulkan.getVulkan().getWindow();
		int fps = TestProperties.MAX_FRAME_RATE.getValue();
		double frameTime = 1D / fps;
		
		long t0 = System.currentTimeMillis();
		long t1 = t0;
		while (!window.shouldClose())
		{
			window.renderScreenOnce(t1 - t0);
			window.waitEvents(frameTime);
			t0 = t1;
			t1 = System.currentTimeMillis();
		}
	}
	
	public static void end()
	{
		if (entities != null) for (Entity entity : entities) entity.close();
		KirryVulkan.terminate();
	}
}
