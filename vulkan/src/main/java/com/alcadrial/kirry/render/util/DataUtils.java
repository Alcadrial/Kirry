package com.alcadrial.kirry.render.util;

public class DataUtils {
	
	public DataUtils()
	{}
	
	public static final String getBufferUsage(int usage)
	{
		if (usage == 0) return "";
		final String[] codes = {"SRC", "DST", "UT", "ST", "U", "S", "I", "V", "N"};
		StringBuilder ss = new StringBuilder(22);
		for (int i = 9; i >= 0; i--) if ((usage & (1 << i)) != 0) ss.append(codes[i]).append("|");
		return ss.substring(0, ss.length() - 1);
	}
}
