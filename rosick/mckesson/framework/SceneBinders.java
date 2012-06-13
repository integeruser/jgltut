package rosick.mckesson.framework;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL33.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.BufferUtils;

import rosick.jglsdk.glm.Mat4;
import rosick.jglsdk.glm.Vec3;
import rosick.mckesson.framework.Scene.SceneNode;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class SceneBinders {

	public static void associateUniformWithNodes(ArrayList<SceneNode> nodes,
			UniformBinderBase binder, String unifName) {	
		for (SceneNode nodeRef : nodes) {
			binder.associateWithProgram(nodeRef.getProgram(), unifName);
		}
	};
	
	public static void setStateBinderWithNodes(ArrayList<SceneNode> nodes,
			UniformBinderBase binder) {
		for (SceneNode nodeRef : nodes) {
			nodeRef.setStateBinder(binder);
		}
	}
	
	
	public static abstract class StateBinder {
		abstract void bindState(int prog);
		abstract void unbindState(int prog);
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	static abstract class UniformBinderBase extends StateBinder {
		private Map<Integer, Integer> m_progUnifLoc;		
		
		public UniformBinderBase() {
			m_progUnifLoc = new HashMap<Integer, Integer>();
		}
		
		void associateWithProgram(int prog, String unifName) {
			m_progUnifLoc.put(prog, glGetUniformLocation(prog, unifName));
		}

		int getUniformLoc(int prog) {
			Object unif = m_progUnifLoc.get(prog);
			
			return unif == null ? -1 : (int) unif;
		}
	};
	
	public static class UniformIntBinder extends UniformBinderBase {
		private int m_val;

		
		@Override
		void bindState(int prog) {
			glUniform1i(getUniformLoc(prog), m_val);	
		}

		@Override
		void unbindState(int prog) {
		};
		
		
		public void setValue(int val) { 
			m_val = val; 
		}
	}

	
	public static class UniformVec3Binder extends UniformBinderBase {
		private Vec3 m_val = new Vec3();
		private FloatBuffer temp = BufferUtils.createFloatBuffer(3);
		
		
		@Override
		void bindState(int prog) {
			glUniform3(getUniformLoc(prog), m_val.fillAndFlipBuffer(temp));
		}

		@Override
		void unbindState(int prog) {
		};
		
		
		public void setValue(Vec3 val) { 
			m_val = new Vec3(val); 
		}
	}
	
	
	public static class UniformMat4Binder extends UniformBinderBase {
		private Mat4 m_val = new Mat4(1.0f);
		private FloatBuffer temp = BufferUtils.createFloatBuffer(16);
		
		
		@Override
		void bindState(int prog) {
			glUniformMatrix4(getUniformLoc(prog), false, m_val.fillAndFlipBuffer(temp));
		}

		@Override
		void unbindState(int prog) {
		};
		
		
		public void setValue(Mat4 val) { 
			m_val = new Mat4(val); 
		}
	}
	
	
	

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public class TextureBinder extends StateBinder {
		private int m_texUnit;
		private int m_texType;
		private int m_texObj;
		private int m_samplerObj;
	
		public TextureBinder() {
			m_texType = GL_TEXTURE_2D;		
		}
	
		
		@Override
		void bindState(int prog) {
			glActiveTexture(GL_TEXTURE0 + m_texUnit);
			glBindTexture(m_texType, m_texObj);
			glBindSampler(m_texUnit, m_samplerObj);			
		}

		@Override
		void unbindState(int prog) {
			glActiveTexture(GL_TEXTURE0 + m_texUnit);
			glBindTexture(m_texType, 0);
			glBindSampler(m_texUnit, 0);			
		}
		
		void setTexture(int texUnit, int texType, int texObj, int samplerObj) {
			m_texUnit = texUnit;
			m_texType = texType;
			m_texObj = texObj;
			m_samplerObj = samplerObj;
		}
	}


}