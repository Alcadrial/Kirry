#version 330//for every vertices

in vec3 position;
in vec4 color;
in vec2 uv;

uniform mat4 transformation;

out vec4 color0;
out vec2 uv0;

void main()
{
	gl_Position = transformation * vec4(position, 1);
	color0 = color;
	uv0 = uv;
}
