package com.alcadrial.kirry.render.renderingmanager;

import java.util.EnumMap;
import java.util.Map;

import org.jspecify.annotations.NonNull;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kirry.render.renderer.Renderer;
import com.alcadrial.kirry.render.renderer.RenderingMode;
import com.alcadrial.kirry.render.renderer.font.Font;
import com.alcadrial.kirry.render.renderer.font.FontRenderer;
import com.alcadrial.kirry.render.renderer.universal.UniversalRenderer;
import com.alcadrial.kirry.render.shader.ShaderProgram;

public class RenderingManager {
	
	private Map<RenderingMode, Rendering<UniversalRenderer>> universalRendering;
	private Map<RenderingMode, Rendering<FontRenderer>> fontRendering;
	
	private RendererPreferences defaultPreferences;
	
	public RenderingManager(RendererPreferences defaultPreferences)
	{
		universalRendering = new EnumMap<>(RenderingMode.class);
		for (RenderingMode mode : RenderingMode.values()) universalRendering.put(mode, new Rendering<>(new UniversalRenderer(ShaderProgram.DEFAULT)));
		fontRendering = new EnumMap<>(RenderingMode.class);
		for (RenderingMode mode : RenderingMode.values()) fontRendering.put(mode, new Rendering<>(new FontRenderer(ShaderProgram.DEFAULT, null)));
		
		this.defaultPreferences = defaultPreferences;
	}
	
	public UniversalRenderer start(RenderingMode mode, ShaderProgram program)
	{
		return start(mode, program, defaultPreferences);
	}
	
	public UniversalRenderer start(RenderingMode mode, ShaderProgram program, RendererPreferences preferences)
	{
		return startUniversal(mode, program, preferences);
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
		return startUniversal(mode, program, defaultPreferences);
	}
	
	public UniversalRenderer startUniversal(RenderingMode mode, ShaderProgram program, RendererPreferences preferences)
	{
		Rendering<UniversalRenderer> rendering = (@NonNull Rendering<UniversalRenderer>) universalRendering.get(mode);
		if (rendering.rendering) throw new RenderingAlreadyStartedException(mode);
		rendering.renderer.clear();
		rendering.renderer.setShaderProgram(program);
		if (preferences.autoSync()) rendering.renderer.setAutoSync(true);
		rendering.rendering = true;
		return rendering.renderer;
	}
	
	public UniversalRenderer getUniversal(RenderingMode mode)
	{
		return ((@NonNull Rendering<UniversalRenderer>) universalRendering.get(mode)).renderer;
	}
	
	public void endUniversal(RenderingMode mode, Matrix4f matrix)
	{
		Rendering<UniversalRenderer> rendering = (@NonNull Rendering<UniversalRenderer>) universalRendering.get(mode);
		if (!rendering.rendering) throw new RenderingNotStartedException(mode);
		rendering.renderer.draw(mode, matrix);
		rendering.rendering = false;
	}
	
	public FontRenderer startFont(RenderingMode mode, ShaderProgram program, Font font)
	{
		return startFont(mode, program, font, defaultPreferences);
	}
	
	public FontRenderer startFont(RenderingMode mode, ShaderProgram program, Font font, RendererPreferences preferences)
	{
		Rendering<FontRenderer> rendering = (@NonNull Rendering<FontRenderer>) fontRendering.get(mode);
		if (rendering.rendering) throw new RenderingAlreadyStartedException(mode);
		rendering.renderer.clear();
		rendering.renderer.setShaderProgram(program);
		rendering.renderer.setFont(font);
		if (preferences.autoSync()) rendering.renderer.setAutoSync(true);
		rendering.rendering = true;
		return rendering.renderer;
	}
	
	public FontRenderer getFont(RenderingMode mode)
	{
		return ((@NonNull Rendering<FontRenderer>) fontRendering.get(mode)).renderer;
	}
	
	public void endFont(RenderingMode mode, Matrix4f matrix)
	{
		Rendering<FontRenderer> rendering = (@NonNull Rendering<FontRenderer>) fontRendering.get(mode);
		if (!rendering.rendering) throw new RenderingNotStartedException(mode);
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
