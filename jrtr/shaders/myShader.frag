#version 330

#define MAX_LIGHTS 8
// GLSL version 1.50
// Fragment shader for diffuse shading in combination with a texture map



// Uniform variables passed in from host program
uniform sampler2D myTexture;
uniform int nLights;
uniform int shininess;

uniform vec4 lightDirection[MAX_LIGHTS];
uniform vec4 lightDiffuseRadiance[MAX_LIGHTS];


uniform vec3 lightPosition[MAX_LIGHTS];
uniform float lightReflection[MAX_LIGHTS];
uniform vec3 viewPos;
uniform vec3 camPos;


in vec2 frag_texcoord;



in vec3 FragPos;
in vec3 Normal;


// Output variable, will be written to framebuffer automatically
out vec4 frag_shaded;


void main()
{
lightDirection;
   vec2 frag_texcoor = vec2(FragPos);
   vec4 texColor = texture(myTexture, frag_texcoor);


    vec4 color = vec4(1,1,1,0);//texture(myTexture, frag_texcoord);
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
        float distanceSqr = length(lightPosition[i]-FragPos);
        vec3 lightDir = normalize(lightDiff);
        float ndotl = max(dot(norm, lightDir), 0.0);
        diffuse = diffuse + ndotl*lightDiffuseRadiance[i]*(1/pow(distanceSqr,2));


       }


        // Specular
        vec4 specular = vec4(0,0,0,0);
        for(int i = 0; i < nLights; i++) {
            vec3 n = normalize(Normal);
            vec3 L = normalize(lightPosition[i] - FragPos);
            float distanceSqr = length(lightPosition[i]-FragPos);
            vec3 R = reflect(-L,n);
            vec3 e = normalize(-FragPos);
            float rdotes = pow(max(dot(R,e), 0.0), shininess);
            specular = specular + lightDiffuseRadiance[i]/pow(distanceSqr,2) * vec4(1,1,1,0) * rdotes;
        }




    frag_shaded = (specular+diffuse)*color+ambient;





	// The built-in GLSL function "texture" performs the texture lookup

	//ndotl * texture(myTexture, frag_texcoord);

	//frag_shaded = vec4(1f,1f,1f,1f);
}

