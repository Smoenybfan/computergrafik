#version 330
// GLSL version 1.50 
// Vertex shader for diffuse shading in combination with a texture map

#define MAX_LIGHTS 8


// Uniform variables, passed in from host program via suitable 
// variants of glUniform*
uniform mat4 projection;
uniform mat4 modelview;
uniform mat4 model;
uniform mat4 view;


// Input vertex attributes; passed in from host program to shader
// via vertex buffer objects

in vec3 normal;
in vec2 texcoord;
in vec4 position;

out vec3 FragPos;
out vec3 Normal;




// Output variables for fragment shader
out vec2 frag_texcoord;



void main()
{

	// Pass texture coordiantes to fragment shader, OpenGL automatically
	// interpolates them to each pixel  (in a perspectively correct manner) 
	frag_texcoord = texcoord;

	// Transform position, including projection matrix
	// Note: gl_Position is a default output variable containing
	// the transformed vertex position
	vec4 newPos = projection * modelview * position;
	gl_Position = newPos;

    //FragPos = vec3(model * position);
    FragPos = vec3(modelview*position);

	//Normal = normal;
    Normal = mat3(transpose(inverse(model))) * normal;
   // Normal = mat3(modelview) * normal;




}
