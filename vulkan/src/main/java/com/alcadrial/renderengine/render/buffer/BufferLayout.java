package com.alcadrial.renderengine.render.buffer;

import java.util.HashMap;

import org.lwjgl.vulkan.VK10;
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
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.shader.ShaderInputType;
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.texture.TextureSampler;
import com.alcadrial.renderengine.render.texture.TextureView;

public class BufferLayout {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private Device device;
	private HashMap<String, Pair<ShaderVariable, Integer>> marks;
	private VkWriteDescriptorSet.Buffer writeDescriptorSets;
	private int stride;
	
	public BufferLayout(Device device, VkWriteDescriptorSet.Buffer writeDescriptorSets, int stride)
	{
		this.device = device;
		marks = new HashMap<>();
		this.writeDescriptorSets = writeDescriptorSets;
		this.stride = stride;
	}
	
	public BufferLayout(BufferLayout other)
	{
		device = other.device;
		marks = new HashMap<>(other.marks);
		writeDescriptorSets = other.writeDescriptorSets;
		stride = other.stride;
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
		VK10.vkUpdateDescriptorSets(device.getVk(), writeDescriptorSets, null);
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
			checkUniformValue(ShaderInputType.MATRIX_FLOAT_4X4, p.left.getType());
			buffer.asFloatBuffer().put(index * stride + p.right, value.toArray());
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
			checkUniformValue(ShaderInputType.VECTOR_FLOAT_2, p.left.getType());
			buffer.asFloatBuffer().put(index * stride + p.right, value.getValuesArray());
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
			checkUniformValue(ShaderInputType.VECTOR_FLOAT_3, p.left.getType());
			buffer.asFloatBuffer().put(index * stride + p.right, value.getValuesArray());
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
			checkUniformValue(ShaderInputType.VECTOR_FLOAT_4, p.left.getType());
			buffer.asFloatBuffer().put(index * stride + p.right, value.getValuesArray());
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
			checkUniformValue(ShaderInputType.VECTOR_INT_2, p.left.getType());
			buffer.asIntBuffer().put(index * stride + p.right, value.getValuesArray());
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
			checkUniformValue(ShaderInputType.VECTOR_INT_3, p.left.getType());
			buffer.asIntBuffer().put(index * stride + p.right, value.getValuesArray());
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
			checkUniformValue(ShaderInputType.VECTOR_INT_4, p.left.getType());
			buffer.asIntBuffer().put(index * stride + p.right, value.getValuesArray());
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
			checkUniformValue(ShaderInputType.SAMPLER_2D, p.left.getType());
			VkDescriptorImageInfo.Buffer descriptorImageInfo = writeDescriptorSets.get(index * stride + p.right).pImageInfo();
			descriptorImageInfo.imageView(value.getVk());
			descriptorImageInfo.sampler(sampler.getVk());
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
			checkUniformValue(ShaderInputType.INT, p.left.getType());
			buffer.asIntBuffer().put(index * stride + p.right, value);
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
			checkUniformValue(ShaderInputType.FLOAT, p.left.getType());
			buffer.asFloatBuffer().put(index * stride + p.right, value);
		}
	}
	
	private void checkUniformValue(ShaderInputType required, ShaderInputType provided)
	{
		if (required != provided) throw new IllegalArgumentException("Provided a " + provided.getName() + " value for a " + required.getName() + " uniform");
	}
}
