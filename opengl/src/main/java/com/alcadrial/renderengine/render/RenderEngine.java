package com.alcadrial.renderengine.render;

import java.lang.reflect.Field;
import java.util.Optional;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.opengl.GL30;

import com.alcadrial.renderengine.core.util.Nonnull;
import com.alcadrial.renderengine.core.util.Pair;
import com.alcadrial.renderengine.core.util.color.Color;
import com.alcadrial.renderengine.render.framebuffer.FrameBuffer;
import com.alcadrial.renderengine.render.util.wrapper.CullFaceMode;

public class RenderEngine {
	
	private static volatile boolean debug = false;
	private static volatile boolean initializated = false;
	private static final StateConfiguration DEFAULT_STATE = new StateConfiguration();
	private static volatile StateConfiguration currentState = DEFAULT_STATE;
	
	public static boolean initialize()
	{
		if (initializated) return true;
		if (!GLFW.glfwInit()) return false;
		DEFAULT_STATE.parentState = DEFAULT_STATE;
		DEFAULT_STATE.copy(new StateConfigurator().setFrameBuffer(FrameBuffer.DEFAULT));
		return initializated = true;
	}
	
	public static void terminate()
	{
		if (initializated)
		{
			GLFW.glfwTerminate();
			initializated = false;
		}
	}
	
	public static boolean isInitializated()
	{
		return initializated;
	}
	
	public static boolean isDebug()
	{
		return debug;
	}
	
	public static void enableDebug()
	{
		debug = true;
	}
	
	public static void disableDebug()
	{
		debug = false;
	}
	
	public static void depthTest(boolean enable)
	{
		if (enable) enableDepthTest();
		else disableDepthTest();
	}
	
	public static void enableDepthTest()
	{
		GL30.glEnable(GL30.GL_DEPTH_TEST);
	}
	
	public static void disableDepthTest()
	{
		GL30.glDisable(GL30.GL_DEPTH_TEST);
	}
	
	public static void blend(boolean enable)
	{
		if (enable) enableBlend();
		else disableBlend();
	}
	
	public static void enableBlend()
	{
		GL30.glEnable(GL30.GL_BLEND);
	}
	
	public static void disableBlend()
	{
		GL30.glDisable(GL30.GL_BLEND);
	}
	
	public static void cullFace(boolean enable)
	{
		if (enable) enableCullFace();
		else disableCullFace();
	}
	
	public static void enableCullFace()
	{
		GL30.glEnable(GL30.GL_CULL_FACE);
	}
	
	public static void disableCullFace()
	{
		GL30.glDisable(GL30.GL_CULL_FACE);
	}
	
	public static void setBlendFactors(int sourceFactor, int destinationFactor)
	{
		GL30.glBlendFunc(sourceFactor, destinationFactor);
	}
	
	public static void setCullFace(CullFaceMode mode)
	{
		GL30.glCullFace(mode.getId());
	}
	
	public static void setFrontFaceDirection(boolean clockwise)
	{
		GL30.glFrontFace(clockwise ? GL30.GL_CW : GL30.GL_CCW);
	}
	
	public static void setClearColor(Color color)
	{
		setClearColor(color.r(), color.g(), color.b(), color.a());
	}
	
	public static void setClearColor(float r, float g, float b, float a)
	{
		GL30.glClearColor(r / 255, g / 255, b / 255, a / 255);
	}
	
	public static void setClearDepth(float d)
	{
		GL30.glClearDepth(d);
	}
	
	public static void setClearStencil(int s)
	{
		GL30.glClearStencil(s);
	}
	
	public static void setGlfwErrorCallback(GLFWErrorCallbackI callback)
	{
		GLFW.glfwSetErrorCallback(callback);
	}
	
	public static StateConfigurator configure()
	{
		return new StateConfigurator();
	}
	
	public static final class StateConfigurator extends State {
		
		public StateConfigurator()
		{
			for (Field field : State.class.getDeclaredFields())
			{
				try
				{
					field.set(this, Optional.empty());
				}
				catch (IllegalArgumentException | IllegalAccessException e)
				{
					throw new RuntimeException("The field \"" + field.getName() + "\" cannot be copied, this cannot happen!");
				}
			}
		}
		
		public StateConfigurator depthTest(boolean enable)
		{
			depthTest = Optional.of(enable);
			return this;
		}
		
		public StateConfigurator blend(boolean enable)
		{
			blend = Optional.of(enable);
			return this;
		}
		
		public StateConfigurator cullFace(boolean enable)
		{
			cullFace = Optional.of(enable);
			return this;
		}
		
		public StateConfigurator setBlendFactors(int sourceFactor, int destinationFactor)
		{
			blendFactors = Optional.of(new Pair<>(sourceFactor, destinationFactor));
			return this;
		}
		
		public StateConfigurator setCullFace(@Nonnull CullFaceMode mode)
		{
			cullFaceMode = Optional.of(mode);
			return this;
		}
		
		public StateConfigurator setFrontFaceDirection(boolean clockwise)
		{
			clockwiseFrontFace = Optional.of(clockwise);
			return this;
		}
		
		public StateConfigurator setClearColor(@Nonnull Color color)
		{
			clearColor = Optional.of(color);
			return this;
		}
		
		public StateConfigurator setClearColor(float r, float g, float b, float a)
		{
			return setClearColor(Color.of(r, g, b, a));
		}
		
		public StateConfigurator setClearDepth(float d)
		{
			clearDepth = Optional.of(d);
			return this;
		}
		
		public StateConfigurator setClearStencil(int s)
		{
			clearStencil = Optional.of(s);
			return this;
		}
		
		public StateConfigurator setFrameBuffer(FrameBuffer frameBuffer)
		{
			this.frameBuffer = Optional.of(frameBuffer);
			return this;
		}
		
		public StateConfiguration apply()
		{
			return new StateConfiguration(currentState, this);
		}
	}
	
	public static final class StateConfiguration extends State implements AutoCloseable {
		
		private StateConfiguration parentState;
		
		private StateConfiguration()
		{
			
		}
		
		private StateConfiguration(StateConfiguration parentState, StateConfigurator configurator)
		{
			this.parentState = parentState;
			copy(configurator);
			apply();
		}
		
		private void copy(StateConfigurator configurator)
		{
			for (Field field : State.class.getDeclaredFields())
			{
				try
				{
					field.set(this, field.get(configurator));
				}
				catch (IllegalArgumentException | IllegalAccessException e)
				{
					throw new RuntimeException("The field \"" + field.getName() + "\" cannot be copied, this cannot happen!");
				}
			}
		}
		
		private void apply()
		{
			depthTest.ifPresent(RenderEngine::depthTest);
			blend.ifPresent(RenderEngine::blend);
			cullFace.ifPresent(RenderEngine::cullFace);
			blendFactors.ifPresent(p -> setBlendFactors(p.l(), p.r()));
			clearColor.ifPresent(RenderEngine::setClearColor);
			clearDepth.ifPresent(RenderEngine::setClearDepth);
			clearStencil.ifPresent(RenderEngine::setClearStencil);
			cullFaceMode.ifPresent(RenderEngine::setCullFace);
			clockwiseFrontFace.ifPresent(RenderEngine::setFrontFaceDirection);
			frameBuffer.ifPresent(FrameBuffer::bind);
		}
		
		@Override
		public void close()
		{
			parentState.apply();
		}
	}
	
	private static sealed class State permits StateConfiguration, StateConfigurator {
		
		protected Optional<Boolean> depthTest;
		protected Optional<Boolean> blend;
		protected Optional<Boolean> cullFace;
		protected Optional<Pair<Integer, Integer>> blendFactors;
		protected Optional<Color> clearColor;
		protected Optional<Float> clearDepth;
		protected Optional<Integer> clearStencil;
		protected Optional<CullFaceMode> cullFaceMode;
		protected Optional<Boolean> clockwiseFrontFace;
		protected Optional<FrameBuffer> frameBuffer;
	}
}
