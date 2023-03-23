package com.alcadrial.renderengine;

public class SwappingBuffer {
	
	private final long imageAvailable;
	private final long renderFinished;
	private final long fence;
	
	public SwappingBuffer(long imageAvailable, long renderFinished, long fence)
	{
		this.imageAvailable = imageAvailable;
		this.renderFinished = renderFinished;
		this.fence = fence;
	}
	
	public long getImageAvailable()
	{
		return imageAvailable;
	}
	
	public long getRenderFinished()
	{
		return renderFinished;
	}
	
	public long getFence()
	{
		return fence;
	}
}
