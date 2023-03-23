package com.alcadra.renderengine.renderer.screen;

import com.alcadra.math.Matrix4f;
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
	protected void initializeArrayBuffer()
	{
		vertexArray.bind();
		currentArrayBuffer.bind();
		super.initializeArrayBuffer();
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
	public void drawVertices(RenderingMode mode, Matrix4f matrix, int count, int offset)
	{}
}
