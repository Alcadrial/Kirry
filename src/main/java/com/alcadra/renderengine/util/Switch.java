package com.alcadra.renderengine.util;

import java.util.function.Supplier;

public class Switch {
	
	private Supplier<Boolean> activation;
	private Supplier<Boolean> deactivation;
	private boolean on;
	
	public Switch(Supplier<Boolean> activation, Supplier<Boolean> deactivation, boolean on)
	{
		this.activation = activation;
		this.deactivation = deactivation;
		this.on = on;
	}
	
	public boolean turnOn()
	{
		on = activation.get();
		return on;
	}
	
	public boolean turnOff()
	{
		on = !deactivation.get();
		return !on;
	}
	
	public boolean turn()
	{
		return on ? turnOff() : turnOn();
	}
	
	public boolean isOn()
	{
		return on;
	}
	
	public boolean isOff()
	{
		return !on;
	}
}
