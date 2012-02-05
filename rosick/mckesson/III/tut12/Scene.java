package rosick.mckesson.III.tut12;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;

import rosick.PortingUtils;
import rosick.PortingUtils.BufferableData;
import rosick.framework.Mesh;
import rosick.glm.Glm;
import rosick.glm.Mat3;
import rosick.glm.Vec3;
import rosick.glm.Vec4;
import rosick.glutil.MatrixStack;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public abstract class Scene {
		 	
	static class ProgramData {
		int theProgram;

		int modelToCameraMatrixUnif;
		int normalModelToCameraMatrixUnif;
	}

	
	private class MaterialBlock extends BufferableData {
		Vec4 diffuseColor;
		Vec4 specularColor;
		float specularShininess;
		float padding[] = new float[3];

		static final int SIZE = (4 + 4 + 1 + 3) * (Float.SIZE / 8);


		@Override
		public byte[] getAsByteArray() {
			float data[] = new float[12];
			System.arraycopy(diffuseColor.get(), 0, data, 0, 4);
			System.arraycopy(specularColor.get(), 0, data, 4, 4);
			data[8] = specularShininess;
			System.arraycopy(padding, 0, data, 9, padding.length);
			
			return PortingUtils.toByteArray(data);
		}
	}
	
	
	private static final int MATERIAL_COUNT = 6;

	private Mesh m_pTerrainMesh;
	private Mesh m_pCubeMesh;
	private Mesh m_pTetraMesh;
	private Mesh m_pCylMesh;
	private Mesh m_pSphereMesh;

	private int m_sizeMaterialBlock;
	private int m_materialUniformBuffer;
	
	private FloatBuffer tempSharedFloatBuffer9 = BufferUtils.createFloatBuffer(9);
	private FloatBuffer tempSharedFloatBuffer16 = BufferUtils.createFloatBuffer(16);
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	Scene(String basepath) {
		m_pTerrainMesh 	= new Mesh(basepath + "Ground.xml");
		m_pCubeMesh 	= new Mesh(basepath + "UnitCube.xml");
		m_pTetraMesh 	= new Mesh(basepath + "UnitTetrahedron.xml");
		m_pCylMesh 		= new Mesh(basepath + "UnitCylinder.xml");
		m_pSphereMesh 	= new Mesh(basepath + "UnitSphere.xml");
		
		// Align the size of each MaterialBlock to the uniform buffer alignment.
		int uniformBufferAlignSize = glGetInteger(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT);

		m_sizeMaterialBlock = MaterialBlock.SIZE;
		m_sizeMaterialBlock += uniformBufferAlignSize - (m_sizeMaterialBlock % uniformBufferAlignSize);

		int sizeMaterialUniformBuffer = m_sizeMaterialBlock * MATERIAL_COUNT;

		ArrayList<MaterialBlock> materials = new ArrayList<>();
		getMaterials(materials);

		byte mtlBuffer[] = new byte[sizeMaterialUniformBuffer];
		
		for (int mtl = 0; mtl < materials.size(); mtl++) {
			byte currMtl[] = materials.get(mtl).getAsByteArray();
			
			System.arraycopy(currMtl, 0, mtlBuffer, mtl * m_sizeMaterialBlock, MaterialBlock.SIZE);
		}

		ByteBuffer tempByteBuffer = BufferUtils.createByteBuffer(mtlBuffer.length);
		tempByteBuffer.put(mtlBuffer);
		tempByteBuffer.flip();
		
		m_materialUniformBuffer = glGenBuffers();
		glBindBuffer(GL_UNIFORM_BUFFER, m_materialUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, tempByteBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	abstract ProgramData getProgram(LightingProgramTypes eType);
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	enum LightingProgramTypes {
		LP_VERT_COLOR_DIFFUSE_SPECULAR,
		LP_VERT_COLOR_DIFFUSE,

		LP_MTL_COLOR_DIFFUSE_SPECULAR,
		LP_MTL_COLOR_DIFFUSE,

		LP_MAX_LIGHTING_PROGRAM_TYPES;
	}
	
	
	void draw(MatrixStack modelMatrix, int materialBlockIndex, float alphaTetra) {
		// Render the ground plane.
		{
			modelMatrix.push();
			
			modelMatrix.rotateX(-90.0f);

			drawObject(m_pTerrainMesh, getProgram(LightingProgramTypes.LP_VERT_COLOR_DIFFUSE), materialBlockIndex, 0, modelMatrix);
			
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

			drawObject(m_pTetraMesh, "lit-color", getProgram(LightingProgramTypes.LP_VERT_COLOR_DIFFUSE_SPECULAR), materialBlockIndex, 1, modelMatrix);
			
			modelMatrix.pop();
		}

		// Render the monolith object.
		{
			modelMatrix.push();
			
			modelMatrix.translate(88.0f, 5.0f, -80.0f);
			modelMatrix.scale(4.0f, 4.0f, 4.0f);
			modelMatrix.scale(4.0f, 9.0f, 1.0f);
			modelMatrix.translate(0.0f, 0.5f, 0.0f);

			drawObject(m_pCubeMesh, "lit", getProgram(LightingProgramTypes.LP_MTL_COLOR_DIFFUSE_SPECULAR), materialBlockIndex, 2, modelMatrix);
			
			modelMatrix.pop();
		}

		// Render the cube object.
		{
			modelMatrix.push();
			
			modelMatrix.translate(-52.5f, 14.0f, 65.0f);
			modelMatrix.rotateZ(50.0f);
			modelMatrix.rotateY(-10.0f);
			modelMatrix.scale(20.0f, 20.0f, 20.0f);

			drawObject(m_pCubeMesh, "lit-color", getProgram(LightingProgramTypes.LP_VERT_COLOR_DIFFUSE_SPECULAR), materialBlockIndex, 3, modelMatrix);
			
			modelMatrix.pop();
		}

		// Render the cylinder.
		{
			modelMatrix.push();
			
			modelMatrix.translate(-7.0f, 30.0f, -14.0f);
			modelMatrix.scale(15.0f, 55.0f, 15.0f);
			modelMatrix.translate(0.0f, 0.5f, 0.0f);

			drawObject(m_pCylMesh, "lit-color", getProgram(LightingProgramTypes.LP_VERT_COLOR_DIFFUSE_SPECULAR), materialBlockIndex, 4, modelMatrix);
			
			modelMatrix.pop();
		}
 
		// Render the sphere.
		{
			modelMatrix.push();
			
			modelMatrix.translate(-83.0f, 14.0f, -77.0f);
			modelMatrix.scale(20.0f, 20.0f, 20.0f);

			drawObject(m_pSphereMesh, "lit", getProgram(LightingProgramTypes.LP_MTL_COLOR_DIFFUSE_SPECULAR), materialBlockIndex, 5, modelMatrix);
			
			modelMatrix.pop();
		}
	}
		
	
	void drawObject(Mesh pMesh, ProgramData prog, int materialBlockIndex, int mtlIx, MatrixStack modelMatrix) {
		glBindBufferRange(GL_UNIFORM_BUFFER, materialBlockIndex, m_materialUniformBuffer, mtlIx * m_sizeMaterialBlock, MaterialBlock.SIZE);
		
		Mat3 normMatrix = new Mat3(modelMatrix.top());
		normMatrix = Glm.transpose(Glm.inverse(normMatrix));
		
		glUseProgram(prog.theProgram);
		glUniformMatrix4(prog.modelToCameraMatrixUnif, false, modelMatrix.top().fillBuffer(tempSharedFloatBuffer16));

		glUniformMatrix3(prog.normalModelToCameraMatrixUnif, false, normMatrix.fillBuffer(tempSharedFloatBuffer9));
		pMesh.render();
		glUseProgram(0);
		
		glBindBufferBase(GL_UNIFORM_BUFFER, materialBlockIndex, 0);
	}
	
	void drawObject(Mesh pMesh, String meshName, ProgramData prog, int materialBlockIndex, int mtlIx, MatrixStack modelMatrix) {
		glBindBufferRange(GL_UNIFORM_BUFFER, materialBlockIndex, m_materialUniformBuffer, mtlIx * m_sizeMaterialBlock, MaterialBlock.SIZE);
		
		Mat3 normMatrix = new Mat3(modelMatrix.top());
		normMatrix = Glm.transpose(Glm.inverse(normMatrix));
		
		glUseProgram(prog.theProgram);
		glUniformMatrix4(prog.modelToCameraMatrixUnif, false, modelMatrix.top().fillBuffer(tempSharedFloatBuffer16));

		glUniformMatrix3(prog.normalModelToCameraMatrixUnif, false, normMatrix.fillBuffer(tempSharedFloatBuffer9));
		pMesh.render(meshName);
		glUseProgram(0);
		
		glBindBufferBase(GL_UNIFORM_BUFFER, materialBlockIndex, 0);
	}
	
	
	Mesh getSphereMesh() {
		return m_pSphereMesh;
	}
	
	Mesh getCubeMesh() {
		return m_pCubeMesh;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private void getMaterials(ArrayList<MaterialBlock> materials) {	
		MaterialBlock temp;
		
		// Ground
		temp = new MaterialBlock();
		temp.diffuseColor = new Vec4(1.0f);
		temp.specularColor = new Vec4(0.5f, 0.5f, 0.5f, 1.0f);
		temp.specularShininess = 0.6f;
		materials.add(temp);
		
		// Tetrahedron
		temp = new MaterialBlock();
		temp.diffuseColor = new Vec4(0.5f);
		temp.specularColor = new Vec4(0.5f, 0.5f, 0.5f, 1.0f);
		temp.specularShininess = 0.05f;
		materials.add(temp);

		// Monolith
		temp = new MaterialBlock();
		temp.diffuseColor = new Vec4(0.05f);
		temp.specularColor = new Vec4(0.95f, 0.95f, 0.95f, 1.0f);
		temp.specularShininess = 0.4f;
		materials.add(temp);

		// Cube
		temp = new MaterialBlock();
		temp.diffuseColor = new Vec4(0.5f);
		temp.specularColor = new Vec4(0.3f, 0.3f, 0.3f, 1.0f);
		temp.specularShininess = 0.1f;
		materials.add(temp);

		// Cylinder
		temp = new MaterialBlock();
		temp.diffuseColor = new Vec4(0.5f);
		temp.specularColor = new Vec4(0.0f, 0.0f, 0.0f, 1.0f);
		temp.specularShininess = 0.6f;
		materials.add(temp);

		// Sphere
		temp = new MaterialBlock();
		temp.diffuseColor = new Vec4(0.63f, 0.60f, 0.02f, 1.0f);
		temp.specularColor = new Vec4(0.22f, 0.20f, 0.0f, 1.0f);
		temp.specularShininess = 0.3f;
		materials.add(temp);
	}
}