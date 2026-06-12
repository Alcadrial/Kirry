package com.alcadrial.kirry.render.window.widget;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector4i;
import com.alcadrial.kirry.render.framebuffer.FrameBuffer;
import com.alcadrial.kirry.render.renderer.VertexArray;
import com.alcadrial.kirry.render.renderingmanager.RenderingManager;

public interface DrawableWidget extends Widget {
	
	/**
	 * Use to create objects like {@link FrameBuffer}s and {@link VertexArray}s to use in the {@link #render(Matrix4f, RenderingManager, float) render} method.
	 * 
	 * @param matrix a custom matrix
	 * @param manager
	 * @param partialFrame
	 */
	void initializeRendering(Matrix4f matrix, RenderingManager manager, float partialFrame);
	/**
	 * Draws textures and collects vertices to use in the {@link #draw(Matrix4f, RenderingManager, float) draw} method.
	 * <p>
	 * This method is called after {@link #initializeRendering(Matrix4f, RenderingManager, float) initializeRendering}.
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
	 * If the method {@link #requestDraw() requestDraw} is called, is guaranteed to that this call will draw the entire widget.
	 * <p>
	 * This method is called after {@link #render(Matrix4f, RenderingManager, float) render}.
	 * 
	 * @param matrix the tranformation matrix
	 * @param manager
	 * @param partialFrame
	 */
	void draw(Matrix4f matrix, RenderingManager manager, float partialFrame);
	/**
	 * Closes any resource previously allocated in {@link #initializeRendering(Matrix4f, RenderingManager, float) initializeRendering}.
	 * <p>
	 * The invokation this method will allow another invokation of initializeRendering.
	 */
	void clearRenderingData();
	
	/**
	 * Computes the rectangle of the screen that will be affected by a call to {@link #draw(Matrix4f, RenderingManager, float) draw} using the given matrix.
	 * <p>
	 * The x and y components of the returned {@link Vector4i} are the position of the rectangle, the z and w components are the width and height of the rectangle.
	 * 
	 * @param matrix the tranformation matrix
	 * @param partialFrame
	 * @return the area affected by {@link #draw(Matrix4f, RenderingManager, float) draw} or null if not known
	 */
	Vector4i drawingArea(Matrix4f matrix, float partialFrame);
	/**
	 * Returns true if this widget has been drawn into the screen and it should not be cleared.
	 * 
	 * @return true if this widget has been drawn into the screen and it should not be cleared
	 */
	boolean isOnScreen();
	/**
	 * Returns true if this widget has collected all the vertices after a 
	 * {@link #render(Matrix4f, RenderingManager, float) render} and is ready to {@link #draw(Matrix4f, RenderingManager, float) draw}.
	 * 
	 * @return true if this widget has been rendered
	 */
	boolean isRendered();
	/**
	 * Returns true if this widget had created all the rendering objects necessary for {@link #render(Matrix4f, RenderingManager, float) render}.
	 * 
	 * @return true if this widget has been initialized for rendering
	 */
	boolean isInitialized();
	/**
	 * Marks this widget to not skip the next {@link #render(Matrix4f, RenderingManager, float) render}.
	 */
	void requestRender();
	/**
	 * Marks this widget to not skip the next {@link #draw(Matrix4f, RenderingManager, float) draw}.
	 */
	void requestDraw();
}
