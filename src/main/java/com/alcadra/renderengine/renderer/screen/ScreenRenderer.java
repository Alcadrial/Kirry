package com.alcadra.renderengine.renderer.screen;

import com.alcadra.renderengine.buffer.ArrayBuffer;
import com.alcadra.renderengine.buffer.ObjectArrayBuffer;
import com.alcadra.renderengine.renderer.AbstractShaderRendererUpdate;
import com.alcadra.renderengine.shader.ShaderProgram;
import com.alcadra.renderengine.util.NotYetImplemented;
import com.alcadra.renderengine.window.widget.DrawableWidgetUpdate;

@NotYetImplemented
class ScreenRenderer extends AbstractShaderRendererUpdate<ArrayBuffer<?>> {
	
	private ArrayBuffer<?> currentArrayBuffer;
	
	public ScreenRenderer(ShaderProgram program)
	{
		super(program, new ObjectArrayBuffer<>());
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
	
	public void useBuffer(ArrayBuffer<?> buffer)
	{
		currentArrayBuffer = buffer;
	}
}
