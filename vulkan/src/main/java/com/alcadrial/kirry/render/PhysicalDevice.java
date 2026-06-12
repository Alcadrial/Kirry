package com.alcadrial.kirry.render;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPhysicalDeviceLimits;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

import com.alcadrial.kirry.render.queue.QueueFamily;
import com.alcadrial.kirry.render.util.AbstractVulkanResource;

/**
 * A physically connected device usable by vulkan that is necessary
 * to create a {@link Device Logical Device} and a {@link Surface}.
 * <p>
 * A {@link List} of all the available Physical Devices can be queried by
 * {@link Vulkan#getAvailablePhysicalDevices()} or {@link Vulkan#getValidPhysicalDevices()}
 * to filter the returned Physical Devices.
 * <p>
 * It derives from a {@link Vulkan} instance.
 * 
 * @author Alcadrial
 *
 */
public class PhysicalDevice extends AbstractVulkanResource<VkPhysicalDevice> {
	
	static
	{
		KirryVulkan.assertLoaded();
	}
	
	private static final int[] BEST_MATCHER_MAP = new int[] {4, 2, 0, 1, 3};
	private static final String[] PREFIXS;
	private static final String[] PADDED_PREFIXS;
	
	static
	{
		PREFIXS = new String[16];
		PADDED_PREFIXS = new String[16];
		for (int i = 0; i < 16; i++)
		{
			String s = "";
			if ((i & VK10.VK_QUEUE_SPARSE_BINDING_BIT) != 0) s += "S";
			if ((i & VK10.VK_QUEUE_TRANSFER_BIT) != 0) s += "T";
			if ((i & VK10.VK_QUEUE_COMPUTE_BIT) != 0) s += "C";
			if ((i & VK10.VK_QUEUE_GRAPHICS_BIT) != 0) s += "G";
			PREFIXS[i] = s;
			PADDED_PREFIXS[i] = s + " ".repeat(4 - s.length());
		}
	}
	
	@FunctionalInterface
	public static interface PhysicalDevicePickFilter {
		
		boolean filter(PhysicalDevice device, Surface.Builder surface);
	}
	
	public static final class Filter {
		
		public static PhysicalDevicePickFilter withSurfaceGraphics()
		{
			return (physicalDevice, surface) -> physicalDevice.getQueueFamily(VK10.VK_QUEUE_GRAPHICS_BIT, surface) != null;
		}
		
		public static PhysicalDevicePickFilter withSurfaceGraphicsAndCompute()
		{
			return (physicalDevice, surface) -> physicalDevice.getQueueFamily(VK10.VK_QUEUE_GRAPHICS_BIT | VK10.VK_QUEUE_COMPUTE_BIT, surface) != null;
		}
		
		public static PhysicalDevicePickFilter withCompute()
		{
			return (physicalDevice, _) -> physicalDevice.getQueueFamily(VK10.VK_QUEUE_COMPUTE_BIT) != null;
		}
		
		public static PhysicalDevicePickFilter any()
		{
			return (_, _) -> true;
		}
	}
	
	@FunctionalInterface
	public static interface PhysicalDevicePickStrategy {
		
		PhysicalDevice pick(List<PhysicalDevice> devices);
	}
	
	public static final class Strategy {
		
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
		 * @return a Comparator
		 */
		public static Comparator<PhysicalDevice> typeBestMatcher()
		{
			return Comparator.comparingInt(device -> {
				int deviceType = device.getProperties().deviceType();
				if (deviceType < 0 || deviceType >= BEST_MATCHER_MAP.length) return -1;
				return BEST_MATCHER_MAP[deviceType];
			});
		}
		
		public static PhysicalDevicePickStrategy findBest()
		{
			return findBest(typeBestMatcher());
		}
		
		public static PhysicalDevicePickStrategy findBest(Comparator<PhysicalDevice> comparator)
		{
			return devices -> {
				Iterator<PhysicalDevice> i = devices.iterator();
				PhysicalDevice bestMatch = i.next();
				while (i.hasNext())
				{
					PhysicalDevice candidate = i.next();
					if (comparator.compare(bestMatch, candidate) < 0) bestMatch = candidate;
				}
				return bestMatch;
			};
		}
		
		public static PhysicalDevicePickStrategy atIndexOrElse(int index, PhysicalDevicePickStrategy fallback)
		{
			if (index < 0) return fallback;
			return devices -> {
				if (index < devices.size()) return devices.get(index);
				return fallback.pick(devices);
			};
		}
	}
	
	public static String queueFlagsToString(int flag)
	{
		return PREFIXS[Objects.checkIndex(flag, PREFIXS.length)];
	}
	
	public static String queueFlagsToPaddedString(int flag)
	{
		return PADDED_PREFIXS[Objects.checkIndex(flag, PREFIXS.length)];
	}
	
	private VkPhysicalDeviceProperties properties;
	private VkPhysicalDeviceFeatures features;
	private List<LinkedList<QueueFamily>> queues;
	
	public PhysicalDevice(VkPhysicalDevice vk)
	{
		this.vk = vk;
		properties = VkPhysicalDeviceProperties.create();
		VK10.vkGetPhysicalDeviceProperties(vk, properties);
		features = VkPhysicalDeviceFeatures.create();
		VK10.vkGetPhysicalDeviceFeatures(vk, features);
		queues = new ArrayList<>(16);
		for (int i = 0; i < 16; i++) queues.add(new LinkedList<>());
		searchFamilies();
	}
	
	private void searchFamilies()
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			for (LinkedList<QueueFamily> l : queues) l.clear();
			
			int[] p0 = new int[1];
			VK10.vkGetPhysicalDeviceQueueFamilyProperties(vk, p0, null);
			VkQueueFamilyProperties.Buffer buffer = VkQueueFamilyProperties.malloc(p0[0], stack);
			VK10.vkGetPhysicalDeviceQueueFamilyProperties(vk, p0, buffer);
			
			for (int i = 0; i < buffer.capacity(); i++) queues.get(buffer.get(i).queueFlags() & 0x0F).add(new QueueFamily(buffer.get(i), i));
		}
	}
	
	public VkPhysicalDeviceProperties getProperties()
	{
		return properties;
	}
	
	public VkPhysicalDeviceFeatures getFeatures()
	{
		return features;
	}
	
	public VkPhysicalDeviceLimits getLimits()
	{
		return properties.limits();
	}
	
	public QueueFamily getGraphicsQueueFamily()
	{
		return (@NonNull QueueFamily) getQueueFamily(VK10.VK_QUEUE_GRAPHICS_BIT, 0, null);
	}
	
	public @Nullable QueueFamily getGraphicsQueueFamily(Surface.Builder surface)
	{
		return getQueueFamily(VK10.VK_QUEUE_GRAPHICS_BIT, surface.getVk(), null);
	}
	
	public @Nullable QueueFamily getGraphicsQueueFamily(Surface surface)
	{
		return getQueueFamily(VK10.VK_QUEUE_GRAPHICS_BIT, surface.getVk(), null);
	}
	
	public @Nullable QueueFamily getQueueFamily(int flags)
	{
		return getQueueFamily(flags, 0, null);
	}
	
	public @Nullable QueueFamily getQueueFamily(int flags, Surface.Builder surface)
	{
		return getQueueFamily(flags, surface.getVk(), null);
	}
	
	public @Nullable QueueFamily getQueueFamily(int flags, Surface surface)
	{
		return getQueueFamily(flags, surface.getVk(), null);
	}
	
	public @Nullable QueueFamily getQueueFamily(int flags, @Nullable Comparator<QueueFamily> preference)
	{
		return getQueueFamily(flags, 0, preference);
	}
	
	public @Nullable QueueFamily getQueueFamily(int flags, @Nullable Surface surface, @Nullable Comparator<QueueFamily> preference)
	{
		return getQueueFamily(flags, surface == null ? 0 : surface.getVk(), preference);
	}
	
	public @Nullable QueueFamily getQueueFamily(int flags, Surface.@Nullable Builder surface, @Nullable Comparator<QueueFamily> preference)
	{
		return getQueueFamily(flags, surface == null ? 0 : surface.getVk(), preference);
	}
	
	public @Nullable QueueFamily getQueueFamily(int flags, long surfaceVk, @Nullable Comparator<QueueFamily> preference)
	{
		if (flags >= 16) throw new IllegalArgumentException("Unsupported flag: " + flags);
		
		int minimalIndex = (~flags) & 0xF;
		Integer[] steps = new Integer[minimalIndex + 1];
		extractValidFlags(minimalIndex, 8, steps);
		List<List<QueueFamily>> families = new ArrayList<>(steps.length);
		for (Integer i : steps) if (i != null) families.add(queues.get(i + flags));
		if (families.size() == 0) return null;
		
		QueueFamily bestFamily = null;
		Iterator<List<QueueFamily>> i = families.iterator();
		List<QueueFamily> l = null;
		if (surfaceVk != 0)
		{
			int[] p = new int[1];
			out:
			while (i.hasNext())
			{
				l = i.next();
				for (QueueFamily family : l)
				{
					KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR(vk, family.getIndex(), surfaceVk, p);
					if (p[0] == VK10.VK_TRUE)
					{
						bestFamily = l.get(0);
						break out;
					}
				}
			}
		}
		else
		{
			out:
			while (i.hasNext())
			{
				l = i.next();
				if (l.size() > 0)
				{
					bestFamily = l.get(0);
					break out;
				}
			}
		}
		if (preference == null) return bestFamily;
		
		if (surfaceVk != 0)
		{
			int[] p = new int[1];
			while (i.hasNext())
			{
				l = i.next();
				for (QueueFamily family : l)
				{
					KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR(vk, family.getIndex(), surfaceVk, p);
					if (p[0] == VK10.VK_TRUE && preference.compare(family, bestFamily) > 0) bestFamily = family;
				}
			}
		}
		else
		{
			while (i.hasNext())
			{
				l = i.next();
				for (QueueFamily family : l) if (preference.compare(family, bestFamily) > 0) bestFamily = family;
			}
		}
		return bestFamily;
	}
	
	private void extractValidFlags(int flags, int index, Integer[] destination)
	{
		destination[flags] = flags;
		while (index > 0)
		{
			if ((flags & index) != 0) extractValidFlags(flags ^ index, index >>= 1, destination);
			else index >>= 1;
		}
	}
	
	public int indices()
	{
		return queues.stream().collect(Collectors.summingInt(List::size));
	}
	
	@Override
	public void close()
	{
		properties.close();
		features.close();
		super.close();
	}
	
	@Override
	public void debug()
	{
		Debug.print("vk: " + Debug.address(vk));
	}
}
