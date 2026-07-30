#version 450

layout (location = 0) in vec3 vertexPosition;
layout (location = 1) in vec2 textureUv;
layout (location = 2) in vec3 normalPosition;

layout (binding = 1) uniform U {
	vec3 LIGHT_POS;
	float LIGHT_INTENSITY;
	float SPACE_FOG_INTENSITY;
};
layout (binding = 2) uniform sampler2D TEXTURE;

layout (location = 0) out vec4 tex;

void main()
{
	vec3 lightPos = LIGHT_POS;
	vec3 lightDistanceVec = abs(lightPos - vertexPosition);
	vec4 color = texture(TEXTURE, textureUv);
	
	float lightDistance = distance(lightPos, vertexPosition);
	float d = dot(lightPos - vertexPosition, normalPosition) / length(lightPos - vertexPosition);
	
	
	float factor = LIGHT_INTENSITY * (1 - SPACE_FOG_INTENSITY);
	
	if (lightDistance < 0.1F * factor) {
		tex = vec4(1, 1, 1, 1);
	}
	else if (lightDistance > 1.5F * factor) {
		tex = vec4(0, 0, 0, 1);
	}
	else if (d <= 0) {
		tex = vec4(0, 0, 0, 1);
	}
	else if (lightDistance > 0.1F) {
		float lightLevel = d * LIGHT_INTENSITY * (1 - SPACE_FOG_INTENSITY) / lightDistance;
		
		vec3 c = color.rgb * lightLevel;
	//	if (c.x > 1) c.yz *= (c.x - 1) / lightLevel + 1;
	//	else if (c.y > 1) c.xz *= (c.y - 1) / lightLevel + 1;
	//	else if (c.z > 1) c.xy *= (c.z - 1) / lightLevel + 1;
		tex = vec4(c, color.a);
	}
	else {
		tex = vec4(1, 1, 1, 1);
	}
}
