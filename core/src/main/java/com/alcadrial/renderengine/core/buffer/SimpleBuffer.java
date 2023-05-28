package com.alcadrial.renderengine.core.buffer;

public abstract class SimpleBuffer implements AutoCloseable {
	
	private final int target, id;
	
	protected SimpleBuffer(int target, int id)
	{
		this.target = target;
		this.id = id;
	}
	
	public abstract void bind();
	
	public void asBinded(Runnable runnable)
	{
		bind();
		runnable.run();
		unbind();
	}
	
	public abstract void unbind();
	
	public int getTarget()
	{
		return target;
	}
	
	public int getId()
	{
		return id;
	}
	
	@Override
	public abstract void close();
}
