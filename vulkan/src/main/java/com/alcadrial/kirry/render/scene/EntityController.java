package com.alcadrial.kirry.render.scene;

import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.kirry.core.component.Entity;
import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;
import com.alcadrial.kirry.render.component.SpatialComponent;
import com.alcadrial.kirry.render.component.SpatialComponent.State;
import com.alcadrial.kirry.render.util.VulkanErrorUtils;
import com.alcadrial.kirry.render.window.EventRegistry;

public class EntityController extends Controller {
	
	private Entity entity;
	private Vector3f pos;
	
	public EntityController(Entity entity)
	{
		this.entity = entity;
		State state = VulkanErrorUtils.nonNull(entity.getComponent(SpatialComponent.TYPE), "Cannot control an entity that can't move").getState();
		pos = state.position();
	}
	
	@Override
	public void update(CommandBuffer commandBuffer, float elapsed)
	{
		
	}
	
	@Override
	public void registerEvents(EventRegistry registry)
	{
		
	}
	
	public Vector3f getPos()
	{
		return pos;
	}
}
