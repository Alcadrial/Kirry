#version 330//for every vertices

in vec3 vertexPosition;
in vec4 color;

uniform mat4 TRANSFORMATION;

out vec4 color0;

void main()
{
	gl_Position = TRANSFORMATION * vec4(vertexPosition, 1);
	color0 = color;
}
