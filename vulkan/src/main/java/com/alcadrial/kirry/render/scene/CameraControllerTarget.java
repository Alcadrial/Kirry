package com.alcadrial.kirry.render.scene;

import com.alcadrial.kector.annotation.Angle;
import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;

public class CameraControllerTarget extends ControllerTarget {
	
	private Camera camera;
	
	public CameraControllerTarget(Camera camera)
	{
		this.camera = camera;
	}
	
	@Override
	public void update(CommandBuffer commandBuffer, float elapsed)
	{
		if (direction.maxSize() > 0.00001)
		{
			camera.setPosition(camera.getFlatViewQuaternion().rotate(tempDirection.setAll(direction)).multiplyAll(elapsed / 1000).add(camera.getPosition()));
		}
	}
	
	public void setCamera(Camera camera)
	{
		this.camera = camera;
	}

	@Override
	protected float getXRotation()
	{
		return camera.getXRotation();
	}

	@Override
	protected float getYRotation()
	{
		return camera.getYRotation();
	}

	@Override
	protected void setXRotation(@Angle float f)
	{
		camera.setXRotation(f);
	}

	@Override
	protected void setYRotation(@Angle float f)
	{
		camera.setYRotation(f);
	}
}
