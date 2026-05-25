package com.alcadrial.renderengine.render.texture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.function.Function;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkImageCopy;

import com.alcadrial.renderengine.core.util.Pair;
import com.alcadrial.renderengine.core.util.color.Color;
import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngineVulkan;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.commandbuffer.CommandBuffer;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public class Atlas {
	
	static
	{
		RenderEngineVulkan.assertLoaded();
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
		private Object id;
		
		public Resource(int width, int height, Object id)
		{
			this.width = width;
			this.height = height;
			this.id = id;
		}
		
		abstract void copy(CommandBuffer commandBuffer, Texture atlasTexture, int x, int y);
		abstract void close();
		
		@Override
		public boolean equals(@Nullable Object obj)
		{
			return obj instanceof Resource r && r.id.equals(id);
		}
		
		@Override
		public int hashCode()
		{
			return id.hashCode();
		}
	}
	
	private class TextureResource extends Resource {
		
		Texture texture;
		
		public TextureResource(Texture texture, Object id)
		{
			super(texture.getWidth(), texture.getHeight(), id);
			this.texture = texture;
		}
		
		@Override
		void copy(CommandBuffer commandBuffer, Texture atlasTexture, int x, int y)
		{
			commandBuffer.copyImage(texture, 0, 0, 0, atlasTexture, x, y, 0, width, height, 1);
		}
		
		@Override
		void close()
		{
			texture.close();
		}
	}
	
	private class VulkanBufferResource extends Resource {
		
		VulkanBuffer buffer;
		
		public VulkanBufferResource(VulkanBuffer buffer, int width, int height, Object id)
		{
			super(width, height, id);
			this.buffer = buffer;
		}
		
		@Override
		void copy(CommandBuffer commandBuffer, Texture atlasTexture, int x, int y)
		{
			commandBuffer.copyBufferToImage(buffer, 0, atlasTexture, x, y, 0, width, height, 1);
		}
		
		@Override
		void close()
		{
			buffer.close();
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
	
	private MutableList<Region> oldRegions;
	private MutableList<Region> regions;
	private TreeSet<Slot> slots;
	private Map<Resource, Region> resources;
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
		isDirty = true;
		texture = Texture.create2dTexture(device, width, height, format, this.usage, aspectMask);
		Debug.runOnDebug(() -> texture.setName("Atlas"));
		device.configureAndSubmitSingleUseCommandBuffer(device.getGraphicsQueue(), commandBuffer -> {
			texture.changeLayout(commandBuffer, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
			if (debug)
			{
				commandBuffer.clearColorImage(texture, Color.LIGHT_RED);
			}
		});
		slots = new TreeSet<>();
		oldRegions = Lists.mutable.of();
		regions = Lists.mutable.of();
		resources = Maps.mutable.of();
		emptySlots();
	}
	
	public TextureRegion add(Texture texture, Object id)
	{
		return add(new TextureResource(texture, id));
	}
	
	public <T> List<TextureRegion> addAllTextures(Function<T, Texture> getTexture, Function<T, Object> getId, @SuppressWarnings("unchecked") T... textures)
	{
		List<Resource> resources = new ArrayList<>(textures.length);
		for (T texture : textures) resources.add(new TextureResource(getTexture.apply(texture), getId.apply(texture)));
		return addAll(resources);
	}
	
	public <T> List<TextureRegion> addAllTextures(Map<T, Texture> textures)
	{
		List<Resource> resources = new ArrayList<>(textures.size());
		for (Entry<T, Texture> e : textures.entrySet()) resources.add(new TextureResource(e.getValue(), e.getKey()));
		return addAll(resources);
	}
	
	public TextureRegion add(TextureBuffer buffer)
	{
		return add(new VulkanBufferResource(buffer.getBuffer(), buffer.getWidth(), buffer.getHeight(), buffer.getId()));
	}
	
	public TextureRegion add(TextureBuffer buffer, Object id)
	{
		return add(new VulkanBufferResource(buffer.getBuffer(), buffer.getWidth(), buffer.getHeight(), id));
	}
	
	public List<TextureRegion> addAllBuffers(Iterable<TextureBuffer> buffers)
	{
		List<Resource> resources = new ArrayList<>();
		for (TextureBuffer buffer : buffers) resources.add(new VulkanBufferResource(buffer.getBuffer(), buffer.getWidth(), buffer.getHeight(), buffer.getId()));
		return addAll(resources);
	}
	
	public List<TextureRegion> addAllBuffers(Collection<TextureBuffer> buffers)
	{
		List<Resource> resources = new ArrayList<>(buffers.size());
		for (TextureBuffer buffer : buffers) resources.add(new VulkanBufferResource(buffer.getBuffer(), buffer.getWidth(), buffer.getHeight(), buffer.getId()));
		return addAll(resources);
	}
	
	public List<TextureRegion> addAllBuffers(TextureBuffer... buffers)
	{
		List<Resource> resources = new ArrayList<>(buffers.length);
		for (TextureBuffer buffer : buffers)
		{
			resources.add(new VulkanBufferResource(buffer.getBuffer(), buffer.getWidth(), buffer.getHeight(), buffer.getId()));
		}
		return addAll(resources);
	}
	
	public <T> List<TextureRegion> addAllBuffers(Function<T, TextureBuffer> getBuffer, Function<T, Object> getId, @SuppressWarnings("unchecked") T... buffers)
	{
		List<Resource> resources = new ArrayList<>(buffers.length);
		for (T bufferT : buffers)
		{
			TextureBuffer buffer = getBuffer.apply(bufferT);
			resources.add(new VulkanBufferResource(buffer.getBuffer(), buffer.getWidth(), buffer.getHeight(), getId.apply(bufferT)));
		}
		return addAll(resources);
	}
	
	public <T> List<TextureRegion> addAllBuffers(Map<T, TextureBuffer> buffers)
	{
		List<Resource> resources = new ArrayList<>(buffers.size());
		for (Entry<T, TextureBuffer> e : buffers.entrySet())
		{
			TextureBuffer buffer = e.getValue();
			resources.add(new VulkanBufferResource(buffer.getBuffer(), buffer.getWidth(), buffer.getHeight(), e.getKey()));
		}
		return addAll(resources);
	}
	
	public void freeRegion(TextureRegion region)
	{
		int newX = region.getXStart();
		int newY = region.getYStart();
		int newWidth = region.getXEnd() - region.getXStart();
		int newHeight = region.getYEnd() - region.getYStart();
		int tests = 4;
		for (Iterator<Slot> i = slots.reversed().iterator(); i.hasNext() && tests > 0;)
		{
			Slot slot = i.next();
			if (region.getYStart() < slot.y + slot.height && region.getYEnd() < slot.y)
			{
				if (slot.x + slot.width == region.getXStart())
				{
					newX -= slot.width;
					newWidth += slot.width;
					i.remove();
					tests--;
				}
				else if (slot.x == region.getXEnd())
				{
					newWidth += slot.width;
					i.remove();
					tests--;
				}
			}
			else if (region.getXStart() < slot.x + slot.width && region.getXEnd() < slot.x)
			{
				if (slot.y + slot.height == region.getYStart())
				{
					newY -= slot.height;
					newHeight += slot.height;
					i.remove();
					tests--;
				}
				else if (slot.y == region.getYEnd())
				{
					newHeight += slot.height;
					i.remove();
					tests--;
				}
			}
		}
		slots.add(new Slot(newX, newY, newWidth, newHeight));
		availableArea += (region.getXEnd() - region.getXStart()) * (region.getYEnd() - region.getYStart());
	}
	
	private TextureRegion add(Resource resource)
	{
		return addAll(List.of(resource)).getFirst();
	}
	
	private List<TextureRegion> addAll(List<Resource> resources)
	{
		record Holder(Resource r, int i) {}
		
		FastList<Holder> newResources = new FastList<>(resources.size());
		TextureRegion[] newRegions = new TextureRegion[resources.size()];
		{
			int i = 0;
			for (Resource resource : resources)
			{
				Region r = this.resources.get(resource);
				if (r != null) newRegions[i++] = r.texture;
				else newResources.add(new Holder(resource, i++));
			}
		}
		
		if (!newResources.isEmpty())
		{
			isDirty = true;
			
			newResources.sortThis((t0, t1) -> -Integer.compare(t0.r.width, t1.r.width));
			
			int requiredArea = 0;
			for (Holder h : newResources) requiredArea += h.r.width * h.r.height;
			
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
			
			for (Holder h : newResources)
			{
				boolean isMixed = false;
				Region region;
				while ((region = tryInsert(h.r, null)) == null)
				{
					if (isMixed)
					{
						growAtlas();
						mixTextures();
						isMixed = false;
					}
					else
					{
						mixTextures();
						isMixed = true;
					}
				}
				newRegions[h.i] = region.texture;
				this.regions.add(region);
				this.resources.put(h.r, region);
			}
		}
		return Arrays.asList(newRegions);
	}
	
	private @Nullable Region tryInsert(Resource resource, @Nullable TextureRegion oldRegion)
	{
		Iterator<Slot> i = slots.tailSet(new Slot(resource.width)).iterator();
		while (i.hasNext())
		{
			Slot slot = i.next();
			if (slot.height >= resource.height)
			{
				i.remove();
				return insertIntoSlot(resource, oldRegion, slot);
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
	
	private Region insertIntoSlot(Resource resource, @Nullable TextureRegion oldRegion, Slot s)
	{
		TextureRegion region = oldRegion != null ? oldRegion : new TextureRegion(texture, s.x, s.y, s.x + resource.width, s.y + resource.height);
		
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
		List<Resource> copy = new ArrayList<>(regions.size());
		for (Region region : regions) copy.add(region.resource);
		
		oldRegions.clear();
		oldRegions.addAll(regions);
		regions.clear();
		
		isDirty = true;
		List<Pair<Resource, TextureRegion>> l = oldRegions.collect(r -> new Pair<>(r.resource, r.texture));
		l.sort((t0, t1) -> -Integer.compare(t0.left.width, t1.left.width));
		
		for (Pair<Resource, TextureRegion> p : l)
		{
			Region region;
			while ((region = tryInsert(p.left, p.right)) == null) growAtlas();
			regions.add(region);
		}
	}
	
	private void growAtlas()
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
			Debug.runOnDebug(() -> newTexture.setName("Atlas"));
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
							region.resource.copy(commandBuffer, newTexture, region.x, region.y);
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
							oldRegion.texture.update(newTexture, region.x, region.y, region.x + region.width, region.y + region.height);
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
					newTexture.changeLayout(commandBuffer, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
				}
			}).join();
			
			texture.close();
			texture = newTexture;
		}
		else if (texture.getWidth() < width || texture.getHeight() < height)
		{
			texture.close();
			Texture newTexture = Texture.create2dTexture(device, width, height, format, usage, aspectMask);
			Debug.runOnDebug(() -> newTexture.setName("Atlas"));
			
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
				newTexture.changeLayout(commandBuffer, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
			}).join();
			texture = newTexture;
		}
		else
		{
			device.configureAndSubmitSingleUseCommandBuffer(device.getGraphicsQueue(), commandBuffer -> {
				texture.changeLayout(commandBuffer, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
				for (int i = 0; i < regions.size(); i++)
				{
					Region region = regions.get(i);
					if (region.isDirty)
					{
						region.resource.copy(commandBuffer, texture, region.x, region.y);
						region.isDirty = false;
					}
				}
				texture.changeLayout(commandBuffer, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
			}).join();
		}
		for (Region region : regions) region.resource.close();
		
		isDirty = false;
	}
	
	public void clear()
	{
		clear(width, height);
	}
	
	public void clear(int width, int height)
	{
		isDirty |= !regions.isEmpty();
		this.width = width;
		this.height = height;
		emptySlots();
		regions.clear();
		resources.clear();
	}
	
	private void emptySlots()
	{
		slots.clear();
		slots.add(new Slot(0, 0, width, height));
		availableArea = width * height;
	}
	
	public void tryFit()
	{
		mixTextures();
	}
	
	public Device getDevice()
	{
		return device;
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
	
	public void close()
	{
		texture.close();
	}
	
	@Override
	public String toString()
	{
		return "Atlas{" + texture + "}";
	}
}
