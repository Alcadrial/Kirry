#version 450

struct Particle {
  vec3 position;
  vec3 velocity;
  vec4 color;
};

layout (location = 0) in vec3 vertexPosition;
layout (location = 1) in vec4 color;

layout (binding = 1) uniform U {
	vec3 LIGHT_POS;
	float LIGHT_INTENSITY;
	float SPACE_FOG_INTENSITY;
};

layout(std140, binding = 3) readonly buffer ParticleSSBOIn {
   Particle particlesIn[];
};

layout (location = 0) out vec4 tex;

void main()
{
	tex = color;
}
