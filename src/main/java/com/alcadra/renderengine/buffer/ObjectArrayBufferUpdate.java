package com.alcadra.renderengine.buffer;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryUtil;

import com.alcadra.renderengine.util.Tracker;
import com.alcadra.renderengine.util.update.UpdatedFrom;

@UpdatedFrom("ObjectArrayBuffer")
public class ObjectArrayBufferUpdate<E> extends SyncronizableArrayBuffer<IntBuffer> {
	
	private List<Holder> values;
	private List<Holder> modifieds;
	private IntBuffer oldLocalData;
	private IntBuffer oldNativeData;
	private boolean needResize;
	private int intSize;
	
	public ObjectArrayBufferUpdate()
	{
		values = new ArrayList<>();
		modifieds = new ArrayList<>();
		oldLocalData = MemoryUtil.memAllocInt(0);
		oldNativeData = MemoryUtil.memAllocInt(0);
	}
	
	public Tracker<int[]> add(E element, int[] elementData)
	{
		Tracker0 tracker = new Tracker0(new Holder(element, elementData));
		isModified |= values.add(tracker.holder);
		intSize += elementData.length;
		if (oldLocalData.capacity() < intSize) needResize = true;
		return tracker;
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
			if (!needResize)
			{
				int i = modifieds.indexOf(holder);
				if (i == -1) modifieds.add(holder);
				else modifieds.remove(i);
			}
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
			if (!needResize)
			{
				int i = modifieds.indexOf(holder);
				if (i == -1) modifieds.add(holder);
				else modifieds.remove(i);
			}
			isModified = true;
			holder.enabled = true;
			return true;
		}
		return false;
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
	
	public void merge(ObjectArrayBufferUpdate<E> arrayBuffer)
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
	public void sync()// effettua le modifiche senza poi azzerare la lista delle modifiche (è ancora vero?)
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
				for (Holder e : values) oldLocalData.put(e.enabled ? e.data : new int[e.data.length]);
				modifieds.clear();
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
	
	public class Tracker0 implements Tracker<int[]> {
		
		private Holder holder;
		private boolean valid;
		
		public Tracker0(Holder holder)
		{
			this.holder = holder;
			valid = true;
		}
		
		@Override
		public void modify(int[] data)
		{
			if (valid)
			{
				if (!needResize && !modifieds.contains(holder)) modifieds.add(holder);
				isModified |= holder.data != data;
				needResize |= holder.data.length != data.length;
				holder.data = data;
			}
		}
		
		@Override
		public boolean turnOn()
		{
			if (isOff())
			{
				if (!needResize && !modifieds.contains(holder)) modifieds.add(holder);
				isModified = true;
				holder.enabled = true;
				return true;
			}
			return false;
		}
		
		@Override
		public boolean turnOff()
		{
			if (isOn())
			{
				if (!needResize && !modifieds.contains(holder)) modifieds.add(holder);
				isModified = true;
				holder.enabled = false;
				return true;
			}
			return false;
		}
		
		@Override
		public void remove()
		{
			if (valid)
			{
				if (!needResize)
				{
					int i = modifieds.indexOf(holder);
					if (i > -1) modifieds.remove(i);
				}
				intSize -= holder.data.length;
				values.remove(holder);
				isModified = true;
				valid = false;
			}
		}
		
		@Override
		public boolean isOn()
		{
			return valid && holder.enabled;
		}
		
		@Override
		public boolean isOff()
		{
			return valid && !holder.enabled;
		}
		
		@Override
		public boolean isValid()
		{
			return valid;
		}
	}
}
