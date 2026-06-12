package com.alcadrial.kirry.render.window;

import java.util.EnumMap;
import java.util.PriorityQueue;
import java.util.function.Consumer;

import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.kirry.render.window.event.ClickEvent;
import com.alcadrial.kirry.render.window.event.Event;
import com.alcadrial.kirry.render.window.event.EventType;

public class EventRegistry {
	
	private record EventHolder(Event e, Consumer<Object[]> run) {}
	
	private EnumMap<EventType, PriorityQueue<EventHolder>> events;
	
	public EventRegistry()
	{
		events = new EnumMap<>(EventType.class);
	}
	
	public void register(ClickEvent event)
	{
		register(EventType.CLICK_EVENT, event, params -> event.run((Integer) params[0], (Integer) params[1], (Vector2i) params[2]));
	}
	
	private void register(EventType type, Event event, Consumer<Object[]> run)
	{
		events.computeIfAbsent(type, _ -> new PriorityQueue<>()).add(new EventHolder(event, run));
	}
	
	public void runEvent(EventType type, Object... params)
	{
		PriorityQueue<EventHolder> q = events.get(type);
		if (q != null)
		{
			for (EventHolder h : q) h.run.accept(params);
		}
	}
}
