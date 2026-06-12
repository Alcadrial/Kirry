package com.alcadrial.kirry.core.buffer;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;

public abstract class SynchronizableArrayBuffer extends ArrayBuffer {
	
	protected SynchronizableArrayBuffer(int target, int id)
	{
		super(target, id);
	}
	
	protected abstract ByteBuffer nativeBuffer();
	protected abstract ByteBuffer nativeGpuBuffer();
	
	protected abstract void syncRewrite(ByteBuffer buffer);
	protected abstract void syncModify(ByteBuffer buffer, int from, int to);
	
	protected abstract boolean equals(ByteBuffer buffer, ByteBuffer other);
	
	public void sync()
	{
		if (isModified)
		{
			ByteBuffer buffer = nativeBuffer();
			syncRewrite(buffer);
			MemoryUtil.memFree(buffer);
			isModified = false;
		}
	}
}
