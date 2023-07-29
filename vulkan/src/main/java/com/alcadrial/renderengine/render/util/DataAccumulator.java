package com.alcadrial.renderengine.render.util;

import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.FrameBuffer;
import com.alcadrial.renderengine.render.PhysicalDevice;
import com.alcadrial.renderengine.render.RenderPass;
import com.alcadrial.renderengine.render.Surface;
import com.alcadrial.renderengine.render.SwapChain;
import com.alcadrial.renderengine.render.buffer.IndexBuffer;
import com.alcadrial.renderengine.render.buffer.UniformBuffer;
import com.alcadrial.renderengine.render.buffer.VertexBuffer;
import com.alcadrial.renderengine.render.pipeline.GraphicsPipeline;
import com.alcadrial.renderengine.render.pipeline.PipelineLayout;
import com.alcadrial.renderengine.render.shader.Shader;
import com.alcadrial.renderengine.render.window.Window;

public abstract class DataAccumulator<SDA extends DataAccumulator<SDA>> {
	
	protected boolean autoCloseLayout;
	protected boolean autoCloseRenderPass;
	protected Shader shader;
	protected Shader vertexShader;
	protected Shader tessellationControlShader;
	protected Shader tessellationEvaluationShader;
	protected Shader geometryShader;
	protected Shader fragmentShader;
	protected Shader computeShader;
	protected Window targetWindow;
	protected Device device;
	protected PhysicalDevice physicalDevice;
	protected GraphicsPipeline graphicsPipeline;
	protected PipelineLayout pipelineLayout;
	protected FrameBuffer frameBuffer;
	protected RenderPass renderPass;
	protected Surface surface;
	protected SwapChain swapChain;
	protected VertexBuffer vertexBuffer;
	protected IndexBuffer indexBuffer;
	protected UniformBuffer uniformBuffer;
	
	protected DataAccumulator()
	{
		
	}
	
	protected SDA autoCloseLayout(boolean value)
	{
		autoCloseLayout = value;
		return myself();
	}
	
	protected SDA autoCloseRenderPass(boolean value)
	{
		autoCloseRenderPass = value;
		return myself();
	}
	
	protected SDA shader(Shader shader)
	{
		if (shader != null)
		{
			Debug.printlnIf(shader.getOrCompile().hasErrors(), "Shader " + shader.getFile() + " cannot be used becouse of errors");
			this.shader = shader.getOrCompile().hasErrors() ? null : shader;
		}
		return myself();
	}
	
	/**
	 * Use {@link Shader#getType()} to distinguish the type
	 * @param shader
	 * @return
	 */
	protected SDA genericShader(Shader shader)
	{
		Debug.printlnIf(shader.getOrCompile().hasErrors(), "Shader " + shader.getFile() + " cannot be used becouse of errors");
		switch (shader.getType())
		{
			case VERTEX -> vertexShader = shader.getOrCompile().hasErrors() ? null : shader;
			case TESS_CONTROL -> tessellationControlShader = shader.getOrCompile().hasErrors() ? null : shader;
			case TESS_EVALUATION -> tessellationEvaluationShader = shader.getOrCompile().hasErrors() ? null : shader;
			case GEOMETRY -> geometryShader = shader.getOrCompile().hasErrors() ? null : shader;
			case FRAGMENT -> fragmentShader = shader.getOrCompile().hasErrors() ? null : shader;
			case COMPUTE -> computeShader = shader.getOrCompile().hasErrors() ? null : shader;
			default -> Debug.println("Shader " + shader.getFile() + " cannot be used becouse the type is unknown");
		}
		return myself();
	}
	
	protected SDA vertexShader(Shader shader)
	{
		if (shader != null)
		{
			Debug.printlnIf(shader.getOrCompile().hasErrors(), "Shader " + shader.getFile() + " cannot be used becouse of errors");
			vertexShader = shader.getOrCompile().hasErrors() ? null : shader;
		}
		return myself();
	}
	
	protected SDA tessellationControlShader(Shader shader)
	{
		if (shader != null)
		{
			Debug.printlnIf(shader.getOrCompile().hasErrors(), "Shader " + shader.getFile() + " cannot be used becouse of errors");
			tessellationControlShader = shader.getOrCompile().hasErrors() ? null : shader;
		}
		return myself();
	}
	
	protected SDA tessellationEvaluationShader(Shader shader)
	{
		if (shader != null)
		{
			Debug.printlnIf(shader.getOrCompile().hasErrors(), "Shader " + shader.getFile() + " cannot be used becouse of errors");
			tessellationEvaluationShader = shader.getOrCompile().hasErrors() ? null : shader;
		}
		return myself();
	}
	
	protected SDA geometryShader(Shader shader)
	{
		if (shader != null)
		{
			Debug.printlnIf(shader.getOrCompile().hasErrors(), "Shader " + shader.getFile() + " cannot be used becouse of errors");
			geometryShader = shader.getOrCompile().hasErrors() ? null : shader;
		}
		return myself();
	}
	
	protected SDA fragmentShader(Shader shader)
	{
		if (shader != null)
		{
			Debug.printlnIf(shader.getOrCompile().hasErrors(), "Shader " + shader.getFile() + " cannot be used becouse of errors");
			fragmentShader = shader.getOrCompile().hasErrors() ? null : shader;
		}
		return myself();
	}
	
	protected SDA computeShader(Shader shader)
	{
		if (shader != null)
		{
			Debug.printlnIf(shader.getOrCompile().hasErrors(), "Shader " + shader.getFile() + " cannot be used becouse of errors");
			computeShader = shader.getOrCompile().hasErrors() ? null : shader;
		}
		return myself();
	}
	
	protected SDA targetWindow(Window targetWindow)
	{
		this.targetWindow = targetWindow;
		return myself();
	}
	
	protected SDA device(Device device)
	{
		this.device = device;
		return myself();
	}
	
	protected SDA physicalDevice(PhysicalDevice physicalDevice)
	{
		this.physicalDevice = physicalDevice;
		return myself();
	}
	
	protected SDA graphicsPipeline(GraphicsPipeline graphicsPipeline)
	{
		this.graphicsPipeline = graphicsPipeline;
		return myself();
	}
	
	protected SDA pipelineLayout(PipelineLayout pipelineLayout)
	{
		this.pipelineLayout = pipelineLayout;
		return myself();
	}
	
	protected SDA frameBuffer(FrameBuffer frameBuffer)
	{
		this.frameBuffer = frameBuffer;
		return myself();
	}
	
	protected SDA renderPass(RenderPass renderPass)
	{
		this.renderPass = renderPass;
		return myself();
	}
	
	protected SDA surface(Surface surface)
	{
		this.surface = surface;
		return myself();
	}
	
	protected SDA swapChain(SwapChain swapChain)
	{
		this.swapChain = swapChain;
		return myself();
	}
	
	protected SDA vertexBuffer(VertexBuffer vertexBuffer)
	{
		this.vertexBuffer = vertexBuffer;
		return myself();
	}
	
	protected SDA indexBuffer(IndexBuffer indexBuffer)
	{
		this.indexBuffer = indexBuffer;
		return myself();
	}
	
	protected SDA uniformBuffer(UniformBuffer uniformBuffer)
	{
		this.uniformBuffer = uniformBuffer;
		return myself();
	}
	
	@SuppressWarnings("unchecked")
	protected SDA myself()
	{
		return (SDA) this;
	}
}
