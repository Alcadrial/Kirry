package com.alcadrial.renderengine.render.test;

import org.lwjgl.glfw.GLFW;

import com.alcadrial.renderengine.render.PhysicalDevice;
import com.alcadrial.renderengine.render.RenderEngineVulkan;
import com.alcadrial.renderengine.render.font.Font;
import com.alcadrial.renderengine.render.test.util.RenderTest;
import com.alcadrial.renderengine.render.test.util.TestProperties;
import com.alcadrial.renderengine.render.texture.Texture;
import com.alcadrial.renderengine.render.util.exception.FreeTypeException;
import com.alcadrial.renderengine.render.window.VulkanWindow;

public class FontTest extends RenderTest {
	
	public static void main(String[] args)
	{
		try
		{
			TestProperties.init(args);
			RenderEngineVulkan.enableDebug();
			RenderTest.initRenderEngine().multiThreading(false).useFont(true).initialize();
			
			RenderEngineVulkan.setWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, true);
			VulkanWindow window = VulkanWindow.defaultBuilder()
				.physicalDeviceStrategy(PhysicalDevice.Strategy.atIndexOrElse(TestProperties.DEVICE.getValue(), PhysicalDevice.Strategy.findBest()))
				.deviceFeatures(f -> f
					.samplerAnisotropy(true)
				)
				.useDepthTexture(false)
				.build(128, 128, "Test");
			registerDragging();
			
			Font font = new Font(window.getDevice(), getPath("fonts/ITCBLKAD.TTF"), 0);
			//Font font = new Font(window.getDevice(), getFile("fonts/ARIALI.TTF"), 0);
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
						System.out.println(font.getCharRegion(c));
					}
					catch (FreeTypeException e)
					{
						System.out.println("Failed to load char:" + c);
					}
				}
				font.getAtlas().tryFit();
			}, TestProperties.PRINT_LOADING_TIMES);
			println("Draw", () -> {
				window.getDevice().configureAndSubmitSingleUseCommandBuffer(window.getDevice().getGraphicsQueue(), font.getAtlas()::draw);
			}, TestProperties.PRINT_LOADING_TIMES);
			System.out.println(font.getAtlas());
			Texture texture = font.getAtlas().getTexture();
			
			int w = texture.getWidth();
			int h = texture.getHeight();
			float scale = 512F / w;
			
			window.setSize((int) (w * scale), (int) (h * scale));
			window.pollEvents();
			window.getSurface().reinit();
			
			RenderTest.renderTexture(window, texture, 1);
			
			while (!window.shouldClose())
			{
				Thread.sleep(50);
				window.pollEvents();
			}
			font.close();
		}
		catch (Exception e)
		{
			e.printStackTrace(System.out);
		}
		finally
		{
			RenderEngineVulkan.terminate();
		}
	}
}
