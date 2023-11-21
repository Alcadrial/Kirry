#version 450

layout (location = 0) in vec2 uv;

layout (binding = 1) uniform sampler2D TEXTURE_SAMPLER;

layout (location = 0) out vec4 tex;

void main()
{
	tex = texture(TEXTURE_SAMPLER, uv);
}
