package com.alcadra.renderengine.renderer.object;

import java.util.function.Consumer;
import java.util.function.IntFunction;

import org.lwjgl.opengl.GL30;

import com.alcadra.math.Matrix4f;
import com.alcadra.math.vector.Vector2f;
import com.alcadra.math.vector.Vector2i;
import com.alcadra.math.vector.Vector3f;
import com.alcadra.math.vector.Vector3i;
import com.alcadra.math.vector.Vector4f;
import com.alcadra.math.vector.Vector4i;
import com.alcadra.renderengine.buffer.Bufferable;
import com.alcadra.renderengine.buffer.ObjectArrayBuffer;
import com.alcadra.renderengine.renderer.BaseRenderer;
import com.alcadra.renderengine.renderer.ShaderRenderer;
import com.alcadra.renderengine.renderer.VertexArray;
import com.alcadra.renderengine.renderer.VertexBuilder;
import com.alcadra.renderengine.shader.Shader;
import com.alcadra.renderengine.textures.ColorTexture;
import com.alcadra.renderengine.textures.Texture;
import com.alcadra.renderengine.util.Tracker;

public class ObjectRenderer extends BaseRenderer<ObjectArrayBuffer<Object>> implements ShaderRenderer, VertexBuilder<ObjectRenderer> {
	
	private Vector4i bindedColor;
	private Vector2i bindedUV;
	private Vector3i bindedNormal;
	private ColorTexture texture;
	private int offset;
	private Bufferable buildingValue;
	private int vertexSize;
	
	public ObjectRenderer(Shader shader)
	{
		super(shader, new VertexArray(), new ObjectArrayBuffer<>());
		initializeArrayBuffer();
		bindedColor = Vector4i.getOrigin();
		bindedUV = Vector2i.getOrigin();
		bindedNormal = Vector3i.getOrigin();
		texture = ColorTexture.getNull();
	}
	
	@Override
	protected void initializeArrayBuffer()
	{
		vertexArray.bindBuffer(arrayBuffer);
		vertexSize = 12 + (shader.useColor() ? 16 : 0) + (shader.useUV() ? 8 : 0) + (shader.useNormal() ? 12 : 0);
		int offset = 0;
		vertexArray.vertexAttributePointer(0, 3, GL30.GL_FLOAT, false, vertexSize, offset);
		vertexArray.enableVertexAttribute(0);
		offset += 12;
		if (shader.useColor())
		{
			vertexArray.vertexAttributePointer(1, 4, GL30.GL_FLOAT, false, vertexSize, offset);
			vertexArray.enableVertexAttribute(1);
			offset += 16;
		}
		if (shader.useUV())
		{
			vertexArray.vertexAttributePointer(2, 2, GL30.GL_FLOAT, false, vertexSize, offset);
			vertexArray.enableVertexAttribute(2);
			offset += 8;
		}
		if (shader.useNormal())
		{
			vertexArray.vertexAttributePointer(3, 3, GL30.GL_FLOAT, false, vertexSize, offset);
			vertexArray.enableVertexAttribute(3);
			offset += 12;
		}
		VertexArray.unbind();
	}
	
	@Override
	protected void disableAttributes()
	{
		vertexArray.bind();
		if (shader.useColor()) vertexArray.disableVertexAttribute(1);
		if (shader.useUV()) vertexArray.disableVertexAttribute(2);
		if (shader.useNormal()) vertexArray.disableVertexAttribute(3);
	}
	
	public void loadTexture(ColorTexture texture)
	{
		this.texture = texture;
	}
	
	public ObjectBuilder start(Bufferable value)
	{
		buildingValue = value;
		return new ObjectBuilder(this, bindedColor, bindedUV, bindedNormal);
	}
	
	public void startIfAbsent(Bufferable value, Consumer<ObjectBuilder> consumer)
	{
		if (!arrayBuffer.contains(value))
		{
			buildingValue = value;
			consumer.accept(new ObjectBuilder(this));
		}
	}
	
	ObjectBuilder start(Bufferable value, ObjectBuilder builder)
	{
		buildingValue = value;
		return builder;
	}
	
	void startIfAbsent(Bufferable value, ObjectBuilder builder, Consumer<ObjectBuilder> consumer)
	{
		if (!arrayBuffer.contains(value))
		{
			buildingValue = value;
			consumer.accept(builder);
		}
	}
	
	public <B extends Bufferable> Tracker<B> end(ObjectBuilder builder)
	{
		return new Tracker0<>(arrayBuffer.add(buildingValue, buildData(builder::getModifier, buildingValue, builder.availableModifier())), builder::getModifier, builder.availableModifier());
	}
	
	private int[] buildData(IntFunction<VertexModifier> modifiers, Bufferable bufferable, int available)
	{
		int vertices = bufferable.vertices();
		try
		{
			int[] buildedData = new int[vertexSize / 4 * vertices];
			int[][] data = bufferable.getData();
			for (int i = 0; i < vertices; i++)
			{
				int[] vertexData = bufferable.getVertexData(i, modifiers.apply(i), data[i]);
				for (int j = 0; j < vertexData.length; j++) buildedData[i * vertexData.length + j] = vertexData[j];
			}
			return buildedData;
		}
		catch (IndexOutOfBoundsException e)
		{
			throw new InsufficientVertexDataException("Required " + vertices + " modifiers, available " + available, e);
		}
	}
	
	public void enable(int index)
	{
		arrayBuffer.enable(index);
	}
	
	public void disable(int index)
	{
		arrayBuffer.disable(index);
	}
	
	@Override
	public ObjectRenderer color(Vector4f color)
	{
		bindedColor = new Vector4i(Float.floatToIntBits(color.x() / 255), Float.floatToIntBits(color.y() / 255), Float.floatToIntBits(color.z() / 255), Float.floatToIntBits(color.w() / 255));
		return this;
	}
	
	@Override
	public ObjectRenderer uv(Vector2f uv)
	{
		bindedUV = new Vector2i(Float.floatToIntBits(uv.x()), Float.floatToIntBits(uv.y()));
		return this;
	}
	
	@Override
	public ObjectRenderer normal(Vector3f normal)
	{
		bindedNormal = new Vector3i(Float.floatToIntBits(normal.x()), Float.floatToIntBits(normal.y()), Float.floatToIntBits(normal.z()));
		return this;
	}
	
	public void draw(int mode, Matrix4f matrix)
	{
		if (shader.useUV())
		{
			Texture.setActive(0);
			texture.bind();
		}
		if (shader != null)
		{
			shader.enable();
			shader.setUniform(matrix, "transformation");
			if (shader.hasUniformColor()) shader.setUniformFloat(bindedColor, "color");
			if (shader.hasUniformUV()) shader.setUniformFloat(bindedUV, "uv");
			if (shader.hasUniformNormal()) shader.setUniformFloat(bindedNormal, "normal");
			if (shader.useUV() || shader.hasUniformUV()) shader.setUniform(0, "textureSampler");
		}
		draw(mode, arrayBuffer.intSize());
		Shader.disableShaders();
	}
	
	public void reset()
	{
		bindedColor = Vector4i.getOrigin();
		bindedUV = Vector2i.getOrigin();
		bindedNormal = Vector3i.getOrigin();
		clear();
	}
	
	@Override
	public void clear()
	{
		arrayBuffer.clear();
		offset = 0;
	}
	
	public void merge(ObjectRenderer renderer)
	{
		arrayBuffer.merge(renderer.arrayBuffer);
		offset += renderer.getOffset();
	}
	
	public int getOffset()
	{
		return offset;
	}
	
	@Override
	public String toString()
	{
		return "ObjectRenderer{" + arrayBuffer + "}";
	}
	
	private class Tracker0<B extends Bufferable> implements Tracker<B> {
		
		private Tracker<int[]> tracker;
		private IntFunction<VertexModifier> modifiers;
		private int available;
		
		public Tracker0(Tracker<int[]> tracker, IntFunction<VertexModifier> modifiers, int available)
		{
			this.tracker = tracker;
			this.modifiers = modifiers;
			this.available = available;
		}
		
		@Override
		public void modify(B elementData)
		{
			tracker.modify(buildData(modifiers, elementData, available));
		}
		
		@Override
		public boolean turnOn()
		{
			return tracker.turnOn();
		}
		
		@Override
		public boolean turnOff()
		{
			return tracker.turnOff();
		}
		
		@Override
		public void remove()
		{
			tracker.remove();
		}
		
		@Override
		public boolean isOn()
		{
			return tracker.isOn();
		}
		
		@Override
		public boolean isOff()
		{
			return !tracker.isOff();
		}
		
		@Override
		public boolean isValid()
		{
			return tracker.isValid();
		}
	}
}
