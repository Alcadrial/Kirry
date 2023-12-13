package com.alcadrial.renderengine.render.buffer;

import java.util.HashMap;
import java.util.Map;

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
import com.alcadrial.renderengine.render.shader.ShaderUniform;
import com.alcadrial.renderengine.render.texture.TextureSampler;
import com.alcadrial.renderengine.render.texture.TextureView;

public class UniformBuffer extends VulkanBuffer {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private Map<String, Pair<ShaderUniform, Integer>> marks;
	private VkWriteDescriptorSet.Buffer writeDescriptorSets;
	
	public UniformBuffer(VkWriteDescriptorSet.Buffer writeDescriptorSets, long size)
	{
		this(RenderEngine.getVulkan().getDevice(), writeDescriptorSets, size);
	}
	
	public UniformBuffer(Device device, VkWriteDescriptorSet.Buffer writeDescriptorSets, long size)
	{
		super(device, size, VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT);
		marks = new HashMap<>();
		this.writeDescriptorSets = writeDescriptorSets;
	}
	
	public void mark(ShaderUniform uniform, int offset)
	{
		marks.put(uniform.getName(), new Pair<>(uniform, offset));
	}
	
	public boolean isPresent(String uniform)
	{
		return marks.containsKey(uniform);
	}
	
	public void updateDescriptorSets()
	{
		VK10.vkUpdateDescriptorSets(device.getVk(), writeDescriptorSets, null);
	}
	
	public void set(String uniform, Matrix4f value)
	{
		Pair<ShaderUniform, Integer> p = marks.get(uniform);
		if (p != null)
		{
			checkUniformValue(ShaderInputType.MATRIX_FLOAT_4X4, p.l().getType());
			asFloatBuffer().put(p.r(), value.toArray());
		}
	}
	
	public void set(String uniform, Vector2f value)
	{
		Pair<ShaderUniform, Integer> p = marks.get(uniform);
		if (p != null)
		{
			checkUniformValue(ShaderInputType.VECTOR_FLOAT_2, p.l().getType());
			asFloatBuffer().put(p.r(), value.getValuesArray());
		}
	}
	
	public void set(String uniform, Vector3f value)
	{
		Pair<ShaderUniform, Integer> p = marks.get(uniform);
		if (p != null)
		{
			checkUniformValue(ShaderInputType.VECTOR_FLOAT_3, p.l().getType());
			asFloatBuffer().put(p.r(), value.getValuesArray());
		}
	}
	
	public void set(String uniform, Vector4f value)
	{
		Pair<ShaderUniform, Integer> p = marks.get(uniform);
		if (p != null)
		{
			checkUniformValue(ShaderInputType.VECTOR_FLOAT_4, p.l().getType());
			asFloatBuffer().put(p.r(), value.getValuesArray());
		}
	}
	
	public void set(String uniform, Vector2i value)
	{
		Pair<ShaderUniform, Integer> p = marks.get(uniform);
		if (p != null)
		{
			checkUniformValue(ShaderInputType.VECTOR_INT_2, p.l().getType());
			asIntBuffer().put(p.r(), value.getValuesArray());
		}
	}
	
	public void set(String uniform, Vector3i value)
	{
		Pair<ShaderUniform, Integer> p = marks.get(uniform);
		if (p != null)
		{
			checkUniformValue(ShaderInputType.VECTOR_INT_3, p.l().getType());
			asIntBuffer().put(p.r(), value.getValuesArray());
		}
	}
	
	public void set(String uniform, Vector4i value)
	{
		Pair<ShaderUniform, Integer> p = marks.get(uniform);
		if (p != null)
		{
			checkUniformValue(ShaderInputType.VECTOR_INT_4, p.l().getType());
			asIntBuffer().put(p.r(), value.getValuesArray());
		}
	}
	
	@SuppressWarnings({"null"})
	public void set(String uniform, TextureView value, TextureSampler sampler)
	{
		Pair<ShaderUniform, Integer> p = marks.get(uniform);
		if (p != null)
		{
			checkUniformValue(ShaderInputType.SAMPLER_2D, p.l().getType());
			VkDescriptorImageInfo.Buffer descriptorImageInfo = writeDescriptorSets.get(p.r()).pImageInfo();
			descriptorImageInfo.imageView(value.getVk());
			descriptorImageInfo.sampler(sampler.getVk());
		}
	}
	
	public void set(String uniform, int value)
	{
		Pair<ShaderUniform, Integer> p = marks.get(uniform);
		if (p != null)
		{
			checkUniformValue(ShaderInputType.INT, p.l().getType());
			asIntBuffer().put(p.r(), value);
		}
	}
	
	public void set(String uniform, float value)
	{
		Pair<ShaderUniform, Integer> p = marks.get(uniform);
		if (p != null)
		{
			checkUniformValue(ShaderInputType.FLOAT, p.l().getType());
			asFloatBuffer().put(p.r(), value);
		}
	}
	
	private void checkUniformValue(ShaderInputType required, ShaderInputType provided)
	{
		if (required != provided) throw new IllegalArgumentException("Provided a " + provided.getName() + " value for a " + required.getName() + " uniform");
	}
}
