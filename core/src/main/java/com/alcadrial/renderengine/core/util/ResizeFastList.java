package com.alcadrial.renderengine.core.util;

import java.util.Collection;

import org.eclipse.collections.impl.list.mutable.FastList;
import org.jspecify.annotations.Nullable;

public class ResizeFastList<T> extends FastList<T> {
	
	public ResizeFastList()
	{}
	
	public ResizeFastList(int initialCapacity)
	{
		super(initialCapacity);
	}
	
	public ResizeFastList(T[] array)
	{
		super(array);
	}
	
	public ResizeFastList(Collection<? extends T> source)
	{
		super(source);
	}
	
	public ResizeFastList(int size, T[] array)
	{
		super(size, array);
	}
	
	@Override
	public T set(int index, @Nullable T element)
	{
		if (index >= size)
		{
			ensureCapacity(index + 1);
			size = index + 1;
			this.items[index] = element;
			return null;
		}
		T previous = this.items[index];
		this.items[index] = element;
		return previous;
	};
}
