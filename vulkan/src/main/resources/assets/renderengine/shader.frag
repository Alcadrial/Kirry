#version 450

layout (location = 0) in vec4 color;

layout (binding = 1) uniform U {
	vec4 BACKGROUND_COLOR;
} GLOBAL;

layout (location = 0) out vec4 tex;

void main()
{
	vec4 temp = mix(color, GLOBAL.BACKGROUND_COLOR, color.a);
	tex = temp;
}
