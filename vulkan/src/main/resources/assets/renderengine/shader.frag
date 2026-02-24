#version 450

layout (location = 0) in vec4 color;

layout (binding = 0) uniform U {
	mat4 TRANSFORMATION;
};

layout (location = 0) out vec4 tex;

void main()
{
	tex = color;
}
