package com.alcadrial.renderengine.render.util.future;

import org.jspecify.annotations.Nullable;

import com.alcadrial.renderengine.render.util.exception.VulkanException;
import com.alcadrial.renderengine.render.util.functional.VulkanRunnable;

public class AsyncRunnableTaskHandler extends AsyncTaskHandler<@Nullable Void> {
	
	private VulkanRunnable action;
	
	public AsyncRunnableTaskHandler(VulkanRunnable action)
	{
		this.action = action;
		start();
	}
	
	@Override
	protected @Nullable Void execute() throws VulkanException
	{
		action.run();
		return null;
	}
}