package com.alcadrial.renderengine.core.component;

public interface ComponentType<C extends Component<?, ?>> {
	
	static <C extends Component<?, ?>> ComponentType<C> of(Class<? extends C> clazz)
	{
		return ComponentTypeFactory.of(clazz);
	}
	
	/**
	 * This value is used by an {@link Entity} for indexing,
	 * must be non negative and small enough to be accepted
	 * by the Entity.
	 **/
	int getId();
}
