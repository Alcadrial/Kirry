package com.alcadra.renderengine.renderer.screen;

import com.alcadra.renderengine.buffer.ArrayBufferUpdate;
import com.alcadra.renderengine.renderer.AbstractShaderRendererUpdate;
import com.alcadra.renderengine.shader.ShaderProgram;
import com.alcadra.renderengine.window.widget.DrawableWidgetUpdate;

class ScreenRenderer extends AbstractShaderRendererUpdate<DataModel> {
	
	private ArrayBufferUpdate<?> currentArrayBuffer;
	
	public ScreenRenderer(ShaderProgram program)
	{
		super(program, DataModel.EMPTY);
		currentArrayBuffer = arrayBuffer;
	}
	
	@Override
	protected void initializeArrayBuffer()
	{
		vertexArray.bindBuffer(currentArrayBuffer);
		super.initializeArrayBuffer();
	}
	
	public void drawWidget(DrawableWidgetUpdate<ScreenRenderer> widget)
	{
		
	}
	
	public void draw(DataModel widget)
	{
		
	}
	
	public void useBuffer(ArrayBufferUpdate<?> buffer)
	{
		currentArrayBuffer = buffer;
	}
}
