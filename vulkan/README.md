# Kirry Vulkan

An engine for graphical and computing purposes, using Vulkan, Stb, GLFW and FreeType over LWJGL.
This project requires Java 25+ and can be run on all the major OSes, even in headless mode.
The project is split in debug, release and common code.


## Features

- BufferLayout: An abstraction to save the layout of data inside a buffer to be used in rendering.
- Templates: Because Vulkan is an explicit api many apis like pipelines requires a lot of parameters so there are some Template classes that
  serves as a default set of parameters that can be reused and shared across the code.
- Shader: An abstraction over shaders to automate resource binding in more advanced classes like Renderer.
- MemoryManager: A manager to simplify memory management in Vulkan.
- TextureAtlas: An atlas to collect various texture to use in rendering.
- FontAtlas: An atlas dedicated to keep raster font's drawn letters and symbols to be used in rendering.
- Renderer/Computer: The actual classes that collects all configuration in one place to correclty record drawing/computing call to the gpu .
- ECS: A minimal implementation of the Entity Component System, using files to draw scenes.
- Window and Widgets: A minimal implementation of a 2D GUI, integrated with the ECS to render 3D scenes.

## Examples and tests
In [this folder](vulkan/src/test/java) are located all test, some tests like [InstancingTest](vulkan/src/test/java/com/alcadrial/kirry/render/test/InstancingTest.java)
can be used to understand how to use this library.

## Files management
This project is bundled with a custom Java FileSystem partial implementation to simplify the management of
external resources like textures, shaders, models and fonts and to decouple test resources from resources shipped with the jar.
The FileSystem named "rem" is a proxy to other FileSystem implementation that search for a resource through all the
referenced FileSystems to the first match, it also implements the WatchService with the same behaviour
that is required for the ECS system.

## Build
To build the project run
```bash
./gradlew :vulkan:releaseJar
```
for the release version or  
```bash
./gradlew :vulkan:debugJar
```
for the debug version,
To publish to a maven repository run
```bash
./gradlew :vulkan:publish
```
and all jars (debug, release, ...) will be published
