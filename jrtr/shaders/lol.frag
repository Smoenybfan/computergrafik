#version 330

#define MAX_LIGHTS 8
// GLSL version 1.50
// Fragment shader for diffuse shading in combination with a texture map



// Uniform variables passed in from host program
uniform sampler2D myTexture;
uniform int nLights;

uniform vec4 lightDirection[MAX_LIGHTS];
uniform vec4 lightColor[MAX_LIGHTS];


uniform vec3 lightPosition[MAX_LIGHTS];
uniform float lightReflection[MAX_LIGHTS];
uniform vec3 viewPos;


in vec2 frag_texcoord;



in vec3 FragPos;
in vec3 Normal;


// Output variable, will be written to framebuffer automatically
out vec4 frag_shaded;


void main()
{

   vec2 frag_texcoor = vec2(FragPos);
        	vec4 texColor = texture(myTexture, frag_texcoor);
            frag_shaded = texColor;

    vec4 color = texture(myTexture, frag_texcoord);
    //color = texColor;

    // Ambient
    float ambientStrength = 0.05f;
    vec4 ambientLightColor = vec4(1,1,1,0);
    vec4 ambient = ambientLightColor*ambientStrength;


    // Diffuse
    vec4 diffuse = vec4(0,0,0,0);
    for(int i = 0; i < nLights; i++) {
        vec3 norm = normalize(Normal);
        vec3 lightDiff = (lightPosition[i] - FragPos);
        float distanceSqr = lightDiff.x*lightDiff.x+lightDiff.y*lightDiff.y+lightDiff.z*lightDiff.z;
        vec3 lightDir = normalize(lightDiff);
        float diff = max(dot(norm, lightDir), 0.0);
        vec4 dir = lightDirection[i]; //useless
       // diffuse = diffuse + diff*lightColor[i]*(1/distanceSqr);
        diffuse = diffuse + diff*lightColor[i];
    }


        // Specular
        vec4 specular = vec4(0,0,0,0);
        for(int i = 0; i < nLights; i++) {
            vec3 norm = normalize(Normal);
            float specularStrength = 0.8f;
            vec3 viewDir = normalize(viewPos - FragPos);
            vec3 reflectDir = reflect(vec3(-lightDirection[i]), norm);
            float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32);
            specular = specular+specularStrength * spec * lightColor[i];
        }




    frag_shaded = (specular+ambient+diffuse)*color;





	// The built-in GLSL function "texture" performs the texture lookup

	//ndotl * texture(myTexture, frag_texcoord);

	//frag_shaded = vec4(1f,1f,1f,1f);
}

