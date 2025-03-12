uniform vec2 quadPos;
uniform vec2 quadSize;

uniform int mode;
uniform vec4 paletteCol;
uniform float redValue;
uniform float greenValue;
uniform float blueValue;

void main(void) {
    vec2 uv = (gl_FragCoord.xy - quadPos.xy) / quadSize.xy;
    vec4 clr;
    if (mode == 0){ // Col
        clr = paletteCol;
    }
    if (mode == 1){ // Red
        clr = vec4(redValue, 0.0, 0.0, 1.0);
    }
    if (mode == 2){ // Green
        clr = vec4(0.0, greenValue, 0.0, 1.0);
    }
    if (mode == 3){ // Blue
        clr = vec4(0.0, 0.0, blueValue, 1.0);
    }
    float checkerboardColumns = 20.;
    float checkerboardRows = 4.001;
    vec3 checkerboard = vec3(0);
    vec2 rep = vec2(10.);
    vec2 id = floor(gl_FragCoord.xy/rep);
    if (mod(id.x + id.y, 2.) < 0.001){
        checkerboard += 1.;
    }
    checkerboard *= 0.3;
    gl_FragColor = vec4(mix(checkerboard, clr.rgb, clr.w), 1);
}