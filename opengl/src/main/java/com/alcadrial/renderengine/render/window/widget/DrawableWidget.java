package com.alcadrial.renderengine.render.window.widget;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector4i;
import com.alcadrial.renderengine.render.framebuffer.FrameBuffer;
import com.alcadrial.renderengine.render.renderer.VertexArray;
import com.alcadrial.renderengine.render.renderingmanager.RenderingManager;

public interface DrawableWidget extends Widget {
	
	/**
	 * Use to create objects like {@link FrameBuffer}s and {@link VertexArray}s to use in the {@link #render(Matrix4f, RenderingManager, float)} method.
	 * @param matrix a custom matrix
	 * @param manager
	 * @param partialFrame
	 */
	void initializeRendering(Matrix4f matrix, RenderingManager manager, float partialFrame);
	/**
	 * <p>Draws textures and collects vertices to use in the {@link #draw(Matrix4f, RenderingManager, float)} method.</p>
	 * <p>This method is called after the first call to {@link #initializeRendering(Matrix4f, RenderingManager, float)}.</p>
	 * @param matrix a custom matrix
	 * @param manager
	 * @param partialFrame
	 */
	void render(Matrix4f matrix, RenderingManager manager, float partialFrame);
	/**
	 * <p>Draws the widget in the screen.</p>
	 * <p>This method is called after the first call to {@link #render(Matrix4f, RenderingManager, float)}.</p>
	 * @param matrix the tranformation matrix
	 * @param manager
	 * @param partialFrame
	 */
	void draw(Matrix4f matrix, RenderingManager manager, float partialFrame);
	/**
	 * <p>Closes any resource previously allocated in {@link #initializeRendering(Matrix4f, RenderingManager, float)}.</p>
	 * <p>The invokation this method will allow another invokation of {@link #initializeRendering(Matrix4f, RenderingManager, float)}.</p>
	 */
	void clearRenderingData();
	
	/**
	 * <p>Computes the rectangle of the screen that will be affected by a call to {@link #draw(Matrix4f, RenderingManager, float)} using the given matrix.</p>
	 * <p>The x and y components of the returned {@link Vector4i} are the position of the rectangle, the z and w components are the width and height of the rectangle</p>
	 * @param matrix the tranformation matrix
	 * @param partialFrame
	 * @return the area affected by {@link #draw(Matrix4f, RenderingManager, float)}
	 */
	Vector4i drawingArea(Matrix4f matrix, float partialFrame);
	/**
	 * <p>Returns true if this widget has been drawn into the screen and it should not be cleared.</p>
	 * @return true if this widget has been drawn into the screen and it should not be cleared
	 */
	boolean onScreen();
}
