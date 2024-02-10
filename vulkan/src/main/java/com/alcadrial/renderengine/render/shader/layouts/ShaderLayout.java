package com.alcadrial.renderengine.render.shader.layouts;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.vulkan.EXTMutableDescriptorType;
import org.lwjgl.vulkan.QCOMImageProcessing;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK13;

import com.alcadrial.renderengine.render.RenderEngine;

public abstract sealed class ShaderLayout permits ShaderBufferLayout, ShaderLocalSizeLayout, ShaderInputLayout {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public enum Type {
		
		SAMPLER("sampler", VK10.VK_DESCRIPTOR_TYPE_SAMPLER, false, false, false), // placeholder
		COMBINED_IMAGE_SAMPLER("combinedImageSampler", VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, true, true, false),
		SAMPLED_IMAGE("sampledImage", VK10.VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE, false, false, false), // placeholder
		STORAGE_IMAGE("storageImage", VK10.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE, false, false, false), // placeholder
		UNIFORM_TEXEL_BUFFER("uniformTexelBuffer", VK10.VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER, false, false, false), // placeholder
		STORAGE_TEXEL_BUFFER("storageTexelBuffer", VK10.VK_DESCRIPTOR_TYPE_STORAGE_TEXEL_BUFFER, false, false, false), // placeholder
		UNIFORM_BUFFER("uniformBuffer", VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, true, true, false),
		STORAGE_BUFFER("storageBuffer", VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER, true, true, true),
		UNIFORM_BUFFER_DYNAMIC("uniformBufferDynamic", VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC, false, false, false), // placeholder
		STORAGE_BUFFER_DYNAMIC("storageBufferDynamic", VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER_DYNAMIC, false, false, false), // placeholder
		INPUT_ATTACHMENT("inputAttachment", VK10.VK_DESCRIPTOR_TYPE_INPUT_ATTACHMENT, false, false, false), // placeholder
		INLINE_UNIFORM_BLOCK("inlineUniformBlock", VK13.VK_DESCRIPTOR_TYPE_INLINE_UNIFORM_BLOCK, false, false, false), // placeholder
		MUTABLE_EXT("mutableExt", EXTMutableDescriptorType.VK_DESCRIPTOR_TYPE_MUTABLE_EXT, false, false, false), // placeholder
		SAMPLE_WEIGHT_IMAGE_QCOM("sampleWeightImageQcom", QCOMImageProcessing.VK_DESCRIPTOR_TYPE_SAMPLE_WEIGHT_IMAGE_QCOM, false, false, false), // placeholder
		BLOCK_MATCH_IMAGE_QCOM("blockMatchImageQcom", QCOMImageProcessing.VK_DESCRIPTOR_TYPE_BLOCK_MATCH_IMAGE_QCOM, false, false, false), // placeholder
		
		INPUT("input", 0, true, false, false),
		LOCAL_SIZE("localSize", 0, false, false, false);
		
		private static final Map<String, Type> OF_MAP;
		
		static
		{
			Map<String, Type> map = new HashMap<>();
			for (Type type : values()) map.put(type.name, type);
			OF_MAP = Map.copyOf(map);
		}
		
		public static Type of(String typeName)
		{
			return OF_MAP.get(typeName);
		}
		
		private String name;
		private int type;
		private boolean hasBinding;
		private boolean hasName;
		private boolean hasUsage;
		
		Type(String name, int type, boolean hasBinding, boolean hasName, boolean hasUsage)
		{
			this.name = name;
			this.type = type;
			this.hasBinding = hasBinding;
			this.hasName = hasName;
			this.hasUsage = hasUsage;
		}
		
		public String getName()
		{
			return name;
		}
		
		public int getType()
		{
			return type;
		}
		
		public boolean hasBinding()
		{
			return hasBinding;
		}
		
		public boolean hasName()
		{
			return hasName;
		}
		
		public boolean hasUsage()
		{
			return hasUsage;
		}
	}
	
	private final Type type;
	
	protected ShaderLayout(Type type)
	{
		this.type = type;
	}
	
	public Type getType()
	{
		return type;
	}
}
