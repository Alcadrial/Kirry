package com.alcadrial.renderengine.render.shader.descriptor;

import org.lwjgl.vulkan.VK10;

import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.Device;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.util.RawDeviceResource;

/**
 * A set of descriptors 
 *
 */
public class DescriptorSet extends RawDeviceResource {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private DescriptorPool pool;
	
	public DescriptorSet(Device device, long vk, DescriptorPool pool)
	{
		super(device);
		this.pool = pool;
		this.vk = vk;
	}
	
	@Override
	public void close()
	{
		VK10.vkFreeDescriptorSets(device.getVk(), pool.getVk(), vk);
		super.close();
	}

	@Override
	public void debug()
	{
		Debug.debug("DescriptorPool: " + name);
	}

	@Override
	public void setName(String name)
	{
		device.setObjectDebugName(this, name);
		this.name = name;
	}
}
