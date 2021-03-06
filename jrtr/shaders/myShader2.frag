#version 150
// GLSL version 1.50
// Fragment shader for diffuse shading in combination with a texture map

#define MAX_LIGHTS 8
// Uniform variables passed in from host program
uniform sampler2D myTexture;
uniform float shinyness;
uniform vec4 specular;

// Variables passed in from the vertex shader
in float ndotl[MAX_LIGHTS];
in float ndoth[MAX_LIGHTS];
in vec2 frag_texcoord;
in vec4 lightColors[MAX_LIGHTS];

// Output variable, will be written to framebuffer automatically
out vec4 frag_shaded;

void main()
{
	// The built-in GLSL function "texture" performs the texture lookup
	for(int i=0;i<MAX_LIGHTS;i++){
	    frag_shaded += (lightColors[i]) * (texture(myTexture, frag_texcoord)* ndotl[i] + pow(specular*ndoth[i],shinyness));
	}


}

