package rosick.mckesson.chapter06;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import rosick.engine.Game;
import rosick.engine.Main;
import rosick.engine.math.Vector3;
import rosick.engine.render.utils.ShaderUtils;
import rosick.engine.utils.InputOutput;
import rosick.engine.utils.TimeManager;


public class Hierarchy04 extends Game {
	
	public static void main(String[] args) {		
		Main.startLoop(new Hierarchy04());
	}
	
	

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
	private final float vertexData[] = {
			//Front
			+1.0f, +1.0f, +1.0f,
			+1.0f, -1.0f, +1.0f,
			-1.0f, -1.0f, +1.0f,
			-1.0f, +1.0f, +1.0f,

			//Top
			+1.0f, +1.0f, +1.0f,
			-1.0f, +1.0f, +1.0f,
			-1.0f, +1.0f, -1.0f,
			+1.0f, +1.0f, -1.0f,

			//Left
			+1.0f, +1.0f, +1.0f,
			+1.0f, +1.0f, -1.0f,
			+1.0f, -1.0f, -1.0f,
			+1.0f, -1.0f, +1.0f,

			//Back
			+1.0f, +1.0f, -1.0f,
			-1.0f, +1.0f, -1.0f,
			-1.0f, -1.0f, -1.0f,
			+1.0f, -1.0f, -1.0f,

			//Bottom
			+1.0f, -1.0f, +1.0f,
			+1.0f, -1.0f, -1.0f,
			-1.0f, -1.0f, -1.0f,
			-1.0f, -1.0f, +1.0f,

			//Right
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
	
	int program, vertexBufferObject, indexBufferObject, offsetUniform;
	int modelToCameraMatrixUnif, cameraToClipMatrixUnif;
	int vao;
	
	final int numberOfVertices = 24;

	private FloatBuffer cameraToClipMatrix = BufferUtils.createFloatBuffer(16);
	private FloatBuffer modelToCameraMatrix = BufferUtils.createFloatBuffer(16);
	
	private float fFrustumScale = calcFrustumScale(45.0f);
	
	TimeManager timeManager;
	float elapsedTime;
	Hierarchy robottino = new Hierarchy();

	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	
	
	@Override
	public boolean init() {	
		timeManager = TimeManager.getInstance();
		
		int vertexShader =		ShaderUtils.createShader(GL_VERTEX_SHADER, 		"media/shaders/mckesson/chapter06/PosColorLocalTransform.vert");
		int fragmentShader = 	ShaderUtils.createShader(GL_FRAGMENT_SHADER, 	"media/shaders/mckesson/chapter06/ColorPassthrough.frag");
        
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(vertexShader);
		shaderList.add(fragmentShader);

		program = ShaderUtils.createProgram(shaderList);
		
	    for (Integer integer : shaderList) {
			glDeleteShader(integer);
		}

		{
		    modelToCameraMatrixUnif = glGetUniformLocation(program, "modelToCameraMatrix");
			cameraToClipMatrixUnif = glGetUniformLocation(program, "cameraToClipMatrix");
			
			float fzNear = 1.0f; float fzFar = 45.0f;
			cameraToClipMatrix.put(0, fFrustumScale);
			cameraToClipMatrix.put(5, fFrustumScale);
			cameraToClipMatrix.put(10, (fzFar + fzNear) / (fzNear - fzFar));
			cameraToClipMatrix.put(11, -1.0f);
			cameraToClipMatrix.put(14, (2 * fzFar * fzNear) / (fzNear - fzFar));
			
			modelToCameraMatrix.put(0, 1);
			modelToCameraMatrix.put(5, 1);
			modelToCameraMatrix.put(10, 1);
			modelToCameraMatrix.put(15, 1);
			
			glUseProgram(program);
			glUniformMatrix4(cameraToClipMatrixUnif, false, cameraToClipMatrix);
			glUseProgram(0);
		}
		
		// Initialize Vertex Buffer
		{		    
			FloatBuffer vertexDataBuffer = InputOutput.allocFloats(vertexData);
	        
	        vertexBufferObject = glGenBuffers();	       
			glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
		    glBufferData(GL_ARRAY_BUFFER, vertexDataBuffer, GL_STATIC_DRAW);
			glBindBuffer(GL_ARRAY_BUFFER, 0);
			
			
			ShortBuffer indexDataBuffer = InputOutput.allocShorts(indexData);
	        
	        indexBufferObject = glGenBuffers();	       
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferObject);
		    glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexDataBuffer, GL_STATIC_DRAW);
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		}
		
		// Initialize Vertex Arrays
		{
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
		}
				
		return program > 0;
	}

	
	@Override
	public void initGL() {
	    glEnable(GL_CULL_FACE);
	    glCullFace(GL_BACK);
	    glFrontFace(GL_CW);
	    
	    glEnable(GL_DEPTH_TEST);
		glDepthMask(true);
		glDepthFunc(GL_LEQUAL);
		glDepthRange(0.0f, 1.0f);
	}
	
	
	@Override
	public void update(double dtSecs) {
		dtSecs *= 5;
		
		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			robottino.AdjBase((float) (11.25f*dtSecs));
		}else if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			robottino.AdjBase((float) (-11.25f*dtSecs));
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			robottino.AdjUpperArm((float) (-11.25f*dtSecs));
		}else if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			robottino.AdjUpperArm((float) (11.25f*dtSecs));
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_R)) {
			robottino.AdjLowerArm((float) (-11.25f*dtSecs));
		}else if (Keyboard.isKeyDown(Keyboard.KEY_F)) {
			robottino.AdjLowerArm((float) (11.25f*dtSecs));
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_T)) {
			robottino.AdjWristPitch((float) (-11.25f*dtSecs));
		}else if (Keyboard.isKeyDown(Keyboard.KEY_G)) {
			robottino.AdjWristPitch((float) (11.25f*dtSecs));
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_Z)) {
			robottino.AdjWristRoll((float) (11.25f*dtSecs));
		}else if (Keyboard.isKeyDown(Keyboard.KEY_C)) {
			robottino.AdjWristRoll((float) (-11.25f*dtSecs));
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
			robottino.AdjFingerOpen((float) (9f*dtSecs));
		}else if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
			robottino.AdjFingerOpen((float) (-9f*dtSecs));
		}
	}
	
	
	@Override
	public void render3d() {			
		elapsedTime = (float) timeManager.getElapsedTime() * 0.000000001f;
		
		robottino.Draw();
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	/**
	 * Calcola il valore di frustum scale per ottenere il FOV desiderato
	 */
	private float calcFrustumScale(float fFovDeg) {
		final float degToRad = 3.14159f * 2.0f / 360.0f;
		float fFovRad = fFovDeg * degToRad;
		return 1.0f / (float)(Math.tan(fFovRad / 2.0f));
	}
	
	
	static float DegToRad(float fAngDeg) {
		final float fDegToRad = 3.14159f * 2.0f / 360.0f;
		return fAngDeg * fDegToRad;
	}
	
	
	float Clamp(float fValue, float fMinValue, float fMaxValue) {
		if(fValue < fMinValue)
			return fMinValue;

		if(fValue > fMaxValue)
			return fMaxValue;

		return fValue;
	}
	
	
	public static class MatrixStack {
		private Mat4 m_currMat;
		private Deque<Mat4> m_matrices;
		
		public MatrixStack() {
			m_matrices = new ArrayDeque<>();
			m_currMat = new Mat4(1);
		}
		
		public Mat4 Top() {
			return m_currMat;
		}
		
		public void RotateX(float fAngDeg) {
			m_currMat.mul(Mat4.getRotateX(fAngDeg));
		}

		public void RotateY(float fAngDeg) {
			m_currMat.mul(Mat4.getRotateY(fAngDeg));
		}

		public void RotateZ(float fAngDeg) {
			m_currMat.mul(Mat4.getRotateZ(fAngDeg));
		}
		
		public void Scale(float x, float y, float z) {
			Mat4 m = new Mat4();
			m.put(0, x);
			m.put(5, y);
			m.put(10, z);
			m.put(15, 1);
			
			m_currMat.mul(m);
		}
		
		public void Scale(Vector3 scaleVec) {
			Mat4 m = new Mat4();
			m.put(0, scaleVec.x);
			m.put(5, scaleVec.y);
			m.put(10, scaleVec.z);
			m.put(15, 1);
			
			m_currMat.mul(m);
		}
		
		public void Translate(float x, float y, float z) {
			Mat4 m = new Mat4(1.0f);
			m.put(12, x);
			m.put(13, y);
			m.put(14, z);

			m_currMat.mul(m);
		}
		
		public void Translate(Vector3 offsetVec) {
			Mat4 m = new Mat4(1.0f);
			m.put(12, offsetVec.x);
			m.put(13, offsetVec.y);
			m.put(14, offsetVec.z);

			m_currMat.mul(m);
		}

		void Push() {
			m_matrices.push(m_currMat);
			m_currMat = new Mat4(m_currMat);
		}

		void Pop() {
			m_currMat = m_matrices.pop();
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
		
		void Draw() {
			MatrixStack modelToCameraStack = new MatrixStack();

			glUseProgram(program);
			glBindVertexArray(vao);

			modelToCameraStack.Translate(posBase);
			modelToCameraStack.RotateY(angBase);

			//Draw left base.
			{
				modelToCameraStack.Push();
				modelToCameraStack.Translate(posBaseLeft);
				modelToCameraStack.Scale(1.0f, 1.0f, scaleBaseZ);
				glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.Top().getBuffer());
				glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
				modelToCameraStack.Pop();
			}

			//Draw right base.
			{
				modelToCameraStack.Push();
				modelToCameraStack.Translate(posBaseRight);
				modelToCameraStack.Scale(1.0f, 1.0f, scaleBaseZ);
				glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.Top().getBuffer());
				glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
				modelToCameraStack.Pop();
			}

			//Draw main arm.
			DrawUpperArm(modelToCameraStack);

			glBindVertexArray(0);
			glUseProgram(0);
		}
		
		void DrawUpperArm(MatrixStack modelToCameraStack) {
			modelToCameraStack.Push();
			modelToCameraStack.RotateX(angUpperArm);

			{
				modelToCameraStack.Push();
				modelToCameraStack.Translate(0.0f, 0.0f, (sizeUpperArm / 2.0f) - 1.0f);
				modelToCameraStack.Scale(1.0f, 1.0f, sizeUpperArm / 2.0f);
				glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.Top().getBuffer());
				glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
				modelToCameraStack.Pop();
			}

			DrawLowerArm(modelToCameraStack);

			modelToCameraStack.Pop();
		}
		
		void DrawLowerArm(MatrixStack modelToCameraStack) {
			modelToCameraStack.Push();
			modelToCameraStack.Translate(posLowerArm);
			modelToCameraStack.RotateX(angLowerArm);

			modelToCameraStack.Push();
			modelToCameraStack.Translate(0.0f, 0.0f, lenLowerArm / 2.0f);
			modelToCameraStack.Scale(widthLowerArm / 2.0f, widthLowerArm / 2.0f, lenLowerArm / 2.0f);
			glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.Top().getBuffer());
			glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
			modelToCameraStack.Pop();

			DrawWrist(modelToCameraStack);

			modelToCameraStack.Pop();
		}
		
		void DrawWrist(MatrixStack modelToCameraStack) {
			modelToCameraStack.Push();
			modelToCameraStack.Translate(posWrist);
			modelToCameraStack.RotateZ(angWristRoll);
			modelToCameraStack.RotateX(angWristPitch);

			modelToCameraStack.Push();
			modelToCameraStack.Scale(widthWrist / 2.0f, widthWrist/ 2.0f, lenWrist / 2.0f);
			glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.Top().getBuffer());
			glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
			modelToCameraStack.Pop();

			DrawFingers(modelToCameraStack);

			modelToCameraStack.Pop();
		}
		
		void DrawFingers(MatrixStack modelToCameraStack) {
			//Draw left finger
			modelToCameraStack.Push();
			modelToCameraStack.Translate(posLeftFinger);
			modelToCameraStack.RotateY(angFingerOpen);

			modelToCameraStack.Push();
			modelToCameraStack.Translate(0.0f, 0.0f, lenFinger / 2.0f);
			modelToCameraStack.Scale(widthFinger / 2.0f, widthFinger/ 2.0f, lenFinger / 2.0f);
			glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.Top().getBuffer());
			glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
			modelToCameraStack.Pop();

			{
				//Draw left lower finger
				modelToCameraStack.Push();
				modelToCameraStack.Translate(0.0f, 0.0f, lenFinger);
				modelToCameraStack.RotateY(-angLowerFinger);

				modelToCameraStack.Push();
				modelToCameraStack.Translate(0.0f, 0.0f, lenFinger / 2.0f);
				modelToCameraStack.Scale(widthFinger / 2.0f, widthFinger/ 2.0f, lenFinger / 2.0f);
				glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.Top().getBuffer());
				glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
				modelToCameraStack.Pop();

				modelToCameraStack.Pop();
			}

			modelToCameraStack.Pop();

			//Draw right finger
			modelToCameraStack.Push();
			modelToCameraStack.Translate(posRightFinger);
			modelToCameraStack.RotateY(-angFingerOpen);

			modelToCameraStack.Push();
			modelToCameraStack.Translate(0.0f, 0.0f, lenFinger / 2.0f);
			modelToCameraStack.Scale(widthFinger / 2.0f, widthFinger/ 2.0f, lenFinger / 2.0f);
			glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.Top().getBuffer());
			glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
			modelToCameraStack.Pop();

			{
				//Draw right lower finger
				modelToCameraStack.Push();
				modelToCameraStack.Translate(0.0f, 0.0f, lenFinger);
				modelToCameraStack.RotateY(angLowerFinger);

				modelToCameraStack.Push();
				modelToCameraStack.Translate(0.0f, 0.0f, lenFinger / 2.0f);
				modelToCameraStack.Scale(widthFinger / 2.0f, widthFinger/ 2.0f, lenFinger / 2.0f);
				glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraStack.Top().getBuffer());
				glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
				modelToCameraStack.Pop();

				modelToCameraStack.Pop();
			}

			modelToCameraStack.Pop();
		}
		
		void AdjBase(float fIncrement) {
			angBase += fIncrement;
			angBase = angBase % 360.0f;
		}

		void AdjUpperArm(float bIncrement) {
			angUpperArm += bIncrement;
			angUpperArm = Clamp(angUpperArm, -90.0f, 0.0f);
		}

		void AdjLowerArm(float bIncrement) {
			angLowerArm += bIncrement;
			angLowerArm = Clamp(angLowerArm, 0.0f, 146.25f);
		}

		void AdjWristPitch(float bIncrement) {
			angWristPitch += bIncrement;
			angWristPitch = Clamp(angWristPitch, 0.0f, 90.0f);
		}

		void AdjWristRoll(float bIncrement) {
			angWristRoll += bIncrement;
			angWristRoll = angWristRoll % 360.0f;
		}

		void AdjFingerOpen(float bIncrement) {
			angFingerOpen += bIncrement;
			angFingerOpen = Clamp(angFingerOpen, 9.0f, 90.0f);
		}
	}
	
	
	private static class Mat4 {
		float[] m;
		private FloatBuffer buf;
		
		public Mat4() {
			buf = BufferUtils.createFloatBuffer(16);
			m = new float[16];
		}
		
		public Mat4(float diagonal) {
			buf = BufferUtils.createFloatBuffer(16);
			m = new float[16];
			
			m[0] = diagonal;
			m[5] = diagonal;
			m[10] = diagonal;
			m[15] = diagonal;
		}
		
		public Mat4(Mat4 mat) {
			buf = BufferUtils.createFloatBuffer(16);
			m = new float[16];
			System.arraycopy(mat.m, 0, m, 0, 16);
			
		}

		public FloatBuffer getBuffer() {
			buf.put(m);
			buf.flip();
			return buf;
		}

		public void put(int index, float val) {
			m[index] = val;
		}
		
		public void mul(Mat4 mat) {
			float[] res = new float[16];
			float[] m1 = m;
			float[] m2 = mat.m;
			
			for (int r = 0; r < 4; r++) {
				for (int c = 0; c < 4; c++) { 
					float sum = 0;
					for (int k = 0; k < 4; k++) {
						float a = m1[r + 4*k];
						float b = m2[4*c + k];
						sum += a * b;
					}
					res[r + 4*c] = sum;
				}
			}
			
			System.arraycopy(res, 0, m, 0, 16);
		}
		
		public static Mat4 getRotateX(float angoloDeg) {
			float fAngRad = DegToRad(angoloDeg);
			float fCos = (float) Math.cos(fAngRad);
			float fSin = (float) Math.sin(fAngRad);
			
			Mat4 mat = new Mat4();
			
			//X coloumn
			mat.put(0, 1); //x
			mat.put(1, 0); //y
			mat.put(2, 0); //z
			
			//Y coloumn
			mat.put(4, 0); //x
			mat.put(5, fCos); //y
			mat.put(6, fSin); //z
			
			//Z coloumn
			mat.put(8, 0); //x
			mat.put(9, -fSin); //y
			mat.put(10, fCos); //z
			
			//ultimo
			mat.put(15, 1); 
			
			return mat;
		}

		public static Mat4 getRotateY(float angoloDeg) {
			float fAngRad = DegToRad(angoloDeg);
			float fCos = (float) Math.cos(fAngRad);
			float fSin = (float) Math.sin(fAngRad);
			
			Mat4 mat = new Mat4();
			
			//X coloumn
			mat.put(0, fCos); //x
			mat.put(1, 0); //y
			mat.put(2, -fSin); //z
			
			//Y coloumn
			mat.put(4, 0); //x
			mat.put(5, 1); //y
			mat.put(6, 0); //z
			
			//Z coloumn
			mat.put(8, fSin); //x
			mat.put(9, 0); //y
			mat.put(10, fCos); //z

			//ultimo
			mat.put(15, 1); 
			
			return mat;
		}
		

		public static Mat4 getRotateZ(float angoloDeg) {
			float fAngRad = DegToRad(angoloDeg);
			float fCos = (float) Math.cos(fAngRad);
			float fSin = (float) Math.sin(fAngRad);
			
			Mat4 mat = new Mat4();
			
			//X coloumn
			mat.put(0, fCos); //x
			mat.put(1, fSin); //y
			mat.put(2, 0); //z
			
			//Y coloumn
			mat.put(4, -fSin); //x
			mat.put(5, fCos); //y
			mat.put(6, 0); //z
			
			//Z coloumn
			mat.put(8, 0); //x
			mat.put(9, 0); //y
			mat.put(10, 1); //z

			//ultimo
			mat.put(15, 1); 
			
			return mat;
		}
		
		public String toString() {
			String ris = "";
			for (int i = 0; i < 4; i++) {
				ris += m[i*4] + " " + m[i*4+1] + " " + m[i*4+2] + " " + m[i*4+3] + "\n";
			}
			return ris;
		}
	}
}