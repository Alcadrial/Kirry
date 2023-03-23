#version 450

layout (location = 0) in vec4 color0;

layout (location = 1) out vec4 tex;

void main()
{
	vec4 temp = color0;
	tex = temp;
}
