package com.alcadrial.kirry.render.scene;

import com.alcadrial.kector.vector.Vector3d;
import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;
import com.alcadrial.kirry.render.window.EventRegistry;

public abstract class Controller {
	
	protected float xRotationSensivity;
	protected float yRotationSensivity;
	protected boolean isActive;
	protected ControllerTarget target;
	
	public Controller(ControllerTarget target)
	{
		xRotationSensivity = 1;
		yRotationSensivity = 1;
		isActive = false;
		this.target = target;
	}
	
	public void update(CommandBuffer commandBuffer, float elapsed)
	{
		target.update(commandBuffer, elapsed);
	}
	
	public abstract void registerEvents(EventRegistry registry);
	
	public ControllerTarget getTarget()
	{
		return target;
	}
	
	public void setTarget(ControllerTarget target)
	{
		this.target = target;
	}
	
	public void setXRotationSensivity(float xRotationSensivity)
	{
		target.setXRotationSensivity(xRotationSensivity);
	}
	
	public void setYRotationSensivity(float yRotationSensivity)
	{
		target.setYRotationSensivity(yRotationSensivity);
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
		return target.getDirection();
	}
}
