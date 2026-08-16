package com.alcadrial.kirry.render.window.screen;

import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2ir;
import com.alcadrial.kirry.render.Device;
import com.alcadrial.kirry.render.FrameBuffer;
import com.alcadrial.kirry.render.Renderer;
import com.alcadrial.kirry.render.buffer.VulkanBuffer;
import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;
import com.alcadrial.kirry.render.memory.MemoryManager;
import com.alcadrial.kirry.render.pipeline.GraphicsPipeline;
import com.alcadrial.kirry.render.pipeline.GraphicsPipelineTemplate;
import com.alcadrial.kirry.render.pipeline.PipelineLayout;
import com.alcadrial.kirry.render.renderpass.RenderPass;
import com.alcadrial.kirry.render.shader.ShaderInputType;
import com.alcadrial.kirry.render.shader.ShaderProgram;
import com.alcadrial.kirry.render.shader.ShaderVariable;
import com.alcadrial.kirry.render.shader.binding.BindingInfo;
import com.alcadrial.kirry.render.shader.descriptor.DescriptorPool;
import com.alcadrial.kirry.render.shader.descriptor.DescriptorSet;
import com.alcadrial.kirry.render.shader.descriptor.DescriptorSetLayout;
import com.alcadrial.kirry.render.texture.Atlas;
import com.alcadrial.kirry.render.texture.TextureManager;
import com.alcadrial.kirry.render.texture.TextureSampler;
import com.alcadrial.kirry.render.util.VLazy;
import com.alcadrial.kirry.render.util.VulkanErrorUtils;
import com.alcadrial.kirry.render.util.exception.VulkanException;
import com.alcadrial.kirry.render.window.ActionResult;
import com.alcadrial.kirry.render.window.EventRegistry;
import com.alcadrial.kirry.render.window.FrameScreen;
import com.alcadrial.kirry.render.window.Widget;

public class WidgetTextureScreen implements FrameScreen {
	
	protected static final int NEW = 0x00000000;
	protected static final int INIT = 0x00000001;
	
	protected static final int DIRTY_MASK = 0x0000FF00;
	protected static final int WIDGET_STATE_DIRTY = 0x00000100;
	protected static final int SURFACE_SIZE_DIRTY = 0x00000400;
	protected static final int SURFACE_FORMAT_DIRTY = 0x00000800;
	protected static final int SURFACE_DIRTY = 0x00000C00;
	protected static final int SHADER_DIRTY = 0x00001000;
	
	protected List<Widget> widgets;
	
	protected @Nullable ShaderProgram shaderProgram;
	
	protected @Nullable VulkanBuffer uniformBuffer;
	protected TextureManager textureManager;
	
	protected VLazy<FrameBuffer> lazyFrameBuffer;
	protected @Nullable PipelineLayout pipelineLayout;
	protected @Nullable GraphicsPipelineTemplate graphicsPipelineTemplate;
	protected @Nullable GraphicsPipeline graphicsPipeline;
	
	protected @Nullable Renderer renderer;
	
	protected int state;
	protected DescriptorSet @Nullable [] descriptorSets;
	protected @Nullable DescriptorSetLayout descriptorSetLayout;
	protected @Nullable DescriptorPool descriptorPool;
	
	public WidgetTextureScreen(Device device)
	{
		widgets = new LinkedList<>();
		state = NEW | DIRTY_MASK;
		textureManager = TextureManager.getDefault(() -> new Atlas(device, VK10.VK_FORMAT_R8G8B8A8_SRGB, VK10.VK_IMAGE_USAGE_SAMPLED_BIT, VK10.VK_IMAGE_ASPECT_COLOR_BIT, 1));
		
		lazyFrameBuffer = new VLazy<>();
	}
	
	protected boolean checkState(int state)
	{
		return (this.state & state) != 0;
	}
	
	protected void setState(int state)
	{
		this.state |= state;
	}
	
	protected void removeState(int state)
	{
		this.state &= ~state;
	}
	
	public void add(Widget e)
	{
		if (checkState(INIT)) throw new ConcurrentModificationException("Cannot add widgets after init and before clear");
		widgets.add(e);
	}
	
	@Override
	public void setOutput(FrameBuffer frameBuffer)
	{
		if (lazyFrameBuffer.peek() != frameBuffer)
		{
			lazyFrameBuffer.set(frameBuffer);
			setState(SURFACE_DIRTY);
		}
	}
	
	@Override
	public FrameBuffer getOutput()
	{
		return lazyFrameBuffer.get();
	}
	
	public void setShaderProgram(ShaderProgram shaderProgram)
	{
		if (this.shaderProgram != shaderProgram)
		{
			this.shaderProgram = shaderProgram;
			setState(SHADER_DIRTY);
		}
	}
	
	@Override
	public RenderPass getRenderPass()
	{
		return lazyFrameBuffer.get().getRenderPass();
	}
	
	@Override
	public void init(MemoryManager memoryManager, CommandBuffer commandBuffer) throws VulkanException
	{
		if (!checkState(DIRTY_MASK) || widgets.isEmpty()) return;
		ShaderProgram shaderProgram = VulkanErrorUtils.nonNull(this.shaderProgram, "Missing shaderProgram");
		
		FrameBuffer frameBuffer = lazyFrameBuffer.get();
		Device device = frameBuffer.getDevice();
		
		if (uniformBuffer == null) uniformBuffer = VulkanBuffer.ofUniformsBindings(device, 2, 16 * 2 * 4);
		
		int lastTextureCount = textureManager.getAtlasSize() + textureManager.getTextureSize();
		if (checkState(WIDGET_STATE_DIRTY))
		{
			for (Widget widget : widgets)
			{
				widget.initTextures(memoryManager, commandBuffer, textureManager);
			}
			for (Atlas atlas : textureManager.getAtlases()) atlas.draw(commandBuffer);
			for (Widget widget : widgets)
			{
				widget.initBuffers(memoryManager, commandBuffer);
			}
		}
		int textureCount = textureManager.getAtlasSize() + textureManager.getTextureSize();
		int framesCount = Math.max(1, textureCount);
		
		if (checkState(SURFACE_SIZE_DIRTY) || checkState(SHADER_DIRTY))
		{
			if (graphicsPipelineTemplate == null) graphicsPipelineTemplate = createGraphicsPipelineTemplate();
			if (graphicsPipeline != null) graphicsPipeline.close();
			
			if (lastTextureCount != textureCount || descriptorPool == null)
			{
				if (descriptorSets != null) for (DescriptorSet descriptorSet : descriptorSets) descriptorSet.close();
				if (descriptorSetLayout != null) descriptorSetLayout.close();
				if (descriptorPool != null) descriptorPool.close();
				try (MemoryStack stack = MemoryStack.stackPush())
				{
					List<DescriptorSetDef> descriptors = getDescriptorSetDefs();
					
					VkDescriptorPoolSize.Buffer pPoolSizes = VkDescriptorPoolSize.malloc(descriptors.size(), stack);
					for (DescriptorSetDef d : descriptors) pPoolSizes.apply(s -> s.set(d.type, d.count * framesCount));
					
					descriptorPool = new DescriptorPool(device, VkDescriptorPoolCreateInfo.malloc(stack)
						.sType(VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
						.pNext(0)
						.flags(0)
						.maxSets(framesCount)
						.pPoolSizes(pPoolSizes.rewind())
					);
					VkDescriptorSetLayoutBinding.Buffer pBindings = VkDescriptorSetLayoutBinding.malloc(descriptors.size(), stack);
					for (int i = 0; i < descriptors.size(); i++)
					{
						DescriptorSetDef d = descriptors.get(i);
						pBindings.get().set(i, d.type, d.count, d.stage, null);
					}
					
					descriptorSetLayout = new DescriptorSetLayout(device, VkDescriptorSetLayoutCreateInfo.malloc(stack)
						.sType(VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
						.pNext(0)
						.flags(0)
						.pBindings(pBindings.rewind())
					);
					if (pipelineLayout != null) pipelineLayout.close();
					pipelineLayout = new PipelineLayout(device, VkPipelineLayoutCreateInfo.calloc(stack).pSetLayouts(stack.longs(((@NonNull DescriptorSetLayout) descriptorSetLayout).getVk())));
					descriptorSets = ((@NonNull DescriptorPool) descriptorPool).allocates((@NonNull DescriptorSetLayout) descriptorSetLayout, framesCount);
				}
			}
			graphicsPipeline = ((@NonNull GraphicsPipelineTemplate) graphicsPipelineTemplate).createPipeline(device, shaderProgram, Vector2ir.of(frameBuffer.getWidth(), frameBuffer.getHeight()), (@NonNull PipelineLayout) pipelineLayout, frameBuffer.getRenderPass(), 0);
		}
		if (checkState(SURFACE_DIRTY) || checkState(SHADER_DIRTY) || lastTextureCount != textureCount || renderer == null)
		{
			renderer = Renderer.builder()
				.graphicsPipeline((@NonNull GraphicsPipeline) graphicsPipeline)
				.descriptorSets((DescriptorSet @NonNull []) descriptorSets)
				.buffersCount(framesCount)
				.bindings(i -> getBindingInfo(i, textureCount > 0))
				.build();
			renderer.updateAllDescriptorSets();
		}
		removeState(DIRTY_MASK);
		setState(INIT);
	}
	
	protected GraphicsPipelineTemplate createGraphicsPipelineTemplate()
	{
		return GraphicsPipelineTemplate.createScreenRenderingPipeline()
			.configureVertexInputState(v -> v
				.addBinding(0, 20, VK10.VK_VERTEX_INPUT_RATE_VERTEX)
				.addAttribute(0, 0, ShaderInputType.VECTOR_FLOAT_3, 0)
				.addAttribute(1, 0, ShaderInputType.VECTOR_FLOAT_2, 12)
			)
			.build();
	}
	
	protected record DescriptorSetDef(int type, int count, int stage) {}
	
	protected List<DescriptorSetDef> getDescriptorSetDefs()
	{
		return List.of(
			new DescriptorSetDef(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 1, VK10.VK_SHADER_STAGE_VERTEX_BIT),
			new DescriptorSetDef(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, 1, VK10.VK_SHADER_STAGE_FRAGMENT_BIT)
		);
	}
	
	public BindingInfo[] getBindingInfo(int frameIndex, boolean useTextures) throws VulkanException
	{
		if (useTextures)
		{
			return new BindingInfo[] {
				BindingInfo.ofUniform(VulkanBuffer.ofUniformsBindingsView((@NonNull VulkanBuffer) uniformBuffer, 0, 1, 64 * 2), VK10.VK_PIPELINE_STAGE_VERTEX_SHADER_BIT, List.of(
					ShaderVariable.PROJECTION,
					ShaderVariable.MODEL
				)),
				BindingInfo.ofTexture(frameIndex < textureManager.getAtlasSize() ? textureManager.getAtlas(frameIndex).getTexture() : textureManager.getTexture(frameIndex - textureManager.getAtlasSize()), TextureSampler.ofNearest(lazyFrameBuffer.get().getDevice()), VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT, ShaderVariable.ATLAS)
			};
		}
		return new BindingInfo[] {
			BindingInfo.ofUniform(VulkanBuffer.ofUniformsBindingsView((@NonNull VulkanBuffer) uniformBuffer, 0, 1, 64 * 2), VK10.VK_PIPELINE_STAGE_VERTEX_SHADER_BIT, List.of(
				ShaderVariable.PROJECTION,
				ShaderVariable.MODEL
			))
		};
	}
	
	@Override
	public void render(Matrix4f matrix, CommandBuffer commandBuffer) throws VulkanException
	{
		Renderer renderer = VulkanErrorUtils.nonNull(this.renderer, "Not initialized");
		for (Widget widget : widgets)
		{
			if (!widget.isRendered())
			{
				widget.render(matrix, commandBuffer, renderer);
			}
		}
	}
	
	@Override
	public void update(CommandBuffer commandBuffer, float elapsed) throws VulkanException
	{
		for (Widget widget : widgets) widget.update(commandBuffer, elapsed);
	}
	
	@Override
	public void draw(Matrix4f matrix, CommandBuffer commandBuffer) throws VulkanException
	{
		Renderer renderer = VulkanErrorUtils.nonNull(this.renderer, "Not initialized");
		FrameBuffer frameBuffer = lazyFrameBuffer.get();
		if (widgets.isEmpty()) return;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			renderer.useBufferLayoutWriter(0, 0, writer -> {
				writer.set(ShaderVariable.PROJECTION, matrix);
				writer.set(ShaderVariable.MODEL, new Matrix4f());
			});
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
			for (Widget widget : widgets)
			{
				widget.draw(matrix, commandBuffer, renderer, textureManager);
			}
			commandBuffer.endRenderPass();
		}
	}
	
	@Override
	public void clear()
	{
		removeState(~NEW);
		setState(DIRTY_MASK);
	}
	
	@Override
	public boolean isOnSurface()
	{
		return false;
	}
	
	@Override
	public boolean isRendered()
	{
		return false;
	}
	
	@Override
	public boolean isInitialized()
	{
		return checkState(INIT);
	}
	
	@Override
	public void requestRender()
	{
		
	}
	
	@Override
	public void requestDraw()
	{
		
	}
	
	@Override
	public boolean isDragging()
	{
		return false;
	}
	
	@Override
	public ActionResult onClick(int button, int count, Vector2ir mousePos)
	{
		return ActionResult.CONSUME;
	}
	
	@Override
	public ActionResult onPress(int button, Vector2ir mousePos)
	{
		return ActionResult.CONSUME;
	}
	
	@Override
	public ActionResult onRelease(int button, Vector2ir mousePos)
	{
		return ActionResult.CONSUME;
	}
	
	@Override
	public void onMouseMove(Vector2ir oldPos, Vector2ir newPos)
	{
		
	}
	
	@Override
	public ActionResult onDragStart(Vector2ir mousePos)
	{
		return ActionResult.CONSUME;
	}
	
	@Override
	public ActionResult onDragEnd(Vector2ir startPos, Vector2ir endPos)
	{
		return ActionResult.CONSUME;
	}
	
	@Override
	public ActionResult onScroll(double xScroll, double yScroll, Vector2ir mousePos)
	{
		return ActionResult.CONSUME;
	}
	
	@Override
	public void registerEvents(EventRegistry events)
	{
		for (Widget widget : widgets) widget.registerEvents(events);
	}
	
	@Override
	public void close() throws VulkanException
	{
		try
		{
			for (Widget widget : widgets) widget.close();
		}
		catch (NullPointerException _)
		{}
		if (uniformBuffer != null) uniformBuffer.close();
		textureManager.close();
		
		if (pipelineLayout != null) pipelineLayout.close();
		if (graphicsPipelineTemplate != null) graphicsPipelineTemplate.close();
		if (graphicsPipeline != null) graphicsPipeline.close();
		
		if (descriptorSets != null) for (DescriptorSet descriptorSet : descriptorSets) descriptorSet.close();
		if (descriptorSetLayout != null) descriptorSetLayout.close();
		if (descriptorPool != null) descriptorPool.close();
	}
}
