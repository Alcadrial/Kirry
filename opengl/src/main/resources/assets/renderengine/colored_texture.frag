#version 330

in vec4 color0;
in vec2 uv0;

uniform sampler2D TEXTURE_SAMPLER;

out vec4 pixel;

void main()
{
	vec4 v = texture(TEXTURE_SAMPLER, uv0);
	if (v.a == 0) discard;
	if (color0.r + color0.g + color0.b > 0) 
	{
		v = vec4(color0.rgb * color0.a + v.rgb * (1 - color0.a), v.a);
	}
	pixel = v;
}
