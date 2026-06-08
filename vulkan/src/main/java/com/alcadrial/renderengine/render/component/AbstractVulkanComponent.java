package com.alcadrial.renderengine.render.component;

import com.alcadrial.renderengine.core.component.AbstractComponent;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public abstract class AbstractVulkanComponent<I, P> extends AbstractComponent<I, P, VulkanException> {
	
	public AbstractVulkanComponent()
	{}
}
