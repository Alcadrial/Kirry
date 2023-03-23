package com.alcadra.renderengine.framebuffer;

import java.util.LinkedList;
import java.util.Queue;

import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL32;

import com.alcadra.math.vector.Vector2i;
import com.alcadra.math.vector.Vector4i;
import com.alcadra.renderengine.buffer.RenderBuffer;
import com.alcadra.renderengine.buffer.SimpleBuffer;
import com.alcadra.renderengine.textures.Texture;
import com.alcadra.renderengine.textures.TextureTemplate;
import com.alcadra.renderengine.util.Util;

public class FrameBuffer extends SimpleBuffer {
	
	public static final FrameBuffer DEFAULT = new FrameBuffer(GL32.GL_FRAMEBUFFER, 0);
	private static FrameBuffer binded = DEFAULT;
	private static final Queue<FrameBuffer> BINDED_QUEUE = new LinkedList<>();
	
	private static final int[][] POINTERS = new int[64][];
	private static int pointersNextIndex = 0;
	
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
	private Texture[] colorTexture;
	private Texture depthTexture;
	private Texture stencilTexture;
	private RenderBuffer renderBuffer;
	
	public FrameBuffer()
	{
		this(GL32.GL_FRAMEBUFFER);
	}
	
	protected FrameBuffer(int target)
	{
		super(target, GL32.glGenFramebuffers());
		Util.printError("[INTERNAL] framebuffer");
		colorTexture = new Texture[GL32.glGetInteger(GL32.GL_MAX_COLOR_ATTACHMENTS)];
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
	
	public void setTexture(Texture texture)
	{
		setTexture(texture, 0);
	}
	
	public void setTexture(Texture texture, int mipmapLevel)
	{
		int attachment = texture.getTemplate().attachment();
		switch (attachment)
		{
			case GL32.GL_DEPTH_ATTACHMENT -> depthTexture = texture;
			case GL32.GL_STENCIL_ATTACHMENT -> stencilTexture = texture;
			case GL32.GL_DEPTH_STENCIL_ATTACHMENT -> depthTexture = stencilTexture = texture;
			default -> colorTexture[attachment - GL32.GL_COLOR_ATTACHMENT0] = texture;
		}
		GL32.glFramebufferTexture(getTarget(), attachment, texture.getId(), mipmapLevel);
	}
	
	public void init(int width, int height, boolean useColorTexture, boolean useDepthTexture, boolean useStencilTexture)
	{
		if (useColorTexture | useDepthTexture | useStencilTexture)
		{
			int[] data = new int[width * height];
			if (useColorTexture)
			{
				Texture texture = new Texture(TextureTemplate.RGBA_2D_TEXTURE);
				setTexture(texture);
				texture.setData2D(data, width, height, 0);
			}
			if (useDepthTexture)
			{
				setTexture(new Texture(TextureTemplate.DEPTH_2D_TEXTURE));
				depthTexture.setData2D(data, width, height, 0);
			}
			if (useStencilTexture)
			{
				setTexture(new Texture(TextureTemplate.STENCIL_2D_TEXTURE));
				stencilTexture.setData2D(data, width, height, 0);
			}
		}
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
	
	public int getColorPixel(int x, int y)
	{
		return getColorPixel(x, y, 0);
	}
	
	public int getColorPixel(int x, int y, int attachment)
	{
		synchronized (POINTERS)
		{
			int[] pointer = pushSlot();
			TextureTemplate template = colorTexture[attachment].getTemplate();
			GL13.glReadPixels(x, y, 1, 1, template.textureFormat(), template.dataType(), pointer);
			return pointer[0];
		}
	}
	
	public int getDepthPixel(int x, int y)
	{
		synchronized (POINTERS)
		{
			int[] pointer = pushSlot();
			TextureTemplate template = depthTexture.getTemplate();
			GL13.glReadPixels(x, y, 1, 1, template.textureFormat(), template.dataType(), pointer);
			return pointer[0];
		}
	}
	
	public int getStencilPixel(int x, int y)
	{
		synchronized (POINTERS)
		{
			int[] pointer = pushSlot();
			TextureTemplate template = stencilTexture.getTemplate();
			GL13.glReadPixels(x, y, 1, 1, template.textureFormat(), template.dataType(), pointer);
			return pointer[0];
		}
	}
	
	public Texture getColorTexture(int index)
	{
		return colorTexture[index];
	}
	
	public Texture getDepthTexture()
	{
		return depthTexture;
	}
	
	public Texture getStencilTexture()
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
	
	private int[] pushSlot()
	{
		if (pointersNextIndex >= 64) pointersNextIndex = 0;
		return POINTERS[pointersNextIndex++];
	}
}
