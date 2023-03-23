package com.alcadrial.renderengine.renderer;

public enum RenderingMode {
	
	POINTS,
	LINES,
	LINE_LOOP,
	LINE_STRIP,
	TRIANGLES,
	TRIANGLE_STRIP,
	TRIANGLE_FAN,
	QUADS,
	QUAD_STRIP,
	POLYGON,
	LINES_ADJACENCY,
	LINE_STRIP_ADJACENCY,
	TRIANGLES_ADJACENCY,
	TRIANGLE_STRIP_ADJACENCY,
	PATCHES;
	
	private RenderingMode()
	{
		
	}
}
