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
import org.lwjgl.vulkan.VkClearColorValue;
import org.lwjgl.vulkan.VkClearRect;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkImageCopy;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkImageSubresourceRange;
import org.lwjgl.vulkan.VkMemoryBarrier;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkViewport;

import com.alcadrial.kector.vector.Vector3i;
import com.alcadrial.renderengine.core.util.NotYetImplementedException;
import com.alcadrial.renderengine.core.util.color.Color;
import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngineVulkan;
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
		RenderEngineVulkan.assertLoaded();
	}
	
	private boolean isRecording;
	private CommandPool pool;
	
	public CommandBuffer(Device device, CommandPool pool, VkCommandBuffer commandBuffer)
	{
		super(device);
		vk = commandBuffer;
		isRecording = false;
		this.pool = pool;
		Debug.debugCreate(() -> "Created " + this + " of " + pool);
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
	
	public void bindDescriptorSets(Pipeline pipeline, long[] descriptorSets, int @Nullable [] dynamicOffsets)
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
	
	public void clearColorImage(Texture texture, Color color)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VK10.vkCmdClearColorImage(vk, texture.getVk(), texture.getLayout(),
				VkClearColorValue.malloc(stack)
					.float32(stack.floats(color.r() / 255, color.g() / 255, color.b() / 255, color.a() / 255)),
				VkImageSubresourceRange.malloc(stack)
					.aspectMask(texture.getAspectMask())
					.baseMipLevel(0)
					.levelCount(1)
					.baseArrayLayer(0)
					.layerCount(1)
			);
		}
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
			copyBuffer(srcBuffer, dstBuffer, VkBufferCopy.malloc(1, stack)
				.srcOffset(srcOffset)
				.dstOffset(dstOffset)
				.size(size)
			);
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
			copyBufferToImage(srcBuffer, dstTexture, VkBufferImageCopy.malloc(1, stack)
				.bufferOffset(0)
				.bufferRowLength(0)
				.bufferImageHeight(0)
				.imageSubresource(s -> s
					.aspectMask(dstTexture.getAspectMask())
					.mipLevel(0)
					.baseArrayLayer(0)
					.layerCount(1))
				.imageOffset(o -> o.set(0, 0, 0))
				.imageExtent(e -> e.set(dstTexture.getWidth(), dstTexture.getHeight(), dstTexture.getDepth()))
			);
		}
	}
	
	public void copyBufferToImage(VulkanBuffer srcBuffer, long srcOffset, Texture dstTexture, Vector3i imageOffset, Vector3i imageExtent)
	{
		copyBufferToImage(srcBuffer, srcOffset, dstTexture, imageOffset.x(), imageOffset.y(), imageOffset.z(), imageExtent.x(), imageExtent.y(), imageExtent.z());
	}
	
	public void copyBufferToImage(VulkanBuffer srcBuffer, long srcOffset, Texture dstTexture, int xOffset, int yOffset, int zOffset, int width, int height, int depth)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			copyBufferToImage(srcBuffer, dstTexture, VkBufferImageCopy.malloc(1, stack)
				.bufferOffset(srcOffset)
				.bufferRowLength(0)
				.bufferImageHeight(0)
				.imageSubresource(i -> i
					.aspectMask(dstTexture.getAspectMask())
					.mipLevel(0)
					.baseArrayLayer(0)
					.layerCount(1)
				)
				.imageOffset(o -> o.set(xOffset, yOffset, zOffset))
				.imageExtent(e -> e.set(width, height, depth))
			);
		}
	}
	
	public void copyBufferToImage(VulkanBuffer srcBuffer, Texture dstTexture, VkBufferImageCopy.Buffer copyRegion)
	{
		copyBufferToImage(srcBuffer, dstTexture, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, copyRegion);
	}
	
	public void copyBufferToImage(VulkanBuffer srcBuffer, Texture dstTexture, int dstLayout, VkBufferImageCopy.Buffer copyRegion)
	{
		VK10.vkCmdCopyBufferToImage(vk, srcBuffer.getVk(), dstTexture.getVk(), dstLayout, copyRegion);
	}
	
	public void copyImage(Texture srcTexture, int srcXOffset, int srcYOffset, int srcZOffset, Texture dstTexture, int dstXOffset, int dstYOffset, int dstZOffset, int width, int height, int depth)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			copyImage(srcTexture, dstTexture, VkImageCopy.malloc(1, stack)
				.srcOffset(o -> o.set(srcXOffset, srcYOffset, srcZOffset))
				.srcSubresource(i -> i
					.aspectMask(srcTexture.getAspectMask())
					.mipLevel(0)
					.baseArrayLayer(0)
					.layerCount(1)
				)
				.dstOffset(o -> o.set(dstXOffset, dstYOffset, dstZOffset))
				.dstSubresource(i -> i
					.aspectMask(dstTexture.getAspectMask())
					.mipLevel(0)
					.baseArrayLayer(0)
					.layerCount(1)
				)
				.extent(e -> e.set(width, height, depth))
			);
		}
	}
	
	public void copyImage(Texture srcTexture, Texture dstTexture, VkImageCopy.Buffer copyRegion)
	{
		copyImage(srcTexture, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, dstTexture, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, copyRegion);
	}
	
	public void copyImage(Texture srcTexture, int srcLayout, Texture dstTexture, int dstLayout, VkImageCopy.Buffer copyRegion)
	{
		VK10.vkCmdCopyImage(vk, srcTexture.getVk(), srcLayout, dstTexture.getVk(), dstLayout, copyRegion);
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
	
	public void pipelineBarrier(int srcStageMask, int dstStageMask, int dependencyFlags, VkMemoryBarrier.@Nullable Buffer memoryBarriers, VkBufferMemoryBarrier.@Nullable Buffer bufferMemoryBarriers, VkImageMemoryBarrier.@Nullable Buffer imageMemoryBarriers)
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
		isRecording = false;
	}
	
	public boolean isRecording()
	{
		return isRecording;
	}
	
	public CommandPool getPool()
	{
		return pool;
	}
	
	@Override
	public void close()
	{
		Debug.debugClose(() -> "Closing CommandBuffer " + this);
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
