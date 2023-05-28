package com.alcadrial.renderengine.opengl.buffer;

import org.lwjgl.opengl.GL30;

import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.renderengine.core.buffer.SimpleBuffer;
import com.alcadrial.renderengine.core.util.WorkInProgress;
import com.alcadrial.renderengine.opengl.util.Util;

@WorkInProgress
public class RenderBuffer extends SimpleBuffer {
	
	private final Vector2i size;
	
	public RenderBuffer(int format, Vector2i size)
	{
		this(GL30.GL_RENDERBUFFER, format, size);
	}
	
	protected RenderBuffer(int target, int format, Vector2i size)
	{
		this(target, GL30.glGenRenderbuffers(), format, size);
	}
	
	protected RenderBuffer(int target, int id, int format, Vector2i size)
	{
		super(target, id);
		Util.printError("[INTERNAL] renderbuffer init");
		if (id != 0)
		{
			GL30.glRenderbufferStorage(target, format, size.x(), size.y());
			this.size = size;
		}
		else this.size = null;
		Util.printError("[INTERNAL] renderbuffer storage");
	}
	
	public Vector2i getSize()
	{
		return size;
	}
	
	@Override
	public void bind()
	{
		GL30.glBindRenderbuffer(getTarget(), getId());
	}
	
	@Override
	public void unbind()
	{
		GL30.glBindRenderbuffer(getTarget(), 0);
	}
	
	@Override
	public void close()
	{
		GL30.glDeleteRenderbuffers(getId());
	}
}
