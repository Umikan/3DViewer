#version 460

layout(location = 0) in vec3 vertexPosition_modelspace;
uniform mat4 MVP;
uniform mat4 M;
uniform mat4 V;

void main(){

    vec4 position = vec4(vertexPosition_modelspace, 1.0);
    gl_Position =  MVP * vec4(position.xyz, 1.0);


}