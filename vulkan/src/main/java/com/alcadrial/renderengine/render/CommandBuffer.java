package com.alcadrial.renderengine.render;

import java.util.List;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
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

import com.alcadrial.renderengine.core.util.NotYetImplementedException;
import com.alcadrial.renderengine.core.util.Nullable;
import com.alcadrial.renderengine.render.buffer.IndexBuffer;
import com.alcadrial.renderengine.render.buffer.VertexBuffer;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.pipeline.Pipeline;
import com.alcadrial.renderengine.render.util.AbstractDeviceResource;
import com.alcadrial.renderengine.render.util.VulkanUtils;

public class CommandBuffer extends AbstractDeviceResource<VkCommandBuffer> {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	private boolean isRecording;
	
	public CommandBuffer(Device device, VkCommandBuffer commandBuffer)
	{
		super(device);
		vk = commandBuffer;
		isRecording = false;
	}
	
	public synchronized boolean startRecording()
	{
		if (isRecording) return false;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc(stack);
			beginInfo.sType(VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
			if (VK10.vkBeginCommandBuffer(vk, beginInfo) < 0) return false;
		}
		isRecording = true;
		return true;
	}
	
	public synchronized void beginRenderPass(VkRenderPassBeginInfo renderPassBeginInfo, int subpassContents)
	{
		VK10.vkCmdBeginRenderPass(vk, renderPassBeginInfo, subpassContents);
	}
	
	public synchronized void bindPipeline(Pipeline pipeline)
	{
		VK10.vkCmdBindPipeline(vk, pipeline.bindPoint(), pipeline.getVk());
	}
	
	public synchronized void bindDescriptorSets(Pipeline pipeline, long[] descriptorSets, int[] dynamicOffsets)
	{
		VK10.vkCmdBindDescriptorSets(vk, pipeline.bindPoint(), pipeline.getLayout().getVk(), 0, descriptorSets, dynamicOffsets);
	}
	
	public synchronized void bindIndexBuffer(IndexBuffer buffer, long offset)
	{
		VK10.vkCmdBindIndexBuffer(vk, buffer.getGpuVk(), offset, buffer.getIndexType());
	}
	
	public synchronized void bindVertexBuffers(int bindingOffset, VertexBuffer... buffers)
	{
		bindVertexBuffers(bindingOffset, List.of(buffers));
	}
	
	public synchronized void bindVertexBuffers(int bindingOffset, List<VertexBuffer> buffers)
	{
		VK10.vkCmdBindVertexBuffers(vk, bindingOffset, buffers.stream().mapToLong(VertexBuffer::getGpuVk).toArray(), buffers.stream().mapToLong(VertexBuffer::getOffset).toArray());
	}
	
	/**
	 * NYI
	 */
	public void blitImage()
	{
		// VK10.vkCmdBlitImage(vk);
		throw new NotYetImplementedException();
	}
	
	public synchronized void clear(List<VkClearAttachment> clearAttachments, List<VkClearRect> clearRects)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VK10.vkCmdClearAttachments(vk, VulkanUtils.toBuffer(stack, clearAttachments, VkClearAttachment::calloc), VulkanUtils.toBuffer(stack, clearRects, VkClearRect::calloc));
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
	
	// copy
	
	public synchronized void dispatch(int groupCountX, int groupCountY, int groupCountZ)
	{
		VK10.vkCmdDispatch(vk, groupCountX, groupCountY, groupCountZ);
	}
	
	public synchronized void dispatchIndirect(VulkanBuffer buffer, long offset)
	{
		VK10.vkCmdDispatchIndirect(vk, buffer.getVk(), offset);
	}
	
	public synchronized void draw(int vertexCount, int instanceCount, int vertexOffset, int instanceOffset)
	{
		VK10.vkCmdDraw(vk, vertexCount, instanceCount, vertexOffset, instanceOffset);
	}
	
	public synchronized void drawIndexed(int indexCount, int instanceCount, int indexOffset, int vertexOffset, int instanceOffset)
	{
		VK10.vkCmdDrawIndexed(vk, indexCount, instanceCount, indexOffset, vertexOffset, instanceOffset);
	}
	
	public synchronized void drawIndirect(VulkanBuffer buffer, long offset, int drawCount, int stride)
	{
		VK10.vkCmdDrawIndirect(vk, buffer.getVk(), offset, drawCount, stride);
	}
	
	public synchronized void drawIndexedIndirect(VulkanBuffer buffer, long offset, int drawCount, int stride)
	{
		VK10.vkCmdDrawIndexedIndirect(vk, buffer.getVk(), offset, drawCount, stride);
	}
	
	public synchronized void endRenderPass()
	{
		VK10.vkCmdEndRenderPass(vk);
	}
	
	public synchronized void executeCommands(List<CommandBuffer> commands)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VK10.vkCmdExecuteCommands(vk, VulkanUtils.toAddressBuffer(stack, commands));
		}
	}
	
	public synchronized void fillBuffer(VulkanBuffer buffer, long offset, long size, int value)
	{
		VK10.vkCmdFillBuffer(vk, buffer.getVk(), offset, size, value);
	}
	
	/**
	 * 
	 * @param subpassContents either {@link VK10#VK_SUBPASS_CONTENTS_INLINE} or {@link VK10#VK_SUBPASS_CONTENTS_SECONDARY_COMMAND_BUFFERS}
	 */
	public synchronized void nextSubpass(int subpassContents)
	{
		VK10.vkCmdNextSubpass(vk, subpassContents);
	}
	
	public synchronized void pipelineBarrier(int srcStageMask, int dstStageMask, int dependencyFlags, @Nullable VkMemoryBarrier.Buffer memoryBarriers, @Nullable VkBufferMemoryBarrier.Buffer bufferMemoryBarriers, @Nullable VkImageMemoryBarrier.Buffer imageMemoryBarriers)
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
	
	public synchronized void setScissor(int x, int y, int width, int height)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VK10.vkCmdSetScissor(vk, 0, VulkanUtils.rect2d(stack, x, y, width, height));
		}
	}
	
	public synchronized void setScissor(VkRect2D.Buffer scissors)
	{
		VK10.vkCmdSetScissor(vk, 0, scissors);
	}
	
	public synchronized void setViewport(int x, int y, int width, int height, int minZ, int maxZ)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VK10.vkCmdSetViewport(vk, 0, VulkanUtils.viewport(stack, x, y, width, height, minZ, maxZ));
		}
	}
	
	public synchronized void setViewport(VkViewport.Buffer viewports)
	{
		VK10.vkCmdSetViewport(vk, 0, viewports);
	}
	
	public synchronized boolean endRecording()
	{
		if (!isRecording) return false;
		VK10.vkEndCommandBuffer(vk);
		isRecording = false;
		return true;
	}
	
	public synchronized void reset()
	{
		VK10.vkResetCommandBuffer(vk, 0);
	}
	
	public synchronized boolean isRecording()
	{
		return isRecording;
	}
	
	@Override
	public void debug()
	{
		Debug.print("vk: " + Debug.address(vk));
	}
}
