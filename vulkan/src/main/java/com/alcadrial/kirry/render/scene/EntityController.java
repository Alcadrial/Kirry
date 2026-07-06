package com.alcadrial.kirry.render.scene;

import com.alcadrial.kirry.core.component.Entity;
import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;
import com.alcadrial.kirry.render.window.EventRegistry;

public class EntityController extends Controller {
	
	private Entity entity;
	
	public EntityController(Entity entity)
	{
		this.entity = entity;
	}
	
	@Override
	public void update(CommandBuffer commandBuffer, float elapsed)
	{
		
	}
	
	@Override
	public void registerEvents(EventRegistry registry)
	{
		
	}
}
