package com.alcadra.renderengine.util;

import com.alcadra.renderengine.Launch;

public class Logger {
	
	public void error(String log, Exception e, Object... objects)
	{
		log(newBlock("ERROR") + log, objects);
		e.printStackTrace(System.out);
	}
	
	public void error(String log, Exception e)
	{
		log(newBlock("ERROR") + log);
		e.printStackTrace(System.out);
	}
	
	public void error(String log)
	{
		log(newBlock("ERROR") + log);
	}
	
	public void error(Object... objects)
	{
		String log = newBlock("ERROR");
		for (int i = 0; i < objects.length; i++) log += "{} ";
		log(log, objects);
	}
	
	public void error(String log, Object... objects)
	{
		log(newBlock("ERROR") + log, objects);
	}
	
	public void debug(String log)
	{
		if (Launch.isDebug()) log(newBlock("DEBUG") + log);
	}
	
	public void debug(Object... objects)
	{
		if (Launch.isDebug())
		{
			String log = newBlock("DEBUG");
			for (int i = 0; i < objects.length; i++) log += "{} ";
			log(log, objects);
		}
	}
	
	public void debug(String log, Object... objects)
	{
		if (Launch.isDebug()) log(newBlock("DEBUG") + log, objects);
	}
	
	public void warn(String log)
	{
		log(newBlock("WARN") + log);
	}
	
	public void warn(Object... objects)
	{
		String log = newBlock("WARN");
		for (int i = 0; i < objects.length; i++) log += "{} ";
		log(log, objects);
	}
	
	public void warn(String log, Object... objects)
	{
		log(newBlock("WARN") + log, objects);
	}
	
	public void info(String log)
	{
		log(newBlock("INFO") + log);
	}
	
	public void info(Object... objects)
	{
		String log = newBlock("INFO");
		for (int i = 0; i < objects.length; i++) log += "{} ";
		log(log, objects);
	}
	
	public void info(String log, Object... objects)
	{
		log(newBlock("INFO") + log, objects);
	}
	
	public void log(String log, Object... objects)
	{
		logBase(newBlock(time()) + newBlock(Thread.currentThread().getName() + "/Thread") + log, objects);
	}
	
	private void logBase(String log, Object[] objects)
	{
		System.out.println(Util.newParamerizedString(log, "{}", objects));
	}
	
	private String newBlock(String logblock)
	{
		return "[" + logblock + "] ";
	}
	
	private String time()
	{
		int h = hours();
		int m = minutes();
		int s = seconds();
		String time = "";
		if (h < 10) time += "0";
		time += h + ":";
		if (m < 10) time += "0";
		time += m + ":";
		if (s < 10) time += "0";
		time += s;
		return time;
	}
	
	private int hours()
	{
		return (int) ((currentTimeSecond() / 3600 + 2) % 24);
	}
	
	private int minutes()
	{
		return (int) (currentTimeSecond() / 60 % 60);
	}
	
	private int seconds()
	{
		return (int) (currentTimeSecond() % 60);
	}
	
	private long currentTimeSecond()
	{
		return System.currentTimeMillis() / 1000;
	}
}
