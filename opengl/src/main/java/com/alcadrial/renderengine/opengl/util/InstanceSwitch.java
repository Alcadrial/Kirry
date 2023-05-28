package com.alcadrial.renderengine.opengl.util;

import java.util.function.Supplier;

public class InstanceSwitch implements Switch {
	
	protected Supplier<Boolean> activation;
	protected Supplier<Boolean> deactivation;
	protected boolean on;
	
	public InstanceSwitch(Supplier<Boolean> activation, Supplier<Boolean> deactivation, boolean on)
	{
		this.activation = activation;
		this.deactivation = deactivation;
		this.on = on;
	}
	
	@Override
	public boolean turnOn()
	{
		on = activation.get();
		return on;
	}
	
	@Override
	public boolean turnOff()
	{
		on = !deactivation.get();
		return !on;
	}
	
	@Override
	public boolean isOn()
	{
		return on;
	}
}
