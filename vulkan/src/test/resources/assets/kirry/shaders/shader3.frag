#version 450

layout (location = 0) in vec3 vertexPosition;
layout (location = 1) in vec4 color;
layout (location = 2) in vec3 originPosition;

layout (binding = 1) uniform U {
	float PARTICLE_RADIUS;
};

layout (location = 0) out vec4 tex;

void main()
{
	tex = vec4(color.rgb, color.a);
}