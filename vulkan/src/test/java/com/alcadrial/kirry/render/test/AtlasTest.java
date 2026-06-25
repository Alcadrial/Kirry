package com.alcadrial.kirry.render.test;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.vulkan.VK10;

import com.alcadrial.kirry.render.KirryVulkan;
import com.alcadrial.kirry.render.PhysicalDevice;
import com.alcadrial.kirry.render.test.util.RenderTest;
import com.alcadrial.kirry.render.test.util.TestProperties;
import com.alcadrial.kirry.render.texture.Atlas;
import com.alcadrial.kirry.render.texture.Texture;
import com.alcadrial.kirry.render.texture.TextureBuffer;
import com.alcadrial.kirry.render.texture.TextureRegion;
import com.alcadrial.kirry.render.util.exception.VulkanException;
import com.alcadrial.kirry.render.window.VulkanWindow;

public class AtlasTest {
	
	public static void main(String[] args)
	{
		try
		{
			TestProperties.init(args);
			KirryVulkan.enableDebug();
			RenderTest.initKirry().multiThreading(false).initialize();
			
			KirryVulkan.setWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, true);
			KirryVulkan.setWindowHint(GLFW.GLFW_VISIBLE, false);
			VulkanWindow window = VulkanWindow.defaultBuilder()
				.physicalDeviceStrategy(PhysicalDevice.Strategy.atIndexOrElse(TestProperties.DEVICE.getValue(), PhysicalDevice.Strategy.findBest()))
				.deviceFeatures(f -> f
					.samplerAnisotropy(true)
				)
				.useDepthTexture(false)
				.build(64, 64, "Test");
			
			Atlas atlas = new Atlas(window.getDevice(), VK10.VK_FORMAT_R8G8B8A8_SRGB, VK10.VK_IMAGE_USAGE_SAMPLED_BIT, VK10.VK_IMAGE_ASPECT_COLOR_BIT, 0.75F, true);
			
			List<TextureBuffer> textures = new ArrayList<>();
			
			for (int i = 0; i < 32; i++)
			{
				try
				{
					textures.add(Texture.loadToBufferFromFile(window.getDevice(), RenderTest.getPath("images/icon" + i + ".png")).get());
				}
				catch (VulkanException e)
				{
					System.out.println(e.getMessage());
				}
			}
			
			List<TextureRegion> regions = atlas.addAllBuffers(textures);
			
			window.getDevice().configureAndSubmitSingleUseCommandBuffer(window.getDevice().getGraphicsQueue(), atlas::draw).join();
			
			System.out.println("Size: " + atlas.getWidth() + "x" + atlas.getHeight());
			for (TextureRegion region : regions)
			{
				System.out.println(region);
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
		finally
		{
			KirryVulkan.terminate();
		}
	}
}
