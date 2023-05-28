package com.alcadrial.renderengine.opengl.renderer.screen;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.renderengine.opengl.buffer.GlBuffer;
import com.alcadrial.renderengine.opengl.renderer.AbstractShaderRenderer;
import com.alcadrial.renderengine.opengl.renderer.RenderingMode;
import com.alcadrial.renderengine.opengl.shader.ShaderProgram;
import com.alcadrial.renderengine.opengl.window.widget.DrawableWidget;

class ScreenRenderer extends AbstractShaderRenderer<DataModel> {
	
	private GlBuffer currentArrayBuffer;
	
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
	
	public void drawWidget(DrawableWidget widget)
	{
		
	}
	
	public void draw(DataModel widget)
	{
		
	}
	
	public void useBuffer(GlBuffer buffer)
	{
		currentArrayBuffer = buffer;
	}
	
	@Override
	public void draw(RenderingMode mode, Matrix4f matrix, int count, int offset)
	{
		
	}
}
