package com.alcadra.renderengine.renderer.screen;

import com.alcadra.math.matrix.Matrix4f;
import com.alcadra.renderengine.buffer.ArrayBuffer;
import com.alcadra.renderengine.renderer.AbstractShaderRenderer;
import com.alcadra.renderengine.renderer.RenderingMode;
import com.alcadra.renderengine.shader.ShaderProgram;
import com.alcadra.renderengine.window.widget.DrawableWidget;

class ScreenRenderer extends AbstractShaderRenderer<DataModel> {
	
	private ArrayBuffer<?> currentArrayBuffer;
	
	public ScreenRenderer(ShaderProgram program)
	{
		super(program, DataModel.EMPTY);
		currentArrayBuffer = arrayBuffer;
	}
	
	@Override
	protected void initializeVertexArray()
	{
		vertexArray.bind();
		currentArrayBuffer.bind();
		super.initializeVertexArray();
	}
	
	public void drawWidget(DrawableWidget<ScreenRenderer> widget)
	{
		
	}
	
	public void draw(DataModel widget)
	{
		
	}
	
	public void useBuffer(ArrayBuffer<?> buffer)
	{
		currentArrayBuffer = buffer;
	}
	
	@Override
	public void draw(RenderingMode mode, Matrix4f matrix, int count, int offset)
	{
		
	}
}
