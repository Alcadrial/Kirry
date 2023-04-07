package com.alcadrial.renderengine.rendertest.manager;

import java.io.File;
import java.io.IOException;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;

import com.alcadrial.kector.angle.Angle;
import com.alcadrial.kector.angle.AngleUnit;
import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.renderengine.renderer.RenderingMode;
import com.alcadrial.renderengine.renderer.font.Font;
import com.alcadrial.renderengine.renderer.font.FontRenderer;
import com.alcadrial.renderengine.renderer.font.text.Text;
import com.alcadrial.renderengine.renderer.font.text.TextLine;
import com.alcadrial.renderengine.renderer.universal.UniversalRenderer;
import com.alcadrial.renderengine.renderingmanager.RenderingManager;
import com.alcadrial.renderengine.rendertest.UniversalRenderTest;
import com.alcadrial.renderengine.rendertest.object.RenderTest;
import com.alcadrial.renderengine.shader.ProgramStatus;
import com.alcadrial.renderengine.shader.Shader;
import com.alcadrial.renderengine.shader.ShaderProgram;
import com.alcadrial.renderengine.shader.ShaderStatus;
import com.alcadrial.renderengine.util.Image;
import com.alcadrial.renderengine.util.Util;
import com.alcadrial.renderengine.util.color.Color;
import com.alcadrial.renderengine.window.Window;

public class RenderingManagerTest implements RenderTest {
	
	private Window window;
	private Vector2f cursor;
	private boolean rotating;
	protected float rotationX;
	protected float rotationY;
	private Vector3f position;
	private float enlapsedTime;
	private RenderingManager renderingManager;
	private ShaderProgram colorProgram, textureProgram, fontProgram;
	private Font font;
	@SuppressWarnings("unused")
	private Image icon;
	
	@Override
	public void start()
	{
		window = new Window(1080, 720, "Test");
		window.create();
		window.makeContextCurrent();
		window.init();
		window.setLockKeyModsInputMode(true);
		Util.printError("window build");
		position = Vector3f.of(0, 0, 0);
		cursor = Vector2f.of(0, 0);
		window.addEvent(new GLFWCursorPosCallbackI() {
			
			private double x = 0, y = 0;
			
			@Override
			public void invoke(long id, double xPos, double yPos)
			{
				if (rotating)
				{
					rotationX = (float) (rotationX + yPos - y);
					if (rotationX > 90) rotationX = 90;
					else if (rotationX < -90) rotationX = -90;
					rotationY = (float) ((rotationY + xPos - x) % 360);
					if (rotationY < 0) rotationY = 360 - rotationY;
					x = xPos;
					y = yPos;
					return;
				}
				cursor.setAll((float) xPos, (float) (window.getHeight() - yPos));
			}
		});
		window.addEvent((GLFWWindowSizeCallbackI) (w, width, height) -> {
			RenderingManagerTest.this.window.setWidth(width);
			RenderingManagerTest.this.window.setHeight(height);
		});
		window.addEvent((GLFWKeyCallbackI) (id, key, scancode, action, mods) -> {
			if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT)
			{
				if ((mods & GLFW.GLFW_MOD_NUM_LOCK) == 0)
				{
					if (key == GLFW.GLFW_KEY_KP_8)
					{
						UniversalRenderTest.setActiveTest(UniversalRenderTest.OBJECT_RENDER_TEST);
						return;
					}
					if (key == GLFW.GLFW_KEY_KP_2)
					{
						return;
					}
				}
				switch (key)
				{
					case GLFW.GLFW_KEY_W -> position.add(0, 0, enlapsedTime);
					case GLFW.GLFW_KEY_A -> position.add(-enlapsedTime, 0, 0);
					case GLFW.GLFW_KEY_S -> position.add(0, 0, -enlapsedTime);
					case GLFW.GLFW_KEY_D -> position.add(enlapsedTime, 0, 0);
					case GLFW.GLFW_KEY_SPACE -> position.add(0, enlapsedTime, 0);
					case GLFW.GLFW_KEY_LEFT_SHIFT -> position.add(0, -enlapsedTime, 0);
					case GLFW.GLFW_KEY_P -> position.setAll(0, 0, 0);
				}
			}
			else if (action == GLFW.GLFW_RELEASE && key == GLFW.GLFW_KEY_R)
			{
				rotating = !rotating;
				GLFW.glfwSetInputMode(id, GLFW.GLFW_CURSOR, rotating ? GLFW.GLFW_CURSOR_DISABLED : GLFW.GLFW_CURSOR_NORMAL);
			}
		});
		
		Shader shader0;
		ShaderStatus shaderStatus;
		ProgramStatus programStatus;
		colorProgram = new ShaderProgram();
		Util.printError("shader p");
		try
		{
			shader0 = new Shader(new File(UniversalRenderTest.colorShaderVertexPath));
			shaderStatus = shader0.loadShader();
			if (!shaderStatus.isCompiled()) System.out.println(shaderStatus);
			colorProgram.attachShader(shader0);
			
			shader0 = new Shader(new File(UniversalRenderTest.colorShaderFragmentPath));
			shaderStatus = shader0.loadShader();
			if (!shaderStatus.isCompiled()) System.out.println(shaderStatus);
			colorProgram.attachShader(shader0);
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
		}
		programStatus = colorProgram.build();
		if (!programStatus.isBuilded()) System.out.println(programStatus);
		
		textureProgram = new ShaderProgram();
		try
		{
			shader0 = new Shader(new File(UniversalRenderTest.textureShaderVertexPath));
			shaderStatus = shader0.loadShader();
			if (!shaderStatus.isCompiled()) System.out.println(shaderStatus);
			textureProgram.attachShader(shader0);
			
			shader0 = new Shader(new File(UniversalRenderTest.textureShaderFragmentPath));
			shaderStatus = shader0.loadShader();
			if (!shaderStatus.isCompiled()) System.out.println(shaderStatus);
			textureProgram.attachShader(shader0);
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
		}
		programStatus = textureProgram.build();
		if (!programStatus.isBuilded()) System.out.println(programStatus);
		
		fontProgram = new ShaderProgram();
		try
		{
			shader0 = new Shader(new File(UniversalRenderTest.fontShaderVertexPath));
			shader0.loadShader();
			if (!shader0.getStatus().isCompiled()) System.out.println(shader0.getStatus());
			fontProgram.attachShader(shader0);
			shader0 = new Shader(new File(UniversalRenderTest.fontShaderFragmentPath));
			shader0.loadShader();
			if (!shader0.getStatus().isCompiled()) System.out.println(shader0.getStatus());
			fontProgram.attachShader(shader0);
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
		}
		fontProgram.build();
		Util.printError("font shader");
		try
		{
			font = new Font(new File(UniversalRenderTest.fontPath), new File(UniversalRenderTest.fontImagePath));
			font.getTexture().enableAntiAlias();
			icon = new Image(new File(UniversalRenderTest.iconPath));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		renderingManager = new RenderingManager();
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
		
	}
	
	@Override
	public void loop(float enlapsedTime)
	{
		this.enlapsedTime = enlapsedTime;
		{
			Matrix4f matrix = new Matrix4f();
			
			UniversalRenderer renderer = renderingManager.start(RenderingMode.QUADS, colorProgram);
			renderer.vertex(-0.5F, 0.5F, 0.9F).color(255, 255, 0, 255).end();
			renderer.vertex(0.5F, 0.5F, 0.9F).color(255, 255, 0, 255).end();
			renderer.vertex(0.5F, -0.5F, 0.9F).color(255, 255, 0, 255).end();
			renderer.vertex(-0.5F, -0.5F, 0.9F).color(255, 255, 0, 255).end();
			renderingManager.end(RenderingMode.QUADS, matrix);
		}
		{
			Matrix4f matrix = new Matrix4f();
			matrix.project(Angle.of(60, AngleUnit.DEGREES), 1000, 1, window.getAspectRatio());
			matrix.multiply(new Matrix4f().view(position, rotationX, rotationY));
			
			UniversalRenderer renderer = renderingManager.start(RenderingMode.QUADS, colorProgram);
			renderer.vertex(-0.5F, 0.5F, 0).color(0, 255, 0, 255).end();
			renderer.vertex(0.5F, 0.5F, 0).color(0, 255, 0, 255).end();
			renderer.vertex(0.5F, -0.5F, 0).color(0, 255, 0, 255).end();
			renderer.vertex(-0.5F, -0.5F, 0).color(0, 255, 0, 255).end();
			renderingManager.end(RenderingMode.QUADS, matrix);
		}
		{
			Matrix4f matrix = new Matrix4f();
			matrix.orthographicPositive(window.getWidth(), window.getHeight());
			
			Text text = new Text();
			text.addLine(new TextLine("1 Line"));
			text.addLine(new TextLine("2 very very long Line of text"));
			text.addLine(new TextLine("3 Line"));
			text.addLine(new TextLine("4 Line"));
			text.addLine(new TextLine("5 Line"));
			text.addLine(new TextLine("6 Line"));
			text.addLine(new TextLine("7 Line"));
			text.addLine(new TextLine("8 Line"));
			text.addLine(new TextLine(position));
			text.addNewLine();
			
			FontRenderer renderer = renderingManager.startFont(RenderingMode.QUADS, fontProgram, font);
			renderer.loadText(text, Vector2f.of(0, 256), Color.BLUE, -1, 128, 8, 0);
			renderingManager.endFont(RenderingMode.QUADS, matrix);
		}
		{
			Matrix4f matrix = new Matrix4f();
			matrix.orthographicPositive(window.getWidth(), window.getHeight());
			
			Matrix4f projection = new Matrix4f();
			projection.project(Angle.of(60, AngleUnit.DEGREES), 1000, 1, window.getAspectRatio());
			projection.multiply(new Matrix4f().view(position, rotationX, rotationY));
			float[] m = projection.toArray();
			
			Text text = new Text();
			text.addLine(new TextLine("\u250C "));
			text.addLine(new TextLine("\u2502 " + m[0] + " " + m[1] + " " + m[2] + " " + m[3] + " \u2502"));
			text.addLine(new TextLine("\u2502 " + m[4] + " " + m[5] + " " + m[6] + " " + m[7] + " \u2502"));
			text.addLine(new TextLine("\u2502 " + m[8] + " " + m[9] + " " + m[10] + " " + m[11] + " \u2502"));
			text.addLine(new TextLine("\u2502 " + m[12] + " " + m[13] + " " + m[14] + " " + m[15] + " \u2502"));
			text.addLine(new TextLine("\u250C"));
			
			FontRenderer renderer = renderingManager.startFont(RenderingMode.QUADS, fontProgram, font);
			renderer.loadText(text, Vector2f.of(0, 512), Color.DARK_MAGENTA, -1, -1, -1, 20);
			renderingManager.endFont(RenderingMode.QUADS, matrix);
		}
		
		// window.draw(compact, currentTime);
		window.update();
		window.clear(true, true, false);
		Util.printError("window update");
	}
	
	@Override
	public Window getWindow()
	{
		return window;
	}
}
