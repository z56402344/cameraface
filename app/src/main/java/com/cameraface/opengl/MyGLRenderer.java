package com.cameraface.opengl;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

public class MyGLRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "MyGLRenderer";
    private Context mCtx;
    private DrawTexture mDrawTexture;
//    private DrawTexture2 mDrawTexture2;
//    private DrawTexture3 mDrawTexture3;
//    private DrawTexture4 mDrawTexture4;
//    private DrawTexture5 mDrawTexture5;

    private int mW,mH;

    public MyGLRenderer(Context ctx){
        super();
        mCtx = ctx;
    }

    public void setWH(int w,int h){
        mW = w;
        mH = h;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // 设置背景颜色
//        GLES20.glClearColor(255.0f, 255.0f, 255.0f, 0f);//OpenGL支持两种颜色模式：一种是RGBA
        GLES20.glClearColor(0f, 0f, 0f, 0f);//OpenGL支持两种颜色模式：一种是RGBA

        mDrawTexture = new DrawTexture(mCtx);
//        mDrawTexture2 = new DrawTexture2(mCtx);
//        mDrawTexture3 = new DrawTexture3(mCtx);
//        mDrawTexture4 = new DrawTexture4(mCtx);
//        mDrawTexture5 = new DrawTexture5(mCtx);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 unused) {
        float[] scratch = new float[16];
        mDrawTexture.setWH(mW,mH);
        // 画背景颜色
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
//                GLES20.glClear(GLES20.GL_COLOR_CLEAR_VALUE);
        mDrawTexture.onDrawFrame(unused);
//        mDrawTexture2.onDrawFrame(unused);
//        mDrawTexture3.onDrawFrame(unused);
//        mDrawTexture4.onDrawFrame(unused);
//        mDrawTexture5.onDrawFrame(unused);
    }

    public void setFaces(Camera.Face[] faces){
        if (mDrawTexture != null && faces != null && faces.length > 0){
            mDrawTexture.setFaces(faces[0]);
        }
    }


}