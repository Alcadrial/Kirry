package com.alcadrial.renderengine.render;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.pipeline.GraphicsPipeline;
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.util.Tracker;

public class VertexAccumulator {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private GraphicsPipeline graphicsPipeline;
	private VulkanBuffer vertexBuffer;
	private int bufferIndex;
	private int index;
	
	public VertexAccumulator(GraphicsPipeline graphicsPipeline, VulkanBuffer vertexBuffer)
	{
		this.graphicsPipeline = graphicsPipeline;
		this.vertexBuffer = vertexBuffer;
	}
	
	public VertexBuilder vertex(float x, float y, float z)
	{
		return new VertexBuilder(graphicsPipeline, x, y, z, index++);
	}
	
	public void end()
	{
		//vertexBuffer.sendToPhysicalDevice();
	}
	
	public class VertexBuilder extends AbstractVertexBuilder<VertexBuilder, VertexResult> {
		
		private int index;
		
		protected VertexBuilder(GraphicsPipeline pipeline, float x, float y, float z, int index)
		{
			super(pipeline, x, y, z);
			this.index = index;
		}
		
		@Override
		public VertexResult end()
		{
			int vertexIndex = bufferIndex;
			float[] data = gatherProgramDataArray();
			vertexBuffer.asFloatBuffer().put(vertexIndex, data);
			bufferIndex += data.length;
			map.clear();
			Map<ShaderVariable, Integer> inputs = new HashMap<>();
			for (ShaderVariable input : graphicsPipeline.getInputs())
			{
				inputs.put(input, vertexIndex);
				vertexIndex += input.getType().getSize();
			}
			return new VertexResult(vertexBuffer, inputs, index);
		}
		
		public VertexBuilder nextVertex(float x, float y, float z)
		{
			end();
			return vertex(x, y, z);
		}
		
		public VertexAccumulator next()
		{
			end();
			return VertexAccumulator.this;
		}
	}
	
	public class VertexResult implements IVertexResult {
		
		private VulkanBuffer vertexBuffer;
		private Map<ShaderVariable, Integer> inputs;
		private int index;
		
		public VertexResult(VulkanBuffer vertexBuffer, Map<ShaderVariable, Integer> inputs, int index)
		{
			this.vertexBuffer = vertexBuffer;
			this.inputs = inputs;
			this.index = index;
		}
		
		@Override
		public Tracker<int[]> track(ShaderVariable input)
		{
			return new InputTracker(inputs.get(input), vertexBuffer, input.getType().getSize());
		}
		
		@Override
		public VulkanBuffer getBuffer()
		{
			return vertexBuffer;
		}
		
		@Override
		public int getIndex()
		{
			return index;
		}
	}
	
	private class InputTracker implements Tracker<int[]> {
		
		private int bufferIndex;
		private VulkanBuffer vertexBuffer;
		private int size;
		private boolean valid;
		
		public InputTracker(int index, VulkanBuffer vertexBuffer, int size)
		{
			bufferIndex = index;
			this.vertexBuffer = vertexBuffer;
			this.size = size;
			valid = true;
		}
		
		@Override
		public void modify(int[] data)
		{
			if (valid)
			{
				IntBuffer buffer = vertexBuffer.asIntBuffer();
				buffer.put(data);
				while (buffer.hasRemaining()) buffer.put(0);
				buffer.rewind();
			}
		}
		
		@Override
		public void flush()
		{
			//vertexBuffer.sendToPhysicalDevice(bufferIndex, size);
		}
		
		@Override
		public void remove()
		{
			if (valid)
			{
				IntBuffer buffer = vertexBuffer.asIntBuffer();
				for (int i = bufferIndex; i < size + bufferIndex; i++) buffer.put(i, 0);
			}
		}
		
		@Override
		public boolean isValid()
		{
			return valid;
		}
	}
}
