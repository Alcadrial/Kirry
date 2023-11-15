package com.alcadrial.renderengine.render.util;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Pointer;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
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
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import com.alcadrial.renderengine.core.util.Pair;
import com.alcadrial.renderengine.render.RenderEngine;

public class VulkanUtils {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public static <S extends Struct<S>> Pair<S, ByteBuffer> copy(S struct, Function<Long, S> builder)
	{
		ByteBuffer buffer = MemoryUtil.memByteBuffer(struct.address(), struct.sizeof());
		ByteBuffer copyBuffer = MemoryUtil.memAlloc(struct.sizeof());
		S copyStruct = builder.apply(MemoryUtil.memAddress(copyBuffer));
		copyBuffer.put(buffer);
		return new Pair<>(copyStruct, copyBuffer);
	}
	
	public static <S extends Struct<S>> Pair<S, ByteBuffer> allocate(int size, Function<Long, S> builder)
	{
		ByteBuffer buffer = MemoryUtil.memAlloc(size);
		return new Pair<>(builder.apply(MemoryUtil.memAddress(buffer)), buffer);
	}
	
	public static <S extends Struct<S>> Pair<S, ByteBuffer> clearAllocate(int size, Function<Long, S> builder)
	{
		ByteBuffer buffer = MemoryUtil.memCalloc(size);
		return new Pair<>(builder.apply(MemoryUtil.memAddress(buffer)), buffer);
	}
	
	public static ByteBuffer allocate(MemoryStack stack, byte[] data)
	{
		return stack.malloc(data.length).put(data).rewind();
	}
	
	public static <S extends Struct<S>, B extends StructBuffer<S, B>> B toBuffer(List<S> structs, Function<Integer, B> builder)
	{
		B buffer = builder.apply(structs.size());
		for (S s : structs) buffer.put(s);
		return buffer.rewind();
	}
	
	public static <S extends Struct<S>, B extends StructBuffer<S, B>> B toBuffer(MemoryStack stack, List<S> structs, BiFunction<Integer, MemoryStack, B> builder)
	{
		B buffer = builder.apply(structs.size(), stack);
		for (S s : structs) buffer.put(s);
		return buffer.rewind();
	}
	
	@SafeVarargs
	public static <VR extends VulkanResource<Long>> PointerBuffer toPointerBuffer(MemoryStack stack, VR... resources)
	{
		return toPointerBuffer(stack, List.of(resources));
	}
	
	public static <VR extends VulkanResource<Long>> PointerBuffer toPointerBuffer(MemoryStack stack, List<VR> resources)
	{
		PointerBuffer buffer = stack.mallocPointer(resources.size());
		for (VR vr : resources) buffer.put(vr.getVk());
		return buffer.rewind();
	}
	
	@SafeVarargs
	public static <VR extends VulkanResource<VK>, VK extends Pointer> PointerBuffer toAddressBuffer(MemoryStack stack, VR... resources)
	{
		return toAddressBuffer(stack, List.of(resources));
	}
	
	public static <VR extends VulkanResource<VK>, VK extends Pointer> PointerBuffer toAddressBuffer(MemoryStack stack, List<VR> resources)
	{
		PointerBuffer buffer = stack.mallocPointer(resources.size());
		for (VR vr : resources) buffer.put(vr.vk.address());
		return buffer.rewind();
	}
	
	/**
	 * 
	 * @param stack the stack from which to allocate
	 * @param x the x offset
	 * @param y the y offset
	 * @return a new {@link VkOffset2D} in the stack
	 */
	public static VkOffset2D offset(MemoryStack stack, int x, int y)
	{
		return VkOffset2D.malloc(stack).set(x, y);
	}
	
	/**
	 * 
	 * @param stack the stack from which to allocate
	 * @param width the width of the extent
	 * @param height the height of the extent
	 * @return a new {@link VkExtent2D} in the stack
	 */
	public static VkExtent2D extent(MemoryStack stack, int width, int height)
	{
		return VkExtent2D.malloc(stack).set(width, height);
	}
	
	/**
	 * 
	 * @param stack the stack from which to allocate
	 * @param sourceOffset the starting offset in bytes from the start of srcBuffer
	 * @param destinationOffset the starting offset in bytes from the start of dstBuffer
	 * @param size the number of bytes to copy
	 * @return a new buffer of {@link VkBufferCopy}s in the stack
	 */
	public static VkBufferCopy.Buffer bufferCopy(MemoryStack stack, long sourceOffset, long destinationOffset, long size)
	{
		return VkBufferCopy.malloc(1, stack).apply(0, v -> v.set(sourceOffset, destinationOffset, size));
	}
	
	/**
	 * 
	 * @param stack the stack from which to allocate
	 * @param offset a VkOffset2D specifying the rectangle offset
	 * @param extent a VkExtent2D specifying the rectangle extent
	 * @return a new buffer of {@link VkRect2D}s in the stack
	 */
	public static VkRect2D.Buffer rect2d(MemoryStack stack, VkOffset2D offset, VkExtent2D extent)
	{
		return VkRect2D.malloc(1, stack).apply(0, v -> v.set(offset, extent));
	}
	
	/**
	 * 
	 * @param stack the stack from which to allocate
	 * @param x x and y are the viewport’s upper left corner (x,y)
	 * @param y see x
	 * @param width width and height are the viewport’s width and height, respectively
	 * @param height see width
	 * @param minDepth minDepth and maxDepth are the depth range for the viewport
	 * @param maxDepth see minDepth 
	 * @return a new buffer of {@link VkViewport}s in the stack
	 */
	public static VkViewport.Buffer viewport(MemoryStack stack, float x, float y, float width, float height, float minDepth, float maxDepth)
	{
		return VkViewport.malloc(1, stack).apply(0, v -> v.set(x, y, width, height, minDepth, maxDepth));
	}
	
	/**
	 * 
	 * @param stack the stack from which to allocate
	 * @param descriptorType a VkDescriptorType specifying which type of resource descriptors are used for this binding
	 * @return a new buffer of {@link VkDescriptorSetLayoutBinding}s in the stack
	 */
	public static VkDescriptorSetLayoutBinding.Buffer descriptorSetLayoutBinding(MemoryStack stack, int descriptorType)
	{
		return VkDescriptorSetLayoutBinding.malloc(1, stack).apply(0, v -> v.set(0, descriptorType, 0, 0, null));
	}
	
	/**
	 * 
	 * @param stack the stack from which to allocate
	 * @param stackSize the number of elements to allocate
	 * @param descriptorType a VkDescriptorType specifying which type of resource descriptors are used for this binding
	 * @return a new buffer of {@link VkDescriptorSetLayoutBinding}s in the stack
	 */
	public static VkDescriptorSetLayoutBinding.Buffer descriptorSetLayoutBinding(MemoryStack stack, int stackSize, int descriptorType)
	{
		VkDescriptorSetLayoutBinding.Buffer buffer = VkDescriptorSetLayoutBinding.malloc(stackSize, stack);
		for (int i = 0; i < stackSize; i++) buffer.get().set(0, descriptorType, 0, 0, null);
		return buffer.rewind();
	}
	
	/**
	 * 
	 * @param stack the stack from which to allocate
	 * @param type the type of descriptor
	 * @param count the number of descriptors of that type to allocate. If type is DESCRIPTOR_TYPE_INLINE_UNIFORM_BLOCK then descriptorCount 
	 * 		is the number of bytes to allocate for descriptors of this type
	 * @return a new buffer of {@link VkDescriptorPoolSize}s in the stack
	 */
	public static VkDescriptorPoolSize.Buffer descriptorPoolSize(MemoryStack stack, int type, int count)
	{
		return VkDescriptorPoolSize.calloc(1, stack).type(type).descriptorCount(count);
	}
	
	/**
	 * 
	 * @param stack the stack from which to allocate
	 * @param type a VkDescriptorType specifying the type of each descriptor in pImageInfo, pBufferInfo, or pTexelBufferView, as described below. 
	 * 		If VkDescriptorSetLayoutBinding for dstSet at dstBinding is not equal to DESCRIPTOR_TYPE_MUTABLE_VALVE, descriptorType must be the 
	 * 		same type as the descriptorType specified in VkDescriptorSetLayoutBinding for dstSet at dstBinding. The type of the descriptor also 
	 * 		controls which array the descriptors are taken from
	 * @param count the number of descriptors to update. If the descriptor binding identified by dstSet and dstBinding has a descriptor type of 
	 * 		DESCRIPTOR_TYPE_INLINE_UNIFORM_BLOCK, then descriptorCount specifies the number of bytes to update. Otherwise, descriptorCount is one of
	 * 		<ul>
	 * 			<li>the number of elements in pImageInfo</li>
	 * 			<li>the number of elements in pBufferInfo</li>
	 * 			<li>the number of elements in pTexelBufferView</li>
	 * 			<li>a value matching the dataSize member of a VkWriteDescriptorSetInlineUniformBlock structure in the pNext chain</li>
	 * 			<li>a value matching the accelerationStructureCount of a VkWriteDescriptorSetAccelerationStructureKHR structure in the pNext chain</li>
	 * 		</ul>
	 * @return a new buffer of {@link VkWriteDescriptorSet}s in the stack
	 */
	public static VkWriteDescriptorSet.Buffer writeDescriptorSet(MemoryStack stack, int type, int count)
	{
		return VkWriteDescriptorSet.calloc(1, stack).sType(VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET).descriptorType(type).descriptorCount(count);
	}
	
	/**
	 * 
	 * @param stack the stack from which to allocate
	 * @param stackSize the number of elements to allocate
	 * @param type a VkDescriptorType specifying the type of each descriptor in pImageInfo, pBufferInfo, or pTexelBufferView, as described below. 
	 * 		If VkDescriptorSetLayoutBinding for dstSet at dstBinding is not equal to DESCRIPTOR_TYPE_MUTABLE_VALVE, descriptorType must be the 
	 * 		same type as the descriptorType specified in VkDescriptorSetLayoutBinding for dstSet at dstBinding. The type of the descriptor also 
	 * 		controls which array the descriptors are taken from
	 * @param count the number of descriptors to update. If the descriptor binding identified by dstSet and dstBinding has a descriptor type of 
	 * 		DESCRIPTOR_TYPE_INLINE_UNIFORM_BLOCK, then descriptorCount specifies the number of bytes to update. Otherwise, descriptorCount is one of
	 * 		<ul>
	 * 			<li>the number of elements in pImageInfo</li>
	 * 			<li>the number of elements in pBufferInfo</li>
	 * 			<li>the number of elements in pTexelBufferView</li>
	 * 			<li>a value matching the dataSize member of a VkWriteDescriptorSetInlineUniformBlock structure in the pNext chain</li>
	 * 			<li>a value matching the accelerationStructureCount of a VkWriteDescriptorSetAccelerationStructureKHR structure in the pNext chain</li>
	 * 		</ul>
	 * @return a new buffer of {@link VkWriteDescriptorSet}s in the stack
	 */
	public static VkWriteDescriptorSet.Buffer writeDescriptorSet(MemoryStack stack, int stackSize, int type, int count)
	{
		VkWriteDescriptorSet.Buffer buffer = VkWriteDescriptorSet.calloc(stackSize, stack);
		for (int i = 0; i < stackSize; i++) buffer.get().sType(VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET).descriptorType(type).descriptorCount(count);
		return buffer.rewind();
	}
	
	/**
	 * 
	 * @param stackSize
	 * @param type
	 * @param count
	 * @return a new buffer of {@link VkWriteDescriptorSet}s in the stack
	 * @see VulkanUtils#writeDescriptorSet(MemoryStack, int, int, int)
	 */
	public static VkWriteDescriptorSet.Buffer writeDescriptorSet(int stackSize, int type, int count)
	{
		VkWriteDescriptorSet.Buffer buffer = VkWriteDescriptorSet.calloc(stackSize);
		for (int i = 0; i < stackSize; i++) buffer.get().sType(VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET).descriptorType(type).descriptorCount(count);
		return buffer.rewind();
	}
	
	/**
	 * 
	 * @param stack the stack from which to allocate
	 * @param topology a VkPrimitiveTopology defining the primitive topology, as described below
	 * @param primitiveRestartEnable controls whether a special vertex index value is treated as restarting the assembly of primitives.
	 * 		This enable only applies to indexed draws (CmdDrawIndexed, CmdDrawMultiIndexedEXT, and CmdDrawIndexedIndirect), and the special 
	 * 		index value is either 0xFFFFFFFF when the indexType parameter of vkCmdBindIndexBuffer is equal to INDEX_TYPE_UINT32, 0xFF when 
	 * 		indexType is equal to INDEX_TYPE_UINT8_EXT, or 0xFFFF when indexType is equal to INDEX_TYPE_UINT16. Primitive restart is not 
	 * 		allowed for "list" topologies, unless one of the features primitiveTopologyPatchListRestart (for PRIMITIVE_TOPOLOGY_PATCH_LIST) 
	 * 		or primitiveTopologyListRestart (for all other list topologies) is enabled
	 * @return a new {@link VkPipelineInputAssemblyStateCreateInfo} in the stack
	 */
	public static VkPipelineInputAssemblyStateCreateInfo pipelineInputAssemblyStateInfo(MemoryStack stack, int topology, boolean primitiveRestartEnable)
	{
		return VkPipelineInputAssemblyStateCreateInfo.malloc(stack).set(VK10.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO, 0, 0, topology, primitiveRestartEnable);
	}
	
	/**
	 * 
	 * @param stack the stack from which to allocate
	 * @param viewports a pointer to an array of VkViewport structures, defining the viewport transforms. If the viewport state is dynamic, this member is ignored
	 * @param scissors a pointer to an array of VkRect2D structures defining the rectangular bounds of the scissor for the corresponding viewport. If the scissor state is dynamic, this member is ignored
	 * @return a new {@link VkPipelineViewportStateCreateInfo} in the stack
	 */
	public static VkPipelineViewportStateCreateInfo pipelineViewportStateInfo(MemoryStack stack, VkViewport.Buffer viewports, VkRect2D.Buffer scissors)
	{
		return VkPipelineViewportStateCreateInfo.malloc(stack).set(VK10.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO, 0, 0, viewports.remaining(), viewports, scissors.remaining(), scissors);
	}
	
	/**
	 * 
	 * @param stack the stack from which to allocate
	 * @param polygonMode the triangle rendering mode. See VkPolygonMode
	 * @param cullMode the triangle facing direction used for primitive culling. See VkCullModeFlagBits
	 * @param frontFace a VkFrontFace value specifying the front-facing triangle orientation to be used for culling
	 * @return a new {@link VkPipelineRasterizationStateCreateInfo} in the stack
	 */
	public static VkPipelineRasterizationStateCreateInfo pipelineRasterizationStateInfo(MemoryStack stack, int polygonMode, int cullMode, int frontFace)
	{
		return VkPipelineRasterizationStateCreateInfo.calloc(stack).sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO).polygonMode(polygonMode).cullMode(cullMode).frontFace(frontFace);
	}
	
	/**
	 * 
	 * @param stack the stack from which to allocate
	 * @param rasterizationSamples a VkSampleCountFlagBits value specifying the number of samples used in rasterization
	 * @return a new {@link VkPipelineMultisampleStateCreateInfo} in the stack
	 */
	public static VkPipelineMultisampleStateCreateInfo pipelineMultisampleStateInfo(MemoryStack stack, int rasterizationSamples)
	{
		return VkPipelineMultisampleStateCreateInfo.calloc(stack).sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO).rasterizationSamples(rasterizationSamples);
	}
	
	/**
	 * 
	 * @param stack the stack from which to allocate
	 * @param commandPool the command pool from which the command buffers are allocated
	 * @param level a VkCommandBufferLevel value specifying the command buffer level
	 * @param count the number of command buffers to allocate from the pool
	 * @return a new {@link VkCommandBufferAllocateInfo} in the stack
	 */
	public static VkCommandBufferAllocateInfo commandBufferAllocateInfo(MemoryStack stack, long commandPool, int level, int count)
	{
		return VkCommandBufferAllocateInfo.calloc(stack).sType(VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO).commandPool(commandPool).level(level).commandBufferCount(count);
	}
	
	/**
	 * 
	 * @param stack the stack from which to allocate
	 * @param flags a bitmask of VkCommandBufferUsageFlagBits specifying usage behavior for the command buffer
	 * @return a new {@link VkCommandBufferBeginInfo} in the stack
	 */
	public static VkCommandBufferBeginInfo commandBufferBeginInfo(MemoryStack stack, int flags)
	{
		return VkCommandBufferBeginInfo.calloc(stack).sType(VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO).flags(flags);
	}
	
	/**
	 * 
	 * @param stack the stack from which to allocate
	 * @param commandBuffer a pointer to an array of VkCommandBuffer handles to execute in the batch
	 * @return a new {@link VkSubmitInfo} in the stack
	 */
	public static VkSubmitInfo submitInfo(MemoryStack stack, VkCommandBuffer... commandBuffer)
	{
		return VkSubmitInfo.calloc(stack).sType(VK10.VK_STRUCTURE_TYPE_SUBMIT_INFO).pCommandBuffers(stack.pointers(commandBuffer));
	}
	
	/**
	 * 
	 * @param stack the stack from which to allocate
	 * @param allocationSize the size of the allocation in bytes
	 * @param memoryTypeIndex an index identifying a memory type from the memoryTypes array of the VkPhysicalDeviceMemoryProperties structure
	 * @return a new {@link VkMemoryAllocateInfo} in the stack
	 */
	public static VkMemoryAllocateInfo memoryAllocateInfo(MemoryStack stack, long allocationSize, int memoryTypeIndex)
	{
		return VkMemoryAllocateInfo.calloc(stack).sType(VK10.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO).allocationSize(allocationSize).memoryTypeIndex(memoryTypeIndex);
	}
	
	/**
	 * 
	 * @param stack the stack from which to allocate
	 * @param size the size in bytes of the buffer to be created
	 * @param usage a bitmask of VkBufferUsageFlagBits specifying allowed usages of the buffer
	 * @return a new {@link VkBufferCreateInfo} in the stack
	 */
	public static VkBufferCreateInfo bufferInfo(MemoryStack stack, long size, int usage)
	{
		return VkBufferCreateInfo.calloc(stack).sType(VK10.VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO).size(size).usage(usage);
	}
	
	/**
	 * 
	 * @param stack the stack from which to allocate
	 * @param binary points to code that is used to create the shader module
	 * @return a new {@link VkShaderModuleCreateInfo} in the stack
	 */
	public static VkShaderModuleCreateInfo shaderModuleInfo(MemoryStack stack, byte[] binary)
	{
		return VkShaderModuleCreateInfo.calloc(stack).sType(VK10.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO).pCode(allocate(stack, binary));
	}
	
	/**
	 * 
	 * @param stack the stack from which to allocate
	 * @param flags a bitmask of VkDescriptorSetLayoutCreateFlagBits specifying options for descriptor set layout creation
	 * @param bindings a pointer to an array of VkDescriptorSetLayoutBinding structures
	 * @return a new {@link VkDescriptorSetLayoutCreateInfo} in the stack
	 */
	public static VkDescriptorSetLayoutCreateInfo descriptorSetLayoutInfo(MemoryStack stack, int flags, VkDescriptorSetLayoutBinding.Buffer bindings)
	{
		return VkDescriptorSetLayoutCreateInfo.calloc(stack).sType(VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO).flags(flags).pBindings(bindings);
	}
	
	/**
	 * 
	 * @param stack the stack from which to allocate
	 * @param poolSizes a pointer to an array of VkDescriptorPoolSize structures, each containing a descriptor type and number of descriptors of that type to be allocated in the pool
	 * @return a new {@link VkDescriptorPoolCreateInfo} in the stack
	 */
	public static VkDescriptorPoolCreateInfo descriptorPoolCreateInfo(MemoryStack stack, VkDescriptorPoolSize.Buffer poolSizes)
	{
		return VkDescriptorPoolCreateInfo.calloc(stack).sType(VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO).pPoolSizes(poolSizes);
	}
	
	/**
	 * 
	 * @param stack the stack from which to allocate
	 * @param descriptorPool the pool which the sets will be allocated from
	 * @param setLayouts a pointer to an array of descriptor set layouts, with each member specifying how the corresponding descriptor set is allocated
	 * @return a new {@link VkDescriptorSetAllocateInfo} in the stack
	 */
	public static VkDescriptorSetAllocateInfo descriptorSetAllocateInfo(MemoryStack stack, long descriptorPool, LongBuffer setLayouts)
	{
		return VkDescriptorSetAllocateInfo.calloc(stack).sType(VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO).descriptorPool(descriptorPool).pSetLayouts(setLayouts);
	}
	
	/**
	 * @param stack the stack from which to allocate
	 * @param buffer NULL_HANDLE or the buffer resource
	 * @param offset the offset in bytes from the start of buffer. Access to buffer memory via this descriptor uses addressing that is relative to this starting offset
	 * @param range the size in bytes that is used for this descriptor update, or WHOLE_SIZE to use the range from offset to the end of the buffer
	 * @return a new buffer of {@link VkDescriptorBufferInfo}s in the stack
	 */
	public static VkDescriptorBufferInfo.Buffer descriptorBufferInfo(MemoryStack stack, long buffer, long offset, long range)
	{
		return VkDescriptorBufferInfo.calloc(1, stack).buffer(buffer).offset(offset).range(range);
	}
}
