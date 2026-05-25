package com.alcadrial.renderengine.core.component;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import org.jspecify.annotations.Nullable;

public interface Entity {

	static Entity supportAll(ComponentType<?>... components)
	{
		return of(Arrays.stream(components).mapToInt(ComponentType::getId).max().orElse(-1));
	}

	static Entity supportAll(Collection<ComponentType<?>> components)
	{
		return of(components.stream().mapToInt(ComponentType::getId).max().orElse(-1));
	}
	
	static Entity of(int maxComponentId)
	{
		return new EntityImpl(maxComponentId);
	}
	
	<C extends Component<?, ?, ?>> @Nullable C getComponent(ComponentType<C> type);
	<C extends Component<?, ?, ?>> boolean setComponent(ComponentType<C> type, C component);
	
	CompletableFuture<?> load();
	CompletableFuture<?> reload();
	void close();
}
