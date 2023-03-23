#version 330

in vec3 color0;
in vec2 uv0;

uniform vec3 COLOR;
uniform float ALFA_MASK;
uniform sampler2D TEXTURE_SAMPLER;

out vec4 color;

void main()
{
	vec4 q = texture(TEXTURE_SAMPLER, uv0);
	if (q.a == 0) discard;
	color = vec4(color0, q.a * ALFA_MASK);
}