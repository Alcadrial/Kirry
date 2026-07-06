package com.alcadrial.kirry.render.scene;

import com.alcadrial.kector.vector.Vector3d;
import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;
import com.alcadrial.kirry.render.window.EventRegistry;

public abstract class Controller {
	
	protected float xRotationSensivity;
	protected float yRotationSensivity;
	protected boolean isActive;
	protected Vector3d direction;
	
	public Controller()
	{
		xRotationSensivity = 1;
		yRotationSensivity = 1;
		isActive = false;
		direction = Vector3d.of();
	}

	public abstract void update(CommandBuffer commandBuffer, float elapsed);
	public abstract void registerEvents(EventRegistry registry);
	
	public void setXRotationSensivity(float xRotationSensivity)
	{
		this.xRotationSensivity = xRotationSensivity;
	}
	
	public void setYRotationSensivity(float yRotationSensivity)
	{
		this.yRotationSensivity = yRotationSensivity;
	}
	
	public void setActive(boolean isActive)
	{
		this.isActive = isActive;
	}
	
	public boolean isActive()
	{
		return isActive;
	}
	
	public Vector3d getDirection()
	{
		return direction;
	}
}
