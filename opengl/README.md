# Kirry OpenGL

A legacy rendering engine with OpenGL and GLFW over LWJGL, superseded by the vulkan implementation.
This project requires Java 21+ and can be run on all the major OSes, does not work in headless mode.

## Features

- BufferStack: A stack where the elements are buffers, can be used with stack-based rendering
- FrameBuffer: The implementation of a frame buffer useful for post processing and windowing
- Shader: A thin abstraction for GLSL shaders handling
- Texture: A wrapper for the opengl api of textures
- Renderer/Manager: There are classes used for rendering models and font, the RenderingManager handles both along with the global opengl state for rendering
- Window: An implementation with events of a GLFW Window that can handle widgets
- Various curves: Implementation of various famous curves like bèzier curves

## Examples and tests
[UniversalRenderTest](opengl/src/main/java/com/alcadrial/kirry/render/rendertest/UniversalRenderTest.java) is
the main testing class and the only example of usage of the library with minimal widgets implementations

## Build
To build the project run
```bash
./gradlew :opengl:build
```
To publish to a maven repository run
```bash
./gradlew :opengl:publish
```
test classes will be ignored automatically from each source 
