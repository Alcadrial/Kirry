package com.alcadrial.kirry.render.window;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2ir;
import com.alcadrial.kirry.render.FrameBuffer;
import com.alcadrial.kirry.render.buffer.VulkanBuffer;
import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;
import com.alcadrial.kirry.render.memory.MemoryManager;
import com.alcadrial.kirry.render.renderpass.RenderPass;
import com.alcadrial.kirry.render.util.exception.VulkanException;

public interface Screen extends AutoCloseable {
	
	/**
	 * Use to create objects like {@link FrameBuffer}s and {@link VulkanBuffer}s to use in the {@link #render(Matrix4f, CommandBuffer)} method.
	 * @throws VulkanException
	 */
	void init(MemoryManager memoryManager, CommandBuffer commandBuffer) throws VulkanException;
	/**
	 * Draws textures and collects vertices to use in the {@link #draw(Matrix4f, CommandBuffer)} method.
	 * <p>
	 * This method is called after {@link #init(MemoryManager, CommandBuffer)}.
	 * 
	 * @param matrix a custom matrix
	 * @param commandBuffer
	 * @throws VulkanException
	 */
	void render(Matrix4f matrix, CommandBuffer commandBuffer) throws VulkanException;
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
	 * If the method {@link #requestDraw()} is called, is guaranteed to that this call will draw the entire widget.
	 * <p>
	 * This method is called after {@link #render(Matrix4f, CommandBuffer)}.
	 * 
	 * @param matrix the transformation matrix
	 * @param commandBuffer
	 * @throws VulkanException
	 */
	void draw(Matrix4f matrix, CommandBuffer commandBuffer) throws VulkanException;
	/**
	 * Closes any resource previously allocated in {@link #init(MemoryManager, CommandBuffer)}.
	 * <p>
	 * The invocation this method will allow another invocation of init().
	 */
	void clear();
	
	/**
	 * Returns true if this widget has been drawn into the window surface and it should not be cleared.
	 * 
	 * @return true if this widget has been drawn into the window surface and it should not be cleared
	 */
	boolean isOnSurface();
	/**
	 * Returns true if this widget has collected all the vertices after a
	 * {@link #render(Matrix4f, CommandBuffer)} and is ready to {@link #draw(Matrix4f, CommandBuffer)}.
	 * 
	 * @return true if this widget has been rendered
	 */
	boolean isRendered();
	/**
	 * Returns true if this widget had created all the rendering objects necessary for {@link #render(Matrix4f, CommandBuffer)}.
	 * 
	 * @return true if this widget has been initialized for rendering
	 */
	boolean isInitialized();
	/**
	 * Marks this widget to not skip the next {@link #render(Matrix4f, CommandBuffer)}.
	 */
	void requestRender();
	/**
	 * Marks this widget to not skip the next {@link #draw(Matrix4f, CommandBuffer)}.
	 */
	void requestDraw();
	
	boolean isDragging();
	/**
	 * Register events callback to be executed by the main thread
	 * @param events
	 */
	void registerEvents(EventRegistry events);
	
	ActionResult onClick(int button, int count, Vector2ir mousePos);
	ActionResult onPress(int button, Vector2ir mousePos);
	ActionResult onRelease(int button, Vector2ir mousePos);
	
	void onMouseMove(Vector2ir oldPos, Vector2ir newPos);
	
	ActionResult onDragStart(Vector2ir mousePos);
	ActionResult onDragEnd(Vector2ir startPos, Vector2ir endPos);
	ActionResult onScroll(double xScroll, double yScroll, Vector2ir mousePos);
	RenderPass getRenderPass();
	
	@Override
	void close() throws VulkanException;
}
