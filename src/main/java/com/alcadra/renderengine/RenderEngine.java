package com.alcadra.renderengine;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import com.alcadra.math.Matrix4f;
import com.alcadra.math.vector.Vector2f;
import com.alcadra.math.vector.Vector2i;
import com.alcadra.math.vector.Vector3f;
import com.alcadra.math.vector.Vector3i;
import com.alcadra.math.vector.Vector4f;
import com.alcadra.renderengine.buffer.ElementArrayBuffer;
import com.alcadra.renderengine.buffer.FloatArrayBuffer;
import com.alcadra.renderengine.buffer.FrameBuffer;
import com.alcadra.renderengine.renderer.RenderingMode;
import com.alcadra.renderengine.renderer.font.Font;
import com.alcadra.renderengine.renderer.font.FontRenderer;
import com.alcadra.renderengine.renderer.font.text.Text;
import com.alcadra.renderengine.renderer.font.text.TextLine;
import com.alcadra.renderengine.renderer.universal.UniversalRenderer;
import com.alcadra.renderengine.rendertest.ObjectRenderTest;
import com.alcadra.renderengine.rendertest.RenderTest;
import com.alcadra.renderengine.shader.Shader;
import com.alcadra.renderengine.shader.ShaderProgram;
import com.alcadra.renderengine.shader.uniform.ShaderUniform;
import com.alcadra.renderengine.util.Image;
import com.alcadra.renderengine.util.RendererUtil;
import com.alcadra.renderengine.util.Test;
import com.alcadra.renderengine.util.Util;
import com.alcadra.renderengine.util.color.Color;
import com.alcadra.renderengine.window.Window;
import com.alcadra.renderengine.window.cursor.StandardCursor;
import com.alcadra.renderengine.window.widget.VisibleButton;
import com.alcadra.threads.TimeThread;

@SuppressWarnings("unused")
@Test
public class RenderEngine implements RenderTest {
	
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
	
	public static final RenderEngine RENDER_ENGINE_TEST = new RenderEngine();
	public static final ObjectRenderTest OBJECT_RENDER_TEST = new ObjectRenderTest();
	private static RenderTest activeTest;
	
	private static int maxLoops = 60;
	private static final ShaderUniform<Matrix4f> TRANSFORMATION = ShaderUniform.ofMatrix4f("TRANSFORMATION");
	private static final ShaderUniform<Integer> STATE = ShaderUniform.ofInt("STATE");
	
	public static void setActiveTest(RenderTest activeTest)
	{
		RenderEngine.activeTest.getWindow().hide();
		RenderEngine.activeTest = activeTest;
		RenderEngine.activeTest.restart();
	}
	
	public static RenderTest getActiveTest()
	{
		return activeTest;
	}
	
	public static void main(String[] args) throws Exception
	{
		for (String string : args)
		{
			System.out.println(string);
			if (string.equalsIgnoreCase("debug")) Util.enableDebug();
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
		if (GLFW.glfwInit()) RENDER_ENGINE_TEST.render();
	}
	
	private TimeThread thread;
	
	public void render() throws InterruptedException
	{
		activeTest = this;
		thread = new TimeThread("Test", maxLoops, this::start, t -> !activeTest.getWindow().shouldClose(), this::loopMethod, GLFW::glfwTerminate);
		thread.setDaemon(false);
		thread.start();
		synchronized (this)
		{
			wait(1000);
		}
	}
	
	public void loopMethod(float partialTicks)
	{
		Util.printError("origin");
		activeTest.loop(partialTicks);
		Util.printError("loop");
		while (FrameBuffer.getBinded().notDefault()) FrameBuffer.getBinded().unbind();
		GL30.glEnable(GL30.GL_DEPTH_TEST);
		GL30.glEnable(GL30.GL_BLEND);
		GL30.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
		GL30.glEnable(GL30.GL_CULL_FACE);
		GL30.glClearColor(0.6F, 0.7F, 1, 1);
		Util.printError("renew");
	}
	
	private Window window;
	private Vector2f cursor;
	private ShaderProgram colorProgram, textureProgram;
	private UniversalRenderer universalRenderer, widgetRenderer;
	private FloatArrayBuffer buffer0, buffer1;
	private ElementArrayBuffer element0, element1;
	private FontRenderer fontRenderer;
	private Font font;
	
	@Override
	public void start()
	{
		window = new Window(1080, 720, "Test");
		window.build();
		window.makeContextCurrent();
		Util.printError("window build");
		cursor = Vector2f.getOrigin();
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
				cursor = new Vector2f((float) xPos, (float) (window.getHeight() - yPos));
			}
		});
		window.addEvent((GLFWWindowSizeCallbackI) (w, width, height) -> {
			RenderEngine.this.window.setWidth(width);
			RenderEngine.this.window.setHeight(height);
		});
		window.addEvent((GLFWKeyCallbackI) (id, key, scancode, action, mods) -> {
			if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT)
			{
				switch (key)
				{
					case GLFW.GLFW_KEY_KP_7 -> setActiveTest(OBJECT_RENDER_TEST);
					default -> position = switch (key)
					{
						case GLFW.GLFW_KEY_W -> position.add(new Vector3f(0, 0, partial));
						case GLFW.GLFW_KEY_A -> position.add(new Vector3f(-partial, 0, 0));
						case GLFW.GLFW_KEY_S -> position.add(new Vector3f(0, 0, -partial));
						case GLFW.GLFW_KEY_D -> position.add(new Vector3f(partial, 0, 0));
						case GLFW.GLFW_KEY_SPACE -> position.add(new Vector3f(0, partial, 0));
						case GLFW.GLFW_KEY_LEFT_SHIFT -> position.add(new Vector3f(0, -partial, 0));
						case GLFW.GLFW_KEY_P -> Vector3f.getOrigin();
						default -> position;
					};
				}
			}
			else if (action == GLFW.GLFW_RELEASE && key == GLFW.GLFW_KEY_R)
			{
				rotating = !rotating;
				GLFW.glfwSetInputMode(id, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL + (rotating ? 2 : 0));
			}
		});
		window.setCursor(StandardCursor.CROSSHAIR);
		Util.printError("events");
		FrameBuffer.getBinded();
		Util.printError("capabilities");
		frameBuffer = new FrameBuffer();
		Util.printError("framebuffer");
		
		Shader shader0;
		colorProgram = new ShaderProgram();
		try
		{
			shader0 = new Shader(new File(colorShaderVertexPath));
			shader0.loadShader();
			colorProgram.attachShader(shader0);
			shader0 = new Shader(new File(colorShaderFragmentPath));
			shader0.loadShader();
			colorProgram.attachShader(shader0);
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
		}
		colorProgram.build();
		textureProgram = new ShaderProgram();
		try
		{
			shader0 = new Shader(new File(textureShaderVertexPath));
			shader0.loadShader();
			textureProgram.attachShader(shader0);
			shader0 = new Shader(new File(textureShaderFragmentPath));
			shader0.loadShader();
			textureProgram.attachShader(shader0);
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
		}
		textureProgram.build();
		
		Util.printError("shader init");
		universalRenderer = new UniversalRenderer(colorProgram);
		widgetRenderer = new UniversalRenderer(colorProgram);
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
		Util.printError("font renderer");
		
		window.setIcon(icon);
		window.show();
		window.addWidget(new VisibleButton(new Vector3i(10, 10, 0), new Vector2i(16, 16), window) {
			
			@Override
			public void onLeftClick(Vector2i mousePos)
			{
				System.out.println("l");
			}
			
			@Override
			public void onRightClick(Vector2i mousePos)
			{
				System.out.println("r");
			}
		}, widgetRenderer);
		Util.printError("window");
		
		frameBuffer.initializeDepthTextureBuffer(1024, 1024);
		
		Matrix4f projection = new Matrix4f().project(10, 100, 8, window.getAspectRatio());
		Matrix4f view = new Matrix4f().view(position, rotationX, rotationY);
		Matrix4f viewer = new Matrix4f(projection).multiply(view);
		Vector3f end = new Vector3f(10, 10, 10);
		
		buffer0 = new FloatArrayBuffer();
		element0 = new ElementArrayBuffer();
		universalRenderer.pushBuffer(buffer0, element0);
		RendererUtil.loadOrthoCube(universalRenderer, Vector3f.getOrigin(), end, Color.LIGHT_YELLOW);
		RendererUtil.loadOrthoCube(universalRenderer, viewer, end, end.add(end), Color.LIGHT_YELLOW);
		universalRenderer.pullBuffer();
		buffer1 = new FloatArrayBuffer();
		element1 = new ElementArrayBuffer();
		universalRenderer.pushBuffer(buffer1, element1);
		RendererUtil.loadSquare(universalRenderer, 100, 100, 128, 128, 1, 255, 255, 0, 255);
		universalRenderer.pullBuffer();
		
		window.render(new Matrix4f().orthographic(window.getWidth(), window.getHeight()), 0);
		
		Util.printError("renderer load");
	}
	
	@Override
	public void restart()
	{
		window.show();
		window.makeContextCurrent();
		window.render(new Matrix4f().orthographic(window.getWidth(), window.getHeight()), 0);
	}
	
	private FrameBuffer frameBuffer;
	private Image icon;
	private Random random = new Random();
	private Integer iteration = 0;
	private double o = 0;
	private String s = "";
	private Vector3f position = new Vector3f(0, 0, 0);
	private float rotationX = 0, rotationY = 0, partial;
	private boolean rotating = false;
	
	@Override
	public void loop(float partialTicks)
	{
		partial = partialTicks * 0.1F;
		Matrix4f identity = new Matrix4f();
		Matrix4f compact = new Matrix4f().orthographic(window.getWidth(), window.getHeight());
		Matrix4f projection = new Matrix4f().project(10, 100, 8, window.getAspectRatio());
		Matrix4f view = new Matrix4f().view(position, rotationX, rotationY);
		Matrix4f camera = new Matrix4f().view(Vector3f.getOrigin(), (float) o, (float) (o / 2));
		// Matrix4f projectional = new Matrix4f(compact).multiply(projection);
		// Matrix4f rotational = new Matrix4f(compact).multiply(view);
		Matrix4f cameral = new Matrix4f(compact).multiply(camera);
		// Matrix4f composite = new Matrix4f(projectional).multiply(view);
		// Matrix4f compositeC = new Matrix4f(projectional).multiply(camera);
		Matrix4f viewer = new Matrix4f(projection).multiply(view);
		Matrix4f projected = new Matrix4f(projection).multiply(camera);
		Matrix4f modeller = new Matrix4f(projected).multiply(compact);
		Matrix4f viewed = new Matrix4f(viewer).multiply(compact);
		Vector3f end = new Vector3f(10, 10, 10);
		Util.printError("compact");
		
		fontRenderer.loadText(new Text("ala.beta@delta"), new Vector2i(0, (int) o % 360), Color.LIGHT_RED, 0.5F, 500, 0);
		fontRenderer.loadText(new Text(Util.toPointString(cursor)), new Vector2i(cursor.subtract(window.getSize().multiply(0.5F))), Color.ofVector(cursor.x() * 255F / window.getWidth(), cursor.y() * 255F / window.getHeight(), 0), 0.003F, 550, 0);
		Text text = new Text();
		text.addLine(new TextLine("partialTicks " + partialTicks));
		text.addLine(new TextLine("fps " + thread.getRunsPerSecond()));
		text.addLine(new TextLine("xRot " + rotationX));
		text.addLine(new TextLine("yRot " + rotationY));
		text.addLine(new TextLine("pos " + position.toString().substring(8)));
		Vector3f v = new Vector4f(end, 1).multiply(cameral).xyz();
		text.addLine(new TextLine("x=" + v.x()));
		text.addLine(new TextLine("y=" + v.y()));
		text.addLine(new TextLine("z=" + v.z()));
		v = new Vector4f(end, 1).multiply(viewed).xyz();
		text.addLine(new TextLine("x=" + v.x()));
		text.addLine(new TextLine("y=" + v.y()));
		text.addLine(new TextLine("z=" + v.z()));
		text.addLine(new TextLine("VG"));
		text.addLine(new TextLine("o " + o));
		text.addLine(new TextLine(Double.toString(o % 180)));
		text.addLine(new TextLine(Double.toString(o % 360)));
		fontRenderer.loadText(text, new Vector2i(-window.getWidth() / 2, window.getHeight() / 2 - 40), Color.BLACK, 0.8F, 1000, 0);
		Util.printError("FontRender texting");
		fontRenderer.draw(compact);
		Util.printError("FontRender draw");
		
		// frameBuffer.bind();
		Util.printError("FrameBuffer draw");
		
		universalRenderer.pushBuffer(buffer0, element0);
		universalRenderer.drawVertices(RenderingMode.QUADS, projected);
		universalRenderer.pullBuffer();
		universalRenderer.pushBuffer(buffer1, element1);
		universalRenderer.drawVertices(RenderingMode.QUADS, compact);
		universalRenderer.pullBuffer();
		Util.printError("TextureRender draw");
		
		// frameBuffer.unbind();
		window.update(compact, partialTicks);
		window.clear(true, true, false);
		Util.printError("window update");
		o += partialTicks * 1;
	}
	
	@Override
	public Window getWindow()
	{
		return window;
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
	private Object info = null;
}