package com.alcadrial.renderengine.render.commandbuffer;

import java.util.List;

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
import com.alcadrial.renderengine.core.util.Nullable;
import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.pipeline.Pipeline;
import com.alcadrial.renderengine.render.texture.Texture;
import com.alcadrial.renderengine.render.util.AbstractDeviceResource;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.VulkanUtils;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public class CommandBuffer extends AbstractDeviceResource<VkCommandBuffer> {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private boolean isRecording;
	private CommandPool pool;
	
	public CommandBuffer(Device device, CommandPool pool, VkCommandBuffer commandBuffer)
	{
		super(device);
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
	
	public boolean beginRecording()
	{
		if (isRecording) return false;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc(stack)
				.sType(VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
			if (VK10.vkBeginCommandBuffer(vk, beginInfo) < 0) return false;
		}
		isRecording = true;
		return true;
	}
	
	public void beginRenderPass(VkRenderPassBeginInfo renderPassBeginInfo, int subpassContents)
	{
		VK10.vkCmdBeginRenderPass(vk, renderPassBeginInfo, subpassContents);
	}
	
	public void bindPipeline(Pipeline pipeline)
	{
		VK10.vkCmdBindPipeline(vk, pipeline.bindPoint(), pipeline.getVk());
	}
	
	public void bindDescriptorSets(Pipeline pipeline, long[] descriptorSets, int[] dynamicOffsets)
	{
		VK10.vkCmdBindDescriptorSets(vk, pipeline.bindPoint(), pipeline.getLayout().getVk(), 0, descriptorSets, dynamicOffsets);
	}
	
	public void bindIndexBuffer(VulkanBuffer buffer, long offset, int indexType)
	{
		VK10.vkCmdBindIndexBuffer(vk, buffer.getVk(), offset, indexType);
	}
	
	public void bindVertexBuffers(int bindingOffset, VulkanBuffer... buffers)
	{
		bindVertexBuffers(bindingOffset, List.of(buffers));
	}
	
	public void bindVertexBuffers(int bindingOffset, List<VulkanBuffer> buffers)
	{
		bindVertexBuffers(bindingOffset, buffers.stream().mapToLong(VulkanBuffer::getVk).toArray());
	}
	
	private void bindVertexBuffers(int bindingOffset, long... buffers)
	{
		VK10.vkCmdBindVertexBuffers(vk, bindingOffset, buffers, new long[buffers.length]);
	}
	
	/**
	 * NYI
	 */
	public void blitImage()
	{
		// VK10.vkCmdBlitImage(vk);
		throw new NotYetImplementedException();
	}
	
	public void clear(List<VkClearAttachment> clearAttachments, List<VkClearRect> clearRects)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkClearAttachment.Buffer clearAttachmentsBuffer = VulkanUtils.toBuffer(stack, clearAttachments, VkClearAttachment::calloc);
			VkClearRect.Buffer clearRectsBuffer = VulkanUtils.toBuffer(stack, clearRects, VkClearRect::calloc);
			VK10.vkCmdClearAttachments(vk, clearAttachmentsBuffer, clearRectsBuffer);
		}
	}
	
	/**
	 * NYI
	 */
	public void clearColorImage()
	{
		// VK10.vkCmdClearColorImage(vk);
		throw new NotYetImplementedException();
	}
	
	/**
	 * NYI
	 */
	public void clearDepthStencilImage()
	{
		// VK10.vkCmdClearDepthStencilImage(vk);
		throw new NotYetImplementedException();
	}
	
	public void copyBuffer(VulkanBuffer srcBuffer, long srcOffset, VulkanBuffer dstBuffer, long dstOffset, int size)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			copyBuffer(srcBuffer, dstBuffer, VulkanUtils.bufferCopy(stack, srcOffset, dstOffset, size));
		}
	}
	
	public void copyBuffer(VulkanBuffer srcBuffer, VulkanBuffer dstBuffer, VkBufferCopy.Buffer copyRegion)
	{
		VK10.vkCmdCopyBuffer(vk, srcBuffer.getVk(), dstBuffer.getVk(), copyRegion);
	}
	
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
	
	public void copyBufferToImage(VulkanBuffer srcBuffer, long srcOffset, Texture dstTexture, Vector3i imageOffset, Vector3i imageExtent)
	{
		copyBufferToImage(srcBuffer, srcOffset, dstTexture, imageOffset.x(), imageOffset.y(), imageOffset.z(), imageExtent.x(), imageExtent.y(), imageExtent.z());
	}
	
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
	
	public void copyBufferToImage(VulkanBuffer srcBuffer, Texture dstTexture, VkBufferImageCopy.Buffer copyRegion)
	{
		VK10.vkCmdCopyBufferToImage(vk, srcBuffer.getVk(), dstTexture.getVk(), VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, copyRegion);
	}
	
	// copy
	
	public void dispatch(int groupCountX, int groupCountY, int groupCountZ)
	{
		VK10.vkCmdDispatch(vk, groupCountX, groupCountY, groupCountZ);
	}
	
	public void dispatchIndirect(VulkanBuffer buffer, long offset)
	{
		VK10.vkCmdDispatchIndirect(vk, buffer.getVk(), offset);
	}
	
	public void draw(int vertexCount, int instanceCount, int vertexOffset, int instanceOffset)
	{
		VK10.vkCmdDraw(vk, vertexCount, instanceCount, vertexOffset, instanceOffset);
	}
	
	public void drawIndexed(int indexCount, int instanceCount, int indexOffset, int vertexOffset, int instanceOffset)
	{
		VK10.vkCmdDrawIndexed(vk, indexCount, instanceCount, indexOffset, vertexOffset, instanceOffset);
	}
	
	public void drawIndirect(VulkanBuffer buffer, long offset, int drawCount, int stride)
	{
		VK10.vkCmdDrawIndirect(vk, buffer.getVk(), offset, drawCount, stride);
	}
	
	public void drawIndexedIndirect(VulkanBuffer buffer, long offset, int drawCount, int stride)
	{
		VK10.vkCmdDrawIndexedIndirect(vk, buffer.getVk(), offset, drawCount, stride);
	}
	
	public void endRenderPass()
	{
		VK10.vkCmdEndRenderPass(vk);
	}
	
	public void executeCommands(List<CommandBuffer> commands)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			PointerBuffer buffer = VulkanUtils.toAddressBuffer(stack, commands);
			VK10.vkCmdExecuteCommands(vk, buffer);
		}
	}
	
	public void fillBuffer(VulkanBuffer buffer, long offset, long size, int value)
	{
		VK10.vkCmdFillBuffer(vk, buffer.getVk(), offset, size, value);
	}
	
	/**
	 *
	 * @param subpassContents either {@link VK10#VK_SUBPASS_CONTENTS_INLINE} or {@link VK10#VK_SUBPASS_CONTENTS_SECONDARY_COMMAND_BUFFERS}
	 */
	public void nextSubpass(int subpassContents)
	{
		VK10.vkCmdNextSubpass(vk, subpassContents);
	}
	
	public void pipelineBarrier(int srcStageMask, int dstStageMask, int dependencyFlags, @Nullable VkMemoryBarrier.Buffer memoryBarriers, @Nullable VkBufferMemoryBarrier.Buffer bufferMemoryBarriers, @Nullable VkImageMemoryBarrier.Buffer imageMemoryBarriers)
	{
		VK10.vkCmdPipelineBarrier(vk, srcStageMask, dstStageMask, dependencyFlags, memoryBarriers, bufferMemoryBarriers, imageMemoryBarriers);
	}
	
	// pushConstants
	
	/**
	 * NYI
	 */
	public void resetEvent()
	{
		// VK10.vkCmdResetEvent(vk);
		throw new NotYetImplementedException();
	}
	
	/**
	 * NYI
	 */
	public void resetQueryPool()
	{
		// VK10.vkCmdResetQueryPool(vk);
		throw new NotYetImplementedException();
	}
	
	/**
	 * NYI
	 */
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
	public void waitEvent()
	{
		// VK10.vkCmdWaitEvent(vk);
		throw new NotYetImplementedException();
	}
	
	/**
	 * NYI
	 */
	public void writeTimestamp()
	{
		// VK10.vkCmdWriteTimestamp(vk);
		throw new NotYetImplementedException();
	}
	
	public void setScissor(int x, int y, int width, int height)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			setScissor(VulkanUtils.rect2d(stack, x, y, width, height));
		}
	}
	
	public void setScissor(VkRect2D.Buffer scissors)
	{
		VK10.vkCmdSetScissor(vk, 0, scissors);
	}
	
	public void setViewport(int x, int y, int width, int height, int minZ, int maxZ)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			setViewport(VulkanUtils.viewport(stack, x, y, width, height, minZ, maxZ));
		}
	}
	
	public void setViewport(VkViewport.Buffer viewports)
	{
		VK10.vkCmdSetViewport(vk, 0, viewports);
	}
	
	public boolean endRecording() throws VulkanException
	{
		if (!isRecording) return false;
		isRecording = false;
		VulkanErrorUtils.assertNoError(VK10.vkEndCommandBuffer(vk), "Error during command buffer registration (" + Debug.address(vk) + ")");
		return true;
	}
	
	public void reset()
	{
		VK10.vkResetCommandBuffer(vk, 0);
	}
	
	public boolean isRecording()
	{
		return isRecording;
	}
	
	@Override
	public void close()
	{
		VK10.vkFreeCommandBuffers(device.getVk(), pool.getVk(), vk);
		super.close();
	}
	
	@Override
	public void debug()
	{
		Debug.print("vk: ", vk);
		Debug.print("pool: ", pool);
	}
}
