package com.alcadrial.kirry.render.util.future;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

import org.jspecify.annotations.Nullable;

public class CompletableFutureUtils {

	public static CompletableFuture<?> ofAll(@Nullable CompletableFuture<?>... futures)
	{
		return ofAll(Arrays.asList(futures));
	}
	
	public static CompletableFuture<?> ofAll(Iterable<@Nullable CompletableFuture<?>> futures)
	{
		CompletableFuture<?> future = new CompletableFuture<>();
		handleFutureAnd(future, futures.iterator());
		return future;
	}
	
	private static void handleFutureAnd(CompletableFuture<?> target, Iterator<@Nullable CompletableFuture<?>> i)
	{
		while (i.hasNext())
		{
			CompletableFuture<?> f0 = i.next();
			if (f0 == null) continue;
			f0.handle((_, e) -> {
				if (e != null) target.completeExceptionally(e);
				else handleFutureAnd(target, i);
				return null;
			});
			return;
		}
		target.complete(null);
	}
}
