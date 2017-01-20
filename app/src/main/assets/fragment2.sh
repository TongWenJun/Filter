precision highp float;

uniform sampler2D y_texture;
uniform sampler2D uv_texture;
varying highp vec2 v_texCoord;

void main()
{
    mediump vec3 yuv[9];
    highp vec3 rgb[9];
    vec2 offset[9];
    float kV[9];
    const float scaleFactor = 1.0;

    float width = 2048.0;//800.0; //
    float height = 1536.0;//480.0; //
    offset[0] = vec2(-1.0/width, -1.0/height);
    offset[1] = vec2(0.0/width, -1.0/height);
    offset[2] = vec2(1.0/width, -1.0/height);
    offset[3] = vec2(-1.0/width, 0.0/height);
    offset[4] = vec2(0.0/width, 0.0/height);
    offset[5] = vec2(1.0/width, 0.0/height);
    offset[6] = vec2(-1.0/width, 1.0/height);
    offset[7] = vec2(0.0/width, 1.0/height);
    offset[8] = vec2(1.0/width, 1.0/height);

    kV[0] = 2.0;    kV[1] = 0.0;    kV[2] = 2.0;
    kV[3] = 0.0;    kV[4] = 0.0;   kV[5] = 0.0;
    kV[6] = 3.0;    kV[7] = 0.0;    kV[8] = -6.0;

    vec4 sum = vec4(0.0, 0.0, 0.0, 0.0);

    yuv[0].x = texture2D(y_texture, v_texCoord + offset[0]).r;
    yuv[0].y = texture2D(uv_texture, v_texCoord + offset[0]*2.0).a - 0.5;
    yuv[0].z = texture2D(uv_texture, v_texCoord + offset[0]*2.0).r - 0.5;
    rgb[0] = mat3( 1, 1, 1,
                    0, -0.344, 1.770,
                    1.403, -0.714, 0) * yuv[0];

    yuv[1].x = texture2D(y_texture, v_texCoord + offset[1]).r;
    yuv[1].y = texture2D(uv_texture, v_texCoord + offset[1]*2.0).a - 0.5;
    yuv[1].z = texture2D(uv_texture, v_texCoord + offset[1]*2.0).r - 0.5;
    rgb[1] = mat3( 1, 1, 1,
                 0, -0.344, 1.770,
                 1.403, -0.714, 0) * yuv[1];

    yuv[2].x = texture2D(y_texture, v_texCoord + offset[2]).r;
    yuv[2].y = texture2D(uv_texture, v_texCoord + offset[2]*2.0).a - 0.5;
    yuv[2].z = texture2D(uv_texture, v_texCoord + offset[2]*2.0).r - 0.5;
    rgb[2] = mat3( 1, 1, 1,
                    0, -0.344, 1.770,
                    1.403, -0.714, 0) * yuv[2];

    yuv[3].x = texture2D(y_texture, v_texCoord + offset[3]).r;
    yuv[3].y = texture2D(uv_texture, v_texCoord + offset[3]*2.0).a - 0.5;
    yuv[3].z = texture2D(uv_texture, v_texCoord + offset[3]*2.0).r - 0.5;
    rgb[3] = mat3( 1, 1, 1,
                    0, -0.344, 1.770,
                    1.403, -0.714, 0) * yuv[3];

    yuv[4].x = texture2D(y_texture, v_texCoord + offset[4]).r;
    yuv[4].y = texture2D(uv_texture, v_texCoord + offset[4]*2.0).a - 0.5;
    yuv[4].z = texture2D(uv_texture, v_texCoord + offset[4]*2.0).r - 0.5;
    rgb[4] = mat3( 1, 1, 1,
                    0, -0.344, 1.770,
                    1.403, -0.714, 0) * yuv[4];

    yuv[5].x = texture2D(y_texture, v_texCoord + offset[5]).r;
    yuv[5].y = texture2D(uv_texture, v_texCoord + offset[5]*2.0).a - 0.5;
    yuv[5].z = texture2D(uv_texture, v_texCoord + offset[5]*2.0).r - 0.5;
    rgb[5] = mat3( 1, 1, 1,
                 0, -0.344, 1.770,
                 1.403, -0.714, 0) * yuv[5];

    yuv[6].x = texture2D(y_texture, v_texCoord + offset[6]).r;
    yuv[6].y = texture2D(uv_texture, v_texCoord + offset[6]*2.0).a - 0.5;
    yuv[6].z = texture2D(uv_texture, v_texCoord + offset[6]*2.0).r - 0.5;
    rgb[6] = mat3( 1, 1, 1,
                    0, -0.344, 1.770,
                    1.403, -0.714, 0) * yuv[6];

    yuv[7].x = texture2D(y_texture, v_texCoord + offset[7]).r;
    yuv[7].y = texture2D(uv_texture, v_texCoord + offset[7]*2.0).a - 0.5;
    yuv[7].z = texture2D(uv_texture, v_texCoord + offset[7]*2.0).r - 0.5;
    rgb[7] = mat3( 1, 1, 1,
                    0, -0.344, 1.770,
                    1.403, -0.714, 0) * yuv[7];

    yuv[8].x = texture2D(y_texture, v_texCoord + offset[8]).r;
    yuv[8].y = texture2D(uv_texture, v_texCoord + offset[8]*2.0).a - 0.5;
    yuv[8].z = texture2D(uv_texture, v_texCoord + offset[8]*2.0).r - 0.5;
    rgb[8] = mat3( 1, 1, 1,
                    0, -0.344, 1.770,
                    1.403, -0.714, 0) * yuv[8];

    //sum += kV[i]*vec4(rgb[i], 1);
    //sum += vec4(rgb[i], 1);
    sum = kV[0]*vec4(rgb[0], 1) + kV[1]*vec4(rgb[1], 1) + kV[2]*vec4(rgb[2], 1) + kV[3]*vec4(rgb[3], 1) +
            kV[4]*vec4(rgb[4], 1) + kV[5]*vec4(rgb[5], 1) + kV[6]*vec4(rgb[6], 1) + kV[7]*vec4(rgb[7], 1) + kV[8]*vec4(rgb[8], 1);

    float hd = (sum.r + sum.g + sum.b) / 3.0;

    gl_FragColor = vec4(hd) * scaleFactor;
    //gl_FragColor = sum * scaleFactor;
    //gl_FragColor = vec4(rgb[4], 1);
}