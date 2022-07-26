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
import com.alcadra.renderengine.util.Tracker;

public class ObjectBuilder {
	
	private Vector4i bindedColor;
	private Vector2i bindedUV;
	private Vector3i bindedNormal;
	private ObjectRenderer renderer;
	private List<VertexModifier> modifiers;
	
	public ObjectBuilder(ObjectRenderer renderer)
	{
		this.renderer = renderer;
		modifiers = new ArrayList<>();
		bindedColor = Vector4i.getOrigin();
		bindedUV = Vector2i.getOrigin();
		bindedNormal = Vector3i.getOrigin();
	}
	
	public ObjectBuilder(ObjectRenderer renderer, Vector4i bindedColor, Vector2i bindedUV, Vector3i bindedNormal)
	{
		this.renderer = renderer;
		modifiers = new ArrayList<>();
		this.bindedColor = bindedColor;
		this.bindedUV = bindedUV;
		this.bindedNormal = bindedNormal;
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
	
	public <B extends Bufferable> Tracker<B> end()
	{
		Tracker<B> tracker = renderer.end(this);
		modifiers.clear();
		return tracker;
	}
	
	public void next(Bufferable value)
	{
		renderer.end(this);
		renderer.start(value, this);
	}
	
	public class Builder implements VertexBuilder<Builder> {
		
		private Vector4i bindedColor;
		private Vector2i bindedUV;
		private Vector3i bindedNormal;
		private boolean building;
		
		private Builder()
		{
			building = true;
			bindedColor = ObjectBuilder.this.bindedColor;
			bindedUV = ObjectBuilder.this.bindedUV;
			bindedNormal = ObjectBuilder.this.bindedNormal;
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
			bindedColor = new Vector4i(Float.floatToIntBits(color.x() / 255), Float.floatToIntBits(color.y() / 255), Float.floatToIntBits(color.z() / 255), Float.floatToIntBits(color.w() / 255));
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
	
	List<VertexModifier> getModifiers()
	{
		return List.copyOf(modifiers);
	}
}
