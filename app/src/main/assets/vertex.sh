uniform float u_offset;
attribute vec4 a_position;
attribute vec2 a_texCoord;
varying highp vec2 v_texCoord;

void main()
{
    gl_Position = a_position;
    gl_Position.x += u_offset;
    v_texCoord = a_texCoord;
}