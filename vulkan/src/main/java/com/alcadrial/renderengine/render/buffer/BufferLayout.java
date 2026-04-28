package com.alcadrial.renderengine.render.buffer;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.collections.api.factory.Maps;
import org.jspecify.annotations.Nullable;

import com.alcadrial.renderengine.render.Debug;
import com.alcadrial.renderengine.render.RenderEngineVulkan;
import com.alcadrial.renderengine.render.shader.ShaderVariable;
import com.alcadrial.renderengine.render.util.VulkanDebug;

public class BufferLayout implements VulkanDebug {
	
	static
	{
		RenderEngineVulkan.assertLoaded();
	}
	
	private final Map<ShaderVariable, Integer> marks;
	
	public BufferLayout()
	{
		marks = Maps.mutable.empty();
	}
	
	public void mark(ShaderVariable variable, int offset)
	{
		marks.put(variable, offset);
	}
	
	public boolean isMarked(ShaderVariable variable)
	{
		return marks.containsKey(variable);
	}
	
	public @Nullable Integer getOffset(ShaderVariable variable)
	{
		return marks.get(variable);
	}
	
	@Override
	public void debug()
	{
		Debug.print("marks {");
		Debug.indent();
		for (Entry<ShaderVariable, Integer> p : marks.entrySet().stream().sorted((e0, e1) -> Integer.compare(e0.getValue(), e1.getValue())).toList())
		{
			Debug.print(p.getKey() + " -> " + p.getValue());
		}
		Debug.deindent();
		Debug.print("}");
	}
}
