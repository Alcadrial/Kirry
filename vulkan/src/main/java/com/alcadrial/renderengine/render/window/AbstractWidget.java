package com.alcadrial.renderengine.render.window;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3i;
import com.alcadrial.kector.vector.Vector4i;
import com.alcadrial.kector.vector.getter.Vector2fGetter;
import com.alcadrial.kector.vector.getter.Vector2iGetter;
import com.alcadrial.kector.vector.getter.Vector3iGetter;
import com.alcadrial.renderengine.core.util.EmptyImplementation;
import com.alcadrial.renderengine.render.Renderer;
import com.alcadrial.renderengine.render.commandbuffer.CommandBuffer;
import com.alcadrial.renderengine.render.memory.MemoryManager;
import com.alcadrial.renderengine.render.texture.Atlas;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public abstract class AbstractWidget implements Widget {
	
	private static final byte EMPTY_STATE = 0x00;
	private static final byte INITIALIZED_BIT = 0x01;
	private static final byte RENDERED_BIT = 0x02;
	private static final byte HOVERED_BIT = 0x04;
	private static final byte DRAGGING_BIT = 0x08;
	
	protected final VulkanWindow window;
	protected Vector3i pos;
	protected Vector2i size;
	private byte internalStates;
	//private Object transformation;
	
	public AbstractWidget(VulkanWindow window, Vector3i pos, Vector2i size)
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
	public void initBuffers(MemoryManager memoryManager, CommandBuffer commandBuffer) throws VulkanException
	{
		setInternalStateBit(INITIALIZED_BIT, true);
	}
	
	@EmptyImplementation
	@Override
	public void initAtlas(Atlas atlas)
	{}
	
	@Override
	public void render(Matrix4f matrix, Renderer renderer)
	{
		setInternalStateBit(RENDERED_BIT, true);
	}
	
	@EmptyImplementation
	@Override
	public void draw(Matrix4f matrix, CommandBuffer commandBuffer, Renderer manager, float partialFrame) throws VulkanException
	{}
	
	@Override
	public Vector4i drawingArea(Matrix4f matrix, float partialFrame)
	{
		return Vector4i.of(Vector2i.of(pos.multiply(matrix).xy()), Vector2i.of(pos.xyz().add(size).multiply(matrix).xy()));
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
	public void clearRenderingData()
	{
		setInternalStateBit(INITIALIZED_BIT, false);
	}
	
	@Override
	public boolean isHovered(Vector2iGetter v)
	{
		return v.x() > pos.x() && v.x() < pos.x() + size.x() && v.y() > pos.y() && v.y() < pos.y() + size.y();
	}
	
	@Override
	public boolean isHovered(Vector2fGetter v)
	{
		return v.x() > pos.x() && v.x() < pos.x() + size.x() && v.y() > pos.y() && v.y() < pos.y() + size.y();
	}
	
	protected void onMouseEnter(Vector2iGetter enterPos)
	{
		setInternalStateBit(HOVERED_BIT, true);
	}
	
	protected void onMouseExit(Vector2iGetter exitPos)
	{
		setInternalStateBit(HOVERED_BIT, false);
	}
	
	@Override
	public void onMouseMove(Vector2iGetter oldPos, Vector2iGetter newPos)
	{
		boolean wasOvered = isMouseHover();
		boolean isPassedThrough = wasOvered ^ isHovered(newPos);
		if (isPassedThrough && wasOvered) onMouseExit(newPos);
		else if (isPassedThrough) onMouseEnter(newPos);
	}
	
	@EmptyImplementation
	protected void move(Vector3iGetter newPos)
	{}
	
	@Override
	public void setSize(Vector2iGetter size)
	{
		Vector2i oldSize = Vector2i.copyOf(this.size);
		resize(size);
		this.size.setX(size.x()).setY(size.y());
		onResize(oldSize, size);
	}
	
	@EmptyImplementation
	protected void resize(Vector2iGetter newSize)
	{}
	
	@Override
	public Vector3iGetter getPos()
	{
		return pos;
	}
	
	@Override
	public Vector2iGetter getSize()
	{
		return size;
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
	public void setPos(Vector3iGetter pos)
	{
		Vector3i oldPos = Vector3i.copyOf(this.pos);
		move(pos);
		this.pos.setX(pos.x()).setY(pos.y()).setZ(pos.z());
		onMove(oldPos, pos);
	}
	
	@EmptyImplementation
	protected void onMove(Vector3iGetter oldPos, Vector3iGetter newPos)
	{}
	
	@EmptyImplementation
	protected void onResize(Vector2iGetter oldSize, Vector2iGetter newSize)
	{}
	
	@Override
	@EmptyImplementation
	public ActionResult onPress(int button, Vector2iGetter mousePos)
	{
		return ActionResult.CONSUME;
	}
	
	@Override
	@EmptyImplementation
	public ActionResult onRelease(int button, Vector2iGetter mousePos)
	{
		return ActionResult.CONSUME;
	}
	
	@Override
	@EmptyImplementation
	public ActionResult onClick(int button, int count, Vector2iGetter mousePos)
	{
		return ActionResult.CONSUME;
	}
	
	@Override
	@EmptyImplementation
	public ActionResult onScroll(double xScroll, double yScroll, Vector2iGetter mousePos)
	{
		return ActionResult.CONSUME;
	}
	
	@Override
	public ActionResult onDragStart(Vector2iGetter mousePos)
	{
		setInternalStateBit(DRAGGING_BIT, true);
		return ActionResult.CONSUME;
	}
	
	@Override
	public ActionResult onDragEnd(Vector2iGetter startPos, Vector2iGetter mousePos)
	{
		setInternalStateBit(DRAGGING_BIT, false);
		return ActionResult.CONSUME;
	}
}