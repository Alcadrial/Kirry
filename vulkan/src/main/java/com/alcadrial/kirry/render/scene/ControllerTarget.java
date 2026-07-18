package com.alcadrial.kirry.render.scene;

import com.alcadrial.kector.annotation.Angle;
import com.alcadrial.kector.vector.Vector3d;
import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;

public abstract class ControllerTarget {
	
	protected float xRotationSensivity;
	protected float yRotationSensivity;
	protected Vector3d direction;
	protected Vector3d tempDirection;
	
	public ControllerTarget()
	{
		xRotationSensivity = 1;
		yRotationSensivity = 1;
		direction = Vector3d.of();
		tempDirection = Vector3d.of();
	}
	
	public abstract void update(CommandBuffer commandBuffer, float elapsed);
	
	public void setXRotationSensivity(float xRotationSensivity)
	{
		this.xRotationSensivity = xRotationSensivity;
	}
	
	public void setYRotationSensivity(float yRotationSensivity)
	{
		this.yRotationSensivity = yRotationSensivity;
	}
	
	public Vector3d getDirection()
	{
		return direction;
	}
	
	protected abstract float getXRotation();
	protected abstract float getYRotation();
	
	protected abstract void setXRotation(@Angle float f);
	protected abstract void setYRotation(@Angle float f);
}
