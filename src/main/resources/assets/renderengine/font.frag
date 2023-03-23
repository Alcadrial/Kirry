#version 330//for every pixel

in vec3 color0;
in vec2 uv0;

uniform sampler2D atlas;

out vec4 color;

void main()
{
	vec4 q = texture(atlas, uv0);
	if (q.a == 0) discard;
	color = vec4(color0, q.a);
}