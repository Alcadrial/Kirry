package com.alcadra.renderengine.renderer;

import java.awt.IllegalComponentStateException;

import com.alcadra.math.Matrix4f;
import com.alcadra.math.vector.Vector3f;
import com.alcadra.math.vector.Vector4f;
import com.alcadra.math.vector.complex.Quaternion;
import com.alcadra.renderengine.shader.Shader;
import com.alcadra.renderengine.util.update.NoReplacement;
import com.alcadra.renderengine.util.update.UseInstead;

@Deprecated(since = "2.0-migration", forRemoval = true)
@UseInstead(name = "UniversalRenderer")
@NoReplacement
public class ColorRenderer extends Renderer {
	
	public ColorRenderer(Shader shader)
	{
		super(shader);
	}
	
	@Override
	protected void initializeArrayBuffer()
	{
		if (!shader.useColor()) throw new IllegalComponentStateException("Shader cannot use a color for render in a ColorRenderer!");
		super.initializeArrayBuffer();
	}
	
	@Override
	public ColorRenderer vertex(float x, float y, float z)
	{
		return (ColorRenderer) super.vertex(x, y, z);
	}
	
	public ColorRenderer loadPoint(float x, float y, float z, float r, float g, float b, float a)
	{
		color(r, g, b, a);
		return vertex(x, y, z);
	}
	
	public ColorRenderer loadPoint(Vector3f pos, Vector4f color)
	{
		return loadPoint(pos.x(), pos.y(), pos.z(), color.x(), color.y(), color.z(), color.w());
	}
	
	public ColorRenderer loadLine(float startX, float startY, float startZ, float endX, float endY, float endZ, float r, float g, float b, float a)
	{
		loadPoint(startX, startY, startZ, r, g, b, a);
		return loadPoint(endX, endY, endZ, r, g, b, a);
	}
	
	public ColorRenderer loadLine(Vector3f start, Vector3f end, Vector4f color)
	{
		return loadLine(start.x(), start.y(), start.z(), end.x(), end.y(), end.z(), color.x(), color.y(), color.z(), color.w());
	}
	
	public ColorRenderer loadLine(Matrix4f matrix, Vector3f start, Vector3f end, Vector4f color)
	{
		return loadLine(matrix.multiply(new Vector4f(start, 1)).xyz(), matrix.multiply(new Vector4f(end, 1)).xyz(), color);
	}
	
	public ColorRenderer loadQuad(float startX, float startY, float startZ, float endX, float endY, float endZ, float r, float g, float b, float a)
	{
		loadLine(startX, endY, startZ, endX, endY, startZ, r, g, b, a);
		return loadLine(endX, startY, startZ, startX, startY, startZ, r, g, b, a);
	}
	
	public ColorRenderer loadQuad(Matrix4f matrix, Vector3f start, Vector3f end, Vector3f normal, Vector4f color)
	{
		start = matrix.multiply(new Vector4f(start, 1)).xyz();
		end = matrix.multiply(new Vector4f(end, 1)).xyz();
		
		Vector3f size = end.subtract(start).multiply(0.5F);
		Vector3f h = start.add(new Quaternion(normal, 90).rotate(size).xyz());
		Vector3f w = end.subtract(h).add(start);
		
		loadLine(h, end, color);
		return loadLine(w, start, color);
	}
	
	public ColorRenderer loadTriangle(float aX, float aY, float aZ, float bX, float bY, float bZ, float cX, float cY, float cZ, float r, float g, float b, float a)
	{
		loadPoint(aX, aY, aZ, r, g, b, a);
		loadPoint(bX, bY, bZ, r, g, b, a);
		return loadPoint(cX, cY, cZ, r, g, b, a);
	}
	
	public ColorRenderer loadTriangle(Vector3f pos0, Vector3f pos1, Vector3f pos2, Vector4f color)
	{
		return loadTriangle(pos0.x(), pos0.y(), pos0.z(), pos1.x(), pos1.y(), pos1.z(), pos2.x(), pos2.y(), pos2.z(), color.x(), color.y(), color.z(), color.w());
	}
	
	public ColorRenderer loadCube(float startX, float startY, float startZ, float endX, float endY, float endZ, float r, float g, float b, float a)
	{
		loadQuad(startX, startY, startZ, endX, endY, startZ, r, g, b, a);
		loadQuad(endX, startY, endZ, startX, startY, startZ, r, g, b, a);
		loadQuad(endX, startY, endZ, startX, endY, endZ, r, g, b, a);
		loadQuad(startX, endY, startZ, endX, endY, endZ, r, g, b, a);
		loadQuad(startX, startY, endZ, startX, endY, startZ, r, g, b, a);
		loadQuad(endX, startY, startZ, endX, endY, endZ, r, g, b, a);
		return this;
	}
	
	public ColorRenderer loadCube(Vector3f start, Vector3f end, Vector4f color)
	{
		return loadCube(start.x(), start.y(), start.z(), end.x(), end.y(), end.z(), color.x(), color.y(), color.z(), color.w());
	}
	
	public ColorRenderer loadCube(Matrix4f matrix, Vector3f start, Vector3f end, Vector4f color)
	{
		return loadCube(matrix.multiply(new Vector4f(start, 1)).xyz(), matrix.multiply(new Vector4f(end, 1)).xyz(), color);
	}
}
