package com.cameraface.opengl;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.cameraface.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by duguang on 18-9-28.
 */
public class DrawTexture {

    private static final String VERTEX_SHADER =
            "uniform mat4 u_MVPMatrix;" +
                    "attribute vec4 a_position;" +
                    "attribute vec2 a_texCoord;" +
                    "varying vec2 v_texCoord;" +
                    "void main() {" +
                    "  gl_Position = a_position;" +
                    "  v_texCoord = a_texCoord;" +
                    "}";

    private static final String FRAGMENT_SHADER =
            "precision lowp float;" +
                    "varying vec2 v_texCoord;" +
                    "uniform sampler2D u_samplerTexture;" +
                    "void main() {" +
                    "  gl_FragColor = texture2D(u_samplerTexture, v_texCoord);" +
                    "}";

    private float[] quadVertex = new float[]{
            -1.0f, 0.296f, 0.0f, // Position 0
            0, 0, // TexCoord 1
            -1f, -0.296f, 0.0f, // Position 1
            0, 1.0f, // TexCoord 0
            1f, -0.296f, 0.0f, // Position 2
            1.0f, 1.0f, // TexCoord 3
            1f, 0.296f, 0.0f, // Position 3
            1.0f, 0, // TexCoord 2
    };
    private short[] quadIndex = new short[]{
            0, 1, 2, // 0号点，1号点，2号点组成一个三角形
            0, 2, 3, // 0号点，2号点，3号点组成一个三角形
    };

    private float[] TEX_VERTEX = new float[]{
            0, 0, // TexCoord 1
            0, 1.0f, // TexCoord 0
            1.0f, 1.0f, // TexCoord 3
            1.0f, 0, // TexCoord 2
    };

    private FloatBuffer mVertexBuffer;
    private ShortBuffer mIndexBuffer;
    private FloatBuffer mTexVertexBuffer;
    private final Context mContext;
    private final float[] mMVPMatrix = new float[16];

    private int mProgram;
    private int attribPosition;
    private int mMatrixHandle;
    private int attribTexCoord;
    private int uniformTexture;
    private int[] textureId;

    public DrawTexture(Context context) {
        super();
        mContext = context;
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);
// Active the texture unit 0
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        loadVertex();
        initShader();
        loadTexture();

    }

    private void loadVertex() {
        // float size = 4
        this.mVertexBuffer = ByteBuffer.allocateDirect(quadVertex.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        this.mVertexBuffer.put(quadVertex).position(0);
        // short size = 2
        this.mIndexBuffer = ByteBuffer.allocateDirect(quadIndex.length * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer();
        this.mIndexBuffer.put(quadIndex).position(0);

        this.mTexVertexBuffer = ByteBuffer.allocateDirect(TEX_VERTEX.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        this.mTexVertexBuffer.put(TEX_VERTEX).position(0);
    }

    private void initShader() {

//        String vertexSource = Tools.readFromAssets("VertexShader.glsl");
//        String fragmentSource = Tools.readFromAssets("FragmentShader.glsl");
        // Load the shaders and get a linked program
        mProgram = loadProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        // Get the attribute locations
        attribPosition = GLES20.glGetAttribLocation(mProgram, "a_position");
        attribTexCoord = GLES20.glGetAttribLocation(mProgram, "a_texCoord");
        uniformTexture = GLES20.glGetUniformLocation(mProgram,
                "u_samplerTexture");
        GLES20.glUseProgram(mProgram);
        GLES20.glEnableVertexAttribArray(attribPosition);
        GLES20.glEnableVertexAttribArray(attribTexCoord);
        // Set the sampler to texture unit 0
        GLES20.glUniform1i(uniformTexture, 0);
    }

    public static int loadProgram(String vertexSource, String fragmentSource) {

        // Load the vertex shaders
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        // Load the fragment shaders
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        // Create the program object
        int program = GLES20.glCreateProgram();
        if (program == 0) {
            throw new RuntimeException("Error create program.");
        }
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        // Link the program
        GLES20.glLinkProgram(program);
        int[] linked = new int[1];
        // Check the link status
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linked, 0);
        if (linked[0] == 0) {
            GLES20.glDeleteProgram(program);
            throw new RuntimeException("Error linking program: " +
                    GLES20.glGetProgramInfoLog(program));
        }
        // Free up no longer needed shader resources
        GLES20.glDeleteShader(vertexShader);
        GLES20.glDeleteShader(fragmentShader);
        return program;
    }

    public static int loadShader(int shaderType, String source) {

        // Create the shader object
        int shader = GLES20.glCreateShader(shaderType);
        if (shader == 0) {
            throw new RuntimeException("Error create shader.");
        }
        int[] compiled = new int[1];
        // Load the shader source
        GLES20.glShaderSource(shader, source);
        // Compile the shader
        GLES20.glCompileShader(shader);
        // Check the compile status
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            GLES20.glDeleteShader(shader);
            throw new RuntimeException("Error compile shader: " +
                    GLES20.glGetShaderInfoLog(shader));
        }
        return shader;
    }

    public int[] loadTexture() {
        textureId = new int[1];
        // Generate a texture object
        GLES20.glGenTextures(1, textureId, 0);
        int[] result = null;
        if (textureId[0] != 0) {
//            InputStream is = Tools.readFromAsserts(path);
//            Bitmap bitmap;
//            try {
//                bitmap = BitmapFactory.decodeStream(is);
//            } finally {
//                try {
//                    is.close();
//                } catch (IOException e) {
//                    throw new RuntimeException("Error loading Bitmap.");
//                }
//            }
            Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_auto);
            result = new int[3];
            result[0] = textureId[0]; // TEXTURE_ID
            result[1] = bitmap.getWidth(); // TEXTURE_WIDTH
            result[2] = bitmap.getHeight(); // TEXTURE_HEIGHT
            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0]);
            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                    GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                    GLES20.GL_CLAMP_TO_EDGE);
            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
        } else {
            throw new RuntimeException("Error loading texture.");
        }
        return result;
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {


    }

    //绘制每一帧的时候回调
    public void onDrawFrame(GL10 unused) {
        // clear screen to black
//        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0]);
        mVertexBuffer.position(0);
        // load the position
        // 3(x , y , z)
        // (2 + 3 )* 4 (float size) = 20
        GLES20.glVertexAttribPointer(attribPosition,  3, GLES20.GL_FLOAT, false, 20, mVertexBuffer);
        mVertexBuffer.position(3);
        // load the texture coordinate
        GLES20.glVertexAttribPointer(attribTexCoord, 2, GLES20.GL_FLOAT, false, 20, mVertexBuffer);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, mIndexBuffer);

    }


}
