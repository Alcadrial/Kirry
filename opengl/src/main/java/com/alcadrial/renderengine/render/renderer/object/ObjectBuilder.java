package com.alcadrial.renderengine.render.renderer.object;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alcadrial.renderengine.core.util.WorkInProgress;
import com.alcadrial.renderengine.render.buffer.FloatArrayBuffer;
import com.alcadrial.renderengine.render.renderer.AbstractVertexBuilder;
import com.alcadrial.renderengine.render.renderer.IVertexResult;
import com.alcadrial.renderengine.render.renderer.IllegalBuildingState;
import com.alcadrial.renderengine.render.renderer.data.FloatListVertexData;
import com.alcadrial.renderengine.render.renderer.data.VertexData;
import com.alcadrial.renderengine.render.shader.ShaderInput;
import com.alcadrial.renderengine.render.shader.ShaderProgram;
import com.alcadrial.renderengine.render.util.SwitchableTracker;
import com.alcadrial.renderengine.render.util.Tracker;

@WorkInProgress
public class ObjectBuilder {
	
	private ObjectRenderer renderer;
	private List<VertexData> data;
	
	public ObjectBuilder(ObjectRenderer renderer)
	{
		this.renderer = renderer;
		data = new ArrayList<>();
	}
	
	public ShaderProgram getShaderProgram()
	{
		return renderer.getShaderProgram();
	}
	
	public VertexBuilder vertex()
	{
		return new VertexBuilder();
	}
	
	public <B extends BufferableModel> SwitchableTracker<B> end()
	{
		return renderer.build(this);
	}
	
	private void addVertex(VertexData vertexData)
	{
		data.add(vertexData);
	}
	
	public class VertexBuilder extends AbstractVertexBuilder<VertexBuilder, VertexResult> {
		
		private boolean building;
		
		private VertexBuilder()
		{
			super(renderer.getShaderProgram());
			building = true;
		}
		
		@Override
		public VertexResult end()
		{
			if (building)
			{
				map.clear();
				building = false;
			}
			return null;
		}
		
		public VertexBuilder next()
		{
			if (!building) throw new IllegalBuildingState("Cannot build more vertex after #end()!");
			addVertex(new FloatListVertexData(map, 0));
			map.clear();
			return this;
		}
	}
	
	public class VertexResult implements IVertexResult {
		
		private FloatArrayBuffer buffer;
		private Map<ShaderInput, Integer> inputs;
		
		public VertexResult(FloatArrayBuffer buffer, Map<ShaderInput, Integer> inputs)
		{
			this.buffer = buffer;
			this.inputs = inputs;
		}
		
		@Override
		public Tracker<int[]> track(ShaderInput input)
		{
			return new InputTracker(inputs.get(input), buffer, input.getType().getSize());
		}
	}
	
	private class InputTracker implements Tracker<int[]> {
		
		private int index;
		private FloatArrayBuffer buffer;
		private int size;
		private boolean valid;
		
		public InputTracker(int index, FloatArrayBuffer buffer, int size)
		{
			this.index = index;
			this.buffer = buffer;
			this.size = size;
			valid = true;
		}
		
		@Override
		public void modify(int[] data)
		{
			if (valid)
			{
				int i = 0;
				for (; i < data.length && i < size; i++) buffer.setFloat(i + index, Float.intBitsToFloat(data[i]));
				for (; i < size; i++) buffer.setFloat(i + index, 0);
			}
		}
		
		@Override
		public void remove()
		{
			if (valid) for (int i = index; i < size + index; i++) buffer.setFloat(i, 0);
		}
		
		@Override
		public boolean isValid()
		{
			return valid;
		}
	}
}
