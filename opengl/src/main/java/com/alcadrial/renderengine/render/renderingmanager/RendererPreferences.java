package com.alcadrial.renderengine.render.renderingmanager;

public class RendererPreferences {
	
	public static Builder create()
	{
		return new Builder();
	}
	
	public static final class Builder {
		
		private boolean autoSync;
		
		private Builder()
		{
			
		}
		
		public Builder autoSync(boolean autoSync)
		{
			this.autoSync = autoSync;
			return this;
		}
		
		public RendererPreferences build()
		{
			return new RendererPreferences(autoSync);
		}
	}
	
	private final boolean autoSync;
	
	private RendererPreferences(boolean autoSync)
	{
		this.autoSync = autoSync;
	}
	
	public boolean autoSync()
	{
		return autoSync;
	}
}
