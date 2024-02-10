#version 450

layout (location = 0) in vec3 vertexPosition;
layout (location = 1) in vec4 color;
layout (location = 2) in vec3 position;

layout (binding = 0) uniform U {
	mat4 PROJECTION;
};

layout (location = 0) out vec3 vertexPosition0;
layout (location = 1) out vec4 color0;

void main()
{
	vec4 v = PROJECTION * vec4(vertexPosition + position, 1);

	gl_Position = vec4(v.xyz, 1);
	
	vertexPosition0 = v.xyz;
	color0 = color;
}
