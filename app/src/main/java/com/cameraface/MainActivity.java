package com.cameraface;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.cameraface.camera.CameraView;
import com.cameraface.opengl.MyGLSurfaceView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "MainActivity";

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    public static int AC_DRAW = 2;
    public static int AC_CAMERA_DATA = 3;

    private FaceView mIvFace;
    private CameraView mCameraView;
    private MyGLSurfaceView mGLView;
    private RelativeLayout mRlSurface;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Camera.Face[] faces = null;
            if (msg.what == AC_DRAW) {
                faces = (Camera.Face[]) msg.obj;
                mIvFace.setFaces(faces);
            } else if(msg.what == AC_CAMERA_DATA) {
                mIvFace.setCameraData(msg.arg1, msg.arg2);
            }
            if (mGLView != null){
                mGLView.requestRenderAndFace(faces);
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCameraView = findViewById(R.id.camera);
        mIvFace = findViewById(R.id.iv_face_pic);
        mGLView = new MyGLSurfaceView(this);
        mRlSurface = findViewById(R.id.mRlSurface);
        mRlSurface.addView(mGLView);

        if (mCameraView != null) {
            mCameraView.init(mHandler);
//            mCameraView.addCallback(mCallback);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            mCameraView.start();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ConfirmationDialogFragment
                    .newInstance("获取相机权限失败",
                            new String[]{Manifest.permission.CAMERA},
                            REQUEST_CAMERA_PERMISSION,
                            "没有相机权限，app不能为您进行脸部检测")
                    .show(getSupportFragmentManager(), "");
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }
        mGLView.onResume();
    }

    @Override
    protected void onPause() {
        mCameraView.stop();
        mGLView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
        case REQUEST_CAMERA_PERMISSION:
            if (permissions.length != 1 || grantResults.length != 1) {
                throw new RuntimeException("Error on requesting camera permission.");
            }
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "获取到拍照权限",
                        Toast.LENGTH_SHORT).show();
            }
            // No need to start camera here; it is handled by onResume
            break;
        }
    }

    @Override
    public void onClick(View v) {
        if (mIvFace == null){
            return;
        }
        switch (v.getId()){
        case R.id.mBtnHead:
            mIvFace.setHeadShow(!mIvFace.isMouthShow);
            break;
        case R.id.mBtnFace:
            mIvFace.setFaceIndicatorShow(!mIvFace.isFaceIndicatorShow);
            break;
        case R.id.mBtnLeftEye:
            mIvFace.setLeftEyeShow(!mIvFace.isLeftEyeShow);
            break;
        case R.id.mBtnRightEye:
            mIvFace.setRightEyeShow(!mIvFace.isRightEyeShow);
            break;
        case R.id.mBtnMouth:
            mIvFace.setMouthShow(!mIvFace.isMouthShow);
            break;
        case R.id.mBtnClear:
            mIvFace.clearShow();
            break;
        }
    }

    public static class ConfirmationDialogFragment extends DialogFragment {

        private static final String ARG_MESSAGE = "message";
        private static final String ARG_PERMISSIONS = "permissions";
        private static final String ARG_REQUEST_CODE = "request_code";
        private static final String ARG_NOT_GRANTED_MESSAGE = "not_granted_message";

        public static ConfirmationDialogFragment newInstance(String message,
                                                             String[] permissions, int requestCode, String notGrantedMessage) {
            ConfirmationDialogFragment fragment = new ConfirmationDialogFragment();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            args.putStringArray(ARG_PERMISSIONS, permissions);
            args.putInt(ARG_REQUEST_CODE, requestCode);
            args.putString(ARG_NOT_GRANTED_MESSAGE, notGrantedMessage);
            fragment.setArguments(args);
            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Bundle args = getArguments();
            return new AlertDialog.Builder(getActivity())
                    .setMessage(args.getString(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String[] permissions = args.getStringArray(ARG_PERMISSIONS);
                                    if (permissions == null) {
                                        throw new IllegalArgumentException();
                                    }
                                    ActivityCompat.requestPermissions(getActivity(),
                                            permissions, args.getInt(ARG_REQUEST_CODE));
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(getActivity(),
                                            args.getString(ARG_NOT_GRANTED_MESSAGE),
                                            Toast.LENGTH_SHORT).show();
                                }
                            })
                    .create();
        }

    }
}
