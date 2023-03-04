#version 460

out vec4 color;

in vec2 f_texcoord;
uniform sampler2D tex;

void main(){
    vec2 flipped_texcoord = vec2(f_texcoord.x, 1.0 - f_texcoord.y);
    color = texture2D(tex, flipped_texcoord);
    //color = vec4(1.0,1.0,1.0,1.0);
}