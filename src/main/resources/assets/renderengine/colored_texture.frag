#version 330

in vec2 uv0;
in vec4 color0;

uniform sampler2D textureSampler;

out vec4 pixel;

void main()
{
	vec4 v = texture(textureSampler, uv0);
	if (v.a == 0) discard;
	if (color0.r > 0) v = vec4(mix(color0, v, 0.5).rgb, v.a);
	pixel = v;
}
