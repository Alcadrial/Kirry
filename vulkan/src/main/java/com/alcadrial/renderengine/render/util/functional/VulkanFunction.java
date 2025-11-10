package com.alcadrial.renderengine.render.util.functional;

import java.util.function.Function;

import com.alcadrial.renderengine.render.util.exception.VulkanException;

@FunctionalInterface
public interface VulkanFunction<T, R> {
	
	static <T, R> VulkanFunction<T, R> of(Function<T, R> function)
	{
		return function::apply;
	}
	
	R apply(T t) throws VulkanException;
}
