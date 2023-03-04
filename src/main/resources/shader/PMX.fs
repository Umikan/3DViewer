#version 460

out vec4 color;
in vec3 EyeDirection_cameraspace;
in vec3 LightDirection_cameraspace;
in vec3 Normal_cameraspace;
in vec2 f_texcoord;
layout(binding = 1) uniform sampler2D tex;
layout(location = 6) uniform bool hasTexture;
layout(location = 7) uniform vec4 diffuse;
layout(location = 200) uniform bool mustFlip;


void main(){

    vec3 n = normalize( Normal_cameraspace );
    vec3 l = normalize( LightDirection_cameraspace );

    float pre_cosTheta = dot(n, l);
    float cosTheta = clamp( pre_cosTheta, 0, 1);
    float diff = (0.5 * cosTheta + 0.5);
    float diff2 = diff * diff;

    vec3 LightColor = vec3(1.0, 1.0, 1.0);

    vec2 texcoord = f_texcoord;
    if (mustFlip) texcoord.y = 1.0 - texcoord.y;
    vec4 color1 = vec4(diff2 * LightColor, 1.0);
    vec4 texDiffuse = texture2D(tex, texcoord);

    if (hasTexture) color = mix(color1 * texDiffuse, texDiffuse, 1.0);
    else color = color1 * diffuse;

    if (color.a < 0.5) discard;
}