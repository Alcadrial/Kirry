package com.alcadra.renderengine.renderingmanager;

import com.alcadra.math.matrix.Matrix4f;
import com.alcadra.renderengine.renderer.Renderer;
import com.alcadra.renderengine.renderer.RenderingMode;
import com.alcadra.renderengine.renderer.font.Font;
import com.alcadra.renderengine.renderer.font.FontRenderer;
import com.alcadra.renderengine.renderer.universal.UniversalRenderer;
import com.alcadra.renderengine.shader.ShaderProgram;

public class RenderingManager {
	
	private static final class Rendering<R extends Renderer> {
		
		private R renderer;
		private RenderingMode mode;
		
		public Rendering(R renderer)
		{
			this.renderer = renderer;
			mode = null;
		}
	}
	
	private Rendering<UniversalRenderer> universalRendering;
	private Rendering<FontRenderer> fontRendering;
	
	public RenderingManager()
	{
		universalRendering = new Rendering<>(new UniversalRenderer(ShaderProgram.DEFAULT));
		fontRendering = new Rendering<>(new FontRenderer(ShaderProgram.DEFAULT, null));
	}
	
	public UniversalRenderer start(RenderingMode mode, ShaderProgram program)
	{
		return startUniversal(mode, program);
	}
	
	public void end(Matrix4f matrix)
	{
		endUniversal(matrix);
	}
	
	public UniversalRenderer startUniversal(RenderingMode mode, ShaderProgram program)
	{
		if (universalRendering.mode != null) throw new RenderingAlreadyStartedException();
		universalRendering.renderer.clear();
		universalRendering.renderer.setShaderProgram(program);
		universalRendering.mode = mode;
		return universalRendering.renderer;
	}
	
	public void endUniversal(Matrix4f matrix)
	{
		if (universalRendering.mode == null) throw new RenderingNotStartedException();
		universalRendering.renderer.draw(universalRendering.mode, matrix);
		universalRendering.mode = null;
	}
	
	public FontRenderer startFont(RenderingMode mode, ShaderProgram program, Font font)
	{
		if (fontRendering.mode != null) throw new RenderingAlreadyStartedException();
		fontRendering.renderer.clear();
		fontRendering.renderer.setShaderProgram(program);
		fontRendering.renderer.setFont(font);
		fontRendering.mode = mode;
		return fontRendering.renderer;
	}
	
	public void endFont(Matrix4f matrix)
	{
		if (fontRendering.mode == null) throw new RenderingNotStartedException();
		fontRendering.renderer.draw(fontRendering.mode, matrix);
		fontRendering.mode = null;
	}
}
