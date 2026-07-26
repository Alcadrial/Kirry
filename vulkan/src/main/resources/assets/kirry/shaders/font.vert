#version 450

layout (location = 0) in vec3 vertexPosition;
layout (location = 1) in uint glyph;

layout (location = 0) out uint outGlyph;

void main()
{
	gl_Position = vec4(vertexPosition, 1);
	
	outGlyph = glyph;
}
