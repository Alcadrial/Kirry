package com.alcadrial.kirry.render.test.widgets;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallbackI;

import com.alcadrial.kector.vector.Vector2ir;
import com.alcadrial.kector.vector.Vector3ir;
import com.alcadrial.kirry.render.KirryVulkan;
import com.alcadrial.kirry.render.PhysicalDevice;
import com.alcadrial.kirry.render.resources.FileManager;
import com.alcadrial.kirry.render.resources.Mesh;
import com.alcadrial.kirry.render.resources.ShaderResource;
import com.alcadrial.kirry.render.resources.TextureResource;
import com.alcadrial.kirry.render.shader.ShaderProgram;
import com.alcadrial.kirry.render.test.util.RecompileShaderResource;
import com.alcadrial.kirry.render.test.util.RenderTest;
import com.alcadrial.kirry.render.test.util.TestProperties;
import com.alcadrial.kirry.render.util.exception.VulkanException;
import com.alcadrial.kirry.render.window.WindowScreen;
import com.alcadrial.kirry.render.window.screen.BlackScreen;
import com.alcadrial.kirry.render.window.VulkanWindow;
import com.alcadrial.kirry.render.window.widget.Sprite;

public class ScreenTest extends RenderTest {
	
	public static void main(String[] args)
	{
		mainTest(args, ScreenTest::start, ScreenTest::loop, ScreenTest::end);
	}
	
	private static final String SHADER = SHADERS + "shader4";
	private static final List<WindowScreen> SCREENS = new LinkedList<>();
	
	private static int index = 0;
	
	public static void start() throws VulkanException
	{
		ShaderProgram shaderProgram;
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
			
			shaderProgram = ShaderProgram.create().add(loadShader(SHADER + ".vert"), loadShader(SHADER + ".frag")).build();
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
			end();
			return;
		}
		
		VulkanWindow window = VulkanWindow.defaultBuilder()
			.physicalDeviceStrategy(PhysicalDevice.Strategy.atIndexOrElse(TestProperties.DEVICE.getValue(), PhysicalDevice.Strategy.findBest()))
			.resourceManager(fileManager)
			.build(1080, 720, "Test");
		window.setCallback((GLFWKeyCallbackI) (_, k, _, a, _) -> {
			
			if (a == GLFW.GLFW_RELEASE)
			{
				switch (k)
				{
					case GLFW.GLFW_KEY_UP -> {
						if (index > 0) index--;
					}
					case GLFW.GLFW_KEY_DOWN -> {
						if (index < SCREENS.size() - 1) index++;
					}
					default -> {
						return;
					}
				}
				System.out.println("New screen: " + index);
				window.setScreen(SCREENS.get(index));
			}
		});
		
		TextureResource icon;
		TextureResource icon0;
		try
		{
			icon = fileManager.getResource(IMAGES + "icon.png", TextureResource.TYPE);
			icon0 = fileManager.getResource(IMAGES + "icon0.png", TextureResource.TYPE);
			
			CompletableFuture.allOf(icon.load(), icon0.load()).join();
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
			KirryVulkan.terminate();
			return;
		}
		
		SCREENS.add(BlackScreen.BLACK_SCREEN);
		BasicScreen s = new BasicScreen(window);
		s.add(Sprite.ofWindowed(window, Vector3ir.of(20, 20, 0), Vector2ir.of(256, 256), icon0));
		s.add(Sprite.ofWindowed(window, Vector3ir.of(296, 20, 0), Vector2ir.of(256, 256), icon));
		s.add(Sprite.ofWindowed(window, Vector3ir.of(20, 296, 0), Vector2ir.of(256, 256), icon));
		s.add(Sprite.ofWindowed(window, Vector3ir.of(296, 296, 0), Vector2ir.of(256, 256), icon0));
		s.setShaderProgram(shaderProgram);
		SCREENS.add(s);
		
		index = 1;
		window.setScreen(SCREENS.get(index));
	}
	
	public static void loop() throws VulkanException
	{
		VulkanWindow window = KirryVulkan.getVulkan().getWindow();
		try
		{
			while (!window.shouldClose())
			{
				window.renderScreenOnce(50);
				window.pollEvents();
				Thread.sleep(50);
			}
		}
		catch (InterruptedException _)
		{}
	}
	
	public static void end()
	{
		KirryVulkan.terminate();
	}
}
