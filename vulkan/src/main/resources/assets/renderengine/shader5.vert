#version 450

struct Animation {
	int index;
	int length;
};

layout (location = 0) in vec3 vertexPosition;
layout (location = 1) in vec4 color;
layout (location = 2) in vec3 normalPosition;
layout (location = 3) in vec3 instancePosition;
layout (location = 4) in int animationIndex;

layout (binding = 0) uniform U {
	mat4 PROJECTION;
	mat4 MODEL;
};

layout (std430, binding = 2) readonly buffer AnimationPaths {
	vec3 positions[];
};

layout (std430, binding = 3) readonly buffer Input {
	Animation animations[];
};

layout (location = 0) out vec3 vertexPosition0;
layout (location = 1) out vec4 color0;
layout (location = 2) out vec3 normalPosition0;

void main()
{
	vec3 v0 = vertexPosition + instancePosition + positions[animations[gl_InstanceIndex * 2 + animationIndex].index];
	vec4 v = PROJECTION * MODEL * vec4(v0, 1);
	
	gl_Position = vec4(v.xyz, 1);
	
	vertexPosition0 = v.xyz;
	color0 = color;
	normalPosition0 = (MODEL * vec4(normalPosition, 0)).xyz;
}
