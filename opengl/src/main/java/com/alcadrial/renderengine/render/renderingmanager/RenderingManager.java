package com.alcadrial.renderengine.render.renderingmanager;

import java.util.EnumMap;
import java.util.Map;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.renderengine.render.renderer.Renderer;
import com.alcadrial.renderengine.render.renderer.RenderingMode;
import com.alcadrial.renderengine.render.renderer.font.Font;
import com.alcadrial.renderengine.render.renderer.font.FontRenderer;
import com.alcadrial.renderengine.render.renderer.universal.UniversalRenderer;
import com.alcadrial.renderengine.render.shader.ShaderProgram;

public class RenderingManager {
	
	private Map<RenderingMode, Rendering<UniversalRenderer>> universalRendering;
	private Map<RenderingMode, Rendering<FontRenderer>> fontRendering;
	
	public RenderingManager()
	{
		universalRendering = new EnumMap<>(RenderingMode.class);
		for (RenderingMode mode : RenderingMode.values()) universalRendering.put(mode, new Rendering<>(new UniversalRenderer(ShaderProgram.DEFAULT)));
		fontRendering = new EnumMap<>(RenderingMode.class);
		for (RenderingMode mode : RenderingMode.values()) fontRendering.put(mode, new Rendering<>(new FontRenderer(ShaderProgram.DEFAULT, null)));
	}
	
	public UniversalRenderer start(RenderingMode mode, ShaderProgram program)
	{
		return startUniversal(mode, program);
	}
	
	public UniversalRenderer get(RenderingMode mode)
	{
		return getUniversal(mode);
	}
	
	public void end(RenderingMode mode, Matrix4f matrix)
	{
		endUniversal(mode, matrix);
	}
	
	public UniversalRenderer startUniversal(RenderingMode mode, ShaderProgram program)
	{
		Rendering<UniversalRenderer> rendering = universalRendering.get(mode);
		if (rendering.rendering) throw new RenderingAlreadyStartedException(mode);
		rendering.renderer.clear();
		rendering.renderer.setShaderProgram(program);
		rendering.rendering = true;
		return rendering.renderer;
	}
	
	public UniversalRenderer getUniversal(RenderingMode mode)
	{
		return universalRendering.get(mode).renderer;
	}
	
	public void endUniversal(RenderingMode mode, Matrix4f matrix)
	{
		endUniversal(mode, matrix, true);
	}
	
	public void endUniversal(RenderingMode mode, Matrix4f matrix, boolean sync)
	{
		Rendering<UniversalRenderer> rendering = universalRendering.get(mode);
		if (!rendering.rendering) throw new RenderingNotStartedException(mode);
		rendering.renderer.setAutoSync(sync);
		rendering.renderer.draw(mode, matrix);
		rendering.rendering = false;
	}
	
	public FontRenderer startFont(RenderingMode mode, ShaderProgram program, Font font)
	{
		return startFont(mode, program, font, true);
	}
	
	public FontRenderer startFont(RenderingMode mode, ShaderProgram program, Font font, boolean autoSync)
	{
		Rendering<FontRenderer> rendering = fontRendering.get(mode);
		if (rendering.rendering) throw new RenderingAlreadyStartedException(mode);
		rendering.renderer.clear();
		rendering.renderer.setShaderProgram(program);
		rendering.renderer.setFont(font);
		rendering.rendering = true;
		return rendering.renderer;
	}
	
	public FontRenderer getFont(RenderingMode mode)
	{
		return fontRendering.get(mode).renderer;
	}
	
	public void endFont(RenderingMode mode, Matrix4f matrix)
	{
		endFont(mode, matrix, true);
	}
	
	public void endFont(RenderingMode mode, Matrix4f matrix, boolean sync)
	{
		Rendering<FontRenderer> rendering = fontRendering.get(mode);
		if (!rendering.rendering) throw new RenderingNotStartedException(mode);
		rendering.renderer.setAutoSync(sync);
		rendering.renderer.draw(mode, matrix);
		rendering.rendering = false;
	}
	
	private static final class Rendering<R extends Renderer> {
		
		private R renderer;
		private boolean rendering;
		
		public Rendering(R renderer)
		{
			this.renderer = renderer;
			rendering = false;
		}
	}
}
