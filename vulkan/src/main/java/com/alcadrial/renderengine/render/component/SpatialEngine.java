package com.alcadrial.renderengine.render.component;

import org.eclipse.collections.api.list.MutableList;
import org.jspecify.annotations.Nullable;

import com.alcadrial.renderengine.core.component.Entity;
import com.alcadrial.renderengine.core.component.EntityEngine;
import com.alcadrial.renderengine.render.Computer;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.commandbuffer.CommandBuffer;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public class SpatialEngine implements EntityEngine<SpatialEngine.InitParams, SpatialEngine.ReinitParams, SpatialEngine.UpdateParams, SpatialEngine.Properties, VulkanException> {
	
	public record InitParams(
		int vertexLength,
		VulkanBuffer vertexBuffer, int vertexStride,
		@Nullable VulkanBuffer normalBuffer, int normalStride
	) {}
	
	public record ReinitParams(
		int vertexLength,
		VulkanBuffer vertexBuffer, int vertexStride,
		@Nullable VulkanBuffer normalBuffer, int normalStride
	) {}
	
	public record UpdateParams(
		CommandBuffer commandBuffer,
		int frameIndex,
		float enlapsedTime
	) {}
	
	public record Properties(
	) {}
	
	private @Nullable Computer computer;
	private MutableList<Entity> entities;
	
	public SpatialEngine(MutableList<Entity> entities)
	{
		this.entities = entities;
	}

	@Override
	public void init(InitParams params) throws VulkanException
	{/*
		computer = Computer.builder()
			.descriptorSets()
			.computePipeline(null)
			.bindings()
			.groupCount(Vector3i.of(0, 0, 0))
			.build();*/
	}
	@Override
	public void reinit(ReinitParams params) throws VulkanException
	{}
	
	@Override
	public void update(UpdateParams params) throws VulkanException
	{
		Computer computer = VulkanErrorUtils.nonNull(this.computer, "Spatial engine was not initialized");
		float enlapsedTime = params.enlapsedTime;
		for (Entity entity : entities)
		{
			SpatialComponent spatial = entity.getComponent(SpatialComponent.TYPE);
			if (spatial != null)
			{
				SpatialComponent.Properties properties = spatial.getProperties();
				if (properties.isDirty())
				{
					
					spatial.reset();
				}
			}
		}
		computer.dispatch(params.commandBuffer, 0, 0, 0, 0);
	}

	@Override
	public Properties getProperties()
	{
		return new Properties();
	}

	@Override
	public void close()
	{}
}
