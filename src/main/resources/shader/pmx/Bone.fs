#version 460

out vec4 color;
layout(location = 1) uniform vec4 c = vec4(1.0, 1.0, 1.0, 1.0);

void main(){
    color = c;
}