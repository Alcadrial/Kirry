package com.alcadrial.kirry.render.window;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kirry.render.window.event.ClickEvent;
import com.alcadrial.kirry.render.window.event.Event;
import com.alcadrial.kirry.render.window.event.EventType;
import com.alcadrial.kirry.render.window.event.KeyboardEvent;
import com.alcadrial.kirry.render.window.event.MouseMoveEvent;
import com.alcadrial.kirry.render.window.event.WindowResizeEvent;

public class EventRegistry {
	
	private record EventHolder(Event e, Consumer<Object[]> run) {}
	
	private EnumMap<EventType, Queue<EventHolder>> events;
	
	public EventRegistry()
	{
		events = new EnumMap<>(EventType.class);
	}
	
	public void register(ClickEvent event)
	{
		register(EventType.CLICK, event, params -> event.run((int) params[0], (int) params[1], (Vector2i) params[2]));
	}
	
	public void register(MouseMoveEvent event)
	{
		register(EventType.MOUSE_MOVE, event, params -> event.run((double) params[0], (double) params[1], (double) params[2], (double) params[3], (double) params[4], (double) params[5]));
	}
	
	public void register(KeyboardEvent event)
	{
		register(EventType.KEYBOARD, event, params -> event.run((int) params[0], (int) params[1], (int) params[2], (int) params[3]));
	}
	
	public void register(WindowResizeEvent event)
	{
		register(EventType.WINDOW_RESIZE, event, params -> event.run((int) params[0], (int) params[1]));
	}
	
	private void register(EventType type, Event event, Consumer<Object[]> run)
	{
		events.computeIfAbsent(type, _ -> new LinkedList<>()).add(new EventHolder(event, run));
	}
	
	public void runEvent(EventType type, Object... params)
	{
		Queue<EventHolder> q = events.get(type);
		if (q != null)
		{
			for (EventHolder h : q) h.run.accept(params);
		}
	}
}
