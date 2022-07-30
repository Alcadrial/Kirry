#version 330//for every vertices

in vec3 vertexPosition;
in vec4 color;
in vec2 uv;

uniform mat4 transformation;

out vec4 color0;
out vec2 uv0;

void main(void)
{
	gl_Position = transformation * vec4(vertexPosition, 1);
	color0 = color;
	uv0 = uv;
}