package com.demo.chad.mediarecoderdemo;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.demo.chad.view.CustomPreViewCamera;

/**
 * Created by Chad on 2017/3/28.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private HandlerThread cameraPreViewHandleThread;
    private FrameLayout fl_preview;
    private Handler cameraPreViewHandle;
    private CustomPreViewCamera customPreViewCamera;
    private Button btn_recording;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initPreViewCamera();
    }

    private void initPreViewCamera() {
        cameraPreViewHandleThread = new HandlerThread("CameraPreViewHandle");
        cameraPreViewHandleThread.start();
        cameraPreViewHandle = new Handler(cameraPreViewHandleThread.getLooper()) {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case 0:
                        if(customPreViewCamera!=null){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    fl_preview.removeAllViews();
                                }
                            });
                        }
                        break;
                    case 1:
                        if(customPreViewCamera==null){
                            customPreViewCamera = new CustomPreViewCamera(MainActivity.this);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                fl_preview.addView(customPreViewCamera);
                            }
                        });
                        break;
                }
            }
        };
        cameraPreViewHandle.sendEmptyMessage(1);
    }

    private void initView() {
        fl_preview = (FrameLayout) findViewById(R.id.fl_preview);
        btn_recording = (Button) findViewById(R.id.btn_recording);
        btn_recording.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_recording:
                if(!customPreViewCamera.isRecording()){
                    customPreViewCamera.startRecord();
                    btn_recording.setText("正在录像");
                }else{
                    customPreViewCamera.stopRecord();
                    btn_recording.setText("录像");
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(customPreViewCamera != null){
            customPreViewCamera.onResume();
            btn_recording.setText("录像");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(customPreViewCamera !=null ){
            customPreViewCamera.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(customPreViewCamera!=null){
            customPreViewCamera.onDestroy();
        }
    }
}
