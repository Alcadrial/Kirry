package com.alcadrial.renderengine.render.test.widgets;

import com.alcadrial.renderengine.render.test.util.RenderTest;

public class ScreenTest extends RenderTest {
	/*
		public static void main(String[] args)
		{
			mainTest(args, ScreenTest::start, ScreenTest::loop, ScreenTest::end);
		}
	
		private static final String SHADER = "shader0";
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
							if (index > 0)
							{
								index--;
							}
							window.setScreen(SCREENS.get(index));
						}
						case GLFW.GLFW_KEY_DOWN -> {
							if (index < SCREENS.size() - 1)
							{
								index++;
							}
							window.setScreen(SCREENS.get(index));
						}
					}
				}
			});
			window.setShaderProgram(shaderProgram);
	
			if (!pickPhysicalDevice(window))
			{
				return;
			}
	
			VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.calloc();
			deviceFeatures.wideLines(true);
			deviceFeatures.samplerAnisotropy(true);
			Device device = vulkan.createDevice(deviceFeatures, KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME, EXTDepthRangeUnrestricted.VK_EXT_DEPTH_RANGE_UNRESTRICTED_EXTENSION_NAME);
			window.init(device);
	
			Texture textureKhishweTransparent;
			Texture textureIcon;
	
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				BufferedImage khishweTransparent = ImageIO.read(getFile("KhishweTransparent.png"));
	
				textureKhishweTransparent = Texture.create2dTexture(device, khishweTransparent.getWidth(), khishweTransparent.getHeight(), VK10.VK_FORMAT_R8G8B8A8_SRGB, VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK10.VK_IMAGE_USAGE_SAMPLED_BIT, VK10.VK_IMAGE_ASPECT_COLOR_BIT);
	
				CommandBuffer commandBuffer = device.createSingleUseCommandBuffer();
	
				textureKhishweTransparent.changeLayout(commandBuffer, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
				IntBuffer buffer = textureKhishweTransparent.getBuffer().asIntBuffer();
				for (int element : khishweTransparent.getRGB(0, 0, khishweTransparent.getWidth(), khishweTransparent.getHeight(), null, 0, khishweTransparent.getWidth()))
				{
					buffer.put(element);
				}
				textureKhishweTransparent.sendToPhysicalDevice(commandBuffer);
				textureKhishweTransparent.changeLayout(commandBuffer, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
	
				device.submitSingleUseCommandBuffer(stack, commandBuffer);
	
				BufferedImage icon = ImageIO.read(getFile("icon.png"));
	
				textureIcon = Texture.create2dTexture(device, icon.getWidth(), icon.getHeight(), VK10.VK_FORMAT_R8G8B8A8_SRGB, VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK10.VK_IMAGE_USAGE_SAMPLED_BIT, VK10.VK_IMAGE_ASPECT_COLOR_BIT);
	
				commandBuffer = device.createSingleUseCommandBuffer(stack);
	
				textureIcon.changeLayout(commandBuffer, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
				buffer = textureIcon.getBuffer().asIntBuffer();
				for (int element : icon.getRGB(0, 0, icon.getWidth(), icon.getHeight(), null, 0, icon.getWidth()))
				{
					buffer.put(element);
				}
				textureIcon.sendToPhysicalDevice(commandBuffer);
				textureIcon.changeLayout(commandBuffer, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
	
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
			s.add(new Sprite(window, Vector2i.of(20, 20), Vector2i.of(256, 256), new TextureRegion(textureKhishweTransparent, Vector2f.origin(), Vector2f.of(1, 1))));
			s.add(new Sprite(window, Vector2i.of(296, 20), Vector2i.of(256, 256), new TextureRegion(textureIcon, Vector2f.origin(), Vector2f.of(1, 1))));
			s.add(new Sprite(window, Vector2i.of(20, 296), Vector2i.of(256, 256), new TextureRegion(textureIcon, Vector2f.origin(), Vector2f.of(1, 1))));
			s.add(new Sprite(window, Vector2i.of(296, 296), Vector2i.of(256, 256), new TextureRegion(textureKhishweTransparent, Vector2f.origin(), Vector2f.of(1, 1))));
			SCREENS.add(s);
	
			window.setScreen(SCREENS.get(0));
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
		}*/
}
