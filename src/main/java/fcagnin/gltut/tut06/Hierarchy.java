package fcagnin.gltut.tut06;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Stack;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;

import fcagnin.gltut.LWJGLWindow;
import fcagnin.jglsdk.glm.Mat3;
import fcagnin.jglsdk.glm.Mat4;
import fcagnin.jglsdk.glm.Vec3;
import fcagnin.jglsdk.glm.Vec4;
import fcagnin.gltut.framework.Framework;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * II. Positioning
 * 6. Objects in Motion
 * http://www.arcsynthesis.org/gltut/Positioning/Tutorial%2006.html
 * @author integeruser
 * 
 *  Node Angle		Increase/Left	Decrease/Right
 *  Base Spin			   D			   A
 *	Arm Raise		       S			   W
 *	Elbow Raise		       F			   R
 *	Wrist Raise		       G			   T
 *	Wrist Spin		       C		       Z
 *	Finger     		       Q		       E
 *
 *  SPACE	- print current armature position.
 */
public class Hierarchy extends LWJGLWindow {
	
	public static void main(String[] args) {
		Framework.CURRENT_TUTORIAL_DATAPATH = "/fcagnin/gltut/tut06/data/";

		new Hierarchy().start(700, 700);
	}
	
	

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */			
	
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
			armature.adjBase(false);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			armature.adjBase(true);
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			armature.adjUpperArm(false);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			armature.adjUpperArm(true);
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_R)) {
			armature.adjLowerArm(false);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_F)) {
			armature.adjLowerArm(true);
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_T)) {
			armature.adjWristPitch(false);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_G)) {
			armature.adjWristPitch(true);
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_Z)) {
			armature.adjWristRoll(false);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_C)) {
			armature.adjWristRoll(true);
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
			armature.adjFingerOpen(true);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
			armature.adjFingerOpen(false);
		}
		
		
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				switch (Keyboard.getEventKey()) {
				case Keyboard.KEY_SPACE:
					armature.writePose();
					break;
				
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
		
		armature.draw();
	}

	
	@Override
	protected void reshape(int width, int height) {
		cameraToClipMatrix.set(0, 0, frustumScale / (width / (float) height));
		cameraToClipMatrix.set(1, 1, frustumScale);

		glUseProgram(theProgram);
		glUniformMatrix4(cameraToClipMatrixUnif, false, cameraToClipMatrix.fillAndFlipBuffer(mat4Buffer));
		glUseProgram(0);

		glViewport(0, 0, width, height);
	}

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private int theProgram;
	private int positionAttrib, colorAttrib;
	private int modelToCameraMatrixUnif, cameraToClipMatrixUnif;
	
	private Mat4 cameraToClipMatrix = new Mat4(0.0f);
	
	private FloatBuffer mat4Buffer = BufferUtils.createFloatBuffer(Mat4.SIZE);
	
	
	private void initializeProgram() {	
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, 	"PosColorLocalTransform.vert"));
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, "ColorPassthrough.frag"));

		theProgram = Framework.createProgram(shaderList);
		
		positionAttrib = glGetAttribLocation(theProgram, "position");
		colorAttrib = glGetAttribLocation(theProgram, "color");
		
	    modelToCameraMatrixUnif = glGetUniformLocation(theProgram, "modelToCameraMatrix");
		cameraToClipMatrixUnif = glGetUniformLocation(theProgram, "cameraToClipMatrix");
		
		float zNear = 1.0f; float zFar = 100.0f;

		cameraToClipMatrix.set(0, 0, 	frustumScale);
		cameraToClipMatrix.set(1, 1, 	frustumScale);
		cameraToClipMatrix.set(2, 2,	(zFar + zNear) / (zNear - zFar));
		cameraToClipMatrix.set(2, 3,	-1.0f);
		cameraToClipMatrix.set(3, 2,	(2 * zFar * zNear) / (zNear - zFar));
		
		glUseProgram(theProgram);
		glUniformMatrix4(cameraToClipMatrixUnif, false, cameraToClipMatrix.fillAndFlipBuffer(mat4Buffer));
		glUseProgram(0);
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private final int numberOfVertices = 24;

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
			1.0f, 0.0f, 1.0f, 1.0f};
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
			22, 23, 20};
	
	private int vertexBufferObject, indexBufferObject;
	private int vao;

	
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
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private class MatrixStack {
		private Stack<Mat4> matrixStack;
		private Mat4 currentMatrix;
		
		
		MatrixStack() {
			matrixStack = new Stack<>();
			currentMatrix = new Mat4(1.0f);
		}
		
				
		Mat4 top() {
			return currentMatrix;
		}
		
		void push() {
			matrixStack.push(new Mat4(currentMatrix));
		}
		
		void pop() {
			currentMatrix = matrixStack.pop();
		}
		
		
		void rotateX(float fAngDeg) {
			currentMatrix.mul(new Mat4( Hierarchy.rotateX( fAngDeg )));
		}

		void rotateY(float fAngDeg) {
			currentMatrix.mul(new Mat4( Hierarchy.rotateY( fAngDeg )));
		}

		void rotateZ(float fAngDeg) {
			currentMatrix.mul(new Mat4( Hierarchy.rotateZ( fAngDeg )));
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

	private final float STANDARD_ANGLE_INCREMENT 	= 11.25f;
	private final float SMALL_ANGLE_INCREMENT 		= 9.0f;
	
	private Hierarchy_ armature = new Hierarchy_();
	
	
	private class Hierarchy_ {
		private Vec3 posBase = new Vec3(3.0f, -5.0f, -40.0f);
		private float angBase = -45.0f;

		private Vec3 posBaseLeft = new Vec3(2.0f, 0.0f, 0.0f);
		private Vec3 posBaseRight = new Vec3(-2.0f, 0.0f, 0.0f);
		private float scaleBaseZ = 3.0f;

		private float angUpperArm = -70.75f;
		private float sizeUpperArm = 9.0f;

		private Vec3 posLowerArm = new Vec3(0.0f, 0.0f, 8.0f);
		private float angLowerArm = 60.25f;
		private float lenLowerArm = 5.0f;
		private float widthLowerArm = 1.5f;

		private Vec3 posWrist = new Vec3(0.0f, 0.0f, 5.0f);
		private float angWristRoll = 0.0f;
		private float angWristPitch = 67.5f;
		private float lenWrist = 2.0f;
		private float widthWrist = 2.0f;

		private Vec3 posLeftFinger = new Vec3(1.0f, 0.0f, 1.0f);
		private Vec3 posRightFinger = new Vec3(-1.0f, 0.0f, 1.0f);
		private float angFingerOpen = 45.0f;
		private float lenFinger = 2.0f;
		private float widthFinger = 0.5f;
		private float angLowerFinger = 45.0f;
				
		
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
				modelToCameraStack.scale(new Vec3(1.0f, 1.0f, scaleBaseZ));
				
				glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.top().fillAndFlipBuffer(mat4Buffer));
				glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
				
				modelToCameraStack.pop();
			}

			// Draw right base.
			{
				modelToCameraStack.push();
				
				modelToCameraStack.translate(posBaseRight);
				modelToCameraStack.scale(new Vec3(1.0f, 1.0f, scaleBaseZ));
				
				glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.top().fillAndFlipBuffer(mat4Buffer));
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
				
				glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.top().fillAndFlipBuffer(mat4Buffer));
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
				
				glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.top().fillAndFlipBuffer(mat4Buffer));
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
				
				glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.top().fillAndFlipBuffer(mat4Buffer));
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
				
				glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.top().fillAndFlipBuffer(mat4Buffer));
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
					
					glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.top().fillAndFlipBuffer(mat4Buffer));
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
					
					glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.top().fillAndFlipBuffer(mat4Buffer));
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
						
						glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.top().fillAndFlipBuffer(mat4Buffer));
						glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
						
						modelToCameraStack.pop();
					}
	
					modelToCameraStack.pop();
				}
	
				modelToCameraStack.pop();
			}
		}
		
		
		void adjBase(boolean increment) {
			float lastFrameDuration = getLastFrameDuration() * 5 / 1000.0f;

			angBase += increment ? STANDARD_ANGLE_INCREMENT * lastFrameDuration : -STANDARD_ANGLE_INCREMENT * lastFrameDuration;
			angBase = angBase % 360.0f;
		}

		void adjUpperArm(boolean increment) {
			float lastFrameDuration = getLastFrameDuration() * 5 / 1000.0f;

			angUpperArm += increment ? STANDARD_ANGLE_INCREMENT * lastFrameDuration : -STANDARD_ANGLE_INCREMENT * lastFrameDuration;
			angUpperArm = clamp(angUpperArm, -90.0f, 0.0f);
		}

		void adjLowerArm(boolean increment) {
			float lastFrameDuration = getLastFrameDuration() * 5 / 1000.0f;

			angLowerArm += increment ? STANDARD_ANGLE_INCREMENT * lastFrameDuration : -STANDARD_ANGLE_INCREMENT * lastFrameDuration;
			angLowerArm = clamp(angLowerArm, 0.0f, 146.25f);
		}

		void adjWristPitch(boolean increment) {
			float lastFrameDuration = getLastFrameDuration() * 5 / 1000.0f;

			angWristPitch += increment ? STANDARD_ANGLE_INCREMENT * lastFrameDuration : -STANDARD_ANGLE_INCREMENT * lastFrameDuration;
			angWristPitch = clamp(angWristPitch, 0.0f, 90.0f);
		}

		void adjWristRoll(boolean increment) {
			float lastFrameDuration = getLastFrameDuration() * 5 / 1000.0f;

			angWristRoll += increment ? STANDARD_ANGLE_INCREMENT * lastFrameDuration : -STANDARD_ANGLE_INCREMENT * lastFrameDuration;
			angWristRoll = angWristRoll % 360.0f;
		}

		void adjFingerOpen(boolean increment) {
			float lastFrameDuration = getLastFrameDuration() * 5 / 1000.0f;

			angFingerOpen += increment ? SMALL_ANGLE_INCREMENT * lastFrameDuration : -SMALL_ANGLE_INCREMENT * lastFrameDuration;
			angFingerOpen = clamp(angFingerOpen, 9.0f, 90.0f);
		}
	
		
		void writePose() {
			System.out.printf("angBase:\t%f\n", angBase);
			System.out.printf("angUpperArm:\t%f\n", angUpperArm);
			System.out.printf("angLowerArm:\t%f\n", angLowerArm);
			System.out.printf("angWristPitch:\t%f\n", angWristPitch);
			System.out.printf("angWristRoll:\t%f\n", angWristRoll);
			System.out.printf("angFingerOpen:\t%f\n", angFingerOpen);
			System.out.printf("\n");
		}
	}
	
		
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private final float frustumScale = calcFrustumScale(45.0f);

	
	private float calcFrustumScale(float fovDeg) {
		final float degToRad = 3.14159f * 2.0f / 360.0f;
		float fovRad = fovDeg * degToRad;
		
		return 1.0f / (float) (Math.tan(fovRad / 2.0f));
	}
	
	
	private float clamp(float value, float minValue, float maxValue) {
		if (value < minValue) {
			return minValue;
		}

		if (value > maxValue) {
			return maxValue;
		}

		return value;
	}

	
	private static float degToRad(float angDeg) {
		final float degToRad = 3.14159f * 2.0f / 360.0f;
		
		return angDeg * degToRad;
	}
	
	
	private static Mat3 rotateX(float angDeg) {
		float angRad = degToRad(angDeg);
		float cos = (float) Math.cos(angRad);
		float sin = (float) Math.sin(angRad);

		Mat3 theMat = new Mat3(1.0f);
		theMat.set(1, 1, cos); theMat.set(2, 1, -sin); 
		theMat.set(1, 2, sin); theMat.set(2, 2, cos); 
		
		return theMat;
	}

	private static Mat3 rotateY(float angDeg) {
		float angRad = degToRad(angDeg);
		float cos = (float) Math.cos(angRad);
		float sin = (float) Math.sin(angRad);

		Mat3 theMat = new Mat3(1.0f);
		theMat.set(0, 0, cos); 	theMat.set(2, 0, sin); 
		theMat.set(0, 2, -sin); theMat.set(2, 2, cos);
		
		return theMat;
	}

	private static Mat3 rotateZ(float angDeg) {
		float angRad = degToRad(angDeg);
		float cos = (float) Math.cos(angRad);
		float sin = (float) Math.sin(angRad);

		Mat3 theMat = new Mat3(1.0f);
		theMat.set(0, 0, cos); theMat.set(1, 0, -sin); 
		theMat.set(0, 1, sin); theMat.set(1, 1, cos);
		
		return theMat;
	}
}