package com.alcadra.renderengine;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;

import com.alcadra.math.Matrix4f;
import com.alcadra.math.vector.Vector2f;
import com.alcadra.math.vector.Vector2i;
import com.alcadra.math.vector.Vector3f;
import com.alcadra.math.vector.Vector3i;
import com.alcadra.math.vector.Vector4f;
import com.alcadra.renderengine.buffer.FrameBuffer;
import com.alcadra.renderengine.renderer.ColorRenderer;
import com.alcadra.renderengine.renderer.Renderer;
import com.alcadra.renderengine.renderer.TextureRenderer;
import com.alcadra.renderengine.renderer.font.Font;
import com.alcadra.renderengine.renderer.font.FontRenderer;
import com.alcadra.renderengine.renderer.font.text.Text;
import com.alcadra.renderengine.renderer.font.text.TextLine;
import com.alcadra.renderengine.shader.Shader;
import com.alcadra.renderengine.shader.ShaderInputDataType;
import com.alcadra.renderengine.shader.ShaderType;
import com.alcadra.renderengine.textures.ColorTexture;
import com.alcadra.renderengine.util.Image;
import com.alcadra.renderengine.util.Logger;
import com.alcadra.renderengine.util.Util;
import com.alcadra.renderengine.window.Window;
import com.alcadra.renderengine.window.widget.VisibleButton;
import com.alcadra.threads.TimeThread;

public class Launch {
	
	public static final Logger LOGGER = new Logger();
	private static volatile String colorShaderVertexPath = "src/main/resources/assets/renderengine/colored_cube.vert";
	private static volatile String colorShaderFragmentPath = "src/main/resources/assets/renderengine/colored_cube.frag";
	private static volatile String textureShaderVertexPath = "src/main/resources/assets/renderengine/colored_texture.vert";
	private static volatile String textureShaderFragmentPath = "src/main/resources/assets/renderengine/colored_texture.frag";
	private static volatile String fontShaderVertexPath = "src/main/resources/assets/renderengine/font.vert";
	private static volatile String fontShaderFragmentPath = "src/main/resources/assets/renderengine/font.frag";
	private static volatile String iconPath = "src/main/resources/assets/renderengine/icon.png";
	private static volatile String fontPath = "src/main/resources/assets/renderengine/Segoe UI.json";
	private static volatile String fontImagePath = "src/main/resources/assets/renderengine/font.png";
	private static int maxLoops = 60;
	private static volatile boolean debug = false;
	
	public static boolean isDebug()
	{
		return debug;
	}
	
	public static void main(String[] args) throws Exception
	{
		for (String string : args)
		{
			if (string.equalsIgnoreCase("debug")) debug = true;
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
		if (GLFW.glfwInit()) new Launch().render();
	}
	
	Window window;
	Vector2f cursor;
	TimeThread thread;
	
	public void render() throws InterruptedException
	{
		thread = new TimeThread("Test", maxLoops, this::start, t -> !GLFW.glfwWindowShouldClose(window.getId()), this::loopMethod, GLFW::glfwTerminate);
		thread.setDaemon(false);
		thread.start();
		synchronized (this)
		{
			wait(1000);
		}
	}
	
	public static void printError()
	{
		printError("");
	}
	
	public static void printError(String s)
	{
		int error = GL30.glGetError();
		while (debug)
		{
			switch (error)
			{
				case GL30.GL_NO_ERROR:
					return;
				case GL30.GL_INVALID_ENUM:
					System.out.println(s + " INVALID ENUM");
					break;
				case GL30.GL_INVALID_VALUE:
					System.out.println(s + " INVALID VALUE");
					break;
				case GL30.GL_INVALID_OPERATION:
					System.out.println(s + " INVALID OPERATION");
					break;
				case GL30.GL_INVALID_FRAMEBUFFER_OPERATION:
					System.out.println(s + " INVALID FRAMEBUFFER OPERATION");
					break;
			}
			error = GL30.glGetError();
		}
	}
	
	public void loopMethod(float partialTicks)
	{
		printError();
		loop(partialTicks);
		FrameBuffer.unbind();
		GL30.glEnable(GL30.GL_DEPTH_TEST);
		GL30.glEnable(GL30.GL_BLEND);
		GL30.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
		GL30.glClearColor(0.6F, 0.7F, 1, 1);
	}
	
	Shader shader, shaderTexture;
	Renderer renderer;
	FontRenderer fontRenderer;
	TextureRenderer textureRenderer;
	ColorRenderer colorRenderer;
	Font font;
	
	public void start()
	{
		window = new Window(1080, 720, "Test");
		window.build();
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
			Launch.this.window.setWidth(width);
			Launch.this.window.setHeight(height);
			fontRenderer.initializeFrameBuffer(width, height);
		});
		window.addEvent((GLFWKeyCallbackI) (id, key, scancode, action, mods) -> {
			if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT) position = switch (key)
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
			else if (action == GLFW.GLFW_RELEASE && key == GLFW.GLFW_KEY_R)
			{
				rotating = !rotating;
				GLFW.glfwSetInputMode(id, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL + (rotating ? 2 : 0));
			}
		});
		GL.createCapabilities();
		frameBuffer = new FrameBuffer();
		shader = new Shader(ShaderInputDataType.PER_VERTEX, ShaderInputDataType.UNUSUED, ShaderInputDataType.UNUSUED);
		try
		{
			shader.loadShaderFile(new File(colorShaderVertexPath), ShaderType.VERTEX);
			shader.loadShaderFile(new File(colorShaderFragmentPath), ShaderType.FRAGMENT);
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
		}
		shaderTexture = new Shader(ShaderInputDataType.PER_VERTEX, ShaderInputDataType.PER_VERTEX, ShaderInputDataType.UNUSUED);
		try
		{
			shaderTexture.loadShaderFile(new File(textureShaderVertexPath), ShaderType.VERTEX);
			shaderTexture.loadShaderFile(new File(textureShaderFragmentPath), ShaderType.FRAGMENT);
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
		}
		renderer = new Renderer(shader);
		colorRenderer = new ColorRenderer(shader);
		textureRenderer = new TextureRenderer(shaderTexture);
		Shader fontShader = new Shader(ShaderInputDataType.UNUSUED, ShaderInputDataType.PER_VERTEX, ShaderInputDataType.UNUSUED);
		try
		{
			fontShader.loadShaderFile(new File(fontShaderVertexPath), ShaderType.VERTEX);
			fontShader.loadShaderFile(new File(fontShaderFragmentPath), ShaderType.FRAGMENT);
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
		}
		fontRenderer = new FontRenderer(fontShader, shaderTexture);
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
		window.setIcon(icon);
		window.show();
		window.addWidget(new VisibleButton(new Vector3i(10, 10, 0), new Vector2i(16, 16), window));
		
		frameBuffer.initializeDepthTextureBuffer(1024, 1024);
		fontRenderer.initializeFrameBuffer(window.getWidth(), window.getHeight());
		
		textureRenderer.loadTexture(new ColorTexture(icon));
		
		colorRenderer.loadQuad(0, 0, 0, 100, 100, 0, 255, 254, 2, 255);
	}
	
	FrameBuffer frameBuffer;
	Image icon;
	Random random = new Random();
	Integer iteration = 0;
	double o = 0;
	String s = "";
	Vector3f position = new Vector3f(0, 0, 0);
	float rotationX = 0, rotationY = 0, partial;
	boolean rotating = false;
	
	@SuppressWarnings("unused")
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
		printError("compact");
		
		fontRenderer.drawString(compact, font, new Text("ala.beta@delta"), new Vector2i(0, (int) o % 180), new Vector3f(255, 0, 0), 0.5F, 0);
		fontRenderer.drawString(compact, font, new Text(Util.toPointString(cursor)), new Vector2i(cursor.subtract(window.getSize().multiply(0.5F))), new Vector2f(cursor.x() * 255F / window.getWidth(), cursor.y() * 255F / window.getHeight()), 0.3F, 0);
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
		fontRenderer.drawString(compact, font, text, new Vector2i(-450, 500), 1, 1000, 0);
		printError("FontRender texting");
		fontRenderer.drawTexture(identity, 0F);
		printError("FontRender draw");
		
		frameBuffer.bind();
		printError("FrameBuffer draw");
		
		renderer.clear();
		renderer.color(255, 255, 0, 255);
		renderer.loadCube(identity, Vector3f.getOrigin(), end);
		// render.loadCube(viewer, end, end.add(end));
		// render.draw(GL30.GL_QUADS, projected);
		printError("TextureRender draw");
		
		FrameBuffer.unbind();
		window.update(compact, colorRenderer, textureRenderer, partialTicks);
		o += partialTicks * 1;
	}
}
