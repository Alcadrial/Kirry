package com.alcadra.renderengine.buffer;

import java.util.LinkedList;
import java.util.Queue;

import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import com.alcadra.math.vector.Vector2i;
import com.alcadra.math.vector.Vector4i;
import com.alcadra.renderengine.Launch;
import com.alcadra.renderengine.textures.ColorTexture;
import com.alcadra.renderengine.textures.DepthTexture;
import com.alcadra.renderengine.textures.Texture;

public class FrameBuffer extends SimpleBuffer {
	
	private static final FrameBuffer DEFAULT = new FrameBuffer(GL32.GL_FRAMEBUFFER, 0);
	private static FrameBuffer binded = DEFAULT;
	private static final Queue<FrameBuffer> BINDED_QUEUE = new LinkedList<>();
	
	private Vector4i screen = new Vector4i(0, 0, 0, 0);
	private ColorTexture colorTexture;
	private DepthTexture depthTexture;
	private RenderBuffer renderBuffer;
	
	public FrameBuffer()
	{
		this(GL32.GL_FRAMEBUFFER);
		colorTexture = ColorTexture.getNull();
		Launch.printError("[INTERNAL] framebuffer colorstep");
		depthTexture = DepthTexture.getNull();
		Launch.printError("[INTERNAL] framebuffer depthstep");
		renderBuffer = RenderBuffer.getNull();
		Launch.printError("[INTERNAL] framebuffer renderstep");
	}
	
	protected FrameBuffer(int target)
	{
		super(target, GL32.glGenFramebuffers());
		Launch.printError("[INTERNAL] framebuffer");
	}
	
	protected FrameBuffer(int target, int id)
	{
		super(target, id);
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
	
	public void unbind()
	{
		if (isBinded())
		{
			FrameBuffer frameBuffer = BINDED_QUEUE.poll();
			if (frameBuffer == null)
			{
				DEFAULT.bind0();
				return;
			}
			frameBuffer.bind0();
		}
	}
	
	public void bindUnbinded()
	{
		if (!isBinded()) bind();
	}
	
	public boolean isBinded()
	{
		return binded.getId() == getId();
	}
	
	public void bindTexture(Texture texture, int attachment)
	{
		if (texture.nonNull())
		{
			bindUnbinded();
			GL32.glFramebufferTexture(getTarget(), attachment, texture.getId(), 0);
			setDrawArea(screen.xy(), texture.getSize());
			if (texture instanceof ColorTexture) colorTexture = (ColorTexture) texture;
			else if (texture instanceof DepthTexture) depthTexture = (DepthTexture) texture;
		}
	}
	
	public void initializeTexture(int width, int height)
	{
		if (notDefault())
		{
			if (depthTexture.nonNull()) bindTexture(DepthTexture.getNull(), GL32.GL_DEPTH_ATTACHMENT);
			bindTexture(new ColorTexture(width, height), GL32.GL_COLOR_ATTACHMENT0);
		}
	}
	
	public void initializeDepthBuffer(int width, int height)
	{
		if (notDefault())
		{
			if (colorTexture.nonNull()) bindTexture(ColorTexture.getNull(), GL32.GL_COLOR_ATTACHMENT0);
			bindTexture(new DepthTexture(width, height), GL32.GL_DEPTH_ATTACHMENT);
			GL30.glEnable(GL30.GL_DEPTH_TEST);
			GL32.glClear(GL32.GL_DEPTH_BUFFER_BIT);
		}
	}
	
	public void initializeDepthTextureBuffer(int width, int height)
	{
		if (notDefault())
		{
			bind();
			bindTexture(new DepthTexture(width, height), GL32.GL_DEPTH_ATTACHMENT);
			GL32.glClear(GL32.GL_DEPTH_BUFFER_BIT);
			bindTexture(new ColorTexture(width, height), GL32.GL_COLOR_ATTACHMENT0);
			GL30.glEnable(GL30.GL_DEPTH_TEST);
			GL32.glEnable(GL32.GL_BLEND);
			GL32.glBlendFunc(GL32.GL_SRC_ALPHA, GL32.GL_ONE_MINUS_SRC_ALPHA);
			unbind();
		}
	}
	
	public void bindRenderBuffer(RenderBuffer buffer)
	{
		if (renderBuffer.getId() != buffer.getId())
		{
			renderBuffer = buffer;
			GL32.glFramebufferRenderbuffer(getTarget(), GL32.GL_COLOR_ATTACHMENT0, renderBuffer.getTarget(), renderBuffer.getId());
		}
	}
	
	public void setDrawArea(Vector2i start, Vector2i size)
	{
		screen = new Vector4i(start.x(), start.y(), size.x(), size.y());
		if (isBinded()) bind();
		else GL32.glViewport(screen.x(), screen.y(), screen.z(), screen.w());
	}
	
	public void clear()
	{
		bindUnbinded();
		GL32.glClear(GL32.GL_DEPTH_BUFFER_BIT | GL32.GL_COLOR_BUFFER_BIT);
	}
	
	public ColorTexture getColorTexture()
	{
		return colorTexture;
	}
	
	public DepthTexture getDepthTexture()
	{
		return depthTexture;
	}
	
	public RenderBuffer getRenderBuffer()
	{
		return renderBuffer;
	}
	
	public String getError()
	{
		bind();
		String error = getBindedError();
		unbind();
		return error;
	}
	
	public boolean notDefault()
	{
		return getId() != DEFAULT.getId();
	}
	
	public static FrameBuffer getDefault()
	{
		return DEFAULT;
	}
	
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
}
