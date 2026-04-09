package com.alcadrial.renderengine.render.pipeline;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.eclipse.collections.api.factory.Lists;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDynamicStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineTessellationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import org.lwjgl.vulkan.VkViewport;

import com.alcadrial.kector.shape.OrthoShape;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.kector.vector.Vector4f;
import com.alcadrial.kector.vector.Vector4i;
import com.alcadrial.renderengine.core.util.Pair;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.Surface;
import com.alcadrial.renderengine.render.renderpass.RenderPass;
import com.alcadrial.renderengine.render.shader.PreparedShader;
import com.alcadrial.renderengine.render.shader.ShaderInputType;
import com.alcadrial.renderengine.render.shader.ShaderProgram;
import com.alcadrial.renderengine.render.shader.ShaderType;
import com.alcadrial.renderengine.render.shader.ShaderUtils;
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.shader.layout.ShaderInputLayout;
import com.alcadrial.renderengine.render.shader.layout.ShaderLayout;
import com.alcadrial.renderengine.render.shader.layout.ShaderLayout.Type;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.window.VulkanWindow;

public class GraphicsPipelineTemplate implements AutoCloseable {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public static Builder create()
	{
		return new Builder();
	}
	
	public static Builder create3dRenderingPipeline()
	{
		return create()
			.configureVertexInputState(_ -> {})
			.configureInputAssemblyState(s -> s
				.topology(VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST)
			)
			.configureViewportState(s -> s
				.addDefaultViewport()
				.addFullScissor()
			)
			.configureRasterizationState(_ -> {})
			.configureMultisampleState(_ -> {})
			.configureDepthStencilState(s -> s
				.depthTestEnable(true)
			)
			.configureColorBlendState(s -> s
				.blendConstants(0, 0, 0, 0)
				.addAttachment(false, 0, 0, 0, 0, 0, 0, VK10.VK_COLOR_COMPONENT_R_BIT | VK10.VK_COLOR_COMPONENT_G_BIT | VK10.VK_COLOR_COMPONENT_B_BIT | VK10.VK_COLOR_COMPONENT_A_BIT)
			)
			.configureDynamicStateState(_ -> {});
	}
	
	public static Builder create3dOutlineRenderingPipeline()
	{
		return create()
			.configureVertexInputState(_ -> {})
			.configureInputAssemblyState(s -> s
				.topology(VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST)
			)
			.configureViewportState(s -> s
				.addDefaultViewport()
				.addFullScissor()
			)
			.configureRasterizationState(s -> s
				.cullMode(VK10.VK_CULL_MODE_BACK_BIT)
				.polygonMode(VK10.VK_POLYGON_MODE_LINE)
			)
			.configureMultisampleState(_ -> {})
			.configureDepthStencilState(s -> s
				.depthTestEnable(true)
			)
			.configureColorBlendState(s -> s
				.blendConstants(0, 0, 0, 0)
				.addAttachment(false, 0, 0, 0, 0, 0, 0, VK10.VK_COLOR_COMPONENT_R_BIT | VK10.VK_COLOR_COMPONENT_G_BIT | VK10.VK_COLOR_COMPONENT_B_BIT | VK10.VK_COLOR_COMPONENT_A_BIT)
			)
			.configureDynamicStateState(_ -> {});
	}
	
	public static Builder createScreenRenderingPipeline()
	{
		return create()
			.configureVertexInputState(_ -> {})
			.configureInputAssemblyState(s -> s
				.topology(VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST)
			)
			.configureViewportState(s -> s
				.addDefaultViewport()
				.addFullScissor()
			)
			.configureRasterizationState(s -> s
				.cullMode(VK10.VK_CULL_MODE_NONE)
			)
			.configureMultisampleState(_ -> {})
			.configureDepthStencilState(s -> s
				.depthTestEnable(true)
			)
			.configureColorBlendState(s -> s
				.blendConstants(0, 0, 0, 0)
				.addAttachment(false, 0, 0, 0, 0, 0, 0, VK10.VK_COLOR_COMPONENT_R_BIT | VK10.VK_COLOR_COMPONENT_G_BIT | VK10.VK_COLOR_COMPONENT_B_BIT | VK10.VK_COLOR_COMPONENT_A_BIT)
			)
			.configureDynamicStateState(_ -> {});
	}
	
	private VkGraphicsPipelineCreateInfo createInfo;
	private Builder.VertexInputStateConfigurator.@Nullable ExtraConfiguration vertexInputStateExtraConfiguration;
	private Builder.ViewportStateConfigurator.@Nullable ExtraConfiguration viewportStateExtraConfiguration;
	
	private GraphicsPipelineTemplate(VkGraphicsPipelineCreateInfo createInfo, Builder.VertexInputStateConfigurator.@Nullable ExtraConfiguration vertexInputStateExtraConfiguration, Builder.ViewportStateConfigurator.@Nullable ExtraConfiguration viewportStateExtraConfiguration)
	{
		this.createInfo = createInfo;
		this.vertexInputStateExtraConfiguration = vertexInputStateExtraConfiguration;
		this.viewportStateExtraConfiguration = viewportStateExtraConfiguration;
	}
	
	public GraphicsPipeline createPipeline(Device device, ShaderProgram program, VulkanWindow window, PipelineLayout layout, RenderPass renderPass, int subpass) throws VulkanException
	{
		return createPipeline(device, program, window.getSurface(), layout, renderPass, subpass);
	}
	
	public GraphicsPipeline createPipeline(Device device, ShaderProgram program, Surface surface, PipelineLayout layout, RenderPass renderPass, int subpass) throws VulkanException
	{
		return createPipeline(device, program, surface.getData().size(), layout, renderPass, subpass);
	}
	
	//TODO change ShaderProgram to ShaderVertexInputs
	public GraphicsPipeline createPipeline(Device device, ShaderProgram program, Vector2i surfaceSize, PipelineLayout layout, RenderPass renderPass, int subpass) throws VulkanException
	{
		VkPipelineShaderStageCreateInfo.Buffer pStages = ShaderUtils.createPipelineShaderStageCreateInfoBuffer(device, program);
		try
		{
			createInfo.pStages(pStages);
			if (vertexInputStateExtraConfiguration != null) vertexInputStateExtraConfiguration.apply((@NonNull VkPipelineVertexInputStateCreateInfo) createInfo.pVertexInputState(), program);
			if (viewportStateExtraConfiguration != null) viewportStateExtraConfiguration.apply((@NonNull VkPipelineViewportStateCreateInfo) createInfo.pViewportState(), surfaceSize);
			createInfo
				.layout(layout.getVk())
				.renderPass(renderPass.getVk())
				.subpass(subpass)
				.basePipelineHandle(0)
				.basePipelineIndex(0);
			return new GraphicsPipeline(device, layout, createInfo);
		}
		finally
		{
			//TODO reuse shader modules
			for (VkPipelineShaderStageCreateInfo createInfo : pStages) VK10.vkDestroyShaderModule(device.getVk(), createInfo.module(), null);
			pStages.free();
		}
	}
	
	private void free(@Nullable NativeResource struct)
	{
		if (struct != null) struct.free();
	}
	
	@Override
	public void close()
	{
		VkPipelineVertexInputStateCreateInfo pVertexInputState = createInfo.pVertexInputState();
		if (pVertexInputState != null)
		{
			free(pVertexInputState.pVertexBindingDescriptions());
			free(pVertexInputState.pVertexAttributeDescriptions());
			pVertexInputState.free();
		}
		free(createInfo.pInputAssemblyState());
		free(createInfo.pTessellationState());
		VkPipelineViewportStateCreateInfo pViewportState = createInfo.pViewportState();
		if (pViewportState != null)
		{
			free(pViewportState.pViewports());
			free(pViewportState.pScissors());
			pViewportState.free();
		}
		free(createInfo.pRasterizationState());
		VkPipelineMultisampleStateCreateInfo pMultisampleState = createInfo.pMultisampleState();
		if (pMultisampleState != null)
		{
			MemoryUtil.memFree(pMultisampleState.pSampleMask(pMultisampleState.rasterizationSamples()));
			pMultisampleState.free();
		}
		free(createInfo.pDepthStencilState());
		VkPipelineColorBlendStateCreateInfo pColorBlendState = createInfo.pColorBlendState();
		if (pColorBlendState != null)
		{
			free(pColorBlendState.pAttachments());
			pColorBlendState.free();
		}
		VkPipelineDynamicStateCreateInfo pDynamicState = createInfo.pDynamicState();
		if (pDynamicState != null)
		{
			MemoryUtil.memFree(pDynamicState.pDynamicStates());
			pDynamicState.free();
		}
		createInfo.free();
	}
	
	public static class Builder {
		
		public class VertexInputStateConfigurator {
			
			@FunctionalInterface
			public static interface ExtraConfiguration {
				
				VkPipelineVertexInputStateCreateInfo apply(VkPipelineVertexInputStateCreateInfo createInfo, ShaderProgram shaderProgram);//TODO change ShaderProgram to ShaderVertexInputs
			}
			
			private Map<Integer, Pair<Integer, Integer>> bindings;
			private Map<Vector2i, Integer> attributes;
			
			public VertexInputStateConfigurator()
			{
				bindings = new HashMap<>();
				attributes = new HashMap<>();
			}
			
			public VertexInputStateConfigurator addBinding(int binding, int stride, int inputRate)
			{
				bindings.put(binding, new Pair<>(stride, inputRate));
				return this;
			}
			
			public VertexInputStateConfigurator addAttribute(int location, int binding, int offset)
			{
				attributes.put(Vector2i.of(location, binding), offset);
				return this;
			}
			
			protected VkPipelineVertexInputStateCreateInfo configure()
			{
				VkVertexInputBindingDescription.Buffer pVertexBindingDescriptions = null;
				
				if (!bindings.isEmpty())
				{
					pVertexBindingDescriptions = VkVertexInputBindingDescription.malloc(bindings.size());
					for (Entry<Integer, Pair<Integer, Integer>> e : bindings.entrySet()) pVertexBindingDescriptions.get()
						.binding(e.getKey())
						.stride(e.getValue().l())
						.inputRate(e.getValue().r());
					pVertexBindingDescriptions.rewind();
				}
				
				return VkPipelineVertexInputStateCreateInfo.malloc()
					.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO)
					.pNext(0)
					.flags(0)
					.pVertexBindingDescriptions(pVertexBindingDescriptions)
					.pVertexAttributeDescriptions(null);
			}
			
			public ExtraConfiguration extraConfiguration()
			{
				return (createInfo, program) -> {
					
					int variablesCount = (int) program.getShaders().sumOfInt(s -> s.info().getType() == ShaderType.VERTEX ? (int) s.info().getLayouts().sumOfInt(l -> l.getType() == Type.INPUT ? ((ShaderInputLayout) l).getVariables().size() : 0) : 0);
					VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.malloc(variablesCount);
					
					for (PreparedShader shader : program.getShaders()) if (shader.info().getType() == ShaderType.VERTEX)
					{
						for (ShaderLayout l : shader.info().getLayouts()) if (l.getType() == Type.INPUT)
						{
							ShaderInputLayout layout = (ShaderInputLayout) l;
							for (Pair<Integer, ShaderVariable> p : layout.getVariables())
							{
								ShaderInputType type = p.right.getType();
								attributeDescriptions.get().set(p.left, layout.getBinding(), type.getVulkanType(), attributes.getOrDefault(Vector2i.of(p.left, layout.getBinding()), 0));
							}
						}
					}
					createInfo.pVertexAttributeDescriptions(attributeDescriptions.rewind());
					
					return createInfo;
				};
			}
			
			public Builder end()
			{
				return Builder.this;
			}
		}
		
		public class InputAssemblyStateConfigurator {
			
			private int topology;
			private boolean primitiveRestartEnable;
			
			public InputAssemblyStateConfigurator()
			{
				topology = VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST;
				primitiveRestartEnable = false;
			}
			
			public InputAssemblyStateConfigurator topology(int topology)
			{
				this.topology = topology;
				return this;
			}
			
			public InputAssemblyStateConfigurator primitiveRestartEnable(boolean primitiveRestartEnable)
			{
				this.primitiveRestartEnable = primitiveRestartEnable;
				return this;
			}
			
			protected VkPipelineInputAssemblyStateCreateInfo configure()
			{
				return VkPipelineInputAssemblyStateCreateInfo.malloc()
					.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO)
					.pNext(0)
					.flags(0)
					.topology(topology)
					.primitiveRestartEnable(primitiveRestartEnable);
			}
			
			public Builder end()
			{
				return Builder.this;
			}
		}
		
		public class TessellationStateConfigurator {
			
			private int patchControlPoints;
			
			public TessellationStateConfigurator()
			{
				patchControlPoints = 0;
			}
			
			public TessellationStateConfigurator patchControlPoints(int patchControlPoints)
			{
				this.patchControlPoints = patchControlPoints;
				return this;
			}
			
			protected VkPipelineTessellationStateCreateInfo configure()
			{
				return VkPipelineTessellationStateCreateInfo.malloc()
					.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_TESSELLATION_STATE_CREATE_INFO)
					.pNext(0)
					.flags(0)
					.patchControlPoints(patchControlPoints);
			}
			
			public Builder end()
			{
				return Builder.this;
			}
		}
		
		public class ViewportStateConfigurator {
			
			@FunctionalInterface
			public static interface ExtraConfiguration {
				
				VkPipelineViewportStateCreateInfo apply(VkPipelineViewportStateCreateInfo createInfo, Vector2i surfaceSize);
			}
			
			private List<OrthoShape> viewports;
			//TODO replace with a List<Rectangle> 
			private List<@Nullable Vector4i> scissors;
			
			public ViewportStateConfigurator()
			{
				viewports = new LinkedList<>();
				scissors = new LinkedList<>();
			}
			
			public ViewportStateConfigurator addDefaultViewport()
			{
				//TODO: replace with a special "lazy" OrthoShape
				viewports.add(null);
				return this;
			}
			
			public ViewportStateConfigurator addFullScissor()
			{
				//TODO: replace with a special "lazy" rectangle
				scissors.add(null);
				return this;
			}
			
			public ViewportStateConfigurator addViewport(OrthoShape viewport)
			{
				viewports.add(viewport);
				return this;
			}
			
			public ViewportStateConfigurator addScissor(Vector2i offset, Vector2i extent)
			{
				scissors.add(Vector4i.of(offset, extent));
				return this;
			}
			
			protected VkPipelineViewportStateCreateInfo configure()
			{
				VkViewport.Buffer pViewports = null;
				List<OrthoShape> viewports = this.viewports;
				if (!viewports.isEmpty())
				{
					pViewports = VkViewport.malloc(viewports.size());
					for (OrthoShape viewport : viewports)
					{
						VkViewport vkViewport = pViewports.get();
						if (viewport != null)
						{
							vkViewport
								.x(viewport.getStart().x())
								.y(viewport.getStart().y())
								.minDepth(viewport.getStart().z())
								.width(viewport.getEnd().x() - viewport.getStart().x())
								.height(viewport.getEnd().y() - viewport.getStart().y())
								.maxDepth(viewport.getEnd().z());
						}
					}
					pViewports.rewind();
				}
				VkRect2D.Buffer pScissors = null;
				List<@Nullable Vector4i> scissors = this.scissors;
				if (!scissors.isEmpty())
				{
					pScissors = VkRect2D.malloc(scissors.size());
					for (Vector4i scissor : scissors)
					{
						VkRect2D vkScissor = pScissors.get();
						if (scissor != null)
						{
							vkScissor.offset()
								.x(scissor.x())
								.y(scissor.y());
							vkScissor.extent()
								.width(scissor.z())
								.height(scissor.w());
						}
					}
					pScissors.rewind();
				}
				
				return VkPipelineViewportStateCreateInfo.malloc()
					.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO)
					.pNext(0)
					.flags(0)
					.pViewports(pViewports)
					.pScissors(pScissors);
			}
			
			public ExtraConfiguration extraConfiguration()
			{
				List<@Nullable Vector4i> scissors = Lists.fixedSize.ofAll(this.scissors);
				return (createInfo, surfaceSize) -> {
					VkRect2D.Buffer pScissors = createInfo.pScissors();
					if (pScissors != null)
					{
						for (Vector4i scissor : scissors)
						{
							VkRect2D vkScissor = pScissors.get();
							if (scissor == null)
							{
								VkRect2D rect = vkScissor;
								rect.offset()
									.x(0)
									.y(0);
								rect.extent()
									.width(surfaceSize.x())
									.height(surfaceSize.y());
							}
						}
						pScissors.rewind();
					}
					VkViewport.Buffer pViewports = createInfo.pViewports();
					if (pViewports != null)
					{
						for (OrthoShape viewport : viewports)
						{
							VkViewport vkViewport = pViewports.get();
							if (viewport == null)
							{
								vkViewport
									.x(0)
									.y(0)
									.minDepth(0)
									.width(surfaceSize.x())
									.height(surfaceSize.y())
									.maxDepth(1);
							}
						}
						pViewports.rewind();
					}
					return createInfo;
				};
			}
			
			public Builder end()
			{
				return Builder.this;
			}
		}
		
		public class RasterizationStateConfigurator {
			
			private boolean depthClampEnable;
			private boolean rasterizerDiscardEnable;
			private int polygonMode;
			private int cullMode;
			private int frontFace;
			private boolean depthBiasEnable;
			private float lineWidth;
			private float depthBiasConstantFactor;
			private float depthBiasClamp;
			private float depthBiasSlopeFactor;
			
			public RasterizationStateConfigurator()
			{
				depthClampEnable = false;
				rasterizerDiscardEnable = false;
				polygonMode = VK10.VK_POLYGON_MODE_FILL;
				cullMode = VK10.VK_CULL_MODE_BACK_BIT;
				frontFace = VK10.VK_FRONT_FACE_CLOCKWISE;
				depthBiasEnable = false;
				depthBiasConstantFactor = 1;
				depthBiasClamp = 0;
				depthBiasSlopeFactor = 1;
				lineWidth = 1;
			}
			
			public RasterizationStateConfigurator depthClampEnable(boolean depthClampEnable)
			{
				this.depthClampEnable = depthClampEnable;
				return this;
			}
			
			public RasterizationStateConfigurator rasterizerDiscardEnable(boolean rasterizerDiscardEnable)
			{
				this.rasterizerDiscardEnable = rasterizerDiscardEnable;
				return this;
			}
			
			public RasterizationStateConfigurator polygonMode(int polygonMode)
			{
				this.polygonMode = polygonMode;
				return this;
			}
			
			public RasterizationStateConfigurator cullMode(int cullMode)
			{
				this.cullMode = cullMode;
				return this;
			}
			
			public RasterizationStateConfigurator frontFace(int frontFace)
			{
				this.frontFace = frontFace;
				return this;
			}
			
			public RasterizationStateConfigurator depthBiasEnable(boolean depthBiasEnable)
			{
				this.depthBiasEnable = depthBiasEnable;
				return this;
			}
			
			public RasterizationStateConfigurator lineWidth(float lineWidth)
			{
				this.lineWidth = lineWidth;
				return this;
			}
			
			public RasterizationStateConfigurator depthBiasConstantFactor(float depthBiasConstantFactor)
			{
				this.depthBiasConstantFactor = depthBiasConstantFactor;
				return this;
			}
			
			public RasterizationStateConfigurator depthBiasClamp(float depthBiasClamp)
			{
				this.depthBiasClamp = depthBiasClamp;
				return this;
			}
			
			public RasterizationStateConfigurator depthBiasSlopeFactor(float depthBiasSlopeFactor)
			{
				this.depthBiasSlopeFactor = depthBiasSlopeFactor;
				return this;
			}
			
			protected VkPipelineRasterizationStateCreateInfo configure()
			{
				return VkPipelineRasterizationStateCreateInfo.malloc()
					.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO)
					.pNext(0)
					.flags(0)
					.depthClampEnable(depthClampEnable)
					.rasterizerDiscardEnable(rasterizerDiscardEnable)
					.polygonMode(polygonMode)
					.cullMode(cullMode)
					.frontFace(frontFace)
					.depthBiasEnable(depthBiasEnable)
					.depthBiasConstantFactor(depthBiasConstantFactor)
					.depthBiasClamp(depthBiasClamp)
					.depthBiasSlopeFactor(depthBiasSlopeFactor)
					.lineWidth(lineWidth);
			}
			
			public Builder end()
			{
				return Builder.this;
			}
		}
		
		public class MultisampleStateConfigurator {
			
			private int rasterizationSamples;
			private boolean sampleShadingEnable;
			private float minSampleShading;
			private int @Nullable [] sampleMask;
			private boolean alphaToCoverageEnable;
			private boolean alphaToOneEnable;
			
			public MultisampleStateConfigurator()
			{
				rasterizationSamples = VK10.VK_SAMPLE_COUNT_1_BIT;
				sampleShadingEnable = false;
				minSampleShading = 0;
				sampleMask = null;
				alphaToCoverageEnable = false;
				alphaToOneEnable = false;
			}
			
			public MultisampleStateConfigurator rasterizationSamples(int rasterizationSamples)
			{
				this.rasterizationSamples = rasterizationSamples;
				return this;
			}
			
			public MultisampleStateConfigurator sampleShadingEnable(boolean sampleShadingEnable)
			{
				this.sampleShadingEnable = sampleShadingEnable;
				return this;
			}
			
			public MultisampleStateConfigurator minSampleShading(float minSampleShading)
			{
				this.minSampleShading = minSampleShading;
				return this;
			}
			
			public MultisampleStateConfigurator alphaToCoverageEnable(boolean alphaToCoverageEnable)
			{
				this.alphaToCoverageEnable = alphaToCoverageEnable;
				return this;
			}
			
			public MultisampleStateConfigurator alphaToOneEnable(boolean alphaToOneEnable)
			{
				this.alphaToOneEnable = alphaToOneEnable;
				return this;
			}
			
			public MultisampleStateConfigurator sampleMask(int... bits)
			{
				sampleMask = bits;
				return this;
			}
			
			protected VkPipelineMultisampleStateCreateInfo configure()
			{
				IntBuffer pSampleMask = null;
				int[] sampleMask = this.sampleMask;
				if (sampleMask != null)
				{
					pSampleMask = MemoryUtil.memAllocInt(rasterizationSamples);
					int i = 0;
					if (sampleMask.length < rasterizationSamples)
					{
						while (i < sampleMask.length) pSampleMask.put(sampleMask[i++]);
					}
					while (i < rasterizationSamples) pSampleMask.put(0xFFFFFFFF);
					pSampleMask.rewind();
				}
				return VkPipelineMultisampleStateCreateInfo.malloc()
					.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO)
					.pNext(0)
					.flags(0)
					.rasterizationSamples(rasterizationSamples)
					.sampleShadingEnable(sampleShadingEnable)
					.minSampleShading(minSampleShading)
					.pSampleMask(pSampleMask)
					.alphaToCoverageEnable(alphaToCoverageEnable)
					.alphaToOneEnable(alphaToOneEnable);
			}
			
			public Builder end()
			{
				return Builder.this;
			}
		}
		
		public class DepthStencilStateConfigurator {
			
			private int flags;
			private boolean depthTestEnable;
			private boolean depthWriteEnable;
			private int depthCompareOp;
			private boolean depthBoundsTestEnable;
			private boolean stencilTestEnable;
			private float minDepthBounds;
			private float maxDepthBounds;
			
			public DepthStencilStateConfigurator()
			{
				flags = 0;
				depthTestEnable = false;
				depthWriteEnable = true;
				depthCompareOp = VK10.VK_COMPARE_OP_LESS;
				depthBoundsTestEnable = false;
				stencilTestEnable = false;
				minDepthBounds = 0;
				maxDepthBounds = 1;
			}
			
			public DepthStencilStateConfigurator flags(int flags)
			{
				this.flags = flags;
				return this;
			}
			
			public DepthStencilStateConfigurator depthTestEnable(boolean depthTestEnable)
			{
				this.depthTestEnable = depthTestEnable;
				return this;
			}
			
			public DepthStencilStateConfigurator depthWriteEnable(boolean depthWriteEnable)
			{
				this.depthWriteEnable = depthWriteEnable;
				return this;
			}
			
			public DepthStencilStateConfigurator depthCompareOp(int depthCompareOp)
			{
				this.depthCompareOp = depthCompareOp;
				return this;
			}
			
			public DepthStencilStateConfigurator depthBoundsTestEnable(boolean depthBoundsTestEnable)
			{
				this.depthBoundsTestEnable = depthBoundsTestEnable;
				return this;
			}
			
			public DepthStencilStateConfigurator stencilTestEnable(boolean stencilTestEnable)
			{
				this.stencilTestEnable = stencilTestEnable;
				return this;
			}
			
			public DepthStencilStateConfigurator minDepthBounds(float minDepthBounds)
			{
				this.minDepthBounds = minDepthBounds;
				return this;
			}
			
			public DepthStencilStateConfigurator maxDepthBounds(float maxDepthBounds)
			{
				this.maxDepthBounds = maxDepthBounds;
				return this;
			}
			
			protected VkPipelineDepthStencilStateCreateInfo configure()
			{
				return VkPipelineDepthStencilStateCreateInfo.malloc()
					.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO)
					.pNext(0)
					.flags(flags)
					.depthTestEnable(depthTestEnable)
					.depthWriteEnable(depthWriteEnable)
					.depthCompareOp(depthCompareOp)
					.depthBoundsTestEnable(depthBoundsTestEnable)
					.stencilTestEnable(stencilTestEnable)
					.back(c -> c.clear())
					.front(c -> c.clear())
					.minDepthBounds(minDepthBounds)
					.maxDepthBounds(maxDepthBounds);
			}
			
			public Builder end()
			{
				return Builder.this;
			}
		}
		
		public class ColorBlendStateConfigurator {
			
			private record AttachmentData(boolean blendEnable, int srcColorBlendFactor, int dstColorBlendFactor, int colorBlendOp, int srcAlphaBlendFactor, int dstAlphaBlendFactor, int alphaBlendOp, int colorWriteMask) {}
			
			private int flags;
			private boolean logicOpEnable;
			private int logicOp;
			private List<AttachmentData> attachments;
			private float[] blendConstants;
			
			public ColorBlendStateConfigurator()
			{
				flags = 0;
				logicOpEnable = false;
				logicOp = VK10.VK_LOGIC_OP_COPY;
				attachments = new LinkedList<>();
				blendConstants = new float[4];
			}
			
			public ColorBlendStateConfigurator flags(int flags)
			{
				this.flags = flags;
				return this;
			}
			
			public ColorBlendStateConfigurator logicOpEnable(boolean logicOpEnable)
			{
				this.logicOpEnable = logicOpEnable;
				return this;
			}
			
			public ColorBlendStateConfigurator logicOp(int logicOp)
			{
				this.logicOp = logicOp;
				return this;
			}
			
			public ColorBlendStateConfigurator addAttachment(boolean blendEnable, int srcColorBlendFactor, int dstColorBlendFactor, int colorBlendOp, int srcAlphaBlendFactor, int dstAlphaBlendFactor, int alphaBlendOp, int colorWriteMask)
			{
				attachments.add(new AttachmentData(blendEnable, srcColorBlendFactor, dstColorBlendFactor, colorBlendOp, srcAlphaBlendFactor, dstAlphaBlendFactor, alphaBlendOp, colorWriteMask));
				return this;
			}
			
			public ColorBlendStateConfigurator blendConstants(float[] blendConstants)
			{
				this.blendConstants = blendConstants;
				return this;
			}
			
			public ColorBlendStateConfigurator blendConstants(float r, float g, float b, float a)
			{
				this.blendConstants[0] = r;
				this.blendConstants[1] = g;
				this.blendConstants[2] = b;
				this.blendConstants[3] = a;
				return this;
			}
			
			public ColorBlendStateConfigurator blendConstants(float r, float g, float b)
			{
				return blendConstants(r, g, b, 1);
			}
			
			public ColorBlendStateConfigurator blendConstants(Vector3f color)
			{
				return blendConstants(color.x(), color.y(), color.z(), 1);
			}
			
			public ColorBlendStateConfigurator blendConstants(Vector4f color)
			{
				return blendConstants(color.x(), color.y(), color.z(), color.w());
			}
			
			protected VkPipelineColorBlendStateCreateInfo configure()
			{
				VkPipelineColorBlendAttachmentState.Buffer pAttachments = null;
				List<AttachmentData> attachments = this.attachments;
				if (!attachments.isEmpty())
				{
					pAttachments = VkPipelineColorBlendAttachmentState.malloc(attachments.size());
					for (AttachmentData data : attachments) pAttachments.get()
						.blendEnable(data.blendEnable)
						.srcColorBlendFactor(data.srcColorBlendFactor)
						.dstColorBlendFactor(data.dstColorBlendFactor)
						.colorBlendOp(data.colorBlendOp)
						.srcAlphaBlendFactor(data.srcAlphaBlendFactor)
						.dstAlphaBlendFactor(data.dstAlphaBlendFactor)
						.alphaBlendOp(data.alphaBlendOp)
						.colorWriteMask(data.colorWriteMask);
					pAttachments.rewind();
				}
				return VkPipelineColorBlendStateCreateInfo.malloc()
					.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO)
					.pNext(0)
					.flags(flags)
					.logicOpEnable(logicOpEnable)
					.logicOp(logicOp)
					.pAttachments(pAttachments)
					.blendConstants(0, blendConstants[0])
					.blendConstants(1, blendConstants[1])
					.blendConstants(2, blendConstants[2])
					.blendConstants(3, blendConstants[3]);
			}
			
			public Builder end()
			{
				return Builder.this;
			}
		}
		
		public class DynamicStateConfigurator {
			
			private List<Integer> dynamicStates;
			
			public DynamicStateConfigurator()
			{
				dynamicStates = new LinkedList<>();
			}
			
			public DynamicStateConfigurator addDynamicState(int dynamicState)
			{
				dynamicStates.add(dynamicState);
				return this;
			}
			
			protected VkPipelineDynamicStateCreateInfo configure()
			{
				IntBuffer pDynamicStates = MemoryUtil.memAllocInt(dynamicStates.size());
				for (Integer state : dynamicStates) pDynamicStates.put(state);
				return VkPipelineDynamicStateCreateInfo.malloc()
					.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO)
					.pNext(0)
					.flags(0)
					.pDynamicStates(pDynamicStates.rewind());
			}
			
			public Builder end()
			{
				return Builder.this;
			}
		}
		
		private @Nullable VertexInputStateConfigurator vertexInputStateConfigurator;
		private @Nullable InputAssemblyStateConfigurator inputAssemblyStateConfigurator;
		private @Nullable TessellationStateConfigurator tessellationStateConfigurator;
		private @Nullable ViewportStateConfigurator viewportStateConfigurator;
		private @Nullable RasterizationStateConfigurator rasterizationStateConfigurator;
		private @Nullable MultisampleStateConfigurator multisampleStateConfigurator;
		private @Nullable DepthStencilStateConfigurator depthStencilStateConfigurator;
		private @Nullable ColorBlendStateConfigurator colorBlendStateConfigurator;
		private @Nullable DynamicStateConfigurator dynamicStateConfigurator;
		
		public Builder()
		{
			
		}
		
		public VertexInputStateConfigurator configureVertexInputState()
		{
			return vertexInputStateConfigurator = new VertexInputStateConfigurator();
		}
		
		public Builder configureVertexInputState(Consumer<VertexInputStateConfigurator> configure)
		{
			configure.accept(vertexInputStateConfigurator = new VertexInputStateConfigurator());
			return this;
		}
		
		public InputAssemblyStateConfigurator configureInputAssemblyState()
		{
			return inputAssemblyStateConfigurator = new InputAssemblyStateConfigurator();
		}
		
		public Builder configureInputAssemblyState(Consumer<InputAssemblyStateConfigurator> configure)
		{
			configure.accept(inputAssemblyStateConfigurator = new InputAssemblyStateConfigurator());
			return this;
		}
		
		public TessellationStateConfigurator configureTessellationState()
		{
			return tessellationStateConfigurator = new TessellationStateConfigurator();
		}
		
		public Builder configureTessellationState(Consumer<TessellationStateConfigurator> configure)
		{
			configure.accept(tessellationStateConfigurator = new TessellationStateConfigurator());
			return this;
		}
		
		public ViewportStateConfigurator configureViewportState()
		{
			return viewportStateConfigurator = new ViewportStateConfigurator();
		}
		
		public Builder configureViewportState(Consumer<ViewportStateConfigurator> configure)
		{
			configure.accept(viewportStateConfigurator = new ViewportStateConfigurator());
			return this;
		}
		
		public RasterizationStateConfigurator configureRasterizationState()
		{
			return rasterizationStateConfigurator = new RasterizationStateConfigurator();
		}
		
		public Builder configureRasterizationState(Consumer<RasterizationStateConfigurator> configure)
		{
			configure.accept(rasterizationStateConfigurator = new RasterizationStateConfigurator());
			return this;
		}
		
		public MultisampleStateConfigurator configureMultisampleState()
		{
			return multisampleStateConfigurator = new MultisampleStateConfigurator();
		}
		
		public Builder configureMultisampleState(Consumer<MultisampleStateConfigurator> configure)
		{
			configure.accept(multisampleStateConfigurator = new MultisampleStateConfigurator());
			return this;
		}
		
		public DepthStencilStateConfigurator configureDepthStencilState()
		{
			return depthStencilStateConfigurator = new DepthStencilStateConfigurator();
		}
		
		public Builder configureDepthStencilState(Consumer<DepthStencilStateConfigurator> configure)
		{
			configure.accept(depthStencilStateConfigurator = new DepthStencilStateConfigurator());
			return this;
		}
		
		public ColorBlendStateConfigurator configureColorBlendState()
		{
			return colorBlendStateConfigurator = new ColorBlendStateConfigurator();
		}
		
		public Builder configureColorBlendState(Consumer<ColorBlendStateConfigurator> configure)
		{
			configure.accept(colorBlendStateConfigurator = new ColorBlendStateConfigurator());
			return this;
		}
		
		public DynamicStateConfigurator configureDynamicStateState()
		{
			return dynamicStateConfigurator = new DynamicStateConfigurator();
		}
		
		public Builder configureDynamicStateState(Consumer<DynamicStateConfigurator> configure)
		{
			configure.accept(dynamicStateConfigurator = new DynamicStateConfigurator());
			return this;
		}
		
		public GraphicsPipelineTemplate build()
		{
			VkGraphicsPipelineCreateInfo createInfo = VkGraphicsPipelineCreateInfo.malloc()
				.sType(VK10.VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO)
				.pNext(0)
				.flags(0)
				.pVertexInputState(vertexInputStateConfigurator != null ? vertexInputStateConfigurator.configure() : null)
				.pInputAssemblyState(inputAssemblyStateConfigurator != null ? inputAssemblyStateConfigurator.configure() : null)
				.pTessellationState(tessellationStateConfigurator != null ? tessellationStateConfigurator.configure() : null)
				.pViewportState(viewportStateConfigurator != null ? viewportStateConfigurator.configure() : null)
				.pRasterizationState(rasterizationStateConfigurator != null ? rasterizationStateConfigurator.configure() : null)
				.pMultisampleState(multisampleStateConfigurator != null ? multisampleStateConfigurator.configure() : null)
				.pDepthStencilState(depthStencilStateConfigurator != null ? depthStencilStateConfigurator.configure() : null)
				.pColorBlendState(colorBlendStateConfigurator != null ? colorBlendStateConfigurator.configure() : null)
				.pDynamicState(dynamicStateConfigurator != null ? dynamicStateConfigurator.configure() : null);
			
			return new GraphicsPipelineTemplate(createInfo, vertexInputStateConfigurator != null ? vertexInputStateConfigurator.extraConfiguration() : null, viewportStateConfigurator != null ? viewportStateConfigurator.extraConfiguration() : null);
		}
	}
}
