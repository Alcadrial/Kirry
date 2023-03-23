package com.alcadra.renderengine.renderingmanager;

import java.util.EnumMap;
import java.util.Map;

import com.alcadra.math.matrix.Matrix4f;
import com.alcadra.renderengine.renderer.Renderer;
import com.alcadra.renderengine.renderer.RenderingMode;
import com.alcadra.renderengine.renderer.font.Font;
import com.alcadra.renderengine.renderer.font.FontRenderer;
import com.alcadra.renderengine.renderer.universal.UniversalRenderer;
import com.alcadra.renderengine.shader.ShaderProgram;

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
	
	public void end(RenderingMode mode, Matrix4f matrix)
	{
		endUniversal(mode, matrix);
	}
	
	public UniversalRenderer startUniversal(RenderingMode mode, ShaderProgram program)
	{
		Rendering<UniversalRenderer> rendering = universalRendering.get(mode);
		if (rendering.rendering) throw new RenderingAlreadyStartedException();
		rendering.renderer.clear();
		rendering.renderer.setShaderProgram(program);
		rendering.rendering = true;
		return rendering.renderer;
	}
	
	public void endUniversal(RenderingMode mode, Matrix4f matrix)
	{
		Rendering<UniversalRenderer> rendering = universalRendering.get(mode);
		if (!rendering.rendering) throw new RenderingNotStartedException();
		rendering.renderer.draw(mode, matrix);
		rendering.rendering = false;
	}
	
	public FontRenderer startFont(RenderingMode mode, ShaderProgram program, Font font)
	{
		Rendering<FontRenderer> rendering = fontRendering.get(mode);
		if (rendering.rendering) throw new RenderingAlreadyStartedException();
		rendering.renderer.clear();
		rendering.renderer.setShaderProgram(program);
		rendering.renderer.setFont(font);
		rendering.rendering = true;
		return rendering.renderer;
	}
	
	public void endFont(RenderingMode mode, Matrix4f matrix)
	{
		Rendering<FontRenderer> rendering = fontRendering.get(mode);
		if (!rendering.rendering) throw new RenderingNotStartedException();
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
