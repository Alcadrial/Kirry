package com.alcadrial.kirry.render.component;

import java.util.concurrent.CompletableFuture;

import org.jspecify.annotations.Nullable;

import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.kector.vector.Vector3fr;
import com.alcadrial.kirry.core.component.ComponentType;
import com.alcadrial.kirry.render.util.exception.VulkanException;

public class SpatialComponent extends AbstractVulkanComponent<SpatialComponent.Params, SpatialComponent.Properties> {
	
	public static final ComponentType<SpatialComponent> TYPE = ComponentType.of(SpatialComponent.class);
	
	public record Params(
		@Nullable Vector3fr position,
		@Nullable Vector3fr movement
	) {}
	
	public record Properties(
		boolean isDirty
	) {}
	
	public record State(
		Vector3f position,
		Vector3f movement,
		boolean[] isManuallyUpdated
	) {}
	
	public static SpatialComponent of(Vector3f position, Vector3f oldPosition)
	{
		SpatialComponent spatial = new SpatialComponent(new State(position, Vector3f.of(0, 0, 0), new boolean[] {false}));
		spatial.setOldPosition(oldPosition);
		return spatial;
	}
	
	public static SpatialComponent of(Vector3f position)
	{
		return new SpatialComponent(new State(position, Vector3f.of(0, 0, 0), new boolean[] {false}));
	}
	
	public static SpatialComponent of(State state)
	{
		return new SpatialComponent(state);
	}
	
	private final State state;
	private @Nullable Vector3fr oldPosition;
	private boolean isDirty;
	
	public SpatialComponent()
	{
		state = new State(Vector3f.of(0, 0, 0), Vector3f.of(0, 0, 0), new boolean[] {false});
	}
	
	public SpatialComponent(State state)
	{
		this.state = state;
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
	public void init(Params params)
	{
		reinit(params);
	}
	
	@Override
	public void reinit(Params params)
	{
		if (params.position != null) state.position.setAll(params.position);
		if (params.movement != null) state.movement.setAll(params.movement);
		isDirty = true;
	}
	
	@Override
	public boolean validate(@Nullable Params params) throws VulkanException
	{
		//TODO
		return false;
	}
	
	public void reset()
	{
		isDirty = false;
	}
	
	public void setOldPosition(Vector3fr oldPosition)
	{
		this.oldPosition = oldPosition;
	}
	
	public @Nullable Vector3fr getOldPosition()
	{
		return oldPosition;
	}
	
	@Override
	public Properties getProperties()
	{
		return new Properties(isDirty);
	}
	
	public State getState()
	{
		return state;
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
