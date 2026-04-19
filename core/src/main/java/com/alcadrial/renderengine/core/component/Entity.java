package com.alcadrial.renderengine.core.component;

import java.util.concurrent.CompletableFuture;

import org.jspecify.annotations.Nullable;

public interface Entity {
	
	static Entity of(int maxComponentId)
	{
		return new EntityImpl(maxComponentId);
	}
	
	<T extends ComponentType> @Nullable Component<T> getComponent(T type);
	<T extends ComponentType> boolean setComponent(T type, Component<T> component);
	
	CompletableFuture<?> load();
	CompletableFuture<?> reload();
	void close();
}
