package com.alcadrial.renderengine.buffer;

import java.util.Collection;

public class ElementArrayBufferStack extends ElementArrayBuffer {
	
	private Node head;
	private Node top;
	private int size;
	
	public void push(ElementArrayBuffer buffer)
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
	
	public ElementArrayBuffer current()
	{
		return top != null ? top.buffer : this;
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
	public void add(int index)
	{
		if (top != null) top.buffer.add(index);
		else super.add(index);
	}
	
	@Override
	public void addAll(Collection<Integer> indices)
	{
		if (top != null) top.buffer.addAll(indices);
		else super.addAll(indices);
	}
	
	@Override
	public void addAll(int[] indices)
	{
		if (top != null) top.buffer.addAll(indices);
		else super.addAll(indices);
	}
	
	@Override
	public void addAll(ElementArrayBuffer elementArrayBuffer)
	{
		if (top != null) top.buffer.addAll(elementArrayBuffer);
		else super.addAll(elementArrayBuffer);
	}
	
	@Override
	public int get(int index)
	{
		if (top != null) return top.buffer.get(index);
		return super.get(index);
	}
	
	@Override
	public void merge(ElementArrayBuffer elementBuffer, int offset)
	{
		if (top != null) top.buffer.merge(elementBuffer, offset);
		else super.merge(elementBuffer, offset);
	}
	
	@Override
	public void clear()
	{
		if (top != null) top.buffer.clear();
		else super.clear();
	}
	
	@Override
	public int getMax()
	{
		return top != null ? top.buffer.getMax() : super.getMax();
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
	protected void syncModify()
	{
		if (top != null) top.buffer.syncModify();
		else super.syncModify();
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
		return "ElementArrayBufferStack{size=" + size + ", topValues=" + (top != null ? top.buffer : this).values + "}";
	}
	
	private class Node {
		
		Node next;
		ElementArrayBuffer buffer;
		
		public Node(Node next, ElementArrayBuffer buffer)
		{
			this.next = next;
			this.buffer = buffer;
		}
	}
}
