package com.alcadrial.renderengine.render.texture;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkImageCopy;

import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.renderengine.core.util.color.Color;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.commandbuffer.CommandBuffer;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public class Atlas {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private class Slot implements Comparable<@NonNull Slot> {
		
		int x;
		int y;
		int width;
		int height;
		
		public Slot(int width)
		{
			this(0, 0, width, 0);
		}
		
		public Slot(int x, int y, int width, int height)
		{
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}
		
		@Override
		public int compareTo(Slot o)
		{
			int c = Integer.compare(width, o.width);
			if (c != 0) return c;
			c = Integer.compare(height, o.height);
			if (c != 0) return c;
			c = Integer.compare(x, o.x);
			if (c != 0) return c;
			c = Integer.compare(y, o.y);
			return c;
		}
	}
	
	private abstract class Resource {
		
		protected int width;
		protected int height;
		
		public Resource(int width, int height)
		{
			this.width = width;
			this.height = height;
		}
		
		abstract void copy(CommandBuffer commandBuffer, Texture atlasTexture, int x, int y);
	}
	
	private class TextureResource extends Resource {
		
		Texture texture;
		
		public TextureResource(Texture texture)
		{
			super(texture.getWidth(), texture.getHeight());
			this.texture = texture;
		}
		
		@Override
		void copy(CommandBuffer commandBuffer, Texture atlasTexture, int x, int y)
		{
			commandBuffer.copyImage(texture, 0, 0, 0, atlasTexture, x, y, 0, width, height, 1);
		}
	}
	
	private class VulkanBufferResource extends Resource {
		
		VulkanBuffer buffer;
		
		public VulkanBufferResource(VulkanBuffer buffer, int width, int height)
		{
			super(width, height);
			this.buffer = buffer;
		}
		
		@Override
		void copy(CommandBuffer commandBuffer, Texture atlasTexture, int x, int y)
		{
			commandBuffer.copyBufferToImage(buffer, 0, atlasTexture, x, y, 0, width, height, 1);
		}
	}
	
	private class Region extends Slot {
		
		Resource resource;
		TextureRegion texture;
		boolean isDirty;
		
		public Region(Resource resource, TextureRegion texture, int x, int y, int width, int height)
		{
			super(x, y, width, height);
			this.resource = resource;
			this.texture = texture;
			isDirty = true;
		}
	}
	
	private Device device;
	private int format;
	private int usage;
	private int aspectMask;
	private int width;
	private int height;
	private Texture texture;
	private boolean isDirty;
	
	private List<Region> oldRegions;
	private List<Region> regions;
	private TreeSet<Slot> slots;
	private int availableArea;
	
	private boolean debug;
	
	public Atlas(Device device, int format, int usage, int aspectMask) throws VulkanException
	{
		this(device, 2048, 2048, format, usage, aspectMask, false);
	}
	
	public Atlas(Device device, int startWidth, int startHeight, int format, int usage, int aspectMask) throws VulkanException
	{
		this(device, startWidth, startHeight, format, usage, aspectMask, false);
	}
	
	public Atlas(Device device, int startWidth, int startHeight, int format, int usage, int aspectMask, boolean debug) throws VulkanException
	{
		this.device = device;
		this.format = format;
		this.usage = usage | VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT;
		this.aspectMask = aspectMask;
		this.debug = debug;
		width = startWidth;
		height = startHeight;
		texture = Texture.create2dTexture(device, width, height, format, this.usage, aspectMask);
		device.configureAndSubmitSingleUseCommandBuffer(device.getGraphicsQueue(), commandBuffer -> {
			texture.changeLayout(commandBuffer, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
			if (debug)
			{
				commandBuffer.clearColorImage(texture, Color.LIGHT_RED);
			}
		});
		slots = new TreeSet<>();
		oldRegions = new ArrayList<>();
		regions = new ArrayList<>();
		emptySlots();
	}
	
	public TextureRegion add(Texture texture)
	{
		return add(new TextureResource(texture));
	}
	
	public List<TextureRegion> addAll(Texture... textures)
	{
		List<Resource> resources = new ArrayList<>(textures.length);
		for (Texture texture : textures) resources.add(new TextureResource(texture));
		return addAll(resources);
	}
	
	public TextureRegion add(TextureBuffer buffer)
	{
		return add(new VulkanBufferResource(buffer.getBuffer(), buffer.getWidth(), buffer.getHeight()));
	}
	
	public List<TextureRegion> addAll(TextureBuffer... buffers)
	{
		List<Resource> resources = new ArrayList<>(buffers.length);
		for (TextureBuffer buffer : buffers) resources.add(new VulkanBufferResource(buffer.getBuffer(), buffer.getWidth(), buffer.getHeight()));
		return addAll(resources);
	}
	
	private TextureRegion add(Resource resource)
	{
		return addAll(List.of(resource)).getFirst();
	}
	
	private List<TextureRegion> addAll(List<Resource> resources)
	{
		if (resources.isEmpty()) return List.of();
		isDirty = true;
		List<Resource> l = new ArrayList<>(resources);
		l.sort((t0, t1) -> -Integer.compare(t0.width, t1.width));
		
		int requiredArea = 0;
		for (Resource resource : resources) requiredArea += resource.width * resource.height;
		
		int currentArea = availableArea;
		while (currentArea < requiredArea)
		{
			if (height < width) height *= 2;
			else width *= 2;
			currentArea *= 2;
		}
		if (currentArea != availableArea)
		{
			mixTextures();
			availableArea = currentArea;
		}
		
		List<TextureRegion> newRegions = new ArrayList<>(texture.getSize());
		for (Resource resource : l)
		{
			boolean isMixed = false;
			Region region;
			while ((region = tryInsert(resource)) == null)
			{
				if (isMixed)
				{
					growAtlas(l);
					mixTextures(l);
					isMixed = false;
				}
				else
				{
					mixTextures(l);
					isMixed = true;
				}
			}
			newRegions.add(region.texture);
			regions.add(region);
		}
		return newRegions;
	}
	
	private @Nullable Region tryInsert(Resource resource)
	{
		Iterator<Slot> i = slots.tailSet(new Slot(resource.width)).iterator();
		while (i.hasNext())
		{
			Slot slot = i.next();
			if (slot.height >= resource.height)
			{
				i.remove();
				return insertIntoSlot(resource, slot);
			}
		}
		return null;
	}
	
	private boolean intersect(Slot s0, Slot s1)
	{
		if (s0.x + s0.width <= s1.x || s1.x + s1.width <= s0.x) return false;
		if (s0.y + s0.height <= s1.y || s1.y + s1.height <= s0.y) return false;
		return true;
	}
	
	private Region insertIntoSlot(Resource resource, Slot s)
	{
		TextureRegion region = new TextureRegion(texture, Vector2f.of((float) s.x / width, (float) s.y / height), Vector2f.of((float) (s.x + resource.width) / width, (float) (s.y + resource.height) / height));
		
		Slot regionSlot = new Slot(s.x, s.y, resource.width, resource.height);
		int x0 = regionSlot.x;
		int x1 = regionSlot.x + regionSlot.width;
		int y0 = regionSlot.y;
		int y1 = regionSlot.y + regionSlot.height;
		for (Slot slot : List.copyOf(slots)) if (intersect(slot, regionSlot))
		{
			int w0 = (slot.x + slot.width) - x1;
			int w1 = x0 - slot.x;
			if (w0 > 0) slots.add(new Slot(x1, slot.y, w0, slot.height));
			if (w1 > 0) slots.add(new Slot(slot.x, slot.y, w1, slot.height));
			int h0 = (slot.y + slot.height) - y1;
			int h1 = y0 - slot.y;
			if (h0 > 0) slots.add(new Slot(slot.x, y1, slot.width, h0));
			if (h1 > 0) slots.add(new Slot(slot.x, slot.y, slot.width, h1));
			slots.remove(slot);
		}
		if (s.width != resource.width) slots.add(new Slot(s.x + resource.width, s.y, s.width - resource.width, s.height));
		if (s.height != resource.height) slots.add(new Slot(s.x, s.y + resource.height, s.width, s.height - resource.height));
		
		pruneSlots();
		
		return new Region(resource, region, s.x, s.y, resource.width, resource.height);
	}
	
	private void pruneSlots()
	{
		Iterator<Slot> i = slots.reversed().iterator();
		while (i.hasNext())
		{
			Slot bigSlot = i.next();
			boolean removed = false;
			Iterator<Slot> j = slots.headSet(bigSlot, false).iterator();
			while (j.hasNext())
			{
				Slot smallSlot = j.next();
				if (bigSlot.x <= smallSlot.x && (smallSlot.x + smallSlot.width) <= (bigSlot.x + bigSlot.width))
				{
					if (bigSlot.y <= smallSlot.y && (smallSlot.y + smallSlot.height) <= (bigSlot.y + bigSlot.height))
					{
						j.remove();
						removed = true;
					}
				}
			}
			if (removed) i = slots.reversed().tailSet(bigSlot, false).iterator();
		}
	}
	
	private void mixTextures()
	{
		mixTextures(List.of());
	}
	
	private void mixTextures(List<Resource> resources)
	{
		List<Resource> copy = new ArrayList<>(regions.size());
		for (Region region : regions) copy.add(region.resource);
		
		oldRegions.clear();
		oldRegions.addAll(regions);
		
		clear();
		addAll(copy);
	}
	
	private void growAtlas(List<Resource> resources)
	{
		if (height < width) height *= 2;
		else width *= 2;
		availableArea *= 2;
	}
	
	public void draw() throws VulkanException
	{
		if (!isDirty) return;
		
		int newRegionsStartingIndex = oldRegions.size();
		
		if (newRegionsStartingIndex != 0)
		{
			Texture newTexture = Texture.create2dTexture(device, width, height, format, usage, aspectMask);
			
			device.configureAndSubmitSingleUseCommandBuffer(device.getGraphicsQueue(), commandBuffer -> {
				texture.changeLayout(commandBuffer, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL);
				newTexture.changeLayout(commandBuffer, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
				if (debug)
				{
					commandBuffer.clearColorImage(newTexture, Color.LIGHT_RED);
				}
				try (MemoryStack stack = MemoryStack.stackPush())
				{
					VkImageCopy.Buffer copyRegion = VkImageCopy.malloc(oldRegions.size(), stack);
					for (int j = 0; j < oldRegions.size(); j++)
					{
						Region oldRegion = oldRegions.get(j);
						Region region = regions.get(j);
						if (oldRegion.isDirty)
						{
							oldRegion.resource.copy(commandBuffer, newTexture, region.x, region.y);
							oldRegion.isDirty = false;
						}
						else
						{
							copyRegion.get()
								.srcOffset(o -> o.set(oldRegion.x, oldRegion.y, 0))
								.srcSubresource(i -> i
									.aspectMask(texture.getAspectMask())
									.mipLevel(0)
									.baseArrayLayer(0)
									.layerCount(1)
								)
								.dstOffset(o -> o.set(region.x, region.y, 0))
								.dstSubresource(i -> i
									.aspectMask(newTexture.getAspectMask())
									.mipLevel(0)
									.baseArrayLayer(0)
									.layerCount(1)
								)
								.extent(e -> e.set(region.width, region.height, 1));
						}
					}
					oldRegions.clear();
					copyRegion.flip();
					if (copyRegion.limit() > 0) commandBuffer.copyImage(texture, newTexture, copyRegion.flip());
					for (int i = newRegionsStartingIndex; i < regions.size(); i++)
					{
						Region region = regions.get(i);
						if (region.isDirty)
						{
							region.resource.copy(commandBuffer, newTexture, region.x, region.y);
							region.isDirty = false;
						}
					}
				}
			}).join();
			
			texture.close();
			texture = newTexture;
		}
		else if (texture.getWidth() < width || texture.getHeight() < height)
		{
			texture.close();
			Texture newTexture = Texture.create2dTexture(device, width, height, format, usage, aspectMask);
			
			device.configureAndSubmitSingleUseCommandBuffer(device.getGraphicsQueue(), commandBuffer -> {
				newTexture.changeLayout(commandBuffer, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
				if (debug)
				{
					commandBuffer.clearColorImage(newTexture, Color.LIGHT_RED);
				}
				for (int i = 0; i < regions.size(); i++)
				{
					Region region = regions.get(i);
					if (region.isDirty)
					{
						region.resource.copy(commandBuffer, newTexture, region.x, region.y);
						region.isDirty = false;
					}
				}
			}).join();
			texture = newTexture;
		}
		else
		{
			device.configureAndSubmitSingleUseCommandBuffer(device.getGraphicsQueue(), commandBuffer -> {
				for (int i = 0; i < regions.size(); i++)
				{
					Region region = regions.get(i);
					if (region.isDirty)
					{
						region.resource.copy(commandBuffer, texture, region.x, region.y);
						region.isDirty = false;
					}
				}
			}).join();
		}
		
		isDirty = false;
	}
	
	public void clear()
	{
		isDirty |= !regions.isEmpty();
		emptySlots();
		regions.clear();
	}
	
	private void emptySlots()
	{
		slots.clear();
		slots.add(new Slot(0, 0, width, height));
		availableArea = width * height;
	}
	
	public Texture getTexture()
	{
		return texture;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
}
