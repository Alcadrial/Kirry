package com.alcadrial.renderengine.render.test.widgets;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.EXTDepthRangeUnrestricted;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;

import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.renderengine.render.CommandBuffer;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.Vulkan;
import com.alcadrial.renderengine.render.shader.ShaderProgram;
import com.alcadrial.renderengine.render.test.RenderTest;
import com.alcadrial.renderengine.render.test.TestProperties;
import com.alcadrial.renderengine.render.texture.Texture;
import com.alcadrial.renderengine.render.texture.TextureRegion;
import com.alcadrial.renderengine.render.util.Properties;
import com.alcadrial.renderengine.render.window.BlackScreen;
import com.alcadrial.renderengine.render.window.Screen;
import com.alcadrial.renderengine.render.window.VulkanWindow;
import com.alcadrial.renderengine.render.window.widget.Sprite;

public class ScreenTest extends RenderTest {
	
	public static void main(String[] args)
	{
		TestProperties.init(args);
		
		if (Properties.DEBUG.getValue()) RenderEngine.enableDebug();
		
		println("Render Engine initialization", RenderTest::initRenderEngine, TestProperties.PRINT_LOADING_TIMES);
		if (!RenderEngine.isInitializated()) throw new RuntimeException("Cannot initialize RenderEngine", RenderEngine.getLastInitializationException());
		
		println("Start", ScreenTest::start, TestProperties.PRINT_LOADING_TIMES);
		if (!RenderEngine.isInitializated()) return;
		
		println("Loop", ScreenTest::loop, TestProperties.PRINT_LOADING_TIMES);
		if (!RenderEngine.isInitializated()) return;
		
		println("End", ScreenTest::end, TestProperties.PRINT_LOADING_TIMES);
	}
	
	private static final String SHADER = "shader";
	private static int index = 0;
	private static final List<Screen> SCREENS = new LinkedList<>();
	
	public static void start()
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
		
		Vulkan vulkan = RenderEngine.getVulkan();
		
		RenderEngine.setWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, true);
		
		VulkanWindow window = new VulkanWindow(1080, 720, "Vulkan");
		vulkan.setWindow(window);
		window.setCallback((GLFWKeyCallbackI) (w, key, code, action, mods) -> {
			
			if (action == GLFW.GLFW_RELEASE)
			{
				switch (key)
				{
					case GLFW.GLFW_KEY_UP -> {
						if (index > 0) index--;
						window.setScreen(SCREENS.get(index));
					}
					case GLFW.GLFW_KEY_DOWN -> {
						if (index < SCREENS.size() - 1) index++;
						window.setScreen(SCREENS.get(index));
					}
				}
			}
		});
		window.setShaderProgram(shaderProgram);
		
		if (!pickPhysicalDevice(window)) return;
		
		VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.calloc();
		deviceFeatures.wideLines(true);
		deviceFeatures.samplerAnisotropy(true);
		Device device = vulkan.createDevice(deviceFeatures, KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME, EXTDepthRangeUnrestricted.VK_EXT_DEPTH_RANGE_UNRESTRICTED_EXTENSION_NAME);
		window.init(device);
		
		Texture texture;
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			BufferedImage image = ImageIO.read(getFile("KhishweTransparent.png"));
			
			texture = Texture.create2dTexture(device, image.getWidth(), image.getHeight(), VK10.VK_FORMAT_R8G8B8A8_SRGB, VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK10.VK_IMAGE_USAGE_SAMPLED_BIT, VK10.VK_IMAGE_ASPECT_COLOR_BIT);
			
			CommandBuffer commandBuffer = device.createSingleUseCommandBuffer(stack);
			
			texture.changeLayout(commandBuffer, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
			IntBuffer buffer = texture.getBuffer().asIntBuffer();
			for (int element : image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth())) buffer.put(element);
			texture.sendToPhysicalDevice(commandBuffer);
			texture.changeLayout(commandBuffer, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
			
			device.submitSingleUseCommandBuffer(stack, commandBuffer);
			
			//	renderPass = RenderPassTemplate.initColorTextureTemplate(window.getSurface().getFormat().format()).createRenderPass(device);
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
			RenderEngine.terminate();
			return;
		}
		
		SCREENS.add(BlackScreen.BLACK_SCREEN);
		BasicScreen s = new BasicScreen();
		Sprite sprite = new Sprite(window, Vector2i.of(20, 20), Vector2i.of(256, 256), new TextureRegion(texture, Vector2f.origin(), Vector2f.of(1, 1)));
		s.add(sprite);
		SCREENS.add(s);
		
		window.setScreen(SCREENS.get(1));
	}
	
	public static void loop()
	{
		VulkanWindow window = RenderEngine.getVulkan().getWindow();
		
		while (!window.shouldClose())
		{
			window.pollEvents();
			window.update();
		}
	}
	
	public static void end()
	{
		RenderEngine.terminate();
	}
}
