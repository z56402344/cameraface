package com.cameraface;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;


/**
 * 用于通过出入的关键节点OnDraw
 */
public class FaceView extends ImageView {
    private static final String TAG = "FACE";
    private Context mContext;
    private Camera.Face[] mFaces;
    private Matrix mMatrix = new Matrix();
    private RectF mRect = new RectF();
    private Drawable mHeadDraw = null;//整个头
    private Drawable mFaceIndicatorDraw = null;//整张脸图片
    private Drawable mLeftEyeDraw = null;//左眼图片
    private Drawable mRightEyeDraw = null;//右眼图片
    private Drawable mMouthDraw = null;//嘴的图片

    private int mCameraW = 0;
    private int mCameraH = 0;

    public boolean isHeadShow = true;
    public boolean isFaceIndicatorShow = false;
    public boolean isLeftEyeShow = false;
    public boolean isRightEyeShow = false;
    public boolean isMouthShow = false;

    public FaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        //bg_rectangle_blue
        mFaceIndicatorDraw = getResources().getDrawable(R.mipmap.ic_boy);
        mHeadDraw = getResources().getDrawable(R.mipmap.ic_elk);
        mLeftEyeDraw = getResources().getDrawable(R.mipmap.ic_eye_left);
        mRightEyeDraw = getResources().getDrawable(R.mipmap.ic_eye_right);
        mMouthDraw = getResources().getDrawable(R.mipmap.ic_mouth);
    }


    public void setFaces(Camera.Face[] faces) {
        this.mFaces = faces;
        invalidate();
    }

    public void clearFaces() {
        mFaces = null;
        invalidate();
    }

    public void setCameraData(int w, int h){
        mCameraW = w;
        mCameraH = h;
    }

    public void setHeadShow(boolean headShow){
        isHeadShow = headShow;
    }

    public void setFaceIndicatorShow(boolean faceIndicatorShow){
        isFaceIndicatorShow = faceIndicatorShow;
    }

    public void setLeftEyeShow(boolean leftEyeShow){
        isLeftEyeShow = leftEyeShow;
    }

    public void setRightEyeShow(boolean rightEyeShow){
        isRightEyeShow = rightEyeShow;
    }

    public void setMouthShow(boolean mouthShow){
        isMouthShow = mouthShow;
    }

    public void clearShow(){
        isHeadShow = false;
        isFaceIndicatorShow = false;
        isLeftEyeShow = false;
        isRightEyeShow = false;
        isMouthShow = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mFaces == null || mFaces.length < 1) {
            return;
        }
        boolean isMirror = false;
//        int Id =CameraInterface.getInstance().getCameraId();
//        if(Id == CameraInfo.CAMERA_FACING_BACK){
//            isMirror = false; //后置Camera无需mirror
//        }else if(Id == CameraInfo.CAMERA_FACING_FRONT){
        isMirror = true;  //前置Camera需要mirror
//        }
//        Util.prepareMatrix(mMatrix, isMirror, 90, mCameraW, mCameraH);
        Util.prepareMatrix(mMatrix, isMirror, 90, getWidth(), getHeight());
        canvas.save();
        mMatrix.postRotate(0); //Matrix.postRotate默认是顺时针
        canvas.rotate(-0);   //Canvas.rotate()默认是逆时针
        if (mFaces == null){
            mFaceIndicatorDraw.draw(canvas);
            mLeftEyeDraw.draw(canvas);
            mRightEyeDraw.draw(canvas);
            mMouthDraw.draw(canvas);
        }else {
            for (int i = 0; i < mFaces.length; i++) {
                Camera.Face face = mFaces[i];
                mRect.set(face.rect);
                mMatrix.mapRect(mRect);//计算出在父布局的真是坐标
                //识别的Rect 系数，使装饰图片根据人脸与摄像头的距离放大或者缩小
                float dx = mRect.bottom - mRect.top;
                if (isHeadShow){
                    Log.i(TAG, "FaceView  dx=" +dx );
                    float h = dx * 0.4f;
                    mHeadDraw.setBounds(Math.round(mRect.left), Math.round(mRect.top - h),
                            Math.round(mRect.right), Math.round(mRect.bottom - h));
                    mHeadDraw.draw(canvas);
                }

                if (isFaceIndicatorShow){
                    mFaceIndicatorDraw.setBounds(Math.round(mRect.left), Math.round(mRect.top),
                            Math.round(mRect.right), Math.round(mRect.bottom));
                    mFaceIndicatorDraw.draw(canvas);
                }

                if (isLeftEyeShow){
                    float[] leftEye = {face.leftEye.x, face.leftEye.y};
                    mMatrix.mapPoints(leftEye);//计算出在父布局的真是坐标
                    float h = dx * 0.1f;
                    float w = dx * 0.15f;
                    mLeftEyeDraw.setBounds(Math.round(leftEye[0]-w), Math.round(leftEye[1]-h),
                            Math.round(leftEye[0]+w), Math.round(leftEye[1]+h));
                    mLeftEyeDraw.draw(canvas);
                }

                if (isRightEyeShow){
                    float[] rightEye = {face.rightEye.x, face.rightEye.y};
                    float w = dx * 0.15f;
                    float h = dx * 0.1f;
                    mMatrix.mapPoints(rightEye);//计算出在父布局的真是坐标
                    mRightEyeDraw.setBounds(Math.round(rightEye[0]-w), Math.round(rightEye[1]-h),
                            Math.round(rightEye[0]+w), Math.round(rightEye[1]+h));
                    mRightEyeDraw.draw(canvas);
                }

                if (isMouthShow){
                    float[] mouthP = {face.mouth.x, face.mouth.y};
                    mMatrix.mapPoints(mouthP);//计算出在父布局的真是坐标
                    float w = dx * 0.15f;
                    float h = dx * 0.2f;
                    mMouthDraw.setBounds(Math.round(mouthP[0]-w), Math.round(mouthP[1]-h),
                            Math.round(mouthP[0]+w), Math.round(mouthP[1]));
                    mMouthDraw.draw(canvas);
                }
            }
        }
        canvas.restore();
        super.onDraw(canvas);
    }
}
