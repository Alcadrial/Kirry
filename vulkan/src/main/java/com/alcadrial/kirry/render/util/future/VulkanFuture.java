package com.alcadrial.kirry.render.util.future;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jspecify.annotations.Nullable;

import com.alcadrial.kirry.render.util.exception.ExternalException;
import com.alcadrial.kirry.render.util.exception.VulkanException;
import com.alcadrial.kirry.render.util.functional.VulkanConsumer;
import com.alcadrial.kirry.render.util.functional.VulkanFunction;
import com.alcadrial.kirry.render.util.functional.VulkanRunnable;
import com.alcadrial.kirry.render.util.functional.VulkanSupplier;

public interface VulkanFuture<V> {
	
	static <V> VulkanFuture<V> ofFuture(Future<V> future)
	{
		return new FutureToVulkanFutureWrapper<>(future);
	}
	
	@SuppressWarnings("unchecked")
	static <V> VulkanFuture<@Nullable V> completedTask()
	{
		return (VulkanFuture<@Nullable V>) CompletedTaskHandler.NULL_COMPLETED;
	}
	
	static <V> VulkanFuture<V> completedTask(V value)
	{
		return new CompletedTaskHandler<>(value);
	}
	
	static CompletableTaskHandler<?> completableTask()
	{
		return new CompletableTaskHandler<>();
	}
	
	public static VulkanFuture<?> submitAsync(VulkanRunnable action)
	{
		return new AsyncRunnableTaskHandler(action);
	}
	
	public static <V> VulkanFuture<V> submitAsync(Callable<V> action)
	{
		return new AsyncSupplierTaskHandler<>(action);
	}
	
	public static VulkanFuture<?> composeAfter(VulkanFuture<?> future, VulkanRunnable action)
	{
		return new AsyncDependentTaskHandler<>(future, _ -> {
			action.run();
			return null;
		});
	}
	
	public static <V> VulkanFuture<V> composeAfter(VulkanFuture<?> future, VulkanSupplier<V> action)
	{
		return new AsyncDependentTaskHandler<>(future, _ -> action.get());
	}
	
	public static <V> VulkanFuture<?> composeAfter(VulkanFuture<V> future, VulkanConsumer<V> action)
	{
		return new AsyncDependentTaskHandler<>(future, v -> {
			action.accept(v);
			return null;
		});
	}
	
	public static <V, W> VulkanFuture<W> composeAfter(VulkanFuture<V> future, VulkanFunction<V, W> action)
	{
		return new AsyncDependentTaskHandler<>(future, action);
	}
	
	void await() throws InterruptedException;
	void await(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException;
	V get() throws VulkanException, InterruptedException;
	V get(long timeout, TimeUnit unit) throws VulkanException, InterruptedException, TimeoutException;
	boolean isDone();
	
	default VulkanFuture<V> join() throws VulkanException
	{
		try
		{
			get();
		}
		catch (InterruptedException e)
		{
			throw new ExternalException(e);
		}
		return this;
	}
	
	default Future<V> toFuture()
	{
		return new VulkanFutureToFutureWrapper<>(this);
	}
}
