package com.alcadrial.renderengine.core.component;

import java.util.concurrent.CompletableFuture;

public interface Component<T extends ComponentType> {

	CompletableFuture<?> load();
	CompletableFuture<?> reload();
	void init(ComponentInitParams<T> params);
	void reinit(ComponentInitParams<T> params);
	ComponentProperties<T> getProperties();
	void close();
}
