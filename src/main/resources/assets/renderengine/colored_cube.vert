#version 330//for every vertices

in vec3 vertices;
in vec4 colorStart;

uniform mat4 transformation;

out vec4 color_start;

void main()
{
	gl_Position = transformation * vec4(vertices, 1);
    color_start = colorStart;
}
