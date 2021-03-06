#version 400

precision highp float;

in vec3 position;

out vec4 ambientColor;
out vec3 fragNormal;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
uniform vec4 color;
uniform float ambientFactor;

void main() {
    mat3 simplified = mat3(model);
    vec3 fragVertex = simplified * position;
    
    fragNormal = normalize(fragVertex - simplified * vec3(0.0));
    ambientColor = vec4(ambientFactor * color.xyz, color.w);
    
    gl_Position = projection * view * model * vec4(position, 1.0);
}
