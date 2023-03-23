package com.alcadra.renderengine.buffer;

import java.util.LinkedList;
import java.util.Queue;

import org.lwjgl.opengl.GL32;

import com.alcadra.math.vector.Vector2i;
import com.alcadra.math.vector.Vector4i;
import com.alcadra.renderengine.textures.ColorTexture;
import com.alcadra.renderengine.textures.DepthTexture;
import com.alcadra.renderengine.textures.StencilTexture;
import com.alcadra.renderengine.util.Util;

public class FrameBuffer extends SimpleBuffer {
	
	public static final FrameBuffer DEFAULT = new FrameBuffer(GL32.GL_FRAMEBUFFER, 0);
	private static FrameBuffer binded = DEFAULT;
	private static final Queue<FrameBuffer> BINDED_QUEUE = new LinkedList<>();
	
	public static FrameBuffer getBinded()
	{
		return binded;
	}
	
	public static String getBindedError()
	{
		return switch (GL32.glCheckFramebufferStatus(binded.getTarget()))
		{
			case GL32.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT -> "FRAMEBUFFER INCOMPLETE ATTACHMENT";
			case GL32.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT -> "FRAMEBUFFER INCOMPLETE MISSING ATTACHMENT";
			case GL32.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER -> "FRAMEBUFFER INCOMPLETE DRAW_BUFFER";
			case GL32.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER -> "FRAMEBUFFER INCOMPLETE READ_BUFFER";
			case GL32.GL_FRAMEBUFFER_UNSUPPORTED -> "FRAMEBUFFER UNSUPPORTED";
			case GL32.GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE -> "FRAMEBUFFER INCOMPLETE MULTISAMPLE";
			case GL32.GL_FRAMEBUFFER_UNDEFINED -> "FRAMEBUFFER UNDEFINED";
			default -> "";
		};
	}
	
	private Vector4i screen = new Vector4i(0, 0, 0, 0);
	private ColorTexture colorTexture;
	private DepthTexture depthTexture;
	private StencilTexture stencilTexture;
	private RenderBuffer renderBuffer;
	
	public FrameBuffer()
	{
		this(GL32.GL_FRAMEBUFFER);
	}
	
	protected FrameBuffer(int target)
	{
		super(target, GL32.glGenFramebuffers());
		Util.printError("[INTERNAL] framebuffer");
		colorTexture = null;
		depthTexture = null;
		stencilTexture = null;
		renderBuffer = null;
	}
	
	private FrameBuffer(int target, int id)
	{
		super(target, id);
		colorTexture = null;
		depthTexture = null;
		stencilTexture = null;
		renderBuffer = null;
	}
	
	@Override
	public void bind()
	{
		GL32.glBindFramebuffer(getTarget(), getId());
		BINDED_QUEUE.offer(binded);
		binded = this;
		GL32.glViewport(screen.x(), screen.y(), screen.z(), screen.w());
	}
	
	private void bind0()
	{
		if (!isBinded())
		{
			GL32.glBindFramebuffer(getTarget(), getId());
			binded = this;
			GL32.glViewport(screen.x(), screen.y(), screen.z(), screen.w());
		}
	}
	
	@Override
	public void unbind()
	{
		if (isBinded())
		{
			if (BINDED_QUEUE.isEmpty()) DEFAULT.bind0();
			else BINDED_QUEUE.poll().bind0();
		}
	}
	
	public boolean isBinded()
	{
		return binded.getId() == getId();
	}
	
	public boolean notDefault()
	{
		return getId() != DEFAULT.getId();
	}
	
	public void setTexture(ColorTexture texture)
	{
		GL32.glFramebufferTexture(getTarget(), GL32.GL_COLOR_ATTACHMENT0, texture.getId(), 0);
		colorTexture = texture;
	}
	
	public void setTexture(DepthTexture texture)
	{
		GL32.glFramebufferTexture(getTarget(), GL32.GL_DEPTH_ATTACHMENT, texture.getId(), 0);
		depthTexture = texture;
	}
	
	public void setTexture(StencilTexture texture)
	{
		GL32.glFramebufferTexture(getTarget(), GL32.GL_STENCIL_ATTACHMENT, texture.getId(), 0);
		stencilTexture = texture;
	}
	
	public void init(int width, int height, boolean useColorTexture, boolean useDepthTexture, boolean useStencilTexture)
	{
		if (useColorTexture) setTexture(new ColorTexture(width, height));
		if (useDepthTexture) setTexture(new DepthTexture(width, height));
		if (useStencilTexture) setTexture(new StencilTexture(width, height));
		setDrawArea(screen.x(), screen.y(), width, height);
	}
	
	public void bindRenderBuffer(RenderBuffer buffer, int attachment)
	{
		if (renderBuffer == null || renderBuffer.getId() != buffer.getId())
		{
			renderBuffer = buffer;
			GL32.glFramebufferRenderbuffer(getTarget(), attachment, renderBuffer.getTarget(), renderBuffer.getId());
		}
	}
	
	public void setDrawArea(Vector2i start, Vector2i size)
	{
		setDrawArea(start.x(), start.y(), size.x(), size.y());
	}
	
	public void setDrawArea(int x, int y, int width, int height)
	{
		screen = new Vector4i(x, y, width, height);
		GL32.glViewport(x, y, width, height);
	}
	
	public void clear(boolean color, boolean depth, boolean stencil)
	{
		int mask = 0;
		if (color) mask |= GL32.GL_COLOR_BUFFER_BIT;
		if (depth) mask |= GL32.GL_DEPTH_BUFFER_BIT;
		if (stencil) mask |= GL32.GL_STENCIL_BUFFER_BIT;
		if (mask != 0) GL32.glClear(mask);
	}
	
	public ColorTexture getColorTexture()
	{
		return colorTexture;
	}
	
	public DepthTexture getDepthTexture()
	{
		return depthTexture;
	}
	
	public StencilTexture getStencilTexture()
	{
		return stencilTexture;
	}
	
	public RenderBuffer getRenderBuffer()
	{
		return renderBuffer;
	}
	
	public String getError()
	{
		try
		{
			bind();
			return getBindedError();
		}
		finally
		{
			unbind();
		}
	}
}
