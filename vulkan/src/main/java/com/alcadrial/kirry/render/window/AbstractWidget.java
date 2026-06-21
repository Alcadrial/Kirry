package com.alcadrial.kirry.render.window;

import java.math.RoundingMode;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2fr;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector2ir;
import com.alcadrial.kector.vector.Vector3i;
import com.alcadrial.kector.vector.Vector3ir;
import com.alcadrial.kector.vector.Vector4f;
import com.alcadrial.kector.vector.Vector4fr;
import com.alcadrial.kector.vector.Vector4i;
import com.alcadrial.kirry.core.util.EmptyImplementation;
import com.alcadrial.kirry.render.Renderer;
import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;
import com.alcadrial.kirry.render.memory.MemoryManager;
import com.alcadrial.kirry.render.texture.Atlas;
import com.alcadrial.kirry.render.util.exception.VulkanException;

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
	
	public AbstractWidget(VulkanWindow window, Vector3ir pos, Vector2ir size)
	{
		this.window = window;
		this.pos = Vector3i.of(pos);
		this.size = Vector2i.of(size);
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
		Vector4f transformPos = matrix.multiply(Vector4fr.of(pos, 1));
		Vector4f transformSize = matrix.multiply(Vector4fr.of(size, 0, 0));
		return Vector4i.of(transformPos.x(), transformPos.y(), transformPos.x() + transformSize.x(), transformPos.y() + transformSize.y(), RoundingMode.FLOOR);
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
	public boolean isHovered(Vector2ir v)
	{
		return v.x() > pos.x() && v.x() < pos.x() + size.x() && v.y() > pos.y() && v.y() < pos.y() + size.y();
	}
	
	@Override
	public boolean isHovered(Vector2fr v)
	{
		return v.x() > pos.x() && v.x() < pos.x() + size.x() && v.y() > pos.y() && v.y() < pos.y() + size.y();
	}
	
	protected void onMouseEnter(Vector2ir enterPos)
	{
		setInternalStateBit(HOVERED_BIT, true);
	}
	
	protected void onMouseExit(Vector2ir exitPos)
	{
		setInternalStateBit(HOVERED_BIT, false);
	}
	
	@Override
	public void onMouseMove(Vector2ir oldPos, Vector2ir newPos)
	{
		boolean wasOvered = isMouseHover();
		boolean isPassedThrough = wasOvered ^ isHovered(newPos);
		if (isPassedThrough && wasOvered) onMouseExit(newPos);
		else if (isPassedThrough) onMouseEnter(newPos);
	}
	
	@EmptyImplementation
	protected void move(Vector3ir newPos)
	{}
	
	@Override
	public void setSize(Vector2ir size)
	{
		Vector2ir oldSize = Vector2ir.of(this.size);
		resize(size);
		this.size.setAll(size);
		onResize(oldSize, size);
	}
	
	@EmptyImplementation
	protected void resize(Vector2ir newSize)
	{}
	
	@Override
	public Vector3ir getPos()
	{
		return pos;
	}
	
	@Override
	public Vector2ir getSize()
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
	public void setPos(Vector3ir pos)
	{
		Vector3ir oldPos = Vector3ir.of(this.pos);
		move(pos);
		this.pos.setAll(pos);
		onMove(oldPos, pos);
	}
	
	@EmptyImplementation
	protected void onMove(Vector3ir oldPos, Vector3ir newPos)
	{}
	
	@EmptyImplementation
	protected void onResize(Vector2ir oldSize, Vector2ir newSize)
	{}
	
	@Override
	@EmptyImplementation
	public ActionResult onPress(int button, Vector2ir mousePos)
	{
		return ActionResult.CONSUME;
	}
	
	@Override
	@EmptyImplementation
	public ActionResult onRelease(int button, Vector2ir mousePos)
	{
		return ActionResult.CONSUME;
	}
	
	@Override
	@EmptyImplementation
	public ActionResult onClick(int button, int count, Vector2ir mousePos)
	{
		return ActionResult.CONSUME;
	}
	
	@Override
	@EmptyImplementation
	public ActionResult onScroll(double xScroll, double yScroll, Vector2ir mousePos)
	{
		return ActionResult.CONSUME;
	}
	
	@Override
	public ActionResult onDragStart(Vector2ir mousePos)
	{
		setInternalStateBit(DRAGGING_BIT, true);
		return ActionResult.CONSUME;
	}
	
	@Override
	public ActionResult onDragEnd(Vector2ir startPos, Vector2ir mousePos)
	{
		setInternalStateBit(DRAGGING_BIT, false);
		return ActionResult.CONSUME;
	}
}