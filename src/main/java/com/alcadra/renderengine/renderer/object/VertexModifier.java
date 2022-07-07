package com.alcadra.renderengine.renderer.object;

import com.alcadra.math.vector.Vector2f;
import com.alcadra.math.vector.Vector2i;
import com.alcadra.math.vector.Vector3f;
import com.alcadra.math.vector.Vector3i;
import com.alcadra.math.vector.Vector4f;
import com.alcadra.math.vector.Vector4i;
import com.alcadra.renderengine.renderer.VertexBuilder;
import com.alcadra.renderengine.shader.Shader;

public class VertexModifier implements VertexBuilder<VertexModifier> {
	
	private Vector4i bindedColor;
	private Vector2i bindedUV;
	private Vector3i bindedNormal;
	private Shader shader;
	
	public VertexModifier(Vector4i bindedColor, Vector2i bindedUV, Vector3i bindedNormal, Shader shader)
	{
		this.bindedColor = bindedColor;
		this.bindedUV = bindedUV;
		this.bindedNormal = bindedNormal;
		this.shader = shader;
	}
	
	public int[] insertModifier(int[] data, int from)
	{
		int i = from;
		if (shader.useColor())
		{
			data[i] = bindedColor.x();
			data[i + 1] = bindedColor.y();
			data[i + 2] = bindedColor.z();
			data[i + 3] = bindedColor.w();
			i += 4;
		}
		if (shader.useUV())
		{
			data[i] = bindedUV.x();
			data[i + 1] = bindedUV.y();
			i += 2;
		}
		if (shader.useNormal())
		{
			data[i] = bindedNormal.x();
			data[i + 1] = bindedNormal.y();
			data[i + 2] = bindedNormal.z();
		}
		return data;
	}
	
	public int getModifierSize()
	{
		return (shader.useColor() ? 4 : 0) + (shader.useUV() ? 2 : 0) + (shader.useNormal() ? 3 : 0);
	}
	
	@Override
	public VertexModifier color(Vector4f color)
	{
		bindedColor = new Vector4i(Float.floatToIntBits(color.x()), Float.floatToIntBits(color.y()), Float.floatToIntBits(color.z()), Float.floatToIntBits(color.w()));
		return this;
	}
	
	@Override
	public VertexModifier uv(Vector2f uv)
	{
		bindedUV = new Vector2i(Float.floatToIntBits(uv.x()), Float.floatToIntBits(uv.y()));
		return this;
	}
	
	@Override
	public VertexModifier normal(Vector3f normal)
	{
		bindedNormal = new Vector3i(Float.floatToIntBits(normal.x()), Float.floatToIntBits(normal.y()), Float.floatToIntBits(normal.z()));
		return this;
	}
}
