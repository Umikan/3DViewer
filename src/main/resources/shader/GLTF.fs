#version 460

out vec4 color;
in vec3 EyeDirection_cameraspace;
in vec3 LightDirection_cameraspace;
in vec3 Normal_cameraspace;
in vec2 f_texcoord;
layout(location = 6) uniform bool hasTexture;
uniform sampler2D tex;

void main(){
    // カメラ空間で、計算されたフラグメントの法線
    vec3 n = normalize( Normal_cameraspace );
    // 光の方向(フラグメントから光の方向)
    vec3 l = normalize( LightDirection_cameraspace );

    float pre_cosTheta = dot(n, l);
    float cosTheta = clamp( pre_cosTheta, 0, 1);
    float diff = (0.5 * cosTheta + 0.5);
    float diff2 = diff * diff;

    vec3 LightColor = vec3(1.0, 1.0, 1.0);

    vec2 flipped_texcoord = vec2(f_texcoord.x, 1.0 - f_texcoord.y);
    vec4 color1 = vec4(diff2 * LightColor, 1.0);
    //vec4 color1 = vec4(LightColor * cosTheta, 1.0);
    vec4 color2 = texture2D(tex, flipped_texcoord);
    //color = color1 * color2;
    if (hasTexture) color = color2;
    else color = color1;
    if (color.a < 0.5) discard;


}