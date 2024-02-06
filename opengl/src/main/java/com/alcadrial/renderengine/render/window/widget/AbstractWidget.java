package com.alcadrial.renderengine.render.window.widget;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3i;
import com.alcadrial.kector.vector.Vector4i;
import com.alcadrial.renderengine.core.util.EmptyImplementation;
import com.alcadrial.renderengine.render.renderingmanager.RenderingManager;
import com.alcadrial.renderengine.render.window.OpenGlWindow;

public abstract class AbstractWidget implements ClickableWidget, DrawableWidget {
	
	protected OpenGlWindow window;
	protected Vector3i pos;
	protected Vector2i size;
	private boolean isDrawn;
	private boolean isRendered;
	private boolean isMouseHover;
	
	public AbstractWidget(OpenGlWindow window, Vector3i pos, Vector2i size)
	{
		this.window = window;
		this.pos = Vector3i.copyOf(pos);
		this.size = Vector2i.copyOf(size);
		isMouseHover = isHovered(window.getMousePos());
	}
	
	@Override
	@EmptyImplementation
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
	
	@Override
	public void requestRender()
	{
		isRendered = false;
	}
	
	@Override
	public void requestDraw()
	{
		isDrawn = false;
	}
	
	@Override
	public boolean isMouseHover()
	{
		return isMouseHover;
	}
	
	@Override
	public boolean isHovered(Vector2i v)
	{
		return v.x() > pos.x() && v.x() < pos.x() + size.x() && v.y() > pos.y() && v.y() < pos.y() + size.y();
	}
	
	@Override
	public void onMouseEnter(Vector2i enterPos)
	{
		isMouseHover = true;
	}
	
	@Override
	public void onMouseExit(Vector2i exitPos)
	{
		isMouseHover = false;
	}
	
	@EmptyImplementation
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
	
	@EmptyImplementation
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
	
	@EmptyImplementation
	protected void onMove(Vector3i oldPos, Vector3i newPos)
	{
		
	}
	
	@EmptyImplementation
	protected void onResize(Vector2i oldSize, Vector2i newSize)
	{
		
	}
	
	@Override
	@EmptyImplementation
	public void onPress(int button, Vector2i mousePos)
	{
		
	}
	
	@Override
	@EmptyImplementation
	public void onRelease(int button, Vector2i mousePos)
	{
		
	}
	
	@Override
	@EmptyImplementation
	public void onClick(int button, int count, Vector2i mousePos)
	{
		
	}
	
	@Override
	@EmptyImplementation
	public void onDragStart(Vector2i mousePos)
	{
		
	}
	
	@Override
	@EmptyImplementation
	public void onDragEnd(Vector2i mousePos)
	{
		
	}
	
	@Override
	@EmptyImplementation
	public void onDrag(Vector2i startPos, Vector2i endPos)
	{
		
	}
}