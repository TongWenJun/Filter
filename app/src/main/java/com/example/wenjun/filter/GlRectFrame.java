package com.example.wenjun.filter;

import android.content.Context;
import android.media.Image;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * Created by wenjun on 17-1-12.
 */

public class GlRectFrame {

    private static final String TAG = "GlRectFrame";

    private Context context;

    private int vertexShader;
    private int fragmentShader;
    private int program;

//    private IntBuffer glBufs;
    private IntBuffer texIdBuf;
    private FloatBuffer vertexBuf;
    private FloatBuffer texCoordBuf;
    private ShortBuffer indexBuf;

    public GlRectFrame(Context context) {
        this.context = context;
    }

    public void init() {
        //init vertex
        float[] vertexs = new float[] {
                -1f, 1f, 0f, 1f,
                -1f, -1f, 0f, 1f,
                1f, -1f, 0f, 1f,
                1f, 1f, 0f, 1f
        };

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertexs.length<<2);
        byteBuffer.order(ByteOrder.nativeOrder());
        vertexBuf = byteBuffer.asFloatBuffer();
        vertexBuf.put(vertexs);
        vertexBuf.position(0);

        //init texture coord
        float[] tCoords = new float[] {
                0f, 0f,
                0f, 1f,
                1f, 1f,
                1f, 0f
        };
        byteBuffer = ByteBuffer.allocateDirect(tCoords.length<<2);
        byteBuffer.order(ByteOrder.nativeOrder());
        texCoordBuf = byteBuffer.asFloatBuffer();
        texCoordBuf.put(tCoords);
        texCoordBuf.position(0);

        //init index
        short[] indexs = new short[] {
                0, 1, 2,
                0, 2, 3
        };
        byteBuffer = ByteBuffer.allocateDirect(indexs.length<<1);
        byteBuffer.order(ByteOrder.nativeOrder());
        indexBuf = byteBuffer.asShortBuffer();
        indexBuf.put(indexs);
        indexBuf.position(0);

//        glBufs = IntBuffer.allocate(3);
//        GLES20.glGenBuffers(3, glBufs);
//
//        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, glBufs.get(0));
//        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexs.length<<2, vertexBuf, GLES20.GL_STATIC_DRAW);
//
//        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, glBufs.get(1));
//        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, tCoords.length<<2, texCoordBuf, GLES20.GL_STATIC_DRAW);
//
//        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, glBufs.get(2));
//        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, indexs.length<<1, indexBuf, GLES20.GL_STATIC_DRAW);

        //init texture
        texIdBuf = IntBuffer.allocate(2);
        GLES20.glGenTextures(2, texIdBuf);

        GLES20.glEnable(GLES20.GL_TEXTURE_2D);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texIdBuf.get(0));
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texIdBuf.get(1));
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        //init shader
        vertexShader = ShaderUtil.loadShader(GLES20.GL_VERTEX_SHADER,
                ShaderUtil.loadFromAssertsFile("vertex.sh", context.getResources()));
        fragmentShader = ShaderUtil.loadShader(GLES20.GL_FRAGMENT_SHADER,
                ShaderUtil.loadFromAssertsFile("fragment2.sh", context.getResources()));
        program = ShaderUtil.createProgram(vertexShader, fragmentShader);

        GLES20.glBindAttribLocation(program, 0, "a_position");
        GLES20.glBindAttribLocation(program, 1, "a_texCoord");

        GLES20.glClearColor(0f, 0f, 0f, 0f);
    }

    public void unInit() {
        GLES20.glDeleteTextures(2, texIdBuf);
//        GLES20.glDeleteBuffers(3, glBufs);
        GLES20.glDeleteShader(vertexShader);
        GLES20.glDeleteShader(fragmentShader);
        GLES20.glDeleteProgram(program);
    }

    public void draw(int w, int h, ByteBuffer yBuf, ByteBuffer uvBuf) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texIdBuf.get(0));
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, w, h, 0,
                GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, yBuf);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texIdBuf.get(1));
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE_ALPHA, w>>1, h>>1, 0,
                GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE, uvBuf);

        GLES20.glUseProgram(program);

        int textureUniformY = GLES20.glGetUniformLocation(program, "y_texture");
        int textureUniformU = GLES20.glGetUniformLocation(program, "uv_texture");

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texIdBuf.get(0));
        GLES20.glUniform1i(textureUniformY, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texIdBuf.get(1));
        GLES20.glUniform1i(textureUniformU, 1);

//        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, glBufs.get(0));
//        GLES20.glVertexAttribPointer(0, 4, GLES20.GL_FLOAT, false, 16, 0);
        GLES20.glVertexAttribPointer(0, 4, GLES20.GL_FLOAT, false, 16, vertexBuf);
        GLES20.glEnableVertexAttribArray(0);

//        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, glBufs.get(1));
//        GLES20.glVertexAttribPointer(1, 2, GLES20.GL_FLOAT, false, 8, 0);
        GLES20.glVertexAttribPointer(1, 2, GLES20.GL_FLOAT, false, 8, texCoordBuf);
        GLES20.glEnableVertexAttribArray(1);

//        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, glBufs.get(2));
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, indexBuf);

        GLES20.glDisableVertexAttribArray(0);
        GLES20.glDisableVertexAttribArray(1);
    }
}
