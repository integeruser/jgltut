package rosick.mckesson.chapter06;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import rosick.engine.Game;
import rosick.engine.Main;
import rosick.engine.render.utils.ShaderUtils;
import rosick.engine.utils.InputOutput;
import rosick.engine.utils.TimeManager;


public class Translation01 extends Game {
	
	public static void main(String[] args) {		
		Main.startLoop(new Translation01());
	}
	
	

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
	private final float vertexData[] = {
			+1.0f, +1.0f, +1.0f,
			-1.0f, -1.0f, +1.0f,
			-1.0f, +1.0f, -1.0f,
			+1.0f, -1.0f, -1.0f,

			-1.0f, -1.0f, -1.0f,
			+1.0f, +1.0f, -1.0f,
			+1.0f, -1.0f, +1.0f,
			-1.0f, +1.0f, +1.0f,

			0.0f, 1.0f, 0.0f, 1.0f,
			0.0f, 0.0f, 1.0f, 1.0f,
			1.0f, 0.0f, 0.0f, 1.0f,
			0.5f, 0.5f, 0.0f, 1.0f,

			0.0f, 1.0f, 0.0f, 1.0f,
			0.0f, 0.0f, 1.0f, 1.0f,
			1.0f, 0.0f, 0.0f, 1.0f,
			0.5f, 0.5f, 0.0f, 1.0f,
	};
	
	private final short indexData[] = {
			0, 1, 2,
			1, 0, 3,
			2, 3, 0,
			3, 2, 1,

			5, 4, 6,
			4, 5, 7,
			7, 6, 4,
			6, 7, 5,
	};
	
	int program, vertexBufferObject, indexBufferObject, offsetUniform;
	int modelToCameraMatrixUnif, cameraToClipMatrixUnif;
	int vao;
	
	final int numberOfVertices = 8;

	private FloatBuffer cameraToClipMatrix = BufferUtils.createFloatBuffer(16);
	private FloatBuffer modelToCameraMatrix = BufferUtils.createFloatBuffer(16);
	
	private float fFrustumScale = calcFrustumScale(45.0f);
	
	TimeManager timeManager;
	float elapsedTime;
	

	
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
	public void render3d() {	
		glUseProgram(program);
		glBindVertexArray(vao);
		
		elapsedTime = (float) timeManager.getElapsedTime() * 0.000000001f;
		
		stationaryOffset(elapsedTime);
		glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraMatrix);
		glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
		
		ovalOffset(elapsedTime);
		glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraMatrix);
		glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
		
		bottomCircleOffset(elapsedTime);
		glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraMatrix);
		glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);

		glBindVertexArray(0);
		glUseProgram(0);
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
	
	
	private void stationaryOffset(float fElapsedTime) {
		modelToCameraMatrix.put(12, 0);  											// x
		modelToCameraMatrix.put(13, 0); 											// y
		modelToCameraMatrix.put(14, -20); 											// z
	}

	
	private void ovalOffset(float fElapsedTime) {
		final float fLoopDuration = 3.0f;
		final float fScale = 3.14159f * 2.0f / fLoopDuration;

		float fCurrTimeThroughLoop = fElapsedTime % fLoopDuration;
		
		modelToCameraMatrix.put(12, (float) (Math.cos(fCurrTimeThroughLoop * fScale) * 4.f)); 			// x
		modelToCameraMatrix.put(13, (float) (Math.sin(fCurrTimeThroughLoop * fScale) * 6.f)); 			// y
		modelToCameraMatrix.put(14, -20); 																// z
	}

	
	private void bottomCircleOffset(float fElapsedTime) {
		final float fLoopDuration = 12.0f;
		final float fScale = 3.14159f * 2.0f / fLoopDuration;

		float fCurrTimeThroughLoop = fElapsedTime % fLoopDuration;

		modelToCameraMatrix.put(12, (float) (Math.cos(fCurrTimeThroughLoop * fScale) * 5.f)); 			// x
		modelToCameraMatrix.put(13, -3.5f); 															// y
		modelToCameraMatrix.put(14, (float) (Math.sin(fCurrTimeThroughLoop * fScale) * 5.f -20.f)); 	// z
	}
}