package com.alcadrial.renderengine.render.buffer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;

import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

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
import com.alcadrial.renderengine.render.shader.ShaderInputType;
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.texture.TextureSampler;
import com.alcadrial.renderengine.render.texture.TextureView;
import com.alcadrial.renderengine.render.util.VulkanDebug;

public class BufferLayout implements VulkanDebug {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private Lock lock;
	private Device device;
	private ConcurrentHashMap<String, Pair<ShaderVariable, Integer>> marks;
	private VkWriteDescriptorSet.Buffer writeDescriptorSets;
	private int stride;
	private boolean updated;
	
	public BufferLayout(Device device, VkWriteDescriptorSet.Buffer writeDescriptorSets, int stride, Lock lock)
	{
		this.device = device;
		marks = new ConcurrentHashMap<>();
		this.writeDescriptorSets = writeDescriptorSets;
		this.stride = stride;
		this.lock = lock;
		updated = true;
	}
	
	public BufferLayout(BufferLayout other)
	{
		device = other.device;
		marks = new ConcurrentHashMap<>(other.marks);
		writeDescriptorSets = other.writeDescriptorSets;
		stride = other.stride;
		lock = other.lock;
		updated = true;
	}
	
	public void mark(ShaderVariable variable, int offset)
	{
		marks.put(variable.getName(), new Pair<>(variable, offset));
	}
	
	public boolean isPresent(ShaderVariable variable)
	{
		return marks.containsKey(variable.getName());
	}
	
	public Pair<ShaderVariable, Integer> getOffset(String variableName)
	{
		return marks.get(variableName);
	}
	
	public void updateDescriptorSets()
	{
		lock.lock();
		try
		{
			if (updated)
			{
				VK10.vkUpdateDescriptorSets(device.getVk(), writeDescriptorSets, null);
				updated = false;
			}
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
			action.accept(writeDescriptorSets);
			VK10.vkUpdateDescriptorSets(device.getVk(), writeDescriptorSets, null);
			updated = false;
		}
		finally
		{
			lock.unlock();
		}
	}
	
	public void consume(Consumer<VkWriteDescriptorSet.Buffer> action)
	{
		lock.lock();
		try
		{
			if (updated)
			{
				action.accept(writeDescriptorSets);
				updated = false;
			}
		}
		finally
		{
			lock.unlock();
		}
	}
	
	public void set(String uniform, VulkanBuffer buffer, Matrix4f value)
	{
		set(uniform, buffer, 0, value);
	}
	
	public void set(String uniform, VulkanBuffer buffer, int index, Matrix4f value)
	{
		Pair<ShaderVariable, Integer> p = getOffset(uniform);
		if (p != null)
		{
			checkValue(ShaderInputType.MATRIX_FLOAT_4X4, p.left.getType());
			buffer.asFloatBuffer().put(index * stride + p.right, value.toArray());
			updated = true;
		}
	}
	
	public void set(String uniform, VulkanBuffer buffer, Vector2f value)
	{
		set(uniform, buffer, 0, value);
	}
	
	public void set(String uniform, VulkanBuffer buffer, int index, Vector2f value)
	{
		Pair<ShaderVariable, Integer> p = getOffset(uniform);
		if (p != null)
		{
			checkValue(ShaderInputType.VECTOR_FLOAT_2, p.left.getType());
			buffer.asFloatBuffer().put(index * stride + p.right, value.getValuesArray());
			updated = true;
		}
	}
	
	public void set(String uniform, VulkanBuffer buffer, Vector3f value)
	{
		set(uniform, buffer, 0, value);
	}
	
	public void set(String uniform, VulkanBuffer buffer, int index, Vector3f value)
	{
		Pair<ShaderVariable, Integer> p = getOffset(uniform);
		if (p != null)
		{
			checkValue(ShaderInputType.VECTOR_FLOAT_3, p.left.getType());
			buffer.asFloatBuffer().put(index * stride + p.right, value.getValuesArray());
			updated = true;
		}
	}
	
	public void set(String uniform, VulkanBuffer buffer, Vector4f value)
	{
		set(uniform, buffer, 0, value);
	}
	
	public void set(String uniform, VulkanBuffer buffer, int index, Vector4f value)
	{
		Pair<ShaderVariable, Integer> p = getOffset(uniform);
		if (p != null)
		{
			checkValue(ShaderInputType.VECTOR_FLOAT_4, p.left.getType());
			buffer.asFloatBuffer().put(index * stride + p.right, value.getValuesArray());
			updated = true;
		}
	}
	
	public void set(String uniform, VulkanBuffer buffer, Vector2i value)
	{
		set(uniform, buffer, 0, value);
	}
	
	public void set(String uniform, VulkanBuffer buffer, int index, Vector2i value)
	{
		Pair<ShaderVariable, Integer> p = getOffset(uniform);
		if (p != null)
		{
			checkValue(ShaderInputType.VECTOR_INT_2, p.left.getType());
			buffer.asIntBuffer().put(index * stride + p.right, value.getValuesArray());
			updated = true;
		}
	}
	
	public void set(String uniform, VulkanBuffer buffer, Vector3i value)
	{
		set(uniform, buffer, 0, value);
	}
	
	public void set(String uniform, VulkanBuffer buffer, int index, Vector3i value)
	{
		Pair<ShaderVariable, Integer> p = getOffset(uniform);
		if (p != null)
		{
			checkValue(ShaderInputType.VECTOR_INT_3, p.left.getType());
			buffer.asIntBuffer().put(index * stride + p.right, value.getValuesArray());
			updated = true;
		}
	}
	
	public void set(String uniform, VulkanBuffer buffer, Vector4i value)
	{
		set(uniform, buffer, 0, value);
	}
	
	public void set(String uniform, VulkanBuffer buffer, int index, Vector4i value)
	{
		Pair<ShaderVariable, Integer> p = getOffset(uniform);
		if (p != null)
		{
			checkValue(ShaderInputType.VECTOR_INT_4, p.left.getType());
			buffer.asIntBuffer().put(index * stride + p.right, value.getValuesArray());
			updated = true;
		}
	}
	
	public void set(String uniform, TextureView value, TextureSampler sampler)
	{
		set(uniform, 0, value, sampler);
	}
	
	@SuppressWarnings("null")
	public void set(String uniform, int index, TextureView value, TextureSampler sampler)
	{
		Pair<ShaderVariable, Integer> p = getOffset(uniform);
		if (p != null)
		{
			checkValue(ShaderInputType.SAMPLER_2D, p.left.getType());
			lock.lock();
			try
			{
				VkDescriptorImageInfo.Buffer descriptorImageInfo = writeDescriptorSets.get(index * stride + p.right).pImageInfo();
				descriptorImageInfo.imageView(value.getVk());
				descriptorImageInfo.sampler(sampler.getVk());
				updated = true;
			}
			finally
			{
				lock.unlock();
			}
		}
	}
	
	public void set(String uniform, VulkanBuffer buffer, int value)
	{
		set(uniform, buffer, 0, value);
	}
	
	public void set(String uniform, VulkanBuffer buffer, int index, int value)
	{
		Pair<ShaderVariable, Integer> p = getOffset(uniform);
		if (p != null)
		{
			checkValue(ShaderInputType.INT, p.left.getType());
			buffer.asIntBuffer().put(index * stride + p.right, value);
			updated = true;
		}
	}
	
	public void set(String uniform, VulkanBuffer buffer, float value)
	{
		set(uniform, buffer, 0, value);
	}
	
	public void set(String uniform, VulkanBuffer buffer, int index, float value)
	{
		Pair<ShaderVariable, Integer> p = getOffset(uniform);
		if (p != null)
		{
			checkValue(ShaderInputType.FLOAT, p.left.getType());
			buffer.asFloatBuffer().put(index * stride + p.right, value);
			updated = true;
		}
	}
	
	private void checkValue(ShaderInputType required, ShaderInputType provided)
	{
		if (required != provided) throw new IllegalArgumentException("Provided a " + provided.getName() + " value for a " + required.getName());
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
