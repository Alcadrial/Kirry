#version 450

layout (location = 0) in vec3 vertex;
layout (location = 1) in vec2 textureUv;
layout (location = 2) in vec3 normal;

layout (binding = 0) uniform U {
	mat4 PROJECTION;
	mat4 MODEL;
};

layout (location = 0) out vec3 vertex0;
layout (location = 1) out vec2 textureUv0;
layout (location = 2) out vec3 normal0;

void main()
{
	vec4 v = PROJECTION * MODEL * vec4(vertex, 1);

	gl_Position = v;
	
	vertex0 = v.xyz / v.w;
	textureUv0 = textureUv;
	normal0 = (MODEL * vec4(normal, 0)).xyz;
}
