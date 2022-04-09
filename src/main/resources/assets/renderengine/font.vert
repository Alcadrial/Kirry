#version 330//for every vertices

in vec2 position;
in vec2 uv;

uniform mat4 transformation;
uniform float z;

out vec2 uv0;

void main()
{
	gl_Position = transformation * vec4(position, z, 1);
	uv0 = uv;
}
