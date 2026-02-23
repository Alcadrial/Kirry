package com.alcadrial.renderengine.render.shader;

import java.util.List;

import com.alcadrial.renderengine.render.RenderEngine;

public class ShaderBufferView {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	private static class Entry {
		
		private ShaderVariable variable;
		private int offset;
		
		public Entry(ShaderVariable variable, int offset)
		{
			this.variable = variable;
			this.offset = offset;
		}
	}
	
	private Entry[] entries;
	
	public ShaderBufferView(List<ShaderVariable> variables)
	{
		entries = new Entry[variables.size()];
		int i = 0;
		int offset = 0;
		for (ShaderVariable variable : variables)
		{
			entries[i++] = new Entry(variable, offset);
			offset += variable.getType().getLength();
		}
	}
	
	public ShaderBufferView(ShaderVariable... variables)
	{
		entries = new Entry[variables.length];
		int i = 0;
		int offset = 0;
		for (ShaderVariable variable : variables)
		{
			entries[i++] = new Entry(variable, offset);
			offset += variable.getType().getLength();
		}
	}
}
