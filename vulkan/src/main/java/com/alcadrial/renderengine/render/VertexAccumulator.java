package com.alcadrial.renderengine.render;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import com.alcadrial.renderengine.render.buffer.VertexBuffer;
import com.alcadrial.renderengine.render.pipeline.GraphicsPipeline;
import com.alcadrial.renderengine.render.shader.ShaderInput;
import com.alcadrial.renderengine.render.util.Tracker;

public class VertexAccumulator {
	
	private GraphicsPipeline graphicsPipeline;
	private VertexBuffer vertexBuffer;
	private int index;
	
	public VertexAccumulator(GraphicsPipeline graphicsPipeline, VertexBuffer vertexBuffer)
	{
		this.graphicsPipeline = graphicsPipeline;
		this.vertexBuffer = vertexBuffer;
	}
	
	public VertexBuilder vertex(float x, float y, float z)
	{
		return new VertexBuilder(graphicsPipeline, x, y, z);
	}
	
	public class VertexBuilder extends AbstractVertexBuilder<VertexBuilder, VertexResult> {
		
		protected VertexBuilder(GraphicsPipeline pipeline, float x, float y, float z)
		{
			super(pipeline, x, y, z);
		}
		
		@Override
		public VertexResult end()
		{
			int vertexIndex = index;
			float[] data = gatherProgramDataArray();
			vertexBuffer.asFloatBuffer().put(vertexIndex, data);
			index += data.length;
			map.clear();
			Map<ShaderInput, Integer> inputs = new HashMap<>();
			for (ShaderInput input : graphicsPipeline.getInputs())
			{
				inputs.put(input, vertexIndex);
				vertexIndex += input.getType().getSize();
			}
			return new VertexResult(vertexBuffer, inputs);
		}
	}
	
	public class VertexResult implements IVertexResult {
		
		private VertexBuffer vertexBuffer;
		private Map<ShaderInput, Integer> inputs;
		
		public VertexResult(VertexBuffer vertexBuffer, Map<ShaderInput, Integer> inputs)
		{
			this.vertexBuffer = vertexBuffer;
			this.inputs = inputs;
		}
		
		@Override
		public Tracker<int[]> track(ShaderInput input)
		{
			return new InputTracker(inputs.get(input), vertexBuffer, input.getType().getSize());
		}
		
		@Override
		public VertexBuffer getBuffer()
		{
			return vertexBuffer;
		}
	}
	
	private class InputTracker implements Tracker<int[]> {
		
		private int index;
		private VertexBuffer vertexBuffer;
		private int size;
		private boolean valid;
		
		public InputTracker(int index, VertexBuffer vertexBuffer, int size)
		{
			this.index = index;
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
			vertexBuffer.sendToGpu(index, size);
		}
		
		@Override
		public void remove()
		{
			if (valid)
			{
				IntBuffer buffer = vertexBuffer.asIntBuffer();
				for (int i = index; i < size + index; i++) buffer.put(i, 0);
			}
		}
		
		@Override
		public boolean isValid()
		{
			return valid;
		}
	}
}
