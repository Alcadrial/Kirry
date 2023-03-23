package com.alcadrial.renderengine.buffer;

import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.List;

public class FloatArrayBufferStack extends FloatArrayBuffer {
	
	private Node head;
	private Node top;
	private int size;
	
	public void push(FloatArrayBuffer buffer)
	{
		size++;
		if (head == null)
		{
			head = top = new Node(null, buffer);
			return;
		}
		top.next = new Node(null, buffer);
		top = top.next;
	}
	
	public FloatArrayBuffer current()
	{
		return top != null ? top.buffer : this;
	}
	
	public FloatArrayBuffer pull()
	{
		FloatArrayBuffer current = current();
		if (top != null)
		{
			size--;
			if (head == top)
			{
				head = top = null;
				return current;
			}
			Node n = head;
			while (n.next != top) n = n.next;
			top = n;
			top.next = null;
		}
		return current;
	}
	
	public int stackSize()
	{
		return size;
	}
	
	@Override
	public void add(float f)
	{
		if (top != null) top.buffer.add(f);
		else super.add(f);
	}
	
	@Override
	public void addAll(Collection<Float> f)
	{
		if (top != null) top.buffer.addAll(f);
		else super.addAll(f);
	}
	
	@Override
	public void addAll(float[] f)
	{
		if (top != null) top.buffer.addAll(f);
		else super.addAll(f);
	}
	
	@Override
	public float getFloat(int index)
	{
		if (top != null) return top.buffer.getFloat(index);
		return super.getFloat(index);
	}
	
	@Override
	public void merge(FloatArrayBuffer arrayBuffer)
	{
		if (top != null) top.buffer.merge(arrayBuffer);
		else super.merge(arrayBuffer);
	}
	
	@Override
	public void clear()
	{
		if (top != null) top.buffer.clear();
		else super.clear();
	}
	
	@Override
	public List<Float> asList()
	{
		return top != null ? top.buffer.asList() : super.asList();
	}
	
	@Override
	public int size()
	{
		return top != null ? top.buffer.size() : super.size();
	}
	
	@Override
	public void sync()
	{
		if (top != null) top.buffer.sync();
		else super.sync();
	}
	
	@Override
	protected FloatBuffer nativeBuffer()
	{
		return top != null ? top.buffer.nativeBuffer() : super.nativeBuffer();
	}
	
	@Override
	protected FloatBuffer nativeGlBuffer()
	{
		return top != null ? top.buffer.nativeGlBuffer() : super.nativeGlBuffer();
	}
	
	@Override
	protected void syncRewrite(FloatBuffer buffer)
	{
		if (top != null) top.buffer.syncRewrite(buffer);
		else super.syncRewrite(buffer);
	}
	
	@Override
	protected void syncModify(FloatBuffer buffer, int from, int to)
	{
		if (top != null) top.buffer.syncModify(buffer, from, to);
		else super.syncModify(buffer, from, to);
	}
	
	@Override
	public float[] getGlFloatData()
	{
		return top != null ? top.buffer.getGlFloatData() : super.getGlFloatData();
	}
	
	@Override
	public int getId()
	{
		return top != null ? top.buffer.getId() : super.getId();
	}
	
	@Override
	public int getTarget()
	{
		return top != null ? top.buffer.getTarget() : super.getTarget();
	}
	
	@Override
	public String toString()
	{
		return "FloatArrayBufferStack{size=" + size + ", topValues=" + (top != null ? top.buffer : this).values + "}";
	}
	
	private class Node {
		
		Node next;
		FloatArrayBuffer buffer;
		
		public Node(Node next, FloatArrayBuffer buffer)
		{
			this.next = next;
			this.buffer = buffer;
		}
	}
}
