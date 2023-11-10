package com.alcadrial.renderengine.render.window.widget;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3i;
import com.alcadrial.kector.vector.Vector4i;
import com.alcadrial.renderengine.render.renderingmanager.RenderingManager;
import com.alcadrial.renderengine.render.window.OpenGlWindow;

public abstract class AbstractWidget implements ClickableWidget, DrawableWidget {
	
	protected OpenGlWindow window;
	protected Vector3i pos;
	protected Vector2i size;
	private boolean isDrawn;
	private boolean isRendered;
	
	public AbstractWidget(OpenGlWindow window, Vector3i pos, Vector2i size)
	{
		this.window = window;
		this.pos = Vector3i.copyOf(pos);
		this.size = Vector2i.copyOf(size);
	}
	
	@Override
	public void initializeRendering(Matrix4f matrix, RenderingManager manager, float partialFrame)
	{
		
	}
	
	@Override
	public void render(Matrix4f matrix, RenderingManager manager, float partialFrame)
	{
		isRendered = true;
	}
	
	@Override
	public void draw(Matrix4f matrix, RenderingManager manager, float partialFrame)
	{
		isDrawn = true;
	}
	
	@Override
	public void clearRenderingData()
	{
		isDrawn = false;
		isRendered = false;
	}
	
	@Override
	public Vector4i drawingArea(Matrix4f matrix, float partialFrame)
	{
		return Vector4i.of(Vector2i.of(pos.multiply(matrix).xy()), Vector2i.of(getPos().add(size).multiply(matrix).xy()));
	}
	
	@Override
	public boolean isOnScreen()
	{
		return isDrawn;
	}
	
	@Override
	public boolean isChanged()
	{
		return !isRendered;
	}
	
	public void requestRender()
	{
		isRendered = false;
	}
	
	public void requestDraw()
	{
		isDrawn = false;
	}
	
	@Override
	public boolean isMouseHover()
	{
		return isHovered(window.getMousePos());
	}
	
	@Override
	public boolean isHovered(Vector2i v)
	{
		return v.x() > pos.x() && v.x() < pos.x() + size.x() && v.y() > pos.y() && v.y() < pos.y() + size.y();
	}
	
	protected void move(Vector3i newPos)
	{
		
	}
	
	@Override
	public void setSize(Vector2i size)
	{
		Vector2i oldSize = Vector2i.copyOf(this.size.xy());
		resize(size);
		this.size = size.xy();
		onResize(oldSize, size);
	}
	
	protected void resize(Vector2i newSize)
	{
		
	}
	
	@Override
	public Vector3i getPos()
	{
		return Vector3i.copyOf(pos);
	}
	
	@Override
	public Vector2i getSize()
	{
		return Vector2i.copyOf(size);
	}
	
	@Override
	public int getX()
	{
		return pos.x();
	}
	
	@Override
	public int getY()
	{
		return pos.y();
	}
	
	@Override
	public int getZ()
	{
		return pos.z();
	}
	
	@Override
	public int getWidth()
	{
		return size.x();
	}
	
	@Override
	public int getHeight()
	{
		return size.y();
	}
	
	@Override
	public void setPos(Vector3i pos)
	{
		Vector3i oldPos = Vector3i.copyOf(this.pos);
		move(pos);
		this.pos = pos.xyz();
		onMove(oldPos, pos);
	}
	
	protected void onMove(Vector3i oldPos, Vector3i newPos)
	{
		
	}
	
	protected void onResize(Vector2i oldSize, Vector2i newSize)
	{
		
	}
	
	@Override
	public void onPress(int button, Vector2i mousePos)
	{
		
	}
	
	@Override
	public void onRelease(int button, Vector2i mousePos)
	{
		
	}
	
	@Override
	public void onClick(int button, int count, Vector2i mousePos)
	{
		
	}
	
	@Override
	public void onDragStart(Vector2i mousePos)
	{
		
	}
	
	@Override
	public void onDragEnd(Vector2i mousePos)
	{
		
	}
	
	@Override
	public void onDrag(Vector2i startPos, Vector2i endPos)
	{
		
	}
}