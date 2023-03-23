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
	
	private static final int[] POINTER = new int[1];
	
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
	
	private final Vector4i screen;
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
		screen = Vector4i.of(0, 0, 0, 0);
		colorTexture = new Texture[GL32.glGetInteger(GL32.GL_MAX_COLOR_ATTACHMENTS)];
		depthTexture = null;
		stencilTexture = null;
		renderBuffer = null;
	}
	
	private FrameBuffer(int target, int id)
	{
		super(target, id);
		screen = Vector4i.of(0, 0, 0, 0);
		colorTexture = null;
		depthTexture = null;
		stencilTexture = null;
		renderBuffer = null;
	}
	
	public void offer()
	{
		BINDED_QUEUE.offer(binded);
		bind();
	}
	
	@Override
	public void bind()
	{
		GL32.glBindFramebuffer(getTarget(), getId());
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
	
	public void poll()
	{
		if (isBinded())
		{
			if (BINDED_QUEUE.isEmpty()) DEFAULT.bind0();
			else BINDED_QUEUE.poll().bind0();
		}
	}
	
	@Override
	public void unbind()
	{
		DEFAULT.bind0();
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
		texture.bind();
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
				texture.setData2D(texture.getTemplate(), data, width, height, 0);
			}
			if (useDepthTexture)
			{
				setTexture(new Texture(TextureTemplate.DEPTH_2D_TEXTURE));
				depthTexture.setData2D(depthTexture.getTemplate(), data, width, height, 0);
			}
			if (useStencilTexture)
			{
				setTexture(new Texture(TextureTemplate.STENCIL_2D_TEXTURE));
				stencilTexture.setData2D(stencilTexture.getTemplate(), data, width, height, 0);
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
		screen.setAll(x, y, width, height);
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
		TextureTemplate template = colorTexture[attachment].getTemplate();
		GL13.glReadPixels(x, y, 1, 1, template.textureFormat(), template.dataType(), POINTER);
		return POINTER[0];
	}
	
	public int[] getColorData(int x, int y, int width, int height)
	{
		return getColorData(x, y, width, height, 0);
	}
	
	public int[] getColorData(int x, int y, int width, int height, int attachment)
	{
		int[] pointer = new int[(x - width) * (y - height)];
		TextureTemplate template = colorTexture[attachment].getTemplate();
		GL13.glReadPixels(x, y, width, height, template.textureFormat(), template.dataType(), pointer);
		return pointer;
	}
	
	public int getDepthPixel(int x, int y)
	{
		TextureTemplate template = depthTexture.getTemplate();
		GL13.glReadPixels(x, y, 1, 1, template.textureFormat(), template.dataType(), POINTER);
		return POINTER[0];
	}
	
	public int[] getDepthData(int x, int y, int width, int height)
	{
		int[] pointer = new int[(x - width) * (y - height)];
		TextureTemplate template = depthTexture.getTemplate();
		GL13.glReadPixels(x, y, width, height, template.textureFormat(), template.dataType(), pointer);
		return pointer;
	}
	
	public int getStencilPixel(int x, int y)
	{
		TextureTemplate template = stencilTexture.getTemplate();
		GL13.glReadPixels(x, y, 1, 1, template.textureFormat(), template.dataType(), POINTER);
		return POINTER[0];
	}
	
	public int[] getStencilData(int x, int y, int width, int height)
	{
		int[] pointer = new int[(x - width) * (y - height)];
		TextureTemplate template = stencilTexture.getTemplate();
		GL13.glReadPixels(x, y, width, height, template.textureFormat(), template.dataType(), pointer);
		return pointer;
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
}
