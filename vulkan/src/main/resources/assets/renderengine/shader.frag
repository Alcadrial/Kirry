#version 330//for every pixel

layout (location = 0) in vec4 color0;

layout () out vec4 tex;

void main()
{
	vec4 temp = color0;
	tex = temp;
}
