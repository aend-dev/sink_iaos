package com.sinkleader.install.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;
import com.sinkleader.install.R;
import com.sinkleader.install.util.Constant;
import com.sinkleader.install.util.MediaManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CameraActivity extends AppCompatActivity implements AutoPermissionsListener, Constant, View.OnTouchListener {
    CameraSurfaceView cameraView;
    RelativeLayout wait;
    FileSaveThread fileSaveThread;

    private Camera mCamera = null;
    private int MAX_CAMERA_INDEX;

    String strCarinfo = "";
    boolean isBtn = false;

    float mDist = 0;

    CameraActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        activity = this;
        fileSaveThread = new FileSaveThread();

        FrameLayout previewFrame = findViewById(R.id.previewFrame);
        cameraView = new CameraSurfaceView(this);
        cameraView.setOnTouchListener(this);
        previewFrame.addView(cameraView);

        Button button = findViewById(R.id.btn_shurtter_camera);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!isBtn){
                    isBtn = true;
                    wait.setVisibility(View.VISIBLE);
                    takePicture();
                }else{
                    Log.d("shurtter_camera", "isBtn false");
                }
            }
        });

        button = findViewById(R.id.btn_cancel_camera);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        button = findViewById(R.id.btn_gallery_camera);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });

        wait = findViewById(R.id.waitting_camera);


        AutoPermissions.Companion.loadAllPermissions(this, 101);
    }

    @Override
    protected void onDestroy() {
        fileSaveThread = null;
        super.onDestroy();
    }

    public void takePicture() {
        cameraView.capture(new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                            String filePath = getFilePath();

                            camera.startPreview();
                            fileSaveThread.handler.handleMessage(filePath,bitmap);

                            runOnUiThread(()->{
                                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                File file = new File(filePath);
                                intent.setData(Uri.fromFile(file));
                                sendBroadcast(intent);

                                if (Build.VERSION_CODES.P > Build.VERSION.SDK_INT){
                                    Handler handler = new Handler(getMainLooper());
                                    handler.postDelayed(()->{
                                        runOnUiThread(()->{
                                            isBtn = false;
                                            wait.setVisibility(View.GONE);
                                        });
                                    },1000);
                                }else{
                                    isBtn = false;
                                    wait.setVisibility(View.GONE);
                                }
                            });

                            bitmap.recycle();
                            bitmap = null;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // Get the pointer ID
        Camera.Parameters params = mCamera.getParameters();
        int action = event.getAction();

        if (event.getPointerCount() > 1) {
            // handle multi-touch events
            if (action == MotionEvent.ACTION_POINTER_DOWN) {
                mDist = getFingerSpacing(event);
            } else if (action == MotionEvent.ACTION_MOVE
                    && params.isZoomSupported()) {
                mCamera.cancelAutoFocus();
                cameraView.handleZoom(event, params);
            }
        } else {
            // handle single touch events
            if (action == MotionEvent.ACTION_UP) {
                cameraView.handleFocus(event, params);
            }
        }
        return true;
    }

    private void autoFocus(){
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if(success){
//                        Toast.makeText(getApplicationContext(),"Auto Focus Success",Toast.LENGTH_SHORT).show();
                    Log.d("onAutoFocus", "Auto Focus Success");
                }
                else{
//                        Toast.makeText(getApplicationContext(),"Auto Focus Failed",Toast.LENGTH_SHORT).show();
                    Log.d("onAutoFocus", "Auto Focus Failed");
                }
            }
        });
    }

    class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
        private SurfaceHolder mHolder;


        public CameraSurfaceView(Context context) {
            super(context);

            mHolder = getHolder();
            mHolder.addCallback(this);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            mCamera = Camera.open();

            setCameraOrientation();

            try {
                mCamera.setPreviewDisplay(mHolder);
            } catch (Exception e) {
                e.printStackTrace();
            }

            mCamera.setZoomChangeListener(new Camera.OnZoomChangeListener() {
                @Override
                public void onZoomChange(int zoomValue, boolean stopped, Camera camera) {
                    Log.d("onZoomChange", "" + zoomValue + " " + stopped);
                }
            });

            MAX_CAMERA_INDEX = mCamera.getParameters().getMaxZoom();
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            mCamera.startPreview();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

        public boolean capture(Camera.PictureCallback handler) {
            if (mCamera != null) {
                mCamera.takePicture(null, null, handler);
                return true;
            } else {
                return false;
            }
        }

        public void setCameraOrientation() {
            if (mCamera == null) {
                return;
            }

            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(0, info);

            WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            int rotation = manager.getDefaultDisplay().getRotation();

            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0: degrees = 0; break;
                case Surface.ROTATION_90: degrees = 90; break;
                case Surface.ROTATION_180: degrees = 180; break;
                case Surface.ROTATION_270: degrees = 270; break;
            }

            int result;
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360;
                result = (360 - result) % 360;
            } else {
                result = (info.orientation - degrees + 360) % 360;
            }

            mCamera.setDisplayOrientation(result);
        }

        private void handleZoom(MotionEvent event, Camera.Parameters params) {
            int maxZoom = params.getMaxZoom();
            int zoom = params.getZoom();
            float newDist = getFingerSpacing(event);
            if (newDist > mDist) {
                // zoom in
                if (zoom < maxZoom)
                    zoom++;
            } else if (newDist < mDist) {
                // zoom out
                if (zoom > 0)
                    zoom--;
            }
            mDist = newDist;
            params.setZoom(zoom);
            mCamera.setParameters(params);
        }

        public void handleFocus(MotionEvent event, Camera.Parameters params) {
            int pointerId = event.getPointerId(0);
            int pointerIndex = event.findPointerIndex(pointerId);
            // Get the pointer's current position
            float x = event.getX(pointerIndex);
            float y = event.getY(pointerIndex);

            List<String> supportedFocusModes = params.getSupportedFocusModes();
            if (supportedFocusModes != null
                    && supportedFocusModes
                    .contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean b, Camera camera) {
                        // currently set to auto-focus on single touch
                    }
                });
            }
        }

    }

    private File createFile(String filePath) {
        return new File(filePath);
    }

    private String getFilePath() {
        Date currentTime = Calendar.getInstance().getTime();
//        String date_text = new SimpleDateFormat("MMdd").format(currentTime) + "_" + strCarinfo;
        String date_text = Constant.SDCARD_FOLDER;
        File folder = new File(Environment.getExternalStorageDirectory() + "/" + date_text );

        if (folder.exists() == false) {
            folder.mkdir();
        }

        Long tsLong = System.currentTimeMillis() / 1000;
        String ext = ".jpg";
        String filename = tsLong.toString() + ext;

        return Environment.getExternalStorageDirectory() + "/" + date_text + "/" + filename;
    }

    private void saveBitmap(Bitmap bitmap, File file) {
        OutputStream stream = null;

        bitmap = MediaManager.rotateImage(bitmap, 90);

        try {
            stream = new FileOutputStream(file);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (Build.VERSION_CODES.Q > Build.VERSION.SDK_INT){
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        }else{
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        }

        try {
            stream.flush();
            stream.close();
            bitmap.recycle();
            bitmap = null;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        AutoPermissions.Companion.parsePermissions(this, requestCode, permissions, this);
    }

    @Override
    public void onDenied(int requestCode, String[] permissions) {
//        Toast.makeText(this, "permissions denied : " + permissions.length, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGranted(int requestCode, String[] permissions) {
//        Toast.makeText(this, "permissions granted : " + permissions.length, Toast.LENGTH_LONG).show();
    }

    class FileSaveThread extends Thread {
        ThreadHandler handler;
        public FileSaveThread(){

            handler = new ThreadHandler();
        }

        public void run() {
            Looper.prepare();
            Looper.loop();
        }
    }

    class ThreadHandler extends Handler {
        public void handleMessage(String filePath, Bitmap bmp) {
            File file = createFile(filePath);
            saveBitmap(bmp, file);
        }
    }


    /** Determine the space between the first two fingers */
    private float getFingerSpacing(MotionEvent event) {
        // ...
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }
}