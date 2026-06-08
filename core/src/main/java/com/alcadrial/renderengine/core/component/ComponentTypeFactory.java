package com.alcadrial.renderengine.core.component;

import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Maps;

@SuppressWarnings("null")
final class ComponentTypeFactory {
	
	private static final MutableMap<Class<?>, ? extends ComponentType<?>> CACHE;
	private static int nextId;
	
	static
	{
		CACHE = Maps.mutable.empty();
		nextId = 0;
	}
	
	@SuppressWarnings("unchecked")
	static <C extends Component<?, ?, ?>> ComponentType<C> of(Class<? extends C> clazz)
	{
		return (ComponentType<C>) CACHE.getIfAbsentPut(clazz, () -> {
			int id = nextId++;
			return () -> id;
		});
	}
	
	static int getRegisteredTypesCount()
	{
		return nextId;
	}
}
