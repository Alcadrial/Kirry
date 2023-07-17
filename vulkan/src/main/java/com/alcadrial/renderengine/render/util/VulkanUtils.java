package com.alcadrial.renderengine.render.util;

import java.nio.ByteBuffer;
import java.util.function.Function;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Struct;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkViewport;

public class VulkanUtils {
	
	public static <S extends Struct> Pair<S, ByteBuffer> copy(S struct, Function<Long, S> builder)
	{
		ByteBuffer buffer = MemoryUtil.memByteBuffer(struct.address(), struct.sizeof());
		ByteBuffer copyBuffer = MemoryUtil.memCalloc(struct.sizeof());
		S copyStruct = builder.apply(MemoryUtil.memAddress(copyBuffer));
		copyBuffer.put(buffer);
		return new Pair<>(copyStruct, copyBuffer);
	}
	
	public static <S extends Struct> Pair<S, ByteBuffer> allocate(int size, Function<Long, S> builder)
	{
		ByteBuffer buffer = MemoryUtil.memCalloc(size);
		return new Pair<>(builder.apply(MemoryUtil.memAddress(buffer)), buffer);
	}
	
	public static ByteBuffer allocate(MemoryStack stack, byte[] data)
	{
		return stack.malloc(data.length).put(data).rewind();
	}
	
	public static VkOffset2D offset(MemoryStack stack, int x, int y)
	{
		return VkOffset2D.calloc(stack).set(x, y);
	}
	
	public static VkExtent2D extent(MemoryStack stack, int width, int height)
	{
		return VkExtent2D.calloc(stack).set(width, height);
	}
	
	public static VkBufferCopy.Buffer bufferCopy(MemoryStack stack, long sourceOffset, long destinationOffset, long size)
	{
		return VkBufferCopy.calloc(1, stack).apply(0, v -> v.set(sourceOffset, destinationOffset, size));
	}
	
	public static VkRect2D.Buffer rect2d(MemoryStack stack, VkOffset2D offset, VkExtent2D extent)
	{
		return VkRect2D.calloc(1, stack).apply(0, v -> v.set(offset, extent));
	}
	
	public static VkViewport.Buffer viewport(MemoryStack stack, float x, float y, float width, float height, float minDepth, float maxDepth)
	{
		return VkViewport.calloc(1, stack).apply(0, v -> v.set(x, y, width, height, minDepth, maxDepth));
	}
	
	public static Pair<VkPipelineInputAssemblyStateCreateInfo, ByteBuffer> pipelineInputAssemblyStateInfo(int topology, boolean primitiveRestartEnable)
	{
		Pair<VkPipelineInputAssemblyStateCreateInfo, ByteBuffer> createInfo = allocate(1, VkPipelineInputAssemblyStateCreateInfo::create);
		createInfo.getLeft().set(VK10.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO, 0, 0, topology, primitiveRestartEnable);
		return createInfo;
	}
	
	public static VkPipelineInputAssemblyStateCreateInfo pipelineInputAssemblyStateInfo(MemoryStack stack, int topology, boolean primitiveRestartEnable)
	{
		return VkPipelineInputAssemblyStateCreateInfo.calloc(stack).set(VK10.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO, 0, 0, topology, primitiveRestartEnable);
	}
	
	public static VkPipelineViewportStateCreateInfo pipelineViewportStateInfo(MemoryStack stack, VkViewport.Buffer viewports, VkRect2D.Buffer scissors)
	{
		return VkPipelineViewportStateCreateInfo.calloc(stack).set(VK10.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO, 0, 0, viewports.remaining(), viewports, scissors.remaining(), scissors);
	}
	
	public static VkPipelineRasterizationStateCreateInfo pipelineRasterizationStateInfo(MemoryStack stack, int polygonMode, int cullMode, int frontFace)
	{
		return VkPipelineRasterizationStateCreateInfo.calloc(stack).sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO).polygonMode(polygonMode).cullMode(cullMode).frontFace(frontFace);
	}
	
	public static VkPipelineMultisampleStateCreateInfo pipelineMultisampleStateInfo(MemoryStack stack, int rasterizationSamples)
	{
		return VkPipelineMultisampleStateCreateInfo.calloc(stack).sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO).rasterizationSamples(rasterizationSamples);
	}
	
	public static VkCommandBufferAllocateInfo commandBufferAllocateInfo(MemoryStack stack, long commandPool, int level, int count)
	{
		return VkCommandBufferAllocateInfo.calloc(stack).sType(VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO).commandPool(commandPool).level(level).commandBufferCount(count);
	}
	
	public static VkCommandBufferBeginInfo commandBufferBeginInfo(MemoryStack stack, int flags)
	{
		return VkCommandBufferBeginInfo.calloc(stack).sType(VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO).flags(flags);
	}
	
	public static VkSubmitInfo submitInfo(MemoryStack stack, VkCommandBuffer... commandBuffer)
	{
		return VkSubmitInfo.calloc(stack).sType(VK10.VK_STRUCTURE_TYPE_SUBMIT_INFO).pCommandBuffers(stack.pointers(commandBuffer));
	}
	
	public static VkMemoryAllocateInfo memoryAllocateInfo(MemoryStack stack, long allocationSize, int memoryTypeIndex)
	{
		return VkMemoryAllocateInfo.calloc(stack).sType(VK10.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO).allocationSize(allocationSize).memoryTypeIndex(memoryTypeIndex);
	}
	
	public static VkBufferCreateInfo bufferCreateInfo(MemoryStack stack, int flags, long size, int usage)
	{
		return VkBufferCreateInfo.calloc(stack).sType(VK10.VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO).flags(flags).size(size).usage(usage);
	}
	
	public static VkShaderModuleCreateInfo shaderModuleCreateInfo(MemoryStack stack, byte[] binary)
	{
		return VkShaderModuleCreateInfo.calloc(stack).sType(VK10.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO).pCode(allocate(stack, binary));
	}
}
