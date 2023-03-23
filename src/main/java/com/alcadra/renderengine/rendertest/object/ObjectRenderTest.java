package com.alcadra.renderengine.rendertest.object;

import java.io.File;
import java.io.IOException;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallbackI;

import com.alcadra.math.Matrix4f;
import com.alcadra.math.vector.Axis;
import com.alcadra.math.vector.Vector2f;
import com.alcadra.math.vector.Vector2i;
import com.alcadra.math.vector.Vector3f;
import com.alcadra.renderengine.renderer.RenderingMode;
import com.alcadra.renderengine.renderer.font.Font;
import com.alcadra.renderengine.renderer.font.FontRenderer;
import com.alcadra.renderengine.renderer.font.text.Text;
import com.alcadra.renderengine.renderer.font.text.TextLine;
import com.alcadra.renderengine.renderer.object.BufferableModel;
import com.alcadra.renderengine.renderer.object.ObjectBuilder;
import com.alcadra.renderengine.renderer.object.ObjectRenderer;
import com.alcadra.renderengine.rendertest.UniversalRenderTest;
import com.alcadra.renderengine.shader.Shader;
import com.alcadra.renderengine.shader.ShaderProgram;
import com.alcadra.renderengine.textures.Texture;
import com.alcadra.renderengine.textures.TextureTemplate;
import com.alcadra.renderengine.util.Image;
import com.alcadra.renderengine.util.SwitchableTracker;
import com.alcadra.renderengine.util.color.Color;
import com.alcadra.renderengine.util.color.ColorRGB;
import com.alcadra.renderengine.window.Window;

@SuppressWarnings("deprecation")
public class ObjectRenderTest implements RenderTest {
	
	private Window window;
	private Font font;
	
	private FontRenderer fontRenderer;
	private ObjectRenderer objectColorRenderer;
	private ObjectRenderer objectTexturedRenderer;
	private ObjectRenderer cubeRenderer;
	private Texture texture;
	
	@Override
	public void start()
	{
		window = new Window(1080, 720, "ObjectRenderTest");
		window.create();
		window.makeContextCurrent();
		window.init();
		window.setLockKeyModsInputMode(true);
		window.addEvent((GLFWKeyCallbackI) (id, key, scancode, action, mods) -> {
			if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT)
			{
				if ((mods & GLFW.GLFW_MOD_NUM_LOCK) == 0)
				{
					if (key == GLFW.GLFW_KEY_KP_8)
					{
						UniversalRenderTest.setActiveTest(UniversalRenderTest.RENDER_ENGINE_TEST);
						return;
					}
					if (key == GLFW.GLFW_KEY_KP_2)
					{
						return;
					}
				}
			}
			else if (action == GLFW.GLFW_RELEASE && key == GLFW.GLFW_KEY_R)
			{
				
			}
		});
		
		try
		{
			font = new Font(new File(UniversalRenderTest.fontPath), new File(UniversalRenderTest.fontImagePath));
			window.setIcon(new Image(new File(UniversalRenderTest.iconPath)));
			texture = new Texture(TextureTemplate.RGBA_2D_TEXTURE);
			Image image = new Image(new File(UniversalRenderTest.texturePath));
			texture.setData2D(texture.getTemplate(), image.loadToRGBABuffer(), image.getBuffered().getWidth(), image.getBuffered().getHeight(), 0);
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
		}
		
		Shader shader;
		ShaderProgram fontProgram = new ShaderProgram();
		try
		{
			shader = new Shader(new File(UniversalRenderTest.fontShaderVertexPath));
			shader.loadShader();
			if (!shader.getStatus().isCompiled()) System.out.println(shader.getStatus());
			fontProgram.attachShader(shader);
			shader = new Shader(new File(UniversalRenderTest.fontShaderFragmentPath));
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
			shader = new Shader(new File(UniversalRenderTest.colorShaderVertexPath));
			shader.loadShader();
			colorProgram.attachShader(shader);
			shader = new Shader(new File(UniversalRenderTest.colorShaderFragmentPath));
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
			shader = new Shader(new File(UniversalRenderTest.textureShaderVertexPath));
			shader.loadShader();
			objectTexturedShader.attachShader(shader);
			shader = new Shader(new File(UniversalRenderTest.textureShaderFragmentPath));
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
		trackers[1] = buildQuad(quad1, Color.of(0x00, 0x00, 0x00, 0x7F));
		trackers[2] = buildQuad(quad2, Color.of(0xFF, 0x00, 0x00, 0x7F));
		trackers[3] = buildQuad(quad3, Color.of(0x00, 0x00, 0xFF, 0x7F));
		trackers[4] = buildQuad(quad4, Color.of(0xFF, 0xFF, 0x00, 0x7F));
		trackers[5] = track(cube0, cubeRenderer);
		trackers[6] = track(cube1, cubeRenderer);
		trackers[6].disable();
		trackers[7] = track(cube2, cubeRenderer);
		trackers[7].disable();
		trackers[8] = track(cube3, cubeRenderer);
		trackers[8].disable();
		
		window.render(new Matrix4f().orthographic(window.getWidth(), window.getHeight()), 0);
	}
	
	private SwitchableTracker<BufferableModel> buildQuad(Quad quad, Color color)
	{
		ObjectBuilder builder = objectColorRenderer.start(quad);
		builder.vertex().color(color.r(), color.g(), color.b()).color(color.r(), color.g(), color.b(), color.a()).next().next().next().end();
		return builder.end();
	}
	
	private SwitchableTracker<BufferableModel> buildQuad(Quad quad, ColorRGB color)
	{
		ObjectBuilder builder = objectColorRenderer.start(quad);
		builder.vertex().color(color.r(), color.g(), color.b()).next().next().next().end();
		return builder.end();
	}
	
	private SwitchableTracker<BufferableModel> track(Cube cube, ObjectRenderer renderer)
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
	
	private Quad quad0 = new Quad(Vector2f.of(16, 16), Vector2f.of(32, 32));
	private Quad quad1 = new Quad(Vector2f.of(64, 320), Vector2f.of(256, 256));
	private Quad quad2 = new Quad(Vector2f.of(320, 320), Vector2f.of(256, 256));
	private Quad quad3 = new Quad(Vector2f.of(320, 64), Vector2f.of(256, 256));
	private Quad quad4 = new Quad(Vector2f.of(64, 64), Vector2f.of(256, 256));
	
	private Cube cube0 = new Cube(Vector3f.of(0, 0, 0), Vector3f.of(1, 1, 1));
	private Cube cube1 = new Cube(Vector3f.of(0, 0, 0), Vector3f.of(2, 2, 2));
	private Cube cube2 = new Cube(Vector3f.of(0, 0, 0), Vector3f.of(3, 3, 3));
	private Cube cube3 = new Cube(Vector3f.of(0, 0, 0), Vector3f.of(4, 4, 4));
	private Cube[] cubes = new Cube[] {cube0, cube1, cube2, cube3};
	
	@SuppressWarnings("rawtypes")
	private SwitchableTracker[] trackers = new SwitchableTracker[10];
	private int o = 0;
	
	@SuppressWarnings("unchecked")
	@Override
	public void loop(float partialTicks)
	{
		Matrix4f orthographic = new Matrix4f().orthographic(window.getWidth(), window.getHeight());
		Matrix4f orthographicPositive = new Matrix4f().orthographicPositive(window.getWidth(), window.getHeight());
		Matrix4f projection = new Matrix4f().project(10, 100, 8, window.getAspectRatio());
		
		Text text = new Text();
		text.addLine(new TextLine("partialticks " + partialTicks));
		text.addLine(new TextLine("time " + o));
		fontRenderer.loadText(text, Vector2i.of(0, window.getHeight() - font.getLineHeight()), Color.BLACK, 1, window.getWidth(), 0);
		fontRenderer.draw(orthographicPositive);
		
		int i = o / 10 % 14;
		if (i == 0) trackers[0].disable();
		else if (i == 5) trackers[0].enable();
		objectColorRenderer.draw(RenderingMode.QUADS, orthographicPositive);
		
		if (i > 0 && i < 5) trackers[i].disable();
		else if (i > 5 && i < 10) trackers[i - 5].enable();
		texture.bind();
		objectTexturedRenderer.draw(RenderingMode.QUADS, orthographicPositive);
		
		if (i == 10) trackers[5].modify(cubes[1]);
		else if (i == 11) trackers[5].modify(cubes[2]);
		else if (i == 12) trackers[5].modify(cubes[3]);
		else if (i == 13) trackers[5].modify(cubes[0]);
		cubeRenderer.draw(RenderingMode.QUADS, new Matrix4f(projection).multiply(new Matrix4f().view(Vector3f.of(10, 0, 10), 0, 0).multiply(new Matrix4f().rotate(Axis.Y, o * 8 / 13F % 360))));
		
		window.draw(orthographic, partialTicks);
		window.update();
		window.clear(true, true, false);
		o++;
	}
	
	@Override
	public Window getWindow()
	{
		return window;
	}
}
