#version 460

layout(location = 0) in vec3 position;
layout(location = 1) in vec3 texcoord;

out vec2 f_texcoord;

void main(){
    f_texcoord = texcoord.xy;
    gl_Position =  vec4(position.xy, 0.0, 1.0);
}