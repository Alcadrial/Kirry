package com.alcadrial.kirry.render.scene;

import org.lwjgl.glfw.GLFW;

import com.alcadrial.kector.annotation.Angle;
import com.alcadrial.kector.vector.Vector3dr;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.kector.vector.axis.Axisd;
import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;
import com.alcadrial.kirry.render.window.EventRegistry;
import com.alcadrial.kirry.render.window.event.KeyboardEvent;
import com.alcadrial.kirry.render.window.event.MouseMoveEvent;

public class CameraController extends Controller {
	
	private Camera camera;
	
	public CameraController(Camera camera)
	{
		this.camera = camera;
	}
	
	@Override
	public void update(CommandBuffer commandBuffer, float elapsed)
	{
		if (direction.maxSize() > 0.00001)
		{
			camera.setPosition(Vector3f.of(direction).unitary().multiplyAll(elapsed / 1000).add(camera.getPosition()));
		}
	}
	
	@Override
	public void registerEvents(EventRegistry registry)
	{
		registry.register((MouseMoveEvent) (_, _, _, _, dx, dy) -> {
			if (isActive)
			{
				camera.setXRotation((@Angle float) (camera.getXRotation() + dy * yRotationSensivity / 50));
				camera.setYRotation((@Angle float) (camera.getYRotation() + dx * xRotationSensivity / 50));
			}
		});
		registry.register((KeyboardEvent) (key, _, action, _) -> {
			if (isActive)
			{
				Vector3dr v = switch (key)
				{
					case GLFW.GLFW_KEY_W -> Axisd.NEGATIVE_Z;
					case GLFW.GLFW_KEY_A -> Axisd.NEGATIVE_X;
					case GLFW.GLFW_KEY_S -> Axisd.Z;
					case GLFW.GLFW_KEY_D -> Axisd.X;
					case GLFW.GLFW_KEY_SPACE -> Axisd.Y;
					case GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_LEFT_CONTROL -> Axisd.NEGATIVE_Y;
					default -> Vector3dr.of();
				};
				
				switch (action)
				{
					case GLFW.GLFW_PRESS -> direction.add(v);
					case GLFW.GLFW_RELEASE -> direction.subtract(v);
				}
			}
		});
	}
	
	public void setCamera(Camera camera)
	{
		this.camera = camera;
	}
}
