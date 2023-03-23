package com.alcadra.renderengine.util;

public interface Switch {
	
	boolean turnOn();
	boolean turnOff();
	
	default boolean enable()
	{
		return turnOn();
	}
	
	default boolean disable()
	{
		return turnOff();
	}
	
	default boolean turn()
	{
		return isOn() ? turnOff() : turnOn();
	}
	
	boolean isOn();
	
	default boolean isOff()
	{
		return !isOn();
	}
	
	default boolean isEnabled()
	{
		return isOn();
	}
	
	default boolean isDisabled()
	{
		return isOff();
	}
}