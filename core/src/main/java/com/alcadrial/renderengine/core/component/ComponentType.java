package com.alcadrial.renderengine.core.component;

/**
 * The type of a {@link Component} used to select a Component from an {@link Entity}.
 * <p>
 * Use {@link #of(Class)} to create a new instance and cache it for the best performance.
 * @param <C>
 */
public interface ComponentType<C extends Component<?, ?, ?>> {
	
	static <C extends Component<?, ?, ?>> ComponentType<C> of(Class<? extends C> clazz)
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
