package com.example.jdxtrial2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.badlogic.gdx.backends.android.AndroidGL20;


import com.example.jdxtrial2.ErrorHandler.ErrorType;
import com.example.jdxtrial2.ErrorHandler;
import com.example.jdxtrial2.MainActivity;
import com.learnopengles.android.common.BOResourceReader;
import com.learnopengles.android.common.RawResourceReader;
import com.learnopengles.android.common.ShaderHelper;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

public class JDXTrial2Renderer implements GLSurfaceView.Renderer {
	
	private final MainActivity jDXTrial2Activity;
	private final ErrorHandler errorHandler;
	
	private final AndroidGL20 glEs20;
	
	private final float[] modelMatrix = new float[16];
	private final float[] viewMatrix = new float[16];
	private final float[] projectionMatrix = new float[16];
	private final float[] mvpMatrix = new float[16];
	private final float[] lightModelMatrix = new float[16];
	private final float[] temporaryMatrix = new float[16];
	
	private int mvpMatrixUniform;
	private int mvMatrixUniform;
	private int positionAttribute;
	private int normalAttribute;
	private int lightPosUniform;
	private int pointMVPMatrixHandle;
	private int pointPositionHandle;
	private int colorUniform;
	
	private static final String MVP_MATRIX_UNIFORM = "u_MVPMatrix";
	private static final String MV_MATRIX_UNIFORM = "u_MVMatrix";
	private static final String LIGHT_POSITION_UNIFORM = "u_LightPos";
	private static final String COLOR_UNIFORM = "u_Color";
	
	private static final String POSITION_ATTRIBUTE = "a_Position";
	private static final String NORMAL_ATTRIBUTE = "a_Normal";
	
	private static final int POSITION_DATA_SIZE_IN_ELEMENTS = 3;
	private static final int NORMAL_DATA_SIZE_IN_ELEMENTS = 3;
	private static final int COLOR_DATA_SIZE_IN_ELEMENTS = 4;

	private static final int BYTES_PER_FLOAT = 4;
	private static final int BYTES_PER_SHORT = 2;
	
	private static final int STRIDE = (POSITION_DATA_SIZE_IN_ELEMENTS + NORMAL_DATA_SIZE_IN_ELEMENTS)
			* BYTES_PER_FLOAT;
	
	private final int jVBORawData = R.raw.j_vbodata;
	private final int jIBORawData = R.raw.j_ibodata;
	private final int dVBORawData = R.raw.d_vbodata;
	private final int dIBORawData = R.raw.d_ibodata;
	private final int xVBORawData = R.raw.x_vbodata;
	private final int xIBORawData = R.raw.x_ibodata;
	
	private final float[] lightPosInModelSpace = new float[] { 0.0f, 0.0f, 0.0f, 1.0f };
	private final float[] lightPosInWorldSpace = new float[4];
	private final float[] lightPosInEyeSpace = new float[4];
	
	private int letterProgram;
	private int lightProgram;
	
	private LetterMap jMap;
	private LetterMap dMap;
	private LetterMap xMap;
	
	
	public JDXTrial2Renderer(final MainActivity jDXTrial2Activity, ErrorHandler errorHandler) {
		this.jDXTrial2Activity = jDXTrial2Activity;
		this.errorHandler = errorHandler;
		glEs20 = new AndroidGL20();
	}
	
	public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
		jMap = new LetterMap(jVBORawData,jIBORawData);
		dMap = new LetterMap(dVBORawData,dIBORawData);
		xMap = new LetterMap(xVBORawData,xIBORawData);
		
		
		// Set the background clear color to black.
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		// Enable depth testing
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glDisable(GLES20.GL_CULL_FACE);

		// Position the eye in front of the origin.
		final float eyeX = 0.0f;
		final float eyeY = 0.0f;
		final float eyeZ = -0.5f;

		// We are looking toward the distance
		final float lookX = 0.0f;
		final float lookY = 0.0f;
		final float lookZ = -5.0f;

		// Set our up vector. This is where our head would be pointing were we
		// holding the camera.
		final float upX = 0.0f;
		final float upY = 1.0f;
		final float upZ = 0.0f;

		// Set the view matrix. This matrix can be said to represent the camera
		// position.
		// NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination
		// of a model and view matrix. In OpenGL 2, we can keep track of these
		// matrices separately if we choose.
		Matrix.setLookAtM(viewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
		
		//Read, compile, and link letter shaders
		final String letterVertexShader = RawResourceReader.readTextFileFromRawResource(jDXTrial2Activity,
				R.raw.jdx_trial_2_vertex_shader);
		final String letterFragmentShader = RawResourceReader.readTextFileFromRawResource(jDXTrial2Activity,
				R.raw.jdx_trial_2_fragment_shader);

		final int letterVertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, letterVertexShader);
		final int letterFragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, letterFragmentShader);

		letterProgram = ShaderHelper.createAndLinkProgram(letterVertexShaderHandle, letterFragmentShaderHandle, new String[] {
				POSITION_ATTRIBUTE, NORMAL_ATTRIBUTE});
		
		//Same with Light shaders
		final String vertexShader = RawResourceReader.readTextFileFromRawResource(jDXTrial2Activity,
				R.raw.point_vertex_shader);
		final String fragmentShader = RawResourceReader.readTextFileFromRawResource(jDXTrial2Activity,
				R.raw.point_fragment_shader);

		final int vertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);
		final int fragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);

		lightProgram = ShaderHelper.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle, new String[] {
				POSITION_ATTRIBUTE});
		
		
		
	}
	
	
	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height) {
		// Set the OpenGL viewport to the same size as the surface.
		GLES20.glViewport(0, 0, width, height);

		// Create a new perspective projection matrix. The height will stay the
		// same while the width will vary as per aspect ratio.
		final float ratio = (float) width / height;
		final float left = -ratio;
		final float right = ratio;
		final float bottom = -1.0f;
		final float top = 1.0f;
		final float near = 1.0f;
		final float far = 1000.0f;

		Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near, far);
	}
	
	
	
	@Override
	public void onDrawFrame(GL10 glUnused) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		
		long time = SystemClock.uptimeMillis() % 10000L;        
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);  
		// Set our per-vertex lighting program.
		GLES20.glUseProgram(letterProgram);

		// Set program handles for cube drawing.
		mvpMatrixUniform = GLES20.glGetUniformLocation(letterProgram, MVP_MATRIX_UNIFORM);
		mvMatrixUniform = GLES20.glGetUniformLocation(letterProgram, MV_MATRIX_UNIFORM);
		lightPosUniform = GLES20.glGetUniformLocation(letterProgram, LIGHT_POSITION_UNIFORM);
		positionAttribute = GLES20.glGetAttribLocation(letterProgram, POSITION_ATTRIBUTE);
		normalAttribute = GLES20.glGetAttribLocation(letterProgram, NORMAL_ATTRIBUTE);
		colorUniform = GLES20.glGetUniformLocation(letterProgram, COLOR_UNIFORM);
		
		// Calculate position of the light. Rotate and then push into the distance.
        Matrix.setIdentityM(lightModelMatrix, 0);
        Matrix.translateM(lightModelMatrix, 0, 0.0f, 0.0f, -11.0f);      
        Matrix.rotateM(lightModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
        Matrix.translateM(lightModelMatrix, 0, 0.0f, 0.0f,5.0f);
               
        Matrix.multiplyMV(lightPosInWorldSpace, 0, lightModelMatrix, 0, lightPosInModelSpace, 0);
        Matrix.multiplyMV(lightPosInEyeSpace, 0, viewMatrix, 0, lightPosInWorldSpace, 0);          
        GLES20.glUniform3f(lightPosUniform, lightPosInEyeSpace[0], lightPosInEyeSpace[1], lightPosInEyeSpace[2]);
        
        //J
        Matrix.setIdentityM(modelMatrix, 0);
		Matrix.translateM(modelMatrix, 0, -12.0f, -5.0f, -12f);
		 Matrix.rotateM(modelMatrix, 0, -angleInDegrees, 0.0f, 1.0f, 0.0f);
		Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0);
		GLES20.glUniformMatrix4fv(mvMatrixUniform, 1, false, mvpMatrix, 0);
		Matrix.multiplyMM(temporaryMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);
		System.arraycopy(temporaryMatrix, 0, mvpMatrix, 0, 16);

		// Pass in the combined matrix.
		GLES20.glUniformMatrix4fv(mvpMatrixUniform, 1, false, mvpMatrix, 0);
		GLES20.glUniform4f(colorUniform, 0.0f, 0.0f, 1.0f,1.0f);
		jMap.render();
		
		//D
        Matrix.setIdentityM(modelMatrix, 0);
		Matrix.translateM(modelMatrix, 0, 0.0f, -5.0f, -12f);
		 Matrix.rotateM(modelMatrix, 0, -angleInDegrees, 0.0f, 1.0f, 0.0f);
		Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0);
		GLES20.glUniformMatrix4fv(mvMatrixUniform, 1, false, mvpMatrix, 0);
		Matrix.multiplyMM(temporaryMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);
		System.arraycopy(temporaryMatrix, 0, mvpMatrix, 0, 16);

		// Pass in the combined matrix.
		GLES20.glUniformMatrix4fv(mvpMatrixUniform, 1, false, mvpMatrix, 0);
		GLES20.glUniform4f(colorUniform, 0.0f, 0.0f, 1.0f,1.0f);
		dMap.render();
		
		//X
        Matrix.setIdentityM(modelMatrix, 0);
		Matrix.translateM(modelMatrix, 0, 12.0f, -5.0f, -12f);
		 Matrix.rotateM(modelMatrix, 0, -angleInDegrees, 0.0f, 1.0f, 0.0f);
		Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0);
		GLES20.glUniformMatrix4fv(mvMatrixUniform, 1, false, mvpMatrix, 0);
		Matrix.multiplyMM(temporaryMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);
		System.arraycopy(temporaryMatrix, 0, mvpMatrix, 0, 16);

		// Pass in the combined matrix.
		GLES20.glUniformMatrix4fv(mvpMatrixUniform, 1, false, mvpMatrix, 0);
		GLES20.glUniform4f(colorUniform, 0.0f, 0.0f, 1.0f,1.0f);
		xMap.render();
		
		// Draw a point to indicate the light.
        GLES20.glUseProgram(lightProgram);        
        drawLight();
	}
	
	private void drawLight()
	{
		final int pointMVPMatrixHandle = GLES20.glGetUniformLocation(lightProgram, "u_MVPMatrix");
        final int pointPositionHandle = GLES20.glGetAttribLocation(lightProgram, "a_Position");
        
		// Pass in the position.
		GLES20.glVertexAttrib3f(pointPositionHandle, lightPosInModelSpace[0], lightPosInModelSpace[1], lightPosInModelSpace[2]);

		// Since we are not using a buffer object, disable vertex arrays for this attribute.
        GLES20.glDisableVertexAttribArray(pointPositionHandle);  
		
		// Pass in the transformation matrix.
		Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, lightModelMatrix, 0);
		Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);
		GLES20.glUniformMatrix4fv(pointMVPMatrixHandle, 1, false, mvpMatrix, 0);
		
		// Draw the point.
		GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);
	}
	
class LetterMap {
		

		final int[] vbo = new int[1];
		final int[] ibo = new int[2];

		int iBOCount;
		int vBOCount;

		LetterMap(final int vboID, final int iboID) {
			try {				
				BOResourceReader boResourceReader = new BOResourceReader(jDXTrial2Activity, vboID, iboID);					
				vBOCount = boResourceReader.getmVBOCount();
				iBOCount = boResourceReader.getmIBOCount();
				float[] vBOData = boResourceReader.getVBOData();
				short[] iBOData = boResourceReader.getIBOData();
				
				
				
				//Create and add data to client-side buffers
				final FloatBuffer heightMapVertexDataBuffer = ByteBuffer
						.allocateDirect(vBOData.length * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder())
						.asFloatBuffer();
				heightMapVertexDataBuffer.put(vBOData).position(0);

				final ShortBuffer heightMapIndexDataBuffer = ByteBuffer
						.allocateDirect(iBOData.length * BYTES_PER_SHORT).order(ByteOrder.nativeOrder())
						.asShortBuffer();
				heightMapIndexDataBuffer.put(iBOData).position(0);
				
				
				//Generate and add data from client-side buffers to GPU
				GLES20.glGenBuffers(1, vbo, 0);
				GLES20.glGenBuffers(1, ibo, 0);

				if (vbo[0] > 0 && ibo[0] > 0) {
					GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);
					GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, heightMapVertexDataBuffer.capacity() * BYTES_PER_FLOAT,
							heightMapVertexDataBuffer, GLES20.GL_STATIC_DRAW);

					GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
					GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, heightMapIndexDataBuffer.capacity()
							* BYTES_PER_SHORT, heightMapIndexDataBuffer, GLES20.GL_STATIC_DRAW);
					
					
					GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
					GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
				} else {
					errorHandler.handleError(ErrorType.BUFFER_CREATION_ERROR, "glGenBuffers");
				}
			} catch (Throwable t) {
				Log.w("JDX Trial 2 Renderer ", t);
				errorHandler.handleError(ErrorType.BUFFER_CREATION_ERROR, t.getLocalizedMessage());
			}
		}

		void render() {
			if (vbo[0] > 0 && ibo[0] > 0) {				
				GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);

				// Bind Attributes
				glEs20.glVertexAttribPointer(positionAttribute, POSITION_DATA_SIZE_IN_ELEMENTS, GLES20.GL_FLOAT, false,
						STRIDE, 0);
				GLES20.glEnableVertexAttribArray(positionAttribute);
				
				glEs20.glVertexAttribPointer(normalAttribute, NORMAL_DATA_SIZE_IN_ELEMENTS, GLES20.GL_FLOAT, false,
						STRIDE, (POSITION_DATA_SIZE_IN_ELEMENTS) * BYTES_PER_FLOAT);
				GLES20.glEnableVertexAttribArray(normalAttribute);
				
				/*glEs20.glVertexAttribPointer(colorAttribute, COLOR_DATA_SIZE_IN_ELEMENTS, GLES20.GL_FLOAT, false,
						jSTRIDE, POSITION_DATA_SIZE_IN_ELEMENTS  * BYTES_PER_FLOAT);
				GLES20.glEnableVertexAttribArray(colorAttribute);*/				

				// Draw
				GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
				glEs20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, iBOCount, GLES20.GL_UNSIGNED_SHORT, 0);
				GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
				GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
			}
		}

		void release() {
			if (vbo[0] > 0) {
				GLES20.glDeleteBuffers(vbo.length, vbo, 0);
				vbo[0] = 0;
			}

			if (ibo[0] > 0) {
				GLES20.glDeleteBuffers(ibo.length, ibo, 0);
				ibo[0] = 0;
			}
		}
	}
}

