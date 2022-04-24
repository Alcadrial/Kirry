package com.alcadra.renderengine.buffer;

import org.lwjgl.opengl.GL30;

import com.alcadra.math.vector.Vector2i;
import com.alcadra.renderengine.Launch;

public class RenderBuffer extends SimpleBuffer {
	
	private static final RenderBuffer NULL = new RenderBuffer(GL30.GL_RENDERBUFFER, 0, GL30.GL_RGBA, Vector2i.getOrigin());
	private Vector2i size;
	
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
		Launch.printError("[INTERNAL] renderbuffer init");
		if (id != 0) GL30.glRenderbufferStorage(target, format, size.x(), size.y());
		Launch.printError("[INTERNAL] renderbuffer storage");
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
	
	public void unbind()
	{
		NULL.bind();
	}
	
	public static RenderBuffer getNull()
	{
		return NULL;
	}
}
