package com.alcadrial.kirry.core.util;

import java.util.Objects;

public class Pair<L, R> {
	
	public L left;
	public R right;
	
	public Pair(L left, R right)
	{
		this.left = left;
		this.right = right;
	}
	
	/**
	 * 
	 * @return the left value
	 * @see Pair#getLeft()
	 */
	public L l()
	{
		return left;
	}
	
	public L getLeft()
	{
		return left;
	}
	
	public void setLeft(L left)
	{
		this.left = left;
	}
	
	/**
	 * 
	 * @return the right value
	 * @see Pair#getRight()
	 */
	public R r()
	{
		return right;
	}
	
	public R getRight()
	{
		return right;
	}
	
	public void setRight(R right)
	{
		this.right = right;
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(left, right);
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		return obj instanceof Pair p && Objects.equals(left, p.left) && Objects.equals(right, p.right);
	}
	
	@Override
	public String toString()
	{
		return "Pair{left=" + left + ", right=" + right + "}";
	}
}
