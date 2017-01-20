package com.example.wenjun.filter;

import android.media.Image;
import android.media.ImageReader;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    public static Object[] previewDataSync = new Object[0];

    private Camera2 camera2;

    private RenderView renderView;
    private GLRenderer renderer;

    private Camera2.CameraOpenCallback cameraOpenCallback = new Camera2.CameraOpenCallback() {
        @Override
        public void opened() {
            camera2.startPreview(null, renderView.getWidth(), renderView.getHeight(), startPreviewCallback);
        }

        @Override
        public void disconnected() {

        }

        @Override
        public void error(int i) {

        }
    };

    private Camera2.CameraStartPreviewCallback startPreviewCallback = new Camera2.CameraStartPreviewCallback() {
        @Override
        public void success() {
            Log.i(TAG, "start preview success");
        }

        @Override
        public void failed() {
            Log.i(TAG, "start preview failed");
        }
    };

    private ImageReader.OnImageAvailableListener previewDataAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader imageReader) {
            Image image = imageReader.acquireNextImage();
            if (null != image) {
                //process image
                Log.i(TAG, "preview data " + image.getWidth() + "x" + image.getHeight() + "---");

                renderer.setRenderFrame(image);
                renderView.requestRender();
                synchronized (previewDataSync) {
                    image.close();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        setContentView(R.layout.activity_main);

        renderView = (RenderView) findViewById(R.id.render_view);
        renderer = new GLRenderer(this);

        renderView.setRenderer(renderer);
        renderView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        renderView.setKeepScreenOn(true);

        camera2 = new Camera2(this);
        camera2.setOnPreviewDataAvailableListener(previewDataAvailableListener);
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();

        renderView.post(new Runnable() {
            @Override
            public void run() {
                camera2.openCamera("0", cameraOpenCallback);
            }
        });
//        camera2.openCamera("0", cameraOpenCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();

        camera2.closeCamera();
    }
}
