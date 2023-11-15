package com.alcadrial.renderengine.render;

import java.util.List;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkClearAttachment;
import org.lwjgl.vulkan.VkClearRect;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;

import com.alcadrial.renderengine.core.util.NotYetImplementedException;
import com.alcadrial.renderengine.render.buffer.IndexBuffer;
import com.alcadrial.renderengine.render.buffer.VertexBuffer;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.pipeline.Pipeline;
import com.alcadrial.renderengine.render.util.DeviceResource;
import com.alcadrial.renderengine.render.util.VulkanUtils;

public class CommandBufferData extends DeviceResource<VkCommandBuffer> {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private final long imageAvailable;
	private final long renderFinished;
	private final long fence;
	private boolean isRecording;
	
	public CommandBufferData(Device device, long commandBuffer, long imageAvailable, long renderFinished, long fence)
	{
		super(device);
		vk = new VkCommandBuffer(commandBuffer, device.getVk());
		this.imageAvailable = imageAvailable;
		this.renderFinished = renderFinished;
		this.fence = fence;
		isRecording = false;
	}
	
	public boolean startRecording()
	{
		if (isRecording) return false;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc(stack);
			beginInfo.sType(VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
			int error = VK10.vkBeginCommandBuffer(vk, beginInfo);
			if (error < 0) return false;
		}
		isRecording = true;
		return true;
	}
	
	public void bindPipeline(Pipeline pipeline)
	{
		VK10.vkCmdBindPipeline(vk, pipeline.bindPoint(), pipeline.getVk());
	}
	
	public void bindDescriptorSets(Pipeline pipeline, long[] descriptorSets, int[] dynamicOffsets)
	{
		VK10.vkCmdBindDescriptorSets(vk, pipeline.bindPoint(), pipeline.getLayout().getVk(), 0, descriptorSets, dynamicOffsets);
	}
	
	public void bindIndexBuffer(IndexBuffer buffer, long offset)
	{
		VK10.vkCmdBindIndexBuffer(vk, buffer.getGpuVk(), offset, buffer.getIndexType());
	}
	
	public void bindVertexBuffers(int bindingOffset, VertexBuffer... buffers)
	{
		bindVertexBuffers(bindingOffset, List.of(buffers));
	}
	
	public void bindVertexBuffers(int bindingOffset, List<VertexBuffer> buffers)
	{
		VK10.vkCmdBindVertexBuffers(vk, bindingOffset, buffers.stream().mapToLong(VertexBuffer::getGpuVk).toArray(), buffers.stream().mapToLong(VertexBuffer::getOffset).toArray());
	}
	
	/**
	 * NYI
	 */
	public void blitImage()
	{
		//VK10.vkCmdBlitImage(vk);
		throw new NotYetImplementedException();
	}
	
	public void clear(List<VkClearAttachment> clearAttachments, List<VkClearRect> clearRects)
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
		//VK10.vkCmdClearColorImage(vk);
		throw new NotYetImplementedException();
	}
	
	/**
	 * NYI
	 */
	public void clearDepthStencilImage()
	{
		//VK10.vkCmdClearDepthStencilImage(vk);
		throw new NotYetImplementedException();
	}
	
	//copy
	
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
	
	public void executeCommands(List<CommandBufferData> commands)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VK10.vkCmdExecuteCommands(vk, VulkanUtils.toAddressBuffer(stack, commands));
		}
	}
	
	public void fillBuffer(VulkanBuffer buffer, long offset, long size, int value)
	{
		VK10.vkCmdFillBuffer(vk, buffer.getVk(), offset, size, value);
	}
	
	/**
	 * 
	 * @param contents either {@link VK10#VK_SUBPASS_CONTENTS_INLINE} or {@link VK10#VK_SUBPASS_CONTENTS_SECONDARY_COMMAND_BUFFERS}
	 */
	public void nextSubpass(int contents)
	{
		VK10.vkCmdNextSubpass(vk, contents);
	}
	
	/**
	 * NYI
	 */
	public void pipelineBarrier(int contents)
	{
		//VK10.vkCmdPipelineBarrier(vk, srcStageMask, dstStageMask, flags, memoryBarriers, bufferMemoryBarriers, imageMemoryBarriers);
		throw new NotYetImplementedException();
	}
	
	//pushConstants
	
	/**
	 * NYI
	 */
	public void resetEvent()
	{
		//VK10.vkCmdResetEvent(vk);
		throw new NotYetImplementedException();
	}
	
	/**
	 * NYI
	 */
	public void resetQueryPool()
	{
		//VK10.vkCmdResetQueryPool(vk);
		throw new NotYetImplementedException();
	}
	
	/**
	 * NYI
	 */
	public void resolveImage()
	{
		//VK10.vkCmdResolveImage(vk);
		throw new NotYetImplementedException();
	}
	
	//setters
	//updateBuffer
	
	/**
	 * NYI
	 */
	public void waitEvent()
	{
		//VK10.vkCmdWaitEvent(vk);
		throw new NotYetImplementedException();
	}
	
	/**
	 * NYI
	 */
	public void writeTimestamp()
	{
		//VK10.vkCmdWriteTimestamp(vk);
		throw new NotYetImplementedException();
	}
	
	/**
	 * NYI
	 */
	public void setViewport()
	{
		//VK10.vkCmdSetViewport(vk);
		throw new NotYetImplementedException();
	}
	
	public boolean endRecording()
	{
		if (!isRecording) return false;
		VK10.vkEndCommandBuffer(vk);
		isRecording = false;
		return true;
	}
	
	public void resetFence()
	{
		VK10.vkResetFences(device.getVk(), fence);
	}
	
	public void reset()
	{
		VK10.vkResetCommandBuffer(vk, 0);
	}
	
	public long getImageAvailableSemaphore()
	{
		return imageAvailable;
	}
	
	public long getRenderFinishedSemaphore()
	{
		return renderFinished;
	}
	
	public long getFence()
	{
		return fence;
	}
	
	public boolean isRecording()
	{
		return isRecording;
	}
	
	@Override
	public void close()
	{
		VK10.vkDestroySemaphore(device.getVk(), imageAvailable, null);
		VK10.vkDestroySemaphore(device.getVk(), renderFinished, null);
		VK10.vkDestroyFence(device.getVk(), fence, null);
		super.close();
	}
}
