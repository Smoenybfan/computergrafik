#version 150
// GLSL version 1.50
// Vertex shader for diffuse shading in combination with a texture map

#define MAX_LIGHTS 8
// Uniform variables, passed in from host program via suitable
// variants of glUniform*
//lightDirection must already be the correct calculated value, same for radiance
uniform mat4 projection;
uniform mat4 modelview;
uniform vec4 lightDirection[MAX_LIGHTS];
uniform int nLights;
uniform vec4 lightDiffuseRadiance[MAX_LIGHTS];
uniform vec4 lightPosition[MAX_LIGHTS];

// Input vertex attributes; passed in from host program to shader
// via vertex buffer objects
in vec3 normal;
in vec4 position;
in vec2 texcoord;
in float difCoef;

// Output variables for fragment shader
out float ndotl[MAX_LIGHTS];
out vec2 frag_texcoord;
out vec4 lightColors[MAX_LIGHTS];
out float ndoth[MAX_LIGHTS];

vec4 midwayVector[MAX_LIGHTS];
vec4 directions[MAX_LIGHTS];


void getMidwayVector(in int i){

    mat4 turn90 = mat4(
    0 , 1 , 0 , 0,
    1 , 0 , 0 , 0,
    0 , 0 , 1 , 0,
    0 , 0 , 0 , 1
    );

    vec4 e = turn90 * directions[i];
    float lpluseLength = sqrt(pow(directions[i].x+e.x,2) + pow(directions[i].y+e.y,2) + pow(directions[i].z+e.z,2));

    midwayVector[i] = (directions[i] + e) * (1.f/lpluseLength);


}

// Transforms the light direction
void transformLightAndDirection(in int i){
    lightDirection;

    float pminusvLength = sqrt(pow(lightPosition[i].x-position.x,2.0)+ pow(lightPosition[i].y-position.y,sqrt(4)) + pow(lightPosition[i].z-position.z,2.0));
    directions[i] = (lightPosition[i]-position) * (1.f/pminusvLength);
    lightColors[i] = lightDiffuseRadiance[i] *(1.f/ pow(pminusvLength,2.0));

}

void main()
{
	// Compute dot product of normal and light direction
	// and pass color to fragment shader
	// Note: here we assume "lightDirection" is specified in camera coordinates,
	// so we transform the normal to camera coordinates, and we don't transform
	// the light direction, i.e., it stays in camera coordinates
	for(int i=0;i<nLights;i++){

	    transformLightAndDirection(i);
        getMidwayVector(i);
	    ndotl[i] = max(dot(modelview * vec4(normal,0), directions[i]),0);
	    ndoth[i] = max(dot(modelview * vec4(normal,0), midwayVector[i]),0);
	}

	// Pass texture coordiantes to fragment shader, OpenGL automatically
	// interpolates them to each pixel  (in a perspectively correct manner)
	frag_texcoord = texcoord;

	// Transform position, including projection matrix
	// Note: gl_Position is a default output variable containing
	// the transformed vertex position
	gl_Position = projection * modelview * position;
}

