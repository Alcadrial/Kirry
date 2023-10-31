package com.alcadrial.renderengine.render.framebuffer;

import java.util.LinkedList;
import java.util.Queue;

import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector4i;
import com.alcadrial.renderengine.core.buffer.SimpleBuffer;
import com.alcadrial.renderengine.render.buffer.RenderBuffer;
import com.alcadrial.renderengine.render.textures.Texture;
import com.alcadrial.renderengine.render.textures.TextureTemplate;
import com.alcadrial.renderengine.render.util.Util;

public class FrameBuffer extends SimpleBuffer {
	
	public static final FrameBuffer DEFAULT = new FrameBuffer(FrameBufferTarget.BOTH, 0);
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
	
	public static FrameBuffer pushFrameBuffer()
	{
		FrameBuffer frameBuffer = new FrameBuffer();
		frameBuffer.offer();
		return frameBuffer;
	}
	
	public static FrameBuffer pushFrameBuffer(Texture texture)
	{
		int target = texture.getTemplate().target();
		boolean useColorTexture = target < GL30.GL_COLOR_ATTACHMENT0 || target > GL30.GL_COLOR_ATTACHMENT31;
		boolean useDepthTexture = target != GL30.GL_DEPTH_ATTACHMENT && target != GL30.GL_DEPTH_STENCIL_ATTACHMENT;
		boolean useStencilTexture = target != GL30.GL_STENCIL_ATTACHMENT && target != GL30.GL_DEPTH_STENCIL_ATTACHMENT;
		
		FrameBuffer frameBuffer = pushFrameBuffer();
		frameBuffer.init(texture.getWidth(), texture.getHeight(), useColorTexture, useDepthTexture, useStencilTexture);
		frameBuffer.setTexture(texture);
		return frameBuffer;
	}
	
	public static FrameBuffer pushFrameBuffer(Vector2i size, boolean useColorTexture, boolean useDepthTexture, boolean useStencilTexture)
	{
		return pushFrameBuffer(size.x(), size.y(), useColorTexture, useDepthTexture, useStencilTexture);
	}
	
	public static FrameBuffer pushFrameBuffer(int width, int height, boolean useColorTexture, boolean useDepthTexture, boolean useStencilTexture)
	{
		FrameBuffer frameBuffer = pushFrameBuffer();
		frameBuffer.init(width, height, useColorTexture, useDepthTexture, useStencilTexture);
		return frameBuffer;
	}
	
	private final Vector4i screen;
	private FrameBufferTarget target;
	private TextureHolder[] colorTexture;
	private TextureHolder depthTexture;
	private TextureHolder stencilTexture;
	private RenderBuffer renderBuffer;
	
	public FrameBuffer()
	{
		this(FrameBufferTarget.BOTH);
	}
	
	public FrameBuffer(FrameBufferTarget target)
	{
		this(target, GL32.glGenFramebuffers());
		Util.printError("[INTERNAL] framebuffer");
	}
	
	private FrameBuffer(FrameBufferTarget target, int id)
	{
		super(target.getTarget(), id);
		screen = Vector4i.of(0, 0, 0, 0);
		this.target = target;
		colorTexture = new TextureHolder[GL32.glGetInteger(GL32.GL_MAX_COLOR_ATTACHMENTS)];
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
		setTexture(texture, GL32.GL_COLOR_ATTACHMENT0);
	}
	
	public void setTexture(Texture texture, int attachment)
	{
		setTexture(texture, attachment, 0);
	}
	
	public void setTexture(Texture texture, int attachment, int mipmapLevel)
	{
		switch (attachment)
		{
			case GL32.GL_DEPTH_ATTACHMENT -> depthTexture = new TextureHolder(texture, false);
			case GL32.GL_STENCIL_ATTACHMENT -> stencilTexture = new TextureHolder(texture, false);
			case GL32.GL_DEPTH_STENCIL_ATTACHMENT -> depthTexture = stencilTexture = new TextureHolder(texture, false);
			default ->
			{
				if (attachment >= GL32.GL_COLOR_ATTACHMENT0 && attachment <= GL32.GL_COLOR_ATTACHMENT31)
				{
					colorTexture[attachment - GL32.GL_COLOR_ATTACHMENT0] = new TextureHolder(texture, false);
				}
			}
		}
		GL32.glFramebufferTexture(getTarget(), attachment, texture.getId(), mipmapLevel);
	}
	
	private void setTextureInternal(Texture texture, int attachment)
	{
		setTextureInternal(texture, attachment, 0);
	}
	
	private void setTextureInternal(Texture texture, int attachment, int mipmapLevel)
	{
		switch (attachment)
		{
			case GL32.GL_DEPTH_ATTACHMENT -> depthTexture = new TextureHolder(texture, true);
			case GL32.GL_STENCIL_ATTACHMENT -> stencilTexture = new TextureHolder(texture, true);
			case GL32.GL_DEPTH_STENCIL_ATTACHMENT -> depthTexture = stencilTexture = new TextureHolder(texture, true);
			default -> colorTexture[attachment - GL32.GL_COLOR_ATTACHMENT0] = new TextureHolder(texture, true);
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
				if (colorTexture[0] != null && colorTexture[0].isInternal) colorTexture[0].texture.close();
				Texture texture = new Texture(TextureTemplate.RGBA_2D_TEXTURE);
				texture.setData2D(texture.getTemplate(), data, width, height, 0);
				texture.disableAntiAlias();
				setTextureInternal(texture, GL32.GL_COLOR_ATTACHMENT0);
			}
			if (useDepthTexture && useStencilTexture)
			{
				if (depthTexture != null && depthTexture.isInternal) depthTexture.texture.close();
				if (stencilTexture != null && stencilTexture.isInternal && stencilTexture.texture != depthTexture.texture) stencilTexture.texture.close();
				Texture texture = new Texture(TextureTemplate.DEPTH_STENCIL_2D_TEXTURE);
				texture.setData2D(texture.getTemplate(), data, width, height, 0);
				texture.disableAntiAlias();
				setTextureInternal(texture, GL32.GL_DEPTH_STENCIL_ATTACHMENT);
			}
			else if (useDepthTexture)
			{
				if (depthTexture != null && depthTexture.isInternal) depthTexture.texture.close();
				Texture texture = new Texture(TextureTemplate.DEPTH_2D_TEXTURE);
				texture.setData2D(texture.getTemplate(), data, width, height, 0);
				texture.disableAntiAlias();
				setTextureInternal(texture, GL32.GL_DEPTH_ATTACHMENT);
			}
			else if (useStencilTexture)
			{
				if (stencilTexture != null && stencilTexture.isInternal) stencilTexture.texture.close();
				Texture texture = new Texture(TextureTemplate.STENCIL_2D_TEXTURE);
				texture.setData2D(texture.getTemplate(), data, width, height, 0);
				texture.disableAntiAlias();
				setTextureInternal(texture, GL32.GL_STENCIL_ATTACHMENT);
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
		TextureTemplate template = colorTexture[attachment].texture.getTemplate();
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
		TextureTemplate template = colorTexture[attachment].texture.getTemplate();
		GL13.glReadPixels(x, y, width, height, template.textureFormat(), template.dataType(), pointer);
		return pointer;
	}
	
	public int getDepthPixel(int x, int y)
	{
		TextureTemplate template = depthTexture.texture.getTemplate();
		GL13.glReadPixels(x, y, 1, 1, template.textureFormat(), template.dataType(), POINTER);
		return POINTER[0];
	}
	
	public int[] getDepthData(int x, int y, int width, int height)
	{
		int[] pointer = new int[(x - width) * (y - height)];
		TextureTemplate template = depthTexture.texture.getTemplate();
		GL13.glReadPixels(x, y, width, height, template.textureFormat(), template.dataType(), pointer);
		return pointer;
	}
	
	public int getStencilPixel(int x, int y)
	{
		TextureTemplate template = stencilTexture.texture.getTemplate();
		GL13.glReadPixels(x, y, 1, 1, template.textureFormat(), template.dataType(), POINTER);
		return POINTER[0];
	}
	
	public int[] getStencilData(int x, int y, int width, int height)
	{
		int[] pointer = new int[(x - width) * (y - height)];
		TextureTemplate template = stencilTexture.texture.getTemplate();
		GL13.glReadPixels(x, y, width, height, template.textureFormat(), template.dataType(), pointer);
		return pointer;
	}
	
	public Texture getColorTexture()
	{
		return getColorTexture(0);
	}
	
	public Texture getColorTexture(int index)
	{
		return colorTexture[index].texture;
	}
	
	public Texture retriveColorTexture()
	{
		return retriveColorTexture(0);
	}
	
	public Texture retriveColorTexture(int index)
	{
		colorTexture[index].isInternal = false;
		return colorTexture[index].texture;
	}
	
	public Texture getDepthTexture()
	{
		return depthTexture.texture;
	}
	
	public Texture retriveDepthTexture()
	{
		depthTexture.isInternal = false;
		return depthTexture.texture;
	}
	
	public Texture getStencilTexture()
	{
		return stencilTexture.texture;
	}
	
	public Texture retriveStencilTexture()
	{
		stencilTexture.isInternal = false;
		return stencilTexture.texture;
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
	
	@Override
	public int getTarget()
	{
		return target.getTarget();
	}
	
	public void setTarget(FrameBufferTarget target)
	{
		this.target = target;
		if (isBinded()) GL32.glBindFramebuffer(target.getTarget(), getId());
	}
	
	@Override
	public void close()
	{
		poll();
		for (TextureHolder holder : colorTexture) if (holder != null && holder.isInternal) holder.texture.close();
		if (depthTexture != null && depthTexture.isInternal) depthTexture.texture.close();
		if (stencilTexture != null && stencilTexture.isInternal && stencilTexture.texture != depthTexture.texture) stencilTexture.texture.close();
		GL32.glDeleteFramebuffers(getId());
	}
	
	private final class TextureHolder {
		
		private Texture texture;
		private boolean isInternal;
		
		public TextureHolder(Texture texture, boolean isInternal)
		{
			this.texture = texture;
			this.isInternal = isInternal;
		}
	}
}
