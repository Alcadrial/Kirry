package com.alcadrial.renderengine.render.buffer;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.kector.vector.Vector3i;
import com.alcadrial.kector.vector.Vector4f;
import com.alcadrial.kector.vector.Vector4i;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.shader.ShaderInputType;
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public class BufferLayoutWriter implements AutoCloseable {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private final BufferLayout layout;
	private final BufferWriter writer;
	
	public BufferLayoutWriter(VulkanBuffer buffer, BufferLayout layout) throws VulkanException
	{
		this.layout = layout;
		writer = new BufferWriter(buffer);
		writer.setPreferFlush(true);
	}
	
	public BufferLayout getLayout()
	{
		return layout;
	}
	
	public BufferWriter getWriter()
	{
		return writer;
	}
	
	public void set(ShaderVariable variable, Matrix4f value) throws VulkanException
	{
		set(variable, 0, value);
	}
	
	public void set(ShaderVariable variable, int index, Matrix4f value) throws VulkanException
	{
		Integer offset = layout.getOffset(variable);
		if (offset != null)
		{
			checkValue(ShaderInputType.MATRIX_FLOAT_4X4, variable.getType());
			writer.setCursor(16 * index + offset).put(value.toTransposedArray());
		}
	}
	
	public void set(ShaderVariable variable, Vector2f value) throws VulkanException
	{
		set(variable, 0, value);
	}
	
	public void set(ShaderVariable variable, int index, Vector2f value) throws VulkanException
	{
		Integer offset = layout.getOffset(variable);
		if (offset != null)
		{
			checkValue(ShaderInputType.VECTOR_FLOAT_2, variable.getType());
			writer.setCursor(2 * index + offset).put(value.getValuesArray());
		}
	}
	
	public void set(ShaderVariable variable, Vector3f value) throws VulkanException
	{
		set(variable, 0, value);
	}
	
	public void set(ShaderVariable variable, int index, Vector3f value) throws VulkanException
	{
		Integer offset = layout.getOffset(variable);
		if (offset != null)
		{
			checkValue(ShaderInputType.VECTOR_FLOAT_3, variable.getType());
			writer.setCursor(4 * index + offset).put(value.getValuesArray());
		}
	}
	
	public void set(ShaderVariable variable, Vector4f value) throws VulkanException
	{
		set(variable, 0, value);
	}
	
	public void set(ShaderVariable variable, int index, Vector4f value) throws VulkanException
	{
		Integer offset = layout.getOffset(variable);
		if (offset != null)
		{
			checkValue(ShaderInputType.VECTOR_FLOAT_4, variable.getType());
			writer.setCursor(4 * index + offset).put(value.getValuesArray());
		}
	}
	
	public void set(ShaderVariable variable, Vector2i value) throws VulkanException
	{
		set(variable, 0, value);
	}
	
	public void set(ShaderVariable variable, int index, Vector2i value) throws VulkanException
	{
		Integer offset = layout.getOffset(variable);
		if (offset != null)
		{
			checkValue(ShaderInputType.VECTOR_INT_2, variable.getType());
			writer.setCursor(2 * index + offset).put(value.getValuesArray());
		}
	}
	
	public void set(ShaderVariable variable, Vector3i value) throws VulkanException
	{
		set(variable, 0, value);
	}
	
	public void set(ShaderVariable variable, int index, Vector3i value) throws VulkanException
	{
		Integer offset = layout.getOffset(variable);
		if (offset != null)
		{
			checkValue(ShaderInputType.VECTOR_INT_3, variable.getType());
			writer.setCursor(4 * index + offset).put(value.getValuesArray());
		}
	}
	
	public void set(ShaderVariable variable, Vector4i value) throws VulkanException
	{
		set(variable, 0, value);
	}
	
	public void set(ShaderVariable variable, int index, Vector4i value) throws VulkanException
	{
		Integer offset = layout.getOffset(variable);
		if (offset != null)
		{
			checkValue(ShaderInputType.VECTOR_INT_4, variable.getType());
			writer.setCursor(4 * index + offset).put(value.getValuesArray());
		}
	}
	
	public void set(ShaderVariable variable, int value) throws VulkanException
	{
		set(variable, 0, value);
	}
	
	public void set(ShaderVariable variable, int index, int value) throws VulkanException
	{
		Integer offset = layout.getOffset(variable);
		if (offset != null)
		{
			checkValue(ShaderInputType.INT, variable.getType());
			writer.setCursor(index + offset).put(value);
		}
	}
	
	public void set(ShaderVariable variable, float value) throws VulkanException
	{
		set(variable, 0, value);
	}
	
	public void set(ShaderVariable variable, int index, float value) throws VulkanException
	{
		Integer offset = layout.getOffset(variable);
		if (offset != null)
		{
			checkValue(ShaderInputType.FLOAT, variable.getType());
			writer.setCursor(index + offset).put(value);
		}
	}
	
	private void checkValue(ShaderInputType required, ShaderInputType provided)
	{
		if (required != provided)
		{
			throw new IllegalArgumentException("Provided a " + provided.getName() + " value for a " + required.getName());
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
	
	@Override
	public void close() throws VulkanException
	{
		writer.close();
	}
}
