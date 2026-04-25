package com.alcadrial.renderengine.render.test.widgets;

import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;

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
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.FrameBuffer;
import com.alcadrial.renderengine.render.Renderer;
import com.alcadrial.renderengine.render.Surface.SurfaceData;
import com.alcadrial.renderengine.render.buffer.StaticBigBuffer;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.commandbuffer.CommandBuffer;
import com.alcadrial.renderengine.render.pipeline.GraphicsPipeline;
import com.alcadrial.renderengine.render.pipeline.GraphicsPipelineTemplate;
import com.alcadrial.renderengine.render.pipeline.PipelineLayout;
import com.alcadrial.renderengine.render.renderpass.RenderPass;
import com.alcadrial.renderengine.render.renderpass.RenderPassTemplate;
import com.alcadrial.renderengine.render.shader.ShaderInputType;
import com.alcadrial.renderengine.render.shader.ShaderProgram;
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.shader.binding.BindingInfo;
import com.alcadrial.renderengine.render.shader.descriptor.DescriptorPool;
import com.alcadrial.renderengine.render.shader.descriptor.DescriptorSet;
import com.alcadrial.renderengine.render.shader.descriptor.DescriptorSetLayout;
import com.alcadrial.renderengine.render.texture.Atlas;
import com.alcadrial.renderengine.render.texture.TextureSampler;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.window.ActionResult;
import com.alcadrial.renderengine.render.window.EventRegistry;
import com.alcadrial.renderengine.render.window.Screen;
import com.alcadrial.renderengine.render.window.VulkanWindow;
import com.alcadrial.renderengine.render.window.Widget;

public class BasicScreen implements Screen {
	
	private static final int NEW = 0x00000000;
	private static final int INIT = 0x00000001;
	
	private static final int DIRTY_MASK = 0x0000FF00;
	private static final int WIDGET_STATE_DIRTY = 0x00000100;
	private static final int SURFACE_SIZE_DIRTY = 0x00000400;
	private static final int SURFACE_FORMAT_DIRTY = 0x00000800;
	private static final int SURFACE_DIRTY = 0x00000C00;
	private static final int SHADER_DIRTY = 0x00001000;
	
	private List<Widget> widgets;
	
	private VulkanWindow window;
	private ShaderProgram shaderProgram;
	
	private StaticBigBuffer staticBigBuffer;
	private VulkanBuffer uniformBuffer;
	private Atlas atlas;
	
	private RenderPassTemplate renderPassTemplate;
	private RenderPass renderPass;
	private PipelineLayout pipelineLayout;
	private GraphicsPipelineTemplate graphicsPipelineTemplate;
	private GraphicsPipeline graphicsPipeline;
	
	private Renderer renderer;
	
	private int state;
	private DescriptorSet[] descriptorSets;
	private DescriptorSetLayout descriptorSetLayout;
	private DescriptorPool descriptorPool;
	
	public BasicScreen() throws VulkanException
	{
		widgets = new LinkedList<>();
		state = NEW | DIRTY_MASK;
	}
	
	private boolean checkState(int state)
	{
		return (this.state & state) != 0;
	}
	
	private void setState(int state)
	{
		this.state |= state;
	}
	
	private void removeState(int state)
	{
		this.state &= ~state;
	}
	
	public void add(Widget e)
	{
		if (checkState(INIT)) throw new ConcurrentModificationException("Cannot add widgets after init");
		widgets.add(e);
	}
	 
	@Override
	public void setWindow(VulkanWindow window)
	{
		if (this.window != window)
		{
			if (this.window != null) this.window.removeOnSwapchainRecreationCallback(this::onSwapchainRecreation);
			this.window = window;
			setState(SURFACE_DIRTY);
			window.addOnSwapchainRecreationCallback(this::onSwapchainRecreation);
		}
	}
	
	protected void onSwapchainRecreation(SurfaceData data)
	{
		setState(SURFACE_SIZE_DIRTY);
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
		return renderPass;
	}
	
	@Override
	public void init() throws VulkanException
	{
		if (!checkState(DIRTY_MASK)) return;
		
		Device device = window.getDevice();
		SurfaceData data = window.getSurface().getData();
		
		if (uniformBuffer == null) uniformBuffer = VulkanBuffer.ofUniformsBindings(device, 2, 16 * 2 * 4);
		
		if (checkState(WIDGET_STATE_DIRTY))
		{
			if (atlas == null)
			{
				atlas = new Atlas(device, VK10.VK_FORMAT_R8G8B8A8_SRGB, VK10.VK_IMAGE_USAGE_SAMPLED_BIT, VK10.VK_IMAGE_ASPECT_COLOR_BIT);
				for (Widget widget : widgets)
				{
					widget.initAtlas(atlas);
				}
				atlas.draw();
			}
			int size = 0;
			for (Widget widget : widgets)
			{
				size += widget.estimateMemoryUsage();
			}
			if (staticBigBuffer == null) staticBigBuffer = new StaticBigBuffer(device, size * 3 / 2);
			else if (staticBigBuffer.getMaxAllocableSize() < size)
			{
				staticBigBuffer.close();
				staticBigBuffer = new StaticBigBuffer(device, size * 3 / 2);
			}
			for (Widget widget : widgets)
			{
				widget.initBuffers(staticBigBuffer);
			}
		}
		
		if (checkState(SURFACE_FORMAT_DIRTY))
		{
			if (renderPass != null) renderPass.close();
			if (renderPassTemplate == null)
			{
				renderPassTemplate = window.hasDepthBuffer() ? RenderPassTemplate.initColorDepthTextureTemplate(data.format().format()) : RenderPassTemplate.initColorTextureTemplate(data.format().format());
			}
			renderPass = renderPassTemplate.createRenderPass(device);
		}
		if (checkState(SURFACE_SIZE_DIRTY) || checkState(SHADER_DIRTY))
		{
			if (graphicsPipelineTemplate == null) graphicsPipelineTemplate = GraphicsPipelineTemplate.createScreenRenderingPipeline()
				.configureVertexInputState(v -> v
					.addBinding(0, 20, VK10.VK_VERTEX_INPUT_RATE_VERTEX)
					.addAttribute(0, 0, ShaderInputType.VECTOR_FLOAT_3, 0)
					.addAttribute(1, 0, ShaderInputType.VECTOR_FLOAT_2, 12)
				)
				.build();
			if (graphicsPipeline != null) graphicsPipeline.close();
			if (pipelineLayout != null) pipelineLayout.close();
			
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				descriptorPool = new DescriptorPool(device, VkDescriptorPoolCreateInfo.malloc(stack)
					.sType(VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
					.pNext(0)
					.flags(0)
					.maxSets(1)
					.pPoolSizes(VkDescriptorPoolSize.malloc(2, stack)
						.apply(b -> b
							.type(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
							.descriptorCount(1)
						)
						.apply(b -> b
							.type(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
							.descriptorCount(1)
						)
						.rewind()
					)
				);
				descriptorSetLayout = new DescriptorSetLayout(device, VkDescriptorSetLayoutCreateInfo.malloc(stack)
					.sType(VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
					.pNext(0)
					.flags(0)
					.pBindings(VkDescriptorSetLayoutBinding.malloc(2, stack)
						.apply(b -> b
							.binding(0)
							.descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
							.descriptorCount(1)
							.stageFlags(VK10.VK_SHADER_STAGE_VERTEX_BIT)
							.pImmutableSamplers(null))
						.apply(b -> b
							.binding(1)
							.descriptorType(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
							.descriptorCount(1)
							.stageFlags(VK10.VK_SHADER_STAGE_FRAGMENT_BIT)
							.pImmutableSamplers(null))
						.rewind()
					)
				);
				pipelineLayout = new PipelineLayout(device, VkPipelineLayoutCreateInfo.calloc(stack)
					.pSetLayouts(stack.longs(descriptorSetLayout.getVk()))
				);
				graphicsPipeline = graphicsPipelineTemplate.createPipeline(device, shaderProgram, data.size(), pipelineLayout, renderPass, 0);
				
				descriptorSets = descriptorPool.allocates(descriptorSetLayout, 1);
			}
		}
		if (checkState(SURFACE_DIRTY) || checkState(SHADER_DIRTY))
		{
			renderer = Renderer.builder()
				.graphicsPipeline(graphicsPipeline)
				.descriptorSets(descriptorSets)
				.bindings(
					BindingInfo.ofUniform(VulkanBuffer.ofUniformsBindingsView(uniformBuffer, 0, 1, 16 * 2), VK10.VK_PIPELINE_STAGE_VERTEX_SHADER_BIT, List.of(
						ShaderVariable.PROJECTION,
						ShaderVariable.MODEL
					)),
					BindingInfo.ofTexture(atlas.getTexture(), new TextureSampler(device), VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT, ShaderVariable.ATLAS)
				)
				.build();
		}
		removeState(DIRTY_MASK);
		setState(INIT);
	}
	
	@Override
	public void render(Matrix4f matrix, Renderer renderer)
	{
		for (Widget widget : widgets)
		{
			if (!widget.isRendered())
			{
				widget.render(matrix, renderer);
			}
		}
	}
	
	@Override
	public void draw(Matrix4f matrix, CommandBuffer commandBuffer, float partialFrame) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			uniformBuffer.getMemory().mapMemory();
			renderer.useBufferLayoutWriter(0, 0, writer -> {
				writer.set(ShaderVariable.PROJECTION, matrix);
				writer.set(ShaderVariable.MODEL, new Matrix4f());
			});
			uniformBuffer.getMemory().unmapMemory();
			VkRenderPassBeginInfo renderPassBeginInfo = VkRenderPassBeginInfo.malloc(stack);
			renderPassBeginInfo.sType(VK10.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
			renderPassBeginInfo.pNext(0);
			renderPassBeginInfo.renderPass(renderPass.getVk());
			
			FrameBuffer frameBuffer = window.getCurrentFrameBuffer();
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
				widget.draw(matrix, commandBuffer, renderer, partialFrame);
				renderer.draw(commandBuffer, 0);
			}
			commandBuffer.endRenderPass();
		}
	}
	
	@Override
	public void clearRenderingData()
	{
		staticBigBuffer.clear();
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
	public ActionResult onClick(int button, int count, Vector2i mousePos)
	{
		return ActionResult.CONSUME;
	}
	
	@Override
	public ActionResult onPress(int button, Vector2i mousePos)
	{
		return ActionResult.CONSUME;
	}
	
	@Override
	public ActionResult onRelease(int button, Vector2i mousePos)
	{
		return ActionResult.CONSUME;
	}
	
	@Override
	public void onMouseMove(Vector2i oldPos, Vector2i newPos)
	{
		
	}
	
	@Override
	public ActionResult onDragStart(Vector2i mousePos)
	{
		return ActionResult.CONSUME;
	}
	
	@Override
	public ActionResult onDragEnd(Vector2i startPos, Vector2i endPos)
	{
		return ActionResult.CONSUME;
	}
	
	@Override
	public ActionResult onScroll(double xScroll, double yScroll, Vector2i mousePos)
	{
		return ActionResult.CONSUME;
	}
	
	@Override
	public void registerEvents(EventRegistry events)
	{}
	
	@Override
	public void close() throws VulkanException
	{
		staticBigBuffer.close();
		uniformBuffer.close();
		atlas.close();
		
		renderPassTemplate.close();
		renderPass.close();
		pipelineLayout.close();
		graphicsPipelineTemplate.close();
		graphicsPipeline.close();
		
		for (DescriptorSet descriptorSet : descriptorSets) descriptorSet.close();
		descriptorSetLayout.close();
		descriptorPool.close();
		
		renderer.close();
	}
}
