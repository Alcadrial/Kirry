package com.alcadrial.renderengine.buffer;

import java.nio.Buffer;

import org.lwjgl.system.MemoryUtil;

public abstract class SyncronizableArrayBuffer<B extends Buffer> extends ArrayBuffer<B> {
	
	protected abstract B nativeBuffer();
	protected abstract B nativeGlBuffer();
	
	protected abstract void syncRewrite(B buffer);
	protected abstract void syncModify(B buffer, int from, int to);
	
	protected abstract boolean equals(B buffer, B other);
	
	public void sync()
	{
		if (isModified)
		{
			/*
			@off
			int byteSize = getGlByteSize();
			if (byteSize != byteSize())
			{
			@on
			*/
			B buffer = nativeBuffer();
			syncRewrite(buffer);
			MemoryUtil.memFree(buffer);
			/*
			@off
			}
			else
			{
				B local = nativeBuffer();
				B buffer = nativeGlBuffer();
				int start = -1;
				int size = size();
				for (int i = 0; i < size; i++)
				{
					if (!equals(buffer, local))
					{
						if (start == -1) start = i;
					}
					else if (start != -1)
					{
						syncModify(local, start, i);
						local.limit(size).position(i);
						start = -1;
					}
				}
				MemoryUtil.memFree(local);
				MemoryUtil.memFree(buffer);
			}
			@on
			*/
			isModified = false;
		}
	}
}
