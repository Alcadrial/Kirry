package com.alcadra.renderengine.renderer.screen;

import org.lwjgl.opengl.GL30;

import com.alcadra.renderengine.buffer.ObjectArrayBuffer;
import com.alcadra.renderengine.renderer.BaseRenderer;
import com.alcadra.renderengine.renderer.VertexArray;
import com.alcadra.renderengine.shader.Shader;
import com.alcadra.renderengine.util.NotYetImplemented;
import com.alcadra.renderengine.window.widget.DrawableWidget;
import com.alcadra.renderengine.window.widget.Widget;

@NotYetImplemented
class ScreenRenderer extends BaseRenderer<ObjectArrayBuffer<Widget>> {
	
	public ScreenRenderer(Shader shader)
	{
		super(shader, new VertexArray(), new ObjectArrayBuffer<>());
	}
	
	@Override
	protected void initializeArrayBuffer()
	{
		vertexArray.bindBuffer(arrayBuffer);
		int vertexSize = 12 + (shader.useColor() ? 16 : 0) + (shader.useUV() ? 8 : 0) + (shader.useNormal() ? 12 : 0);
		int offset = 0;
		vertexArray.vertexAttributePointer(0, 3, GL30.GL_FLOAT, false, vertexSize, offset);
		vertexArray.enableVertexAttribute(0);
		offset += 12;
		if (shader.useColor())
		{
			vertexArray.vertexAttributePointer(1, 4, GL30.GL_FLOAT, false, vertexSize, offset);
			vertexArray.enableVertexAttribute(1);
			offset += 16;
		}
		if (shader.useUV())
		{
			vertexArray.vertexAttributePointer(2, 2, GL30.GL_FLOAT, false, vertexSize, offset);
			vertexArray.enableVertexAttribute(2);
			offset += 8;
		}
		if (shader.useNormal())
		{
			vertexArray.vertexAttributePointer(3, 3, GL30.GL_FLOAT, false, vertexSize, offset);
			vertexArray.enableVertexAttribute(3);
			offset += 12;
		}
		VertexArray.unbind();
	}
	
	@Override
	protected void disableAttributes()
	{
		vertexArray.bind();
		if (shader.useColor()) vertexArray.disableVertexAttribute(1);
		if (shader.useUV()) vertexArray.disableVertexAttribute(2);
		if (shader.useNormal()) vertexArray.disableVertexAttribute(3);
	}
	
	public void drawWidget(DrawableWidget<ScreenRenderer> widget)
	{
		
	}
}
