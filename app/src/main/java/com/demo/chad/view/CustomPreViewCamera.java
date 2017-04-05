package com.demo.chad.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by YoHann on 2017/3/28.
 */

public class CustomPreViewCamera extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = CustomPreViewCamera.class.getSimpleName();
    private Context mContext;
    //取景控制器
    private SurfaceHolder holder;
    private Camera mCamera;
    //屏幕宽高
    private int screenWidth,screenHeight;
    //是否正在录像
    private boolean isRecording = false;
    private MediaRecorder mediaRecorder;
    //相机id=0 前置 1：后置
    private int mCameraID = 0;

    public CustomPreViewCamera(Context context) {
        super(context);
        mContext = context;
        //通过windowmanager得到屏幕宽高
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        //得到surfaceView 控制器
        holder = getHolder();
        //添加回调控制
        holder.addCallback(this);
    }

    /**
     * surface创建的时候调用
     *
     * @param surfaceHolder
     */
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            if (mCamera == null) {
                //打开摄像头  默认 前置=0
                mCamera = Camera.open(mCameraID);
//                mCamera = Camera.open();
            }
            if (mCamera != null) {
                //通過控制器設置預覽顯示（通过surfaceview显示取景画面）  一定要設置，否則黑屏
                mCamera.setPreviewDisplay(holder);
                setCameraParameter();
                //开启预览界面，开启之前可设置相机一系列参数 如预览界面大小，相机拍摄大小，相机方向
                mCamera.startPreview();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
        surface创建时调用
     */
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        if (mCamera != null) {
            setCameraParameter();
            mCamera.startPreview();
        }
    }

    /*
        surface销毁时调用
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (mCamera != null) {
//            mCamera.stopPreview();
        }
    }

    private void setCameraParameter() {
        //得到相機默認參數
        Camera.Parameters parameters = mCamera.getParameters();
        //設置拍照格式
        parameters.setPictureFormat(PixelFormat.JPEG);
        //圖片質量
        parameters.set("jpeg-quality", 85);
        //設置對焦模式 視頻自動連續對焦
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        //相機支持的預覽大小
        List<Camera.Size> supportedPreviewSizes = parameters
                .getSupportedPreviewSizes();
        //得到最佳大小
        Camera.Size size = getBestSize(supportedPreviewSizes);
        //设置最佳预览大小（目的兼容android机型）
        if (null != size) {
            Log.i(TAG, "PreviewSize, w = " + size.width + ", h = "
                    + size.height);
            parameters.setPreviewSize(size.width, size.height);
        } else {
            Log.w(TAG, "no PreviewSize");
        }
        setCameraDisplayOrientation((Activity) mContext, mCameraID, mCamera);
        mCamera.setParameters(parameters);
        mCamera.cancelAutoFocus();
    }

    public void onResume() {
        if (mCamera == null) {
            try {
                mCamera = Camera.open(mCameraID);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private Camera.Size getBestSize(List<Camera.Size> sizes) {
        /*
            先将获取手机支持预览的尺寸列表通过parmeters.getSupportPreviewSize返回的是一个集合。
            进行屏幕方向的判断，因为预览的尺寸都是w>h 如果是竖屏，则需要将宽和高进行调换。
            将预览尺寸列表的每个元素的宽和高与SurfaceView的宽和高进行比较，如果存在宽和高尺寸SurfaceView的宽和高，
            相同的size，则将当前的宽高设置为预览尺寸。
            如果没有找到，则将尺寸列表的比例和SUrfaceView的比例做比较，找一个相同或者相近的 。
         */
        if (sizes != null && sizes.size() > 0) {
            int width = 0, height = 0;
            if ((sizes.get(0).height > sizes.get(0).width && screenHeight > screenWidth)
                    || (sizes.get(0).height < sizes.get(0).width && screenHeight < screenWidth)) {
                width = screenWidth;
                height = screenHeight;
            } else {
                width = screenHeight;
                height = screenWidth;
            }
            float rate = (float) height / (float) width;
            if (rate > 1) {
                rate = 1.0f / rate;
            }
            for (int i = 0; i < sizes.size(); i++) {
                Camera.Size size = sizes.get(i);
                if (size.width == width && size.height == height) {
                    return size;
                }
            }
            for (int i = 0; i < sizes.size(); i++) {
                Camera.Size size = sizes.get(i);
                if (Math.abs(rate - (float) size.height / (float) size.width) < 0.05f) {
                    return size;
                }
            }
        }
        return null;
    }

    public void setCameraDisplayOrientation(Activity activity,
                                            int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 90;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    public void onPause() {
        if (mCamera != null) {
            releaseMediaRecorder();
            mCamera.stopPreview();
        }
    }

    public void onDestroy() {
        if (mCamera != null) {
            releaseMediaRecorder();
            mCamera.release();
        }
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void startRecord() {
        if (preVideo()) {
            mediaRecorder.start();
        }
        isRecording = true;
    }

    public void stopRecord() {
        releaseMediaRecorder();
        isRecording = false;
    }

    public boolean preVideo() {
        if (mCamera == null) {
            return false;
        }
        mediaRecorder = new MediaRecorder();
        //解鎖相機
        mCamera.unlock();
        //設置相機
        mediaRecorder.setCamera(mCamera);
        //設置音頻源 相機自帶mic(錄音)
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        //設置視頻源 相機
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        //設置保存相機錄制配置信息 質量等級（分辨率）
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));
        //创建保存文件并設置保存文件
        createFile();
        mediaRecorder.setOutputFile(createFile().toString());
        mediaRecorder.setVideoSize(640, 480);
        //設置錄制前的方向(保存视频的方向，即播放视频的方向)
        mediaRecorder.setOrientationHint(90);
        mediaRecorder.setPreviewDisplay(getHolder().getSurface());
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private File createFile() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            File filedir = new File(Environment.getExternalStorageDirectory(), "MediaRecoderDemo");
            if (!filedir.exists()) {
                filedir.mkdir();
            }
            String currenttime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File file = new File(filedir.getAbsolutePath() + File.separator + currenttime + ".mp4");
            return file;
        }
        return null;
    }

    public void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }
}
