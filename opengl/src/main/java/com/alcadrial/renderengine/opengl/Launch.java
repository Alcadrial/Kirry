package com.alcadrial.renderengine.opengl;

import static org.lwjgl.opengl.GL11.GL_DEPTH_COMPONENT;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_STENCIL_INDEX;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_1D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_3D;
import static org.lwjgl.opengl.GL12.GL_UNSIGNED_INT_8_8_8_8;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_DEPTH_ATTACHMENT;
import static org.lwjgl.opengl.GL30.GL_DEPTH_STENCIL;
import static org.lwjgl.opengl.GL30.GL_DEPTH_STENCIL_ATTACHMENT;
import static org.lwjgl.opengl.GL30.GL_STENCIL_ATTACHMENT;
import static org.lwjgl.opengl.GL30.GL_UNSIGNED_INT_24_8;
import static org.lwjgl.opengl.GL31.GL_TEXTURE_RECTANGLE;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

import org.lwjgl.glfw.GLFW;

import com.alcadra.tester.UltimateTester;
import com.alcadrial.renderengine.core.util.Test;
import com.alcadrial.renderengine.opengl.buffer.ElementArrayBuffer;
import com.alcadrial.renderengine.opengl.buffer.ElementArrayBufferStack;
import com.alcadrial.renderengine.opengl.buffer.FloatArrayBuffer;
import com.alcadrial.renderengine.opengl.buffer.FloatArrayBufferStack;
import com.alcadrial.renderengine.opengl.shader.Shader;
import com.alcadrial.renderengine.opengl.shader.ShaderInput;
import com.alcadrial.renderengine.opengl.shader.ShaderProgram;
import com.alcadrial.renderengine.opengl.shader.ShaderType;
import com.alcadrial.renderengine.opengl.textures.Texture;
import com.alcadrial.renderengine.opengl.textures.TextureTemplate;
import com.alcadrial.renderengine.opengl.textures.TextureTemplateException;
import com.alcadrial.renderengine.opengl.util.Deletable;
import com.alcadrial.renderengine.opengl.window.Window;

@Test
public class Launch {
	
	private static Window window;
	
	public static void main(String[] args) throws IOException
	{
		Pattern pattern;
		if (args.length == 0) pattern = Pattern.compile(".*");
		else pattern = Pattern.compile(args[0]);
		
		RenderEngine.enableDebug();
		if (!GLFW.glfwInit()) return;
		window = new Window(1080, 720, "Test");
		window.create();
		if (!window.isCreated())
		{
			GLFW.glfwTerminate();
			System.out.println("e");
			return;
		}
		window.makeContextCurrent();
		window.init();
		
		FileOutputStream stream = new FileOutputStream("Test Results.txt");
		
		UltimateTester t = new UltimateTester(window::detachCurrentContext, window::makeContextCurrent);
		t.setOutputStream(new MultipleOutputStream(System.out, stream));
		
		if (pattern.matcher("Buffers Test").matches()) t.test("Buffers Test", enclosiveContext(() -> {
			
			t.println();
			boolean floatArrayBuffer = false;
			floatArrayBuffer = t.<Supplier<FloatArrayBuffer>>test("Float Array Buffer", enclosiveContext(builder -> {
				t.assertTest("Constructor 0", b -> b.size() == 0 && b.getTarget() == GL_ARRAY_BUFFER, builder.get());
				t.println();
				t.test("Add", b -> {
					float f = 0.630625F;
					b.add(f);
					t.println("Size = " + b.size());
					t.println("Data = " + b.getFloat(0));
					return b.size() == 1 && f == b.getFloat(0);
				}, builder.get());
				t.test("Add all", enclosiveContext(() -> {
					t.assertTest("Collection", b -> {
						b.addAll(List.of(0F, 2F, 3F));
						t.println("Size = " + b.size());
						t.println("Data = " + b.getFloat(0) + ", " + b.getFloat(1) + ", " + b.getFloat(2));
						return b.size() == 3 && b.getFloat(0) == 0 && b.getFloat(1) == 2 && b.getFloat(2) == 3;
					}, builder.get());
					t.assertTest("Array", b -> {
						b.addAll(new float[] {0, 2, 3});
						t.println("Size = " + b.size());
						t.println("Data = " + b.getFloat(0) + ", " + b.getFloat(1) + ", " + b.getFloat(2));
						return b.size() == 3 && b.getFloat(0) == 0 && b.getFloat(1) == 2 && b.getFloat(2) == 3;
					}, builder.get());
					return true;
				}));
				t.println();
				t.test("Merge", enclosiveContext(b -> {
					b.add(12F);
					FloatArrayBuffer extra = builder.get();
					extra.add(0.1F);
					extra.add(11.5F);
					b.merge(extra);
					t.println("Size = " + b.size());
					t.println("Data = " + b.getFloat(0) + ", " + b.getFloat(1) + ", " + b.getFloat(2));
					return b.size() == 3 && b.getFloat(0) == 12F && b.getFloat(1) == 0.1F && b.getFloat(2) == 11.5F;
				}), builder.get());
				t.assertTest("Clear", b -> {
					b.add(0F);
					b.add(1F);
					b.clear();
					t.println("Size = " + b.size());
					return b.size() == 0;
				}, builder.get());
				t.println();
				t.assertTest("GL sync", enclosiveContext(b -> {
					b.bind();
					if (b.getGpuSize() != 0) return false;
					float[] data;
					
					b.add(0F);
					b.add(2F);
					b.add(3F);
					b.sync();
					
					t.println("Size = " + b.getGpuSize());
					if (b.getGpuSize() != 3) return false;
					data = b.getGpuFloatData();
					t.println("Data = " + data[0] + ", " + data[1] + ", " + data[2]);
					if (data.length != 3 || data[0] != 0 || data[1] != 2 || data[2] != 3) return false;
					
					t.println();
					b.add(4F);
					b.sync();
					
					t.println("Size = " + b.getGpuSize());
					if (b.getGpuSize() != 4) return false;
					data = b.getGpuFloatData();
					t.println("Data = " + data[0] + ", " + data[1] + ", " + data[2] + ", " + data[3]);
					if (data.length != 4 || data[0] != 0 || data[1] != 2 || data[2] != 3 || data[3] != 4) return false;
					
					t.println();
					b.sync();
					
					t.println("Size = " + b.getGpuSize());
					if (b.getGpuSize() != 4) return false;
					data = b.getGpuFloatData();
					t.println("Data = " + data[0] + ", " + data[1] + ", " + data[2] + ", " + data[3]);
					if (data.length != 4 || data[0] != 0 || data[1] != 2 || data[2] != 3 || data[3] != 4) return false;
					
					t.println();
					b.clear();
					b.add(0F);
					b.add(1F);
					b.add(2F);
					b.add(4F);
					b.sync();
					
					t.println("Size = " + b.getGpuSize());
					if (b.getGpuSize() != 4) return false;
					data = b.getGpuFloatData();
					t.println("Data = " + data[0] + ", " + data[1] + ", " + data[2] + ", " + data[3]);
					return data.length == 4 && data[0] == 0 && data[1] == 1 && data[2] == 2 && data[3] == 4;
				}), builder.get());
				return true;
			}), FloatArrayBuffer::new);
			@SuppressWarnings("unused")
			boolean floatArrayBufferStack = false;
			if (floatArrayBuffer) floatArrayBufferStack = t.test("Float Array Buffer Stack", enclosiveContext(() -> {
				t.assertTest("Constructor", b -> b.size() == 0 && b.current() == b, new FloatArrayBufferStack());
				t.println();
				t.assertTest("Push", enclosiveContext(b -> {
					try (FloatArrayBuffer b0 = new FloatArrayBuffer(); FloatArrayBuffer b1 = new FloatArrayBuffer(); FloatArrayBuffer b2 = new FloatArrayBuffer(); FloatArrayBuffer b3 = new FloatArrayBuffer())
					{
						b.push(b0);
						t.println("Current = " + b.current());
						if (b.current() != b0 && b.size() != 1) return false;
						b.push(b1);
						b.push(b2);
						t.println("Current = " + b.current());
						if (b.current() != b2 && b.size() != 3) return false;
						b.push(b1);
						b.push(b3);
						b.push(b);
						t.println("Current = " + b.current());
						if (b.current() != b && b.size() != 6) return false;
						return true;
					}
				}), new FloatArrayBufferStack());
				t.assertTest("Pull", enclosiveContext(b -> {
					try (FloatArrayBuffer b0 = new FloatArrayBuffer(); FloatArrayBuffer b1 = new FloatArrayBuffer(); FloatArrayBuffer b2 = new FloatArrayBuffer(); FloatArrayBuffer b3 = new FloatArrayBuffer())
					{
						b.push(b0);
						b.pull();
						b.push(b2);
						t.println("Current = " + b.current());
						if (b.current() != b2 && b.size() != 1) return false;
						b.push(b1);
						b.push(b3);
						b.pull();
						b.push(b);
						b.pull();
						b.pull();
						b.pull();
						t.println("Current = " + b.current());
						if (b.current() != b && b.size() != 0) return false;
						b.pull();
						t.println("Current = " + b.current());
						if (b.current() != b && b.size() != 0) return false;
						return true;
					}
				}), new FloatArrayBufferStack());
				t.println();
				t.<Supplier<FloatArrayBuffer>>assertTest("Superclass methods", t.getLastPredicateTestCast("Float Array Buffer"), FloatArrayBufferStack::new);
				FloatArrayBufferStack stack = new FloatArrayBufferStack();
				t.<Supplier<FloatArrayBuffer>>assertTest("Pulled Superclass methods", t.getLastPredicateTestCast("Float Array Buffer"), () -> {
					stack.pull();
					stack.push(new FloatArrayBuffer());
					return stack.current();
				});
				stack.close();
				return true;
			}));
			boolean elementArrayBuffer = t.<Supplier<ElementArrayBuffer>>test("Element Array Buffer", enclosiveContext(builder -> {
				t.assertTest("Constructor", b -> b.size() == 0 && b.getTarget() == GL_ELEMENT_ARRAY_BUFFER, builder.get());
				t.println();
				t.assertTest("Add", b -> {
					b.add(0);
					b.add(2);
					b.add(3);
					t.println("Size = " + b.size());
					t.println("Data = " + b.get(0) + ", " + b.get(1) + ", " + b.get(2));
					return b.size() == 3 && b.get(0) == 0 && b.get(1) == 2 && b.get(2) == 3;
				}, builder.get());
				t.test("Add all", enclosiveContext(() -> {
					t.assertTest("Collection", b -> {
						b.addAll(List.of(0, 2, 3));
						t.println("Size = " + b.size());
						t.println("Data = " + b.get(0) + ", " + b.get(1) + ", " + b.get(2));
						return b.size() == 3 && b.get(0) == 0 && b.get(1) == 2 && b.get(2) == 3;
					}, builder.get());
					t.assertTest("Array", b -> {
						b.addAll(new int[] {0, 2, 3});
						t.println("Size = " + b.size());
						t.println("Data = " + b.get(0) + ", " + b.get(1) + ", " + b.get(2));
						return b.size() == 3 && b.get(0) == 0 && b.get(1) == 2 && b.get(2) == 3;
					}, builder.get());
					t.assertTest("ElementArrayBuffer", enclosiveContext(b -> {
						ElementArrayBuffer extra = builder.get();
						extra.add(0);
						extra.add(2);
						extra.add(3);
						b.addAll(extra);
						t.println("Size = " + b.size());
						t.println("Data = " + b.get(0) + ", " + b.get(1) + ", " + b.get(2));
						return b.size() == 3 && b.get(0) == 0 && b.get(1) == 2 && b.get(2) == 3;
					}), builder.get());
					return true;
				}));
				t.println();
				t.test("Merge", enclosiveContext(b -> {
					ElementArrayBuffer extra = builder.get();
					extra.add(0);
					extra.add(1);
					b.merge(extra, 0);
					b.merge(extra, 2);
					t.println("Size = " + b.size());
					t.println("Data = " + b.get(0) + ", " + b.get(1) + ", " + b.get(2) + ", " + b.get(3));
					return b.size() == 4 && b.get(0) == 0 && b.get(1) == 1 && b.get(2) == 2 && b.get(3) == 3;
				}), builder.get());
				t.assertTest("Clear", b -> {
					b.add(0);
					b.add(1);
					b.clear();
					t.println("Size = " + b.size());
					return b.size() == 0;
				}, builder.get());
				t.println();
				t.assertTest("GL sync", enclosiveContext(b -> {
					b.bind();
					if (b.getGpuSize() != 0) return false;
					int[] data;
					
					b.add(0);
					b.add(2);
					b.add(3);
					b.sync();
					
					t.println("Size = " + b.getGpuSize());
					if (b.getGpuSize() != 3) return false;
					data = b.getGpuIntData();
					t.println("Data = " + data[0] + ", " + data[1] + ", " + data[2]);
					if (data.length != 3 || data[0] != 0 || data[1] != 2 || data[2] != 3) return false;
					
					t.println();
					b.add(4);
					b.sync();
					
					t.println("Size = " + b.getGpuSize());
					if (b.getGpuSize() != 4) return false;
					data = b.getGpuIntData();
					t.println("Data = " + data[0] + ", " + data[1] + ", " + data[2] + ", " + data[3]);
					if (data.length != 4 || data[0] != 0 || data[1] != 2 || data[2] != 3 || data[3] != 4) return false;
					
					t.println();
					b.sync();
					
					t.println("Size = " + b.getGpuSize());
					if (b.getGpuSize() != 4) return false;
					data = b.getGpuIntData();
					t.println("Data = " + data[0] + ", " + data[1] + ", " + data[2] + ", " + data[3]);
					if (data.length != 4 || data[0] != 0 || data[1] != 2 || data[2] != 3 || data[3] != 4) return false;
					
					t.println();
					b.clear();
					b.add(0);
					b.add(1);
					b.add(2);
					b.add(4);
					b.sync();
					
					t.println("Size = " + b.getGpuSize());
					if (b.getGpuSize() != 4) return false;
					data = b.getGpuIntData();
					t.println("Data = " + data[0] + ", " + data[1] + ", " + data[2] + ", " + data[3]);
					return data.length == 4 && data[0] == 0 && data[1] == 1 && data[2] == 2 && data[3] == 4;
				}), builder.get());
				t.test("Get array", enclosiveContext(b -> {
					b.bind();
					b.add(0);
					b.add(1);
					b.add(2);
					b.add(4);
					b.sync();
					int[] data = b.getGpuIntData();
					t.println("Length = " + data.length);
					t.println("Data = " + data[0] + ", " + data[1] + ", " + data[2] + ", " + data[3]);
					return data.length == 4 && data[0] == 0 && data[1] == 1 && data[2] == 2 && data[3] == 4;
				}), builder.get());
				return true;
			}), ElementArrayBuffer::new);
			@SuppressWarnings("unused")
			boolean elementArrayBufferStack = false;
			if (elementArrayBuffer) elementArrayBufferStack = t.test("Element Array Buffer Stack", enclosiveContext(() -> {
				t.assertTest("Constructor", b -> b.size() == 0 && b.current() == b, new ElementArrayBufferStack());
				t.println();
				t.assertTest("Push", enclosiveContext(b -> {
					try (ElementArrayBuffer b0 = new ElementArrayBuffer(); ElementArrayBuffer b1 = new ElementArrayBuffer(); ElementArrayBuffer b2 = new ElementArrayBuffer(); ElementArrayBuffer b3 = new ElementArrayBuffer())
					{
						b.push(b0);
						t.println("Current = " + b.current());
						if (b.current() != b0 && b.size() != 1) return false;
						b.push(b1);
						b.push(b2);
						t.println("Current = " + b.current());
						if (b.current() != b2 && b.size() != 3) return false;
						b.push(b1);
						b.push(b3);
						b.push(b);
						t.println("Current = " + b.current());
						if (b.current() != b && b.size() != 6) return false;
						return true;
					}
				}), new ElementArrayBufferStack());
				t.assertTest("Pull", enclosiveContext(b -> {
					try (ElementArrayBuffer b0 = new ElementArrayBuffer(); ElementArrayBuffer b1 = new ElementArrayBuffer(); ElementArrayBuffer b2 = new ElementArrayBuffer(); ElementArrayBuffer b3 = new ElementArrayBuffer())
					{
						b.push(b0);
						b.pull();
						b.push(b2);
						t.println("Current = " + b.current());
						if (b.current() != b2 && b.size() != 1) return false;
						b.push(b1);
						b.push(b3);
						b.pull();
						b.push(b);
						b.pull();
						b.pull();
						b.pull();
						t.println("Current = " + b.current());
						if (b.current() != b && b.size() != 0) return false;
						b.pull();
						t.println("Current = " + b.current());
						if (b.current() != b && b.size() != 0) return false;
						return true;
					}
				}), new ElementArrayBufferStack());
				t.println();
				t.<Supplier<ElementArrayBuffer>>assertTest("Superclass methods", t.getLastPredicateTestCast("Element Array Buffer"), ElementArrayBufferStack::new);
				ElementArrayBufferStack stack = new ElementArrayBufferStack();
				t.<Supplier<ElementArrayBuffer>>assertTest("Pulled Superclass methods", t.getLastPredicateTestCast("Element Array Buffer"), () -> {
					stack.pull();
					stack.push(new ElementArrayBuffer());
					return stack.current();
				});
				stack.close();
				return true;
			}));
		}));
		
		if (pattern.matcher("Shader").matches()) t.test("Shader", enclosiveContext(() -> {
			
			Shader shader = new Shader(new File("src/main/resources/assets/renderengine/colored_texture.vert"));
			
			t.println("Type = " + shader.getType());
			
			if (shader.getType() != ShaderType.VERTEX) return false;
			if (shader.getStatus().isCompiled()) return false;
			if (shader.getStatus().getId() != shader.getId()) return false;
			
			try
			{
				shader.loadShader();
			}
			catch (IOException e)
			{
				return false;
			}
			
			t.println("Status = " + shader.getStatus());
			
			if (!shader.getStatus().isCompiled()) return false;
			if (shader.getStatus().getId() != shader.getId()) return false;
			
			try (ShaderProgram program = new ShaderProgram(ShaderInput.VERTEX_POSITION, ShaderInput.COLOR, ShaderInput.UV))
			{
				t.println("Id = " + program.getId());
				
				if (program.getId() == 0) return false;
				
				t.println("Known Inputs = " + program.getKnownInputs());
				
				if (program.getKnownInputs().size() != 3) return false;
				if (program.getProgramInputs().size() != 0) return false;
				if (program.getStatus().isBuilded()) return false;
				
				program.attachShader(shader);
				
				t.println("Vertex Shaders = " + program.getShaders(ShaderType.VERTEX));
				
				if (program.getShaders(ShaderType.VERTEX).size() != 1) return false;
				
				program.build();
				
				t.println("Real Inputs = " + program.getProgramInputs());
				
				if (!program.getStatus().isBuilded()) return false;
				if (program.getProgramInputs().size() != 3) return false;
				if (!program.getProgramInputs().equals(program.getKnownInputs())) return false;
			}
			
			return true;
		}));
		if (pattern.matcher("Texture").matches()) t.test("Texture", enclosiveContext(() -> {
			
			boolean textureTemplates = t.test("Texture Templates", () -> {
				
				t.println("RGBA_2D_TEXTURE = " + TextureTemplate.RGBA_2D_TEXTURE);
				if (!check(TextureTemplate.RGBA_2D_TEXTURE, GL_TEXTURE_2D, GL_RGBA, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8, GL_COLOR_ATTACHMENT0)) return false;
				t.println("RGB_2D_TEXTURE = " + TextureTemplate.RGB_2D_TEXTURE);
				if (!check(TextureTemplate.RGB_2D_TEXTURE, GL_TEXTURE_2D, GL_RGB, GL_RGB, GL_UNSIGNED_INT_8_8_8_8, GL_COLOR_ATTACHMENT0)) return false;
				t.println("RGBA_CUBE_MAP_TEXTURE = " + TextureTemplate.RGBA_CUBE_MAP_TEXTURE);
				if (!check(TextureTemplate.RGBA_CUBE_MAP_TEXTURE, GL_TEXTURE_CUBE_MAP, GL_RGBA, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8, GL_COLOR_ATTACHMENT0)) return false;
				t.println("RGBA_RECTANGLE_TEXTURE = " + TextureTemplate.RGBA_RECTANGLE_TEXTURE);
				if (!check(TextureTemplate.RGBA_RECTANGLE_TEXTURE, GL_TEXTURE_RECTANGLE, GL_RGBA, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8, GL_COLOR_ATTACHMENT0)) return false;
				t.println("DEPTH_2D_TEXTURE = " + TextureTemplate.DEPTH_2D_TEXTURE);
				if (!check(TextureTemplate.DEPTH_2D_TEXTURE, GL_TEXTURE_2D, GL_DEPTH_COMPONENT, GL_DEPTH_COMPONENT, GL_FLOAT, GL_DEPTH_ATTACHMENT)) return false;
				t.println("STENCIL_2D_TEXTURE = " + TextureTemplate.STENCIL_2D_TEXTURE);
				if (!check(TextureTemplate.STENCIL_2D_TEXTURE, GL_TEXTURE_2D, GL_DEPTH_STENCIL, GL_STENCIL_INDEX, GL_UNSIGNED_BYTE, GL_STENCIL_ATTACHMENT)) return false;
				t.println("DEPTH_STENCIL_2D_TEXTURE = " + TextureTemplate.DEPTH_STENCIL_2D_TEXTURE);
				if (!check(TextureTemplate.DEPTH_STENCIL_2D_TEXTURE, GL_TEXTURE_2D, GL_DEPTH_STENCIL, GL_DEPTH_STENCIL, GL_UNSIGNED_INT_24_8, GL_DEPTH_STENCIL_ATTACHMENT)) return false;
				
				if (!checkException(() -> new TextureTemplate(0, GL_RGBA, GL_RGBA, GL_UNSIGNED_INT, GL_COLOR_ATTACHMENT0), TextureTemplateException.class)) return false;
				if (!checkException(() -> new TextureTemplate(GL_TEXTURE_2D, 0, GL_RGBA, GL_UNSIGNED_INT, GL_COLOR_ATTACHMENT0), TextureTemplateException.class)) return false;
				if (!checkException(() -> new TextureTemplate(GL_TEXTURE_2D, GL_RGBA, 0, GL_UNSIGNED_INT, GL_COLOR_ATTACHMENT0), TextureTemplateException.class)) return false;
				if (!checkException(() -> new TextureTemplate(GL_TEXTURE_2D, GL_RGBA, GL_RGBA, 0, GL_COLOR_ATTACHMENT0), TextureTemplateException.class)) return false;
				if (!checkException(() -> new TextureTemplate(GL_TEXTURE_2D, GL_RGBA, GL_RGBA, GL_UNSIGNED_INT, 0), TextureTemplateException.class)) return false;
				
				return true;
			});
			
			boolean textureTest = false;
			if (textureTemplates) textureTest = t.test("Texture", enclosiveContext(() -> {
				
				TextureTemplate rgba2d = TextureTemplate.RGBA_2D_TEXTURE;
				TextureTemplate rgba1d = TextureTemplate.RGBA_2D_TEXTURE.withTarget(GL_TEXTURE_1D);
				TextureTemplate rgba3d = TextureTemplate.RGBA_2D_TEXTURE.withTarget(GL_TEXTURE_3D);
				
				t.assertTest("Constructor", enclosiveContext(tryTexture(texture -> texture.getId() == 1 && texture.getTemplate() == rgba2d)), new Texture(rgba2d));
				
				t.assertTest("SetData", dimensions(t, enclosiveContext(() -> {
					try (Texture texture = new Texture(rgba1d))
					{
						int[] data = new int[] {0, 1, 2, 3, 4, 5, 6, 7};
						t.println("Inserted Data = " + h(data));
						
						texture.setData1D(texture.getTemplate(), data, 8, 0);
						
						t.println(texture);
						
						if (texture.getWidth() != 8 || texture.getHeight() != 1 || texture.getDepth() != 1 || texture.getLength() != data.length) return false;
						if (texture.getGlWidth() != 8 || texture.getGlHeight() != 1 || texture.getGlDepth() != 1 || texture.getLength() != data.length) return false;
						
						int[] data0 = texture.getDataArray();
						t.println("Retrived Data = " + h(data0));
						
						return Arrays.equals(data, data0);
					}
				}), enclosiveContext(() -> {
					try (Texture texture = new Texture(rgba2d))
					{
						int[] data = new int[] {0, 1, 2, 3, 4, 5, 6, 7};
						t.println("Inserted Data = " + h(data));
						
						texture.setData2D(TextureTemplate.RGBA_2D_TEXTURE, data, 4, 2, 0);
						
						t.println(texture);
						
						if (texture.getWidth() != 4 || texture.getHeight() != 2 || texture.getDepth() != 1 || texture.getLength() != data.length) return false;
						if (texture.getGlWidth() != 4 || texture.getGlHeight() != 2 || texture.getGlDepth() != 1 || texture.getLength() != data.length) return false;
						
						int[] data0 = texture.getDataArray();
						t.println("Retrived Data = " + h(data0));
						
						return Arrays.equals(data, data0);
					}
				}), enclosiveContext(() -> {
					try (Texture texture = new Texture(rgba3d))
					{
						int[] data = new int[] {0, 1, 2, 3, 4, 5, 6, 7};
						t.println("Inserted Data = " + h(data));
						
						texture.setData3D(TextureTemplate.RGBA_2D_TEXTURE, data, 2, 2, 2, 0);
						
						t.println(texture);
						
						if (texture.getWidth() != 2 || texture.getHeight() != 2 || texture.getDepth() != 2 || texture.getLength() != data.length) return false;
						if (texture.getGlWidth() != 2 || texture.getGlHeight() != 2 || texture.getGlDepth() != 2 || texture.getLength() != data.length) return false;
						
						int[] data0 = texture.getDataArray();
						t.println("Retrived Data = " + h(data0));
						
						return Arrays.equals(data, data0);
					}
				})));
				
				t.assertTest("ClearRegion", dimensions(t, enclosiveContext(() -> {
					try (Texture texture = buildTestTexture(rgba1d, 8, 0))
					{
						texture.clearRegion1D(0x57575744, 1, 4);
						
						int[] data0 = texture.getDataArray();
						t.println("Data = " + h(data0));
						int[] data = new int[] {0, 0x57575744, 0x57575744, 0x57575744, 0x57575744, 5, 6, 7};
						return Arrays.equals(data, data0);
					}
				}), enclosiveContext(() -> {
					try (Texture texture = buildTestTexture(rgba2d, 4, 2, 0))
					{
						texture.clearRegion2D(0x2478936A, 1, 0, 1, 2);
						
						int[] data0 = texture.getDataArray();
						t.println("Data = " + h(data0));
						int[] data = new int[] {0, 0x2478936A, 2, 3, 4, 0x2478936A, 6, 7};
						return Arrays.equals(data, data0);
					}
				}), enclosiveContext(() -> {
					try (Texture texture = buildTestTexture(rgba3d, 2, 2, 2, 0))
					{
						texture.clearRegion3D(0, 1, 0, 0, 1, 2, 2);
						
						int[] data0 = texture.getDataArray();
						t.println("Data = " + h(data0));
						int[] data = new int[] {0, 0, 2, 0, 4, 0, 6, 0};
						return Arrays.equals(data, data0);
					}
				})));
				
				t.assertTest("Resize", dimensions(t, enclosiveContext(() -> {
					try (Texture texture = buildTestTexture(rgba1d, 8, 0))
					{
						texture.resize1D(16);
						
						int[] data0 = texture.getDataArray();
						t.println("Data = " + h(data0));
						int[] data = new int[] {0, 1, 2, 3, 4, 5, 6, 7};
						return data0.length == 16 && Arrays.equals(data, 0, 8, data0, 0, 8);
					}
				}), enclosiveContext(() -> {
					try (Texture texture = buildTestTexture(rgba2d, 4, 2, 0))
					{
						texture.resize2D(8, 4);
						
						int[] data0 = texture.getDataArray();
						t.println("Data = " + h(data0));
						int[] data = new int[] {0, 1, 2, 3, 4, 5, 6, 7};
						return data0.length == 32 && Arrays.equals(data, 0, 4, data0, 0, 4) && Arrays.equals(data, 4, 8, data0, 8, 12);
					}
				}), enclosiveContext(() -> {
					try (Texture texture = buildTestTexture(rgba3d, 2, 2, 2, 0))
					{
						texture.resize3D(4, 4, 4);
						
						int[] data0 = texture.getDataArray();
						t.println("Data = " + h(data0));
						int[] data = new int[] {0, 1, 2, 3, 4, 5, 6, 7};
						if (!Arrays.equals(data, 0, 2, data0, 0, 2) || !Arrays.equals(data, 2, 4, data0, 4, 6)) return false;
						if (!Arrays.equals(data, 4, 6, data0, 16, 18) || !Arrays.equals(data, 6, 8, data0, 20, 22)) return false;
						return data0.length == 64;
					}
				})));
				
				t.test("Copy", dimensions(t, enclosiveContext(() -> {
					try (Texture texture = buildTestTexture(rgba1d, 8, 0))
					{
						try (Texture texture0 = buildTestTexture(rgba1d, 8, 8))
						{
							int[] data0 = texture.getDataArray();
							t.println("Data = " + h(data0));
							data0 = texture0.getDataArray();
							t.println("Data = " + h(data0));
							
							texture.copyTo1D(texture0, 1, 2, 4);
							
							data0 = texture0.getDataArray();
							t.println("Result = " + h(data0));
							int[] data = new int[] {8, 9, 1, 2, 3, 4, 14, 15};
							return data0.length == 8 && Arrays.equals(data, 0, 8, data0, 0, 8);
						}
					}
				}), enclosiveContext(() -> {
					try (Texture texture = buildTestTexture(rgba2d, 4, 2, 0))
					{
						try (Texture texture0 = buildTestTexture(rgba2d, 4, 2, 8))
						{
							texture.copyTo2D(texture0, 0, 0, 0, 0, 2, 2);
							
							int[] data0 = texture0.getDataArray();
							t.println("Data = " + h(data0));
							int[] data = new int[] {0, 1, 10, 11, 4, 5, 14, 15};
							return data0.length == 8 && Arrays.equals(data, data0);
						}
					}
				}), enclosiveContext(() -> {
					try (Texture texture = buildTestTexture(rgba3d, 2, 2, 2, 0))
					{
						try (Texture texture0 = buildTestTexture(rgba3d, 2, 2, 2, 8))
						{
							texture.copyTo3D(texture0, 0, 0, 0, 0, 0, 0, 1, 2, 2);
							
							int[] data0 = texture0.getDataArray();
							t.println("Data = " + h(data0));
							int[] data = new int[] {0, 9, 2, 11, 4, 13, 6, 15};
							return data0.length == 8 && Arrays.equals(data, data0);
						}
					}
				})));
				
				return true;
			}));
			
			return textureTest;
		}));
		stream.close();
		window.swapBuffers();
		RenderEngine.terminate();
	}
	
	public static boolean check(TextureTemplate template, int target, int internalFormat, int textureFormat, int dataType, int attachment)
	{
		return template.target() == target && template.internalFormat() == internalFormat && template.textureFormat() == textureFormat && template.dataType() == dataType && template.attachment() == attachment;
	}
	
	public static long l(byte b)
	{
		return Byte.toUnsignedLong(b);
	}
	
	public static int i(byte b)
	{
		return Byte.toUnsignedInt(b);
	}
	
	public static String h(byte b)
	{
		String h = h(i(b));
		if (h.length() == 1) h = "0" + h;
		return h;
	}
	
	public static String h(int i)
	{
		return Integer.toHexString(i).toUpperCase();
	}
	
	public static String h(int[] i)
	{
		String s = StreamSupport.intStream(Spliterators.spliterator(i, 0), false).collect(StringBuilder::new, (b, j) -> b.append(h(j)).append(", "), StringBuilder::append).toString();
		if (s.length() == 0) return "[]";
		return '[' + s.substring(0, s.length() - 2) + ']';
	}
	
	public static <T extends Deletable> Predicate<T> tryTexture(Predicate<T> predicate)
	{
		return texture -> {
			try
			{
				return predicate.test(texture);
			}
			finally
			{
				texture.close();
			}
		};
	}
	
	public static boolean closeAll(boolean result, Closeable... closeables) throws IOException
	{
		for (Closeable closeable : closeables) closeable.close();
		return result;
	}
	
	public static boolean closeAllNoException(boolean result, Closeable... closeables)
	{
		try
		{
			for (Closeable closeable : closeables) closeable.close();
		}
		catch (IOException e)
		{}
		return result;
	}
	
	public static Texture buildTestTexture(TextureTemplate template, int width, int offset)
	{
		Texture texture = new Texture(template);
		int[] data = new int[width];
		for (int i = 0; i < data.length; i++) data[i] = i + offset;
		texture.setData1D(template, data, width, 0);
		return texture;
	}
	
	public static Texture buildTestTexture(TextureTemplate template, int width, int height, int offset)
	{
		Texture texture = new Texture(template);
		int[] data = new int[width * height];
		for (int i = 0; i < data.length; i++) data[i] = i + offset;
		texture.setData2D(template, data, width, height, 0);
		return texture;
	}
	
	public static Texture buildTestTexture(TextureTemplate template, int width, int height, int depth, int offset)
	{
		Texture texture = new Texture(template);
		int[] data = new int[width * height * depth];
		for (int i = 0; i < data.length; i++) data[i] = i + offset;
		texture.setData3D(template, data, width, height, depth, 0);
		return texture;
	}
	
	public static boolean checkException(Runnable runnable, Class<? extends Exception> clazz)
	{
		try
		{
			runnable.run();
			return false;
		}
		catch (Exception e)
		{
			return clazz.isInstance(e);
		}
	}
	
	public static boolean checkException(Runnable runnable)
	{
		try
		{
			runnable.run();
			return false;
		}
		catch (Exception e)
		{
			return true;
		}
	}
	
	public static boolean checkNoException(Runnable runnable, Class<? extends Exception> clazz)
	{
		try
		{
			runnable.run();
			return true;
		}
		catch (Exception e)
		{
			return !clazz.isInstance(e);
		}
	}
	
	public static boolean checkNoException(Runnable runnable)
	{
		try
		{
			runnable.run();
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	public static Runnable enclosiveContext(Runnable runnable)
	{
		return () -> {
			window.makeContextCurrent();
			try
			{
				runnable.run();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				throw e;
			}
			finally
			{
				window.detachCurrentContext();
			}
		};
	}
	
	public static Supplier<Boolean> enclosiveContext(Supplier<Boolean> supplier)
	{
		return () -> {
			window.makeContextCurrent();
			try
			{
				return supplier.get();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				throw e;
			}
			finally
			{
				window.detachCurrentContext();
			}
		};
	}
	
	public static <T> Predicate<T> enclosiveContext(Predicate<T> predicate)
	{
		return t -> {
			window.makeContextCurrent();
			try
			{
				return predicate.test(t);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				throw e;
			}
			finally
			{
				window.detachCurrentContext();
			}
		};
	}
	
	public static Runnable dimensions(UltimateTester t, Runnable... runnables)
	{
		return enclosiveContext(() -> { for (int i = 0; i < runnables.length; i++) t.test(i + 1 + "D", runnables[i]); });
	}
	
	@SafeVarargs
	public static Supplier<Boolean> dimensions(UltimateTester t, Supplier<Boolean>... suppliers)
	{
		return enclosiveContext(() -> {
			boolean result = true;
			for (int i = 0; i < suppliers.length; i++) result &= t.test(i + 1 + "D", suppliers[i]);
			return result;
		});
	}
	
	@SafeVarargs
	public static <T> Predicate<T> dimensions(UltimateTester t, Predicate<T>... predicates)
	{
		return enclosiveContext(value -> {
			boolean result = true;
			for (int i = 0; i < predicates.length; i++) result &= t.test(i + 1 + "D", predicates[i], value);
			return result;
		});
	}
	
	public static class MultipleOutputStream extends OutputStream {
		
		private OutputStream[] streams;
		
		public MultipleOutputStream(OutputStream... streams)
		{
			this.streams = streams;
		}
		
		@Override
		public void write(int b) throws IOException
		{
			for (OutputStream outputStream : streams) if (outputStream != null) outputStream.write(b);
		}
		
		@Override
		public void write(byte[] b, int off, int len) throws IOException
		{
			for (OutputStream outputStream : streams) if (outputStream != null) outputStream.write(b, off, len);
		}
		
		@Override
		public void flush() throws IOException
		{
			for (OutputStream outputStream : streams) if (outputStream != null) outputStream.flush();
		}
	}
}
