package fcagnin.gltut.tut02;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import org.lwjgl.BufferUtils;

import fcagnin.gltut.LWJGLWindow;
import fcagnin.gltut.framework.Framework;


/**
 * Visit https://github.com/integeruser/gltut-lwjgl for project info, updates and license terms. info, updates and license terms.
 * <p/>
 * Part I. The Basics
 * Chapter 2. Playing with Colors
 * http://www.arcsynthesis.org/gltut/Basics/Tutorial%2002.html
 *
 * @author integeruser
 */
public class VertexColor extends LWJGLWindow {

	public static void main(String[] args) {
		Framework.CURRENT_TUTORIAL_DATAPATH = "/fcagnin/gltut/tut02/data/";

		new VertexColor().start();
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	@Override
	protected void init() {
		initializeProgram();
		initializeVertexBuffer();

		vao = glGenVertexArrays();
		glBindVertexArray(vao);
	}


	@Override
	protected void display() {
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClear(GL_COLOR_BUFFER_BIT);

		glUseProgram(theProgram);

		glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);

		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(0, 4, GL_FLOAT, false, 0, 0);
		glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, 48);

		glDrawArrays(GL_TRIANGLES, 0, 3);

		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glUseProgram(0);
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private int theProgram;
	private int vao;


	private void initializeProgram() {
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, 	"VertexColors.vert"));
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, "VertexColors.frag"));

		theProgram = Framework.createProgram(shaderList);
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private final float vertexData[] = {
			 0.0f,    0.5f, 0.0f, 1.0f,
			 0.5f, -0.366f, 0.0f, 1.0f,
			-0.5f, -0.366f, 0.0f, 1.0f,
			 1.0f,    0.0f, 0.0f, 1.0f,
			 0.0f,    1.0f, 0.0f, 1.0f,
			 0.0f,    0.0f, 1.0f, 1.0f};

	private int vertexBufferObject;


	private void initializeVertexBuffer() {
		FloatBuffer vertexDataBuffer = BufferUtils.createFloatBuffer(vertexData.length);
		vertexDataBuffer.put(vertexData);
		vertexDataBuffer.flip();

		vertexBufferObject = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
	    glBufferData(GL_ARRAY_BUFFER, vertexDataBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
}