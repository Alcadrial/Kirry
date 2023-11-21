#version 450

layout (location = 0) in vec3 vertexPosition;
layout (location = 1) in vec2 uv;

layout (binding = 0) uniform U {
	mat4 TRANSFORMATION;
};

layout (location = 0) out vec2 uv0;

void main()
{
	gl_Position = TRANSFORMATION * vec4(vertexPosition, 1);
	uv0 = uv;
}
