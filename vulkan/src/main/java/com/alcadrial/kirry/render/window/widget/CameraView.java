package com.alcadrial.kirry.render.window.widget;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2ir;
import com.alcadrial.kector.vector.Vector3ir;
import com.alcadrial.kirry.core.component.Entity;
import com.alcadrial.kirry.core.resources.ResourceManager;
import com.alcadrial.kirry.render.Device;
import com.alcadrial.kirry.render.FrameBuffer;
import com.alcadrial.kirry.render.Renderer;
import com.alcadrial.kirry.render.RenderingDataUtils;
import com.alcadrial.kirry.render.Surface.SurfaceData;
import com.alcadrial.kirry.render.buffer.BufferLayoutWriter;
import com.alcadrial.kirry.render.buffer.VulkanBuffer;
import com.alcadrial.kirry.render.buffer.writer.DeferredBufferWriter;
import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;
import com.alcadrial.kirry.render.component.RenderEngine;
import com.alcadrial.kirry.render.memory.MemoryManager;
import com.alcadrial.kirry.render.memory.MemoryTransferState;
import com.alcadrial.kirry.render.renderpass.RenderPass;
import com.alcadrial.kirry.render.renderpass.RenderPassTemplate;
import com.alcadrial.kirry.render.resources.ShaderResource;
import com.alcadrial.kirry.render.scene.Camera;
import com.alcadrial.kirry.render.scene.Controller;
import com.alcadrial.kirry.render.shader.ShaderType;
import com.alcadrial.kirry.render.shader.ShaderVariable;
import com.alcadrial.kirry.render.texture.Texture;
import com.alcadrial.kirry.render.texture.TextureManager;
import com.alcadrial.kirry.render.util.VulkanErrorUtils;
import com.alcadrial.kirry.render.util.exception.ExternalException;
import com.alcadrial.kirry.render.util.exception.VulkanException;
import com.alcadrial.kirry.render.window.AbstractWidget;
import com.alcadrial.kirry.render.window.EventRegistry;
import com.alcadrial.kirry.render.window.VulkanWindow;
import com.alcadrial.kirry.render.window.event.WindowResizeEvent;
import com.alcadrial.kirry.render.window.widget.CameraView.UniformState.Reserved;

public class CameraView extends AbstractWidget {
	
	public static CameraView ofFull(VulkanWindow window, Camera camera) throws VulkanException
	{
		return ofWindowed(window, camera, Vector3ir.of(), window.getSurface().getData().size(), UniformState.EMPTY);
	}
	
	public static CameraView ofFull(VulkanWindow window, Camera camera, UniformState uniformState) throws VulkanException
	{
		return ofWindowed(window, camera, Vector3ir.of(), window.getSurface().getData().size(), uniformState);
	}
	
	public static CameraView ofWindowed(VulkanWindow window, Camera camera, Vector3ir pos, Vector2ir size) throws VulkanException
	{
		return ofWindowed(window, camera, pos, size, UniformState.EMPTY);
	}
	
	public static CameraView ofWindowed(VulkanWindow window, Camera camera, Vector3ir pos, Vector2ir size, UniformState uniformState) throws VulkanException
	{
		ResourceManager resourceManager = VulkanErrorUtils.nonNull(window.getResourceManager(), "Missing resource manager");
		
		try
		{
			ShaderResource spatialMove = resourceManager.getResource(RenderEngine.SPATIAL_MOVE_COMPUTE_SHADER_PATHNAME, ShaderResource.TYPE);
			ShaderResource initCompute = resourceManager.getResource(RenderEngine.INIT_COMPUTE_SHADER_PATHNAME, ShaderResource.TYPE);
			ShaderResource transformCompute = resourceManager.getResource(RenderEngine.TRANSFORM_COMPUTE_SHADER_PATHNAME, ShaderResource.TYPE);
			return new CameraView(window, pos, size, camera, spatialMove, initCompute, transformCompute, uniformState);
		}
		catch (IOException e)
		{
			throw new ExternalException(e);
		}
	}
	
	public static abstract class UniformState {
		
		private static final UniformState EMPTY = new UniformState() {};
		
		protected class Reserved {
			
			private FastList<List<Var<?>>> vars;
			private boolean isDirty;
			
			public Reserved()
			{
				vars = new FastList<>(UniformState.this.vars.size());
				for (FastList<List<Var<?>>> l : UniformState.this.vars)
				{
					MutableList<Var<?>> w = Lists.mutable.withInitialCapacity((int) l.sumOfInt(List::size));
					for (List<Var<?>> v : l)
					{
						for (Var<?> var : v) if (var.isDirty)
						{
							w.add(var.clone());
							var.isDirty = false;
							isDirty = true;
						}
					}
					vars.add(w);
				}
			}
			
			public boolean update(int i, BufferLayoutWriter writer) throws VulkanException
			{
				if (vars.size() <= i) return false;
				boolean updated = false;
				for (Var<?> v : vars.get(i)) updated |= update(v, writer);
				return updated;
			}
			
			private <T> boolean update(Var<T> v, BufferLayoutWriter writer) throws VulkanException
			{
				if (v.isDirty)
				{
					v.update.update(writer, v.v, v.t);
					return true;
				}
				return false;
			}
		}
		
		protected class Var<T> {
			
			@FunctionalInterface
			public static interface Update<T> {
				
				void update(BufferLayoutWriter w, ShaderVariable v, T t) throws VulkanException;
			}
			
			private ShaderVariable v;
			private T t;
			private boolean isDirty;
			private Update<T> update;
			
			public Var(ShaderVariable v, T t, Update<T> update)
			{
				this.v = v;
				this.t = t;
				this.update = update;
				isDirty = t != null;
			}
			
			public void set(T t)
			{
				if (isDirty = this.t != t) this.t = t;
			}
			
			public T get()
			{
				return t;
			}
			
			public boolean isDirty()
			{
				return isDirty;
			}
			
			public void markDirty()
			{
				isDirty = true;
			}
			
			@Override
			protected Var<T> clone()
			{
				return new Var<>(v, t, update);
			}
		}
		
		private FastList<FastList<List<Var<?>>>> vars;
		
		public UniformState()
		{
			vars = FastList.newList();
		}
		
		protected void add(Var<?> v, ShaderType type, int binding)
		{
			if (!type.isKnown()) return;
			ensureCapacity(type, binding);
			vars.get(binding).get(type.ordinal()).add(v);
		}
		
		public MutableList<ShaderVariable> getVertexUniformVariables()
		{
			return vars.asLazy().flatCollect(w -> w.size() <= ShaderType.VERTEX.ordinal() ? List.of() : w.get(ShaderType.VERTEX.ordinal())).collect(v -> v.v).concatenate(Lists.immutable.of(ShaderVariable.PROJECTION, ShaderVariable.MODEL)).toList();
		}
		
		public MutableList<ShaderVariable> getFragmentUniformVariables()
		{
			return vars.asLazy().flatCollect(w -> w.size() <= ShaderType.FRAGMENT.ordinal() ? List.of() : w.get(ShaderType.FRAGMENT.ordinal())).collect(v -> v.v).toList();
		}
		
		public MutableList<ShaderVariable> getStageUniformVariables(ShaderType type)
		{
			if (type == ShaderType.VERTEX) return getVertexUniformVariables();
			return vars.asLazy().flatCollect(w -> w.size() <= type.ordinal() ? List.of() : w.get(type.ordinal())).collect(v -> v.v).toList();
		}
		
		private void ensureCapacity(ShaderType type, int i)
		{
			FastList<FastList<List<Var<?>>>> l = vars;
			if (l.size() <= i)
			{
				l.ensureCapacity(i + 1);
				while (l.size() <= i)
				{
					l.add(FastList.newList(ShaderType.knownTypes().length));
				}
			}
			FastList<List<Var<?>>> ll = l.get(i);
			while (ll.size() <= type.ordinal())
			{
				ll.add(Lists.mutable.of());
			}
		}
		
		protected boolean isDirty()
		{
			for (FastList<List<Var<?>>> l : vars) for (List<Var<?>> v : l) for (Var<?> w : v) if (w.isDirty) return true;
			return false;
		}
		
		public Reserved reserveUpdate()
		{
			return new Reserved();
		}
	}
	
	private RenderEngine renderEngine;
	
	private ShaderResource spatialMoveComputeShaderResource;
	private ShaderResource initComputeShaderResource;
	private ShaderResource transformComputeShaderResource;
	
	private boolean windowResizeFlag;
	private boolean newCameraFlag;
	private boolean pausedFlag;
	private UniformState uniformState;
	
	private Texture sceneColorTexture;
	private Texture sceneDepthTexture;
	private FrameBuffer frameBuffer;
	private VulkanBuffer vertexBuffer;
	private VulkanBuffer indexBuffer;
	private int textureIndex;
	
	private Collection<Entity> entities;
	
	private Camera camera;
	private @Nullable Controller controller;
	
	private CameraView(VulkanWindow window, Vector3ir pos, Vector2ir size, Camera camera, ShaderResource spatialMoveComputeShaderResource, ShaderResource initComputeShaderResource, ShaderResource transformComputeShaderResource, UniformState uniformState)
	{
		super(window, pos, size);
		entities = List.of();
		this.uniformState = uniformState;
		this.spatialMoveComputeShaderResource = spatialMoveComputeShaderResource;
		this.initComputeShaderResource = initComputeShaderResource;
		this.transformComputeShaderResource = transformComputeShaderResource;
		
		renderEngine = (@NonNull RenderEngine) null;
		sceneColorTexture = (@NonNull Texture) null;
		sceneDepthTexture = (@NonNull Texture) null;
		frameBuffer = (@NonNull FrameBuffer) null;
		vertexBuffer = (@NonNull VulkanBuffer) null;
		indexBuffer = (@NonNull VulkanBuffer) null;
		
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
		newCameraFlag = true;
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
	
	public void setUniformState(UniformState uniformState)
	{
		this.uniformState = uniformState;
	}
	
	@Override
	public void initTextures(MemoryManager memoryManager, CommandBuffer commandBuffer, TextureManager textureManager) throws VulkanException
	{
		if (entities.isEmpty()) return;
		Device device = window.getDevice();
		int width = window.getWidth();
		int height = window.getHeight();
		SurfaceData data = window.getSurface().getData();
		sceneColorTexture = Texture.create2dTexture(device, width, height, data.format().format(), VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK10.VK_IMAGE_USAGE_SAMPLED_BIT, VK10.VK_IMAGE_ASPECT_COLOR_BIT, 1, 1);
		sceneDepthTexture = Texture.create2dTexture(device, width, height, VK10.VK_FORMAT_D24_UNORM_S8_UINT, VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT, VK10.VK_IMAGE_ASPECT_DEPTH_BIT, 1, 1);
		
		textureIndex = textureManager.addTexture(sceneColorTexture);
	}
	
	@Override
	public void initBuffers(MemoryManager memoryManager, CommandBuffer commandBuffer) throws VulkanException
	{
		windowResizeFlag = false;
		newCameraFlag = false;
		if (entities.isEmpty()) return;
		Reserved r = uniformState.reserveUpdate();
		super.initBuffers(memoryManager, commandBuffer);
		
		Device device = window.getDevice();
		
		RenderPass renderPass;
		try (RenderPassTemplate renderPassTemplate = RenderPassTemplate.transformColorDepthTextureTemplate(sceneColorTexture.getFormat(), 1))
		{
			renderPass = renderPassTemplate.createRenderPass(device);
		}
		frameBuffer = FrameBuffer.ofDepth(sceneColorTexture, sceneDepthTexture, renderPass);
		
		renderEngine = new RenderEngine(device);
		renderEngine.init(null, spatialMoveComputeShaderResource, initComputeShaderResource, transformComputeShaderResource, memoryManager, commandBuffer, new RenderEngine.TextureTarget(frameBuffer), uniformState.getVertexUniformVariables(), uniformState.getFragmentUniformVariables().toList(), entities);
		
		Matrix4f projection = new Matrix4f(camera.getProjection((float) window.getWidth() / window.getHeight(), true)).multiply(camera.getView());
		
		renderEngine.setUniforms((i, writer) -> {
			switch (i)
			{
				case 0 -> {
					writer.set(ShaderVariable.PROJECTION, projection);
					writer.set(ShaderVariable.MODEL, new Matrix4f().translateX(-pos.x()).translateY(-pos.y()));
					r.update(i, writer);
					writer.flush();
				}
				default -> {
					if (r.update(i, writer)) writer.flush();
				}
			}
		});
		
		vertexBuffer = memoryManager.allocate((5 * 4) * 4, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT);
		indexBuffer = memoryManager.allocate(6 * 4, VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT | VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT);
		
		try (MemoryTransferState transfer = memoryManager.allocateForTransfer(commandBuffer, vertexBuffer, indexBuffer))
		{
			DeferredBufferWriter localVertexWriter = transfer.getWriter(vertexBuffer);
			DeferredBufferWriter localIndexWriter = transfer.getWriter(indexBuffer);
			
			RenderingDataUtils.loadRectangleUv(localVertexWriter, localIndexWriter, 0, pos.x(), pos.y(), pos.x() + size.x(), pos.y() + size.y(), 1, 0, 0, 1, 1);
		}
	}
	
	@Override
	public void update(CommandBuffer commandBuffer, float elapsed) throws VulkanException
	{
		if (entities.isEmpty() || !renderEngine.isInit()) return;
		renderEngine.update(commandBuffer, window.getFrameIndex(), elapsed);
		if (!pausedFlag && isFocused())
		{
			Controller c = controller;
			if (c != null && c.isActive()) c.update(commandBuffer, elapsed);
		}
	}
	
	@Override
	public void render(Matrix4f matrix, CommandBuffer commandBuffer, Renderer renderer) throws VulkanException
	{
		if (entities.isEmpty() || !renderEngine.isInit()) return;
		
		if (windowResizeFlag)
		{
			renderEngine.reinit(commandBuffer, null, null, null, new RenderEngine.SurfaceTarget(window), null, null);
		}
		boolean flag0 = windowResizeFlag || newCameraFlag || camera.isProjectionDirty() || camera.isViewDirty();
		Reserved r = uniformState.reserveUpdate();
		if (flag0 || r.isDirty)
		{
			Matrix4f projection = flag0 ? new Matrix4f(camera.getProjection((float) window.getWidth() / window.getHeight(), windowResizeFlag)).multiply(camera.getView()) : null;
			
			windowResizeFlag = false;
			newCameraFlag = false;
			
			renderEngine.setUniforms((i, writer) -> {
				switch (i)
				{
					case 0 -> {
						if (flag0) writer.set(ShaderVariable.PROJECTION, (@NonNull Matrix4f) projection);
						if (r.update(i, writer) || flag0) writer.flush();
					}
					default -> {
						if (r.update(i, writer)) writer.flush();
					}
				}
			});
		}
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkRenderPassBeginInfo renderPassBeginInfo = VkRenderPassBeginInfo.malloc(stack);
			renderPassBeginInfo.sType(VK10.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
			renderPassBeginInfo.pNext(0);
			renderPassBeginInfo.renderPass(frameBuffer.getRenderPass().getVk());
			renderPassBeginInfo.framebuffer(frameBuffer.getVk());
			
			VkRect2D renderArea = VkRect2D.calloc(stack);
			renderArea.extent(VkExtent2D.malloc(stack).set(frameBuffer.getWidth(), frameBuffer.getHeight()));
			renderPassBeginInfo.renderArea(renderArea);
			
			VkClearValue.Buffer clearValues = VkClearValue.calloc(2, stack);
			clearValues.get().color().float32(stack.floats(0, 0, 0, 0));
			clearValues.get().depthStencil().depth(1).stencil(0);
			renderPassBeginInfo.pClearValues(clearValues.rewind());
			
			commandBuffer.beginRenderPass(renderPassBeginInfo, VK10.VK_SUBPASS_CONTENTS_INLINE);
			renderEngine.draw(commandBuffer, 0);
			commandBuffer.endRenderPass();
		}
		//super.render(matrix, commandBuffer, renderer);
	}
	
	@Override
	public void draw(Matrix4f matrix, CommandBuffer commandBuffer, Renderer renderer, TextureManager textureManager) throws VulkanException
	{
		if (entities.isEmpty() || !renderEngine.isInit()) return;
		renderer.setBuffer(0, vertexBuffer);
		renderer.setIndexBuffer(indexBuffer);
		renderer.draw(commandBuffer, textureManager.textureIndexToGlobal(textureIndex));
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
	
	@Override
	public void close() throws VulkanException
	{
		renderEngine.close();
		frameBuffer.close();
		frameBuffer.getRenderPass().close();
		sceneDepthTexture.close();
		vertexBuffer.close();
		indexBuffer.close();
	}
}
