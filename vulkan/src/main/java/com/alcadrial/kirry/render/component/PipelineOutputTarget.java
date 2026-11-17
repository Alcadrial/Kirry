package com.alcadrial.kirry.render.component;

import com.alcadrial.kirry.render.FrameBuffer;
import com.alcadrial.kirry.render.window.VulkanWindow;

public sealed interface PipelineOutputTarget permits PipelineOutputTarget.SurfaceTarget, PipelineOutputTarget.TextureTarget {
	
	public record SurfaceTarget(VulkanWindow window) implements PipelineOutputTarget {}
	
	public record TextureTarget(FrameBuffer frameBuffer) implements PipelineOutputTarget {}
}
