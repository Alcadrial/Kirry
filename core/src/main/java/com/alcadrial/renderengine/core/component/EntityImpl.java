package com.alcadrial.renderengine.core.component;

import java.util.concurrent.CompletableFuture;

import org.eclipse.collections.impl.list.mutable.FastList;
import org.jspecify.annotations.Nullable;

public class EntityImpl implements Entity {
	
	private int maxComponentId;
	private FastList<@Nullable Component<?>> components;
	
	public EntityImpl(int maxComponentId)
	{
		this.maxComponentId = maxComponentId;
		components = new FastList<>() {
			
			@Override
			public @Nullable Component<?> set(int index, @Nullable Component<?> element)
			{
				if (index >= size)
				{
					ensureCapacity(index + 1);
					size = index + 1;
				}
				super.set(index, element);
				return null;
			};
		};
	}
	
	@SuppressWarnings({"unchecked"})
	@Override
	public <T extends ComponentType> @Nullable Component<T> getComponent(T type)
	{
		int id = type.getId();
		if (id > components.size()) return null;
		return (Component<T>) components.get(id);
	}
	
	@Override
	public <T extends ComponentType> boolean setComponent(T type, Component<T> component)
	{
		int id = type.getId();
		if (id > maxComponentId) return false;
		components.set(id, component);
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	public CompletableFuture<?> load()
	{
		FastList<CompletableFuture<?>> futures = new FastList<>();
		for (Component<?> component : components) if (component != null) futures.add(component.load());
		return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
	}
	
	@SuppressWarnings("null")
	@Override
	public CompletableFuture<?> reload()
	{
		FastList<CompletableFuture<?>> futures = new FastList<>();
		for (Component<?> component : components) if (component != null) futures.add(component.reload());
		return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
	}
	
	@Override
	public void close()
	{
		for (Component<?> component : components) if (component != null) component.close();
	}
}
