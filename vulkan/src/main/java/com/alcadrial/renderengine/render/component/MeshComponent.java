package com.alcadrial.renderengine.render.component;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.collections.api.list.MutableList;
import org.jspecify.annotations.Nullable;

import com.alcadrial.renderengine.core.component.AbstractComponent;
import com.alcadrial.renderengine.core.component.ComponentType;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.commandbuffer.CommandBuffer;
import com.alcadrial.renderengine.render.memory.MemoryManager;
import com.alcadrial.renderengine.render.resources.Mesh;
import com.alcadrial.renderengine.render.resources.Mesh.Material;
import com.alcadrial.renderengine.render.texture.Atlas;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public class MeshComponent extends AbstractComponent<MeshComponent.Params, MeshComponent.Properties, VulkanException> {
	
	public static final ComponentType<MeshComponent> TYPE = ComponentType.of(MeshComponent.class);
	
	public record VertexParams(
		@Nullable VulkanBuffer vertexBuffer, int vertexStride, int vertexOffset,
		@Nullable VulkanBuffer textureUvBuffer, int textureUvStride, int textureUvOffset,
		@Nullable VulkanBuffer normalBuffer, int normalStride, int normalOffset,
		@Nullable VulkanBuffer indexBuffer
	) {}
	
	public record MaterialParams(
		MemoryManager memoryManeger,
		CommandBuffer commandBuffer,
		Atlas atlas
	) {}
	
	public record Params(
		@Nullable VertexParams vertexParams,
		@Nullable MaterialParams materialParams
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
	private AtomicBoolean invalid;
	private boolean init;
	
	public MeshComponent(Mesh mesh)
	{
		this.mesh = mesh;
		invalid = new AtomicBoolean(false);
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
	public void init(Params params) throws VulkanException
	{
		reinit(params);
		if (!init)
		{
			mesh.addReloadListener(_ -> invalid.set(true));
			init = true;
		}
	}
	
	@Override
	public void reinit(Params params) throws VulkanException
	{
		if (params.vertexParams != null) initVertex(params.vertexParams);
		if (params.materialParams != null) initMaterial(params.materialParams);
	}
	
	public void initVertex(VertexParams params) throws VulkanException
	{
		mesh.flush(params.vertexBuffer, params.vertexStride, params.vertexOffset, params.textureUvBuffer, params.textureUvStride, params.textureUvOffset, params.normalBuffer, params.normalStride, params.normalOffset, params.indexBuffer);
	}
	
	public void initMaterial(MaterialParams params) throws VulkanException
	{
		mesh.flushMaterials(params.memoryManeger, params.commandBuffer, params.atlas);
	}
	
	@Override
	public boolean validate(@Nullable Params params) throws VulkanException
	{
		if (params == null) return invalid.get();
		if (!invalid.compareAndExchange(true, false)) return false;
		reinit(params);
		return true;
	}
	
	@Override
	public boolean listenResource()
	{
		try
		{
			mesh.registerForChanges(Mesh::reload);
			return true;
		}
		catch (IOException _)
		{}
		return false;
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
		mesh.close();
	}
	
	@Override
	public String toString()
	{
		return mesh.toString();
	}
}