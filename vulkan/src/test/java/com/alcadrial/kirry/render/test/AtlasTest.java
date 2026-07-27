package com.alcadrial.kirry.render.test;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWWindowRefreshCallbackI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkSamplerCreateInfo;

import com.alcadrial.kirry.render.KirryVulkan;
import com.alcadrial.kirry.render.PhysicalDevice;
import com.alcadrial.kirry.render.test.util.RenderTest;
import com.alcadrial.kirry.render.test.util.RenderTest.RenderParams;
import com.alcadrial.kirry.render.test.util.TestProperties;
import com.alcadrial.kirry.render.texture.Atlas;
import com.alcadrial.kirry.render.texture.Texture;
import com.alcadrial.kirry.render.texture.TextureBuffer;
import com.alcadrial.kirry.render.texture.TextureRegion;
import com.alcadrial.kirry.render.texture.TextureSampler;
import com.alcadrial.kirry.render.util.exception.VulkanException;
import com.alcadrial.kirry.render.window.VulkanWindow;

public class AtlasTest {
	
	public static final int MIP_TO_RENDER = 4;
	
	public static void main(String[] args)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
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
			
			Atlas atlas = new Atlas(window.getDevice(), 256, 4096, VK10.VK_FORMAT_R8G8B8A8_SRGB, VK10.VK_IMAGE_USAGE_SAMPLED_BIT, VK10.VK_IMAGE_ASPECT_COLOR_BIT, -1, 0.75F, true);
			
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
			
			
			RenderParams params = new RenderParams(atlas.getTexture(), 1, new TextureSampler(window.getDevice(), VkSamplerCreateInfo.malloc(stack)
				.sType$Default()
				.pNext(0)
				.flags(0)
				.magFilter(VK10.VK_FILTER_NEAREST)
				.minFilter(VK10.VK_FILTER_NEAREST)
				.mipmapMode(VK10.VK_SAMPLER_MIPMAP_MODE_NEAREST)
				.addressModeU(VK10.VK_SAMPLER_ADDRESS_MODE_REPEAT)
				.addressModeV(VK10.VK_SAMPLER_ADDRESS_MODE_REPEAT)
				.addressModeW(VK10.VK_SAMPLER_ADDRESS_MODE_REPEAT)
				.mipLodBias(0)
				.anisotropyEnable(false)
				.maxAnisotropy(0)
				.compareEnable(false)
				.compareOp(VK10.VK_COMPARE_OP_ALWAYS)
				.minLod(MIP_TO_RENDER)
				.maxLod(MIP_TO_RENDER)
				.borderColor(VK10.VK_BORDER_COLOR_INT_OPAQUE_BLACK)
				.unnormalizedCoordinates(false)
			), null);
			
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
