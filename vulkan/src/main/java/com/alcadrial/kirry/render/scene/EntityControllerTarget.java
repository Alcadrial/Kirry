package com.alcadrial.kirry.render.scene;

import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import com.alcadrial.kector.annotation.Angle;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.kirry.core.component.Entity;
import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;
import com.alcadrial.kirry.render.component.SpatialComponent;
import com.alcadrial.kirry.render.component.SpatialComponent.State;
import com.alcadrial.kirry.render.util.VulkanErrorUtils;

public class EntityControllerTarget extends ControllerTarget {
	
	private Entity entity;
	private Vector3f oldPos;
	private Vector3f pos;
	private @Nullable Consumer<Vector3f> onUpdate;
	
	public EntityControllerTarget(Entity entity)
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
		pos.add(tempDirection.setAll(direction).multiplyAll(elapsed / 1000));
		spatial.getState().isManuallyUpdated()[0] = true;
		if (onUpdate != null) onUpdate.accept(pos);
	}
	
	public Vector3f getPos()
	{
		return pos;
	}

	@Override
	protected float getXRotation()
	{
		return 0;
	}

	@Override
	protected float getYRotation()
	{
		return 0;
	}

	@Override
	protected void setXRotation(@Angle float f)
	{}

	@Override
	protected void setYRotation(@Angle float f)
	{}
}
