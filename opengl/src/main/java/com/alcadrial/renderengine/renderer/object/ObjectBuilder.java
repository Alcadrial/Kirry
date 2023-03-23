package com.alcadrial.renderengine.renderer.object;

import java.util.ArrayList;
import java.util.List;

import com.alcadrial.renderengine.renderer.AbstractVertexBuilder;
import com.alcadrial.renderengine.renderer.IllegalBuildingState;
import com.alcadrial.renderengine.renderer.data.FloatListVertexData;
import com.alcadrial.renderengine.renderer.data.VertexData;
import com.alcadrial.renderengine.shader.ShaderProgram;
import com.alcadrial.renderengine.util.SwitchableTracker;
import com.alcadrial.renderengine.util.WorkInProgress;

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
		SwitchableTracker<B> tracker = renderer.build(this);
		return tracker;
	}
	
	private void addVertex(VertexData vertexData)
	{
		data.add(vertexData);
	}
	
	public class VertexBuilder extends AbstractVertexBuilder<VertexBuilder> {
		
		private boolean building;
		
		private VertexBuilder()
		{
			super(renderer.getShaderProgram());
			building = true;
		}
		
		@Override
		public void end()
		{
			if (building)
			{
				map.clear();
				building = false;
			}
		}
		
		public VertexBuilder next()
		{
			if (!building) throw new IllegalBuildingState("Cannot build more vertex after #end()!");
			addVertex(new FloatListVertexData(map, 0));
			map.clear();
			return this;
		}
	}
}
