package com.alcadrial.kirry.core.component;

import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Maps;

final class ComponentTypeFactory {
	
	private static final MutableMap<Class<?>, ComponentType<?>> CACHE;
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
