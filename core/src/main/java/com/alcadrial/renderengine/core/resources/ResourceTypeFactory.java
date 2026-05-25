package com.alcadrial.renderengine.core.resources;

import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Maps;

@SuppressWarnings("null")
final class ResourceTypeFactory {
	
	private static final MutableMap<Class<?>, ? extends ResourceType<?>> CACHE;
	private static int nextId;
	
	static
	{
		CACHE = Maps.mutable.empty();
		nextId = 0;
	}
	
	@SuppressWarnings("unchecked")
	static <R extends Resource> ResourceType<R> of(Class<? extends R> clazz)
	{
		return (ResourceType<R>) CACHE.getIfAbsentPut(clazz, () -> {
			int id = nextId++;
			return new ResourceType<>() {
				
				public int getId()
				{
					return id;
				}
				@Override
				public String toString()
				{
					return "ResourceType{" + clazz + "}";
				}
			};
		});
	}
	
	static int getRegisteredTypesCount()
	{
		return nextId;
	}
}
