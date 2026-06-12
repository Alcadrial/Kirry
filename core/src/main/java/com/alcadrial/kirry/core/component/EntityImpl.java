package com.alcadrial.kirry.core.component;

import java.util.concurrent.CompletableFuture;

import org.eclipse.collections.impl.list.mutable.FastList;
import org.jspecify.annotations.Nullable;

import com.alcadrial.kirry.core.util.ResizeFastList;

public class EntityImpl implements Entity {
	
	private int maxComponentId;
	private FastList<@Nullable Component<?, ?, ?>> components;
	
	public EntityImpl(int maxComponentId)
	{
		this.maxComponentId = maxComponentId;
		components = new ResizeFastList<>();
	}
	
	@SuppressWarnings({"unchecked"})
	@Override
	public <C extends Component<?, ?, ?>> @Nullable C getComponent(ComponentType<C> type)
	{
		int id = type.getId();
		if (id >= components.size()) return null;
		return (C) components.get(id);
	}
	
	@Override
	public <C extends Component<?, ?, ?>> boolean setComponent(ComponentType<C> type, C component)
	{
		int id = type.getId();
		if (id > maxComponentId) return false;
		components.set(id, component);
		return true;
	}
	
	@Override
	public CompletableFuture<?> load()
	{
		FastList<CompletableFuture<?>> futures = new FastList<>();
		for (Component<?, ?, ?> component : components) if (component != null) futures.add(component.load());
		return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
	}
	
	@Override
	public CompletableFuture<?> reload()
	{
		FastList<CompletableFuture<?>> futures = new FastList<>();
		for (Component<?, ?, ?> component : components) if (component != null) futures.add(component.reload());
		return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
	}
	
	@Override
	public boolean listenComponents()
	{
		boolean result = false;
		for (Component<?, ?, ?> component : components) if (component != null) result |= component.listenResource();
		return result;
	}
	
	@Override
	public void close()
	{
		for (Component<?, ?, ?> component : components) if (component != null) component.close();
	}
}
