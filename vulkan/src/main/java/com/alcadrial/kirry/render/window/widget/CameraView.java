package com.alcadrial.kirry.render.window.widget;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2ir;
import com.alcadrial.kector.vector.Vector3ir;
import com.alcadrial.kirry.core.component.Entity;
import com.alcadrial.kirry.core.resources.ResourceManager;
import com.alcadrial.kirry.render.Renderer;
import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;
import com.alcadrial.kirry.render.component.RenderEngine;
import com.alcadrial.kirry.render.memory.MemoryManager;
import com.alcadrial.kirry.render.resources.ShaderResource;
import com.alcadrial.kirry.render.scene.Camera;
import com.alcadrial.kirry.render.scene.Controller;
import com.alcadrial.kirry.render.shader.ShaderVariable;
import com.alcadrial.kirry.render.util.VulkanErrorUtils;
import com.alcadrial.kirry.render.util.exception.ExternalException;
import com.alcadrial.kirry.render.util.exception.VulkanException;
import com.alcadrial.kirry.render.window.AbstractWidget;
import com.alcadrial.kirry.render.window.EventRegistry;
import com.alcadrial.kirry.render.window.VulkanWindow;
import com.alcadrial.kirry.render.window.event.WindowResizeEvent;

public class CameraView extends AbstractWidget {
	
	public static CameraView ofFull(VulkanWindow window, Camera camera) throws VulkanException
	{
		return ofWindowed(window, camera, Vector3ir.of(), window.getSurface().getData().size());
	}
	
	public static CameraView ofWindowed(VulkanWindow window, Camera camera, Vector3ir pos, Vector2ir size) throws VulkanException
	{
		ResourceManager resourceManager = VulkanErrorUtils.nonNull(window.getResourceManager(), "Missing resource manager");
		
		try
		{
			ShaderResource spatialMove = resourceManager.getResource(RenderEngine.SPATIAL_MOVE_COMPUTE_SHADER_PATHNAME, ShaderResource.TYPE);
			ShaderResource initCompute = resourceManager.getResource(RenderEngine.INIT_COMPUTE_SHADER_PATHNAME, ShaderResource.TYPE);
			ShaderResource transformCompute = resourceManager.getResource(RenderEngine.TRANSFORM_COMPUTE_SHADER_PATHNAME, ShaderResource.TYPE);
			return new CameraView(window, pos, size, camera, spatialMove, initCompute, transformCompute);
		}
		catch (IOException e)
		{
			throw new ExternalException(e);
		}
	}
	
	private RenderEngine renderEngine;
	
	private ShaderResource spatialMoveComputeShaderResource;
	private ShaderResource initComputeShaderResource;
	private ShaderResource transformComputeShaderResource;
	private MutableList<ShaderVariable> vertexUniformVariables;
	private MutableList<ShaderVariable> fragmentUniformVariables;
	
	private boolean windowResizeFlag;
	private boolean pausedFlag;
	
	private Collection<Entity> entities;
	
	private Camera camera;
	private @Nullable Controller controller;
	
	private CameraView(VulkanWindow window, Vector3ir pos, Vector2ir size, Camera camera, ShaderResource spatialMoveComputeShaderResource, ShaderResource initComputeShaderResource, ShaderResource transformComputeShaderResource)
	{
		super(window, pos, size);
		entities = List.of();
		vertexUniformVariables = Lists.mutable.of(ShaderVariable.PROJECTION, ShaderVariable.MODEL);
		fragmentUniformVariables = Lists.mutable.of();
		this.spatialMoveComputeShaderResource = spatialMoveComputeShaderResource;
		this.initComputeShaderResource = initComputeShaderResource;
		this.transformComputeShaderResource = transformComputeShaderResource;
		
		renderEngine = (@NonNull RenderEngine) null;
		
		this.camera = camera;
	}
	
	public void setEntities(Collection<Entity> entities)
	{
		this.entities = entities;
	}
	
	public void setPaused(boolean paused)
	{
		pausedFlag = paused;
	}
	
	public void setCamera(Camera camera)
	{
		this.camera = camera;
	}
	
	public void setController(Controller controller)
	{
		this.controller = controller;
	}
	
	public void setSpatialMoveComputeShaderResource(ShaderResource spatialMoveComputeShaderResource)
	{
		this.spatialMoveComputeShaderResource = spatialMoveComputeShaderResource;
	}
	
	public void setInitComputeShaderResource(ShaderResource initComputeShaderResource)
	{
		this.initComputeShaderResource = initComputeShaderResource;
	}
	
	public void setTransformComputeShaderResource(ShaderResource transformComputeShaderResource)
	{
		this.transformComputeShaderResource = transformComputeShaderResource;
	}
	
	public void setVertexUniformVariables(MutableList<ShaderVariable> vertexUniformVariables)
	{
		this.vertexUniformVariables = vertexUniformVariables;
	}
	
	public void setFragmentUniformVariables(MutableList<ShaderVariable> fragmentUniformVariables)
	{
		this.fragmentUniformVariables = fragmentUniformVariables;
	}
	
	@Override
	public void initBuffers(MemoryManager memoryManager, CommandBuffer commandBuffer) throws VulkanException
	{
		super.initBuffers(memoryManager, commandBuffer);
		renderEngine = new RenderEngine(window.getDevice());
		renderEngine.init(null, spatialMoveComputeShaderResource, initComputeShaderResource, transformComputeShaderResource, memoryManager, commandBuffer, new RenderEngine.SurfaceTarget(window), vertexUniformVariables, fragmentUniformVariables, entities);
		
		Matrix4f projection = new Matrix4f(camera.getProjection((float) window.getWidth() / window.getHeight(), true)).multiply(camera.getView());
		
		renderEngine.setUniforms((i, writer) -> {
			switch (i)
			{
				case 0 -> {
					writer.set(ShaderVariable.PROJECTION, projection);
					writer.set(ShaderVariable.MODEL, new Matrix4f().translateX(-pos.x()).translateY(-pos.y()));
					writer.flush();
				}
				case 1 -> {
				}
			}
		});
	}
	
	@Override
	public void update(CommandBuffer commandBuffer, float elapsed) throws VulkanException
	{
		renderEngine.update(commandBuffer, window.getFrameIndex(), elapsed);
		if (!pausedFlag)
		{
			if (controller != null) controller.update(commandBuffer, elapsed);
		}
	}
	
	@Override
	public void draw(Matrix4f matrix, CommandBuffer commandBuffer, Renderer renderer) throws VulkanException
	{
		if (!renderEngine.isInit()) return;
		
		if (windowResizeFlag)
		{
			renderEngine.reinit(commandBuffer, null, null, null, new RenderEngine.SurfaceTarget(window), null, null);
		}
		if (windowResizeFlag || camera.isProjectionDirty() || camera.isViewDirty())
		{
			Matrix4f projection = new Matrix4f(camera.getProjection((float) window.getWidth() / window.getHeight(), windowResizeFlag)).multiply(camera.getView());
			
			renderEngine.setUniforms((i, writer) -> {
				switch (i)
				{
					case 0 -> {
						writer.set(ShaderVariable.PROJECTION, projection);
						writer.flush();
					}
					case 1 -> {
						
					}
				}
			}, () -> {
				windowResizeFlag = false;
			});
		}
		renderEngine.draw(commandBuffer, window.getFrameIndex());
	}
	
	@Override
	public void registerEvents(EventRegistry registry)
	{
		registry.register((WindowResizeEvent) (_, _) -> windowResizeFlag = true);
	}
	
	public Camera getCamera()
	{
		return camera;
	}
	
	public @Nullable Controller getController()
	{
		return controller;
	}
}
