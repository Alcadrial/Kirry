package com.alcadrial.kirry.render.window.widget;

import org.jspecify.annotations.NonNull;

import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3i;
import com.alcadrial.kirry.render.window.AbstractWidget;
import com.alcadrial.kirry.render.window.EventRegistry;
import com.alcadrial.kirry.render.window.VulkanWindow;


public class CameraView extends AbstractWidget {
	
	public CameraView(VulkanWindow window, Vector3i pos, Vector2i size)
	{
		super(window, pos, size);
	}

	@Override
	public void registerEvents(@NonNull EventRegistry registry)
	{}
}
