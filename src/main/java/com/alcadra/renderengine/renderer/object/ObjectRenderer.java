package com.alcadra.renderengine.renderer.object;

import org.lwjgl.opengl.GL30;

import com.alcadra.math.Matrix4f;
import com.alcadra.math.vector.Vector2f;
import com.alcadra.math.vector.Vector3f;
import com.alcadra.math.vector.Vector4f;
import com.alcadra.renderengine.buffer.Bufferable;
import com.alcadra.renderengine.buffer.ElementArrayBuffer;
import com.alcadra.renderengine.buffer.ObjectArrayBuffer;
import com.alcadra.renderengine.renderer.BaseRenderer;
import com.alcadra.renderengine.renderer.ShaderRenderer;
import com.alcadra.renderengine.renderer.VertexArray;
import com.alcadra.renderengine.renderer.VertexBuilder;
import com.alcadra.renderengine.shader.Shader;
import com.alcadra.renderengine.textures.ColorTexture;
import com.alcadra.renderengine.textures.Texture;

public class ObjectRenderer extends BaseRenderer<ObjectArrayBuffer<Object>> implements ShaderRenderer, VertexBuilder<ObjectRenderer> {
	
	private Vector4f bindedColor = Vector4f.getOrigin();
	private Vector2f bindedUV = Vector2f.getOrigin();
	private Vector3f bindedNormal = Vector3f.getOrigin();
	private ColorTexture texture = ColorTexture.getNull();
	private int offset;
	private Bufferable buildingValue;
	private int vertexSize;
	
	public ObjectRenderer(Shader shader)
	{
		super(shader, new VertexArray(), new ObjectArrayBuffer<>(), new ElementArrayBuffer());
		initializeArrayBuffer();
	}
	
	protected void initializeArrayBuffer()
	{
		VertexArray vertexArray = getVertexArray();
		vertexArray.bindBuffer(getArrayBuffer());
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
	
	protected void disableAttributes()
	{
		VertexArray vertexArray = getVertexArray();
		vertexArray.bind();
		if (shader.useColor()) vertexArray.disableVertexAttribute(1);
		if (shader.useUV()) vertexArray.disableVertexAttribute(2);
		if (shader.useNormal()) vertexArray.disableVertexAttribute(3);
	}
	
	@Override
	public void changeShader(Shader other)
	{
		disableAttributes();
		shader = other;
		initializeArrayBuffer();
	}
	
	public void loadTexture(ColorTexture texture)
	{
		this.texture = texture;
	}
	
	public ObjectBuilder start(Bufferable value)
	{
		buildingValue = value;
		return new ObjectBuilder(this);
	}
	
	ObjectBuilder start(Bufferable value, ObjectBuilder builder)
	{
		buildingValue = value;
		return builder;
	}
	
	public int end(ObjectBuilder builder)
	{
		int vertices = buildingValue.vertices();
		int vertexSize = this.vertexSize / 4;
		int[] buildedData = new int[vertexSize * vertices];
		int[][] data = buildingValue.getData(builder);
		
		try
		{
			for (int i = 0; i < vertices; i++)
			{
				int[] vertexData = buildingValue.getVertexData(i, builder.getModifier(i), data[i]);
				for (int j = 0; j < vertexData.length; j++) buildedData[i * vertexData.length + j] = vertexData[j];
			}
		}
		catch (IndexOutOfBoundsException e)
		{
			throw new InsufficientVertexDataException("Required " + vertices + " modifiers, available " + builder.availableModifier(), e);
		}
		getArrayBuffer().add(buildingValue, buildedData);
		return getArrayBuffer().size() - 1;
	}
	
	public void enable(int index)
	{
		getArrayBuffer().enable(index);
	}
	
	public void disable(int index)
	{
		getArrayBuffer().disable(index);
	}
	
	@Override
	public ObjectRenderer color(Vector4f color)
	{
		bindedColor = color.divide(255);
		return this;
	}
	
	@Override
	public ObjectRenderer uv(Vector2f uv)
	{
		bindedUV = uv;
		return this;
	}
	
	@Override
	public ObjectRenderer normal(Vector3f normal)
	{
		bindedNormal = normal;
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
			if (shader.hasUniformColor()) shader.setUniform(bindedColor, "color");
			if (shader.hasUniformUV()) shader.setUniform(bindedUV, "uv");
			if (shader.hasUniformNormal()) shader.setUniform(bindedNormal, "normal");
			if (shader.useUV() || shader.hasUniformUV()) shader.setUniform(0, "textureSampler");
		}
		draw(mode, getArrayBuffer().intSize());
		Shader.disableShaders();
	}
	
	public void reset()
	{
		bindedColor = Vector4f.getOrigin();
		bindedUV = Vector2f.getOrigin();
		bindedNormal = Vector3f.getOrigin();
		clear();
	}
	
	public void clear()
	{
		getArrayBuffer().clear();
		getElementArrayBuffer().clear();
		offset = 0;
	}
	
	public void merge(ObjectRenderer renderer)
	{
		getArrayBuffer().merge(renderer.getArrayBuffer());
		getElementArrayBuffer().merge(renderer.getElementArrayBuffer(), offset);
		offset += renderer.getOffset();
	}
	
	public int getOffset()
	{
		return offset;
	}
	
	@Override
	public String toString()
	{
		return "ObjectRenderer{" + getArrayBuffer() + ", " + getElementArrayBuffer() + "}";
	}
}
