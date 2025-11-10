package com.alcadrial.renderengine.render;

import java.util.AbstractSequentialList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import com.alcadrial.renderengine.render.util.VulkanDebug;

public abstract class VulkanObject implements AutoCloseable, VulkanDebug {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private static final ConcurrentStack<VulkanObject> VULKAN_INSTANCE_LIST = new ConcurrentStack<>();
	static final List<VulkanObject> VULKAN_INSTANCE_IMMUTABLE_LIST = Collections.unmodifiableList(VULKAN_INSTANCE_LIST);
	
	public VulkanObject()
	{
		VULKAN_INSTANCE_LIST.addFirst(this);
	}
	
	@Override
	public void close()
	{
		if(!VULKAN_INSTANCE_LIST.remove(this) && Debug.isEnabledDeallocationFail())
			throw new IllegalStateException(("Failed to remove resource " + this + "," + VULKAN_INSTANCE_LIST.toString()).replaceAll("[\\[,\\]]", "\n"));
	}
	
	private static final class ConcurrentStack<E> extends AbstractSequentialList<E> {
		
		private class Node {
			
			Node next;
			E data;
			
			public Node(ConcurrentStack<E>.Node next)
			{
				this.next = next;
			}
		}
		
		private Node head;
		private int size;
		
		@Override
		public boolean remove(Object o)
		{
			if (o == null) return false;
			Iterator it = new Iterator();
			while (it.hasNext()) if (o.equals(it.next()))
			{
				it.remove();
				return true;
			}
			return false;
		}
		
		private class Iterator implements ListIterator<E> {
			
			Node p = null;
			Node n = new Node(head);
			
			@Override
			public boolean hasNext()
			{
				return n.next != null;
			}
			
			@Override
			public E next()
			{
				p = n;
				n = n.next;
				return n.data;
			}
			
			@Override
			public int nextIndex()
			{
				return 0;
			}
			
			@Override
			public void remove()
			{
				Node next = n.next;
				synchronized (ConcurrentStack.this)
				{
					if (n == head) head = next;
					p.next = next;
					size--;
				}
				n = next;
			}
			
			@Override
			public void set(E e)
			{
				n.data = e;
			}
			
			@Override
			public void add(E e)
			{
				Node m = new Node(n.next);
				m.data = e;
				synchronized (ConcurrentStack.this)
				{
					if (n.next == head) head = m;
					n.next = m;
					size++;
				}
			}
			
			@Override
			public boolean hasPrevious()
			{
				return false;
			}
			
			@Override
			public E previous()
			{
				throw new UnsupportedOperationException();
			}
			
			@Override
			public int previousIndex()
			{
				return -1;
			}
		}
		
		@Override
		public int size()
		{
			return size;
		}
		
		@Override
		public ListIterator<E> listIterator(int index)
		{
			Iterator iterator = new Iterator();
			for (int i = 0; i < index; i++) iterator.next();
			return iterator;
		}
	}
}
