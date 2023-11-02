package com.alcadrial.renderengine.render.rendertest;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Stream;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import com.alcadrial.kector.Math;
import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.kector.vector.Vector3i;
import com.alcadrial.renderengine.core.cursor.StandardCursor;
import com.alcadrial.renderengine.core.util.Image;
import com.alcadrial.renderengine.core.util.color.Color;
import com.alcadrial.renderengine.core.util.color.ColorRGB;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.buffer.ElementArrayBuffer;
import com.alcadrial.renderengine.render.buffer.FloatArrayBuffer;
import com.alcadrial.renderengine.render.complex.BèzierCurve;
import com.alcadrial.renderengine.render.complex.EllipseCurve;
import com.alcadrial.renderengine.render.complex.InterpolationCurve;
import com.alcadrial.renderengine.render.complex.KochCurve;
import com.alcadrial.renderengine.render.complex.RhodoneaCurve;
import com.alcadrial.renderengine.render.framebuffer.FrameBuffer;
import com.alcadrial.renderengine.render.renderer.RenderingMode;
import com.alcadrial.renderengine.render.renderer.font.Font;
import com.alcadrial.renderengine.render.renderer.font.FontRenderer;
import com.alcadrial.renderengine.render.renderer.font.text.Text;
import com.alcadrial.renderengine.render.renderer.font.text.TextLine;
import com.alcadrial.renderengine.render.renderer.universal.UniversalRenderer;
import com.alcadrial.renderengine.render.renderingmanager.RendererPreferences;
import com.alcadrial.renderengine.render.renderingmanager.RenderingManager;
import com.alcadrial.renderengine.render.rendertest.manager.RenderingManagerTest;
import com.alcadrial.renderengine.render.rendertest.object.ObjectRenderTest;
import com.alcadrial.renderengine.render.rendertest.object.RenderTest;
import com.alcadrial.renderengine.render.shader.ProgramStatus;
import com.alcadrial.renderengine.render.shader.Shader;
import com.alcadrial.renderengine.render.shader.ShaderInput;
import com.alcadrial.renderengine.render.shader.ShaderProgram;
import com.alcadrial.renderengine.render.shader.ShaderStatus;
import com.alcadrial.renderengine.render.shader.uniform.ShaderUniform;
import com.alcadrial.renderengine.render.util.RendererUtil;
import com.alcadrial.renderengine.render.util.RendererUtil.RectangleVertexResult;
import com.alcadrial.renderengine.render.util.Util;
import com.alcadrial.renderengine.render.util.wrapper.CullFaceMode;
import com.alcadrial.renderengine.render.window.DropCallback;
import com.alcadrial.renderengine.render.window.MouseDragEndCallback;
import com.alcadrial.renderengine.render.window.MouseDragStartCallback;
import com.alcadrial.renderengine.render.window.Window;
import com.alcadrial.threads.TimeThread;

@SuppressWarnings("unused")
public class UniversalRenderTest implements RenderTest {
	
	public static volatile String colorShaderVertexPath = "src/main/resources/assets/renderengine/colored_cube.vert";
	public static volatile String colorShaderFragmentPath = "src/main/resources/assets/renderengine/colored_cube.frag";
	public static volatile String textureShaderVertexPath = "src/main/resources/assets/renderengine/colored_texture.vert";
	public static volatile String textureShaderFragmentPath = "src/main/resources/assets/renderengine/colored_texture.frag";
	public static volatile String fontShaderVertexPath = "src/main/resources/assets/renderengine/font.vert";
	public static volatile String fontShaderFragmentPath = "src/main/resources/assets/renderengine/font.frag";
	public static volatile String iconPath = "src/main/resources/assets/renderengine/icon.png";
	public static volatile String texturePath = "src/main/resources/assets/renderengine/grass1.png";
	public static volatile String fontPath = "src/main/resources/assets/renderengine/Segoe UI.json";
	public static volatile String fontImagePath = "src/main/resources/assets/renderengine/font.png";
	
	public static final UniversalRenderTest RENDER_ENGINE_TEST = new UniversalRenderTest();
	public static final ObjectRenderTest OBJECT_RENDER_TEST = new ObjectRenderTest();
	public static final RenderingManagerTest MANAGER_RENDER_TEST = new RenderingManagerTest();
	private static RenderTest activeTest;
	
	private static int maxLoops = 60;
	private static final ShaderUniform<Matrix4f> TRANSFORMATION = ShaderUniform.ofMatrix4f("TRANSFORMATION");
	private static final ShaderUniform<Integer> STATE = ShaderUniform.ofInt("STATE");
	
	public static void setActiveTest(RenderTest test)
	{
		activeTest.getWindow().hide();
		activeTest = test;
		activeTest.restart();
	}
	
	public static RenderTest getActiveTest()
	{
		return activeTest;
	}
	
	public static void main(String[] args)
	{
		System.out.println("args=" + Arrays.toString(args));
		for (String string : args)
		{
			if (string.equalsIgnoreCase("debug")) RenderEngine.enableDebug();
			else if (string.equalsIgnoreCase("exe"))
			{
				colorShaderVertexPath = colorShaderVertexPath.substring(19);
				colorShaderFragmentPath = colorShaderFragmentPath.substring(19);
				textureShaderVertexPath = textureShaderVertexPath.substring(19);
				textureShaderFragmentPath = textureShaderFragmentPath.substring(19);
				fontShaderVertexPath = fontShaderVertexPath.substring(19);
				fontShaderFragmentPath = fontShaderFragmentPath.substring(19);
				iconPath = iconPath.substring(19);
				texturePath = texturePath.substring(19);
				fontPath = fontPath.substring(19);
				fontImagePath = fontImagePath.substring(19);
			}
			else if (string.startsWith("path"))
			{
				String key = string.substring(4, string.indexOf('='));
				String path = string.substring(key.length() + 5);
				switch (key.toLowerCase())
				{
					case "colorshadervertex":
						colorShaderVertexPath = path;
						System.out.println(path);
						break;
					case "colorshaderfragment":
						colorShaderFragmentPath = path;
						System.out.println(path);
						break;
					case "textureshadervertex":
						textureShaderVertexPath = path;
						System.out.println(path);
						break;
					case "textureshaderfragment":
						textureShaderFragmentPath = path;
						System.out.println(path);
						break;
					case "fontshadervertex":
						fontShaderVertexPath = path;
						System.out.println(path);
						break;
					case "fontshaderfragment":
						fontShaderFragmentPath = path;
						System.out.println(path);
						break;
					case "icon":
						iconPath = path;
						System.out.println(path);
						break;
					case "font":
						fontPath = path;
						System.out.println(path);
						break;
					case "fontimage":
						fontImagePath = path;
						System.out.println(path);
						break;
				}
			}
		}
		try
		{
			if (RenderEngine.initialize()) RENDER_ENGINE_TEST.render();
		}
		catch (Exception e)
		{
			e.printStackTrace(System.out);
		}
	}
	
	private TimeThread thread;
	
	public void render() throws InterruptedException
	{
		activeTest = this;
		thread = new TimeThread("Test", maxLoops, this::start, () -> !activeTest.getWindow().shouldClose(), this::loopMethod, RenderEngine::terminate) {
			
			@Override
			protected boolean onException(Exception e)
			{
				e.printStackTrace(System.out);
				return false;
			}
		};
		thread.setDaemon(false);
		thread.start();
		synchronized (this)
		{
			wait(1000);
		}
	}
	
	public void loopMethod(float partialSecond)
	{
		try (var v = RenderEngine.configure()//@off
				.depthTest(true)
				.blend(true).setBlendFactors(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA)
				.cullFace(true).setCullFace(CullFaceMode.BACK).setFrontFaceDirection(true)
				.setClearColor(64, 64, 64, 255).setClearDepth(1).setClearStencil(0)
				.apply())//@on
		{
			Util.printError("origin");
			activeTest.loop(partialSecond);
			Util.printError("loop");
			while (FrameBuffer.getBinded().notDefault()) FrameBuffer.getBinded().unbind();
			Util.printError("renew");
		}
	}
	
	private Window window;
	private Vector2f cursor;
	private ShaderProgram colorProgram, textureProgram;
	private UniversalRenderer universalRenderer, widgetRenderer, textureRenderer;
	private RenderingManager manager;
	private FloatArrayBuffer buffer0, buffer1, buffer2;
	private ElementArrayBuffer element0, element1, element2;
	private FontRenderer fontRenderer;
	private Font font;
	private Vector2f startDrag;
	
	@Override
	public void start()
	{
		window = new Window(1080, 720, "Test");
		window.create();
		window.makeContextCurrent();
		window.init();
		window.setLockKeyModsInputMode(true);
		Util.printError("window build");
		cursor = Vector2f.of(0, 0);
		window.addEvent(new GLFWCursorPosCallbackI() {
			
			private double x = 0, y = 0;
			
			@Override
			public void invoke(long w, double xPos, double yPos)
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
			UniversalRenderTest.this.window.setWidth(width);
			UniversalRenderTest.this.window.setHeight(height);
		});
		window.addEvent((GLFWKeyCallbackI) (w, key, scancode, action, mods) -> {
			if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT)
			{
				if ((mods & GLFW.GLFW_MOD_NUM_LOCK) == 0 && key == GLFW.GLFW_KEY_KP_2)
				{
					setActiveTest(OBJECT_RENDER_TEST);
					return;
				}
				switch (key)
				{
					case GLFW.GLFW_KEY_W -> position.add(0, 0, currentTime);
					case GLFW.GLFW_KEY_A -> position.add(-currentTime, 0, 0);
					case GLFW.GLFW_KEY_S -> position.add(0, 0, -currentTime);
					case GLFW.GLFW_KEY_D -> position.add(currentTime, 0, 0);
					case GLFW.GLFW_KEY_SPACE -> position.add(0, currentTime, 0);
					case GLFW.GLFW_KEY_LEFT_SHIFT -> position.add(0, -currentTime, 0);
					case GLFW.GLFW_KEY_P -> position.setAll(0, 0, 0);
				}
			}
			else if (action == GLFW.GLFW_RELEASE && key == GLFW.GLFW_KEY_R)
			{
				rotating = !rotating;
				GLFW.glfwSetInputMode(w, GLFW.GLFW_CURSOR, rotating ? GLFW.GLFW_CURSOR_DISABLED : GLFW.GLFW_CURSOR_NORMAL);
			}
		});
		window.addEvent((MouseDragStartCallback) (w, button, x, y) -> {
			startDrag = Vector2f.of((float) x, (float) (window.getHeight() - y));
		});
		window.addEvent((MouseDragEndCallback) (w, button, x, y) -> {
			startDrag = null;
		});
		window.addEvent((DropCallback) (w, names) -> {
			System.out.println("Dropped files:");
			for (String name : names) System.out.println("\t" + name);
		});
		window.setCursor(StandardCursor.CROSSHAIR);
		Util.printError("events");
		FrameBuffer.getBinded();
		Util.printError("capabilities");
		frameBuffer = new FrameBuffer();
		Util.printError("framebuffer");
		
		Shader shader0;
		ShaderStatus shaderStatus;
		ProgramStatus programStatus;
		colorProgram = new ShaderProgram();
		Util.printError("shader p");
		try
		{
			shader0 = new Shader(new File(colorShaderVertexPath));
			shaderStatus = shader0.loadShader();
			if (!shaderStatus.isCompiled()) System.out.println(shaderStatus);
			colorProgram.attachShader(shader0);
			
			shader0 = new Shader(new File(colorShaderFragmentPath));
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
			shader0 = new Shader(new File(textureShaderVertexPath));
			shaderStatus = shader0.loadShader();
			if (!shaderStatus.isCompiled()) System.out.println(shaderStatus);
			textureProgram.attachShader(shader0);
			
			shader0 = new Shader(new File(textureShaderFragmentPath));
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
		
		Util.printError("shader init");
		universalRenderer = new UniversalRenderer(colorProgram);
		universalRenderer.setAutoSync(true);
		widgetRenderer = new UniversalRenderer(colorProgram);
		widgetRenderer.setAutoSync(true);
		textureRenderer = new UniversalRenderer(textureProgram);
		textureRenderer.setAutoSync(true);
		Util.printError("renderer init");
		ShaderProgram fontProgram = new ShaderProgram();
		try
		{
			shader0 = new Shader(new File(fontShaderVertexPath));
			shader0.loadShader();
			if (!shader0.getStatus().isCompiled()) System.out.println(shader0.getStatus());
			fontProgram.attachShader(shader0);
			shader0 = new Shader(new File(fontShaderFragmentPath));
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
			font = new Font(new File(fontPath), new File(fontImagePath));
			font.getTexture().enableAntiAlias();
			icon = new Image(new File(iconPath));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		fontRenderer = new FontRenderer(fontProgram, font);
		fontRenderer.setAutoSync(true);
		Util.printError("font renderer");
		manager = new RenderingManager(RendererPreferences.create().autoSync(true).build());
		
		window.setIcon(icon);
		window.show();
		window.addWidget(new Button(Vector3i.of(10, 10, 0), Vector2i.of(64, 64), window) {
			
			@Override
			public void onClick(int button, int count, Vector2i mousePos)
			{
				System.out.println("click " + button + ", " + count + " times");
			}
			
			@Override
			public void onPress(int button, Vector2i mousePos)
			{
				System.out.println("press " + button);
			}
			
			@Override
			public void onRelease(int button, Vector2i mousePos)
			{
				System.out.println("release " + button);
			}
		}, () -> manager);
		Util.printError("window");
		
		frameBuffer.bind();
		frameBuffer.init(1024, 1024, true, true, false);
		frameBuffer.unbind();
		Util.printError("frameBuffer init");
		
		Matrix4f projection = new Matrix4f().project(10, 100, 8, window.getAspectRatio());
		Matrix4f view = new Matrix4f().view(position, rotationX, rotationY);
		Matrix4f viewer = new Matrix4f(projection).multiply(view);
		Vector3f end = Vector3f.of(10, 10, 0);
		
		buffer0 = new FloatArrayBuffer();
		element0 = new ElementArrayBuffer();
		universalRenderer.pushBuffer(buffer0, element0);
		RendererUtil.loadOrthoCube(universalRenderer, Vector3f.origin(), end, Color.LIGHT_YELLOW);
		RendererUtil.loadOrthoCube(universalRenderer, viewer, end, end.xyz().add(end), Color.LIGHT_YELLOW);
		universalRenderer.pullBuffer();
		
		buffer1 = new FloatArrayBuffer();
		element1 = new ElementArrayBuffer();
		universalRenderer.pushBuffer(buffer1, element1);
		RendererUtil.loadRectangle(universalRenderer, 100, 100, 128, 128, 1, 255, 255, 0, 255);
		universalRenderer.pullBuffer();
		
		buffer2 = new FloatArrayBuffer();
		element2 = new ElementArrayBuffer();
		textureRenderer.pushBuffer(buffer2, element2);
		RendererUtil.loadRectangle(textureRenderer, -1, -1, 0, 0, 1, 0, 0, 1, 1);
		RendererUtil.loadRectangle(textureRenderer, 0, -1, 1, 0, 1, 0, 0, 1, 1);
		RendererUtil.loadRectangle(textureRenderer, -1, 0, 0, 1, 1, 0, 0, 1, 1);
		RendererUtil.loadRectangle(textureRenderer, 0, 0, 1, 1, 1, 0, 0, 1, 1);
		textureRenderer.pullBuffer();
		
		Matrix4f orthographic = new Matrix4f().orthographic(window.getWidth(), window.getHeight());
		window.initializeWidgets(orthographic, 0);
		window.renderWidgets(orthographic, 0);
		
		Util.printError("renderer load");
	}
	
	@Override
	public void restart()
	{
		window.show();
		window.makeContextCurrent();
		window.renderWidgets(new Matrix4f().orthographic(window.getWidth(), window.getHeight()), 0);
	}
	
	private FrameBuffer frameBuffer;
	private Image icon;
	private Random random = new Random();
	private Integer iteration = 0;
	private double o = 0;
	private String s = "";
	private Vector3f position = Vector3f.of(0, 0, 0);
	private float rotationX = 0, rotationY = 0, currentTime;
	private long time = System.nanoTime();
	private boolean rotating = false;
	
	@Override
	public void loop(float partialSecond)
	{
		long t = System.nanoTime();
		currentTime = (t - time) / 1000000F / 50;
		time = t;
		Matrix4f identity = new Matrix4f();
		Matrix4f compact = new Matrix4f().orthographic(window.getWidth(), window.getHeight());
		Matrix4f positive = new Matrix4f().orthographicPositive(window.getWidth(), window.getHeight());
		Matrix4f projection = new Matrix4f().project(70, 100, 1, window.getAspectRatio());
		Matrix4f view = new Matrix4f().view(position, rotationX, rotationY);
		Matrix4f camera = new Matrix4f().view(Vector3f.origin(), (float) o, (float) (o / 2));
		// Matrix4f projectional = new Matrix4f(compact).multiply(projection);
		// Matrix4f rotational = new Matrix4f(compact).multiply(view);
		Matrix4f cameral = new Matrix4f(compact).multiply(camera);
		// Matrix4f composite = new Matrix4f(projectional).multiply(view);
		// Matrix4f compositeC = new Matrix4f(projectional).multiply(camera);
		Matrix4f viewer = new Matrix4f(projection).multiply(view);
		Matrix4f projected = new Matrix4f(projection).multiply(camera);
		Matrix4f modeller = new Matrix4f(projected).multiply(compact);
		Matrix4f viewed = new Matrix4f(viewer).multiply(compact);
		Vector3f end = Vector3f.of(10, 10, 10);
		Util.printError("compact");
		
		FontRenderer fontR = manager.startFont(RenderingMode.QUADS, textureProgram, font);
		
		fontR.loadText(new Text("ala.beta@delta"), Vector2i.of(0, (int) o % 360), Color.LIGHT_RED, -1, -1, -1, 0, true);
		ColorRGB color = Color.of(cursor.x() * 255F / window.getWidth(), cursor.y() * 255F / window.getHeight(), 0);
		ColorRGB shadowColor = Color.of(cursor.x() * 255F / window.getWidth() / 2, cursor.y() * 255F / window.getHeight() / 2, 0);
		fontR.loadText(new Text(Util.toPointString(cursor)), Vector2f.copyOf(cursor).subtract(window.getSize().xy().multiplyAll(0.5F)), color, -1, -1, -1, 0, true, shadowColor, 2);
		Text text = new Text();
		text.addLine(new TextLine("partial " + partialSecond));
		text.addLine(new TextLine("time " + currentTime));
		text.addLine(new TextLine("fps " + thread.getRunsPerSecond()));
		text.addLine(new TextLine("xRot " + rotationX));
		text.addLine(new TextLine("yRot " + rotationY));
		text.addLine(new TextLine("pos " + position.toString().substring(8)));
		Vector3f v = end.xyz1().multiply(cameral).xyz();
		text.addLine(new TextLine("x=" + v.x()));
		text.addLine(new TextLine("y=" + v.y()));
		text.addLine(new TextLine("z=" + v.z()));
		v = end.xyz1().multiply(viewed).xyz();
		text.addLine(new TextLine("x=" + v.x()));
		text.addLine(new TextLine("y=" + v.y()));
		text.addLine(new TextLine("z=" + v.z()));
		text.addLine(new TextLine("VG"));
		text.addLine(new TextLine("o " + o));
		text.addLine(new TextLine(Double.toString(o % 180)));
		text.addLine(new TextLine(Double.toString(o % 360)));
		fontR.loadText(text, Vector2i.of(-window.getWidth() / 2, window.getHeight() / 2 - 40), Color.VERY_LIGHT_GRAY, 16, -1, window.getHeight() / 16, 0, false, Color.BLACK, 2);
		Util.printError("FontRender texting");
		fontR.draw(compact);
		Util.printError("FontRender draw");
		fontR.clear();
		fontR.drawFontTexture(identity, 0, 0, 0, 1, 1);
		fontR.clear();
		manager.endFont(RenderingMode.QUADS, identity);
		
		// frameBuffer.bind();
		Util.printError("FrameBuffer draw");
		universalRenderer.setIndicesUse(true);
		universalRenderer.pushBuffer(buffer0, element0);
		universalRenderer.draw(RenderingMode.QUADS, projected);
		universalRenderer.pullBuffer();
		universalRenderer.pushBuffer(buffer1, element1);
		universalRenderer.draw(RenderingMode.QUADS, compact);
		universalRenderer.pullBuffer();
		universalRenderer.pushBuffer();
		B\u00e8zierCurve.renderCurve(universalRenderer, 100, Color.RED, Vector2f.of(0, -400), Vector2f.of(0, -100), Vector2f.of(300, -100), Vector2f.of(300, -400), Vector2f.of(0, -400), Vector2f.of(0, -300), Vector2f.of(100, -300), Vector2f.of(100, -400), Vector2f.of(0, -400));
		universalRenderer.draw(RenderingMode.LINE_STRIP, compact);
		universalRenderer.clear();
		EllipseCurve.renderCurve(universalRenderer, cursor, 300, 300, 50, Color.BLUE);
		EllipseCurve.renderCurve(universalRenderer, cursor, 100, 100, 50, Color.BLUE);
		universalRenderer.draw(RenderingMode.LINE_LOOP, positive);
		universalRenderer.clear();
		RhodoneaCurve.renderCurve(universalRenderer, cursor, 100, 2, 0, 750, Color.VERY_DARK_GREEN);
		universalRenderer.draw(RenderingMode.LINE_STRIP, positive);
		universalRenderer.clear();
		InterpolationCurve.renderCurve(universalRenderer, Vector2f.origin(), Vector2f.of(200, 200), x -> x, 3, Color.LIGHT_BLUE);
		universalRenderer.draw(RenderingMode.LINE_STRIP, compact);
		universalRenderer.clear();
		InterpolationCurve.renderCurve(universalRenderer, Vector2f.origin(), Vector2f.of(200, 200), x -> 1 - x, 3, Color.LIGHT_GRAY);
		universalRenderer.draw(RenderingMode.LINE_STRIP, compact);
		universalRenderer.clear();
		InterpolationCurve.renderCurve(universalRenderer, Vector2f.origin(), Vector2f.of(200, 200), x -> (Math.cos(x * Math.PIF) + 1) / 2, 30, Color.LIGHT_CYAN);
		universalRenderer.draw(RenderingMode.LINE_STRIP, compact);
		universalRenderer.clear();
		InterpolationCurve.renderCurve(universalRenderer, Vector2f.origin(), Vector2f.of(200, 200), x -> (Math.cos((x + 1) * Math.PIF) + 1) / 2, 32, Color.LIGHT_GREEN);
		universalRenderer.draw(RenderingMode.LINE_STRIP, compact);
		universalRenderer.clear();
		InterpolationCurve.renderCurve(universalRenderer, Vector2f.origin(), Vector2f.of(200, 200), x -> x * x, 50, Color.LIGHT_MAGENTA);
		universalRenderer.draw(RenderingMode.LINE_STRIP, compact);
		universalRenderer.clear();
		InterpolationCurve.renderCurve(universalRenderer, Vector2f.origin(), Vector2f.of(200, 200), x -> (float) (Math.log10(x + 1) / Math.log10(2)), 50, Color.LIGHT_RED);
		universalRenderer.draw(RenderingMode.LINE_STRIP, compact);
		universalRenderer.clear();
		InterpolationCurve.renderCurve(universalRenderer, Vector2f.origin(), Vector2f.of(200, 200), x -> (float) (x * Math.pow(Math.E, x - 1)), 50, Color.LIGHT_YELLOW);
		universalRenderer.draw(RenderingMode.LINE_STRIP, compact);
		universalRenderer.clear();
		KochCurve.renderCurve(universalRenderer, Vector2f.of(200, 0), Vector2f.of(400, 0), 5, Color.DARK_CYAN);
		KochCurve.renderCurve(universalRenderer, Vector2f.of(400, 0), Vector2f.of(300, -200), 5, Color.DARK_CYAN);
		KochCurve.renderCurve(universalRenderer, Vector2f.of(300, -200), Vector2f.of(200, 0), 5, Color.DARK_CYAN);
		universalRenderer.draw(RenderingMode.LINE_STRIP, compact);
		universalRenderer.pullBuffer();
		Util.printError("TextureRender draw");
		
		// frameBuffer.unbind();
		
		textureRenderer.pushBuffer(buffer2, element2);
		textureRenderer.draw(RenderingMode.QUADS, identity);
		textureRenderer.pullBuffer();
		
		window.drawWidgets(positive, currentTime);
		
		if (startDrag != null)
		{
			try (var a = RenderEngine.configure().cullFace(false).depthTest(false).apply())
			{
				float sX = startDrag.x() - window.getWidth() / 2F;
				float sY = window.getHeight() / 2F - startDrag.y();
				float eX = cursor.x() - window.getWidth() / 2F;
				float eY = cursor.y() - window.getHeight() / 2F;
				universalRenderer.pushBuffer();
				RectangleVertexResult result = RendererUtil.loadRectangle(universalRenderer, sX, sY, eX, eY, 0, 45, 45, 120, 127);
				universalRenderer.draw(RenderingMode.QUADS, compact);
				result.track(ShaderInput.COLOR, r -> r.modify(Stream.of(90F, 90F, 200F, 127F).map(f -> f / 255).mapToInt(Float::floatToIntBits).toArray()));
				universalRenderer.vertex(sX, eY, 0).color(90, 90, 200, 127).end();
				universalRenderer.draw(RenderingMode.LINE_STRIP, compact);
				universalRenderer.pullBuffer();
			}
		}
		
		window.update();
		window.clear(true, true, false);
		
		Util.printError("window update");
		o += currentTime * 1;
	}
	
	@Override
	public Window getWindow()
	{
		return window;
	}
	
	public ShaderProgram getColorProgram()
	{
		return colorProgram;
	}
	
	public ShaderProgram getTextureProgram()
	{
		return textureProgram;
	}
	
	/**
	 * <h3>{@link BufferUtils}</h3>
	 * <p>
	 * <ol>
	 * <i>
	 * <li>It does not support on-heap Java arrays.</li>
	 * <li>When a direct buffer is passed as an argument to an LWJGL method, no data is copied.</li>
	 * <li>LWJGL never modifies the current position</li>
	 * <li>Memory blocks are zeroed-out on allocation</li>
	 * </i>
	 * </ol>
	 * <h3>{@link MemoryUtil}</h3>
	 * <p>
	 * <ol>
	 * <i>
	 * <li>It does not support on-heap Java arrays.</li>
	 * <li>When a direct buffer is passed as an argument to an LWJGL method, no data is copied.</li>
	 * <li>LWJGL never modifies the current position</li>
	 * </i>
	 * </ol>
	 */
	private static final Object INFO = null;
}