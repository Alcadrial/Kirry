#version 330//for every vertices

in vec3 position;
in vec2 uv;
in vec4 color;

uniform mat4 transformation;

out vec2 uv0;
out vec4 color0;

void main()
{
	gl_Position = transformation * vec4(position, 1);
	uv0 = uv;
	color0 = color;
}
