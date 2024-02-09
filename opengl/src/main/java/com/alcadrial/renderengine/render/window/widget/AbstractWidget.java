package com.alcadrial.renderengine.render.window.widget;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3i;
import com.alcadrial.kector.vector.Vector4i;
import com.alcadrial.renderengine.core.util.EmptyImplementation;
import com.alcadrial.renderengine.render.renderingmanager.RenderingManager;
import com.alcadrial.renderengine.render.window.OpenGlWindow;

public abstract class AbstractWidget implements ClickableWidget, DrawableWidget {
	
	private static final byte EMPTY_STATE = 0x00;
	private static final byte INITIALIZED_BIT = 0x01;
	private static final byte RENDERED_BIT = 0x02;
	private static final byte DRAWN_BIT = 0x04;
	private static final byte HOVERED_BIT = 0x08;
	private static final byte DRAGGING_BIT = 0x10;
	
	protected OpenGlWindow window;
	protected Vector3i pos;
	protected Vector2i size;
	private byte internalStates;
	
	public AbstractWidget(OpenGlWindow window, Vector3i pos, Vector2i size)
	{
		this.window = window;
		this.pos = Vector3i.copyOf(pos);
		this.size = Vector2i.copyOf(size);
		internalStates = EMPTY_STATE;
		
		setInternalStateBit(HOVERED_BIT, isHovered(window.getMousePos()));
	}
	
	private void setInternalStateBit(byte bit, boolean value)
	{
		if (value) internalStates |= bit;
		else internalStates &= ~bit;
	}
	
	private boolean getInternalStateBit(byte bit)
	{
		return (internalStates & bit) != 0;
	}
	
	@Override
	public void initializeRendering(Matrix4f matrix, RenderingManager manager, float partialFrame)
	{
		setInternalStateBit(INITIALIZED_BIT, true);
	}
	
	@Override
	public void render(Matrix4f matrix, RenderingManager manager, float partialFrame)
	{
		setInternalStateBit(RENDERED_BIT, true);
	}
	
	@Override
	public void draw(Matrix4f matrix, RenderingManager manager, float partialFrame)
	{
		setInternalStateBit(DRAWN_BIT, true);
	}
	
	@Override
	public Vector4i drawingArea(Matrix4f matrix, float partialFrame)
	{
		return Vector4i.of(Vector2i.of(pos.multiply(matrix).xy()), Vector2i.of(getPos().add(size).multiply(matrix).xy()));
	}
	
	@Override
	public boolean isOnScreen()
	{
		return getInternalStateBit(DRAWN_BIT);
	}
	
	@Override
	public boolean isRendered()
	{
		return getInternalStateBit(RENDERED_BIT);
	}
	
	@Override
	public boolean isInitialized()
	{
		return getInternalStateBit(INITIALIZED_BIT);
	}
	
	@Override
	public boolean isMouseHover()
	{
		return getInternalStateBit(HOVERED_BIT);
	}
	
	@Override
	public boolean isDragging()
	{
		return getInternalStateBit(DRAGGING_BIT);
	}
	
	@Override
	public void requestRender()
	{
		setInternalStateBit(RENDERED_BIT, false);
	}
	
	@Override
	public void requestDraw()
	{
		setInternalStateBit(DRAWN_BIT, false);
	}
	
	@Override
	public void clearRenderingData()
	{
		setInternalStateBit(INITIALIZED_BIT, false);
	}
	
	@Override
	public boolean isHovered(Vector2i v)
	{
		return v.x() > pos.x() && v.x() < pos.x() + size.x() && v.y() > pos.y() && v.y() < pos.y() + size.y();
	}
	
	protected void onMouseEnter(Vector2i enterPos)
	{
		setInternalStateBit(HOVERED_BIT, true);
	}
	
	protected void onMouseExit(Vector2i exitPos)
	{
		setInternalStateBit(HOVERED_BIT, false);
	}
	
	@Override
	public void onMouseMove(Vector2i oldPos, Vector2i newPos)
	{
		boolean wasOvered = isMouseHover();
		boolean isPassedThrough = wasOvered ^ isHovered(newPos);
		if (isPassedThrough && wasOvered) onMouseExit(newPos);
		else if (isPassedThrough) onMouseEnter(newPos);
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
	public void onDragStart(Vector2i mousePos)
	{
		setInternalStateBit(DRAGGING_BIT, true);
	}
	
	@Override
	public void onDragEnd(Vector2i mousePos)
	{
		setInternalStateBit(DRAGGING_BIT, false);
	}
	
	@Override
	@EmptyImplementation
	public void onDrag(Vector2i startPos, Vector2i endPos)
	{
		
	}
}