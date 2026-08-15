package com.alcadrial.kirry.render.commandbuffer;

import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkBufferImageCopy;
import org.lwjgl.vulkan.VkBufferMemoryBarrier;
import org.lwjgl.vulkan.VkBufferMemoryBarrier2;
import org.lwjgl.vulkan.VkClearAttachment;
import org.lwjgl.vulkan.VkClearColorValue;
import org.lwjgl.vulkan.VkClearRect;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkDependencyInfo;
import org.lwjgl.vulkan.VkImageBlit;
import org.lwjgl.vulkan.VkImageCopy;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkImageMemoryBarrier2;
import org.lwjgl.vulkan.VkImageSubresourceRange;
import org.lwjgl.vulkan.VkMemoryBarrier;
import org.lwjgl.vulkan.VkMemoryBarrier2;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkViewport;

import com.alcadrial.kector.vector.Vector3ir;
import com.alcadrial.kirry.core.util.NotYetImplementedException;
import com.alcadrial.kirry.core.util.color.Color;
import com.alcadrial.kirry.render.Debug;
import com.alcadrial.kirry.render.Device;
import com.alcadrial.kirry.render.KirryVulkan;
import com.alcadrial.kirry.render.buffer.VulkanBuffer;
import com.alcadrial.kirry.render.pipeline.Pipeline;
import com.alcadrial.kirry.render.texture.Texture;
import com.alcadrial.kirry.render.util.AbstractDeviceResource;
import com.alcadrial.kirry.render.util.VulkanErrorUtils;
import com.alcadrial.kirry.render.util.VulkanUtils;
import com.alcadrial.kirry.render.util.exception.VulkanException;
import com.alcadrial.kirry.render.util.functional.VulkanRunnable;

public class CommandBuffer extends AbstractDeviceResource<VkCommandBuffer> {
	
	static
	{
		KirryVulkan.assertLoaded();
	}
	
	private boolean isRecording;
	private CommandPool pool;
	private MutableList<VulkanRunnable> completionActions;
	
	public CommandBuffer(Device device, CommandPool pool, VkCommandBuffer commandBuffer)
	{
		super(device);
		vk = commandBuffer;
		isRecording = false;
		this.pool = pool;
		completionActions = Lists.mutable.of();
		Debug.debugCreate(() -> "Created " + this + " of " + pool);
	}
	
	@Override
	public void setName(String name)
	{
		device.setObjectDebugName(this, name);
		this.name = name;
	}
	
	public boolean beginUniqueRecording()
	{
		if (isRecording) return false;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.malloc(stack)
				.sType$Default()
				.pNext(0)
				.flags(VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)
				.pInheritanceInfo(null);
			if (VK10.vkBeginCommandBuffer(vk, beginInfo) < 0) return false;
		}
		isRecording = true;
		return true;
	}
	
	public boolean beginRecording()
	{
		if (isRecording) return false;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.malloc(stack)
				.sType$Default()
				.pNext(0)
				.flags(0)
				.pInheritanceInfo(null);
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
	
	public void bindVertexBuffers(int bindingOffset, VulkanBuffer buffer)
	{
		bindVertexBuffers(bindingOffset, buffer.getVk());
	}
	
	public void bindVertexBuffers(int bindingOffset, VulkanBuffer buffer, long offset)
	{
		bindVertexBuffers(bindingOffset, new long[] {buffer.getVk()}, offset);
	}
	
	public void bindVertexBuffers(int bindingOffset, VulkanBuffer... buffers)
	{
		bindVertexBuffers(bindingOffset, List.of(buffers));
	}
	
	public void bindVertexBuffers(int bindingOffset, VulkanBuffer[] buffers, long... offsets)
	{
		bindVertexBuffers(bindingOffset, List.of(buffers), offsets);
	}
	
	public void bindVertexBuffers(int bindingOffset, List<VulkanBuffer> buffers)
	{
		bindVertexBuffers(bindingOffset, buffers.stream().mapToLong(VulkanBuffer::getVk).toArray());
	}
	
	public void bindVertexBuffers(int bindingOffset, List<VulkanBuffer> buffers, long... offsets)
	{
		bindVertexBuffers(bindingOffset, buffers.stream().mapToLong(VulkanBuffer::getVk).toArray(), offsets);
	}
	
	private void bindVertexBuffers(int bindingOffset, long... buffers)
	{
		VK10.vkCmdBindVertexBuffers(vk, bindingOffset, buffers, new long[buffers.length]);
	}
	
	private void bindVertexBuffers(int bindingOffset, long[] buffers, long... offsets)
	{
		VK10.vkCmdBindVertexBuffers(vk, bindingOffset, buffers, offsets);
	}
	
	public void blitImage(Texture src, int srcLayout, Texture dst, int dstLayout, VkImageBlit.Buffer regions, int filter)
	{
		VK10.vkCmdBlitImage(vk, src.getVk(), srcLayout, dst.getVk(), dstLayout, regions, filter);
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
	
	public void copyBufferToImage(VulkanBuffer srcBuffer, long srcOffset, Texture dstTexture, Vector3ir imageOffset, Vector3ir imageExtent)
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
	
	public void pipelineBarrierTexture(int srcStageMask, int dstStageMask, int srcAccessMask, int dstAccessMask, Texture texture)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VK10.vkCmdPipelineBarrier(vk, srcStageMask, dstStageMask, 0, null, null, VkImageMemoryBarrier.malloc(1, stack)
				.sType$Default()
				.pNext(0)
				.srcAccessMask(srcAccessMask)
				.dstAccessMask(dstAccessMask)
				.oldLayout(0)
				.newLayout(0)
				.srcQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED)
				.dstQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED)
				.image(texture.getVk())
				.subresourceRange(s -> s
					.aspectMask(texture.getAspectMask())
					.baseMipLevel(0)
					.levelCount(texture.getMipLevels())
					.baseArrayLayer(0)
					.layerCount(1))
				);
		}
	}
	
	public void pipelineBarrier(int srcStageMask, int dstStageMask, int dependencyFlags, VkMemoryBarrier.@Nullable Buffer memoryBarriers, VkBufferMemoryBarrier.@Nullable Buffer bufferMemoryBarriers, VkImageMemoryBarrier.@Nullable Buffer imageMemoryBarriers)
	{
		VK10.vkCmdPipelineBarrier(vk, srcStageMask, dstStageMask, dependencyFlags, memoryBarriers, bufferMemoryBarriers, imageMemoryBarriers);
	}
	
	public void pipelineBarrier(int dependencyFlags, VkMemoryBarrier2.@Nullable Buffer memoryBarriers, VkImageMemoryBarrier2.@Nullable Buffer imageBarriers, VkBufferMemoryBarrier2.@Nullable Buffer bufferBarriers)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VK13.vkCmdPipelineBarrier2(vk, VkDependencyInfo.malloc(stack)
				.sType(VK13.VK_STRUCTURE_TYPE_DEPENDENCY_INFO)
				.pNext(0)
				.dependencyFlags(dependencyFlags)
				.pMemoryBarriers(memoryBarriers)
				.pImageMemoryBarriers(imageBarriers)
				.pBufferMemoryBarriers(bufferBarriers)
			);
		}
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
	
	public void addCompletionAction(VulkanRunnable action)
	{
		completionActions.add(action);
	}
	
	public MutableList<VulkanRunnable> getCompletionActions()
	{
		return completionActions;
	}
	
	public void reset()
	{
		VK10.vkResetCommandBuffer(vk, 0);
		completionActions.clear();
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
		completionActions.clear();
		super.close();
	}
	
	@Override
	public void debug()
	{
		Debug.print("vk", vk);
		Debug.print("pool", pool);
		Debug.print("actions", completionActions);
	}
}
