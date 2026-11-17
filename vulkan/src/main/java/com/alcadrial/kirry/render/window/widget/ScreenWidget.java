package com.alcadrial.kirry.render.window.widget;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.vulkan.VK10;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2ir;
import com.alcadrial.kector.vector.Vector3ir;
import com.alcadrial.kirry.render.Renderer;
import com.alcadrial.kirry.render.RenderingDataUtils;
import com.alcadrial.kirry.render.buffer.VulkanBuffer;
import com.alcadrial.kirry.render.buffer.writer.DeferredBufferWriter;
import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;
import com.alcadrial.kirry.render.memory.MemoryManager;
import com.alcadrial.kirry.render.memory.MemoryTransferState;
import com.alcadrial.kirry.render.texture.Texture;
import com.alcadrial.kirry.render.texture.TextureManager;
import com.alcadrial.kirry.render.util.Lazy;
import com.alcadrial.kirry.render.util.VLazy;
import com.alcadrial.kirry.render.util.exception.VulkanException;
import com.alcadrial.kirry.render.window.AbstractWidget;
import com.alcadrial.kirry.render.window.EventRegistry;
import com.alcadrial.kirry.render.window.FrameScreen;
import com.alcadrial.kirry.render.window.VulkanWindow;

public class ScreenWidget extends AbstractWidget {
	
	public static ScreenWidget ofFull(VulkanWindow window)
	{
		return ofWindowed(window, Vector3ir.of(), window.getSurface().getData().size());
	}
	
	public static ScreenWidget ofWindowed(VulkanWindow window, Vector3ir pos, Vector2ir size)
	{
		return new ScreenWidget(window, pos, size);
	}
	
	private Lazy<FrameScreen> lazyScreen;
	private VLazy<VulkanBuffer> lazyVertexBuffer;
	private VLazy<VulkanBuffer> lazyIndexBuffer;
	private int textureIndex;
	private @Nullable Matrix4f clampMatrix;
	
	public ScreenWidget(VulkanWindow window, Vector3ir pos, Vector2ir size)
	{
		super(window, pos, size);
		lazyScreen = new Lazy<>();
		lazyVertexBuffer = new VLazy<>();
		lazyIndexBuffer = new VLazy<>();
		textureIndex = -1;
	}
	
	public void setScreen(FrameScreen screen)
	{
		lazyScreen.set(screen);
	}
	
	@Override
	public void initTextures(MemoryManager memoryManager, CommandBuffer commandBuffer, TextureManager textureManager) throws VulkanException
	{
		if (textureIndex != -1)
		{
			textureManager.removeTexture(textureIndex);
			textureIndex = -1;
		}
		Texture texture = lazyScreen.get().getOutput().getTextures()[0];
		textureIndex = textureManager.addTexture(texture);
		texture.changeLayout(commandBuffer, VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
		super.initTextures(memoryManager, commandBuffer, textureManager);
	}
	
	@Override
	public void initBuffers(MemoryManager memoryManager, CommandBuffer commandBuffer) throws VulkanException
	{
		try
		{
			lazyVertexBuffer.abortAndSet(memoryManager.allocate(4 * 5 * 4, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT));
			lazyIndexBuffer.abortAndSet(memoryManager.allocate(6 * 4, VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT | VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT));
			
			try (MemoryTransferState transfer = memoryManager.allocateForTransfer(commandBuffer, lazyVertexBuffer.get(), lazyIndexBuffer.get()))
			{
				DeferredBufferWriter localVertexWriter = transfer.getWriter(lazyVertexBuffer.get());
				DeferredBufferWriter localIndexWriter = transfer.getWriter(lazyIndexBuffer.get());
				
				RenderingDataUtils.loadRectangleUv(localVertexWriter, localIndexWriter, 0, pos.x(), pos.y(), pos.x() + size.x(), pos.y() + size.y(), 1, 0, 0, 1, 1);
			}
			
			lazyScreen.get().init(memoryManager, commandBuffer);
		}
		catch (VulkanException e)
		{
			throw e;
		}
		super.initBuffers(memoryManager, commandBuffer);
	}
	
	@Override
	public void render(Matrix4f matrix, CommandBuffer commandBuffer, Renderer renderer) throws VulkanException
	{
		matrix = getClampMatrix();
		lazyScreen.get().render(matrix, commandBuffer);
		lazyScreen.get().draw(matrix, commandBuffer);
		super.render(matrix, commandBuffer, renderer);
	}
	
	@Override
	public void draw(Matrix4f matrix, CommandBuffer commandBuffer, Renderer renderer, TextureManager textureManager) throws VulkanException
	{
		renderer.setBuffer(0, lazyVertexBuffer.get());
		renderer.setIndexBuffer(lazyIndexBuffer.get());
		renderer.draw(commandBuffer, textureManager.textureIndexToGlobal(textureIndex));
	}
	
	protected Matrix4f getClampMatrix()
	{
		if (clampMatrix == null) clampMatrix = new Matrix4f().orthographicPositive(size.x(), size.y());
		return (@NonNull Matrix4f) clampMatrix;
	}
	
	@Override
	protected void onResize(Vector2ir oldSize, Vector2ir newSize)
	{
		clampMatrix = null;
	}
	
	@Override
	public void close() throws VulkanException
	{
		lazyVertexBuffer.close();
		lazyIndexBuffer.close();
	}
	
	@Override
	public void registerEvents(EventRegistry registry)
	{
		//lazyScreen.get().registerEvents(registry);
	}
}
