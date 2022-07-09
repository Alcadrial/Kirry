package com.alcadra.renderengine.buffer;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryUtil;

public class ObjectArrayBuffer<E> extends ArrayBuffer<IntBuffer> {
	
	private List<Holder> values;
	private List<Holder> modifieds;
	private IntBuffer oldLocalData;
	private IntBuffer oldNativeData;
	private boolean needResize;
	private int intSize;
	
	public ObjectArrayBuffer()
	{
		values = new ArrayList<>();
		modifieds = new ArrayList<>();
		oldLocalData = MemoryUtil.memAllocInt(0);
		oldNativeData = MemoryUtil.memAllocInt(0);
	}
	
	public boolean add(E element, int[] elementData)
	{
		isModified |= values.add(new Holder(element, elementData));
		intSize += elementData.length;
		if (oldLocalData.capacity() < intSize) needResize = true;
		return isModified;
	}
	
	public void add(int index, E element, int[] elementData)
	{
		isModified = true;
		values.add(index, new Holder(element, elementData));
		intSize += elementData.length;
		if (oldLocalData.capacity() < intSize) needResize = true;
	}
	
	public E get(int index)
	{
		return values.get(index).element;
	}
	
	public boolean disable(int index)
	{
		Holder holder = values.get(index);
		if (holder.enabled)
		{
			int i = modifieds.indexOf(holder);
			if (i == -1) modifieds.add(holder);
			else modifieds.remove(i);
			isModified = true;
			holder.enabled = false;
			return true;
		}
		return false;
	}
	
	public boolean enable(int index)
	{
		Holder holder = values.get(index);
		if (!holder.enabled)
		{
			int i = modifieds.indexOf(holder);
			if (i == -1) modifieds.add(holder);
			else modifieds.remove(i);
			isModified = true;
			holder.enabled = true;
			return true;
		}
		return false;
	}
	
	public E remove(int index)
	{
		isModified = true;
		Holder holder = values.remove(index);
		intSize -= holder.data.length;
		if (oldLocalData.capacity() >= intSize) needResize = false;
		return values.remove(index).element;
	}
	
	public boolean isEmpty()
	{
		return values.isEmpty();
	}
	
	public boolean contains(E element)
	{
		return indexOf(element) != -1;
	}
	
	public int indexOf(E element)
	{
		for (int i = 0; i < values.size(); i++) if (values.get(i).element.equals(element)) return i;
		return -1;
	}
	
	public int lastIndexOf(E element)
	{
		int j = -1;
		for (int i = 0; i < values.size(); i++) if (values.get(i).element.equals(element)) j = i;
		return j;
	}
	
	@Override
	public void clear()
	{
		values.clear();
		intSize = 0;
		isModified = true;
	}
	
	public Stream<E> stream()
	{
		return values.stream().map(h -> h.element);
	}
	
	public Stream<E> parallelStream()
	{
		return values.parallelStream().map(h -> h.element);
	}
	
	public void merge(ObjectArrayBuffer<E> arrayBuffer)
	{
		values.addAll(arrayBuffer.values);
		isModified = !arrayBuffer.isEmpty();
	}
	
	@Override
	public int size()
	{
		return values.size();
	}
	
	public int intSize()
	{
		return intSize;
	}
	
	@Override
	public int byteSize()
	{
		return intSize * 4;
	}
	
	@Override
	public void sync()
	{
		if (isModified)
		{
			if (needResize)
			{
				syncRewrite(nativeBuffer());
				needResize = false;
			}
			else
			{
				List<Holder> l = List.copyOf(modifieds);
				IntBuffer buffer = nativeBuffer();
				for (Holder e : l) syncModify(buffer, e.ints, e.ints + e.data.length);
			}
			isModified = false;
		}
	}
	
	@Override
	protected IntBuffer nativeBuffer()
	{
		if (isModified)
		{
			if (needResize)
			{
				MemoryUtil.memFree(oldLocalData);
				oldLocalData = MemoryUtil.memAllocInt(intSize);
				for (Holder holder : values) oldLocalData.put(holder.data);
				return oldLocalData.flip();
			}
			for (Holder e : modifieds) oldLocalData.put(e.ints, e.enabled ? e.data : new int[e.data.length]);
			modifieds.clear();
		}
		return oldLocalData;
	}
	
	@Override
	protected IntBuffer nativeGlBuffer()
	{
		if (isModified)
		{
			MemoryUtil.memFree(oldNativeData);
			oldNativeData = MemoryUtil.memAllocInt(byteSize() / 4);
			GL15.glGetBufferSubData(getTarget(), 0, oldNativeData);
			oldNativeData.flip();
		}
		return oldNativeData;
	}
	
	@Override
	protected void syncRewrite(IntBuffer buffer)
	{
		GL15.glBufferData(getTarget(), buffer, GL15.GL_DYNAMIC_DRAW);
	}
	
	@Override
	protected void syncModify(IntBuffer buffer, int from, int to)
	{
		GL15.glBufferSubData(getTarget(), from * 4, buffer.duplicate().limit(to).position(from));
	}
	
	@Override
	protected boolean equals(IntBuffer buffer, IntBuffer other)
	{
		return buffer.get() == other.get();
	}
	
	@Override
	protected int elementSize()
	{
		return 4;
	}
	
	@Override
	public String toString()
	{
		return "ObjectArrayBuffer{values=" + Arrays.deepToString(values.stream().map(h -> h.data).toArray()) + "}";
	}
	
	private class Holder {
		
		E element;
		boolean enabled;
		int[] data;
		int ints;
		
		Holder(E element, int[] data)
		{
			this.element = element;
			enabled = true;
			this.data = data;
			ints = intSize;
		}
	}
}
