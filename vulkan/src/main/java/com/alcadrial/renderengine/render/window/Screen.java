package com.alcadrial.renderengine.render.window;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.renderengine.render.CommandBuffer;
import com.alcadrial.renderengine.render.FrameBuffer;
import com.alcadrial.renderengine.render.RenderingManager;
import com.alcadrial.renderengine.render.buffer.VertexBuffer;

public interface Screen {
	
	/**
	 * Use to create objects like {@link FrameBuffer}s and {@link VertexBuffer}s to use in the {@link #render(Matrix4f, CommandBuffer, float) render} method.
	 * 
	 * @param matrix a custom matrix
	 * @param manager
	 * @param partialFrame
	 */
	void initializeRendering(Matrix4f matrix, RenderingManager manager, float partialFrame);
	/**
	 * Draws textures and collects vertices to use in the {@link #draw(Matrix4f, CommandBuffer, RenderingManager, float) draw} method.
	 * <p>
	 * This method is called after {@link #initializeRendering(Matrix4f, RenderingManager, float) initializeRendering}.
	 * 
	 * @param matrix a custom matrix
	 * @param commandBuffer
	 * @param partialFrame
	 */
	void render(Matrix4f matrix, CommandBuffer commandBuffer, float partialFrame);
	/**
	 * Draws the widget on the screen. If the widget is already drawn on the screen, the implementation may not draw anything or may draw only
	 * the missing portion of the widget if any.
	 * <p>
	 * If the method {@link #requestDraw() requestDraw} is called, is guaranteed to that this call will draw the entire widget.
	 * <p>
	 * This method is called after {@link #render(Matrix4f, CommandBuffer, float) render}.
	 * 
	 * @param matrix the tranformation matrix
	 * @param commandBuffer TODO
	 * @param manager
	 * @param partialFrame
	 */
	void draw(Matrix4f matrix, CommandBuffer commandBuffer, RenderingManager manager, float partialFrame);
	/**
	 * Closes any resource previously allocated in {@link #initializeRendering(Matrix4f, RenderingManager, float) initializeRendering}.
	 * <p>
	 * The invokation this method will allow another invokation of initializeRendering.
	 */
	void clearRenderingData();
	
	/**
	 * Returns true if this widget has been drawn into the screen and it should not be cleared.
	 * 
	 * @return true if this widget has been drawn into the screen and it should not be cleared
	 */
	boolean isOnScreen();
	/**
	 * Returns true if this widget has collected all the vertices after a 
	 * {@link #render(Matrix4f, CommandBuffer, float) render} and is ready to {@link #draw(Matrix4f, CommandBuffer, RenderingManager, float) draw}.
	 * 
	 * @return true if this widget has been rendered
	 */
	boolean isRendered();
	/**
	 * Returns true if this widget had created all the rendering objects necessary for {@link #render(Matrix4f, CommandBuffer, float) render}.
	 * 
	 * @return true if this widget has been initialized for rendering
	 */
	boolean isInitialized();
	/**
	 * Marks this widget to not skip the next {@link #render(Matrix4f, CommandBuffer, float) render}.
	 */
	void requestRender();
	/**
	 * Marks this widget to not skip the next {@link #draw(Matrix4f, CommandBuffer, RenderingManager, float) draw}.
	 */
	void requestDraw();
	
	boolean isDragging();
	
	ActionResult onClick(int button, int count, Vector2i mousePos);
	ActionResult onPress(int button, Vector2i mousePos);
	ActionResult onRelease(int button, Vector2i mousePos);
	
	void onMouseMove(Vector2i oldPos, Vector2i newPos);
	
	ActionResult onDragStart(Vector2i mousePos);
	ActionResult onDragEnd(Vector2i startPos, Vector2i endPos);
	ActionResult onScroll(double xScroll, double yScroll, Vector2i mousePos);
}
