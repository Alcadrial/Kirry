package com.alcadra.renderengine.renderer;

import org.lwjgl.opengl.GL30;

import com.alcadra.math.Matrix4f;
import com.alcadra.math.vector.Vector2f;
import com.alcadra.math.vector.Vector3f;
import com.alcadra.math.vector.Vector4f;
import com.alcadra.math.vector.complex.Quaternion;
import com.alcadra.renderengine.buffer.ElementArrayBuffer;
import com.alcadra.renderengine.buffer.FloatArrayBuffer;
import com.alcadra.renderengine.shader.Shader;

public class Renderer extends BaseRenderer<FloatArrayBuffer> implements ShaderRenderer, VertexBuilder<Renderer> {
	
	private Vector4f bindedColor = Vector4f.getOrigin();
	private Vector2f bindedUV = Vector2f.getOrigin();
	private Vector3f bindedNormal = Vector3f.getOrigin();
	private int offset;
	
	public Renderer(Shader shader)
	{
		super(shader, new VertexArray(), new FloatArrayBuffer(), new ElementArrayBuffer());
		initializeArrayBuffer();
	}
	
	protected void initializeArrayBuffer()
	{
		VertexArray vertexArray = getVertexArray();
		vertexArray.bindBuffer(getArrayBuffer());
		int size = 12 + (shader.useColor() ? 16 : 0) + (shader.useUV() ? 8 : 0) + (shader.useNormal() ? 12 : 0);
		int offset = 0;
		vertexArray.vertexAttributePointer(0, 3, GL30.GL_FLOAT, false, size, offset);
		vertexArray.enableVertexAttribute(0);
		offset += 12;
		if (shader.useColor())
		{
			vertexArray.vertexAttributePointer(1, 4, GL30.GL_FLOAT, false, size, offset);
			vertexArray.enableVertexAttribute(1);
			offset += 16;
		}
		if (shader.useUV())
		{
			vertexArray.vertexAttributePointer(2, 2, GL30.GL_FLOAT, false, size, offset);
			vertexArray.enableVertexAttribute(2);
			offset += 8;
		}
		if (shader.useNormal())
		{
			vertexArray.vertexAttributePointer(3, 3, GL30.GL_FLOAT, false, size, offset);
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
	
	public Renderer loadPoint(int index)
	{
		getElementArrayBuffer().add(index);
		return this;
	}
	
	public Renderer loadPoint(float x, float y, float z)
	{
		getElementArrayBuffer().add(offset);
		return vertex(x, y, z);
	}
	
	public Renderer vertex(float x, float y, float z)
	{
		FloatArrayBuffer arrayBuffer = getArrayBuffer();
		arrayBuffer.add(x);
		arrayBuffer.add(y);
		arrayBuffer.add(z);
		if (shader.useColor())
		{
			arrayBuffer.add(bindedColor.x());
			arrayBuffer.add(bindedColor.y());
			arrayBuffer.add(bindedColor.z());
			arrayBuffer.add(bindedColor.w());
		}
		if (shader.useUV())
		{
			arrayBuffer.add(bindedUV.x());
			arrayBuffer.add(bindedUV.y());
		}
		if (shader.useNormal())
		{
			arrayBuffer.add(bindedNormal.x());
			arrayBuffer.add(bindedNormal.y());
			arrayBuffer.add(bindedNormal.z());
		}
		offset++;
		return this;
	}
	
	@Override
	public Renderer color(Vector4f color)
	{
		bindedColor = color.divide(255);
		return this;
	}
	
	@Override
	public Renderer uv(Vector2f uv)
	{
		bindedUV = uv;
		return this;
	}
	
	@Override
	public Renderer normal(Vector3f normal)
	{
		bindedNormal = normal;
		return this;
	}
	
	public Renderer loadPoint(Vector3f pos)
	{
		return loadPoint(pos.x(), pos.y(), pos.z());
	}
	
	public Renderer loadLine(float startX, float startY, float startZ, float endX, float endY, float endZ)
	{
		loadPoint(startX, startY, startZ);
		return loadPoint(endX, endY, endZ);
	}
	
	public Renderer loadLine(Vector3f start, Vector3f end)
	{
		return loadLine(start.x(), start.y(), start.z(), end.x(), end.y(), end.z());
	}
	
	public Renderer loadLine(Matrix4f matrix, Vector3f start, Vector3f end)
	{
		return loadLine(matrix.multiply(new Vector4f(start, 1)).xyz(), matrix.multiply(new Vector4f(end, 1)).xyz());
	}
	
	public Renderer loadQuad(float startX, float startY, float endX, float endY, float z)
	{
		loadLine(startX, endY, z, endX, endY, z);
		return loadLine(endX, startY, z, startX, startY, z);
	}
	
	public Renderer loadQuad(Matrix4f matrix, Vector3f start, Vector3f end)
	{
		start = matrix.multiply(new Vector4f(start, 1)).xyz();
		end = matrix.multiply(new Vector4f(end, 1)).xyz();
		
		Vector3f size = end.subtract(start).multiply(0.5F);
		Vector3f h = start.add(new Quaternion(bindedNormal).rotate(size).xyz());
		Vector3f w = end.subtract(h).add(start);
		
		loadLine(h, end);
		return loadLine(w, start);
	}
	
	public Renderer loadTriangle(float aX, float aY, float aZ, float bX, float bY, float bZ, float cX, float cY, float cZ)
	{
		loadPoint(aX, aY, aZ);
		loadPoint(bX, bY, bZ);
		return loadPoint(cX, cY, cZ);
	}
	
	public Renderer loadTriangle(Vector3f pos0, Vector3f pos1, Vector3f pos2)
	{
		return loadTriangle(pos0.x(), pos0.y(), pos0.z(), pos1.x(), pos1.y(), pos1.z(), pos2.x(), pos2.y(), pos2.z());
	}
	
	public Renderer loadCube(float startX, float startY, float startZ, float endX, float endY, float endZ)
	{
		loadPoint(offset).loadPoint(offset + 1).loadPoint(offset + 2).loadPoint(offset + 3);
		loadPoint(offset + 2).loadPoint(offset + 3).loadPoint(offset + 7).loadPoint(offset + 6);
		loadPoint(offset + 7).loadPoint(offset + 6).loadPoint(offset + 5).loadPoint(offset + 4);
		loadPoint(offset + 4).loadPoint(offset + 5).loadPoint(offset + 1).loadPoint(offset);
		loadPoint(offset + 4).loadPoint(offset).loadPoint(offset + 3).loadPoint(offset + 7);
		loadPoint(offset + 5).loadPoint(offset + 6).loadPoint(offset + 2).loadPoint(offset + 1);
		vertex(startX, endY, startZ);
		vertex(endX, endY, startZ);
		vertex(endX, startY, startZ);
		vertex(startX, startY, startZ);
		vertex(startX, endY, endZ);
		vertex(endX, endY, endZ);
		vertex(endX, startY, endZ);
		vertex(startX, startY, endZ);
		return this;
	}
	
	public Renderer loadCube(Vector3f start, Vector3f end)
	{
		return loadCube(start.x(), start.y(), start.z(), end.x(), end.y(), end.z());
	}
	
	public Renderer loadCube(Matrix4f matrix, Vector3f start, Vector3f end)
	{
		return loadCube(matrix.multiply(new Vector4f(start, 1)).xyz(), matrix.multiply(new Vector4f(end, 1)).xyz());
	}
	
	public void draw(int mode, Matrix4f matrix)
	{
		if (shader != null)
		{
			shader.enable();
			shader.setUniform(matrix, "transformation");
			if (shader.hasUniformColor()) shader.setUniform(bindedColor, "color");
			if (shader.hasUniformUV()) shader.setUniform(bindedUV, "uv");
			if (shader.hasUniformNormal()) shader.setUniform(bindedNormal, "normal");
			if (shader.useUV() || shader.hasUniformUV()) shader.setUniform(0, "textureSampler");
		}
		drawIndices(mode);
		Shader.disableShaders();
	}
	
	public void reset()
	{
		bindedColor = Vector4f.getOrigin();
		bindedUV = Vector2f.getOrigin();
		bindedNormal = Vector3f.getOrigin();
		clear();
	}
	
	@Override
	public void clear()
	{
		super.clear();
		offset = 0;
	}
	
	public void merge(Renderer renderer)
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
		return "Renderer{" + getArrayBuffer() + ", " + getElementArrayBuffer() + "}";
	}
}
