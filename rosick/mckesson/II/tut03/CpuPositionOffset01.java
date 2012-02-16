package rosick.mckesson.II.tut03;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;

import rosick.GLWindow;
import rosick.jglsdk.framework.Framework;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * II. Positioning
 * Chapter 3. OpenGL's Moving Triangle
 * http://www.arcsynthesis.org/gltut/Positioning/Tutorial%2003.html
 * @author integeruser
 */
public class CpuPositionOffset01 extends GLWindow {
	
	public static void main(String[] args) {		
		new	CpuPositionOffset01().start();
	}
	
	
	private final String TUTORIAL_DATAPATH = "/rosick/mckesson/II/tut03/data/";

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private final float vertexPositions[] = {
			0.25f,  0.25f, 0.0f, 1.0f,
			0.25f, -0.25f, 0.0f, 1.0f,
		   -0.25f, -0.25f, 0.0f, 1.0f,
	};
	
	private int theProgram;
	private int positionBufferObject;
	private int vao;
		
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private void initializeProgram() {			
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, 	TUTORIAL_DATAPATH + "Standard.vert"));
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, TUTORIAL_DATAPATH + "Standard.frag"));

		theProgram = Framework.createProgram(shaderList);
	}
	
	private void initializeVertexBuffer() {
		FloatBuffer vertexPositionsBuffer = BufferUtils.createFloatBuffer(vertexPositions.length);
		vertexPositionsBuffer.put(vertexPositions);
		vertexPositionsBuffer.flip();
		
        positionBufferObject = glGenBuffers();	       
		glBindBuffer(GL_ARRAY_BUFFER, positionBufferObject);
	    glBufferData(GL_ARRAY_BUFFER, vertexPositionsBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	
	@Override
	protected void init() {
		initializeProgram();
		initializeVertexBuffer(); 

		vao = glGenVertexArrays();
		glBindVertexArray(vao);
	}
	
		
	@Override
	protected void display() {	
		computePositionOffsets();
	    adjustVertexData();
		
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClear(GL_COLOR_BUFFER_BIT);

		glUseProgram(theProgram);

		glBindBuffer(GL_ARRAY_BUFFER, positionBufferObject);
		
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 4, GL_FLOAT, false, 0, 0);

		glDrawArrays(GL_TRIANGLES, 0, 3);

		glDisableVertexAttribArray(0);
		glUseProgram(0);
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private float fXOffset, fYOffset;

	
	private void computePositionOffsets() {
		final float fLoopDuration = 5.0f;
	    final float fScale = 3.14159f * 2.0f / fLoopDuration;
	    
		float fElapsedTime = (float) (getElapsedTime() / 1000.0);

		float fCurrTimeThroughLoop = fElapsedTime % fLoopDuration;
			    
	    fXOffset = (float) (Math.cos(fCurrTimeThroughLoop * fScale) * 0.5f);
	    fYOffset = (float) (Math.sin(fCurrTimeThroughLoop * fScale) * 0.5f);
	}
	
	
	private void adjustVertexData() {
		float fNewData[] = new float[vertexPositions.length];
		System.arraycopy(vertexPositions, 0, fNewData, 0, vertexPositions.length);
	    
	    for (int iVertex = 0; iVertex < vertexPositions.length; iVertex += 4) {
	        fNewData[iVertex] += fXOffset;
	        fNewData[iVertex + 1] += fYOffset;
	    }
	    
		FloatBuffer fNewDataBuffer = BufferUtils.createFloatBuffer(fNewData.length);
		fNewDataBuffer.put(fNewData);
		fNewDataBuffer.flip();
		
	    glBindBuffer(GL_ARRAY_BUFFER, positionBufferObject);
	    glBufferSubData(GL_ARRAY_BUFFER, 0, fNewDataBuffer);
	    glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
}