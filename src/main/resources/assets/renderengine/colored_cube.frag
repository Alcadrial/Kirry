#version 330//for every pixel

in vec4 color_start;

out vec4 tex;

void main()
{
	vec4 temp = color_start;
	tex = temp;
}
