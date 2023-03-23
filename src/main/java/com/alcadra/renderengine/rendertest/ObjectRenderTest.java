package com.alcadra.renderengine.rendertest;

import java.io.File;
import java.io.IOException;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallbackI;

import com.alcadra.math.Matrix4f;
import com.alcadra.math.vector.Vector2f;
import com.alcadra.math.vector.Vector2i;
import com.alcadra.math.vector.Vector3f;
import com.alcadra.math.vector.axis.Axis;
import com.alcadra.renderengine.RenderEngine;
import com.alcadra.renderengine.renderer.RenderingMode;
import com.alcadra.renderengine.renderer.font.Font;
import com.alcadra.renderengine.renderer.font.FontRenderer;
import com.alcadra.renderengine.renderer.font.text.Text;
import com.alcadra.renderengine.renderer.font.text.NonStyledTextLine;
import com.alcadra.renderengine.renderer.object.BufferableModel;
import com.alcadra.renderengine.renderer.object.ObjectBuilder;
import com.alcadra.renderengine.renderer.object.ObjectRenderer;
import com.alcadra.renderengine.shader.Shader;
import com.alcadra.renderengine.shader.ShaderProgram;
import com.alcadra.renderengine.textures.ColorTexture;
import com.alcadra.renderengine.util.Image;
import com.alcadra.renderengine.util.Tracker;
import com.alcadra.renderengine.util.color.Color;
import com.alcadra.renderengine.util.color.VectorColorRGB;
import com.alcadra.renderengine.util.color.VectorColorRGBA;
import com.alcadra.renderengine.window.Window;

@SuppressWarnings("deprecation")
public class ObjectRenderTest implements RenderTest {
	
	private Window window;
	private Font font;
	
	private FontRenderer fontRenderer;
	private ObjectRenderer objectColorRenderer;
	private ObjectRenderer objectTexturedRenderer;
	private ObjectRenderer cubeRenderer;
	private ColorTexture texture;
	
	@Override
	public void start()
	{
		window = new Window(1080, 720, "ObjectRenderTest");
		window.create();
		window.makeContextCurrent();
		window.init();
		window.addEvent((GLFWKeyCallbackI) (id, key, scancode, action, mods) -> {
			if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT)
			{
				switch (key)
				{
					case GLFW.GLFW_KEY_KP_7 ->
					{
						RenderEngine.setActiveTest(RenderEngine.RENDER_ENGINE_TEST);
					}
				}
			}
			else if (action == GLFW.GLFW_RELEASE && key == GLFW.GLFW_KEY_R)
			{
				
			}
		});
		
		try
		{
			font = new Font(new File(RenderEngine.fontPath), new File(RenderEngine.fontImagePath));
			window.setIcon(new Image(new File(RenderEngine.iconPath)));
			texture = new ColorTexture(new Image(new File(RenderEngine.texturePath)));
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
		}
		
		Shader shader;
		ShaderProgram fontProgram = new ShaderProgram();
		try
		{
			shader = new Shader(new File(RenderEngine.fontShaderVertexPath));
			shader.loadShader();
			if (!shader.getStatus().isCompiled()) System.out.println(shader.getStatus());
			fontProgram.attachShader(shader);
			shader = new Shader(new File(RenderEngine.fontShaderFragmentPath));
			shader.loadShader();
			if (!shader.getStatus().isCompiled()) System.out.println(shader.getStatus());
			fontProgram.attachShader(shader);
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
		}
		fontProgram.build();
		
		fontRenderer = new FontRenderer(fontProgram, font);
		
		ShaderProgram colorProgram = new ShaderProgram();
		try
		{
			shader = new Shader(new File(RenderEngine.colorShaderVertexPath));
			shader.loadShader();
			colorProgram.attachShader(shader);
			shader = new Shader(new File(RenderEngine.colorShaderFragmentPath));
			shader.loadShader();
			colorProgram.attachShader(shader);
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
		}
		colorProgram.build();
		
		objectColorRenderer = new ObjectRenderer(colorProgram);
		
		ShaderProgram objectTexturedShader = new ShaderProgram();
		try
		{
			shader = new Shader(new File(RenderEngine.textureShaderVertexPath));
			shader.loadShader();
			objectTexturedShader.attachShader(shader);
			shader = new Shader(new File(RenderEngine.textureShaderFragmentPath));
			shader.loadShader();
			objectTexturedShader.attachShader(shader);
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
		}
		objectTexturedShader.build();
		
		objectTexturedRenderer = new ObjectRenderer(objectTexturedShader);
		
		cubeRenderer = new ObjectRenderer(objectTexturedShader);
		
		init();
	}
	
	@Override
	public void restart()
	{
		if (window == null)
		{
			start();
			return;
		}
		window.show();
		window.makeContextCurrent();
		
		objectColorRenderer.clear();
		objectTexturedRenderer.clear();
		cubeRenderer.clear();
		init();
	}
	
	private void init()
	{
		trackers[0] = buildQuad(quad0, Color.LIGHT_YELLOW);
		trackers[1] = buildQuad(quad1, Color.ofVector(0x00, 0x00, 0x00, 0x7F));
		trackers[2] = buildQuad(quad2, Color.ofVector(0xFF, 0x00, 0x00, 0x7F));
		trackers[3] = buildQuad(quad3, Color.ofVector(0x00, 0x00, 0xFF, 0x7F));
		trackers[4] = buildQuad(quad4, Color.ofVector(0xFF, 0xFF, 0x00, 0x7F));
		trackers[5] = track(cube0, cubeRenderer);
		trackers[6] = track(cube1, cubeRenderer);
		trackers[6].disable();
		trackers[7] = track(cube2, cubeRenderer);
		trackers[7].disable();
		trackers[8] = track(cube3, cubeRenderer);
		trackers[8].disable();
		
		window.render(new Matrix4f().orthographic(window.getWidth(), window.getHeight()), 0);
	}
	
	private Tracker<BufferableModel> buildQuad(Quad quad, VectorColorRGBA color)
	{
		ObjectBuilder builder = objectColorRenderer.start(quad);
		builder.vertex().color(color.r(), color.g(), color.b()).color(color.r(), color.g(), color.b(), color.a()).next().next().next().end();
		return builder.end();
	}
	
	private Tracker<BufferableModel> buildQuad(Quad quad, VectorColorRGB color)
	{
		ObjectBuilder builder = objectColorRenderer.start(quad);
		builder.vertex().color(color.r(), color.g(), color.b()).next().next().next().end();
		return builder.end();
	}
	
	private Tracker<BufferableModel> track(Cube cube, ObjectRenderer renderer)
	{
		ObjectBuilder builder = renderer.start(cube);
		builder.vertex().color(0x00, 0x00, 0x00, 0x7F).next().next().next().end();
		builder.vertex().color(0x00, 0x00, 0xFF, 0x7F).next().next().next().end();
		builder.vertex().color(0x00, 0xFF, 0x00, 0x7F).next().next().next().end();
		builder.vertex().color(0x00, 0xFF, 0xFF, 0x7F).next().next().next().end();
		builder.vertex().color(0xFF, 0x00, 0x00, 0x7F).next().next().next().end();
		builder.vertex().color(0xFF, 0x00, 0xFF, 0x7F).next().next().next().end();
		return builder.end();
	}
	
	private Quad quad0 = new Quad(new Vector2f(16, 16), new Vector2f(32, 32));
	private Quad quad1 = new Quad(new Vector2f(64, 320), new Vector2f(256, 256));
	private Quad quad2 = new Quad(new Vector2f(320, 320), new Vector2f(256, 256));
	private Quad quad3 = new Quad(new Vector2f(320, 64), new Vector2f(256, 256));
	private Quad quad4 = new Quad(new Vector2f(64, 64), new Vector2f(256, 256));
	
	private Cube cube0 = new Cube(new Vector3f(0, 0, 0), new Vector3f(1, 1, 1));
	private Cube cube1 = new Cube(new Vector3f(0, 0, 0), new Vector3f(2, 2, 2));
	private Cube cube2 = new Cube(new Vector3f(0, 0, 0), new Vector3f(3, 3, 3));
	private Cube cube3 = new Cube(new Vector3f(0, 0, 0), new Vector3f(4, 4, 4));
	private Cube[] cubes = new Cube[] {cube0, cube1, cube2, cube3};
	
	@SuppressWarnings("rawtypes")
	private Tracker[] trackers = new Tracker[10];
	private int o = 0;
	
	@SuppressWarnings("unchecked")
	@Override
	public void loop(float partialTicks)
	{
		Matrix4f orthographic = new Matrix4f().orthographic(window.getWidth(), window.getHeight());
		Matrix4f orthographicPositive = new Matrix4f().orthographicPositive(window.getWidth(), window.getHeight());
		Matrix4f projection = new Matrix4f().project(10, 100, 8, window.getAspectRatio());
		
		Text text = new Text();
		text.addLine(new NonStyledTextLine("partialticks " + partialTicks));
		text.addLine(new NonStyledTextLine("time " + o));
		fontRenderer.loadText(text, new Vector2i(0, window.getHeight() - font.getLineHeight()), Color.BLACK, 1, window.getWidth(), 0);
		fontRenderer.draw(orthographicPositive);
		
		int i = o / 10 % 14;
		if (i == 0) trackers[0].disable();
		else if (i == 5) trackers[0].enable();
		objectColorRenderer.drawVertices(RenderingMode.QUADS, orthographicPositive);
		
		if (i > 0 && i < 5) trackers[i].disable();
		else if (i > 5 && i < 10) trackers[i - 5].enable();
		texture.bind();
		objectTexturedRenderer.drawVertices(RenderingMode.QUADS, orthographicPositive);
		
		if (i == 10) trackers[5].modify(cubes[1]);
		else if (i == 11) trackers[5].modify(cubes[2]);
		else if (i == 12) trackers[5].modify(cubes[3]);
		else if (i == 13) trackers[5].modify(cubes[0]);
		cubeRenderer.drawVertices(RenderingMode.QUADS, new Matrix4f(projection).multiply(new Matrix4f().view(new Vector3f(10, 0, 10), 0, 0).multiply(new Matrix4f().rotate(Axis.Y, o * 8 / 13F % 360))));
		
		window.update(orthographic, partialTicks);
		window.clear(true, true, false);
		o++;
	}
	
	@Override
	public Window getWindow()
	{
		return window;
	}
}
