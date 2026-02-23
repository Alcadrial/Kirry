package com.alcadrial.renderengine.render.buffer;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;
import org.lwjgl.vulkan.VkWriteDescriptorSet.Buffer;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.kector.vector.Vector3i;
import com.alcadrial.kector.vector.Vector4f;
import com.alcadrial.kector.vector.Vector4i;
import com.alcadrial.renderengine.core.util.Pair;
import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.commandbuffer.CommandBuffer;
import com.alcadrial.renderengine.render.shader.ShaderInputType;
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.texture.TextureSampler;
import com.alcadrial.renderengine.render.texture.TextureView;
import com.alcadrial.renderengine.render.util.VulkanDebug;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public class BufferLayout implements VulkanDebug {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public static Builder of(Device device)
	{
		return new Builder(device);
	}
	
	public static Builder of(BufferLayout layout)
	{
		Builder builder = new Builder(layout.device);
		builder.writer = layout.writer;
		return builder;
	}
	
	public static class Builder {
		
		private Device device;
		private @Nullable BufferWriter writer;
		private VkWriteDescriptorSet.@Nullable Buffer writeDescriptorSets;
		
		private Builder(Device device)
		{
			this.device = device;
		}
		
		public Builder writer(BufferWriter writer)
		{
			this.writer = writer;
			return this;
		}
		
		public Builder writeDescriptorSets(Buffer writeDescriptorSets)
		{
			this.writeDescriptorSets = writeDescriptorSets;
			return this;
		}
		
		public BufferLayout build(Lock lock)
		{
			return new BufferLayout(this, lock);
		}
	}
	
	private final Lock lock;
	private final Device device;
	private final ConcurrentHashMap<String, Pair<ShaderVariable, Integer>> marks;
	private final VkWriteDescriptorSet.Buffer writeDescriptorSets;
	private final BufferWriter writer;
	private boolean isDirty;
	
	private BufferLayout(Builder builder, Lock lock)
	{
		device = builder.device;
		marks = new ConcurrentHashMap<>();
		writeDescriptorSets = Objects.requireNonNull(builder.writeDescriptorSets);
		this.lock = lock;
		writer = Objects.requireNonNull(builder.writer);
		isDirty = true;
	}
	
	public BufferWriter getWriter()
	{
		return writer;
	}
	
	public void mark(ShaderVariable variable, int offset)
	{
		marks.put(variable.getName(), new Pair<>(variable, offset));
	}
	
	public boolean isPresent(ShaderVariable variable)
	{
		return marks.containsKey(variable.getName());
	}
	
	public @Nullable Pair<ShaderVariable, Integer> getOffset(String variableName)
	{
		return marks.get(variableName);
	}
	
	public void forceUpdate()
	{
		lock.lock();
		try
		{
			VK10.vkUpdateDescriptorSets(device.getVk(), writeDescriptorSets, null);
			isDirty = false;
		}
		finally
		{
			lock.unlock();
		}
	}
	
	public void updateDescriptorSets()
	{
		lock.lock();
		try
		{
			if (isDirty)
			{
				VK10.vkUpdateDescriptorSets(device.getVk(), writeDescriptorSets, null);
				isDirty = false;
			}
		}
		finally
		{
			lock.unlock();
		}
	}
	
	public void forceUpdate(Consumer<VkWriteDescriptorSet.Buffer> action)
	{
		lock.lock();
		try
		{
			action.accept(writeDescriptorSets);
			VK10.vkUpdateDescriptorSets(device.getVk(), writeDescriptorSets, null);
			isDirty = false;
		}
		finally
		{
			lock.unlock();
		}
	}
	
	public void update(Consumer<VkWriteDescriptorSet.Buffer> action)
	{
		lock.lock();
		try
		{
			if (isDirty)
			{
				action.accept(writeDescriptorSets);
				isDirty = false;
			}
		}
		finally
		{
			lock.unlock();
		}
	}
	
	public void updateBuffers(VkWriteDescriptorSet.Buffer writeDescriptorSets)
	{
		VK10.vkUpdateDescriptorSets(device.getVk(), writeDescriptorSets, null);
	}
	
	public void set(String uniform, Matrix4f value) throws VulkanException
	{
		set(uniform, 0, value);
	}
	
	public void set(String uniform, int index, Matrix4f value) throws VulkanException
	{
		Pair<ShaderVariable, Integer> p = getOffset(uniform);
		if (p != null)
		{
			checkValue(ShaderInputType.MATRIX_FLOAT_4X4, p.left.getType());
			writer.setCursor(16 * index + p.right).put(value.toArray());
			isDirty = true;
		}
	}
	
	public void set(String uniform, Vector2f value) throws VulkanException
	{
		set(uniform, 0, value);
	}
	
	public void set(String uniform, int index, Vector2f value) throws VulkanException
	{
		Pair<ShaderVariable, Integer> p = getOffset(uniform);
		if (p != null)
		{
			checkValue(ShaderInputType.VECTOR_FLOAT_2, p.left.getType());
			writer.setCursor(2 * index + p.right).put(value.getValuesArray());
			isDirty = true;
		}
	}
	
	public void set(String uniform, Vector3f value) throws VulkanException
	{
		set(uniform, 0, value);
	}
	
	public void set(String uniform, int index, Vector3f value) throws VulkanException
	{
		Pair<ShaderVariable, Integer> p = getOffset(uniform);
		if (p != null)
		{
			checkValue(ShaderInputType.VECTOR_FLOAT_3, p.left.getType());
			writer.setCursor(4 * index + p.right).put(value.getValuesArray());
			isDirty = true;
		}
	}
	
	public void set(String uniform, Vector4f value) throws VulkanException
	{
		set(uniform, 0, value);
	}
	
	public void set(String uniform, int index, Vector4f value) throws VulkanException
	{
		Pair<ShaderVariable, Integer> p = getOffset(uniform);
		if (p != null)
		{
			checkValue(ShaderInputType.VECTOR_FLOAT_4, p.left.getType());
			writer.setCursor(4 * index + p.right).put(value.getValuesArray());
			isDirty = true;
		}
	}
	
	public void set(String uniform, Vector2i value) throws VulkanException
	{
		set(uniform, 0, value);
	}
	
	public void set(String uniform, int index, Vector2i value) throws VulkanException
	{
		Pair<ShaderVariable, Integer> p = getOffset(uniform);
		if (p != null)
		{
			checkValue(ShaderInputType.VECTOR_INT_2, p.left.getType());
			writer.setCursor(2 * index + p.right).put(value.getValuesArray());
			isDirty = true;
		}
	}
	
	public void set(String uniform, Vector3i value) throws VulkanException
	{
		set(uniform, 0, value);
	}
	
	public void set(String uniform, int index, Vector3i value) throws VulkanException
	{
		Pair<ShaderVariable, Integer> p = getOffset(uniform);
		if (p != null)
		{
			checkValue(ShaderInputType.VECTOR_INT_3, p.left.getType());
			writer.setCursor(4 * index + p.right).put(value.getValuesArray());
			isDirty = true;
		}
	}
	
	public void set(String uniform, Vector4i value) throws VulkanException
	{
		set(uniform, 0, value);
	}
	
	public void set(String uniform, int index, Vector4i value) throws VulkanException
	{
		Pair<ShaderVariable, Integer> p = getOffset(uniform);
		if (p != null)
		{
			checkValue(ShaderInputType.VECTOR_INT_4, p.left.getType());
			writer.setCursor(4 * index + p.right).put(value.getValuesArray());
			isDirty = true;
		}
	}
	
	public void set(String uniform, TextureView value, TextureSampler sampler)
	{
		Pair<ShaderVariable, Integer> p = getOffset(uniform);
		if (p != null)
		{
			checkValue(ShaderInputType.SAMPLER_2D, p.left.getType());
			lock.lock();
			try
			{
				VkDescriptorImageInfo.Buffer descriptorImageInfo = writeDescriptorSets.get(p.right).pImageInfo();
				descriptorImageInfo.imageView(value.getVk());
				descriptorImageInfo.sampler(sampler.getVk());
				isDirty = true;
			}
			finally
			{
				lock.unlock();
			}
		}
	}
	
	public void set(String uniform, int value) throws VulkanException
	{
		set(uniform, 0, value);
	}
	
	public void set(String uniform, int index, int value) throws VulkanException
	{
		Pair<ShaderVariable, Integer> p = getOffset(uniform);
		if (p != null)
		{
			checkValue(ShaderInputType.INT, p.left.getType());
			writer.setCursor(index + p.right).put(value);
			isDirty = true;
		}
	}
	
	public void set(String uniform, float value) throws VulkanException
	{
		set(uniform, 0, value);
	}
	
	public void set(String uniform, int index, float value) throws VulkanException
	{
		Pair<ShaderVariable, Integer> p = getOffset(uniform);
		if (p != null)
		{
			checkValue(ShaderInputType.FLOAT, p.left.getType());
			writer.setCursor(index + p.right).put(value);
			isDirty = true;
		}
	}
	
	public void flush() throws VulkanException
	{
		writer.flush();
	}
	
	public void flushAndInvalidate() throws VulkanException
	{
		writer.flushAndInvalidate();
	}
	
	public void transfer() throws VulkanException
	{
		writer.transfer();
	}
	
	public void transfer(CommandBuffer commandBuffer)
	{
		writer.transfer(commandBuffer);
	}
	
	private void checkValue(ShaderInputType required, ShaderInputType provided)
	{
		if (required != provided)
		{
			throw new IllegalArgumentException("Provided a " + provided.getName() + " value for a " + required.getName());
		}
	}
	
	@Override
	public void debug()
	{
		Debug.print(Debug.validity(writeDescriptorSets, VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET));
		Debug.print("dstSet: " + Debug.address(writeDescriptorSets.dstSet()));
		Debug.print("dstBinding: " + writeDescriptorSets.dstBinding());
		Debug.print("dstArrayElement: " + writeDescriptorSets.dstArrayElement());
		Debug.print("descriptorType: " + writeDescriptorSets.descriptorType());
		Debug.print("marks {");
		Debug.indent();
		for (Pair<ShaderVariable, Integer> p : marks.values().stream().sorted((p0, p1) -> Integer.compare(p0.r(), p1.r())).toList())
		{
			Debug.print(p.l() + " -> " + p.r());
		}
		Debug.deindent();
		Debug.print("}");
		VkDescriptorImageInfo.Buffer imageInfoBuffer = writeDescriptorSets.pImageInfo();
		if (imageInfoBuffer != null)
		{
			Debug.print("pImageInfo [");
			Debug.indent();
			for (VkDescriptorImageInfo imageInfo : imageInfoBuffer)
			{
				if (imageInfo.address() == 0)
				{
					Debug.print("null");
					continue;
				}
				Debug.print("Buffer [");
				Debug.indent();
				Debug.print("sampler: " + Debug.address(imageInfo.sampler()));
				Debug.print("view: " + Debug.address(imageInfo.imageView()));
				Debug.print("layout: " + imageInfo.imageLayout());
				Debug.deindent();
				Debug.print("]");
			}
			Debug.deindent();
			Debug.print("]");
		}
		VkDescriptorBufferInfo.Buffer bufferInfoBuffer = writeDescriptorSets.pBufferInfo();
		if (bufferInfoBuffer != null)
		{
			Debug.print("pBufferInfo [");
			Debug.indent();
			for (VkDescriptorBufferInfo bufferInfo : bufferInfoBuffer)
			{
				if (bufferInfo.address() == 0)
				{
					Debug.print("null");
					continue;
				}
				Debug.print("vk: " + Debug.address(bufferInfo.buffer()));
				Debug.print("offset: " + bufferInfo.offset());
				Debug.print("size: " + bufferInfo.range());
			}
			Debug.deindent();
			Debug.print("]");
		}
	}
}
