package com.example.wenjun.filter;

import android.content.res.Resources;
import android.opengl.GLES20;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by wenjun on 17-1-12.
 */

public class ShaderUtil {
    private static final String TAG = "ShaderUtil";

    public static void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
//            throw new RuntimeException(op + ": glError " + error);
        }
    }

    public static String loadFromAssertsFile(String fname, Resources resources) {
        String result = null;

        try {
            InputStream is = resources.getAssets().open(fname);
            int count = 0;
            byte[] tmpBuf = new byte[1024];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((count = is.read(tmpBuf)) != -1) {
                baos.write(tmpBuf, 0, count);
            }
            byte[] buff = baos.toByteArray();
            baos.close();
            is.close();

            result = new String(buff, "UTF-8");
            result = result.replaceAll("\\r\\n", "\n");
//            result = new String(buff);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        checkGlError("loadShader 1");
        if (0 != shader) {
            GLES20.glShaderSource(shader, source);
            checkGlError("loadShader 2");
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compiled shader " + shaderType + ":" + GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }

        return shader;
    }

    public static int createProgram(int vertexShader, int fragmentShader) {
        int program = GLES20.glCreateProgram();
        if (0 != program) {
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader vertex");

            GLES20.glAttachShader(program, fragmentShader);
            checkGlError("glAttachShader fragment");

            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program : " + GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }

        return program;
    }
}
