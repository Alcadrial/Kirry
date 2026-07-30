package com.alcadrial.kirry.render.test;

import java.util.concurrent.CompletableFuture;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.kector.vector.Vector3fr;
import com.alcadrial.kector.vector.Vector4f;
import com.alcadrial.kirry.core.util.color.ColorRGB;
import com.alcadrial.kirry.render.Device;
import com.alcadrial.kirry.render.FrameBuffer;
import com.alcadrial.kirry.render.KirryVulkan;
import com.alcadrial.kirry.render.LinearComputer;
import com.alcadrial.kirry.render.PhysicalDevice;
import com.alcadrial.kirry.render.buffer.VulkanBuffer;
import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;
import com.alcadrial.kirry.render.component.PipelineOutputTarget.SurfaceTarget;
import com.alcadrial.kirry.render.font.DefaultFontRenderer;
import com.alcadrial.kirry.render.font.DefaultStringFormatter;
import com.alcadrial.kirry.render.font.Font;
import com.alcadrial.kirry.render.font.FontRenderer;
import com.alcadrial.kirry.render.font.StringFormatter;
import com.alcadrial.kirry.render.memory.MemoryManager;
import com.alcadrial.kirry.render.memory.MemoryTransferState;
import com.alcadrial.kirry.render.resources.FileManager;
import com.alcadrial.kirry.render.resources.ShaderResource;
import com.alcadrial.kirry.render.shader.PreparedShader;
import com.alcadrial.kirry.render.shader.ShaderProgram;
import com.alcadrial.kirry.render.test.util.RecompileShaderResource;
import com.alcadrial.kirry.render.test.util.RenderTest;
import com.alcadrial.kirry.render.test.util.TestProperties;
import com.alcadrial.kirry.render.util.exception.FreeTypeException;
import com.alcadrial.kirry.render.util.exception.VulkanException;
import com.alcadrial.kirry.render.util.future.VulkanFuture;
import com.alcadrial.kirry.render.window.VulkanWindow;

public class TextTest extends RenderTest {
	
	public static final String MESSAGE = """
		
		Alkaisr Tuillà
		
		ShaderResource fontVertexShaderResource = fileManager.getResource(Font.FONT_VERTEX_SHADER_PATHNAME, ShaderResource.TYPE);
		ShaderResource fontGeometryShaderResource = fileManager.getResource(Font.FONT_GEOMETRY_SHADER_PATHNAME, ShaderResource.TYPE);
		ShaderResource fontFragmentShaderResource = fileManager.getResource(Font.FONT_FRAGMENT_SHADER_PATHNAME, ShaderResource.TYPE);
		ShaderResource linearShaderResource = fileManager.getResource(KirryVulkan.SHADERS_PATH + "linearShader.comp", ShaderResource.TYPE);
		""";
	public static final ColorRGB COLOR = ColorRGB.LIGHT_YELLOW;
	public static final Vector3f POS = Vector3f.of();
	public static final float MAX_WIDTH = 1200;
	public static final float MAX_HEIGHT = 800;
	public static final float SCALE = 0.33F;
	
	private static MemoryManager memoryManager = null;
	
	public static void main(String[] args)
	{
		try
		{
			fileManager = FileManager.of(
				"src/test/resources",
				"src/main/resources",
				FileManager.loaderJarResourceUri(ASSETS)
			);
			fileManager.registerResourceType(ShaderResource.TYPE, RecompileShaderResource::new);
			fileManager.startWatchingResources();
			
			ShaderResource fontVertexShaderResource = fileManager.getResource(Font.FONT_VERTEX_SHADER_PATHNAME, ShaderResource.TYPE);
			ShaderResource fontGeometryShaderResource = fileManager.getResource(Font.FONT_GEOMETRY_SHADER_PATHNAME, ShaderResource.TYPE);
			ShaderResource fontFragmentShaderResource = fileManager.getResource(Font.FONT_FRAGMENT_SHADER_PATHNAME, ShaderResource.TYPE);
			ShaderResource linearShaderResource = fileManager.getResource(LinearComputer.COMPUTE4X4_SHADER_PATHNAME, ShaderResource.TYPE);
			
			TestProperties.init(args);
			KirryVulkan.enableDebug();
			RenderTest.initKirry().multiThreading(false).useFont(true).initialize();
			
			VulkanFuture<ShaderProgram> shaderProgramFuture = VulkanFuture.submitAsync(() -> {
				
				CompletableFuture.allOf(fontVertexShaderResource.load(), fontGeometryShaderResource.load(), fontFragmentShaderResource.load()).join();
				
				return ShaderProgram.create()
					.add(fontVertexShaderResource.getShader())
					.add(fontGeometryShaderResource.getShader())
					.add(fontFragmentShaderResource.getShader())
					.build();
			});
			VulkanFuture<PreparedShader> computeShaderFuture = VulkanFuture.submitAsync(() -> {
				linearShaderResource.load().join();
				return linearShaderResource.getShader();
			});
			
			KirryVulkan.setWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, true);
			KirryVulkan.setWindowHint(GLFW.GLFW_VISIBLE, false);
			VulkanWindow window = VulkanWindow.defaultBuilder()
				.physicalDeviceStrategy(PhysicalDevice.Strategy.atIndexOrElse(TestProperties.DEVICE.getValue(), PhysicalDevice.Strategy.findBest()))
				.deviceFeatures(f -> f
					.geometryShader(true)
					.samplerAnisotropy(true)
				)
				.useDepthTexture(false)
				.build((int) MAX_WIDTH, (int) MAX_HEIGHT, "Test");
			registerDragging();
			
			Device device = window.getDevice();
			memoryManager = MemoryManager.getDefault(device);
			
			VulkanBuffer uniformBuffer = VulkanBuffer.ofUniformsBindings(device, 3, (16 + 16 + 4) * 4);
			VulkanBuffer vertexBuffer = memoryManager.allocate(MESSAGE.length() * (1 + 3) * 4, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT);
			
			VulkanBuffer offsetUniformBuffer = VulkanBuffer.alignedView(uniformBuffer, 0, (16 + 3) * 4);
			Font font = new Font(device, getPath(FONTS + "ITCBLKAD.TTF"), 0, 0, memoryManager, computeShaderFuture.get(), offsetUniformBuffer);
			//Font font = new Font(window.getDevice(), getPath(FONTS + "ARIALI.TTF"), 0, VK10.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT, memoryManager);
			
			font.setSize(120);
			
			int nonEmptyCharCount[] = new int[] {0};
			VulkanFuture<?> dataReady = device.configureAndSubmitSingleUseCommandBuffer(device.getGraphicsQueue(), commandBuffer -> {
				
				font.loadAlphabet();
				for (char c : MESSAGE.toCharArray())
				{
					try
					{
						if (!font.getData(c).isEmpty()) nonEmptyCharCount[0]++;
					}
					catch (FreeTypeException _)
					{}
				}
				font.prepare(commandBuffer);
				
				try (MemoryTransferState transfer = memoryManager.allocateForTransfer(commandBuffer, vertexBuffer))
				{
					DefaultStringFormatter formatter = StringFormatter.ofDefault();
					formatter.setMaxWidth(MAX_WIDTH);
					formatter.setMaxHeight(MAX_HEIGHT);
					formatter.setScale(SCALE);
					formatter.setOrigin(POS);
					formatter.loadText(font, MESSAGE, transfer.getWriter(vertexBuffer));
				}
			});
			
			DefaultFontRenderer fontRenderer = new DefaultFontRenderer(device, new SurfaceTarget(window), shaderProgramFuture.get());
			
			dataReady.join();
			
			fontRenderer.prepare(font, VulkanBuffer.alignedView(uniformBuffer, offsetUniformBuffer.getOffset(), (int) (uniformBuffer.getSize() - offsetUniformBuffer.getOffset())));
			fontRenderer.setProjection(new Matrix4f().orthographicPositive(MAX_WIDTH, MAX_HEIGHT));
			fontRenderer.setModel(new Matrix4f().scale(Vector3fr.of(SCALE, SCALE, 1)));
			fontRenderer.setColor(Vector4f.of(COLOR.r(), COLOR.g(), COLOR.b(), COLOR.a()).divideAll(255));
			fontRenderer.setDataBuffer(vertexBuffer);
			
			if (!window.shouldClose())
			{
				draw(fontRenderer, nonEmptyCharCount[0], vertexBuffer);
				window.show();
				window.waitEvents();
			}
			while (!window.shouldClose())
			{
				draw(fontRenderer, nonEmptyCharCount[0], vertexBuffer);
				window.waitEvents();
			}
			font.close();
			memoryManager.close();
		}
		catch (Exception e)
		{
			e.printStackTrace(System.out);
			if (memoryManager != null) memoryManager.close();
		}
		finally
		{
			KirryVulkan.terminate();
		}
	}
	
	public static void draw(FontRenderer renderer, int count, VulkanBuffer vertexBuffer) throws VulkanException, VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VulkanWindow window = KirryVulkan.getVulkan().getWindow();
			switch (window.startPresentation())
			{
				case EMPTY_SURFACE:
					return;
				case SURFACE_LOST, OUT_OF_MEMORY, FATAL_ERROR:
					throw new VulkanException("Presentaton error");
			}
			CommandBuffer commandBuffer = window.getCurrentCommandBuffer();
			VkRenderPassBeginInfo renderPassBeginInfo = VkRenderPassBeginInfo.malloc(stack);
			renderPassBeginInfo.sType(VK10.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
			renderPassBeginInfo.pNext(0);
			renderPassBeginInfo.renderPass(window.getRenderPass().getVk());
			
			FrameBuffer frameBuffer = window.getCurrentFrameBuffer();
			renderPassBeginInfo.framebuffer(frameBuffer.getVk());
			
			VkRect2D renderArea = VkRect2D.calloc(stack);
			renderArea.extent(VkExtent2D.malloc(stack).set(frameBuffer.getWidth(), frameBuffer.getHeight()));
			renderPassBeginInfo.renderArea(renderArea);
			
			VkClearValue.Buffer clearValues = VkClearValue.calloc(2, stack);
			clearValues.get().color().float32(stack.floats(0, 0, 0, 0));
			clearValues.get().depthStencil().depth(1).stencil(0);
			renderPassBeginInfo.pClearValues(clearValues.rewind());
			
			commandBuffer.beginRenderPass(renderPassBeginInfo, VK10.VK_SUBPASS_CONTENTS_INLINE);
			renderer.draw(commandBuffer, count);
			commandBuffer.endRenderPass();
			window.endPresentation(commandBuffer);
		}
	}
}
