package com.alcadrial.renderengine.core.component;


public interface ComponentType<C extends Component<?, ?>> {
	
	
	/**
	 * This value is used by an {@link Entity} for indexing,
	 * must be non negative and small enough to be accepted
	 * by the Entity.
	 **/
	int getId();
}
