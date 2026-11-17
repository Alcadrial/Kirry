#version 450

layout (points) in;
layout (triangle_strip, max_vertices = 6) out;

layout (binding = 0) uniform U {
	mat4 PROJECTION;
	mat4 MODEL;
};

struct GlyphData {
	vec2 size;
	vec2 advance;
	vec2 bearing;
	vec2 vertBearing;
	vec4 uv;
};

layout (binding = 1) readonly buffer FontData {
	GlyphData DATA[];
};

layout (location = 0) in uint glyph[];

layout (location = 0) out vec2 outUv;
layout (location = 1) out vec3 outPos;

void main(void)
{
	mat4 M = PROJECTION * MODEL;
	vec3 pos = gl_in[0].gl_Position.xyz;
	GlyphData data = DATA[glyph[0]];

	vec3 origin = pos + vec3(data.bearing.x, -data.bearing.y, 0);
	
	gl_Position = M * vec4(origin, 1);
	outPos = vec3(gl_Position.xy, 0);
	outUv = data.uv.xy;
	EmitVertex();

	gl_Position = M * vec4(origin + vec3(data.size.x, 0, 0), 1);
	outPos = vec3(gl_Position.xy, 0.17);
	outUv = data.uv.zy;
	EmitVertex();

	gl_Position = M * vec4(origin + vec3(data.size.x, data.size.y, 0), 1);
	outPos = vec3(gl_Position.xy, 0.33);
	outUv = data.uv.zw;
	EmitVertex();

	EndPrimitive();
	
	gl_Position = M * vec4(origin, 1);
	outPos = vec3(gl_Position.xy, 0.50);
	outUv = data.uv.xy;
	EmitVertex();

	gl_Position = M * vec4(origin + vec3(data.size.x, data.size.y, 0), 1);
	outPos = vec3(gl_Position.xy, 0.67);
	outUv = data.uv.zw;
	EmitVertex();

	gl_Position = M * vec4(origin + vec3(0, data.size.y, 0), 1);
	outPos = vec3(gl_Position.xy, 0.83);
	outUv = data.uv.xw;
	EmitVertex();

	EndPrimitive();
}