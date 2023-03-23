#version 330//for every vertices

layout (location = 0) in vec3 vertexPosition;
layout (location = 1) in vec4 color;

//uniform mat4 TRANSFORMATION;

layout () out vec4 color0;

void main()
{
	gl_Position = /*TRANSFORMATION * */vec4(vertexPosition, 1);
	color0 = color;
}
