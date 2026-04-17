package com.alcadrial.renderengine.render.test.widgets;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallbackI;

import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.PhysicalDevice;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.shader.ShaderProgram;
import com.alcadrial.renderengine.render.test.util.RenderTest;
import com.alcadrial.renderengine.render.test.util.TestProperties;
import com.alcadrial.renderengine.render.texture.Texture;
import com.alcadrial.renderengine.render.texture.TextureBuffer;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.window.BlackScreen;
import com.alcadrial.renderengine.render.window.Screen;
import com.alcadrial.renderengine.render.window.VulkanWindow;
import com.alcadrial.renderengine.render.window.widget.Sprite;

public class ScreenTest extends RenderTest {
	
	public static void main(String[] args)
	{
		mainTest(args, ScreenTest::start, ScreenTest::loop, ScreenTest::end, b -> b.useFont(true));
	}
	
	private static final String SHADER = "shader4";
	private static final List<Screen> SCREENS = new LinkedList<>();
	
	private static int index = 0;
	
	public static void start() throws VulkanException
	{
		ShaderProgram shaderProgram;
		try
		{
			shaderProgram = ShaderProgram.create().add(loadShader(SHADER + ".vert"), loadShader(SHADER + ".frag")).build();
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
			RenderEngine.terminate();
			return;
		}
		
		VulkanWindow window = VulkanWindow.defaultBuilder()
			.physicalDeviceStrategy(PhysicalDevice.Strategy.atIndexOrElse(TestProperties.DEVICE.getValue(), PhysicalDevice.Strategy.findBest()))
			.deviceFeatures(f -> f
				.samplerAnisotropy(true)
			)
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
		Device device = window.getDevice();
		
		TextureBuffer textureIcon;
		TextureBuffer textureIcon0;
		try
		{
			textureIcon = Texture.loadToBufferFromFile(device, getFile("icon.png")).get();
			textureIcon0 = Texture.loadToBufferFromFile(device, getFile("icon0.png")).get();
		}
		catch (InterruptedException _)
		{
			RenderEngine.terminate();
			return;
		}
		
		SCREENS.add(BlackScreen.BLACK_SCREEN);
		BasicScreen s = new BasicScreen();
		s.add(new Sprite(window, Vector2i.of(20, 20), Vector2i.of(256, 256), textureIcon0));
		s.add(new Sprite(window, Vector2i.of(296, 20), Vector2i.of(256, 256), textureIcon));
		s.add(new Sprite(window, Vector2i.of(20, 296), Vector2i.of(256, 256), textureIcon));
		s.add(new Sprite(window, Vector2i.of(296, 296), Vector2i.of(256, 256), textureIcon0));
		s.setShaderProgram(shaderProgram);
		SCREENS.add(s);
		
		index = 1;
		window.setScreen(SCREENS.get(index));
	}
	
	public static void loop() throws VulkanException
	{
		VulkanWindow window = RenderEngine.getVulkan().getWindow();
		try
		{
			while (!window.shouldClose())
			{
				window.renderScreenOnce();
				window.pollEvents();
				Thread.sleep(50);
			}
		}
		catch (InterruptedException _)
		{}
	}
	
	public static void end()
	{
		RenderEngine.terminate();
	}
}
