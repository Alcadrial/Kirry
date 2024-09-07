#version 450

layout (location = 0) in vec3 vertexPosition;
layout (location = 1) in vec4 color;
layout (location = 2) in vec3 normalPosition;

layout (binding = 1) uniform U {
	vec3 LIGHT_POS;
	float LIGHT_INTENSITY;
	float SPACE_FOG_INTENSITY;
};

layout (location = 0) out vec4 tex;

void main()
{
	float lightDistance = distance(LIGHT_POS, vertexPosition);
	float d = dot(LIGHT_POS - vertexPosition, normalPosition);
	
	if (lightDistance > d * d)
	{
		float lightLevel = LIGHT_INTENSITY * (1 - SPACE_FOG_INTENSITY) / sqrt(lightDistance);
		
		vec3 c = color.rgb * lightLevel;
		if (c.x > 1) c.yz *= (c.x - 1) / 4 + 1;
		else if (c.y > 1) c.xz *= (c.y - 1) / 4 + 1;
		else if (c.z > 1) c.xy *= (c.z - 1) / 4 + 1;
		tex = vec4(c, color.a);
	}
	else
	{
		tex = vec4(1, 1, 1, 1);
	}
}