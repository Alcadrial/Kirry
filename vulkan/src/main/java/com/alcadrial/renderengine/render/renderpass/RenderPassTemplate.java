package com.alcadrial.renderengine.render.renderpass;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSubpassDependency;
import org.lwjgl.vulkan.VkSubpassDescription;

import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public class RenderPassTemplate implements AutoCloseable {
	
	public static Builder create()
	{
		return new Builder();
	}
	
	public static RenderPassTemplate colorTextureTemplate(int format)
	{
		return create()
			.addAttachment(config -> config
				.format(format)
				.samples(VK10.VK_SAMPLE_COUNT_1_BIT)
				.loadOp(VK10.VK_ATTACHMENT_LOAD_OP_LOAD)
				.storeOp(VK10.VK_ATTACHMENT_STORE_OP_STORE)
				.stencilLoadOp(VK10.VK_ATTACHMENT_LOAD_OP_DONT_CARE)
				.stencilStoreOp(VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE)
				.initialLayout(VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
				.finalLayout(KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR))
			.addSubpass(config -> config
				.pipelineBindPoint(VK10.VK_PIPELINE_BIND_POINT_GRAPHICS)
				.addColor(0, VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL))
			.addSubpassDependency(config -> config
				.srcSubpass(VK10.VK_SUBPASS_EXTERNAL)
				.srcStageMask(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
				.dstStageMask(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
				.dstAccessMask(VK10.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT))
			.build();
	}
	
	public static RenderPassTemplate initColorTextureTemplate(int format)
	{
		return create()
			.addAttachment(config -> config
				.format(format)
				.samples(VK10.VK_SAMPLE_COUNT_1_BIT)
				.loadOp(VK10.VK_ATTACHMENT_LOAD_OP_CLEAR)
				.storeOp(VK10.VK_ATTACHMENT_STORE_OP_STORE)
				.stencilLoadOp(VK10.VK_ATTACHMENT_LOAD_OP_DONT_CARE)
				.stencilStoreOp(VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE)
				.initialLayout(VK10.VK_IMAGE_LAYOUT_UNDEFINED)
				.finalLayout(KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR))
			.addSubpass(config -> config
				.pipelineBindPoint(VK10.VK_PIPELINE_BIND_POINT_GRAPHICS)
				.addColor(0, VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL))
			.addSubpassDependency(config -> config
				.srcSubpass(VK10.VK_SUBPASS_EXTERNAL)
				.srcStageMask(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
				.dstStageMask(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
				.dstAccessMask(VK10.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT))
			.build();
	}
	
	public static RenderPassTemplate colorDepthTextureTemplate(int format)
	{
		return create()
			.addAttachment(config -> config
				.format(format)
				.samples(VK10.VK_SAMPLE_COUNT_1_BIT)
				.loadOp(VK10.VK_ATTACHMENT_LOAD_OP_LOAD)
				.storeOp(VK10.VK_ATTACHMENT_STORE_OP_STORE)
				.stencilLoadOp(VK10.VK_ATTACHMENT_LOAD_OP_DONT_CARE)
				.stencilStoreOp(VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE)
				.initialLayout(VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
				.finalLayout(KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)
			).addAttachment(config -> config
				.format(VK10.VK_FORMAT_D24_UNORM_S8_UINT)
				.samples(VK10.VK_SAMPLE_COUNT_1_BIT)
				.loadOp(VK10.VK_ATTACHMENT_LOAD_OP_CLEAR)
				.storeOp(VK10.VK_ATTACHMENT_STORE_OP_STORE)
				.stencilLoadOp(VK10.VK_ATTACHMENT_LOAD_OP_DONT_CARE)
				.stencilStoreOp(VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE)
				.initialLayout(VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
				.finalLayout(VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
			).addSubpass(config -> config
				.pipelineBindPoint(VK10.VK_PIPELINE_BIND_POINT_GRAPHICS)
				.addColor(0, VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
				.depthStencil(1, VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
			).addSubpassDependency(config -> config
				.srcSubpass(VK10.VK_SUBPASS_EXTERNAL)
				.srcStageMask(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT | VK10.VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT)
				.dstStageMask(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT | VK10.VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT)
				.dstAccessMask(VK10.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT | VK10.VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT)
			).build();
	}
	
	public static RenderPassTemplate initColorDepthTextureTemplate(int format)
	{
		return create()
			.addAttachment(config -> config
				.format(format)
				.samples(VK10.VK_SAMPLE_COUNT_1_BIT)
				.loadOp(VK10.VK_ATTACHMENT_LOAD_OP_CLEAR)
				.storeOp(VK10.VK_ATTACHMENT_STORE_OP_STORE)
				.stencilLoadOp(VK10.VK_ATTACHMENT_LOAD_OP_DONT_CARE)
				.stencilStoreOp(VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE)
				.initialLayout(VK10.VK_IMAGE_LAYOUT_UNDEFINED)
				.finalLayout(KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)
			).addAttachment(config -> config
				.format(VK10.VK_FORMAT_D24_UNORM_S8_UINT)
				.samples(VK10.VK_SAMPLE_COUNT_1_BIT)
				.loadOp(VK10.VK_ATTACHMENT_LOAD_OP_CLEAR)
				.storeOp(VK10.VK_ATTACHMENT_STORE_OP_STORE)
				.stencilLoadOp(VK10.VK_ATTACHMENT_LOAD_OP_DONT_CARE)
				.stencilStoreOp(VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE)
				.initialLayout(VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
				.finalLayout(VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
			).addSubpass(config -> config
				.pipelineBindPoint(VK10.VK_PIPELINE_BIND_POINT_GRAPHICS)
				.addColor(0, VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
				.depthStencil(1, VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
			).addSubpassDependency(config -> config
				.srcSubpass(VK10.VK_SUBPASS_EXTERNAL)
				.srcStageMask(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT | VK10.VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT)
				.dstStageMask(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT | VK10.VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT)
				.dstAccessMask(VK10.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT | VK10.VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT)
			).build();
	}
	
	private VkRenderPassCreateInfo createInfo;
	
	private RenderPassTemplate(VkRenderPassCreateInfo createInfo)
	{
		this.createInfo = createInfo;
	}
	
	public RenderPass createRenderPass() throws VulkanException
	{
		return createRenderPass(RenderEngine.getVulkan().getDevice());
	}
	
	public RenderPass createRenderPass(Device device) throws VulkanException
	{
		return new RenderPass(device, createInfo);
	}
	
	@SuppressWarnings("null")
	@Override
	public void close()
	{
		for (VkAttachmentDescription attachment : createInfo.pAttachments()) attachment.clear();
		for (VkSubpassDescription subpass : createInfo.pSubpasses())
		{
			for (VkAttachmentReference input : subpass.pInputAttachments()) input.clear();
			for (VkAttachmentReference color : subpass.pColorAttachments()) color.clear();
			for (VkAttachmentReference resolve : subpass.pResolveAttachments()) resolve.clear();
			subpass.pDepthStencilAttachment().clear();
			subpass.pPreserveAttachments().clear();
			subpass.clear();
		}
		for (VkSubpassDependency dependency : createInfo.pDependencies()) dependency.clear();
		createInfo.close();
	}
	
	public static class Builder {
		
		public class AttachmentConfigurator {
			
			private int flags;
			private int format;
			private int samples;
			private int loadOp;
			private int storeOp;
			private int stencilLoadOp;
			private int stencilStoreOp;
			private int initialLayout;
			private int finalLayout;
			
			public AttachmentConfigurator flags(int flags)
			{
				this.flags = flags;
				return this;
			}
			
			public AttachmentConfigurator format(int format)
			{
				this.format = format;
				return this;
			}
			
			public AttachmentConfigurator samples(int samples)
			{
				this.samples = samples;
				return this;
			}
			
			public AttachmentConfigurator loadOp(int loadOp)
			{
				this.loadOp = loadOp;
				return this;
			}
			
			public AttachmentConfigurator storeOp(int storeOp)
			{
				this.storeOp = storeOp;
				return this;
			}
			
			public AttachmentConfigurator stencilLoadOp(int stencilLoadOp)
			{
				this.stencilLoadOp = stencilLoadOp;
				return this;
			}
			
			public AttachmentConfigurator stencilStoreOp(int stencilStoreOp)
			{
				this.stencilStoreOp = stencilStoreOp;
				return this;
			}
			
			public AttachmentConfigurator initialLayout(int initialLayout)
			{
				this.initialLayout = initialLayout;
				return this;
			}
			
			public AttachmentConfigurator finalLayout(int finalLayout)
			{
				this.finalLayout = finalLayout;
				return this;
			}
			
			private VkAttachmentDescription attachment()
			{
				return VkAttachmentDescription.malloc().set(flags, format, samples, loadOp, storeOp, stencilLoadOp, stencilStoreOp, initialLayout, finalLayout);
			}
		}
		
		public class SubpassConfigurator {
			
			private int flags;
			private int pipelineBindPoint;
			private Map<Integer, Integer> inputAttachments;
			private Map<Integer, Integer> colorAttachments;
			private Map<Integer, Integer> resolveAttachments;
			private int depthStencilAttachment;
			private int depthStencilLayout;
			private List<Integer> preserveAttachments;
			
			public SubpassConfigurator()
			{
				inputAttachments = new HashMap<>();
				colorAttachments = new HashMap<>();
				resolveAttachments = new HashMap<>();
				preserveAttachments = new LinkedList<>();
				depthStencilAttachment = -1;
			}
			
			public SubpassConfigurator flags(int flags)
			{
				this.flags = flags;
				return this;
			}
			
			public SubpassConfigurator pipelineBindPoint(int pipelineBindPoint)
			{
				this.pipelineBindPoint = pipelineBindPoint;
				return this;
			}
			
			public SubpassConfigurator addInput(int attachment, int layout)
			{
				inputAttachments.put(attachment, layout);
				return this;
			}
			
			public SubpassConfigurator addColor(int attachment, int layout)
			{
				colorAttachments.put(attachment, layout);
				return this;
			}
			
			public SubpassConfigurator addResolve(int attachment, int layout)
			{
				resolveAttachments.put(attachment, layout);
				return this;
			}
			
			public SubpassConfigurator addPreserve(int attachment)
			{
				preserveAttachments.add(attachment);
				return this;
			}
			
			public SubpassConfigurator depthStencil(int attachment, int layout)
			{
				depthStencilAttachment = attachment;
				depthStencilLayout = layout;
				return this;
			}
			
			private VkSubpassDescription subpass()
			{
				VkAttachmentReference.Buffer input = null;
				VkAttachmentReference.Buffer color = null;
				VkAttachmentReference.Buffer resolve = null;
				VkAttachmentReference depthStencil = null;
				IntBuffer preserve = null;
				
				if (!inputAttachments.isEmpty())
				{
					input = VkAttachmentReference.malloc(inputAttachments.size());
					for (Entry<Integer, Integer> e : inputAttachments.entrySet()) input.get().set(e.getKey(), e.getValue());
					input = input.rewind();
				}
				if (!colorAttachments.isEmpty())
				{
					color = VkAttachmentReference.malloc(colorAttachments.size());
					for (Entry<Integer, Integer> e : colorAttachments.entrySet()) color.get().set(e.getKey(), e.getValue());
					color = color.rewind();
				}
				if (!resolveAttachments.isEmpty())
				{
					resolve = VkAttachmentReference.malloc(resolveAttachments.size());
					for (Entry<Integer, Integer> e : resolveAttachments.entrySet()) resolve.get().set(e.getKey(), e.getValue());
					resolve = resolve.rewind();
				}
				if (!preserveAttachments.isEmpty())
				{
					preserve = MemoryUtil.memAllocInt(preserveAttachments.size());
					for (Integer attachment : preserveAttachments) preserve.put(attachment);
					preserve = preserve.rewind();
				}
				if (depthStencilAttachment != -1)
				{
					depthStencil = VkAttachmentReference.malloc();
					depthStencil.set(depthStencilAttachment, depthStencilLayout);
				}
				
				return VkSubpassDescription.malloc().set(flags, pipelineBindPoint, input, colorAttachments.size(), color, resolve, depthStencil, preserve);
			}
		}
		
		public class SubpassDependencyConfigurator {
			
			private int srcSubpass;
			private int dstSubpass;
			private int srcStageMask;
			private int dstStageMask;
			private int srcAccessMask;
			private int dstAccessMask;
			private int dependencyFlags;
			
			public SubpassDependencyConfigurator srcSubpass(int srcSubpass)
			{
				this.srcSubpass = srcSubpass;
				return this;
			}
			
			public SubpassDependencyConfigurator dstSubpass(int dstSubpass)
			{
				this.dstSubpass = dstSubpass;
				return this;
			}
			
			public SubpassDependencyConfigurator srcStageMask(int srcStageMask)
			{
				this.srcStageMask = srcStageMask;
				return this;
			}
			
			public SubpassDependencyConfigurator dstStageMask(int dstStageMask)
			{
				this.dstStageMask = dstStageMask;
				return this;
			}
			
			public SubpassDependencyConfigurator srcAccessMask(int srcAccessMask)
			{
				this.srcAccessMask = srcAccessMask;
				return this;
			}
			
			public SubpassDependencyConfigurator dstAccessMask(int dstAccessMask)
			{
				this.dstAccessMask = dstAccessMask;
				return this;
			}
			
			public SubpassDependencyConfigurator dependencyFlags(int dependencyFlags)
			{
				this.dependencyFlags = dependencyFlags;
				return this;
			}
			
			private VkSubpassDependency dependency()
			{
				return VkSubpassDependency.malloc().set(srcSubpass, dstSubpass, srcStageMask, dstStageMask, srcAccessMask, dstAccessMask, dependencyFlags);
			}
		}
		
		private List<AttachmentConfigurator> attachmentConfigurators;
		private List<SubpassConfigurator> subpassConfigurators;
		private List<SubpassDependencyConfigurator> dependencyConfigurators;
		
		private Builder()
		{
			attachmentConfigurators = new LinkedList<>();
			subpassConfigurators = new LinkedList<>();
			dependencyConfigurators = new LinkedList<>();
		}
		
		public Builder addAttachment(Consumer<AttachmentConfigurator> config)
		{
			AttachmentConfigurator configurator = new AttachmentConfigurator();
			config.accept(configurator);
			attachmentConfigurators.add(configurator);
			return this;
		}
		
		public Builder addSubpass(Consumer<SubpassConfigurator> config)
		{
			SubpassConfigurator configurator = new SubpassConfigurator();
			config.accept(configurator);
			subpassConfigurators.add(configurator);
			return this;
		}
		
		public Builder addSubpassDependency(Consumer<SubpassDependencyConfigurator> config)
		{
			SubpassDependencyConfigurator configurator = new SubpassDependencyConfigurator();
			config.accept(configurator);
			dependencyConfigurators.add(configurator);
			return this;
		}
		
		public RenderPassTemplate build()
		{
			VkRenderPassCreateInfo createInfo = VkRenderPassCreateInfo.malloc();
			createInfo.sType(VK10.VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO);
			createInfo.pNext(0);
			createInfo.flags(0);
			
			VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.malloc(attachmentConfigurators.size());
			VkSubpassDescription.Buffer subpasses = VkSubpassDescription.malloc(subpassConfigurators.size());
			VkSubpassDependency.Buffer dependencies = VkSubpassDependency.malloc(dependencyConfigurators.size());
			
			for (AttachmentConfigurator configurator : attachmentConfigurators) attachments.put(configurator.attachment());
			for (SubpassConfigurator configurator : subpassConfigurators) subpasses.put(configurator.subpass());
			for (SubpassDependencyConfigurator configurator : dependencyConfigurators) dependencies.put(configurator.dependency());
			
			createInfo.pAttachments(attachments.rewind());
			createInfo.pSubpasses(subpasses.rewind());
			createInfo.pDependencies(dependencies.rewind());
			
			return new RenderPassTemplate(createInfo);
		}
	}
}
