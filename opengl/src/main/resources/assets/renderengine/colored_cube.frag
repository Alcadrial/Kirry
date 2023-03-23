#version 330//for every pixel

in vec4 color0;

out vec4 tex;

void main()
{
	vec4 temp = color0;
	tex = temp;
}
