package com.alcadrial.renderengine.render.component;

import java.util.concurrent.CompletableFuture;

import org.jspecify.annotations.Nullable;

import com.alcadrial.renderengine.core.component.AbstractComponent;
import com.alcadrial.renderengine.core.component.ComponentInitParams;
import com.alcadrial.renderengine.core.component.ComponentProperties;
import com.alcadrial.renderengine.core.component.ComponentType;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;

public class MeshAccuracyComponent extends AbstractComponent<ComponentInitParams, ComponentProperties> {
	
	public enum Type implements ComponentType<MeshComponent> {
		
		INSTANCE;
		
		@Override
		public int getId()
		{
			return 3;
		}
	}
	
	public record Params(
		int vertexLength,
		@Nullable VulkanBuffer vertexBuffer, int vertexStride,
		@Nullable VulkanBuffer indexBuffer,
		@Nullable VulkanBuffer textureUvBuffer, int textureUvStride,
		@Nullable VulkanBuffer normalBuffer, int normalStride
	) implements ComponentInitParams {}
	
	public record Properties(
		boolean useTextureUv,
		boolean useNormals,
		int vertexesSize,
		int indicesSize,
		int textureUvSize,
		int normalsSize
	) implements ComponentProperties {}
	
	@Override
	public CompletableFuture<?> reload()
	{
		throw new UnsupportedOperationException("WIP");
	}
	
	@Override
	public void reinit(ComponentInitParams params)
	{
		throw new UnsupportedOperationException("WIP");
	}
	
	@Override
	public ComponentProperties getProperties()
	{
		throw new UnsupportedOperationException("WIP");
	}
	
	@Override
	public void close()
	{
		throw new UnsupportedOperationException("WIP");
	}
}
