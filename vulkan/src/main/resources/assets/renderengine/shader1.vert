#version 450

layout (location = 0) in vec3 vertexPosition;
layout (location = 1) in vec4 color;
layout (location = 2) in vec3 normalPosition;

layout (binding = 0) uniform U {
	mat4 PROJECTION;
	mat4 MODEL;
};

layout (location = 0) out vec3 vertexPosition0;
layout (location = 1) out vec4 color0;
layout (location = 2) out vec3 normalPosition0;

void main()
{
	vec4 v = PROJECTION * MODEL * vec4(vertexPosition, 1);

	gl_Position = v;
	
	vertexPosition0 = v.xyz / v.w;
	color0 = color;
	normalPosition0 = (MODEL * vec4(normalPosition, 0)).xyz;
}
