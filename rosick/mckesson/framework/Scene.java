package rosick.mckesson.framework;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.lwjgl.BufferUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import rosick.jglsdk.glimg.DdsLoader;
import rosick.jglsdk.glimg.ImageSet;
import rosick.jglsdk.glimg.TextureGenerator;
import rosick.jglsdk.glimg.TextureGenerator.ForcedConvertFlags;
import rosick.jglsdk.glm.Glm;
import rosick.jglsdk.glm.Mat3;
import rosick.jglsdk.glm.Mat4;
import rosick.jglsdk.glm.Quaternion;
import rosick.jglsdk.glm.Vec3;
import rosick.mckesson.framework.SceneBinders.StateBinder;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class Scene {
		
	public Scene(String filename) {
		m_sceneImpl = new SceneImpl(filename);
	}

	

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public void render(Mat4 cameraMatrix) {
		m_sceneImpl.render(cameraMatrix);
	}
	
	
	public final SceneNode findNode(String nodeName) {
		return m_sceneImpl.findNode(nodeName);
	}
	
	public final int findProgram(String progName) {
		return m_sceneImpl.findProgram(progName);
	}

	public final Mesh findMesh(String meshName) {
		return m_sceneImpl.findMesh(meshName);
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private SceneImpl m_sceneImpl;
	
	private FloatBuffer mat3Buffer = BufferUtils.createFloatBuffer(9);
	private FloatBuffer mat4Buffer = BufferUtils.createFloatBuffer(16);
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private class SceneMesh {
		
		SceneMesh(String filename) {
			m_mesh = new Mesh(filename);
		}
		
		
		void render() {
			m_mesh.render();
		}
		
		
		final Mesh getMesh() {
			return m_mesh;
		}
		
		
		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
		private Mesh m_mesh;
	}
	
	
	private class SceneTexture {
		
		SceneTexture(String filename, int creationFlags) {
			String filePath = Framework.findFileOrThrow(filename);
			ImageSet imageSet = null;

			String fileExtension = filename.substring(filename.lastIndexOf('.') + 1);
			if (fileExtension.equals("dds")) {
				try {
					imageSet = DdsLoader.loadFromFile(filePath);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(0);
				}
			}
			else {
				// ImageSet.reset(glimg::loaders::stb::LoadFromFile(pathname.c_str()));
				assert false : fileExtension + " loader not yet implemented";
			}

			m_textureObj = TextureGenerator.createTexture(imageSet, creationFlags);
			m_textureType = TextureGenerator.getTextureType(imageSet, creationFlags);
		}

		@Override
		protected void finalize() throws Throwable {
			super.finalize();
			glDeleteTextures(m_textureObj);
		}
		
		
		final int getTexture() {
			return m_textureObj;
		}
		
		final int getType() {
			return m_textureType;
		}
		
		
		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
		private int m_textureObj, m_textureType;
	}
	
	
	private class SceneProgram {
		
		SceneProgram(int programObj, int matrixLoc, int normalMatLoc) {
			m_programObj = programObj;
			m_matrixLoc = matrixLoc;
			m_normalMatLoc = normalMatLoc;
		}

		@Override
		protected void finalize() throws Throwable {
			super.finalize();
			glDeleteProgram(m_programObj);
		}
		
		
		void useProgram() {
			glUseProgram(m_programObj);
		}
		
		
		final int getMatrixLoc() {
			return m_matrixLoc;
		}
		
		final int getNormalMatLoc() {
			return m_normalMatLoc;
		}

		final int getProgram() {
			return m_programObj;
		}
		
				
		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
		private int m_programObj, m_matrixLoc, m_normalMatLoc;
	}
	
	
	public class SceneNode {
	
		SceneNode(SceneMesh mesh, SceneProgram program, Vec3 nodePos,
				ArrayList<TextureBinding> textureBindings) {
			m_mesh = mesh;
			m_program = program;
			
			m_stateBinders = new ArrayList<StateBinder>();
			m_textureBindings = textureBindings;

			m_nodeTransform = new Transform();
			m_objTransform = new Transform();

			m_nodeTransform.m_trans = nodePos;
		}

	
		/*void nodeSetScale(Vec3 scale) {
			m_nodeTransform.m_scale = new Vec3(scale);
		}*/

		public void nodeSetOrient(Quaternion orient) {
			m_nodeTransform.m_orient = new Quaternion(orient);
		}

		/*void nodeSetTrans(Vec3 offset) {
			m_nodeTransform.m_trans = new Vec3(offset);
		}
		
		void nodeRotate(Quaternion orient) {
			m_nodeTransform.m_orient.mul(orient);
		}

		void nodeOffset(Vec3 offset) {
			m_nodeTransform.m_trans.add(offset);
		}*/

		public final Quaternion nodeGetOrient() {
			return m_nodeTransform.m_orient;
		}

			
		void setNodeScale(Vec3 nodeScale) {
			m_nodeTransform.m_scale = new Vec3(nodeScale);
		}
		
		void setNodeOrient(Quaternion nodeOrient) {
			m_nodeTransform.m_orient = Glm.normalize(nodeOrient);
		}

		
		void render(int samplers[], Mat4 mat) {
			Mat4 baseMat = new Mat4(mat);
			baseMat.mul(m_nodeTransform.getMatrix());
			Mat4 objMat = Mat4.mul(baseMat, m_objTransform.getMatrix());

			m_program.useProgram();
			glUniformMatrix4(m_program.getMatrixLoc(), false, objMat.fillAndFlipBuffer(mat4Buffer));

			if (m_program.getNormalMatLoc() != -1) {
				Mat3 normMat = new Mat3(Glm.transpose(Glm.inverse(objMat)));
				glUniformMatrix3(m_program.getNormalMatLoc(), false, normMat.fillAndFlipBuffer(mat3Buffer));
			}

			for (StateBinder stateBinder : m_stateBinders) {
				stateBinder.bindState(m_program.getProgram());
			}
			
			for (TextureBinding binding : m_textureBindings) {
				glActiveTexture(GL_TEXTURE0 + binding.m_textureUnit);
				glBindTexture(binding.m_texture.getType(), binding.m_texture.getTexture());
				glBindSampler(binding.m_textureUnit, samplers[binding.m_sampler.ordinal()]);
			}

			m_mesh.render();

			for (TextureBinding binding : m_textureBindings) {
				glActiveTexture(GL_TEXTURE0 + binding.m_textureUnit);
				glBindTexture(binding.m_texture.getType(), 0);
				glBindSampler(binding.m_textureUnit, 0);
			}
			
			for (StateBinder stateBinder : m_stateBinders) {
				stateBinder.unbindState(m_program.getProgram());
			}

			glUseProgram(0);
		}
		
	
		void setStateBinder(StateBinder stateBinder) {
			m_stateBinders.add(stateBinder);
		}

		
		final int getProgram() {
			return m_program.getProgram();
		}
		
		
		
		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
		private SceneMesh m_mesh;	
		private SceneProgram m_program;

		private ArrayList<StateBinder> m_stateBinders;	
		private ArrayList<TextureBinding> m_textureBindings;

		private Transform m_nodeTransform;
		private Transform m_objTransform;
	}

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private class Transform {
		
		Transform() {
			m_orient = new Quaternion(1.0f, 0.0f, 0.0f, 0.0f);
			m_scale = new Vec3(1.0f, 1.0f, 1.0f);
			m_trans = new Vec3(0.0f, 0.0f, 0.0f);
		}

		
		Mat4 getMatrix() {
			Mat4 ret = new Mat4();
			ret = Glm.translate(ret, m_trans);
			ret.mul(Glm.matCast(m_orient));
			ret = Glm.scale(ret, m_scale);
			
			return ret;
		}
		
		
		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
		private Quaternion m_orient;
		private Vec3 m_scale, m_trans;
	}

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private enum SamplerTypes {
		NEAREST,
		LINEAR,
		MIPMAP_NEAREST,
		MIPMAP_LINEAR,
		ANISOTROPIC,
		HALF_ANISOTROPIC,

		MAX_SAMPLERS
	}
	
	
	private SamplerTypes getTypeFromName(String samplerName) {
		final String[] samplerNames = {
			"nearest",
			"linear",
			"mipmap nearest",
			"mipmap linear",
			"anisotropic",
			"half anisotropic",
		};
	
		for (int nameIx = 0; nameIx < samplerNames.length; nameIx++) {
			if (samplerNames[nameIx].equals(samplerName)) {
				return SamplerTypes.values()[nameIx];
			}
		}
		
		throw new RuntimeException("Unknown sampler name: " + samplerName);
	}


	private void makeSamplerObjects(int samplers[]) {
		// Always repeat.
		for (int samplerIx = 0; samplerIx < SamplerTypes.MAX_SAMPLERS.ordinal(); samplerIx++) {
			samplers[samplerIx] = glGenSamplers();
			glSamplerParameteri(samplers[samplerIx], GL_TEXTURE_WRAP_S, GL_REPEAT);
			glSamplerParameteri(samplers[samplerIx], GL_TEXTURE_WRAP_T, GL_REPEAT);
			glSamplerParameteri(samplers[samplerIx], GL_TEXTURE_WRAP_R, GL_REPEAT);
		}

		glSamplerParameteri(samplers[0], GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glSamplerParameteri(samplers[0], GL_TEXTURE_MIN_FILTER, GL_NEAREST);

		glSamplerParameteri(samplers[1], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glSamplerParameteri(samplers[1], GL_TEXTURE_MIN_FILTER, GL_LINEAR);

		glSamplerParameteri(samplers[2], GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glSamplerParameteri(samplers[2], GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST);

		glSamplerParameteri(samplers[3], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glSamplerParameteri(samplers[3], GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);

		float maxAniso = glGetFloat(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);

		glSamplerParameteri(samplers[4], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glSamplerParameteri(samplers[4], GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		glSamplerParameterf(samplers[4], GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAniso / 2.0f);

		glSamplerParameteri(samplers[5], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glSamplerParameteri(samplers[5], GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		glSamplerParameterf(samplers[5], GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAniso);
	}

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private class TextureBinding {
		SceneTexture m_texture;
		int m_textureUnit;
		SamplerTypes m_sampler;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
	private class SceneImpl {	
		private Map<String, SceneMesh> 		m_meshes;
		private Map<String, SceneTexture> 	m_textures;
		private Map<String, SceneProgram> 	m_programs;
		private Map<String, SceneNode> 		m_nodes;
		
		private ArrayList<SceneNode> m_rootNodes;

		private int m_samplers[];
		

				
		private SceneImpl(String filename) {		
			m_meshes = new HashMap<>();
			m_textures = new HashMap<>();
			m_programs = new HashMap<>();
			m_nodes = new HashMap<>();
			
			m_rootNodes = new ArrayList<>();

			m_samplers = new int[SamplerTypes.MAX_SAMPLERS.ordinal()];
						
			// Read the xml scene.
			Document document = null;
			try {
				DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
				
				String xmlPath = Framework.findFileOrThrow(filename);
				InputStream xmlInputStream = ClassLoader.class.getResourceAsStream(xmlPath);
				document = documentBuilder.parse(xmlInputStream);
			} catch (SAXException | ParserConfigurationException | IOException e) {
				e.printStackTrace();
				System.exit(0);
			}
					
			Element sceneXmlNode = document.getDocumentElement();

			readMeshes(sceneXmlNode);
			readTextures(sceneXmlNode);
			readPrograms(sceneXmlNode);
			readNodes(null, sceneXmlNode);
			
			makeSamplerObjects(m_samplers);
		}
		
		@Override
		protected void finalize() throws Throwable {
			super.finalize();
			
			for (int sampler : m_samplers) {
				glDeleteSamplers(sampler);
			}
		}

		
		
		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
				
		private void render(Mat4 cameraMat) {
			for (SceneNode sceneNode : m_nodes.values()) {
				sceneNode.render(m_samplers, cameraMat);
			}
		}

		
		
		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
		private SceneNode findNode(String nodeName) {
			SceneNode node = m_nodes.get(nodeName);
			
			if (node == null) {
				throw new RuntimeException("Could not find the node named: " + nodeName);
			}
			
			return node;
		}
		
		private int findProgram(String progName) {
			SceneProgram program = m_programs.get(progName);
			
			if (program == null) {
				throw new RuntimeException("Could not find the program named: " + progName);
			}
			
			return program.getProgram();
		}

		private Mesh findMesh(String meshName) {
			SceneMesh mesh = m_meshes.get(meshName);
			
			if (mesh == null) {
				throw new RuntimeException("Could not find the mesh named: " + meshName);
			}
			
			return mesh.getMesh();
		}
		
		
		
		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
		private void readMeshes(Element sceneNode) {
			ArrayList<Element> meshElements = getElementsByTagName(sceneNode, "mesh");
			
			for (Element element : meshElements) {
				readMesh(element);
			}		
		}
		
		private void readMesh(Element meshNode) {
			String nameNode = meshNode.getAttribute("xml:id");
			String filenameNode = meshNode.getAttribute("file");

			{
				if (nameNode.equals("")) 		throw new RuntimeException("Mesh found with no `xml:id` name specified.");
				if (filenameNode.equals("")) 	throw new RuntimeException("Mesh found with no `file` filename specified.");
	
				if (m_meshes.containsKey(nameNode)) throw new RuntimeException("The mesh named \"" + nameNode + "\" already exists.");
			}
			
			SceneMesh mesh = new SceneMesh(filenameNode);
			m_meshes.put(nameNode, mesh);	
		}	
		
		
		private void readTextures(Element sceneNode) {
			ArrayList<Element> textureElements = getElementsByTagName(sceneNode, "texture");
			
			for (Element element : textureElements) {
				readTexture(element);
			}	
		}
		
		private void readTexture(Element textureNode) {
			String nameNode = textureNode.getAttribute("xml:id");
			String filenameNode = textureNode.getAttribute("file");

			{
				if (nameNode.equals("")) 		throw new RuntimeException("Texture found with no `xml:id` name specified.");
				if (filenameNode.equals(""))	throw new RuntimeException("Texture found with no `file` filename specified.");
	
				if (m_textures.containsKey(nameNode)) throw new RuntimeException("The texture named \"" + nameNode + "\" already exists.");
			}
			
			int creationFlags = 0;
			if (textureNode.getAttribute("srgb").equals("true")) {
				creationFlags = creationFlags | ForcedConvertFlags.FORCE_SRGB_COLORSPACE_FMT;
			}
			
			SceneTexture texture = new SceneTexture(filenameNode, creationFlags);
			m_textures.put(nameNode, texture);	
		}	
		
		
		private void readPrograms(Element sceneNode) {
			ArrayList<Element> programElements = getElementsByTagName(sceneNode, "prog");
			
			for (Element element : programElements) {
				readProgram(element);
			}		
		}
		
		private void readProgram(Element programNode) {
			String nameNode = programNode.getAttribute("xml:id");
			String vertexShaderNode = programNode.getAttribute("vert");
			String fragmentShaderNode = programNode.getAttribute("frag");
			String modelMatrixNode = programNode.getAttribute("model-to-camera");
			
			// Optional.
			String normalMatrixNode = programNode.getAttribute("normal-model-to-camera");
			String geometryShaderNode = programNode.getAttribute("geom");
			
			{
				if (nameNode.equals("")) 			throw new RuntimeException("Program found with no `xml:id` name specified.");
				if (vertexShaderNode.equals("")) 	throw new RuntimeException("Program found with no `vert` filename specified.");
				if (fragmentShaderNode.equals("")) 	throw new RuntimeException("Program found with no `frag` name specified.");
				if (modelMatrixNode.equals("")) 	throw new RuntimeException("Program found with no `model-to-camera` filename specified.");
		
				if (m_programs.containsKey(nameNode)) throw new RuntimeException("The program named \"" + nameNode + "\" already exists.");
			}
			
			int program = 0;
						
			{
				ArrayList<Integer> shaders = new ArrayList<>();
				shaders.add(Framework.loadShader(GL_VERTEX_SHADER, vertexShaderNode));
				shaders.add(Framework.loadShader(GL_FRAGMENT_SHADER, fragmentShaderNode));
			
				if (!geometryShaderNode.equals("")) {
					shaders.add(Framework.loadShader(GL_GEOMETRY_SHADER, geometryShaderNode));	
				}
			
				program = Framework.createProgram(shaders);
				
				for (Integer integer : shaders) {
					glDeleteShader(integer);
				}
			}
			
			int matrixLoc = glGetUniformLocation(program, modelMatrixNode);
			
			if (matrixLoc == -1) {
				glDeleteProgram(program);
				
				throw new RuntimeException("Could not find the matrix uniform " + modelMatrixNode +
						" in program " + nameNode);
			}
			
			int normalMatLoc = -1;
			if (!normalMatrixNode.equals("")) {
				normalMatLoc = glGetUniformLocation(program, normalMatrixNode);
				
				if (normalMatLoc == -1) {
					glDeleteProgram(program);
					
					throw new RuntimeException("Could not find the normal matrix uniform " + normalMatrixNode +
							" in program " + nameNode);
				}
			}
			
			m_programs.put(nameNode, new SceneProgram(program, matrixLoc, normalMatLoc));
			
			readProgramContents(program, programNode);
		}		
		
		private void readProgramContents(int program, Element programNode) {
			Set<String> blockBindings = new HashSet<>();
			Set<String> samplerBindings = new HashSet<>();
			
			{
				NodeList nodes = programNode.getElementsByTagName("block");
				
				for (int nodeIx = 0; nodeIx < nodes.getLength(); nodeIx++) {
					Node node = nodes.item(nodeIx);
					
					if (node.getParentNode().isEqualNode(programNode)) {
						if (node.getNodeType() == Node.ELEMENT_NODE) {
							Element element = (Element) node;

							String nameNode = element.getAttribute("name");
							String bindingNode = element.getAttribute("binding");

							{
								if (nameNode.equals("")) 	throw new RuntimeException("Program `block` element with no `name`.");
								if (bindingNode.equals(""))	throw new RuntimeException("Program `block` element with no `binding`.");
					
								if (blockBindings.contains(nameNode)) {
									throw new RuntimeException("The uniform block " + nameNode + " is used twice in the same program.");
								}
								else {
									blockBindings.add(nameNode);
								}
							}
												
							int blockIx = glGetUniformBlockIndex(program, nameNode);
							if (blockIx == GL_INVALID_INDEX) {
								System.out.println("Warning: the uniform block " + nameNode + " could not be found.");
							}
							
							int bindPoint = Integer.parseInt(bindingNode);
							
							glUniformBlockBinding(program, blockIx, bindPoint);
						}
					}
				}		
			}
			
			{
				NodeList nodes = programNode.getElementsByTagName("sampler");
				
				for (int nodeIx = 0; nodeIx < nodes.getLength(); nodeIx++) {
					Node node = nodes.item(nodeIx);
					
					if (node.getParentNode().isEqualNode(programNode)) {
						if (node.getNodeType() == Node.ELEMENT_NODE) {
							Element element = (Element) node;

							String nameNode = element.getAttribute("name");
							String texunitNode = element.getAttribute("unit");

							{
								if (nameNode.equals("")) 	throw new RuntimeException("Program `sampler` element with no `name`.");
								if (texunitNode.equals(""))	throw new RuntimeException("Program `sampler` element with no `unit`.");
					
								if (samplerBindings.contains(nameNode)) {
									throw new RuntimeException("A sampler " + nameNode + " is used twice within the same program.");
								}
								else {
									samplerBindings.add(nameNode);
								}
							}
												
							int samplerLoc = glGetUniformLocation(program, nameNode);
							if (samplerLoc == -1) {
								System.out.println("Warning: the sampler " + nameNode + " could not be found.");
							}
							
							int textureUnit = Integer.parseInt(texunitNode);
							
							glUseProgram(program);
							glUniform1i(samplerLoc, textureUnit);
							glUseProgram(0);
						}
					}
				}
			}
		}
		
		
		private void readNodes(SceneNode parent, Element sceneNode) {
			ArrayList<Element> nodeElements = getElementsByTagName(sceneNode, "node");
			
			for (Element element : nodeElements) {
				readNode(null, element);
			}	
		}

		private void readNode(SceneNode parent, Element nodeNode) {
			String nameNode = nodeNode.getAttribute("name");
			String meshNode = nodeNode.getAttribute("mesh");
			String progNode = nodeNode.getAttribute("prog");
			
			String positionNode = nodeNode.getAttribute("pos");
			String orientNode = nodeNode.getAttribute("orient");
			String scaleNode = nodeNode.getAttribute("scale");
			
			{
				if (nameNode.equals("")) 		throw new RuntimeException("Node found with no `name` name specified.");
				if (meshNode.equals(""))		throw new RuntimeException("Node found with no `mesh` filename specified.");
				if (progNode.equals("")) 		throw new RuntimeException("Node found with no `prog` name specified.");
				if (positionNode.equals(""))	throw new RuntimeException("Node found with no `pos` name specified.");

				if (m_nodes.containsKey(nameNode)) 		throw new RuntimeException("The node named \"" + nameNode + "\" already exists.");
				if (!m_meshes.containsKey(meshNode)) 	throw new RuntimeException("The node named \"" + nameNode + "\" references the mesh \"" + meshNode + "\" which does not exist.");
				if (!m_programs.containsKey(progNode)) 	throw new RuntimeException("The node named \"" + nameNode + "\" references the program \"" + progNode + "\" which does not exist.");
			}
			
			Vec3 nodePos = attribToVec3(positionNode);
			
			SceneNode node = new SceneNode(m_meshes.get(meshNode), m_programs.get(progNode), 
					nodePos, readNodeTextures(nodeNode));
			m_nodes.put(nameNode, node);
			
			//TODO: parent/child nodes.
			if (parent == null) {
				m_rootNodes.add(node);
			}
			 
			if (!orientNode.equals("")) {
				node.setNodeOrient(attribToQuat(orientNode));
			}
			
			if (!scaleNode.equals("")) {
				if (attribIsVec3(scaleNode)) {
					node.setNodeScale(attribToVec3(scaleNode));
				}
				else {
					float unifScale = attribToFloat(scaleNode);
					node.setNodeScale(new Vec3(unifScale));
				}
			}
		}
		
		private ArrayList<TextureBinding> readNodeTextures(Element node) {
			ArrayList<TextureBinding> textureBindings = new ArrayList<>();
			Set<Integer> textureUnits = new HashSet<>();

			ArrayList<Element> textureElements = getElementsByTagName(node, "texture");
			
			for (Element textureElement : textureElements) {
				String nameNode = textureElement.getAttribute("name");
				String unitName = textureElement.getAttribute("unit");
				String samplerName = textureElement.getAttribute("sampler");

				{
					if (nameNode.equals("")) 	throw new RuntimeException("Textures on nodes must have a `name` attribute.");
					if (unitName.equals("")) 	throw new RuntimeException("Textures on nodes must have a `unit` attribute.");
					if (samplerName.equals("")) throw new RuntimeException("Textures on nodes must have a `sampler` attribute.");
			
					if (!m_textures.containsKey(nameNode)) throw new RuntimeException("The node texture named \"" + nameNode + "\" is a texture which does not exist.");
				}
				
				TextureBinding binding = new TextureBinding();
				binding.m_texture = m_textures.get(nameNode);
				binding.m_textureUnit = attribToInt(unitName);
				binding.m_sampler = getTypeFromName(samplerName);
		
				{
					if (textureUnits.contains(binding.m_textureUnit)) throw new RuntimeException("Multiply bound texture unit in node texture " + nameNode);	
				}
				
				textureBindings.add(binding);
				
				textureUnits.add(binding.m_textureUnit);
			}
			
			return textureBindings;
		}
		
			

		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
		private ArrayList<Element> getElementsByTagName(Element parent, String name) {
			ArrayList<Element> elements = new ArrayList<>();
			
			NodeList childs = parent.getChildNodes();
			
			for (int nodeIx = 0; nodeIx < childs.getLength(); nodeIx++) {
				Node node = childs.item(nodeIx);
								
				if (node.getNodeType() == Node.ELEMENT_NODE &&
						node.getNodeName().equals(name)) {
					elements.add((Element) node);
				}
			}
			
			return elements;
		}
		
		
		private int attribToInt(String attrib) {
			return Integer.parseInt(attrib);
		}
		
		private float attribToFloat(String attrib) {
			return Float.parseFloat(attrib);
		}
		
		private boolean attribIsVec3(String attrib) {
			try {
				attribToVec3(attrib);
				return true;
			} catch (RuntimeException e) {
				return false;
			}
		}
		
		private Vec3 attribToVec3(String attrib) {
			Scanner scanner = new Scanner(attrib);
			Vec3 vec = new Vec3();
			
			vec.x = Float.parseFloat(scanner.next());
			vec.y = Float.parseFloat(scanner.next());
			vec.z = Float.parseFloat(scanner.next());

			return vec;
		}
		
		private Quaternion attribToQuat(String attrib) {
			Scanner scanner = new Scanner(attrib);
			Quaternion quat = new Quaternion();
			
			quat.x = Float.parseFloat(scanner.next());
			quat.y = Float.parseFloat(scanner.next());
			quat.z = Float.parseFloat(scanner.next());
			quat.w = Float.parseFloat(scanner.next());

			return quat;
		}
	}
}