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
import com.alcadra.renderengine.renderer.font.Font;
import com.alcadra.renderengine.renderer.font.FontRenderer;
import com.alcadra.renderengine.renderer.font.text.Text;
import com.alcadra.renderengine.renderer.font.text.TextLine;
import com.alcadra.renderengine.renderer.object.ObjectBuilder;
import com.alcadra.renderengine.renderer.object.ObjectRenderer;
import com.alcadra.renderengine.shader.Shader;
import com.alcadra.renderengine.shader.ShaderInputDataType;
import com.alcadra.renderengine.shader.ShaderStatus;
import com.alcadra.renderengine.shader.ShaderType;
import com.alcadra.renderengine.textures.ColorTexture;
import com.alcadra.renderengine.util.Image;
import com.alcadra.renderengine.util.Switch;
import com.alcadra.renderengine.util.Test;
import com.alcadra.renderengine.window.Window;

@Test
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
		window.build();
		window.makeContextCurrent();
		
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
		
		Shader fontShader = new Shader(ShaderInputDataType.PER_VERTEX, ShaderInputDataType.PER_VERTEX, ShaderInputDataType.UNUSUED);
		try
		{
			ShaderStatus results = fontShader.loadShaderFile(new File(RenderEngine.fontShaderVertexPath), ShaderType.VERTEX);
			if (!results.isCompiled()) System.out.println(results);
			results = fontShader.loadShaderFile(new File(RenderEngine.fontShaderFragmentPath), ShaderType.FRAGMENT);
			if (!results.isCompiled()) System.out.println(results);
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
		}
		fontShader.build();
		
		fontRenderer = new FontRenderer(fontShader);
		fontRenderer.setFont(font);
		
		Shader objectColorShader = new Shader(ShaderInputDataType.PER_VERTEX, ShaderInputDataType.UNUSUED, ShaderInputDataType.UNUSUED);
		try
		{
			ShaderStatus results = objectColorShader.loadShaderFile(new File(RenderEngine.colorShaderVertexPath), ShaderType.VERTEX);
			if (!results.isCompiled()) System.out.println(results);
			results = objectColorShader.loadShaderFile(new File(RenderEngine.colorShaderFragmentPath), ShaderType.FRAGMENT);
			if (!results.isCompiled()) System.out.println(results);
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
		}
		objectColorShader.build();
		
		objectColorRenderer = new ObjectRenderer(objectColorShader);
		
		Shader objectTexturedShader = new Shader(ShaderInputDataType.PER_VERTEX, ShaderInputDataType.PER_VERTEX, ShaderInputDataType.UNUSUED);
		try
		{
			ShaderStatus results = objectTexturedShader.loadShaderFile(new File(RenderEngine.textureShaderVertexPath), ShaderType.VERTEX);
			if (!results.isCompiled()) System.out.println(results);
			results = objectTexturedShader.loadShaderFile(new File(RenderEngine.textureShaderFragmentPath), ShaderType.FRAGMENT);
			if (!results.isCompiled()) System.out.println(results);
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
		}
		objectTexturedShader.build();
		
		objectTexturedRenderer = new ObjectRenderer(objectTexturedShader);
		objectTexturedRenderer.loadTexture(texture);
		
		cubeRenderer = new ObjectRenderer(objectTexturedShader);
		cubeRenderer.loadTexture(texture);
		
		switchs[0] = objectColorRenderer.start(quad0).vertex().color(0xFF, 0xFF, 0x00, 0xFF).next().next().next().end().end();
		switchs[1] = objectTexturedRenderer.start(quad1).vertex().color(0x00, 0x00, 0x00, 0x7F).next().next().next().end().end();
		switchs[2] = objectTexturedRenderer.start(quad2).vertex().color(0xFF, 0x00, 0x00, 0x7F).next().next().next().end().end();
		switchs[3] = objectTexturedRenderer.start(quad3).vertex().color(0x00, 0x00, 0xFF, 0x7F).next().next().next().end().end();
		switchs[4] = objectTexturedRenderer.start(quad4).vertex().color(0xFF, 0xFF, 0x00, 0x7F).next().next().next().end().end();
		ObjectBuilder builder = cubeRenderer.start(cube);
		builder.vertex().color(0x00, 0x00, 0x00, 0x7F).next().next().next().end();
		builder.vertex().color(0x00, 0x00, 0xFF, 0x7F).next().next().next().end();
		builder.vertex().color(0x00, 0xFF, 0x00, 0x7F).next().next().next().end();
		builder.vertex().color(0x00, 0xFF, 0xFF, 0x7F).next().next().next().end();
		builder.vertex().color(0xFF, 0x00, 0x00, 0x7F).next().next().next().end();
		builder.vertex().color(0xFF, 0x00, 0xFF, 0x7F).next().next().next().end();
		switchs[5] = builder.end();
		
		window.render(new Matrix4f().orthographic(window.getWidth(), window.getHeight()), 0);
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
		switchs[0] = objectColorRenderer.start(quad0).vertex().color(0xFF, 0xFF, 0x00, 0xFF).next().next().next().end().end();
		switchs[1] = objectTexturedRenderer.start(quad1).vertex().color(0x00, 0x00, 0x00, 0x7F).next().next().next().end().end();
		switchs[2] = objectTexturedRenderer.start(quad2).vertex().color(0xFF, 0x00, 0x00, 0x7F).next().next().next().end().end();
		switchs[3] = objectTexturedRenderer.start(quad3).vertex().color(0x00, 0x00, 0xFF, 0x7F).next().next().next().end().end();
		switchs[4] = objectTexturedRenderer.start(quad4).vertex().color(0xFF, 0xFF, 0x00, 0x7F).next().next().next().end().end();
		ObjectBuilder builder = cubeRenderer.start(cube);
		builder.vertex().color(0x00, 0x00, 0x00, 0x7F).next().next().next().end();
		builder.vertex().color(0x00, 0x00, 0xFF, 0x7F).next().next().next().end();
		builder.vertex().color(0x00, 0xFF, 0x00, 0x7F).next().next().next().end();
		builder.vertex().color(0x00, 0xFF, 0xFF, 0x7F).next().next().next().end();
		builder.vertex().color(0xFF, 0x00, 0x00, 0x7F).next().next().next().end();
		builder.vertex().color(0xFF, 0x00, 0xFF, 0x7F).next().next().next().end();
		switchs[5] = builder.end();
		
		window.render(new Matrix4f().orthographic(window.getWidth(), window.getHeight()), 0);
	}
	
	private Quad quad0 = new Quad(new Vector2f(16, 16), new Vector2f(32, 32));
	private Quad quad1 = new Quad(new Vector2f(64, 320), new Vector2f(256, 256));
	private Quad quad2 = new Quad(new Vector2f(320, 320), new Vector2f(256, 256));
	private Quad quad3 = new Quad(new Vector2f(320, 64), new Vector2f(256, 256));
	private Quad quad4 = new Quad(new Vector2f(64, 64), new Vector2f(256, 256));
	
	private Cube cube = new Cube(new Vector3f(0, 0, 0), new Vector3f(1, 1, 1));
	
	private Switch[] switchs = new Switch[10];
	private int o = 0;
	
	@Override
	public void loop(float partialTicks)
	{
		Matrix4f orthographic = new Matrix4f().orthographic(window.getWidth(), window.getHeight());
		Matrix4f orthographicPositive = new Matrix4f().orthographicPositive(window.getWidth(), window.getHeight());
		Matrix4f projection = new Matrix4f().project(10, 100, 8, window.getAspectRatio());
		
		Text text = new Text();
		text.addLine(new TextLine("partialticks " + partialTicks));
		text.addLine(new TextLine("time " + o));
		fontRenderer.loadText(text, new Vector2i(0, window.getHeight() - font.getLineHeight()), 1, window.getWidth(), 0);
		fontRenderer.draw(orthographicPositive);
		
		int i = o / 10 % 10;
		if (i == 0) switchs[0].turnOff();
		else if (i == 5) switchs[0].turnOn();
		objectColorRenderer.draw(7, orthographicPositive);
		
		if (i > 0 && i < 5) switchs[i].turnOff();
		else if (i > 5) switchs[i - 5].turnOn();
		objectTexturedRenderer.draw(7, orthographicPositive);
		
		cubeRenderer.draw(7, new Matrix4f(projection).multiply(new Matrix4f().view(new Vector3f(10, 0, 10), 0, 0).multiply(new Matrix4f().rotate(Axis.Y, o * 8 / 13F % 360))));
		
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
