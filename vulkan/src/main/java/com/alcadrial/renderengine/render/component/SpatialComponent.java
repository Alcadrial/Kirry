package com.alcadrial.renderengine.render.component;

import java.util.concurrent.CompletableFuture;

import org.jspecify.annotations.Nullable;

import com.alcadrial.kector.vector.getter.Vector3fGetter;
import com.alcadrial.renderengine.core.component.Component;
import com.alcadrial.renderengine.core.component.ComponentType;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public class SpatialComponent implements Component<SpatialComponent.Params, SpatialComponent.Properties, VulkanException> {

	public static final ComponentType<SpatialComponent> TYPE = ComponentType.of(SpatialComponent.class);
	
	public record Params(
		@Nullable Vector3fGetter position,
		@Nullable Vector3fGetter movement
	) {}
	
	public record Properties(
		boolean isDirty
	) {}
	
	private @Nullable Vector3fGetter position;
	private @Nullable Vector3fGetter movement;
	private boolean isDirty;
	
	public SpatialComponent()
	{
		
	}
	
	@Override
	public CompletableFuture<?> load()
	{
		return reload();
	}
	
	@Override
	public CompletableFuture<?> reload()
	{
		return CompletableFuture.completedFuture(null);
	}
	
	@Override
	public boolean isLoaded()
	{
		return true;
	}
	
	@Override
	public int reloadCount()
	{
		return 0;
	}
	
	@Override
	public void init(Params params)
	{
		reinit(params);
	}
	
	@Override
	public void reinit(Params params)
	{
		if (params.position != null) position = params.position;
		if (params.movement != null) movement = params.movement;
		isDirty = true;
	}
	
	@Override
	public boolean validate(Params params) throws VulkanException
	{
		//TODO
		return false;
	}
	
	public void reset()
	{
		position = null;
		movement = null;
		isDirty = false;
	}
	
	@Override
	public Properties getProperties()
	{
		return new Properties(isDirty);
	}
	
	@Override
	public void close()
	{}

	@Override
	public boolean listenResource()
	{
		return false;
	}
}
