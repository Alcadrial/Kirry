package com.alcadra.renderengine.renderer.object;

import java.util.ArrayList;
import java.util.List;

import com.alcadra.renderengine.renderer.AbstractVertexBuilder;
import com.alcadra.renderengine.renderer.data.FloatListVertexData;
import com.alcadra.renderengine.renderer.data.VertexData;
import com.alcadra.renderengine.shader.ShaderProgram;
import com.alcadra.renderengine.util.NotYetImplemented;
import com.alcadra.renderengine.util.Tracker;
import com.alcadra.renderengine.util.update.UpdatedFrom;

@NotYetImplemented
@UpdatedFrom("ObjectBuilder")
public class ObjectBuilderUpdate {
	
	private ObjectRendererUpdate renderer;
	private List<VertexData> data;
	
	public ObjectBuilderUpdate(ObjectRendererUpdate renderer)
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
	
	public <B extends BufferableModel> Tracker<B> end()
	{
		Tracker<B> tracker = renderer.build(this);
		return tracker;
	}
	
	private void addVertex(VertexData vertexData)
	{
		data.add(vertexData);
	}
	
	public class VertexBuilder extends AbstractVertexBuilder {
		
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
				addVertex(new FloatListVertexData(map, 0));
				building = false;
			}
		}
		
		public VertexBuilder next()
		{
			if (!building) throw new IllegalBuildingState("Cannot build more vertex after #end()!");
			
			return this;
		}
	}
}
