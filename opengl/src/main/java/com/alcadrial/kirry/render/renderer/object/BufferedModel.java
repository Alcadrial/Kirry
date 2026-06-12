package com.alcadrial.kirry.render.renderer.object;

import java.nio.ByteBuffer;

import com.alcadrial.kirry.core.util.NotYetImplemented;
import com.alcadrial.kirry.render.buffer.GlBuffer;
import com.alcadrial.kirry.render.shader.ShaderInput;

@NotYetImplemented
public class BufferedModel extends GlBuffer implements BufferableModel {
	
	@Override
	public DataGetter getVertexData(int vertex, ShaderInput shaderInput)
	{
		return null;
	}
	
	@Override
	public int vertexSize()
	{
		return 0;
	}
	
	@Override
	public int vertices()
	{
		return 0;
	}
	
	@Override
	public int size()
	{
		return 0;
	}
	
	@Override
	public int byteSize()
	{
		return 0;
	}
	
	@Override
	public void clear()
	{}
	
	@Override
	protected int elementSize()
	{
		return 0;
	}
	
	@Override
	protected ByteBuffer nativeBuffer()
	{
		return null;
	}
	
	@Override
	protected ByteBuffer nativeGpuBuffer()
	{
		return null;
	}
	
	@Override
	protected boolean equals(ByteBuffer buffer, ByteBuffer other)
	{
		return false;
	}
}
