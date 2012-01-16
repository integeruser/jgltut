package rosick.mckesson.tut06;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

import org.lwjgl.input.Keyboard;

import rosick.common.GLWindow;
import rosick.common.IOUtils;
import rosick.common.ShaderUtils;
import rosick.common.math.Vector3;
import rosick.common.math.Mat4;

/**
 * Jason L. McKesson
 * II. Positioning
 * 6. Objects in Motion
 * http://www.arcsynthesis.org/gltut/Positioning/Tutorial%2006.html
 */
public class Hierarchy04 extends GLWindow {
	
	public static void main(String[] args) {		
		new Hierarchy04().start(1024, 768);
	}
	
	

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
	final float vertexData[] = {
		// Front
		+1.0f, +1.0f, +1.0f,
		+1.0f, -1.0f, +1.0f,
		-1.0f, -1.0f, +1.0f,
		-1.0f, +1.0f, +1.0f,

		// Top
		+1.0f, +1.0f, +1.0f,
		-1.0f, +1.0f, +1.0f,
		-1.0f, +1.0f, -1.0f,
		+1.0f, +1.0f, -1.0f,

		// Left
		+1.0f, +1.0f, +1.0f,
		+1.0f, +1.0f, -1.0f,
		+1.0f, -1.0f, -1.0f,
		+1.0f, -1.0f, +1.0f,

		// Back
		+1.0f, +1.0f, -1.0f,
		-1.0f, +1.0f, -1.0f,
		-1.0f, -1.0f, -1.0f,
		+1.0f, -1.0f, -1.0f,

		// Bottom
		+1.0f, -1.0f, +1.0f,
		+1.0f, -1.0f, -1.0f,
		-1.0f, -1.0f, -1.0f,
		-1.0f, -1.0f, +1.0f,

		// Right
		-1.0f, +1.0f, +1.0f,
		-1.0f, -1.0f, +1.0f,
		-1.0f, -1.0f, -1.0f,
		-1.0f, +1.0f, -1.0f,


		0.0f, 1.0f, 0.0f, 1.0f,
		0.0f, 1.0f, 0.0f, 1.0f,
		0.0f, 1.0f, 0.0f, 1.0f,
		0.0f, 1.0f, 0.0f, 1.0f,

		0.0f, 0.0f, 1.0f, 1.0f,
		0.0f, 0.0f, 1.0f, 1.0f,
		0.0f, 0.0f, 1.0f, 1.0f,
		0.0f, 0.0f, 1.0f, 1.0f,

		1.0f, 0.0f, 0.0f, 1.0f,
		1.0f, 0.0f, 0.0f, 1.0f,
		1.0f, 0.0f, 0.0f, 1.0f,
		1.0f, 0.0f, 0.0f, 1.0f,

		1.0f, 1.0f, 0.0f, 1.0f,
		1.0f, 1.0f, 0.0f, 1.0f,
		1.0f, 1.0f, 0.0f, 1.0f,
		1.0f, 1.0f, 0.0f, 1.0f,

		0.0f, 1.0f, 1.0f, 1.0f,
		0.0f, 1.0f, 1.0f, 1.0f,
		0.0f, 1.0f, 1.0f, 1.0f,
		0.0f, 1.0f, 1.0f, 1.0f,

		1.0f, 0.0f, 1.0f, 1.0f,
		1.0f, 0.0f, 1.0f, 1.0f,
		1.0f, 0.0f, 1.0f, 1.0f,
		1.0f, 0.0f, 1.0f, 1.0f,
	};
	
	final short indexData[] = {
		0, 1, 2,
		2, 3, 0,

		4, 5, 6,
		6, 7, 4,

		8, 9, 10,
		10, 11, 8,

		12, 13, 14,
		14, 15, 12,

		16, 17, 18,
		18, 19, 16,

		20, 21, 22,
		22, 23, 20,
	};
	
	final int numberOfVertices = 24;
	final float fFrustumScale = calcFrustumScale(45.0f);
	
	int theProgram;
	int offsetUniform, modelToCameraMatrixUnif, cameraToClipMatrixUnif;
	FloatBuffer cameraToClipMatrix = IOUtils.allocFloats(new float[16]);
    FloatBuffer modelToCameraMatrix = IOUtils.allocFloats(new float[16]); 
	int vertexBufferObject, indexBufferObject;
	int vao;
	
	Hierarchy armature = new Hierarchy();
	long nowTime, oldTime;
	

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	
	
	@Override
	protected void init() {
		initializeProgram();
		initializeVertexBuffer(); 

		vao = glGenVertexArrays();
		glBindVertexArray(vao);

		int colorDataOffset = 4 * 3 * numberOfVertices;
		glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, colorDataOffset);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferObject);

		glBindVertexArray(0);
		
	    glEnable(GL_CULL_FACE);
	    glCullFace(GL_BACK);
	    glFrontFace(GL_CW);
	    
	    glEnable(GL_DEPTH_TEST);
		glDepthMask(true);
		glDepthFunc(GL_LEQUAL);
		glDepthRange(0.0f, 1.0f);
	}
	
	private void initializeProgram() {	
		int vertexShader =		ShaderUtils.loadShaderFromFile(GL_VERTEX_SHADER, 	"/rosick/mckesson/shaders/tut06/posColorLocalTransform.vert");
		int fragmentShader = 	ShaderUtils.loadShaderFromFile(GL_FRAGMENT_SHADER, 	"/rosick/mckesson/shaders/tut06/colorPassthrough.frag");
        
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(vertexShader);
		shaderList.add(fragmentShader);

		theProgram = ShaderUtils.createProgram(shaderList);
		
	    
	    modelToCameraMatrixUnif = glGetUniformLocation(theProgram, "modelToCameraMatrix");
		cameraToClipMatrixUnif = glGetUniformLocation(theProgram, "cameraToClipMatrix");
		
		float fzNear = 1.0f; float fzFar = 100.0f;
		cameraToClipMatrix.put(0, fFrustumScale);
		cameraToClipMatrix.put(5, fFrustumScale);
		cameraToClipMatrix.put(10, (fzFar + fzNear) / (fzNear - fzFar));
		cameraToClipMatrix.put(11, -1.0f);
		cameraToClipMatrix.put(14, (2 * fzFar * fzNear) / (fzNear - fzFar));
		
		modelToCameraMatrix.put(0, 1);
		modelToCameraMatrix.put(5, 1);
		modelToCameraMatrix.put(10, 1);
		modelToCameraMatrix.put(15, 1);
		
		glUseProgram(theProgram);
		glUniformMatrix4(cameraToClipMatrixUnif, false, cameraToClipMatrix);
		glUseProgram(0);
	}
	
	private void initializeVertexBuffer() {
		FloatBuffer vertexDataBuffer = IOUtils.allocFloats(vertexData);
        
        vertexBufferObject = glGenBuffers();	       
		glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
	    glBufferData(GL_ARRAY_BUFFER, vertexDataBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		
		
		ShortBuffer indexDataBuffer = IOUtils.allocShorts(indexData);
        
        indexBufferObject = glGenBuffers();	       
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferObject);
	    glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexDataBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}
	
	
	@Override
	protected void update(float fElapsedTime) {
		nowTime = System.currentTimeMillis();
		float fLastFrameDuration = (nowTime - oldTime) / 1000f;
		oldTime = nowTime;
		
		fLastFrameDuration *= 5;
		
		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			armature.adjBase((float) (11.25f * fLastFrameDuration));
		} else if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			armature.adjBase((float) (-11.25f * fLastFrameDuration));
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			armature.adjUpperArm((float) (-11.25f * fLastFrameDuration));
		} else if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			armature.adjUpperArm((float) (11.25f * fLastFrameDuration));
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_R)) {
			armature.adjLowerArm((float) (-11.25f * fLastFrameDuration));
		} else if (Keyboard.isKeyDown(Keyboard.KEY_F)) {
			armature.adjLowerArm((float) (11.25f * fLastFrameDuration));
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_T)) {
			armature.adjWristPitch((float) (-11.25f * fLastFrameDuration));
		} else if (Keyboard.isKeyDown(Keyboard.KEY_G)) {
			armature.adjWristPitch((float) (11.25f * fLastFrameDuration));
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_Z)) {
			armature.adjWristRoll((float) (11.25f * fLastFrameDuration));
		} else if (Keyboard.isKeyDown(Keyboard.KEY_C)) {
			armature.adjWristRoll((float) (-11.25f * fLastFrameDuration));
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
			armature.adjFingerOpen((float) (9f * fLastFrameDuration));
		} else if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
			armature.adjFingerOpen((float) (-9f * fLastFrameDuration));
		}
	}
	
	
	@Override
	protected void render(float fElapsedTime) {
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClearDepth(1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		armature.draw();
	}

	
	@Override
	protected void reshape(int width, int height) {
		cameraToClipMatrix.put(0, fFrustumScale / (width / (float) height));
		cameraToClipMatrix.put(5, fFrustumScale);

		glUseProgram(theProgram);
		glUniformMatrix4(cameraToClipMatrixUnif, false, cameraToClipMatrix);
		glUseProgram(0);

		glViewport(0, 0, width, height);
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private float calcFrustumScale(float fFovDeg) {
		final float degToRad = 3.14159f * 2.0f / 360.0f;
		float fFovRad = fFovDeg * degToRad;
		
		return 1.0f / (float) (Math.tan(fFovRad / 2.0f));
	}
	
	
	private float clamp(float fValue, float fMinValue, float fMaxValue) {
		if (fValue < fMinValue)
			return fMinValue;

		if (fValue > fMaxValue)
			return fMaxValue;

		return fValue;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public static class MatrixStack {
		private Mat4 m_currMat;
		private Deque<Mat4> m_matrices;
		
		
		public MatrixStack() {
			m_matrices = new ArrayDeque<>();
			m_currMat = new Mat4(1);
		}
		
		
		void push() {
			m_matrices.push(m_currMat);
			m_currMat = new Mat4(m_currMat);
		}
		
		void pop() {
			m_currMat = m_matrices.pop();
		}
		
		Mat4 top() {
			return m_currMat;
		}
		
		
		public void rotateX(float fAngDeg) {
			m_currMat.mul(Mat4.getRotateX(fAngDeg));
		}

		public void rotateY(float fAngDeg) {
			m_currMat.mul(Mat4.getRotateY(fAngDeg));
		}

		public void rotateZ(float fAngDeg) {
			m_currMat.mul(Mat4.getRotateZ(fAngDeg));
		}
		
		
		public void scale(float x, float y, float z) {
			Mat4 m = new Mat4();
			m.put(0, x);
			m.put(5, y);
			m.put(10, z);
			m.put(15, 1);
			
			m_currMat.mul(m);
		}
		
		public void scale(Vector3 scaleVec) {
			Mat4 m = new Mat4();
			m.put(0, scaleVec.x);
			m.put(5, scaleVec.y);
			m.put(10, scaleVec.z);
			m.put(15, 1);
			
			m_currMat.mul(m);
		}
		
		
		public void translate(float x, float y, float z) {
			Mat4 m = new Mat4(1.0f);
			m.put(12, x);
			m.put(13, y);
			m.put(14, z);

			m_currMat.mul(m);
		}
		
		public void translate(Vector3 offsetVec) {
			Mat4 m = new Mat4(1.0f);
			m.put(12, offsetVec.x);
			m.put(13, offsetVec.y);
			m.put(14, offsetVec.z);

			m_currMat.mul(m);
		}
	}
	
	
	public class Hierarchy {
		private Vector3 posBase = new Vector3(3.0f, -5.0f, -40.0f);
		private float angBase = -45.0f;

		private Vector3	posBaseLeft = new Vector3(2.0f, 0.0f, 0.0f);
		private Vector3	posBaseRight = new Vector3(-2.0f, 0.0f, 0.0f);
		private float scaleBaseZ = 3.0f;

		private float angUpperArm = -70.75f;
		private float sizeUpperArm = 9;

		private Vector3	posLowerArm = new Vector3(0.0f, 0.0f, 8.0f);
		private float angLowerArm = 60.25f;
		private float lenLowerArm = 5.0f;
		private float widthLowerArm = 1.5f;

		private Vector3 posWrist = new Vector3(0.0f, 0.0f, 5.0f);
		private float angWristRoll = 0;
		private float angWristPitch = 67.5f;
		private float lenWrist = 2;
		private float widthWrist = 2;

		private Vector3 posLeftFinger = new Vector3(1.0f, 0.0f, 1.0f);
		private Vector3 posRightFinger = new Vector3(-1.0f, 0.0f, 1.0f);
		private float angFingerOpen = 45;
		private float lenFinger = 2;
		private float widthFinger = 0.5f;
		private float angLowerFinger = 45;
		
		
		void draw() {
			MatrixStack modelToCameraStack = new MatrixStack();

			glUseProgram(theProgram);
			glBindVertexArray(vao);

			modelToCameraStack.translate(posBase);
			modelToCameraStack.rotateY(angBase);

			// Draw left base.
			{
				modelToCameraStack.push();
				modelToCameraStack.translate(posBaseLeft);
				modelToCameraStack.scale(1.0f, 1.0f, scaleBaseZ);
				glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.top().getBuffer());
				glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
				modelToCameraStack.pop();
			}

			// Draw right base.
			{
				modelToCameraStack.push();
				modelToCameraStack.translate(posBaseRight);
				modelToCameraStack.scale(1.0f, 1.0f, scaleBaseZ);
				glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.top().getBuffer());
				glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
				modelToCameraStack.pop();
			}

			// Draw main arm.
			drawUpperArm(modelToCameraStack);

			glBindVertexArray(0);
			glUseProgram(0);
		}
		
		void drawUpperArm(MatrixStack modelToCameraStack) {
			modelToCameraStack.push();
			modelToCameraStack.rotateX(angUpperArm);

			{
				modelToCameraStack.push();
				modelToCameraStack.translate(0.0f, 0.0f, (sizeUpperArm / 2.0f) - 1.0f);
				modelToCameraStack.scale(1.0f, 1.0f, sizeUpperArm / 2.0f);
				glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.top().getBuffer());
				glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
				modelToCameraStack.pop();
			}

			drawLowerArm(modelToCameraStack);

			modelToCameraStack.pop();
		}
		
		void drawLowerArm(MatrixStack modelToCameraStack) {
			modelToCameraStack.push();
			modelToCameraStack.translate(posLowerArm);
			modelToCameraStack.rotateX(angLowerArm);

			modelToCameraStack.push();
			modelToCameraStack.translate(0.0f, 0.0f, lenLowerArm / 2.0f);
			modelToCameraStack.scale(widthLowerArm / 2.0f, widthLowerArm / 2.0f, lenLowerArm / 2.0f);
			glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.top().getBuffer());
			glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
			modelToCameraStack.pop();

			drawWrist(modelToCameraStack);

			modelToCameraStack.pop();
		}
		
		void drawWrist(MatrixStack modelToCameraStack) {
			modelToCameraStack.push();
			modelToCameraStack.translate(posWrist);
			modelToCameraStack.rotateZ(angWristRoll);
			modelToCameraStack.rotateX(angWristPitch);

			modelToCameraStack.push();
			modelToCameraStack.scale(widthWrist / 2.0f, widthWrist/ 2.0f, lenWrist / 2.0f);
			glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.top().getBuffer());
			glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
			modelToCameraStack.pop();

			drawFingers(modelToCameraStack);

			modelToCameraStack.pop();
		}
		
		void drawFingers(MatrixStack modelToCameraStack) {
			// Draw left finger
			modelToCameraStack.push();
			modelToCameraStack.translate(posLeftFinger);
			modelToCameraStack.rotateY(angFingerOpen);

			modelToCameraStack.push();
			modelToCameraStack.translate(0.0f, 0.0f, lenFinger / 2.0f);
			modelToCameraStack.scale(widthFinger / 2.0f, widthFinger/ 2.0f, lenFinger / 2.0f);
			glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.top().getBuffer());
			glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
			modelToCameraStack.pop();

			{
				// Draw left lower finger
				modelToCameraStack.push();
				modelToCameraStack.translate(0.0f, 0.0f, lenFinger);
				modelToCameraStack.rotateY(-angLowerFinger);

				modelToCameraStack.push();
				modelToCameraStack.translate(0.0f, 0.0f, lenFinger / 2.0f);
				modelToCameraStack.scale(widthFinger / 2.0f, widthFinger/ 2.0f, lenFinger / 2.0f);
				glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.top().getBuffer());
				glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
				modelToCameraStack.pop();

				modelToCameraStack.pop();
			}

			modelToCameraStack.pop();

			// Draw right finger
			modelToCameraStack.push();
			modelToCameraStack.translate(posRightFinger);
			modelToCameraStack.rotateY(-angFingerOpen);

			modelToCameraStack.push();
			modelToCameraStack.translate(0.0f, 0.0f, lenFinger / 2.0f);
			modelToCameraStack.scale(widthFinger / 2.0f, widthFinger/ 2.0f, lenFinger / 2.0f);
			glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.top().getBuffer());
			glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
			modelToCameraStack.pop();

			{
				// Draw right lower finger
				modelToCameraStack.push();
				modelToCameraStack.translate(0.0f, 0.0f, lenFinger);
				modelToCameraStack.rotateY(angLowerFinger);

				modelToCameraStack.push();
				modelToCameraStack.translate(0.0f, 0.0f, lenFinger / 2.0f);
				modelToCameraStack.scale(widthFinger / 2.0f, widthFinger/ 2.0f, lenFinger / 2.0f);
				glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.top().getBuffer());
				glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
				modelToCameraStack.pop();

				modelToCameraStack.pop();
			}

			modelToCameraStack.pop();
		}
		
		void adjBase(float fIncrement) {
			angBase += fIncrement;
			angBase = angBase % 360.0f;
		}

		void adjUpperArm(float bIncrement) {
			angUpperArm += bIncrement;
			angUpperArm = clamp(angUpperArm, -90.0f, 0.0f);
		}

		void adjLowerArm(float bIncrement) {
			angLowerArm += bIncrement;
			angLowerArm = clamp(angLowerArm, 0.0f, 146.25f);
		}

		void adjWristPitch(float bIncrement) {
			angWristPitch += bIncrement;
			angWristPitch = clamp(angWristPitch, 0.0f, 90.0f);
		}

		void adjWristRoll(float bIncrement) {
			angWristRoll += bIncrement;
			angWristRoll = angWristRoll % 360.0f;
		}

		void adjFingerOpen(float bIncrement) {
			angFingerOpen += bIncrement;
			angFingerOpen = clamp(angFingerOpen, 9.0f, 90.0f);
		}
	}
}