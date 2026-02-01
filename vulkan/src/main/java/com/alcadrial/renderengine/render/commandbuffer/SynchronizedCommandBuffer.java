package com.alcadrial.renderengine.render.commandbuffer;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkBufferImageCopy;
import org.lwjgl.vulkan.VkBufferMemoryBarrier;
import org.lwjgl.vulkan.VkClearAttachment;
import org.lwjgl.vulkan.VkClearRect;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkMemoryBarrier;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkViewport;

import com.alcadrial.kector.vector.Vector3i;
import com.alcadrial.renderengine.core.util.NotYetImplementedException;
import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.pipeline.Pipeline;
import com.alcadrial.renderengine.render.texture.Texture;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.VulkanUtils;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public class SynchronizedCommandBuffer extends CommandBuffer {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private boolean isRecording;
	private CommandPool pool;
	
	public SynchronizedCommandBuffer(Device device, CommandPool pool, VkCommandBuffer commandBuffer)
	{
		super(device, pool, commandBuffer);
		vk = commandBuffer;
		isRecording = false;
		this.pool = pool;
	}
	
	@Override
	public void setName(String name)
	{
		device.setObjectDebugName(this, name);
		this.name = name;
	}
	
	@Override
	public synchronized boolean beginRecording()
	{
		if (isRecording) return false;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc(stack);
			beginInfo.sType(VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
			synchronized (pool)
			{
				if (VK10.vkBeginCommandBuffer(vk, beginInfo) < 0) return false;
			}
		}
		isRecording = true;
		return true;
	}
	
	@Override
	public synchronized void beginRenderPass(VkRenderPassBeginInfo renderPassBeginInfo, int subpassContents)
	{
		synchronized (pool)
		{
			VK10.vkCmdBeginRenderPass(vk, renderPassBeginInfo, subpassContents);
		}
	}
	
	@Override
	public synchronized void bindPipeline(Pipeline pipeline)
	{
		synchronized (pool)
		{
			VK10.vkCmdBindPipeline(vk, pipeline.bindPoint(), pipeline.getVk());
		}
	}
	
	@Override
	public synchronized void bindDescriptorSets(Pipeline pipeline, long[] descriptorSets, int @Nullable[] dynamicOffsets)
	{
		synchronized (pool)
		{
			VK10.vkCmdBindDescriptorSets(vk, pipeline.bindPoint(), pipeline.getLayout().getVk(), 0, descriptorSets, dynamicOffsets);
		}
	}
	
	@Override
	public synchronized void bindIndexBuffer(VulkanBuffer buffer, long offset, int indexType)
	{
		synchronized (pool)
		{
			VK10.vkCmdBindIndexBuffer(vk, buffer.getVk(), offset, indexType);
		}
	}
	
	@Override
	public void bindVertexBuffers(int bindingOffset, VulkanBuffer... buffers)
	{
		bindVertexBuffers(bindingOffset, List.of(buffers));
	}
	
	@Override
	public void bindVertexBuffers(int bindingOffset, List<VulkanBuffer> buffers)
	{
		bindVertexBuffers(bindingOffset, buffers.stream().mapToLong(VulkanBuffer::getVk).toArray());
	}
	
	private synchronized void bindVertexBuffers(int bindingOffset, long... buffers)
	{
		synchronized (pool)
		{
			VK10.vkCmdBindVertexBuffers(vk, bindingOffset, buffers, new long[buffers.length]);
		}
	}
	
	/**
	 * NYI
	 */
	@Override
	public void blitImage()
	{
		// VK10.vkCmdBlitImage(vk);
		throw new NotYetImplementedException();
	}
	
	@Override
	public synchronized void clear(List<VkClearAttachment> clearAttachments, List<VkClearRect> clearRects)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkClearAttachment.Buffer clearAttachmentsBuffer = VulkanUtils.toBuffer(stack, clearAttachments, VkClearAttachment::calloc);
			VkClearRect.Buffer clearRectsBuffer = VulkanUtils.toBuffer(stack, clearRects, VkClearRect::calloc);
			synchronized (pool)
			{
				VK10.vkCmdClearAttachments(vk, clearAttachmentsBuffer, clearRectsBuffer);
			}
		}
	}
	
	/**
	 * NYI
	 */
	@Override
	public void clearColorImage()
	{
		// VK10.vkCmdClearColorImage(vk);
		throw new NotYetImplementedException();
	}
	
	/**
	 * NYI
	 */
	@Override
	public void clearDepthStencilImage()
	{
		// VK10.vkCmdClearDepthStencilImage(vk);
		throw new NotYetImplementedException();
	}
	
	@Override
	public void copyBuffer(VulkanBuffer srcBuffer, long srcOffset, VulkanBuffer dstBuffer, long dstOffset, int size)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			copyBuffer(srcBuffer, dstBuffer, VulkanUtils.bufferCopy(stack, srcOffset, dstOffset, size));
		}
	}
	
	@Override
	public synchronized void copyBuffer(VulkanBuffer srcBuffer, VulkanBuffer dstBuffer, VkBufferCopy.Buffer copyRegion)
	{
		synchronized (pool)
		{
			VK10.vkCmdCopyBuffer(vk, srcBuffer.getVk(), dstBuffer.getVk(), copyRegion);
		}
	}
	
	@Override
	public void copyBufferToImage(VulkanBuffer srcBuffer, Texture dstTexture)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkBufferImageCopy.Buffer region = VkBufferImageCopy.malloc(1, stack);
			region.bufferOffset(0);
			region.bufferRowLength(0);
			region.bufferImageHeight(0);
			region.imageSubresource().aspectMask(dstTexture.getAspectMask());
			region.imageSubresource().mipLevel(0);
			region.imageSubresource().baseArrayLayer(0);
			region.imageSubresource().layerCount(1);
			region.imageOffset().set(0, 0, 0);
			region.imageExtent().set(dstTexture.getWidth(), dstTexture.getHeight(), dstTexture.getDepth());
			
			copyBufferToImage(srcBuffer, dstTexture, region);
		}
	}
	
	@Override
	public void copyBufferToImage(VulkanBuffer srcBuffer, long srcOffset, Texture dstTexture, Vector3i imageOffset, Vector3i imageExtent)
	{
		copyBufferToImage(srcBuffer, srcOffset, dstTexture, imageOffset.x(), imageOffset.y(), imageOffset.z(), imageExtent.x(), imageExtent.y(), imageExtent.z());
	}
	
	@Override
	public void copyBufferToImage(VulkanBuffer srcBuffer, long srcOffset, Texture dstTexture, int xOffset, int yOffset, int zOffset, int xExtent, int yExtent, int zExtent)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkBufferImageCopy.Buffer region = VkBufferImageCopy.malloc(1, stack);
			region.bufferOffset(srcOffset);
			region.bufferRowLength(0);
			region.bufferImageHeight(0);
			region.imageSubresource().aspectMask(dstTexture.getAspectMask());
			region.imageSubresource().mipLevel(0);
			region.imageSubresource().baseArrayLayer(0);
			region.imageSubresource().layerCount(1);
			region.imageOffset().set(xOffset, yOffset, zOffset);
			region.imageExtent().set(xExtent, yExtent, zExtent);
			
			copyBufferToImage(srcBuffer, dstTexture, region);
		}
	}
	
	@Override
	public synchronized void copyBufferToImage(VulkanBuffer srcBuffer, Texture dstTexture, VkBufferImageCopy.Buffer copyRegion)
	{
		synchronized (pool)
		{
			VK10.vkCmdCopyBufferToImage(vk, srcBuffer.getVk(), dstTexture.getVk(), VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, copyRegion);
		}
	}
	
	// copy
	
	@Override
	public synchronized void dispatch(int groupCountX, int groupCountY, int groupCountZ)
	{
		synchronized (pool)
		{
			VK10.vkCmdDispatch(vk, groupCountX, groupCountY, groupCountZ);
		}
	}
	
	@Override
	public synchronized void dispatchIndirect(VulkanBuffer buffer, long offset)
	{
		synchronized (pool)
		{
			VK10.vkCmdDispatchIndirect(vk, buffer.getVk(), offset);
		}
	}
	
	@Override
	public synchronized void draw(int vertexCount, int instanceCount, int vertexOffset, int instanceOffset)
	{
		synchronized (pool)
		{
			VK10.vkCmdDraw(vk, vertexCount, instanceCount, vertexOffset, instanceOffset);
		}
	}
	
	@Override
	public synchronized void drawIndexed(int indexCount, int instanceCount, int indexOffset, int vertexOffset, int instanceOffset)
	{
		synchronized (pool)
		{
			VK10.vkCmdDrawIndexed(vk, indexCount, instanceCount, indexOffset, vertexOffset, instanceOffset);
		}
	}
	
	@Override
	public synchronized void drawIndirect(VulkanBuffer buffer, long offset, int drawCount, int stride)
	{
		synchronized (pool)
		{
			VK10.vkCmdDrawIndirect(vk, buffer.getVk(), offset, drawCount, stride);
		}
	}
	
	@Override
	public synchronized void drawIndexedIndirect(VulkanBuffer buffer, long offset, int drawCount, int stride)
	{
		synchronized (pool)
		{
			VK10.vkCmdDrawIndexedIndirect(vk, buffer.getVk(), offset, drawCount, stride);
		}
	}
	
	@Override
	public synchronized void endRenderPass()
	{
		synchronized (pool)
		{
			VK10.vkCmdEndRenderPass(vk);
		}
	}
	
	@Override
	public synchronized void executeCommands(List<CommandBuffer> commands)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			PointerBuffer buffer = VulkanUtils.toAddressBuffer(stack, commands);
			synchronized (pool)
			{
				VK10.vkCmdExecuteCommands(vk, buffer);
			}
		}
	}
	
	@Override
	public synchronized void fillBuffer(VulkanBuffer buffer, long offset, long size, int value)
	{
		synchronized (pool)
		{
			VK10.vkCmdFillBuffer(vk, buffer.getVk(), offset, size, value);
		}
	}
	
	/**
	 *
	 * @param subpassContents either {@link VK10#VK_SUBPASS_CONTENTS_INLINE} or {@link VK10#VK_SUBPASS_CONTENTS_SECONDARY_COMMAND_BUFFERS}
	 */
	@Override
	public synchronized void nextSubpass(int subpassContents)
	{
		synchronized (pool)
		{
			VK10.vkCmdNextSubpass(vk, subpassContents);
		}
	}
	
	@Override
	public synchronized void pipelineBarrier(int srcStageMask, int dstStageMask, int dependencyFlags, VkMemoryBarrier.@Nullable Buffer memoryBarriers, VkBufferMemoryBarrier.@Nullable Buffer bufferMemoryBarriers, VkImageMemoryBarrier.@Nullable Buffer imageMemoryBarriers)
	{
		synchronized (pool)
		{
			VK10.vkCmdPipelineBarrier(vk, srcStageMask, dstStageMask, dependencyFlags, memoryBarriers, bufferMemoryBarriers, imageMemoryBarriers);
		}
	}
	
	// pushConstants
	
	/**
	 * NYI
	 */
	@Override
	public void resetEvent()
	{
		// VK10.vkCmdResetEvent(vk);
		throw new NotYetImplementedException();
	}
	
	/**
	 * NYI
	 */
	@Override
	public void resetQueryPool()
	{
		// VK10.vkCmdResetQueryPool(vk);
		throw new NotYetImplementedException();
	}
	
	/**
	 * NYI
	 */
	@Override
	public void resolveImage()
	{
		// VK10.vkCmdResolveImage(vk);
		throw new NotYetImplementedException();
	}
	
	// setters
	// updateBuffer
	
	/**
	 * NYI
	 */
	@Override
	public void waitEvent()
	{
		// VK10.vkCmdWaitEvent(vk);
		throw new NotYetImplementedException();
	}
	
	/**
	 * NYI
	 */
	@Override
	public void writeTimestamp()
	{
		// VK10.vkCmdWriteTimestamp(vk);
		throw new NotYetImplementedException();
	}
	
	@Override
	public void setScissor(int x, int y, int width, int height)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			setScissor(VulkanUtils.rect2d(stack, x, y, width, height));
		}
	}
	
	@Override
	public synchronized void setScissor(VkRect2D.Buffer scissors)
	{
		synchronized (pool)
		{
			VK10.vkCmdSetScissor(vk, 0, scissors);
		}
	}
	
	@Override
	public void setViewport(int x, int y, int width, int height, int minZ, int maxZ)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			setViewport(VulkanUtils.viewport(stack, x, y, width, height, minZ, maxZ));
		}
	}
	
	@Override
	public synchronized void setViewport(VkViewport.Buffer viewports)
	{
		synchronized (pool)
		{
			VK10.vkCmdSetViewport(vk, 0, viewports);
		}
	}
	
	@Override
	public synchronized boolean endRecording() throws VulkanException
	{
		if (!isRecording) return false;
		isRecording = false;
		synchronized (pool)
		{
			VulkanErrorUtils.assertNoError(VK10.vkEndCommandBuffer(vk), "Error during command buffer registration (" + Debug.address(vk) + ")");
		}
		return true;
	}
	
	@Override
	public synchronized void reset()
	{
		synchronized (pool)
		{
			VK10.vkResetCommandBuffer(vk, 0);
		}
	}
	
	@Override
	public synchronized boolean isRecording()
	{
		return isRecording;
	}
	
	@Override
	public void close()
	{
		synchronized (pool)
		{
			VK10.vkFreeCommandBuffers(device.getVk(), pool.getVk(), vk);
		}
		super.close();
	}
	
	@Override
	public void debug()
	{
		Debug.print("vk: ", vk);
		Debug.print("pool: ", pool);
	}
}
