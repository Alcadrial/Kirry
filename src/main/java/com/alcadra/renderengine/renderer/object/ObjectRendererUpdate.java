package com.alcadra.renderengine.renderer.object;

import java.util.function.Consumer;

import com.alcadra.math.Matrix4f;
import com.alcadra.renderengine.buffer.ObjectArrayBuffer;
import com.alcadra.renderengine.renderer.AbstractShaderRendererUpdate;
import com.alcadra.renderengine.renderer.RenderingMode;
import com.alcadra.renderengine.shader.ShaderInput;
import com.alcadra.renderengine.shader.ShaderProgram;
import com.alcadra.renderengine.util.NotYetImplemented;
import com.alcadra.renderengine.util.Tracker;
import com.alcadra.renderengine.util.update.UpdatedFrom;

@NotYetImplemented
@UpdatedFrom("ObjectRenderer")
public class ObjectRendererUpdate extends AbstractShaderRendererUpdate<ObjectArrayBuffer<Object>> {
	
	private int offset;
	private BufferableModel buildingValue;
	
	public ObjectRendererUpdate(ShaderProgram program)
	{
		super(program, new ObjectArrayBuffer<>());
	}
	
	public ObjectBuilderUpdate start(BufferableModel value)
	{
		return new ObjectBuilderUpdate(this);
	}
	
	public void startIfAbsent(BufferableModel value, Consumer<ObjectBuilderUpdate> consumer)
	{
		
	}
	
	protected <B extends BufferableModel> Tracker<B> build(ObjectBuilderUpdate builder)
	{
		return new Tracker0<>(arrayBuffer.add(buildingValue, buildData(buildingValue)));
	}
	
	private int[] buildData(BufferableModel bufferable)
	{
		int vertices = bufferable.vertices();
		int vertexSize = vertexSize() / 4;
		int[] buildedData = new int[vertexSize * vertices];
		for (int i = 0, j = 0; i < vertices; i++, j = 0)
		{
			for (ShaderInput input : program.getProgramInputs())
			{
				int[] vertexData = bufferable.getVertexData(i, input).getData();
				for (int k = 0; k < vertexData.length; k++, j++) buildedData[i * vertexSize + j] = vertexData[k];
			}
		}
		return buildedData;
	}
	
	public void enable(int index)
	{
		arrayBuffer.enable(index);
	}
	
	public void disable(int index)
	{
		arrayBuffer.disable(index);
	}
	
	public void draw(RenderingMode mode, Matrix4f matrix)
	{
		
		draw(mode, arrayBuffer.intSize());
	}
	
	public void reset()
	{
		clear();
	}
	
	@Override
	public void clear()
	{
		arrayBuffer.clear();
		offset = 0;
	}
	
	public void merge(ObjectRendererUpdate renderer)
	{
		arrayBuffer.merge(renderer.arrayBuffer);
		offset += renderer.getOffset();
	}
	
	public int getOffset()
	{
		return offset;
	}
	
	@Override
	public String toString()
	{
		return "ObjectRenderer{" + arrayBuffer + "}";
	}
	
	private class Tracker0<B extends BufferableModel> implements Tracker<B> {
		
		private Tracker<int[]> tracker;
		
		public Tracker0(Tracker<int[]> tracker)
		{
			this.tracker = tracker;
		}
		
		@Override
		public void modify(B elementData)
		{
			tracker.modify(buildData(elementData));
		}
		
		@Override
		public boolean turnOn()
		{
			return tracker.turnOn();
		}
		
		@Override
		public boolean turnOff()
		{
			return tracker.turnOff();
		}
		
		@Override
		public void remove()
		{
			tracker.remove();
		}
		
		@Override
		public boolean isOn()
		{
			return tracker.isOn();
		}
		
		@Override
		public boolean isOff()
		{
			return !tracker.isOff();
		}
		
		@Override
		public boolean isValid()
		{
			return tracker.isValid();
		}
	}
}
