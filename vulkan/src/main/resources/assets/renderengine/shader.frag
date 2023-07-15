#version 450

layout (location = 0) in vec4 color;

layout (location = 0) out vec4 tex;

void main()
{
	vec4 temp = color;
	tex = temp;
}
