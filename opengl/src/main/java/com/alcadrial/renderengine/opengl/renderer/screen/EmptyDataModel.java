package com.alcadrial.renderengine.opengl.renderer.screen;

import java.nio.ByteBuffer;

public class EmptyDataModel extends DataModel {
	
	EmptyDataModel()
	{
		
	}
	
	@Override
	protected ByteBuffer nativeBuffer()
	{
		return null;
	}
	
	@Override
	protected ByteBuffer nativeGpuBuffer()
	{
		return null;
	}
	
	@Override
	protected boolean equals(ByteBuffer buffer, ByteBuffer other)
	{
		return false;
	}
}
