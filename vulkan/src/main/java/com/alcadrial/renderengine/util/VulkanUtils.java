package com.alcadrial.renderengine.util;

import java.nio.ByteBuffer;
import java.util.function.Function;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Struct;

public class VulkanUtils {
	
	public static <S extends Struct> Pair<S, ByteBuffer> copy(S struct, Function<Long, S> builder)
	{
		ByteBuffer buffer = MemoryUtil.memByteBuffer(struct.address(), struct.sizeof());
		ByteBuffer copyBuffer = MemoryUtil.memCalloc(struct.sizeof());
		S copyStruct = builder.apply(MemoryUtil.memAddress(copyBuffer));
		copyBuffer.put(buffer);
		return new Pair<>(copyStruct, copyBuffer);
	}
	
	public static <S extends Struct> Pair<S, ByteBuffer> allocate(int size, Function<Long, S> builder)
	{
		ByteBuffer buffer = MemoryUtil.memCalloc(size);
		return new Pair<>(builder.apply(MemoryUtil.memAddress(buffer)), buffer);
	}
}
