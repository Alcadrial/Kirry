package com.alcadrial.renderengine.render;

import java.util.Comparator;
import java.util.Iterator;

import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

public class PhysicalDevice implements AutoCloseable {
	
	private static final int[] BEST_MATCHER_MAP = new int[] {4, 2, 0, 1, 3};
	
	/**
	 * BestMatcher for {@link Vulkan#pickPhysicalDevices(Comparator)} that use the device type
	 * <table class="striped" style="text-align:center">
	 *     <caption><b>Device Types hierarchy</b></caption>
	 *     <thead><tr><th scope="col">Priority</th><th scope="col">Device Type</th></tr></thead>
	 *     <tbody>
	 *         <tr><th scope="row">4</th><td>{@link VK10#VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU DISCRETE GPU}</td></tr>
	 *         <tr><th scope="row">3</th><td>{@link VK10#VK_PHYSICAL_DEVICE_TYPE_VIRTUAL_GPU VIRTUAL GPU}</td></tr>
	 *         <tr><th scope="row">2</th><td>{@link VK10#VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU INTEGRATED GPU}</td></tr>
	 *         <tr><th scope="row">1</th><td>{@link VK10#VK_PHYSICAL_DEVICE_TYPE_CPU CPU}</td></tr>
	 *         <tr><th scope="row">0</th><td>{@link VK10#VK_PHYSICAL_DEVICE_TYPE_OTHER OTHER}</td></tr>
	 *     </tbody>
	 * </table>
	 * 
	 * @return
	 */
	public static Comparator<PhysicalDevice> typeBestMatcher()
	{
		return Comparator.comparingInt(device -> {
			int deviceType = device.getProperties().deviceType();
			if (deviceType < 0 || deviceType >= BEST_MATCHER_MAP.length) return -1;
			return BEST_MATCHER_MAP[deviceType];
		});
	}
	
	private VkPhysicalDevice device;
	private VkPhysicalDeviceProperties properties;
	private QueueFamilies queueFamilies;
	
	public PhysicalDevice(VkPhysicalDevice device, long surface)
	{
		this.device = device;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			properties = new VkPhysicalDeviceProperties(BufferUtils.createByteBuffer(VkPhysicalDeviceProperties.SIZEOF));
			VK10.vkGetPhysicalDeviceProperties(device, properties);
		}
		queueFamilies = new QueueFamilies();
		searchQueueSurfaceFamilyProperties(surface);
	}
	
	public VkPhysicalDevice getDevice()
	{
		return device;
	}
	
	public VkPhysicalDeviceProperties getProperties()
	{
		return properties;
	}
	
	public void searchQueueGraphicsFamilyProperties()
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			QueueFamily family = queueFamilies.graphics.family;
			if (family.getProperties() != null)
			{
				family.setProperties(null);
				queueFamilies.families--;
			}
			
			int[] p0 = new int[1];
			VK10.vkGetPhysicalDeviceQueueFamilyProperties(device, p0, null);
			VkQueueFamilyProperties.Buffer queueFamiliesArray = VkQueueFamilyProperties.malloc(p0[0], stack);
			VK10.vkGetPhysicalDeviceQueueFamilyProperties(device, p0, queueFamiliesArray);
			
			for (int i = 0; i < queueFamiliesArray.capacity(); i++)
			{
				if ((queueFamiliesArray.queueFlags() & VK10.VK_QUEUE_GRAPHICS_BIT) != 0)
				{
					family.setProperties(queueFamiliesArray.get(i));
					family.setIndex(i);
					queueFamilies.families++;
					break;
				}
			}
			queueFamilies.buildSet();
		}
	}
	
	public VkQueueFamilyProperties getQueueGraphicsFamilyProperties()
	{
		if (queueFamilies.graphics.family.getProperties() == null) searchQueueGraphicsFamilyProperties();
		return queueFamilies.graphics.family.getProperties();
	}
	
	public int getQueueGraphicsFamilyIndex()
	{
		if (queueFamilies.graphics.family.getProperties() == null) searchQueueGraphicsFamilyProperties();
		return queueFamilies.graphics.family.getIndex();
	}
	
	public void searchQueueSurfaceFamilyProperties(long surface)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			QueueFamily family = queueFamilies.surface.family;
			if (family.getProperties() != null)
			{
				family.setProperties(null);
				queueFamilies.families--;
			}
			
			int[] p = new int[1];
			VK10.vkGetPhysicalDeviceQueueFamilyProperties(device, p, null);
			VkQueueFamilyProperties.Buffer queueFamiliesArray = VkQueueFamilyProperties.malloc(p[0], stack);
			VK10.vkGetPhysicalDeviceQueueFamilyProperties(device, p, queueFamiliesArray);
			
			for (int i = 0; i < queueFamiliesArray.capacity(); i++)
			{
				KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR(device, i, surface, p);
				
				if (p[0] == VK10.VK_TRUE)
				{
					family.setProperties(queueFamiliesArray.get(i));
					family.setIndex(i);
					queueFamilies.families++;
					break;
				}
			}
			queueFamilies.buildSet();
		}
	}
	
	public VkQueueFamilyProperties getQueueSurfaceFamilyProperties()
	{
		return queueFamilies.surface.family.getProperties();
	}
	
	public int getQueueSurfaceFamilyIndex()
	{
		return queueFamilies.surface.family.getIndex();
	}
	
	public Iterable<QueueFamily> iterateFamilies()
	{
		return queueFamilies.iterateFamilies();
	}
	
	public int families()
	{
		return queueFamilies.families;
	}
	
	public Iterable<Integer> iterateIndices()
	{
		return queueFamilies.iterateIndices();
	}
	
	public int indices()
	{
		return queueFamilies.indices;
	}
	
	@Override
	public void close()
	{
		properties.close();
	}
	
	private class QueueFamilies {
		
		private FamilyNode graphics;
		private FamilyNode surface;
		public int families;
		
		private IndexNode indicesHead;
		public int indices;
		
		public QueueFamilies()
		{
			surface = new FamilyNode(new QueueFamily(), null);
			graphics = new FamilyNode(new QueueFamily(), surface);
		}
		
		private void buildSet()
		{
			indicesHead = null;
			queueFamilies.indices = 0;
			family: for (QueueFamily family : iterateFamilies()) if (family.getProperties() != null)
			{
				for (Integer index : iterateIndices()) if (family.getIndex() == index) continue family;
				
				if (indicesHead == null)
				{
					indicesHead = new IndexNode(family.getIndex());
					queueFamilies.indices++;
					continue;
				}
				IndexNode node = indicesHead;
				while (node.next != null) node = node.next;
				node.next = new IndexNode(family.getIndex());
				queueFamilies.indices++;
			}
		}
		
		public Iterable<QueueFamily> iterateFamilies()
		{
			return () -> new Iterator<>() {
				
				private FamilyNode current = graphics;
				
				@Override
				public boolean hasNext()
				{
					return current != null;
				}
				
				@Override
				public QueueFamily next()
				{
					QueueFamily family = current.family;
					current = current.next;
					return family;
				}
			};
		}
		
		public Iterable<Integer> iterateIndices()
		{
			return () -> new Iterator<>() {
				
				private IndexNode current = indicesHead;
				
				@Override
				public boolean hasNext()
				{
					return current != null;
				}
				
				@Override
				public Integer next()
				{
					Integer family = current.index;
					current = current.next;
					return family;
				}
			};
		}
		
		public class FamilyNode {
			
			private QueueFamily family;
			private FamilyNode next;
			
			public FamilyNode(QueueFamily family, FamilyNode next)
			{
				this.family = family;
				this.next = next;
			}
		}
		
		public class IndexNode {
			
			private Integer index;
			private IndexNode next;
			
			public IndexNode(Integer index)
			{
				this.index = index;
				next = null;
			}
		}
	}
}
