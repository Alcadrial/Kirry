package com.alcadra.renderengine.buffer;

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
		return top.buffer;
	}
	
	public void pull()
	{
		if (top != null)
		{
			size--;
			if (head == top)
			{
				head = top = null;
				return;
			}
			Node n = head;
			while (n.next != top) n = n.next;
			top = n;
			top.next = null;
		}
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
	public void bind()
	{
		if (top != null) top.buffer.bind();
		else super.bind();
	}
	
	@Override
	public void sync()
	{
		if (top != null) top.buffer.sync();
		else super.sync();
	}
	
	@Override
	public String toString()
	{
		return "FloatArrayBufferStack{size=" + size + ", topValues=" + top.buffer.values + "}";
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
