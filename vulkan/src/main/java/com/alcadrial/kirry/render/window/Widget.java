package com.alcadrial.kirry.render.window;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2fr;
import com.alcadrial.kector.vector.Vector2ir;
import com.alcadrial.kector.vector.Vector3ir;
import com.alcadrial.kector.vector.Vector4i;
import com.alcadrial.kirry.render.FrameBuffer;
import com.alcadrial.kirry.render.Renderer;
import com.alcadrial.kirry.render.buffer.VulkanBuffer;
import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;
import com.alcadrial.kirry.render.memory.MemoryManager;
import com.alcadrial.kirry.render.texture.TextureManager;
import com.alcadrial.kirry.render.util.exception.VulkanException;

public interface Widget extends AutoCloseable {

	/**
	 * Use to load textures into an atlas, may be called multiple times.
	 * 
	 * @param textureManager
	 */
	void initTextures(MemoryManager memoryManager, CommandBuffer commandBuffer, TextureManager textureManager) throws VulkanException;
	/**
	 * Use to create objects like {@link FrameBuffer}s and {@link VulkanBuffer}s to use in the {@link #render(Matrix4f, CommandBuffer, Renderer) render} method.
	 * Called only once after {@link #initTextures(MemoryManager, CommandBuffer, TextureManager) initTextures}.
	 * 
	 * @param memoryManager
	 * @param commandBuffer
	 * @throws VulkanException
	 */
	void initBuffers(MemoryManager memoryManager, CommandBuffer commandBuffer) throws VulkanException;
	/**
	 * Draws textures and collects vertices to use in the {@link #draw(Matrix4f, CommandBuffer, Renderer, TextureManager) draw} method.
	 * <p>
	 * This method is called after {@link #initBuffers(MemoryManager, CommandBuffer) initBuffers}.
	 * 
	 * @param matrix a custom matrix
	 * @param commandBuffer
	 * @param renderer
	 * @throws VulkanException
	 */
	void render(Matrix4f matrix, CommandBuffer commandBuffer, Renderer renderer) throws VulkanException;
	/**
	 * 
	 * 
	 * @param commandBuffer
	 * @param elapsed
	 * @throws VulkanException
	 */
	void update(CommandBuffer commandBuffer, float elapsed) throws VulkanException;
	/**
	 * Draws the widget on the screen. If the widget is already drawn on the screen, the implementation may not draw anything or may draw only
	 * the missing portion of the widget if any.
	 * <p>
	 * This method is called after {@link #render(Matrix4f, CommandBuffer, Renderer) render}.
	 * 
	 * @param matrix the transformation matrix
	 * @param commandBuffer
	 * @param textureManager
	 * @throws VulkanException
	 */
	void draw(Matrix4f matrix, CommandBuffer commandBuffer, Renderer renderer, TextureManager textureManager) throws VulkanException;
	/**
	 * Closes any resource previously allocated in {@link #initBuffers(MemoryManager, CommandBuffer) initializeRendering}.
	 * <p>
	 * The invocation this method will allow another invocation of initializeRendering.
	 */
	void clear();
	
	@Override
	void close() throws VulkanException;
	
	/**
	 * Computes the rectangle of the screen that will be affected by a call to {@link #draw(Matrix4f, CommandBuffer, Renderer, TextureManager) draw} using the given matrix.
	 * <p>
	 * The x and y components of the returned {@link Vector4i} are the position of the rectangle, the z and w components are the width and height of the rectangle.
	 * 
	 * @param matrix the transformation matrix
	 * @param partialFrame
	 * @return the area affected by {@link #draw(Matrix4f, CommandBuffer, Renderer, TextureManager) draw} or null if not known
	 */
	Vector4i drawingArea(Matrix4f matrix, float partialFrame);
	/**
	 * Returns true if this widget has collected all the vertices after a
	 * {@link #render(Matrix4f, CommandBuffer, Renderer) render} and is ready to {@link #draw(Matrix4f, CommandBuffer, Renderer, TextureManager) draw}.
	 * 
	 * @return true if this widget has been rendered
	 */
	boolean isRendered();
	/**
	 * Returns true if this widget had created all the rendering objects necessary for {@link #render(Matrix4f, CommandBuffer, Renderer) render}.
	 * 
	 * @return true if this widget has been initialized for rendering
	 */
	boolean isInitialized();
	/**
	 * Marks this widget to not skip the next {@link #render(Matrix4f, CommandBuffer, Renderer) render}.
	 */
	void requestRender();
	
	boolean isFocused();
	void setFocus(boolean focus);

	boolean isMouseHover();
	boolean isHovered(Vector2ir v);
	boolean isHovered(Vector2fr v);
	
	boolean isDragging();
	
	ActionResult onClick(int button, int count, Vector2ir mousePos);
	ActionResult onPress(int button, Vector2ir mousePos);
	ActionResult onRelease(int button, Vector2ir mousePos);
	
	void onMouseMove(Vector2ir oldPos, Vector2ir newPos);
	
	ActionResult onDragStart(Vector2ir mousePos);
	ActionResult onDragEnd(Vector2ir startPos, Vector2ir endPos);
	ActionResult onScroll(double xScroll, double yScroll, Vector2ir mousePos);
	
	int getX();
	int getY();
	int getZ();
	int getWidth();
	int getHeight();
	
	Vector3ir getPos();
	Vector2ir getSize();
	void setPos(Vector3ir pos);
	void setSize(Vector2ir size);
	void registerEvents(EventRegistry registry);
}
