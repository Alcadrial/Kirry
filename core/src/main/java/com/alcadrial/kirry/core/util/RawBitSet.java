package com.alcadrial.kirry.core.util;

import java.util.Arrays;

public class RawBitSet {
	
	public static RawBitSet of(int size)
	{
		if (size > Long.SIZE) return new RawBitSet(size);
		if (size > Integer.SIZE) return new RawBitSet64();
		return new RawBitSet32();
	}
	
	private static int bitValue(int i)
	{
		return 1 << i;
	}
	
	private static class RawBitSet32 extends RawBitSet {
		
		private int bits;
		
		@Override
		public boolean isOn(int i)
		{
			return (bits & 1 << i) != 0;
		}
		
		@Override
		public boolean isOff(int i)
		{
			return (bits & 1 << i) == 0;
		}
		
		@Override
		public boolean flip(int i)
		{
			int bitValue = 1 << i;
			bits ^= bitValue;
			return (bits & bitValue) == 0;
		}
		
		@Override
		public boolean set(int i, boolean state)
		{
			int bitValue = 1 << i;
			if (state)
			{
				bits |= bitValue;
				return (bits & bitValue) == 0;
			}
			bits &= ~bitValue;
			return (bits & bitValue) != 0;
		}
		
		@Override
		public boolean turnOff(int i)
		{
			int bitValue = 1 << i;
			bits &= ~bitValue;
			return (bits & bitValue) != 0;
		}
		
		@Override
		public boolean turnOn(int i)
		{
			int bitValue = 1 << i;
			bits |= bitValue;
			return (bits & bitValue) == 0;
		}
		
		@Override
		public void clear()
		{
			bits = 0;
		}
		
		@Override
		public String toHexString()
		{
			StringBuilder ss = new StringBuilder();
			for (int i = Integer.SIZE / 4 - 1; i >= 0; i--)
			{
				int value = 0;
				for (int k = 0; k < 4; k++) if ((bits & bitValue(4 * i + k)) != 0) value += 1 << k;
				ss.append("0123456789abcdef".charAt(value));
			}
			return ss.toString();
		}
		
		@Override
		public String toString()
		{
			StringBuilder ss = new StringBuilder();
			for (long l = 1 << (Integer.SIZE - 1); l > 0; l >>>= 1)
			{
				ss.append((bits & l) == 0 ? '0' : '1');
			}
			return ss.toString();
		}
	}
	
	private static class RawBitSet64 extends RawBitSet {
		
		private long bits;
		
		@Override
		public boolean isOn(int i)
		{
			return (bits & 1L << i) != 0;
		}
		
		@Override
		public boolean isOff(int i)
		{
			return (bits & 1L << i) == 0;
		}
		
		@Override
		public boolean flip(int i)
		{
			long bitValue = 1L << i;
			bits ^= bitValue;
			return (bits & bitValue) == 0;
		}
		
		@Override
		public boolean set(int i, boolean state)
		{
			long bitValue = 1L << i;
			if (state)
			{
				bits |= bitValue;
				return (bits & bitValue) == 0;
			}
			bits &= ~bitValue;
			return (bits & bitValue) != 0;
		}
		
		@Override
		public boolean turnOff(int i)
		{
			long bitValue = 1L << i;
			bits &= ~bitValue;
			return (bits & bitValue) != 0;
		}
		
		@Override
		public boolean turnOn(int i)
		{
			long bitValue = 1L << i;
			bits |= bitValue;
			return (bits & bitValue) == 0;
		}
		
		@Override
		public void clear()
		{
			bits = 0;
		}
		
		@Override
		public String toHexString()
		{
			StringBuilder ss = new StringBuilder();
			for (int i = Integer.SIZE / 4 - 1; i >= 0; i--)
			{
				int value = 0;
				for (int k = 0; k < 4; k++) if ((bits & bitValue(4 * i + k)) != 0) value += 1 << k;
				ss.append("0123456789abcdef".charAt(value));
			}
			return ss.toString();
		}
		
		@Override
		public String toString()
		{
			StringBuilder ss = new StringBuilder();
			for (long l = Long.SIZE - 1; l > 0; l--)
			{
				ss.append((bits & (1 << l)) == 0 ? '0' : '1');
			}
			return ss.toString();
		}
	}
	
	private int[] bits;
	
	protected RawBitSet()
	{}
	
	public RawBitSet(int size)
	{
		bits = new int[Math.ceilDiv(size, Integer.SIZE)];
	}
	
	public RawBitSet(RawBitSet other)
	{
		bits = Arrays.copyOf(other.bits, other.bits.length);
	}
	
	private int wordIndex(int i)
	{
		return i / Integer.SIZE;
	}
	
	private int getWord(int i)
	{
		return bits[wordIndex(i)];
	}
	
	public boolean isOn(int i)
	{
		return (getWord(i) & bitValue(i)) != 0;
	}
	
	public boolean isOff(int i)
	{
		return (getWord(i) & bitValue(i)) == 0;
	}
	
	public boolean flip(int i)
	{
		int wordIndex = wordIndex(i);
		int bitValue = bitValue(i);
		int word = bits[wordIndex];
		bits[wordIndex] = word ^ bitValue;
		return (word & bitValue) != 0;
	}
	
	public boolean set(int i, boolean state)
	{
		int wordIndex = wordIndex(i);
		int bitValue = bitValue(i);
		int word = bits[wordIndex];
		if (state)
		{
			bits[wordIndex] = word | bitValue;
			return (word & bitValue) == 0;
		}
		bits[wordIndex] = word & ~bitValue;
		return (word & bitValue) != 0;
	}
	
	public boolean turnOn(int i)
	{
		int wordIndex = wordIndex(i);
		int bitValue = bitValue(i);
		int word = bits[wordIndex];
		bits[wordIndex] = word | bitValue;
		return (word & bitValue) == 0;
	}
	
	public boolean turnOff(int i)
	{
		int wordIndex = wordIndex(i);
		int bitValue = bitValue(i);
		int word = bits[wordIndex];
		bits[wordIndex] = word & ~bitValue;
		return (word & bitValue) != 0;
	}
	
	public void clear()
	{
		Arrays.fill(bits, 0);
	}
	
	public String toHexString()
	{
		StringBuilder ss = new StringBuilder();
		for (int i = bits.length - 1; i >= 0; i--) for (int j = Integer.SIZE / 4 - 1; j >= 0; j--)
		{
			int value = 0;
			for (int k = 0; k < 4; k++) if ((bits[i] & bitValue(4 * j + k)) != 0) value += 1 << k;
			ss.append("0123456789abcdef".charAt(value));
		}
		return ss.toString();
	}
	
	@Override
	public String toString()
	{
		StringBuilder ss = new StringBuilder();
		for (int i = bits.length - 1; i >= 0; i--) for (long l = 1 << (Integer.SIZE - 1); l > 0; l >>>= 1)
		{
			ss.append((bits[i] & l) == 0 ? '0' : '1');
		}
		return ss.toString();
	}
}
