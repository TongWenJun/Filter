precision highp float;
uniform sampler2D y_texture;
uniform sampler2D uv_texture;
varying highp vec2 v_texCoord;

void main()
{
    mediump vec3 yuv;
    highp vec3 rgb;

    yuv.x = texture2D(y_texture, v_texCoord).r;
    yuv.y = texture2D(uv_texture, v_texCoord).a - 0.5;
    yuv.z = texture2D(uv_texture, v_texCoord).r - 0.5;

    rgb = mat3( 1, 1, 1,
                0, -0.344, 1.770,
                1.403, -0.714, 0) * yuv;

/*
    //blcak-white
    float avg = (rgb.r + rgb.g + rgb.b) / 3.0f;
    if (avg > 0.35f)
    {
        rgb.r = rgb.g = rgb.b = 1.0f;
    }
    else
    {
        rgb.r = rgb.g = rgb.b = 0.0f;
    }
*/


    //film
    rgb.r = 1.0f - rgb.r;
    rgb.g = 1.0f - rgb.g;
    rgb.b = 1.0f - rgb.b;


    //gray
    //rgb.r = rgb.g = rgb.b = rgb.r * 0.3f + rgb.g * 0.59f + rgb.b * 0.11f;

    gl_FragColor = vec4(rgb, 1);
}