package com.alcadrial.kirry.render.scene;

import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import com.alcadrial.kector.vector.Vector3dr;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.kector.vector.axis.Axisd;
import com.alcadrial.kirry.core.component.Entity;
import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;
import com.alcadrial.kirry.render.component.SpatialComponent;
import com.alcadrial.kirry.render.component.SpatialComponent.State;
import com.alcadrial.kirry.render.util.VulkanErrorUtils;
import com.alcadrial.kirry.render.window.EventRegistry;
import com.alcadrial.kirry.render.window.event.KeyboardEvent;

public class EntityController extends Controller {
	
	private Entity entity;
	private Vector3f oldPos;
	private Vector3f pos;
	private @Nullable Consumer<Vector3f> onUpdate;
	
	public EntityController(Entity entity)
	{
		this.entity = entity;
		SpatialComponent spatial = VulkanErrorUtils.nonNull(entity.getComponent(SpatialComponent.TYPE), "Cannot control an entity that can't move");
		State state = spatial.getState();
		pos = state.position();
		spatial.setOldPosition(oldPos = Vector3f.of(pos));
	}

	public void onUpdate(Consumer<Vector3f> onUpdate)
	{
		this.onUpdate = onUpdate;
	}
	
	@Override
	public void update(CommandBuffer commandBuffer, float elapsed)
	{
		if (direction.maxSize() <= 0.00001) return;
		SpatialComponent spatial = entity.getComponent(SpatialComponent.TYPE);
		if (spatial == null) return;
		oldPos.setAll(pos);
		pos.add(Vector3f.of(direction).unitary().multiplyAll(elapsed / 1000));
		spatial.getState().isManuallyUpdated()[0] = true;
		if (onUpdate != null) onUpdate.accept(pos);
	}
	
	@Override
	public void registerEvents(EventRegistry registry)
	{
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
	
	public Vector3f getPos()
	{
		return pos;
	}
}
