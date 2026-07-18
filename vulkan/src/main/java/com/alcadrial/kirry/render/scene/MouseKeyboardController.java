package com.alcadrial.kirry.render.scene;

import org.lwjgl.glfw.GLFW;

import com.alcadrial.kector.annotation.Angle;
import com.alcadrial.kector.vector.Vector3d;
import com.alcadrial.kector.vector.Vector3dr;
import com.alcadrial.kector.vector.axis.Axisd;
import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;
import com.alcadrial.kirry.render.window.EventRegistry;
import com.alcadrial.kirry.render.window.event.KeyboardEvent;
import com.alcadrial.kirry.render.window.event.MouseMoveEvent;

public class MouseKeyboardController extends Controller {
	
	protected Vector3d direction;
	
	public MouseKeyboardController(ControllerTarget target)
	{
		super(target);
		direction = Vector3d.of();
	}
	
	@Override
	public void update(CommandBuffer commandBuffer, float elapsed)
	{
		if (direction.maxSize() > 0.00001)
		{
			target.getDirection().setAll(direction).unitary();
			target.update(commandBuffer, elapsed);
		}
	}
	
	@Override
	public void registerEvents(EventRegistry registry)
	{
		registry.register((MouseMoveEvent) (_, _, _, _, dx, dy) -> {
			if (isActive)
			{
				target.setXRotation((@Angle float) (target.getXRotation() + dy * yRotationSensivity / 200));
				target.setYRotation((@Angle float) (target.getYRotation() + dx * xRotationSensivity / 200));
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
}
