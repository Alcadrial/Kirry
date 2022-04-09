package com.alcadra.renderengine.window.widget;

import com.alcadra.math.vector.Vector2i;
import com.alcadra.math.vector.Vector3i;
import com.alcadra.renderengine.window.Window;

public class Button extends Widget implements ClickableWidget {
	
	public Button(Vector3i pos, Vector2i size, Window window)
	{
		super(window, pos, size);
	}
	
	@Override
	public void onRightClick(Vector2i mousePos)
	{
		System.out.println("r");
	}
	
	@Override
	public void onLeftClick(Vector2i mousePos)
	{
		System.out.println("l");
	}
	
	@Override
	protected boolean onResize(Vector2i oldSize, Vector2i newSize)
	{
		return true;
	}
	
	@Override
	protected void onResized(Vector2i oldSize, Vector2i newSize)
	{}
}