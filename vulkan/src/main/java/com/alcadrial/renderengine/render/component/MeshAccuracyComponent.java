package com.alcadrial.renderengine.render.component;

import java.util.concurrent.CompletableFuture;

import org.jspecify.annotations.Nullable;

import com.alcadrial.renderengine.core.component.AbstractComponent;
import com.alcadrial.renderengine.core.component.ComponentType;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public class MeshAccuracyComponent extends AbstractComponent<MeshAccuracyComponent.Params, MeshAccuracyComponent.Properties, VulkanException> {
	
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
	) {}
	
	public record Properties(
		boolean useTextureUv,
		boolean useNormals,
		int vertexesSize,
		int indicesSize,
		int textureUvSize,
		int normalsSize
	) {}
	
	@Override
	public CompletableFuture<?> reload()
	{
		throw new UnsupportedOperationException("WIP");
	}
	
	@Override
	public void reinit(Params params)
	{
		throw new UnsupportedOperationException("WIP");
	}
	
	@Override
	public boolean validate(Params params) throws VulkanException
	{
		throw new UnsupportedOperationException("WIP");
	}
	
	@Override
	public Properties getProperties()
	{
		throw new UnsupportedOperationException("WIP");
	}
	
	@Override
	public void close()
	{
		throw new UnsupportedOperationException("WIP");
	}
}
