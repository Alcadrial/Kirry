package com.alcadrial.kirry.render.font;


public interface StringFormatter extends TextFormatter<String> {
	
	static DefaultStringFormatter ofDefault()
	{
		return new DefaultStringFormatter();
	}
}
