package rosick.mckesson.tut06;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;

import rosick.GLWindow;
import rosick.framework.Framework;
import rosick.framework.IOUtils;
import rosick.glm.Mat4;
import rosick.glm.Vec3;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * II. Positioning
 * 6. Objects in Motion
 * http://www.arcsynthesis.org/gltut/Positioning/Tutorial%2006.html
 * @author integeruser, xire-
 */
public class Hierarchy04 extends GLWindow {
	
	public static void main(String[] args) {		
		new Hierarchy04().start(1024, 768);
	}
	
	
	private static final String BASEPATH = "/rosick/mckesson/tut06/data/";

	

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
	private int theProgram;
	private int modelToCameraMatrixUnif, cameraToClipMatrixUnif;
	private FloatBuffer cameraToClipMatrixBuffer = IOUtils.allocFloats(new float[16]);
	private FloatBuffer modelToCameraMatrixBuffer = IOUtils.allocFloats(new float[16]); 
	private int vertexBufferObject, indexBufferObject;
	private int vao;
	
	private final float vertexData[] = {
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
	
	private final short indexData[] = {
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
	
	private final int numberOfVertices = 24;
	private final float fFrustumScale = calcFrustumScale(45.0f);
	

	
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
		int vertexShader =		Framework.loadShader(GL_VERTEX_SHADER, 		BASEPATH + "posColorLocalTransform.vert");
		int fragmentShader = 	Framework.loadShader(GL_FRAGMENT_SHADER, 	BASEPATH + "colorPassthrough.frag");
        
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(vertexShader);
		shaderList.add(fragmentShader);

		theProgram = Framework.createProgram(shaderList);
		
	    
	    modelToCameraMatrixUnif = glGetUniformLocation(theProgram, "modelToCameraMatrix");
		cameraToClipMatrixUnif = glGetUniformLocation(theProgram, "cameraToClipMatrix");
		
		float fzNear = 1.0f; float fzFar = 100.0f;
		
		cameraToClipMatrixBuffer.put(0, 	fFrustumScale);
		cameraToClipMatrixBuffer.put(5, 	fFrustumScale);
		cameraToClipMatrixBuffer.put(10, 	(fzFar + fzNear) / (fzNear - fzFar));
		cameraToClipMatrixBuffer.put(11, 	-1.0f);
		cameraToClipMatrixBuffer.put(14, 	(2 * fzFar * fzNear) / (fzNear - fzFar));
		
		modelToCameraMatrixBuffer.put(0, 	1.0f);
		modelToCameraMatrixBuffer.put(5, 	1.0f);
		modelToCameraMatrixBuffer.put(10, 	1.0f);
		modelToCameraMatrixBuffer.put(15, 	1.0f);
		
		glUseProgram(theProgram);
		glUniformMatrix4(cameraToClipMatrixUnif, false, cameraToClipMatrixBuffer);
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
	protected void update() {
		lastFrameDuration *= 5;
		
		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			g_armature.adjBase(false);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			g_armature.adjBase(true);
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			g_armature.adjUpperArm(false);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			g_armature.adjUpperArm(true);
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_R)) {
			g_armature.adjLowerArm(false);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_F)) {
			g_armature.adjLowerArm(true);
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_T)) {
			g_armature.adjWristPitch(false);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_G)) {
			g_armature.adjWristPitch(true);
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_Z)) {
			g_armature.adjWristRoll(false);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_C)) {
			g_armature.adjWristRoll(true);
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
			g_armature.adjFingerOpen(true);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
			g_armature.adjFingerOpen(false);
		}
		
		
		if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
			leaveMainLoop();
		}
	}
	
	
	@Override
	protected void display() {
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClearDepth(1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		g_armature.draw();
	}

	
	@Override
	protected void reshape(int width, int height) {
		cameraToClipMatrixBuffer.put(0, fFrustumScale / (width / (float) height));
		cameraToClipMatrixBuffer.put(5, fFrustumScale);

		glUseProgram(theProgram);
		glUniformMatrix4(cameraToClipMatrixUnif, false, cameraToClipMatrixBuffer);
		glUseProgram(0);

		glViewport(0, 0, width, height);
	}

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private class MatrixStack {	
		
		private Mat4 currentMatrix;
		private float matrices[];
		private int firstIndexUsable;
		
		private Mat4 tempMat;
		
		
		
		public MatrixStack() {
			matrices = new float[160];														
			currentMatrix = new Mat4(1);
			
			tempMat = new Mat4();
			
			firstIndexUsable = 0;
		}

		
		
		public void push() {
			if (firstIndexUsable == matrices.length) {
				// Double the size of matrices[]
				float temp[] = new float[matrices.length * 2];
				System.arraycopy(matrices, 0, temp, 0, matrices.length);
				matrices = temp;
			}
			
			// Store the currentMatrix in the buffer
			System.arraycopy(currentMatrix.matrix, 0, matrices, firstIndexUsable, 16);		
			firstIndexUsable += 16;
		}

		
		public void pop() {
			// Pop the last matrix pushed in the buffer and set it as currentMatrix
			firstIndexUsable -= 16;
			System.arraycopy(matrices, firstIndexUsable, currentMatrix.matrix, 0, 16);		
		}

		public Mat4 top() {		
			return currentMatrix;
		}
		
		
		public void clear() {
			currentMatrix.clear(1);

			firstIndexUsable = 0;
		}
		
		
		public void rotateX(float fAngDeg) {
			currentMatrix.mul(Mat4.getRotateX(fAngDeg));
		}

		public void rotateY(float fAngDeg) {
			currentMatrix.mul(Mat4.getRotateY(fAngDeg));
		}

		public void rotateZ(float fAngDeg) {
			currentMatrix.mul(Mat4.getRotateZ(fAngDeg));
		}

		
		public void scale(float x, float y, float z) {
			tempMat.clear(0);
			tempMat.put(0, x);
			tempMat.put(5, y);
			tempMat.put(10, z);
			tempMat.put(15, 1);

			currentMatrix.mul(tempMat);
		}

		
		public void translate(float x, float y, float z) {
			tempMat.clear(1);
			tempMat.put(12, x);
			tempMat.put(13, y);
			tempMat.put(14, z);

			currentMatrix.mul(tempMat);
		}

		public void translate(Vec3 offset) {
			translate(offset.x, offset.y, offset.z);
		}
	}	
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private final float STANDARD_ANGLE_INCREMENT = 11.25f;
	private final float SMALL_ANGLE_INCREMENT = 9.0f;
	
	private Hierarchy g_armature = new Hierarchy();
	
	
	private class Hierarchy {
		
		private Vec3 posBase = new Vec3(3.0f, -5.0f, -40.0f);
		private float angBase = -45.0f;

		private Vec3 posBaseLeft = new Vec3(2.0f, 0.0f, 0.0f);
		private Vec3 posBaseRight = new Vec3(-2.0f, 0.0f, 0.0f);
		private float scaleBaseZ = 3.0f;

		private float angUpperArm = -70.75f;
		private float sizeUpperArm = 9;

		private Vec3 posLowerArm = new Vec3(0.0f, 0.0f, 8.0f);
		private float angLowerArm = 60.25f;
		private float lenLowerArm = 5.0f;
		private float widthLowerArm = 1.5f;

		private Vec3 posWrist = new Vec3(0.0f, 0.0f, 5.0f);
		private float angWristRoll = 0;
		private float angWristPitch = 67.5f;
		private float lenWrist = 2;
		private float widthWrist = 2;

		private Vec3 posLeftFinger = new Vec3(1.0f, 0.0f, 1.0f);
		private Vec3 posRightFinger = new Vec3(-1.0f, 0.0f, 1.0f);
		private float angFingerOpen = 45;
		private float lenFinger = 2;
		private float widthFinger = 0.5f;
		private float angLowerFinger = 45;
		
		private MatrixStack modelToCameraStack = new MatrixStack();
		private FloatBuffer tempSharedUniformMatrixBuffer = BufferUtils.createFloatBuffer(16);
		
		
		public void draw() {
			modelToCameraStack.clear();

			glUseProgram(theProgram);
			glBindVertexArray(vao);

			modelToCameraStack.translate(posBase);
			modelToCameraStack.rotateY(angBase);

			// Draw left base.
			{
				modelToCameraStack.push();
				
				modelToCameraStack.translate(posBaseLeft);
				modelToCameraStack.scale(1.0f, 1.0f, scaleBaseZ);
				
				glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.top().fillBuffer(tempSharedUniformMatrixBuffer));
				glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
				
				modelToCameraStack.pop();
			}

			// Draw right base.
			{
				modelToCameraStack.push();
				
				modelToCameraStack.translate(posBaseRight);
				modelToCameraStack.scale(1.0f, 1.0f, scaleBaseZ);
				
				glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.top().fillBuffer(tempSharedUniformMatrixBuffer));
				glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
				
				modelToCameraStack.pop();
			}

			// Draw main arm.
			drawUpperArm(modelToCameraStack);

			glBindVertexArray(0);
			glUseProgram(0);
		}
		
		
		public void drawUpperArm(MatrixStack modelToCameraStack) {
			modelToCameraStack.push();
			modelToCameraStack.rotateX(angUpperArm);

			{
				modelToCameraStack.push();
				
				modelToCameraStack.translate(0.0f, 0.0f, (sizeUpperArm / 2.0f) - 1.0f);
				modelToCameraStack.scale(1.0f, 1.0f, sizeUpperArm / 2.0f);
				
				glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.top().fillBuffer(tempSharedUniformMatrixBuffer));
				glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
				
				modelToCameraStack.pop();
			}

			drawLowerArm(modelToCameraStack);

			modelToCameraStack.pop();
		}
		
		public void drawLowerArm(MatrixStack modelToCameraStack) {
			modelToCameraStack.push();
			
			modelToCameraStack.translate(posLowerArm);
			modelToCameraStack.rotateX(angLowerArm);

			{
				modelToCameraStack.push();
				
				modelToCameraStack.translate(0.0f, 0.0f, lenLowerArm / 2.0f);
				modelToCameraStack.scale(widthLowerArm / 2.0f, widthLowerArm / 2.0f, lenLowerArm / 2.0f);
				
				glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.top().fillBuffer(tempSharedUniformMatrixBuffer));
				glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
				
				modelToCameraStack.pop();
			}

			drawWrist(modelToCameraStack);

			modelToCameraStack.pop();
		}
		
		public void drawWrist(MatrixStack modelToCameraStack) {
			modelToCameraStack.push();
			
			modelToCameraStack.translate(posWrist);
			modelToCameraStack.rotateZ(angWristRoll);
			modelToCameraStack.rotateX(angWristPitch);

			{
				modelToCameraStack.push();
				
				modelToCameraStack.scale(widthWrist / 2.0f, widthWrist/ 2.0f, lenWrist / 2.0f);
				
				glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.top().fillBuffer(tempSharedUniformMatrixBuffer));
				glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
				
				modelToCameraStack.pop();
			}

			drawFingers(modelToCameraStack);

			modelToCameraStack.pop();
		}
		
		public void drawFingers(MatrixStack modelToCameraStack) {
			// Draw left finger
			modelToCameraStack.push();
			
			modelToCameraStack.translate(posLeftFinger);
			modelToCameraStack.rotateY(angFingerOpen);

			{
				modelToCameraStack.push();
				
				modelToCameraStack.translate(0.0f, 0.0f, lenFinger / 2.0f);
				modelToCameraStack.scale(widthFinger / 2.0f, widthFinger/ 2.0f, lenFinger / 2.0f);
				
				glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.top().fillBuffer(tempSharedUniformMatrixBuffer));
				glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
				
				modelToCameraStack.pop();
			}

			{
				// Draw left lower finger
				modelToCameraStack.push();
				
				modelToCameraStack.translate(0.0f, 0.0f, lenFinger);
				modelToCameraStack.rotateY(-angLowerFinger);

				{
					modelToCameraStack.push();
					
					modelToCameraStack.translate(0.0f, 0.0f, lenFinger / 2.0f);
					modelToCameraStack.scale(widthFinger / 2.0f, widthFinger/ 2.0f, lenFinger / 2.0f);
					
					glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.top().fillBuffer(tempSharedUniformMatrixBuffer));
					glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
					
					modelToCameraStack.pop();
				}

				modelToCameraStack.pop();
			}

			modelToCameraStack.pop();

			// Draw right finger
			{
				modelToCameraStack.push();
				
				modelToCameraStack.translate(posRightFinger);
				modelToCameraStack.rotateY(-angFingerOpen);
	
				{
					modelToCameraStack.push();
					
					modelToCameraStack.translate(0.0f, 0.0f, lenFinger / 2.0f);
					modelToCameraStack.scale(widthFinger / 2.0f, widthFinger/ 2.0f, lenFinger / 2.0f);
					
					glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.top().fillBuffer(tempSharedUniformMatrixBuffer));
					glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
					
					modelToCameraStack.pop();
				}
	
				{
					// Draw right lower finger
					modelToCameraStack.push();
					
					modelToCameraStack.translate(0.0f, 0.0f, lenFinger);
					modelToCameraStack.rotateY(angLowerFinger);
	
					{
						modelToCameraStack.push();
						
						modelToCameraStack.translate(0.0f, 0.0f, lenFinger / 2.0f);
						modelToCameraStack.scale(widthFinger / 2.0f, widthFinger/ 2.0f, lenFinger / 2.0f);
						
						glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.top().fillBuffer(tempSharedUniformMatrixBuffer));
						glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
						
						modelToCameraStack.pop();
					}
	
					modelToCameraStack.pop();
				}
	
				modelToCameraStack.pop();
			}
		}
		
		
		public void adjBase(boolean bIncrement) {
			angBase += bIncrement ? STANDARD_ANGLE_INCREMENT * lastFrameDuration : -STANDARD_ANGLE_INCREMENT * lastFrameDuration;
			angBase = angBase % 360.0f;
		}

		public void adjUpperArm(boolean bIncrement) {
			angUpperArm += bIncrement ? STANDARD_ANGLE_INCREMENT * lastFrameDuration : -STANDARD_ANGLE_INCREMENT * lastFrameDuration;
			angUpperArm = clamp(angUpperArm, -90.0f, 0.0f);
		}

		public void adjLowerArm(boolean bIncrement) {
			angLowerArm += bIncrement ? STANDARD_ANGLE_INCREMENT * lastFrameDuration : -STANDARD_ANGLE_INCREMENT * lastFrameDuration;
			angLowerArm = clamp(angLowerArm, 0.0f, 146.25f);
		}

		public void adjWristPitch(boolean bIncrement) {
			angWristPitch += bIncrement ? STANDARD_ANGLE_INCREMENT * lastFrameDuration : -STANDARD_ANGLE_INCREMENT * lastFrameDuration;
			angWristPitch = clamp(angWristPitch, 0.0f, 90.0f);
		}

		public void adjWristRoll(boolean bIncrement) {
			angWristRoll += bIncrement ? STANDARD_ANGLE_INCREMENT * lastFrameDuration : -STANDARD_ANGLE_INCREMENT * lastFrameDuration;
			angWristRoll = angWristRoll % 360.0f;
		}

		public void adjFingerOpen(boolean bIncrement) {
			angFingerOpen += bIncrement ? SMALL_ANGLE_INCREMENT * lastFrameDuration : -SMALL_ANGLE_INCREMENT * lastFrameDuration;
			angFingerOpen = clamp(angFingerOpen, 9.0f, 90.0f);
		}
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
	
	
	private static float degToRad(float fAngDeg) {
		final float fDegToRad = 3.14159f * 2.0f / 360.0f;
		
		return fAngDeg * fDegToRad;
	}
	
	
	public static Mat4 getRotateX(float angoloDeg) {
		float fAngRad = degToRad(angoloDeg);
		float fCos = (float) Math.cos(fAngRad);
		float fSin = (float) Math.sin(fAngRad);

		Mat4 mat = new Mat4();

		// X column
		mat.put(0, 1); 															// x
		mat.put(1, 0); 															// y
		mat.put(2, 0); 															// z

		// Y column
		mat.put(4, 0); 															// x
		mat.put(5, fCos); 														// y
		mat.put(6, fSin); 														// z

		// Z column
		mat.put(8, 0); 															// x
		mat.put(9, -fSin); 														// y
		mat.put(10, fCos); 														// z

		// Last
		mat.put(15, 1); 

		return mat;
	}

	public static Mat4 getRotateY(float angoloDeg) {
		float fAngRad = degToRad(angoloDeg);
		float fCos = (float) Math.cos(fAngRad);
		float fSin = (float) Math.sin(fAngRad);

		Mat4 mat = new Mat4();

		// X column
		mat.put(0, fCos); 
		mat.put(1, 0); 
		mat.put(2, -fSin);

		// Y column
		mat.put(4, 0); 
		mat.put(5, 1); 
		mat.put(6, 0); 

		// Z column
		mat.put(8, fSin); 
		mat.put(9, 0);
		mat.put(10, fCos); 

		// Last
		mat.put(15, 1); 

		return mat;
	}

	public static Mat4 getRotateZ(float angoloDeg) {
		float fAngRad = degToRad(angoloDeg);
		float fCos = (float) Math.cos(fAngRad);
		float fSin = (float) Math.sin(fAngRad);

		Mat4 mat = new Mat4();

		// X column
		mat.put(0, fCos); 
		mat.put(1, fSin); 
		mat.put(2, 0);

		// Y column
		mat.put(4, -fSin);
		mat.put(5, fCos); 
		mat.put(6, 0); 

		// Z column
		mat.put(8, 0); 
		mat.put(9, 0); 
		mat.put(10, 1); 

		// Last
		mat.put(15, 1); 

		return mat;
	}
}