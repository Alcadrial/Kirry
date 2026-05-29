package com.alcadrial.renderengine.render.window;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2i;
import com.alcadrial.renderengine.render.FrameBuffer;
import com.alcadrial.renderengine.render.Renderer;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;
import com.alcadrial.renderengine.render.commandbuffer.CommandBuffer;
import com.alcadrial.renderengine.render.memory.MemoryManager;
import com.alcadrial.renderengine.render.renderpass.RenderPass;
import com.alcadrial.renderengine.render.util.exception.VulkanException;

public interface Screen extends AutoCloseable {
	
	/**
	 * Use to create objects like {@link FrameBuffer}s and {@link VulkanBuffer}s to use in the {@link #render(Matrix4f, Renderer)} method.
	 * @throws VulkanException 
	 */
	void init(MemoryManager memoryManager, CommandBuffer commandBuffer) throws VulkanException;
	/**
	 * Draws textures and collects vertices to use in the {@link #draw(Matrix4f, CommandBuffer, float)} method.
	 * <p>
	 * This method is called after {@link #init(MemoryManager, CommandBuffer)}.
	 * 
	 * @param matrix a custom matrix
	 * @param renderer
	 */
	void render(Matrix4f matrix, Renderer renderer);
	/**
	 * Draws the widget on the screen. If the widget is already drawn on the screen, the implementation may not draw anything or may draw only
	 * the missing portion of the widget if any.
	 * <p>
	 * If the method {@link #requestDraw()} is called, is guaranteed to that this call will draw the entire widget.
	 * <p>
	 * This method is called after {@link #render(Matrix4f, Renderer)}.
	 * 
	 * @param matrix the transformation matrix
	 * @param commandBuffer
	 * @param enlapsedTime
	 * @throws VulkanException 
	 */
	void draw(Matrix4f matrix, CommandBuffer commandBuffer, float enlapsedTime) throws VulkanException;
	/**
	 * Closes any resource previously allocated in {@link #init(MemoryManager, CommandBuffer)}.
	 * <p>
	 * The invocation this method will allow another invocation of init().
	 */
	void clearRenderingData();
	
	/**
	 * Returns true if this widget has been drawn into the window surface and it should not be cleared.
	 * 
	 * @return true if this widget has been drawn into the window surface and it should not be cleared
	 */
	boolean isOnSurface();
	/**
	 * Returns true if this widget has collected all the vertices after a 
	 * {@link #render(Matrix4f, Renderer)} and is ready to {@link #draw(Matrix4f, CommandBuffer, float)}.
	 * 
	 * @return true if this widget has been rendered
	 */
	boolean isRendered();
	/**
	 * Returns true if this widget had created all the rendering objects necessary for {@link #render(Matrix4f, Renderer)}.
	 * 
	 * @return true if this widget has been initialized for rendering
	 */
	boolean isInitialized();
	/**
	 * Marks this widget to not skip the next {@link #render(Matrix4f, Renderer)}.
	 */
	void requestRender();
	/**
	 * Marks this widget to not skip the next {@link #draw(Matrix4f, CommandBuffer, float)}.
	 */
	void requestDraw();
	
	boolean isDragging();
	/**
	 * Register events callback to be executed by the main thread
	 * @param events
	 */
	void registerEvents(EventRegistry events);
	
	void setWindow(VulkanWindow window);
	
	ActionResult onClick(int button, int count, Vector2i mousePos);
	ActionResult onPress(int button, Vector2i mousePos);
	ActionResult onRelease(int button, Vector2i mousePos);
	
	void onMouseMove(Vector2i oldPos, Vector2i newPos);
	
	ActionResult onDragStart(Vector2i mousePos);
	ActionResult onDragEnd(Vector2i startPos, Vector2i endPos);
	ActionResult onScroll(double xScroll, double yScroll, Vector2i mousePos);
	RenderPass getRenderPass();
	
	@Override
	void close() throws VulkanException;
}
