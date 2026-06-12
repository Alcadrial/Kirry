package com.alcadrial.kirry.render.renderer.screen;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kirry.render.buffer.GlBuffer;
import com.alcadrial.kirry.render.renderer.AbstractShaderRenderer;
import com.alcadrial.kirry.render.renderer.RenderingMode;
import com.alcadrial.kirry.render.shader.ShaderProgram;
import com.alcadrial.kirry.render.window.widget.DrawableWidget;

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
