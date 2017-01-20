package com.example.wenjun.filter;

import android.content.Context;
import android.media.Image;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.example.wenjun.filter.MainActivity.previewDataSync;

/**
 * Created by wenjun on 17-1-12.
 */

public class GLRenderer implements GLSurfaceView.Renderer {

    private Context context;

    private Image drawImage;
    private int bufWidth;
    private int bufHeight;

    private ByteBuffer yBuf;
    private ByteBuffer uvBuf;

    private GlRectFrame glRectFrame;

    public GLRenderer(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glInit();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glChanged(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        synchronized (previewDataSync) {
            glDrawFrame();
        }
    }

    public void onSurfaceDestory() {
        glUninit();
    }

    public void setRenderFrame(Image image) {
        drawImage = image;

        if (null == yBuf) {
            initImageBuf();
        }

        copyDrawImageToBuf();
    }

    private void glInit() {
        if (null == glRectFrame) {
            glRectFrame = new GlRectFrame(context);
        }
        glRectFrame.init();
    }

    private void glChanged(int w, int h) {
        GLES20.glViewport(0, 0, w, h);
    }

    private void glDrawFrame() {
        if (null != drawImage) {
            glRectFrame.draw(bufWidth, bufHeight, yBuf, uvBuf);
        }
    }

    private void glUninit() {
//        frameBuf = null;
        glRectFrame.unInit();
        glRectFrame = null;
    }

    private void initImageBuf() {
        if (null != drawImage) {
            bufWidth = drawImage.getWidth();
            bufHeight = drawImage.getHeight();
            yBuf = ByteBuffer.allocateDirect(drawImage.getWidth() * drawImage.getHeight());
            uvBuf = ByteBuffer.allocateDirect(drawImage.getWidth() * drawImage.getHeight() / 2);
        }
    }

    private void copyDrawImageToBuf() {
        if (null != drawImage) {
            yBuf.put(drawImage.getPlanes()[0].getBuffer()).position(0);
            uvBuf.put(drawImage.getPlanes()[2].getBuffer()).position(0);
        }
    }
}
