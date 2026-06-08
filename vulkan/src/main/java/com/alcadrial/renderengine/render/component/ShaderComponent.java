package com.alcadrial.renderengine.render.component;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

import com.alcadrial.renderengine.core.component.ComponentType;
import com.alcadrial.renderengine.core.util.RawBitSet;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.resources.ShaderResource;
import com.alcadrial.renderengine.render.shader.PreparedShader;
import com.alcadrial.renderengine.render.shader.ShaderProgram;
import com.alcadrial.renderengine.render.shader.ShaderUtils;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.util.future.CompletableFutureUtils;

public abstract class ShaderComponent extends AbstractVulkanComponent<ShaderComponent.Params, ShaderComponent.Properties> {
	
	public static final ComponentType<ShaderComponent> TYPE = ComponentType.of(ShaderComponent.class);
	
	public record Params(
		Device device
	) {}
	
	public record Properties(
		@Nullable ShaderProgram program,
		VkPipelineShaderStageCreateInfo.@Nullable Buffer stages
	) {}
	
	public static ShaderComponent ofResource(ShaderResource... shaderResources)
	{
		return new ShaderComponent.Resource(shaderResources);
	}
	
	public static ShaderComponent ofStatic(ShaderProgram shaderProgram)
	{
		return new ShaderComponent.Static(shaderProgram);
	}
	
	protected @Nullable Device device;
	protected VkPipelineShaderStageCreateInfo.@Nullable Buffer pStages;
	
	public abstract ShaderProgram getProgram();
	
	public VkPipelineShaderStageCreateInfo.Buffer getStages()
	{
		return VulkanErrorUtils.nonNull(pStages, "Component not initialized");
	}
	
	@Override
	public void close()
	{
		freeStages();
	}
	
	protected void freeStages()
	{
		if (pStages != null)
		{
			pStages.forEach(i -> {
				VK10.vkDestroyShaderModule(((@NonNull Device) device).getVk(), i.module(), null);
			});
			pStages = null;
		}
	}
	
	private static class Resource extends ShaderComponent {
		
		private MutableList<ShaderResource> resources;
		private @Nullable ShaderProgram program;
		private RawBitSet invalidSet;
		private AtomicBoolean invalid;
		
		public Resource(ShaderResource... resources)
		{
			this.resources = Lists.mutable.of(resources);
			invalidSet = RawBitSet.of(resources.length);
			invalid = new AtomicBoolean(false);
		}
		
		@Override
		public CompletableFuture<?> load()
		{
			return CompletableFutureUtils.ofAll(resources.collect(ShaderResource::load)).thenRun(this::createProgram);
		}
		
		@Override
		public CompletableFuture<?> reload()
		{
			return CompletableFutureUtils.ofAll(resources.collect(ShaderResource::reload)).thenRun(this::createProgram);
		}
		
		private void createProgram()
		{
			program = ShaderProgram.create().addAll(resources.asLazy().collect(ShaderResource::getShader)).build();
			isLoaded = true;
			reloadCounter++;
		}
		
		@Override
		public void init(Params params) throws VulkanException
		{
			device = params.device;
			pStages = ShaderUtils.createPipelineShaderStageCreateInfoBuffer(params.device, VulkanErrorUtils.nonNull(program, "Component not initialized"));
			resources.forEachWithIndex(0, resources.size() - 1, (r, i) -> r.addReloadListener(_ -> {
				invalidSet.turnOn(i);
				invalid.set(true);
			}));
		}
		
		@Override
		public void reinit(Params params) throws VulkanException
		{
			if (device != null && !device.equals(params.device)) throw new UnsupportedOperationException("Cannot change device after init");
			if (pStages != null) freeStages();
			pStages = ShaderUtils.createPipelineShaderStageCreateInfoBuffer(params.device, VulkanErrorUtils.nonNull(program, "Component not initialized"));
		}
		
		@Override
		public boolean validate(Params params) throws VulkanException
		{
			if (!invalid.getAndSet(false) || pStages == null) return false;
			if (device != null && !device.equals(params.device)) throw new UnsupportedOperationException("Cannot change device after init");
			program = ShaderProgram.create().addAll(resources.asLazy().collect(ShaderResource::getShader)).build();
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				VkShaderModuleCreateInfo createInfo = VkShaderModuleCreateInfo.malloc(stack)
					.sType(VK10.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
					.pNext(0)
					.flags(0);
				long[] p = new long[1];
				for (int i = 0; i < resources.size(); i++) if (invalidSet.turnOff(i))
				{
					PreparedShader shader = resources.get(i).getShader();
					createInfo.pCode(stack.bytes(shader.shader().getLastCompilation().orElseThrow().binary()));
					VulkanErrorUtils.assertNoError(VK10.vkCreateShaderModule(params.device.getVk(), createInfo, null, p), "Failed to create shader module");
					
					VkPipelineShaderStageCreateInfo stage = ((VkPipelineShaderStageCreateInfo.@NonNull Buffer) pStages).get(i);
					VK10.vkDestroyShaderModule(params.device.getVk(), stage.module(), null);
					stage
						.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
						.pNext(0)
						.flags(0)
						.stage(shader.type().getStageBit())
						.module(p[0])
						.pName(stack.UTF8("main"))
						.pSpecializationInfo(/*shader constants*/null);
				}
			}
			return true;
		}
		
		@Override
		public boolean listenResource()
		{
			boolean result = false;
			for (ShaderResource resource : resources)
			{
				try
				{
					resource.registerForChanges(ShaderResource::reload);
					result = true;
				}
				catch (IOException _)
				{}
			}
			return result;
		}
		
		@Override
		public Properties getProperties()
		{
			return new Properties(program, pStages);
		}
		
		@Override
		public void close()
		{
			resources.forEach(ShaderResource::close);
			super.close();
		}
		
		@Override
		public ShaderProgram getProgram()
		{
			return VulkanErrorUtils.nonNull(program, "Component not initialized");
		}
	}
	
	private static class Static extends ShaderComponent {
		
		private ShaderProgram program;
		
		public Static(ShaderProgram program)
		{
			this.program = program;
			isLoaded = true;
		}
		
		@Override
		public CompletableFuture<?> load()
		{
			return CompletableFuture.completedFuture(null);
		}
		
		@Override
		public CompletableFuture<?> reload()
		{
			return CompletableFuture.completedFuture(null);
		}
		
		@Override
		public void reinit(Params params) throws VulkanException
		{
			if (device != null && !device.equals(params.device)) throw new UnsupportedOperationException("Cannot change device after init");
			device = params.device;
			pStages = ShaderUtils.createPipelineShaderStageCreateInfoBuffer(params.device, program);
		}
		
		@Override
		public boolean validate(Params params) throws VulkanException
		{
			if (device != null && !device.equals(params.device)) throw new UnsupportedOperationException("Cannot change device after init");
			return false;
		}
		
		@Override
		public Properties getProperties()
		{
			return new Properties(program, pStages);
		}
		
		@Override
		public ShaderProgram getProgram()
		{
			return program;
		}
	}
}