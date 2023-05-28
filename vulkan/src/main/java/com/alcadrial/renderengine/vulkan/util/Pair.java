package com.alcadrial.renderengine.vulkan.util;

public class Pair<L, R> {
	
	private L left;
	private R rigth;
	
	public Pair(L left, R rigth)
	{
		this.left = left;
		this.rigth = rigth;
	}
	
	public L getLeft()
	{
		return left;
	}
	
	public void setLeft(L left)
	{
		this.left = left;
	}
	
	public R getRigth()
	{
		return rigth;
	}
	
	public void setRigth(R rigth)
	{
		this.rigth = rigth;
	}
	
	@Override
	public String toString()
	{
		return "Pair{left=" + left + ", rigth=" + rigth + "}";
	}
}
