package com.alcadrial.renderengine.util;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class AbstractVolatileData<D, L, U> implements Supplier<D> {
	
	protected D data;
	
	protected AbstractVolatileData()
	{
		
	}
	
	public boolean isPresent()
	{
		return data != null;
	}
	
	public void ifPresent(Consumer<D> action)
	{
		if (data != null) action.accept(data);
	}
	
	public abstract void load(L loader);
	public abstract void unload(U unloader);
	
	@Override
	public D get()
	{
		if (data == null) throw new NoSuchElementException("No value present");
		return data;
	}
	
	public boolean isEmpty()
	{
		return data == null;
	}
	
	public void ifPresentOrElse(Consumer<D> action, Runnable emptyAction)
	{
		if (data != null) action.accept(data);
		else emptyAction.run();
	}
	
	public Stream<D> stream()
	{
		if (isPresent()) return Stream.of(data);
		return Stream.empty();
	}
	
	public D orElse(D other)
	{
		return data != null ? data : other;
	}
	
	public D orElseGet(Supplier<D> supplier)
	{
		return data != null ? data : supplier.get();
	}
	
	public D orElseThrow()
	{
		if (data == null) throw new NoSuchElementException("No value present");
		return data;
	}
	
	public <X extends Throwable> D orElseThrow(Supplier<X> exceptionSupplier) throws X
	{
		if (data != null) return data;
		throw exceptionSupplier.get();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return this == obj || obj instanceof AbstractVolatileData<?, ?, ?> other && Objects.equals(data, other.data);
	}
	
	@Override
	public int hashCode()
	{
		return data == null ? 0 : data.hashCode();
	}
	
	@Override
	public String toString()
	{
		return data != null ? data.toString() : "empty";
	}
}
