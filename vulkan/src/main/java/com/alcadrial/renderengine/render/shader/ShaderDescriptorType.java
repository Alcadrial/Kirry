package com.alcadrial.renderengine.render.shader;

import org.lwjgl.vulkan.EXTMutableDescriptorType;
import org.lwjgl.vulkan.QCOMImageProcessing;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK13;

import com.alcadrial.renderengine.render.RenderEngine;

public enum ShaderDescriptorType {
	
	SAMPLER("sampler", VK10.VK_DESCRIPTOR_TYPE_SAMPLER),
	COMBINED_IMAGE_SAMPLER("combinedImageSampler", VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER),
	SAMPLED_IMAGE("sampledImage", VK10.VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE),
	STORAGE_IMAGE("storageImage", VK10.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE),
	UNIFORM_TEXEL_BUFFER("uniformTexelBuffer", VK10.VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER),
	STORAGE_TEXEL_BUFFER("storageTexelBuffer", VK10.VK_DESCRIPTOR_TYPE_STORAGE_TEXEL_BUFFER),
	UNIFORM_BUFFER("uniformBuffer", VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER),
	STORAGE_BUFFER("storageBuffer", VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER),
	UNIFORM_BUFFER_DYNAMIC("uniformBufferDynamic", VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC),
	STORAGE_BUFFER_DYNAMIC("storageBufferDynamic", VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER_DYNAMIC),
	INPUT_ATTACHMENT("inputAttachment", VK10.VK_DESCRIPTOR_TYPE_INPUT_ATTACHMENT),
	INLINE_UNIFORM_BLOCK("inlineUniformBlock", VK13.VK_DESCRIPTOR_TYPE_INLINE_UNIFORM_BLOCK),
	MUTABLE_EXT("mutableExt", EXTMutableDescriptorType.VK_DESCRIPTOR_TYPE_MUTABLE_EXT),
	SAMPLE_WEIGHT_IMAGE_QCOM("sampleWeightImageQcom", QCOMImageProcessing.VK_DESCRIPTOR_TYPE_SAMPLE_WEIGHT_IMAGE_QCOM),
	BLOCK_MATCH_IMAGE_QCOM("blockMatchImageQcom", QCOMImageProcessing.VK_DESCRIPTOR_TYPE_BLOCK_MATCH_IMAGE_QCOM);
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public static ShaderDescriptorType of(String typeName)
	{
		for (ShaderDescriptorType type : values()) if (type.name.equals(typeName)) return type;
		return null;
	}
	
	private String name;
	private int type;
	
	ShaderDescriptorType(String name, int type)
	{
		this.name = name;
		this.type = type;
	}
	
	public String getName()
	{
		return name;
	}
	
	public int getType()
	{
		return type;
	}
}