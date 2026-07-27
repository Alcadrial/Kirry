package com.alcadrial.kirry.render.test;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWWindowRefreshCallbackI;

import com.alcadrial.kirry.render.KirryVulkan;
import com.alcadrial.kirry.render.PhysicalDevice;
import com.alcadrial.kirry.render.font.Font;
import com.alcadrial.kirry.render.memory.MemoryManager;
import com.alcadrial.kirry.render.test.util.RenderTest;
import com.alcadrial.kirry.render.test.util.TestProperties;
import com.alcadrial.kirry.render.texture.Texture;
import com.alcadrial.kirry.render.texture.TextureSampler;
import com.alcadrial.kirry.render.util.exception.FreeTypeException;
import com.alcadrial.kirry.render.window.VulkanWindow;

public class FontTest extends RenderTest {
	
	public static void main(String[] args)
	{
		try
		{
			TestProperties.init(args);
			KirryVulkan.enableDebug();
			RenderTest.initKirry().multiThreading(false).useFont(true).initialize();
			
			KirryVulkan.setWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, true);
			KirryVulkan.setWindowHint(GLFW.GLFW_VISIBLE, false);
			VulkanWindow window = VulkanWindow.defaultBuilder()
				.physicalDeviceStrategy(PhysicalDevice.Strategy.atIndexOrElse(TestProperties.DEVICE.getValue(), PhysicalDevice.Strategy.findBest()))
				.deviceFeatures(f -> f
					.samplerAnisotropy(true)
				)
				.useDepthTexture(false)
				.build(128, 128, "Test");
			registerDragging();
			
			MemoryManager memoryManager = MemoryManager.getDefault(window.getDevice());
			
			Font font = new Font(window.getDevice(), getPath("fonts/ITCBLKAD.TTF"), 0, 0, memoryManager);
			//Font font = new Font(window.getDevice(), getFile("fonts/ARIALI.TTF"), 0, memoryManager);
			System.out.println(font);
			font.setSize(64);
			println("Indexing", () -> {
				for (char c : """
									Alkaisr Tuilla
					[03:39:47.331] [DEBUG|main/7576] [Set 0, Binding 0, Index 0] [mat4x4 TRANSFORMATION, mat4x4 MODEL]
					[03:39:47.335] [DEBUG|main/7576] [Set 0, Binding 1, Index 0] sampler2D TEXTURE_SAMPLER
					QWASEDRFTGYHUJIKOLPZXCVBNM
					qwaserdfzxcvtbgnymhjukilop
					òèà+ùçé°*§@[#]ù#[]{}°°ç§
					!"£$%&/()=?^1234567890'ì
					αβγεζδλμξπσωΩÅÃĀÆʤɖËĚĔx				""".toCharArray())
				{
					try
					{
						System.out.println(font.getData(c));
					}
					catch (FreeTypeException e)
					{
						System.out.println("Failed to load char: " + c);
					}
				}
				font.getAtlas().tryFit();
			}, TestProperties.PRINT_LOADING_TIMES);
			println("Draw", () -> {
				window.getDevice().configureAndSubmitSingleUseCommandBuffer(window.getDevice().getGraphicsQueue(), font::prepare).join();
			}, TestProperties.PRINT_LOADING_TIMES);
			System.out.println(font.getAtlas());
			Texture texture = font.getAtlas().getTexture();
			
			int w = texture.getWidth();
			int h = texture.getHeight();
			float scale = 512F / w;
			
			window.setSize((int) (w * scale), (int) (h * scale));
			window.pollEvents();
			window.getSurface().reinit();

			RenderParams params = new RenderParams(font.getAtlas().getTexture(), 1, TextureSampler.ofLinear(window.getDevice()), null);
			
			RenderTest.renderTexture(window, params);
			
			boolean[] rerender = new boolean[] {false};
			
			window.setCallback((GLFWWindowRefreshCallbackI) _ -> {
				rerender[0] = true;
			});
			
			while (!window.shouldClose())
			{
				if (rerender[0])
				{
					RenderTest.renderTexture(window, params);
					rerender[0] = false;
				}
				window.waitEvents();
			}
			font.close();
			memoryManager.close();
		}
		catch (Exception e)
		{
			e.printStackTrace(System.out);
		}
		finally
		{
			KirryVulkan.terminate();
		}
	}
}
