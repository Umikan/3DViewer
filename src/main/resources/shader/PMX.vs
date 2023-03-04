#version 460

layout(location = 0) in vec3 vertexPosition_modelspace;
layout(location = 1) in vec3 vertexNormal_modelspace;
layout(location = 2) in vec2 texcoord;
layout(location = 3) in vec4 joint;
layout(location = 4) in vec4 weight;
layout(location = 5) uniform int jointMatrixCount; 
layout(binding = 2) uniform sampler2D jointMatrixTexture;
uniform mat4 MVP;
uniform mat4 M;
uniform mat4 V;
uniform vec3 LightPosition_worldspace;
layout(location = 10) in vec3 vertexPosition_morph[8];
layout(location = 100) uniform int morphSize = 0;
layout(location = 101) uniform float weights[8];

out vec3 EyeDirection_cameraspace;
out vec3 LightDirection_cameraspace;
out vec3 Normal_cameraspace;
out vec2 f_texcoord;


mat4 getJointMatrix(int index){
    vec2 uv = vec2(0.25, 1.0 / jointMatrixCount);
    vec2 uv_half = uv / 2.0;

    vec4 m0 = texture2D(jointMatrixTexture, uv * vec2(0.0, index) + uv_half);
    vec4 m1 = texture2D(jointMatrixTexture, uv * vec2(1.0, index) + uv_half);
    vec4 m2 = texture2D(jointMatrixTexture, uv * vec2(2.0, index) + uv_half);
    vec4 m3 = texture2D(jointMatrixTexture, uv * vec2(3.0, index) + uv_half);

    return mat4(m0, m1, m2, m3);
                
}

void main(){
    f_texcoord = texcoord;

    vec4 position = vec4(vertexPosition_modelspace, 1.0);
    vec4 normal = vec4(vertexNormal_modelspace, 0.0);

    //morphing
    for (int i = 0; i < 8; i++){
        if (i >= morphSize) break;
        position += weights[i] * vec4(vertexPosition_morph[i].xyz, 0.0);    
    }

/*
    mat4 skinMatrix = weight.x * jointMatrix[int(joint.x)]
                    + weight.y * jointMatrix[int(joint.y)]
                    + weight.z * jointMatrix[int(joint.z)]
                    + weight.w * jointMatrix[int(joint.w)];*/

    mat4 skinMatrix = weight.x * getJointMatrix(int(joint.x))
                    + weight.y * getJointMatrix(int(joint.y))
                    + weight.z * getJointMatrix(int(joint.z))
                    + weight.w * getJointMatrix(int(joint.w));
    position = mix(skinMatrix * position, position,0.0);
    normal = skinMatrix * normal;
    
    gl_Position =  MVP * vec4(position.xyz, 1.0);

    vec3 vertexPosition_cameraspace = ( V * M * position).xyz;
    EyeDirection_cameraspace = vec3(0,0,0) - vertexPosition_cameraspace;

    vec3 LightPosition_cameraspace = ( V * vec4(LightPosition_worldspace,1)).xyz;
    LightDirection_cameraspace = LightPosition_cameraspace + EyeDirection_cameraspace;

    Normal_cameraspace = ( V * inverse(transpose(M)) * normal).xyz; 
}