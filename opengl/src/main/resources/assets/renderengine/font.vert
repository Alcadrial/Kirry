#version 330

in vec3 vertexPosition;
in vec3 colorRGB;
in vec2 uv;

uniform mat4 TRANSFORMATION;

out vec3 color0;
out vec2 uv0;

void main()
{
	gl_Position = TRANSFORMATION * vec4(vertexPosition, 1);
	color0 = colorRGB;
	uv0 = uv;
}
