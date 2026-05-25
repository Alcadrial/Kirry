#version 450

layout (location = 0) in vec2 uv;

layout (binding = 1) uniform sampler2D textures[2];

layout (location = 0) out vec4 tex;

void main()
{
	if (uv.y < 0.5)
	{
		tex = texture(textures[0], uv);
	}
	else if (uv.y >= 0.5)
	{
		tex = texture(textures[1], uv);
	}
}
