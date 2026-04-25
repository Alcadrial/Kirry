package com.alcadrial.renderengine.render.test;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.vulkan.VK10;

import com.alcadrial.renderengine.render.PhysicalDevice;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.test.util.RenderTest;
import com.alcadrial.renderengine.render.test.util.TestProperties;
import com.alcadrial.renderengine.render.texture.Atlas;
import com.alcadrial.renderengine.render.texture.Texture;
import com.alcadrial.renderengine.render.texture.TextureBuffer;
import com.alcadrial.renderengine.render.texture.TextureRegion;
import com.alcadrial.renderengine.render.window.VulkanWindow;

public class AtlasTest {
	
	public static void main(String[] args)
	{
		try
		{
			TestProperties.init(args);
			RenderEngine.enableDebug();
			RenderTest.initRenderEngine().multiThreading(false).initialize();

			RenderEngine.setWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, true);
			VulkanWindow window = VulkanWindow.defaultBuilder()
				.physicalDeviceStrategy(PhysicalDevice.Strategy.atIndexOrElse(TestProperties.DEVICE.getValue(), PhysicalDevice.Strategy.findBest()))
				.deviceFeatures(f -> f
					.samplerAnisotropy(true)
				)
				.useDepthTexture(false)
				.build(64, 64, "Test");
			
			Atlas atlas = new Atlas(window.getDevice(), 1024, 512, VK10.VK_FORMAT_R8G8B8A8_SRGB, VK10.VK_IMAGE_USAGE_SAMPLED_BIT, VK10.VK_IMAGE_ASPECT_COLOR_BIT, true);
			
			List<TextureBuffer> textures = new ArrayList<>(32);
			
			for (int i = 0; i < 32; i++)
			{
				try
				{
					textures.add(Texture.loadToBufferFromFile(window.getDevice(), RenderTest.getFile("images/icon" + i + ".png")).get());
				}
				catch (Exception e)
				{
					System.out.println(e.getCause().getMessage());
				}
			}
			
			List<TextureRegion> regions = atlas.addAll(textures.toArray(TextureBuffer[]::new));
			
			atlas.draw();
			
			System.out.println("Size: " + atlas.getWidth() + "x" + atlas.getHeight());
			for (TextureRegion region : regions)
			{
				System.out.println("Region: " + region.getUvStart() + " -> " + region.getUvEnd());
			}
			
			int w = atlas.getWidth();
			int h = atlas.getHeight();
			float scale = 1024F / w;
			
			window.setSize((int) (w * scale), (int) (h * scale));
			window.pollEvents();
			window.getSurface().reinit();
			
			RenderTest.renderTexture(window, atlas.getTexture());
			
			while (!window.shouldClose())
			{
				Thread.sleep(50);
				window.pollEvents();
			}
			
		}
		catch (Exception e)
		{
			e.printStackTrace(System.out);
		}
		finally {
			RenderEngine.terminate();
		}
	}
}
