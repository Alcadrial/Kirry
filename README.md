# Kirry

Mainly a multi platform rendering engine in Java using LWJGL and GLFW as it's window system, it is split into multiple jars
to decouple the code and the dependencies.

### SubProjects
To better understand a project refer to the relative readme using the links.
- [Core](core/README.md): A project that contains common code used by all other projects, must be included in the classpath to run the other projects.
- [Audio](audio/README.md): A java wrapper over the famous Fmod library
- [Opengl](opengl/README.md): A simple render engine that uses the OpenGl library, a legacy project.
- [Vulkan](vulkan/README.md): A more modern engine that uses the Vulkan specs.
