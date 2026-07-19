package com.alcadrial.kirry.render.scene;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWGamepadState;
import org.lwjgl.system.MemoryStack;

import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;
import com.alcadrial.kirry.render.window.EventRegistry;

public class JoystickController extends Controller {
	
	private int jid;
	
	public JoystickController(ControllerTarget target)
	{
		super(target);
	}
	
	@Override
	public void update(CommandBuffer commandBuffer, float elapsed)
	{
		if (jid < 0)
		{
			for (int i = 0; i <= GLFW.GLFW_JOYSTICK_LAST; i++)
			{
				if (GLFW.glfwJoystickPresent(i))
				{
					jid = i;
					System.out.println("Changed joystick to " + jid);
					break;
				}
			}
		}
		if (jid < 0) return;
		FloatBuffer axis = GLFW.glfwGetJoystickAxes(jid);
		ByteBuffer buttons = GLFW.glfwGetJoystickButtons(jid);
		String guid = GLFW.glfwGetJoystickGUID(jid);
		if (axis == null || buttons == null || guid == null)
		{
			jid = -1;
			return;
		}
		
		if (GLFW.glfwJoystickIsGamepad(jid))
		{
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				GLFWGamepadState state = GLFWGamepadState.malloc(stack);
				if (GLFW.glfwGetGamepadState(jid, state))
				{
					float y = 0;
					if (state.buttons().get(GLFW.GLFW_GAMEPAD_BUTTON_CROSS) == 1) y += 1;
					if (state.buttons().get(GLFW.GLFW_GAMEPAD_BUTTON_CIRCLE) == 1) y += -1;
					
					float leftTrigger = state.axes().get(GLFW.GLFW_GAMEPAD_AXIS_LEFT_TRIGGER);
					float rightTrigger = state.axes().get(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER);
					
					if (Math.abs(leftTrigger) > 0.01) y += -leftTrigger;
					if (Math.abs(rightTrigger) > 0.01) y += rightTrigger;
					
					float leftXAxis = state.axes().get(GLFW.GLFW_GAMEPAD_AXIS_LEFT_X);
					float leftYAxis = state.axes().get(GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y);
					
					if (Math.abs(leftXAxis * leftYAxis) > 0.005)
					{
						target.getDirection().setAll(leftXAxis, y, leftYAxis);
					}
					else
					{
						target.getDirection().setAll(0, y, 0);
					}
					
					float rightXAxis = state.axes().get(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_X);
					float rightYAxis = state.axes().get(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_Y);
					
					if (Math.abs(rightXAxis * rightYAxis) > 0.005)
					{
						target.setXRotation(target.getXRotation() + rightYAxis * xRotationSensivity * elapsed / 200);
						target.setYRotation(target.getYRotation() + rightXAxis * yRotationSensivity * elapsed / 200);
					}
				}
			}
		}
		
		target.update(commandBuffer, elapsed);
	}
	
	@Override
	public void registerEvents(EventRegistry registry)
	{
		if (jid < 0 && GLFW.glfwJoystickPresent(GLFW.GLFW_JOYSTICK_1)) jid = GLFW.GLFW_JOYSTICK_1;
		GLFW.glfwSetJoystickCallback((jid, event) -> {
			switch (event)
			{
				case GLFW.GLFW_CONNECTED -> {
					if (this.jid < 0) this.jid = jid;
					System.out.println("Connected " + jid);
				}
				case GLFW.GLFW_DISCONNECTED -> {
					if (this.jid >= 0) this.jid = -1;
					System.out.println("Disconnected " + jid);
				}
			}
		});
	}
}
