package com.alcadra.renderengine.renderer.object;

import java.util.ArrayList;
import java.util.List;

import com.alcadra.math.vector.Vector2f;
import com.alcadra.math.vector.Vector2i;
import com.alcadra.math.vector.Vector3f;
import com.alcadra.math.vector.Vector3i;
import com.alcadra.math.vector.Vector4f;
import com.alcadra.math.vector.Vector4i;
import com.alcadra.renderengine.buffer.Bufferable;
import com.alcadra.renderengine.renderer.VertexBuilder;
import com.alcadra.renderengine.shader.Shader;

public class ObjectBuilder {
	
	private ObjectRenderer renderer;
	private List<VertexModifier> modifiers;
	
	public ObjectBuilder(ObjectRenderer renderer)
	{
		this.renderer = renderer;
		modifiers = new ArrayList<>();
	}
	
	public Shader getShader()
	{
		return renderer.getShader();
	}
	
	public VertexModifier getModifier(int index)
	{
		return modifiers.get(index);
	}
	
	public int availableModifier()
	{
		return modifiers.size();
	}
	
	public Builder vertex()
	{
		return new Builder();
	}
	
	public int end()
	{
		int id = renderer.end(this);
		modifiers.clear();
		return id;
	}
	
	public void next(Bufferable value)
	{
		renderer.end(this);
		renderer.start(value, this);
	}
	
	public class Builder implements VertexBuilder<Builder> {
		
		private Vector4i bindedColor = Vector4i.getOrigin();
		private Vector2i bindedUV = Vector2i.getOrigin();
		private Vector3i bindedNormal = Vector3i.getOrigin();
		private boolean building;
		
		private Builder()
		{
			building = true;
		}
		
		public ObjectBuilder end()
		{
			if (building)
			{
				modifiers.add(new VertexModifier(bindedColor, bindedUV, bindedNormal, getShader()));
				building = false;
			}
			return ObjectBuilder.this;
		}
		
		public Builder next()
		{
			if (!building) throw new IllegalBuildingState("Cannot build more vertex after #end()!");
			modifiers.add(new VertexModifier(bindedColor, bindedUV, bindedNormal, getShader()));
			return this;
		}
		
		@Override
		public Builder color(Vector4f color)
		{
			bindedColor = new Vector4i(Float.floatToIntBits(color.x()), Float.floatToIntBits(color.y()), Float.floatToIntBits(color.z()), Float.floatToIntBits(color.w()));
			return this;
		}
		
		@Override
		public Builder uv(Vector2f uv)
		{
			bindedUV = new Vector2i(Float.floatToIntBits(uv.x()), Float.floatToIntBits(uv.y()));
			return this;
		}
		
		@Override
		public Builder normal(Vector3f normal)
		{
			bindedNormal = new Vector3i(Float.floatToIntBits(normal.x()), Float.floatToIntBits(normal.y()), Float.floatToIntBits(normal.z()));
			return this;
		}
	}
}
