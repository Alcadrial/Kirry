package com.alcadrial.renderengine.render;

import java.nio.FloatBuffer;
import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;

import com.alcadrial.renderengine.render.util.VulkanErrorUtils;

public class VertexBuffer implements AutoCloseable {
	
	private long id;
	private long memory;
	
	public VertexBuffer()
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			float[] data0 = new float[] {
				-0.5F, 0.5F, 0, 0, 0, 1, 1,
				0.5F, 0.5F, 0, 0, 1, 0, 1,
				0.5F, -0.5F, 0, 1, 0, 0, 1,
				-0.5F, -0.5F, 0, 1, 1, 1, 1,
			};
			Device device = RenderEngine.getVulkan().getDevice();
			VkBufferCreateInfo bufferInfo = VkBufferCreateInfo.calloc(stack);
			bufferInfo.sType(VK10.VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO);
			bufferInfo.size(4 * data0.length);
			bufferInfo.usage(VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
			bufferInfo.sharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);
			
			long[] p = new long[1];
			
			VulkanErrorUtils.assertNoError(VK10.vkCreateBuffer(device.getDevice(), bufferInfo, null, p), "Failed to create vertex buffer");
			
			id = p[0];
			
			VkMemoryRequirements memRequirements = VkMemoryRequirements.malloc(stack);
			VK10.vkGetBufferMemoryRequirements(device.getDevice(), id, memRequirements);
			
			VkMemoryAllocateInfo allocInfo = VkMemoryAllocateInfo.calloc(stack);
			allocInfo.sType(VK10.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO);
			allocInfo.allocationSize(memRequirements.size());
			
			int index;
			int properties = VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
			VkPhysicalDeviceMemoryProperties memProperties = VkPhysicalDeviceMemoryProperties.malloc(stack);
			VK10.vkGetPhysicalDeviceMemoryProperties(RenderEngine.getVulkan().getPhysicalDevice().getDevice(), memProperties);
			
			System.out.println(Integer.toBinaryString(memRequirements.memoryTypeBits()));
			for (index = 0; index < memProperties.memoryTypeCount(); index++)
			{
				System.out.println(Integer.toBinaryString(memProperties.memoryTypes(index).propertyFlags()));
				if ((memRequirements.memoryTypeBits() & (1 << index)) != 0 && (memProperties.memoryTypes(index).propertyFlags() & properties) == properties)
				{
					break;
				}
			}
			//VulkanErrorUtils.assertStatement(index == memProperties.memoryTypeCount(), "Failed to find suitable memory type");
			
			allocInfo.memoryTypeIndex(9);
			
			LongBuffer pVertexBufferMemory = stack.mallocLong(1);
			
			VulkanErrorUtils.assertNoError(VK10.vkAllocateMemory(device.getDevice(), allocInfo, null, pVertexBufferMemory), "Failed to allocate vertex buffer memory");
			
			memory = pVertexBufferMemory.get(0);
			
			VK10.vkBindBufferMemory(device.getDevice(), id, memory, 0);
			
			PointerBuffer data = stack.mallocPointer(1);
			
			VK10.vkMapMemory(device.getDevice(), memory, 0, bufferInfo.size(), 0, data);
			{
				FloatBuffer d = data.getFloatBuffer((int) bufferInfo.size());
				d.put(data0);
			}
			VK10.vkUnmapMemory(device.getDevice(), memory);
		}
	}
	
	public void bind(CommandBufferData commandBufferData)
	{
		VK10.vkCmdBindVertexBuffers(commandBufferData.getCommandBuffer(), 0, new long[] {id}, new long[] {0});
	}
	
	public long getId()
	{
		return id;
	}
	
	@Override
	public void close()
	{
		VkDevice device = RenderEngine.getVulkan().getDevice().getDevice();
		VK10.vkDestroyBuffer(device, id, null);
		VK10.vkFreeMemory(device, memory, null);
	}
}
