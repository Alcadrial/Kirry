package com.alcadra.renderengine;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.lwjgl.glfw.GLFW;

import com.alcadra.renderengine.buffer.ElementArrayBuffer;
import com.alcadra.renderengine.buffer.ElementArrayBufferStack;
import com.alcadra.renderengine.buffer.FloatArrayBuffer;
import com.alcadra.renderengine.buffer.FloatArrayBufferStack;
import com.alcadra.renderengine.buffer.SimpleBuffer;
import com.alcadra.renderengine.shader.Shader;
import com.alcadra.renderengine.shader.ShaderInput;
import com.alcadra.renderengine.shader.ShaderProgram;
import com.alcadra.renderengine.shader.ShaderType;
import com.alcadra.renderengine.util.Test;
import com.alcadra.renderengine.window.Window;
import com.alcadra.tester.UltimateTester;

@Test
public class Launch {
	
	private static Window window;
	
	public static void main(String[] args) throws IOException
	{
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
		// t.setOutputStream(stream);
		
		t.test("Buffers Test", enclosiveContext(() -> {
			t.assertTest("Simple Buffer", enclosiveContext(b -> {
				int value = glGetInteger(GL_ARRAY_BUFFER_BINDING);
				t.println("Binded Before = " + value);
				if (value != 0 || b.getTarget() != GL_ARRAY_BUFFER) return false;
				
				b.bind();
				value = glGetInteger(GL_ARRAY_BUFFER_BINDING);
				t.println("Binded After = " + value);
				if (value != b.getId()) return false;
				
				b.unbind();
				value = glGetInteger(GL_ARRAY_BUFFER_BINDING);
				t.println("Unbinded = " + value);
				if (value != 0) return false;
				
				int[] valueInside = new int[] {0};
				b.asBinded(() -> {
					valueInside[0] = glGetInteger(GL_ARRAY_BUFFER_BINDING);
					t.println("Binded Inside = " + valueInside[0]);
				});
				value = glGetInteger(GL_ARRAY_BUFFER_BINDING);
				t.println("Binded Outside = " + value);
				return valueInside[0] == b.getId() && value == 0;
			}), new SimpleBuffer() {});
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
					if (b.getGlSize() != 0) return false;
					float[] data;
					
					b.add(0F);
					b.add(2F);
					b.add(3F);
					b.sync();
					
					t.println("Size = " + b.getGlSize());
					if (b.getGlSize() != 3) return false;
					data = b.getGlFloatData();
					t.println("Data = " + data[0] + ", " + data[1] + ", " + data[2]);
					if (data.length != 3 || data[0] != 0 || data[1] != 2 || data[2] != 3) return false;
					
					t.println();
					b.add(4F);
					b.sync();
					
					t.println("Size = " + b.getGlSize());
					if (b.getGlSize() != 4) return false;
					data = b.getGlFloatData();
					t.println("Data = " + data[0] + ", " + data[1] + ", " + data[2] + ", " + data[3]);
					if (data.length != 4 || data[0] != 0 || data[1] != 2 || data[2] != 3 || data[3] != 4) return false;
					
					t.println();
					b.sync();
					
					t.println("Size = " + b.getGlSize());
					if (b.getGlSize() != 4) return false;
					data = b.getGlFloatData();
					t.println("Data = " + data[0] + ", " + data[1] + ", " + data[2] + ", " + data[3]);
					if (data.length != 4 || data[0] != 0 || data[1] != 2 || data[2] != 3 || data[3] != 4) return false;
					
					t.println();
					b.clear();
					b.add(0F);
					b.add(1F);
					b.add(2F);
					b.add(4F);
					b.sync();
					
					t.println("Size = " + b.getGlSize());
					if (b.getGlSize() != 4) return false;
					data = b.getGlFloatData();
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
					FloatArrayBuffer b0 = new FloatArrayBuffer();
					FloatArrayBuffer b1 = new FloatArrayBuffer();
					FloatArrayBuffer b2 = new FloatArrayBuffer();
					FloatArrayBuffer b3 = new FloatArrayBuffer();
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
				}), new FloatArrayBufferStack());
				t.assertTest("Pull", enclosiveContext(b -> {
					FloatArrayBuffer b0 = new FloatArrayBuffer();
					FloatArrayBuffer b1 = new FloatArrayBuffer();
					FloatArrayBuffer b2 = new FloatArrayBuffer();
					FloatArrayBuffer b3 = new FloatArrayBuffer();
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
				}), new FloatArrayBufferStack());
				t.println();
				t.<Supplier<FloatArrayBuffer>>assertTest("Superclass methods", t.getLastPredicateTestCast("Float Array Buffer"), FloatArrayBufferStack::new);
				FloatArrayBufferStack stack = new FloatArrayBufferStack();
				t.<Supplier<FloatArrayBuffer>>assertTest("Pulled Superclass methods", t.getLastPredicateTestCast("Float Array Buffer"), () -> {
					stack.pull();
					stack.push(new FloatArrayBuffer());
					return stack.current();
				});
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
					if (b.getGlSize() != 0) return false;
					int[] data;
					
					b.add(0);
					b.add(2);
					b.add(3);
					b.sync();
					
					t.println("Size = " + b.getGlSize());
					if (b.getGlSize() != 3) return false;
					data = b.getGlData();
					t.println("Data = " + data[0] + ", " + data[1] + ", " + data[2]);
					if (data.length != 3 || data[0] != 0 || data[1] != 2 || data[2] != 3) return false;
					
					t.println();
					b.add(4);
					b.sync();
					
					t.println("Size = " + b.getGlSize());
					if (b.getGlSize() != 4) return false;
					data = b.getGlData();
					t.println("Data = " + data[0] + ", " + data[1] + ", " + data[2] + ", " + data[3]);
					if (data.length != 4 || data[0] != 0 || data[1] != 2 || data[2] != 3 || data[3] != 4) return false;
					
					t.println();
					b.sync();
					
					t.println("Size = " + b.getGlSize());
					if (b.getGlSize() != 4) return false;
					data = b.getGlData();
					t.println("Data = " + data[0] + ", " + data[1] + ", " + data[2] + ", " + data[3]);
					if (data.length != 4 || data[0] != 0 || data[1] != 2 || data[2] != 3 || data[3] != 4) return false;
					
					t.println();
					b.clear();
					b.add(0);
					b.add(1);
					b.add(2);
					b.add(4);
					b.sync();
					
					t.println("Size = " + b.getGlSize());
					if (b.getGlSize() != 4) return false;
					data = b.getGlData();
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
					int[] data = b.getGlData();
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
					ElementArrayBuffer b0 = new ElementArrayBuffer();
					ElementArrayBuffer b1 = new ElementArrayBuffer();
					ElementArrayBuffer b2 = new ElementArrayBuffer();
					ElementArrayBuffer b3 = new ElementArrayBuffer();
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
				}), new ElementArrayBufferStack());
				t.assertTest("Pull", enclosiveContext(b -> {
					ElementArrayBuffer b0 = new ElementArrayBuffer();
					ElementArrayBuffer b1 = new ElementArrayBuffer();
					ElementArrayBuffer b2 = new ElementArrayBuffer();
					ElementArrayBuffer b3 = new ElementArrayBuffer();
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
				}), new ElementArrayBufferStack());
				t.println();
				t.<Supplier<ElementArrayBuffer>>assertTest("Superclass methods", t.getLastPredicateTestCast("Element Array Buffer"), ElementArrayBufferStack::new);
				ElementArrayBufferStack stack = new ElementArrayBufferStack();
				t.<Supplier<ElementArrayBuffer>>assertTest("Pulled Superclass methods", t.getLastPredicateTestCast("Element Array Buffer"), () -> {
					stack.pull();
					stack.push(new ElementArrayBuffer());
					return stack.current();
				});
				return true;
			}));
			t.test("Shader", enclosiveContext(() -> {
				
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
				
				ShaderProgram program = new ShaderProgram(ShaderInput.VERTEX_POSITION, ShaderInput.COLOR, ShaderInput.UV);
				
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
				
				return true;
			}));
		}));
		stream.close();
		GLFW.glfwTerminate();
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
	
	public static String h(int b)
	{
		return Integer.toHexString(b).toUpperCase();
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
}
