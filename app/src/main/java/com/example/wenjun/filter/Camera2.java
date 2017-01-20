package com.example.wenjun.filter;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by wenjun on 17-1-11.
 */

public class Camera2 {

    private static final String TAG = "Camera2";

    private Context context;
    private HandlerThread handlerThread;
    private Handler bgHandler;

    private Semaphore cameraOpenCloseLock = new Semaphore(1);
    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private String cameraId;
    private CameraCharacteristics cameraCharacteristics;
    private CameraCaptureSession.CaptureCallback previewCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
        }

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
        }
    };

    private Size previewSize;
    private ImageReader previewReader;
    private ImageReader.OnImageAvailableListener previewAvailable = null;

    private ImageReader captureReader;

    public interface CameraOpenCallback {
        public void opened();
        public void disconnected();
        public void error(int i);
    }

    public interface CameraStartPreviewCallback {
        public void success();
        public void failed();
    }

    public Camera2(Context context) {
        this.context = context;
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }

    public void openCamera(String cameraId, final CameraOpenCallback openCallback) {
        startBgThread();

        String[] cameraIds = null;
        try {
            cameraIds = cameraManager.getCameraIdList();
            for (String tmpCameraId : cameraIds) {
                if (tmpCameraId.equals(cameraId)) {
                    this.cameraId = cameraId;
                    break;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        if (null == this.cameraId) {
            throw new RuntimeException("input error camerad id " + cameraId);
        }

        try {
            cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
                @Override
                public void onOpened(CameraDevice cameraDevice) {
                    cameraOpenCloseLock.release();

                    Camera2.this.cameraDevice = cameraDevice;
                    if (null != openCallback) {
                        openCallback.opened();
                    }
                }

                @Override
                public void onDisconnected(CameraDevice cameraDevice) {
                    cameraOpenCloseLock.release();

                    if (null != openCallback) {
                        openCallback.disconnected();
                    }
                }

                @Override
                public void onError(CameraDevice cameraDevice, int i) {
                    cameraOpenCloseLock.release();

                    if (null != openCallback) {
                        openCallback.error(i);
                    }
                }
            };

            try {
                if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                    throw new RuntimeException("Timeout waitting to lock camera open");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            cameraManager.openCamera(cameraId, stateCallback, bgHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void closeCamera() {
        Log.i(TAG, "closeCamera <---");
        boolean acquireException = false;
        try {
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                acquireException = true;
                throw new RuntimeException("Timeout waitting to lock camera close");
            }

            if (null != cameraCaptureSession) {
                cameraCaptureSession.stopRepeating();
                cameraCaptureSession.close();
                cameraCaptureSession = null;
            }

            if (null != cameraDevice) {
                cameraDevice.close();
                cameraDevice = null;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } finally {
            if (!acquireException) {
                cameraOpenCloseLock.release();
                stopBgThread();
            }
            Log.i(TAG, "closeCamera --->");
        }
    }

    public void setOnPreviewDataAvailableListener(ImageReader.OnImageAvailableListener listener) {
        this.previewAvailable = listener;
    }

    Surface previewSurface = null;
    public void startPreview(Surface surface, int surfaceWidth, int surfaceHeight, final CameraStartPreviewCallback startPreviewCallback) {
        StreamConfigurationMap streamConfigurationMap =
                cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        previewSize = getPreviewSize(streamConfigurationMap, surfaceWidth, surfaceHeight);
        setupPreviewOutputBuffer();

        try {
            final CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            //create display surface
            List<Surface> surfaces = null;
            if (null != surface) {
                previewSurface = surface;
                surfaces = Arrays.asList(surface, previewReader.getSurface(), captureReader.getSurface());
            } else {
                previewSurface = previewReader.getSurface();
                surfaces = Arrays.asList(previewReader.getSurface(), captureReader.getSurface());
            }

            //create cameraCaptureSession
            if (null != cameraCaptureSession) {
                cameraCaptureSession.abortCaptures();
                cameraCaptureSession = null;
            }

            cameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    if (null == cameraDevice) {
                        return;
                    }

                    Camera2.this.cameraCaptureSession = cameraCaptureSession;

                    builder.addTarget(previewSurface);
                    builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

                    try {
                        cameraCaptureSession.setRepeatingRequest(builder.build(), previewCaptureCallback, bgHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }

                    if (null != startPreviewCallback) {
                        startPreviewCallback.success();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    if (null != startPreviewCallback) {
                        startPreviewCallback.failed();
                    }
                }
            }, bgHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void stopPreview() {
        Log.i(TAG, "stopPreview <---");
        try {
            if (null != cameraCaptureSession) {
                cameraCaptureSession.stopRepeating();
                cameraCaptureSession = null;
            }

            if (null != previewReader) {
                previewReader.setOnImageAvailableListener(null, bgHandler);
                previewReader.close();
                previewReader = null;
            }

            if (null != captureReader) {
                captureReader.close();
                captureReader = null;
            }
        }
        catch (CameraAccessException e) {
            e.printStackTrace();
        }
        finally {
            Log.i(TAG, "stopPreview --->");
        }
    }

    private void startBgThread() {
        handlerThread = new HandlerThread("bg");
        handlerThread.start();
        bgHandler = new Handler(handlerThread.getLooper());
    }

    private void stopBgThread() {
        if (null != handlerThread) {
            bgHandler = null;
            handlerThread.quitSafely();

            try {
                handlerThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            handlerThread = null;
        }
    }

    private Size getPreviewSize(StreamConfigurationMap map, int surfaceWidth, int surfaceHeight) {

        List<Size> bigEnough = new ArrayList<>();
        List<Size> notBigEnough = new ArrayList<>();

        float surfaceRotio = surfaceHeight*1.0f / surfaceWidth;
        int minWidth;
        int minHeight;
        if (Math.abs(surfaceRotio - (4.0f/3)) < Math.abs(surfaceRotio - 16.0f/9)) {
            minWidth = 4;
            minHeight = 3;
        } else {
            minWidth = 16;
            minHeight = 9;
        }

        Size[] sizes = map.getOutputSizes(ImageFormat.YUV_420_888);
        for (Size size : sizes) {
            if (minHeight == minWidth*size.getHeight()/size.getWidth()) {
                if (size.getWidth() >= surfaceWidth) {
                    bigEnough.add(size);
                } else {
                    notBigEnough.add(size);
                }
            }
        }

        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizeByArea());
        } else {
            return Collections.max(notBigEnough, new CompareSizeByArea());
        }

    }

    private void setupPreviewOutputBuffer() {
        previewReader = ImageReader.newInstance(previewSize.getWidth(), previewSize.getHeight(), ImageFormat.YUV_420_888, 3);
        previewReader.setOnImageAvailableListener(previewAvailable, bgHandler);

        captureReader = ImageReader.newInstance(previewSize.getWidth(), previewSize.getHeight(), ImageFormat.YUV_420_888, 3);
    }

    static class CompareSizeByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long)lhs.getWidth() * lhs.getHeight() - (long)rhs.getWidth() * rhs.getHeight());
        }
    }
}
