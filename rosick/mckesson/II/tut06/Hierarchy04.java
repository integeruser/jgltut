package rosick.mckesson.II.tut06;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;

import rosick.LWJGLWindow;
import rosick.jglsdk.framework.Framework;
import rosick.jglsdk.glm.Mat3;
import rosick.jglsdk.glm.Mat4;
import rosick.jglsdk.glm.Vec3;
import rosick.jglsdk.glm.Vec4;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * II. Positioning
 * 6. Objects in Motion
 * http://www.arcsynthesis.org/gltut/Positioning/Tutorial%2006.html
 * @author integeruser
 * 
 *  Node Angle		Increase/Left	Decrease/Right
 *  Base Spin		A				D
 *	Arm Raise		W				S
 *	Elbow Raise		R				F
 *	Wrist Raise		T				G
 *	Wrist Spin		Z				C
 *	Finger     		Q				E
 */
public class Hierarchy04 extends LWJGLWindow {
	
	public static void main(String[] args) {		
		new Hierarchy04().start(800, 800);
	}
	
	
	private final int FLOAT_SIZE = Float.SIZE / 8;
	private final String TUTORIAL_DATAPATH = "/rosick/mckesson/II/tut06/data/";

	

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
			
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
			1.0f, 0.0f, 1.0f, 1.0f
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
			22, 23, 20
	};
	
	private final int numberOfVertices = 24;
	
	private int theProgram;
	private int positionAttrib, colorAttrib;
	private int modelToCameraMatrixUnif, cameraToClipMatrixUnif;
	private int vertexBufferObject, indexBufferObject;
	private int vao;
	
	private Mat4 cameraToClipMatrix = new Mat4();
	
	private FloatBuffer tempFloatBuffer16 = BufferUtils.createFloatBuffer(16);
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	

	private void initializeProgram() {	
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, 	TUTORIAL_DATAPATH + "PosColorLocalTransform.vert"));
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, TUTORIAL_DATAPATH + "ColorPassthrough.frag"));

		theProgram = Framework.createProgram(shaderList);
		
		positionAttrib = glGetAttribLocation(theProgram, "position");
		colorAttrib = glGetAttribLocation(theProgram, "color");
		
	    modelToCameraMatrixUnif = glGetUniformLocation(theProgram, "modelToCameraMatrix");
		cameraToClipMatrixUnif = glGetUniformLocation(theProgram, "cameraToClipMatrix");
		
		float fzNear = 1.0f; float fzFar = 100.0f;
		
		cameraToClipMatrix.set(0, 	fFrustumScale);
		cameraToClipMatrix.set(5, 	fFrustumScale);
		cameraToClipMatrix.set(10, 	(fzFar + fzNear) / (fzNear - fzFar));
		cameraToClipMatrix.set(11, 	-1.0f);
		cameraToClipMatrix.set(14, 	(2 * fzFar * fzNear) / (fzNear - fzFar));
		
		glUseProgram(theProgram);
		glUniformMatrix4(cameraToClipMatrixUnif, false, cameraToClipMatrix.fillAndFlipBuffer(tempFloatBuffer16));
		glUseProgram(0);
	}
	
	private void initializeVAO() {
		FloatBuffer vertexDataBuffer = BufferUtils.createFloatBuffer(vertexData.length);
		vertexDataBuffer.put(vertexData);
		vertexDataBuffer.flip();
		
        vertexBufferObject = glGenBuffers();	       
		glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
	    glBufferData(GL_ARRAY_BUFFER, vertexDataBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		
		ShortBuffer indexDataBuffer = BufferUtils.createShortBuffer(indexData.length);
		indexDataBuffer.put(indexData);
		indexDataBuffer.flip();
		
        indexBufferObject = glGenBuffers();	       
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferObject);
	    glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexDataBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		
		
		vao = glGenVertexArrays();
		glBindVertexArray(vao);

		int colorDataOffset = FLOAT_SIZE * 3 * numberOfVertices;
		glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
		glEnableVertexAttribArray(positionAttrib);
		glEnableVertexAttribArray(colorAttrib);
		glVertexAttribPointer(positionAttrib, 3, GL_FLOAT, false, 0, 0);
		glVertexAttribPointer(colorAttrib, 4, GL_FLOAT, false, 0, colorDataOffset);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferObject);

		glBindVertexArray(0);
	}
	
	
	@Override
	protected void init() {
		initializeProgram();
		initializeVAO(); 

	    glEnable(GL_CULL_FACE);
	    glCullFace(GL_BACK);
	    glFrontFace(GL_CW);
	    
	    glEnable(GL_DEPTH_TEST);
		glDepthMask(true);
		glDepthFunc(GL_LEQUAL);
		glDepthRange(0.0f, 1.0f);
	}
	
	
	@Override
	protected void update() {	
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
		
		
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				switch (Keyboard.getEventKey()) {
				case Keyboard.KEY_ESCAPE:
					leaveMainLoop();
					break;
				}
			}
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
		cameraToClipMatrix.set(0, fFrustumScale / (width / (float) height));
		cameraToClipMatrix.set(5, fFrustumScale);

		glUseProgram(theProgram);
		glUniformMatrix4(cameraToClipMatrixUnif, false, cameraToClipMatrix.fillAndFlipBuffer(tempFloatBuffer16));
		glUseProgram(0);

		glViewport(0, 0, width, height);
	}

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private class MatrixStack {	
		
		private Mat4 currentMatrix;
		private float matrices[];
		private int firstIndexUsable;
				
		
		MatrixStack() {
			matrices = new float[160];														
			currentMatrix = new Mat4(1);
						
			firstIndexUsable = 0;
		}

		
		void push() {
			if (firstIndexUsable == matrices.length) {
				// Double the size of matrices[]
				float temp[] = new float[matrices.length * 2];
				System.arraycopy(matrices, 0, temp, 0, matrices.length);
				matrices = temp;
			}
			
			// Store the currentMatrix in the buffer
			System.arraycopy(currentMatrix.get(), 0, matrices, firstIndexUsable, 16);		
			firstIndexUsable += 16;
		}

		
		void pop() {
			// Pop the last matrix pushed in the buffer and set it as currentMatrix
			firstIndexUsable -= 16;
			System.arraycopy(matrices, firstIndexUsable, currentMatrix.get(), 0, 16);		
		}

		Mat4 top() {		
			return currentMatrix;
		}
		
		
		void clear() {
			currentMatrix.clear(1);

			firstIndexUsable = 0;
		}
		
		
		void rotateX(float fAngDeg) {
			currentMatrix.mul(new Mat4(Hierarchy04.rotateX(fAngDeg)));
		}

		void rotateY(float fAngDeg) {
			currentMatrix.mul(new Mat4(Hierarchy04.rotateY(fAngDeg)));
		}

		void rotateZ(float fAngDeg) {
			currentMatrix.mul(new Mat4(Hierarchy04.rotateZ(fAngDeg)));
		}

		
		void scale(Vec3 scaleVec) {
			Mat4 scaleMat = new Mat4(1.0f);
			scaleMat.set(0, scaleVec.x);
			scaleMat.set(5, scaleVec.y);
			scaleMat.set(10, scaleVec.z);

			currentMatrix.mul(scaleMat);
		}

		
		void translate(Vec3 offsetVec) {
			Mat4 translateMat = new Mat4(1.0f);
			translateMat.setColumn(3, new Vec4(offsetVec, 1.0f));

			currentMatrix.mul(translateMat);
		}
	}	
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
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
		
		
		void draw() {
			modelToCameraStack.clear();

			glUseProgram(theProgram);
			glBindVertexArray(vao);

			modelToCameraStack.translate(posBase);
			modelToCameraStack.rotateY(angBase);

			// Draw left base.
			{
				modelToCameraStack.push();
				
				modelToCameraStack.translate(posBaseLeft);
				modelToCameraStack.scale(new Vec3(1.0f, 1.0f, scaleBaseZ));
				
				glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.top().fillAndFlipBuffer(tempFloatBuffer16));
				glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
				
				modelToCameraStack.pop();
			}

			// Draw right base.
			{
				modelToCameraStack.push();
				
				modelToCameraStack.translate(posBaseRight);
				modelToCameraStack.scale(new Vec3(1.0f, 1.0f, scaleBaseZ));
				
				glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.top().fillAndFlipBuffer(tempFloatBuffer16));
				glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
				
				modelToCameraStack.pop();
			}

			// Draw main arm.
			drawUpperArm(modelToCameraStack);

			glBindVertexArray(0);
			glUseProgram(0);
		}
		
		
		private void drawUpperArm(MatrixStack modelToCameraStack) {
			modelToCameraStack.push();
			modelToCameraStack.rotateX(angUpperArm);

			{
				modelToCameraStack.push();
				
				modelToCameraStack.translate(new Vec3(0.0f, 0.0f, (sizeUpperArm / 2.0f) - 1.0f));
				modelToCameraStack.scale(new Vec3(1.0f, 1.0f, sizeUpperArm / 2.0f));
				
				glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.top().fillAndFlipBuffer(tempFloatBuffer16));
				glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
				
				modelToCameraStack.pop();
			}

			drawLowerArm(modelToCameraStack);

			modelToCameraStack.pop();
		}
		
		private void drawLowerArm(MatrixStack modelToCameraStack) {
			modelToCameraStack.push();
			
			modelToCameraStack.translate(posLowerArm);
			modelToCameraStack.rotateX(angLowerArm);

			{
				modelToCameraStack.push();
				
				modelToCameraStack.translate(new Vec3(0.0f, 0.0f, lenLowerArm / 2.0f));
				modelToCameraStack.scale(new Vec3(widthLowerArm / 2.0f, widthLowerArm / 2.0f, lenLowerArm / 2.0f));
				
				glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.top().fillAndFlipBuffer(tempFloatBuffer16));
				glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
				
				modelToCameraStack.pop();
			}

			drawWrist(modelToCameraStack);

			modelToCameraStack.pop();
		}
		
		private void drawWrist(MatrixStack modelToCameraStack) {
			modelToCameraStack.push();
			
			modelToCameraStack.translate(posWrist);
			modelToCameraStack.rotateZ(angWristRoll);
			modelToCameraStack.rotateX(angWristPitch);

			{
				modelToCameraStack.push();
				
				modelToCameraStack.scale(new Vec3(widthWrist / 2.0f, widthWrist/ 2.0f, lenWrist / 2.0f));
				
				glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.top().fillAndFlipBuffer(tempFloatBuffer16));
				glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
				
				modelToCameraStack.pop();
			}

			drawFingers(modelToCameraStack);

			modelToCameraStack.pop();
		}
		
		private void drawFingers(MatrixStack modelToCameraStack) {
			// Draw left finger
			modelToCameraStack.push();
			
			modelToCameraStack.translate(posLeftFinger);
			modelToCameraStack.rotateY(angFingerOpen);

			{
				modelToCameraStack.push();
				
				modelToCameraStack.translate(new Vec3(0.0f, 0.0f, lenFinger / 2.0f));
				modelToCameraStack.scale(new Vec3(widthFinger / 2.0f, widthFinger/ 2.0f, lenFinger / 2.0f));
				
				glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.top().fillAndFlipBuffer(tempFloatBuffer16));
				glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
				
				modelToCameraStack.pop();
			}

			{
				// Draw left lower finger
				modelToCameraStack.push();
				
				modelToCameraStack.translate(new Vec3(0.0f, 0.0f, lenFinger));
				modelToCameraStack.rotateY(-angLowerFinger);

				{
					modelToCameraStack.push();
					
					modelToCameraStack.translate(new Vec3(0.0f, 0.0f, lenFinger / 2.0f));
					modelToCameraStack.scale(new Vec3(widthFinger / 2.0f, widthFinger/ 2.0f, lenFinger / 2.0f));
					
					glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.top().fillAndFlipBuffer(tempFloatBuffer16));
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
					
					modelToCameraStack.translate(new Vec3(0.0f, 0.0f, lenFinger / 2.0f));
					modelToCameraStack.scale(new Vec3(widthFinger / 2.0f, widthFinger/ 2.0f, lenFinger / 2.0f));
					
					glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.top().fillAndFlipBuffer(tempFloatBuffer16));
					glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
					
					modelToCameraStack.pop();
				}
	
				{
					// Draw right lower finger
					modelToCameraStack.push();
					
					modelToCameraStack.translate(new Vec3(0.0f, 0.0f, lenFinger));
					modelToCameraStack.rotateY(angLowerFinger);
	
					{
						modelToCameraStack.push();
						
						modelToCameraStack.translate(new Vec3(0.0f, 0.0f, lenFinger / 2.0f));
						modelToCameraStack.scale(new Vec3(widthFinger / 2.0f, widthFinger/ 2.0f, lenFinger / 2.0f));
						
						glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.top().fillAndFlipBuffer(tempFloatBuffer16));
						glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
						
						modelToCameraStack.pop();
					}
	
					modelToCameraStack.pop();
				}
	
				modelToCameraStack.pop();
			}
		}
		
		
		void adjBase(boolean bIncrement) {
			float lastFrameDuration = (float) (getLastFrameDuration() * 5 / 1000.0);

			angBase += bIncrement ? STANDARD_ANGLE_INCREMENT * lastFrameDuration : -STANDARD_ANGLE_INCREMENT * lastFrameDuration;
			angBase = angBase % 360.0f;
		}

		void adjUpperArm(boolean bIncrement) {
			float lastFrameDuration = (float) (getLastFrameDuration() * 5 / 1000.0);

			angUpperArm += bIncrement ? STANDARD_ANGLE_INCREMENT * lastFrameDuration : -STANDARD_ANGLE_INCREMENT * lastFrameDuration;
			angUpperArm = clamp(angUpperArm, -90.0f, 0.0f);
		}

		void adjLowerArm(boolean bIncrement) {
			float lastFrameDuration = (float) (getLastFrameDuration() * 5 / 1000.0);

			angLowerArm += bIncrement ? STANDARD_ANGLE_INCREMENT * lastFrameDuration : -STANDARD_ANGLE_INCREMENT * lastFrameDuration;
			angLowerArm = clamp(angLowerArm, 0.0f, 146.25f);
		}

		void adjWristPitch(boolean bIncrement) {
			float lastFrameDuration = (float) (getLastFrameDuration() * 5 / 1000.0);

			angWristPitch += bIncrement ? STANDARD_ANGLE_INCREMENT * lastFrameDuration : -STANDARD_ANGLE_INCREMENT * lastFrameDuration;
			angWristPitch = clamp(angWristPitch, 0.0f, 90.0f);
		}

		void adjWristRoll(boolean bIncrement) {
			float lastFrameDuration = (float) (getLastFrameDuration() * 5 / 1000.0);

			angWristRoll += bIncrement ? STANDARD_ANGLE_INCREMENT * lastFrameDuration : -STANDARD_ANGLE_INCREMENT * lastFrameDuration;
			angWristRoll = angWristRoll % 360.0f;
		}

		void adjFingerOpen(boolean bIncrement) {
			float lastFrameDuration = (float) (getLastFrameDuration() * 5 / 1000.0);

			angFingerOpen += bIncrement ? SMALL_ANGLE_INCREMENT * lastFrameDuration : -SMALL_ANGLE_INCREMENT * lastFrameDuration;
			angFingerOpen = clamp(angFingerOpen, 9.0f, 90.0f);
		}
	}
	
	
	private final float STANDARD_ANGLE_INCREMENT = 11.25f;
	private final float SMALL_ANGLE_INCREMENT = 9.0f;
	
	private Hierarchy g_armature = new Hierarchy();
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private final float fFrustumScale = calcFrustumScale(45.0f);

	
	private float calcFrustumScale(float fFovDeg) {
		final float degToRad = 3.14159f * 2.0f / 360.0f;
		float fFovRad = fFovDeg * degToRad;
		
		return 1.0f / (float) (Math.tan(fFovRad / 2.0f));
	}
	
	
	private float clamp(float fValue, float fMinValue, float fMaxValue) {
		if (fValue < fMinValue) {
			return fMinValue;
		}

		if (fValue > fMaxValue) {
			return fMaxValue;
		}

		return fValue;
	}

	
	private static float degToRad(float fAngDeg) {
		final float fDegToRad = 3.14159f * 2.0f / 360.0f;
		
		return fAngDeg * fDegToRad;
	}
	
	
	private static Mat3 rotateX(float fAngDeg) {
		float fAngRad = degToRad(fAngDeg);
		float fCos = (float) Math.cos(fAngRad);
		float fSin = (float) Math.sin(fAngRad);

		Mat3 theMat = new Mat3(1.0f);
		theMat.set(4, fCos); theMat.set(7, -fSin); 
		theMat.set(5, fSin); theMat.set(8, fCos); 
		
		return theMat;
	}

	private static Mat3 rotateY(float fAngDeg) {
		float fAngRad = degToRad(fAngDeg);
		float fCos = (float) Math.cos(fAngRad);
		float fSin = (float) Math.sin(fAngRad);

		Mat3 theMat = new Mat3(1.0f);
		theMat.set(0, fCos); theMat.set(6, fSin); 
		theMat.set(2, -fSin); theMat.set(8, fCos);
		
		return theMat;
	}

	private static Mat3 rotateZ(float fAngDeg) {
		float fAngRad = degToRad(fAngDeg);
		float fCos = (float) Math.cos(fAngRad);
		float fSin = (float) Math.sin(fAngRad);

		Mat3 theMat = new Mat3(1.0f);
		theMat.set(0, fCos); theMat.set(3, -fSin); 
		theMat.set(1, fSin); theMat.set(4, fCos);
		
		return theMat;
	}
}