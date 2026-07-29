#version 450

layout (location = 0) in vec2 uv;
layout (location = 1) in vec3 pos;

layout (binding = 2) uniform sampler2D ATLAS;
layout (binding = 3) uniform U {
	vec4 COLOR;
};

layout (location = 0) out vec4 tex;

void main()
{
	vec4 textureColor = texture(ATLAS, uv);
	
	tex = vec4(mix(textureColor.rgb, COLOR.rgb, textureColor.a), textureColor.a * COLOR.a);
}
