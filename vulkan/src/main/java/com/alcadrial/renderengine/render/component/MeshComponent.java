package com.alcadrial.renderengine.render.component;

import java.util.concurrent.CompletableFuture;

import org.eclipse.collections.api.list.MutableList;
import org.jspecify.annotations.Nullable;

import com.alcadrial.renderengine.core.component.AbstractComponent;
import com.alcadrial.renderengine.core.component.ComponentType;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.resources.Mesh;
import com.alcadrial.renderengine.render.resources.Mesh.Material;
import com.alcadrial.renderengine.render.texture.Atlas;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public class MeshComponent extends AbstractComponent<MeshComponent.Params, MeshComponent.Properties, VulkanException> {
	
	public static ComponentType<MeshComponent> TYPE = ComponentType.of(MeshComponent.class);
	
	public record Params(
		@Nullable VulkanBuffer vertexBuffer, int vertexStride, int vertexOffset,
		@Nullable VulkanBuffer textureUvBuffer, int textureUvStride, int textureUvOffset,
		@Nullable VulkanBuffer normalBuffer, int normalStride, int normalOffset,
		@Nullable VulkanBuffer indexBuffer
	) {}
	
	public record Properties(
		int vertexCount,
		boolean useTextureUv,
		boolean useNormals,
		int vertexesSize,
		int indicesSize,
		int textureUvSize,
		int normalsSize
	) {}
	
	public static MeshComponent ofMesh(Mesh mesh)
	{
		return new MeshComponent(mesh);
	}
	
	private Mesh mesh;
	
	public MeshComponent(Mesh mesh)
	{
		this.mesh = mesh;
	}
	
	@Override
	public CompletableFuture<?> load()
	{
		return mesh.load().whenComplete((_, e) -> isLoaded = e == null);
	}
	
	@Override
	public CompletableFuture<?> reload()
	{
		return mesh.reload().thenRun(() -> reloadCounter++);
	}
	
	@Override
	public void reinit(Params params) throws VulkanException
	{
		mesh.flush(params.vertexBuffer, params.vertexStride, params.vertexOffset, params.textureUvBuffer, params.textureUvStride, params.textureUvOffset, params.normalBuffer, params.normalStride, params.normalOffset, params.indexBuffer);
	}
	
	public void initMaterials(Atlas atlas) throws VulkanException
	{
		mesh.flushMaterials(atlas);
	}
	
	@Override
	public Properties getProperties()
	{
		Mesh.Properties properties = mesh.getProperties();
		return new Properties(properties.vertexCount(), properties.useTextureUv(), properties.useNormals(), properties.vertexesSize(), properties.indicesSize(), properties.textureUvSize(), properties.normalsSize());
	}
	
	public MutableList<Material> getMaterials()
	{
		return mesh.getMaterials();
	}
	
	public int getTextureUvLength()
	{
		return mesh.getTextureUvLength();
	}
	
	@Override
	public void close()
	{
		clear();
	}
	
	private void clear()
	{
		
	}
	
	@Override
	public String toString()
	{
		return mesh.toString();
	}
}