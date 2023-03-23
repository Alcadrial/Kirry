#version 330//for every pixel

in vec2 uv0;
out vec4 color0;

uniform vec4 color;
uniform sampler2D atlas;

void main()
{
	vec4 q = texture(atlas, uv0);
	color0 = vec4(color.rgb, q.a);
}