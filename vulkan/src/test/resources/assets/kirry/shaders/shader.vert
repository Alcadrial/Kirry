#version 450

layout (location = 0) in vec3 vertexPosition;
layout (location = 1) in vec4 color;

layout (binding = 0) uniform U {
	mat4 TRANSFORMATION;
};

layout (location = 0) out vec4 color0;

void main()
{
	gl_Position = TRANSFORMATION * vec4(vertexPosition, 1);
	color0 = color;
}
