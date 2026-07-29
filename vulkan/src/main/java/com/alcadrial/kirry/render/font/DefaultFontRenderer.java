package com.alcadrial.kirry.render.font;

import java.util.List;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2ir;
import com.alcadrial.kector.vector.Vector4fr;
import com.alcadrial.kirry.render.Device;
import com.alcadrial.kirry.render.FrameBuffer;
import com.alcadrial.kirry.render.Renderer;
import com.alcadrial.kirry.render.buffer.VulkanBuffer;
import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;
import com.alcadrial.kirry.render.component.PipelineOutputTarget;
import com.alcadrial.kirry.render.component.PipelineOutputTarget.SurfaceTarget;
import com.alcadrial.kirry.render.component.PipelineOutputTarget.TextureTarget;
import com.alcadrial.kirry.render.pipeline.GraphicsPipeline;
import com.alcadrial.kirry.render.pipeline.GraphicsPipelineTemplate;
import com.alcadrial.kirry.render.pipeline.PipelineLayout;
import com.alcadrial.kirry.render.shader.ShaderInputType;
import com.alcadrial.kirry.render.shader.ShaderProgram;
import com.alcadrial.kirry.render.shader.ShaderVariable;
import com.alcadrial.kirry.render.shader.binding.BindingInfo;
import com.alcadrial.kirry.render.shader.descriptor.DescriptorPool;
import com.alcadrial.kirry.render.shader.descriptor.DescriptorSet;
import com.alcadrial.kirry.render.shader.descriptor.DescriptorSetLayout;
import com.alcadrial.kirry.render.texture.TextureSampler;
import com.alcadrial.kirry.render.util.exception.VulkanException;
import com.alcadrial.kirry.render.window.VulkanWindow;

public class DefaultFontRenderer implements FontRenderer {
	
	private DescriptorPool pool;
	private DescriptorSetLayout descriptorSetLayout;
	private DescriptorSet descriptorSet;
	private PipelineLayout pipelineLayout;
	private GraphicsPipeline graphicsPipeline;
	private Renderer renderer;
	private TextureSampler sampler;
	
	public DefaultFontRenderer(Device device, PipelineOutputTarget target, ShaderProgram shaderProgram) throws VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			pool = new DescriptorPool(device, VkDescriptorPoolCreateInfo.malloc(stack)
				.sType$Default()
				.pNext(0)
				.flags(0)
				.maxSets(1)
				.pPoolSizes(VkDescriptorPoolSize.malloc(3, stack)
					.apply(s -> s
						.type(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
						.descriptorCount(2))
					.apply(s -> s
						.type(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
						.descriptorCount(1))
					.apply(s -> s
						.type(VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER)
						.descriptorCount(1))
					.rewind())
			);
			
			descriptorSetLayout = new DescriptorSetLayout(device, VkDescriptorSetLayoutCreateInfo.malloc(stack)
				.sType$Default()
				.pNext(0)
				.flags(0)
				.pBindings(VkDescriptorSetLayoutBinding.malloc(4, stack)
					.apply(s -> s
						.binding(0)
						.descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
						.descriptorCount(1)
						.stageFlags(VK10.VK_SHADER_STAGE_GEOMETRY_BIT)
						.pImmutableSamplers(null))
					.apply(s -> s
						.binding(1)
						.descriptorType(VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER)
						.descriptorCount(1)
						.stageFlags(VK10.VK_SHADER_STAGE_GEOMETRY_BIT)
						.pImmutableSamplers(null))
					.apply(s -> s
						.binding(2)
						.descriptorType(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
						.descriptorCount(1)
						.stageFlags(VK10.VK_SHADER_STAGE_FRAGMENT_BIT)
						.pImmutableSamplers(null))
					.apply(s -> s
						.binding(3)
						.descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
						.descriptorCount(1)
						.stageFlags(VK10.VK_SHADER_STAGE_FRAGMENT_BIT)
						.pImmutableSamplers(null))
					.rewind())
			);
			
			descriptorSet = pool.allocate(descriptorSetLayout);
			
			pipelineLayout = new PipelineLayout(device, VkPipelineLayoutCreateInfo.malloc(stack)
				.sType$Default()
				.pNext(0)
				.flags(0)
				.pSetLayouts(stack.longs(descriptorSetLayout.getVk()))
				.pPushConstantRanges(null)
			);
			
			try (GraphicsPipelineTemplate template = GraphicsPipelineTemplate.createFontRenderingPipeline()
				.configureVertexInputState(s -> s
					.addAttribute(0, 0, ShaderInputType.VECTOR_FLOAT_3, 0)
					.addAttribute(1, 0, ShaderInputType.UNSIGNED_INT, 12)
					.addBinding(0, 16, VK10.VK_VERTEX_INPUT_RATE_VERTEX)
				)
				.build())
			{
				switch (target)
				{
					case SurfaceTarget t -> {
						VulkanWindow window = t.window();
						graphicsPipeline = template.createPipeline(device, shaderProgram, window, pipelineLayout, window.getRenderPass(), 0);
					}
					case TextureTarget t -> {
						FrameBuffer frameBuffer = t.frameBuffer();
						graphicsPipeline = template.createPipeline(device, shaderProgram, Vector2ir.of(frameBuffer.getWidth(), frameBuffer.getHeight()), pipelineLayout, frameBuffer.getRenderPass(), 0);
					}
				}
			}
			
			renderer = Renderer.builder()
				.graphicsPipeline(graphicsPipeline)
				.descriptorSets(descriptorSet)
				.build();
			
			sampler = TextureSampler.ofNearest(device);
		}
	}
	
	public void prepare(Font font, VulkanBuffer uniformBuffer) throws VulkanException
	{
		renderer.loadDescriptors(
			BindingInfo.ofUniform(VulkanBuffer.view(uniformBuffer, (16 + 16) * 4), VK10.VK_PIPELINE_STAGE_GEOMETRY_SHADER_BIT, List.of(
				ShaderVariable.PROJECTION,
				ShaderVariable.MODEL
			)),
			BindingInfo.ofStorage(font.getRemoteData(), VK10.VK_PIPELINE_STAGE_GEOMETRY_SHADER_BIT, List.of(
				Font.GLYPH_SIZE,
				ShaderVariable.PADDING_2,
				Font.GLYPH_BEARING,
				ShaderVariable.PADDING_2,
				Font.GLYPH_UV_START,
				Font.GLYPH_UV_END
			)),
			BindingInfo.ofTexture(font.getAtlas().getTexture(), sampler, VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT, ShaderVariable.ATLAS),
			BindingInfo.ofUniform(VulkanBuffer.alignedView(uniformBuffer, (16 + 16) * 4, 4 * 4, VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT), VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT, List.of(
				ShaderVariable.COLOR
			))
		);
		renderer.updateAllDescriptorSets();
	}
	
	@Override
	public void draw(CommandBuffer commandBuffer, int count)
	{
		renderer.draw(commandBuffer, 0, 1, count);
	}
	
	public void setFont(Font font) throws VulkanException
	{
		renderer.updateDescriptor(0, 1, font.getRemoteData());
		renderer.updateDescriptor(0, 2, font.getAtlas().getTexture());
		renderer.updateDescriptorSet(0);
	}
	
	public void setProjection(Matrix4f matrix) throws VulkanException
	{
		renderer.getBufferLayoutWriter(0, 0).set(ShaderVariable.PROJECTION, matrix);
	}
	
	public void setModel(Matrix4f matrix) throws VulkanException
	{
		renderer.getBufferLayoutWriter(0, 0).set(ShaderVariable.MODEL, matrix);
	}
	
	public void setColor(Vector4fr color) throws VulkanException
	{
		renderer.getBufferLayoutWriter(0, 3).set(ShaderVariable.COLOR, color);
	}
	
	@Override
	public void close()
	{
		
	}

	public void setDataBuffer(VulkanBuffer vertexBuffer)
	{
		renderer.setBuffer(0, vertexBuffer);
	}
}
