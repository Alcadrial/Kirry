package com.alcadrial.renderengine.render.window.widget;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector4i;
import com.alcadrial.renderengine.render.framebuffer.FrameBuffer;
import com.alcadrial.renderengine.render.renderer.VertexArray;
import com.alcadrial.renderengine.render.renderingmanager.RenderingManager;

public interface DrawableWidget extends Widget {
	
	/**
	 * Use to create objects like {@link FrameBuffer}s and {@link VertexArray}s to use in the {@link #render(Matrix4f, RenderingManager, float)} method.
	 * 
	 * @param matrix a custom matrix
	 * @param manager
	 * @param partialFrame
	 */
	void initializeRendering(Matrix4f matrix, RenderingManager manager, float partialFrame);
	/**
	 * Draws textures and collects vertices to use in the {@link #draw(Matrix4f, RenderingManager, float)} method.
	 * <p>
	 * This method is called after the first call to {@link #initializeRendering(Matrix4f, RenderingManager, float)}.
	 * 
	 * @param matrix a custom matrix
	 * @param manager
	 * @param partialFrame
	 */
	void render(Matrix4f matrix, RenderingManager manager, float partialFrame);
	/**
	 * Draws the widget on the screen. If the widget is already drawn on the screen, the implementation may not draw anything or may draw only
	 * the missing portion of the widget if any.
	 * <p>
	 * If the method {@link #requestDraw()} is called, is guaranteed to that this call will draw all the widget.
	 * <p>
	 * This method is called after the first call to {@link #render(Matrix4f, RenderingManager, float)}.
	 * 
	 * @param matrix the tranformation matrix
	 * @param manager
	 * @param partialFrame
	 */
	void draw(Matrix4f matrix, RenderingManager manager, float partialFrame);
	/**
	 * Closes any resource previously allocated in {@link #initializeRendering(Matrix4f, RenderingManager, float)}.
	 * <p>
	 * The invokation this method will allow another invokation of {@link #initializeRendering(Matrix4f, RenderingManager, float)}.
	 */
	void clearRenderingData();
	
	/**
	 * Computes the rectangle of the screen that will be affected by a call to {@link #draw(Matrix4f, RenderingManager, float)} using the given matrix.
	 * <p>
	 * The x and y components of the returned {@link Vector4i} are the position of the rectangle, the z and w components are the width and height of the rectangle.
	 * 
	 * @param matrix the tranformation matrix
	 * @param partialFrame
	 * @return the area affected by {@link #draw(Matrix4f, RenderingManager, float)} or null if not known
	 */
	Vector4i drawingArea(Matrix4f matrix, float partialFrame);
	/**
	 * Returns true if this widget has been drawn into the screen and it should not be cleared.
	 * 
	 * @return true if this widget has been drawn into the screen and it should not be cleared
	 */
	boolean isOnScreen();
	/**
	 * Returns true if this widget had his vertices modified and needs a new call to {@link #render(Matrix4f, RenderingManager, float)}.
	 * 
	 * @return true if this widget needs to be re-rendered
	 */
	boolean isChanged();
	/**
	 * Marks this widget to not skip the next {@link #render(Matrix4f, RenderingManager, float)} call.
	 */
	void requestRender();
	/**
	 * Marks this widget to not skip the next {@link #draw(Matrix4f, RenderingManager, float)} call.
	 */
	void requestDraw();
}
