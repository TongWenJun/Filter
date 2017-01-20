package com.example.wenjun.filter;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by wenjun on 17-1-11.
 */

public class RenderView extends GLSurfaceView {

    private GLRenderer renderer;

    public RenderView(Context context) {
        super(context);
        init();
    }

    public RenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setEGLContextClientVersion(2);
    }

    @Override
    public void setRenderer(Renderer renderer) {
        this.renderer = (GLRenderer) renderer;
        super.setRenderer(renderer);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        if (null != renderer) {
            renderer.onSurfaceDestory();
            renderer = null;
        }
    }
}
