# CameraFace

Android Camera 内置人脸识别的Demo

主要方法
通过设置回调
mCamera.setFaceDetectionListener(mFaceDetectionListener);

class  FaceDetectionListener implements Camera.FaceDetectionListener{

        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera camera) {
            Message m = mHandler.obtainMessage();
            m.what = MainActivity.AC_DRAW;
            if (null == faces || faces.length == 0) {
                m.obj = null;
                Log.d("face", "onFaceDetection : There is no face found.");
            } else {
                Log.d("face", "onFaceDetection : face found.");
                m.obj = faces;
                for (int i = 0; i < faces.length; i++) {
                    Camera.Face face = faces[i];
                    if (face == null)return;
                    Rect rect = face.rect;
                    Log.i("face","face.score="+face.score);
                    Log.i("face","rect.left="+rect.left+"\nrect.top="+rect.top+"\nrect.right="+rect.right+"\nrect.bottom="+rect.bottom);
                    Log.i("face","id="+face.id+" \nface.leftEye.x="+face.leftEye.x+" \nface.leftEye.y"+face.leftEye.y+" \nface.mouth.x="+face.mouth.x+" \nface.mouth.y="+face.mouth.y);
                }
            }
            m.sendToTarget();
        }
    }


	//faces 识别出的人脸特征数据
	public static class Face {
		public int id;//对应id
		public Point leftEye;//左眼
		public Point mouth;//嘴
		public Rect rect;//脸部坐标
		public Point rightEye;//右眼
		public int score;//识别分数
	}

	//绘制对应的装饰图片
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
