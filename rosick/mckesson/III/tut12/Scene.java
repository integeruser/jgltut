package rosick.mckesson.III.tut12;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;

import org.lwjgl.BufferUtils;

import rosick.jglsdk.BufferableData;
import rosick.jglsdk.glm.Glm;
import rosick.jglsdk.glm.Mat3;
import rosick.jglsdk.glm.Mat4;
import rosick.jglsdk.glm.Vec3;
import rosick.jglsdk.glm.Vec4;
import rosick.jglsdk.glutil.MatrixStack;
import rosick.mckesson.framework.Mesh;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public abstract class Scene {

	Scene() {
		terrainMesh	= new Mesh("Ground.xml");
		cubeMesh 	= new Mesh("UnitCube.xml");
		tetraMesh 	= new Mesh("UnitTetrahedron.xml");
		cylMesh 	= new Mesh("UnitCylinder.xml");
		sphereMesh 	= new Mesh("UnitSphere.xml");
		
		// Align the size of each MaterialBlock to the uniform buffer alignment.
		int uniformBufferAlignSize = glGetInteger(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT);

		sizeMaterialBlock = MaterialBlock.SIZE;
		sizeMaterialBlock += uniformBufferAlignSize - (sizeMaterialBlock % uniformBufferAlignSize);

		int sizeMaterialUniformBuffer = sizeMaterialBlock * MATERIAL_COUNT;

		ArrayList<MaterialBlock> materials = new ArrayList<>(MATERIAL_COUNT);
		getMaterials(materials);
		
		FloatBuffer materialsBuffer = BufferUtils.createFloatBuffer(sizeMaterialUniformBuffer);
		final float[] padding = new float[(sizeMaterialBlock - MaterialBlock.SIZE) / (Float.SIZE / Byte.SIZE)];
		
		for (MaterialBlock materialBlock : materials) {
			materialBlock.fillBuffer(materialsBuffer);
			materialsBuffer.put(padding);						// The buffer size must be sizeMaterialUniformBuffer
		}
		
		materialsBuffer.flip();

		materialUniformBuffer = glGenBuffers();
		glBindBuffer(GL_UNIFORM_BUFFER, materialUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, materialsBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	static class ProgramData {
		int theProgram;

		int modelToCameraMatrixUnif;
		int normalModelToCameraMatrixUnif;
	}
	
	
	abstract ProgramData getProgram(LightingProgramTypes type);
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	enum LightingProgramTypes {
		VERT_COLOR_DIFFUSE_SPECULAR,
		VERT_COLOR_DIFFUSE,

		MTL_COLOR_DIFFUSE_SPECULAR,
		MTL_COLOR_DIFFUSE,

		MAX_LIGHTING_PROGRAM_TYPES
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	void draw(MatrixStack modelMatrix, int materialBlockIndex, float alphaTetra) {
		// Render the ground plane.
		{
			modelMatrix.push();
			
			modelMatrix.rotateX(-90.0f);

			drawObject(terrainMesh, getProgram(LightingProgramTypes.VERT_COLOR_DIFFUSE), 
					materialBlockIndex, 0, modelMatrix);
			
			modelMatrix.pop();
		}

		// Render the tetrahedron object.
		{
			modelMatrix.push();
			
			modelMatrix.translate(75.0f, 5.0f, 75.0f);
			modelMatrix.rotateY(360.0f * alphaTetra);
			modelMatrix.scale(10.0f, 10.0f, 10.0f);
			modelMatrix.translate(0.0f, (float) Math.sqrt(2.0f), 0.0f);
			modelMatrix.rotate(new Vec3(-0.707f, 0.0f, -0.707f), 54.735f);

			drawObject(tetraMesh, "lit-color", getProgram(LightingProgramTypes.VERT_COLOR_DIFFUSE_SPECULAR), 
					materialBlockIndex, 1, modelMatrix);
			
			modelMatrix.pop();
		}

		// Render the monolith object.
		{
			modelMatrix.push();
			
			modelMatrix.translate(88.0f, 5.0f, -80.0f);
			modelMatrix.scale(4.0f, 4.0f, 4.0f);
			modelMatrix.scale(4.0f, 9.0f, 1.0f);
			modelMatrix.translate(0.0f, 0.5f, 0.0f);

			drawObject(cubeMesh, "lit", getProgram(LightingProgramTypes.MTL_COLOR_DIFFUSE_SPECULAR), 
					materialBlockIndex, 2, modelMatrix);
			
			modelMatrix.pop();
		}

		// Render the cube object.
		{
			modelMatrix.push();
			
			modelMatrix.translate(-52.5f, 14.0f, 65.0f);
			modelMatrix.rotateZ(50.0f);
			modelMatrix.rotateY(-10.0f);
			modelMatrix.scale(20.0f, 20.0f, 20.0f);

			drawObject(cubeMesh, "lit-color", getProgram(LightingProgramTypes.VERT_COLOR_DIFFUSE_SPECULAR), 
					materialBlockIndex, 3, modelMatrix);
			
			modelMatrix.pop();
		}

		// Render the cylinder.
		{
			modelMatrix.push();
			
			modelMatrix.translate(-7.0f, 30.0f, -14.0f);
			modelMatrix.scale(15.0f, 55.0f, 15.0f);
			modelMatrix.translate(0.0f, 0.5f, 0.0f);

			drawObject(cylMesh, "lit-color", getProgram(LightingProgramTypes.VERT_COLOR_DIFFUSE_SPECULAR), 
					materialBlockIndex, 4, modelMatrix);
			
			modelMatrix.pop();
		}
 
		// Render the sphere.
		{
			modelMatrix.push();
			
			modelMatrix.translate(-83.0f, 14.0f, -77.0f);
			modelMatrix.scale(20.0f, 20.0f, 20.0f);

			drawObject(sphereMesh, "lit", getProgram(LightingProgramTypes.MTL_COLOR_DIFFUSE_SPECULAR), 
					materialBlockIndex, 5, modelMatrix);
			
			modelMatrix.pop();
		}
	}
		
	
	void drawObject(Mesh mesh, ProgramData progData, int materialBlockIndex, int materialIndex, MatrixStack modelMatrix) {
		glBindBufferRange(GL_UNIFORM_BUFFER, materialBlockIndex, materialUniformBuffer, 
				materialIndex * sizeMaterialBlock, MaterialBlock.SIZE);
		
		Mat3 normMatrix = new Mat3(modelMatrix.top());
		normMatrix = Glm.transpose(Glm.inverse(normMatrix));
		
		glUseProgram(progData.theProgram);
		glUniformMatrix4(progData.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(mat4Buffer));

		glUniformMatrix3(progData.normalModelToCameraMatrixUnif, false, normMatrix.fillAndFlipBuffer(mat3Buffer));
		mesh.render();
		glUseProgram(0);
		
		glBindBufferBase(GL_UNIFORM_BUFFER, materialBlockIndex, 0);
	}
	
	void drawObject(Mesh mesh, String meshName, ProgramData progData, int materialBlockIndex, int materialIndex, MatrixStack modelMatrix) {
		glBindBufferRange(GL_UNIFORM_BUFFER, materialBlockIndex, materialUniformBuffer, 
				materialIndex * sizeMaterialBlock, MaterialBlock.SIZE);
		
		Mat3 normMatrix = new Mat3(modelMatrix.top());
		normMatrix = Glm.transpose(Glm.inverse(normMatrix));
		
		glUseProgram(progData.theProgram);
		glUniformMatrix4(progData.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(mat4Buffer));

		glUniformMatrix3(progData.normalModelToCameraMatrixUnif, false, normMatrix.fillAndFlipBuffer(mat3Buffer));
		mesh.render(meshName);
		glUseProgram(0);
		
		glBindBufferBase(GL_UNIFORM_BUFFER, materialBlockIndex, 0);
	}
	
	
	Mesh getSphereMesh() {
		return sphereMesh;
	}
	
	Mesh getCubeMesh() {
		return cubeMesh;
	}

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private Mesh terrainMesh;
	private Mesh cubeMesh;
	private Mesh tetraMesh;
	private Mesh cylMesh;
	private Mesh sphereMesh;

	private int sizeMaterialBlock;
	private int materialUniformBuffer;
	
	private FloatBuffer mat3Buffer = BufferUtils.createFloatBuffer(Mat3.SIZE);
	private FloatBuffer mat4Buffer = BufferUtils.createFloatBuffer(Mat4.SIZE);
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private final int MATERIAL_COUNT = 6;

	
	private class MaterialBlock extends BufferableData<FloatBuffer> {
		Vec4 diffuseColor;
		Vec4 specularColor;
		float specularShininess;
		float padding[] = new float[3];

		static final int SIZE = Vec4.SIZE + Vec4.SIZE + ((1 + 3) * (Float.SIZE / Byte.SIZE));

		@Override
		public FloatBuffer fillBuffer(FloatBuffer buffer) {
			diffuseColor.fillBuffer(buffer);
			specularColor.fillBuffer(buffer);
			buffer.put(specularShininess);
			buffer.put(padding);
			
			return buffer;
		}
	}
	
	
	private void getMaterials(ArrayList<MaterialBlock> materials) {
		MaterialBlock matBlock;
		
		// Ground
		matBlock = new MaterialBlock();
		matBlock.diffuseColor = new Vec4(1.0f);
		matBlock.specularColor = new Vec4(0.5f, 0.5f, 0.5f, 1.0f);
		matBlock.specularShininess = 0.6f;
		materials.add(matBlock);
		
		// Tetrahedron
		matBlock = new MaterialBlock();
		matBlock.diffuseColor = new Vec4(0.5f);
		matBlock.specularColor = new Vec4(0.5f, 0.5f, 0.5f, 1.0f);
		matBlock.specularShininess = 0.05f;
		materials.add(matBlock);

		// Monolith
		matBlock = new MaterialBlock();
		matBlock.diffuseColor = new Vec4(0.05f);
		matBlock.specularColor = new Vec4(0.95f, 0.95f, 0.95f, 1.0f);
		matBlock.specularShininess = 0.4f;
		materials.add(matBlock);

		// Cube
		matBlock = new MaterialBlock();
		matBlock.diffuseColor = new Vec4(0.5f);
		matBlock.specularColor = new Vec4(0.3f, 0.3f, 0.3f, 1.0f);
		matBlock.specularShininess = 0.1f;
		materials.add(matBlock);

		// Cylinder
		matBlock = new MaterialBlock();
		matBlock.diffuseColor = new Vec4(0.5f);
		matBlock.specularColor = new Vec4(0.0f, 0.0f, 0.0f, 1.0f);
		matBlock.specularShininess = 0.6f;
		materials.add(matBlock);

		// Sphere
		matBlock = new MaterialBlock();
		matBlock.diffuseColor = new Vec4(0.63f, 0.60f, 0.02f, 1.0f);
		matBlock.specularColor = new Vec4(0.22f, 0.20f, 0.0f, 1.0f);
		matBlock.specularShininess = 0.3f;
		materials.add(matBlock);
	}
}