package com.alcadrial.kirry.core.resources;

/**
 * The type of a {@link Resource} used to choose how to represent data in memory
 * (VulkanBuffer, Texture...),
 * often retrieved by a {@link ResourceManager}
 * <p>
 * Use {@link #of(Class)} to create a new instance and cache it for the best performance.
 * @param <R>
 */
public interface ResourceType<R extends Resource> {

	static <R extends Resource> ResourceType<R> of(Class<? extends R> clazz)
	{
		return ResourceTypeFactory.of(clazz);
	}
	
	/**
	 * This value is used by a {@link ResourceManager} for indexing,
	 * must be non negative and small enough to possibly reduce memory usage
	 **/
	int getId();
}
