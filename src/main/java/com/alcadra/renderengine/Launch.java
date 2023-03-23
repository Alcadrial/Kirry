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

import com.alcadra.renderengine.buffer.ArrayBuffer;
import com.alcadra.renderengine.buffer.ElementArrayBuffer;
import com.alcadra.renderengine.buffer.FloatArrayBuffer;
import com.alcadra.renderengine.buffer.SimpleBuffer;
import com.alcadra.renderengine.shader.ShaderInput;
import com.alcadra.renderengine.shader.ShaderProgram;
import com.alcadra.renderengine.shader.ShaderType;
import com.alcadra.renderengine.shader.ShaderUpdate;
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
		window.build();
		if (!window.isBuild())
		{
			GLFW.glfwTerminate();
			System.out.println("e");
			return;
		}
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
			}), new SimpleBuffer() {
				
				@Override
				public SimpleBuffer getNull()
				{
					return new SimpleBuffer(getTarget(), 0) {
						
						@Override
						public SimpleBuffer getNull()
						{
							return this;
						}
					};
				}
			});
			t.println();
			boolean arrayBuffer = t.test("Array Buffer", enclosiveContext(() -> {
				t.assertTest("Constructor 0", b -> b.size() == 0 && b.getTarget() == GL_ARRAY_BUFFER && b.getId() == 0, ArrayBuffer.NULL);
				/*
				@off
				class ArrayBuffer1 extends ArrayBuffer {
					
					public ArrayBuffer1(int target)
					{
						super(target);
					}
				}
				t.test("Constructor 1", b -> b.size() == 0 && b.getTarget() == 23, new ArrayBuffer1(23));
				class ArrayBuffer2 extends ArrayBuffer {
					
					public ArrayBuffer2(int target, int id)
					{
						super(target, id);
					}
				}
				t.test("Constructor 2", b -> b.size() == 0 && b.getTarget() == 101 && b.getId() == 22, new ArrayBuffer2(101, 22));
				t.println();
				t.assertTest("Byte add", b -> {
					b.add((byte) 0x2D);
					t.println("Size = " + b.size());
					t.println("Data = " + h(b.get(0)));
					return b.size() == 1 && b.get(0) == 0x2D;
				}, new ArrayBuffer());
				t.assertTest("Short add", b -> {
					b.add((short) 0x401);
					t.println("Size = " + b.size());
					t.println("Data = " + h(b.get(0)) + h(b.get(1)));
					return b.size() == 2 && b.get(0) == 0x4 && b.get(1) == 0x01;
				}, new ArrayBuffer());
				t.assertTest("Integer add", b -> {
					b.add(0x2A4211);
					t.println("Size = " + b.size());
					t.println("Data = " + h(b.get(0)) + h(b.get(1)) + h(b.get(2)) + h(b.get(3)));
					return b.size() == 4 && b.get(0) == 0x0 && b.get(1) == 0x2A && b.get(2) == 0x42 && b.get(3) == 0x11;
				}, new ArrayBuffer());
				t.test("Float add", b -> {
					float f = 0.630625F;
					b.add(f);
					t.println("Size = " + b.size());
					float f0 = Float.intBitsToFloat(i(b.get(0)) << 24 | i(b.get(1)) << 16 | i(b.get(2)) << 8 | i(b.get(3)));
					t.println("Data = " + f0);
					return b.size() == 4 && f == f0;
				}, new ArrayBuffer());
				t.test("Long add", b -> {
					b.add(0x2A425728194311L);
					t.println("Size = " + b.size());
					t.println("Data = " + h(b.get(0)) + h(b.get(1)) + h(b.get(2)) + h(b.get(3)) + h(b.get(4)) + h(b.get(5)) + h(b.get(6)) + h(b.get(7)));
					return b.size() == 8 && b.get(0) == 0x0 && b.get(1) == 0x2A && b.get(2) == 0x42 && b.get(3) == 0x57 && b.get(4) == 0x28 && b.get(5) == 0x19 && b.get(6) == 0x43 && b.get(7) == 0x11;
				}, new ArrayBuffer());
				t.test("Double add", b -> {
					double d = Double.POSITIVE_INFINITY;
					b.add(d);
					t.println("Size = " + b.size());
					double d0 = Double.longBitsToDouble(l(b.get(0)) << 56 | l(b.get(1)) << 48 | l(b.get(2)) << 40 | l(b.get(3)) << 32 | l(b.get(4)) << 24 | l(b.get(5)) << 16 | l(b.get(6)) << 8 | l(b.get(7)));
					t.println("Data = " + d0);
					return b.size() == 8 && d == d0;
				}, new ArrayBuffer());
				t.println();
				t.test("Merge", enclosiveContext(b -> {
					b.add((short) 0x1231);
					ArrayBuffer extra = new ArrayBuffer();
					extra.add((short) 0x5555);
					b.merge(extra);
					t.println("Size = " + b.size());
					t.println("Data = " + h(b.get(0)) + h(b.get(1)) + h(b.get(2)) + h(b.get(3)));
					return b.size() == 4 && b.get(0) == 0x12 && b.get(1) == 0x31 && b.get(2) == 0x55 && b.get(3) == 0x55;
				}), new ArrayBuffer());
				t.assertTest("Clear", b -> {
					b.add(0x123155);
					b.clear();
					t.println("Size = " + b.size());
					return b.size() == 0;
				}, new ArrayBuffer());
				t.println();
				t.assertTest("GL sync", enclosiveContext(b -> {
					b.bind();
					if (b.getGlByteSize() != 0) return false;
					byte[] data;
					
					b.add(0x123155);
					b.add((short) 0xAAAA);
					b.add((byte) 0xBB);
					b.sync();
					
					if (b.getGlByteSize() != 7) return false;
					t.println("Size = " + b.getGlByteSize());
					t.println("Data = " + h(b.get(0)) + h(b.get(1)) + h(b.get(2)) + h(b.get(3)) + h(b.get(4)) + h(b.get(5)) + h(b.get(6)));
					data = b.getGlData();
					if (data.length != 7 || data[0] != 0x0 || data[1] != 0x12 || data[2] != 0x31 || data[3] != 0x55 || i(data[4]) != 0xAA || i(data[5]) != 0xAA || i(data[6]) != 0xBB) return false;
					
					t.println();
					b.add((byte) 0xCC);
					b.sync();
					
					if (b.getGlByteSize() != 8) return false;
					t.println("Size = " + b.getGlByteSize());
					t.println("Data = " + h(b.get(0)) + h(b.get(1)) + h(b.get(2)) + h(b.get(3)) + h(b.get(4)) + h(b.get(5)) + h(b.get(6)) + h(b.get(7)));
					data = b.getGlData();
					if (data.length != 8 || data[0] != 0x0 || data[1] != 0x12 || data[2] != 0x31 || data[3] != 0x55 || i(data[4]) != 0xAA || i(data[5]) != 0xAA || i(data[6]) != 0xBB || i(data[7]) != 0xCC) return false;
					
					t.println();
					b.sync();
					
					if (b.getGlByteSize() != 8) return false;
					t.println("Size = " + b.getGlByteSize());
					t.println("Data = " + h(b.get(0)) + h(b.get(1)) + h(b.get(2)) + h(b.get(3)) + h(b.get(4)) + h(b.get(5)) + h(b.get(6)) + h(b.get(7)));
					data = b.getGlData();
					if (data.length != 8 || data[0] != 0x0 || data[1] != 0x12 || data[2] != 0x31 || data[3] != 0x55 || i(data[4]) != 0xAA || i(data[5]) != 0xAA || i(data[6]) != 0xBB || i(data[7]) != 0xCC) return false;
					
					t.println();
					b.clear();
					b.add(0x123157);
					b.add(0xBBAABBCC);
					b.sync();
					
					if (b.getGlByteSize() != 8) return false;
					t.println("Size = " + b.getGlByteSize());
					t.println("Data = " + h(b.get(0)) + h(b.get(1)) + h(b.get(2)) + h(b.get(3)) + h(b.get(4)) + h(b.get(5)) + h(b.get(6)) + h(b.get(7)));
					data = b.getGlData();
					return data.length == 8 && data[0] == 0x0 && data[1] == 0x12 && data[2] == 0x31 && data[3] == 0x57 && i(data[4]) == 0xBB && i(data[5]) == 0xAA && i(data[6]) == 0xBB && i(data[7]) == 0xCC;
				}), new ArrayBuffer());
				t.test("GL int array", enclosiveContext(b -> {
					b.bind();
					b.add(0x1B1A);
					b.add(0x2B2A);
					b.add(0x3B3A);
					b.sync();
					int[] data = b.getGlIntData();
					t.println("Length = " + data.length);
					t.println("Data = " + h(data[0]) + ", " + h(data[1]) + ", " + h(data[2]));
					return data.length == 3 && data[0] == 0x1B1A && data[1] == 0x2B2A && data[2] == 0x3B3A;
				}), new ArrayBuffer());
				t.test("GL float array", enclosiveContext(b -> {
					b.bind();
					b.add(0.5F);
					b.add(-0.5F);
					b.add(0F);
					b.sync();
					float[] data = b.getGlFloatData();
					t.println("Length = " + data.length);
					t.println("Data = " + data[0] + ", " + data[1] + ", " + data[2]);
					return data.length == 3 && data[0] == 0.5 && data[1] == -0.5 && data[2] == 0;
				}), new ArrayBuffer());
				*/
				return true;
			}));
			@SuppressWarnings("unused")
			boolean floatArrayBuffer = false;
			if (arrayBuffer) floatArrayBuffer = t.test("Float Array Buffer", enclosiveContext(() -> {
				t.assertTest("Constructor 0", b -> b.size() == 0 && b.getTarget() == GL_ARRAY_BUFFER, new FloatArrayBuffer());
				t.println();
				t.test("Add", b -> {
					float f = 0.630625F;
					b.add(f);
					t.println("Size = " + b.size());
					t.println("Data = " + b.getFloat(0));
					return b.size() == 1 && f == b.getFloat(0);
				}, new FloatArrayBuffer());
				t.test("Add all", enclosiveContext(() -> {
					t.assertTest("Collection", b -> {
						b.addAll(List.of(0F, 2F, 3F));
						t.println("Size = " + b.size());
						t.println("Data = " + b.getFloat(0) + ", " + b.getFloat(1) + ", " + b.getFloat(2));
						return b.size() == 3 && b.getFloat(0) == 0 && b.getFloat(1) == 2 && b.getFloat(2) == 3;
					}, new FloatArrayBuffer());
					t.assertTest("Array", b -> {
						b.addAll(new float[] {0, 2, 3});
						t.println("Size = " + b.size());
						t.println("Data = " + b.getFloat(0) + ", " + b.getFloat(1) + ", " + b.getFloat(2));
						return b.size() == 3 && b.getFloat(0) == 0 && b.getFloat(1) == 2 && b.getFloat(2) == 3;
					}, new FloatArrayBuffer());
					return true;
				}));
				t.println();
				t.test("Merge", enclosiveContext(b -> {
					b.add(12F);
					FloatArrayBuffer extra = new FloatArrayBuffer();
					extra.add(0.1F);
					extra.add(11.5F);
					b.merge(extra);
					t.println("Size = " + b.size());
					t.println("Data = " + b.getFloat(0) + ", " + b.getFloat(1) + ", " + b.getFloat(2));
					return b.size() == 3 && b.getFloat(0) == 12F && b.getFloat(1) == 0.1F && b.getFloat(2) == 11.5F;
				}), new FloatArrayBuffer());
				t.assertTest("Clear", b -> {
					b.add(0F);
					b.add(1F);
					b.clear();
					t.println("Size = " + b.size());
					return b.size() == 0;
				}, new FloatArrayBuffer());
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
				}), new FloatArrayBuffer());
				return true;
			}));
			@SuppressWarnings("unused")
			boolean elementArrayBuffer = t.test("Element Array Buffer", enclosiveContext(() -> {
				t.assertTest("Constructor", b -> b.size() == 0 && b.getTarget() == GL_ELEMENT_ARRAY_BUFFER, new ElementArrayBuffer());
				t.println();
				t.assertTest("Add", b -> {
					b.add(0);
					b.add(2);
					b.add(3);
					t.println("Size = " + b.size());
					t.println("Data = " + b.get(0) + ", " + b.get(1) + ", " + b.get(2));
					return b.size() == 3 && b.get(0) == 0 && b.get(1) == 2 && b.get(2) == 3;
				}, new ElementArrayBuffer());
				t.test("Add all", enclosiveContext(() -> {
					t.assertTest("Collection", b -> {
						b.addAll(List.of(0, 2, 3));
						t.println("Size = " + b.size());
						t.println("Data = " + b.get(0) + ", " + b.get(1) + ", " + b.get(2));
						return b.size() == 3 && b.get(0) == 0 && b.get(1) == 2 && b.get(2) == 3;
					}, new ElementArrayBuffer());
					t.assertTest("Array", b -> {
						b.addAll(new int[] {0, 2, 3});
						t.println("Size = " + b.size());
						t.println("Data = " + b.get(0) + ", " + b.get(1) + ", " + b.get(2));
						return b.size() == 3 && b.get(0) == 0 && b.get(1) == 2 && b.get(2) == 3;
					}, new ElementArrayBuffer());
					t.assertTest("Element Array Buffer", enclosiveContext(b -> {
						ElementArrayBuffer extra = new ElementArrayBuffer();
						extra.add(0);
						extra.add(2);
						extra.add(3);
						b.addAll(extra);
						t.println("Size = " + b.size());
						t.println("Data = " + b.get(0) + ", " + b.get(1) + ", " + b.get(2));
						return b.size() == 3 && b.get(0) == 0 && b.get(1) == 2 && b.get(2) == 3;
					}), new ElementArrayBuffer());
					return true;
				}));
				t.println();
				t.test("Merge", enclosiveContext(b -> {
					ElementArrayBuffer extra = new ElementArrayBuffer();
					extra.add(0);
					extra.add(1);
					b.merge(extra, 0);
					b.merge(extra, 2);
					t.println("Size = " + b.size());
					t.println("Data = " + b.get(0) + ", " + b.get(1) + ", " + b.get(2) + ", " + b.get(3));
					return b.size() == 4 && b.get(0) == 0 && b.get(1) == 1 && b.get(2) == 2 && b.get(3) == 3;
				}), new ElementArrayBuffer());
				t.assertTest("Clear", b -> {
					b.add(0);
					b.add(1);
					b.clear();
					t.println("Size = " + b.size());
					return b.size() == 0;
				}, new ElementArrayBuffer());
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
				}), new ElementArrayBuffer());
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
				}), new ElementArrayBuffer());
				return true;
			}));
			t.test("Shader", enclosiveContext(() -> {
				
				ShaderUpdate shader = new ShaderUpdate(new File("src/main/resources/assets/renderengine/colored_texture.vert"));
				
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

				t.println("Known Inputs = " + program.getKnownInputs());
				
				if (program.getKnownInputs().size() != 3) return false;
				if (program.getProgramInputs().size() != 0) return false;
				if (program.getStatus().isBuilded()) return false;
				for (ShaderType type : ShaderType.values()) if (program.getShaders(type).size() != 0) return false;
				
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
			boolean b;
			window.makeContextCurrent();
			try
			{
				b = supplier.get();
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
			return b;
		};
	}
	
	public static <T> Predicate<T> enclosiveContext(Predicate<T> predicate)
	{
		return t -> {
			boolean b;
			window.makeContextCurrent();
			try
			{
				b = predicate.test(t);
			}
			catch (Exception e)
			{
				throw e;
			}
			finally
			{
				window.detachCurrentContext();
			}
			return b;
		};
	}
}
