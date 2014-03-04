package fcagnin.gltut.framework;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL33.*;

import org.lwjgl.BufferUtils;

import fcagnin.jglsdk.glm.Mat4;
import fcagnin.jglsdk.glm.Vec3;
import fcagnin.gltut.framework.Scene.SceneNode;


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
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	static abstract class StateBinder {
		abstract void bindState(int prog);
		abstract void unbindState(int prog);
	}
	
	static abstract class UniformBinderBase extends StateBinder {

		UniformBinderBase() {
			progUnifLoc = new HashMap<Integer, Integer>();
		}
		
		void associateWithProgram(int prog, String unifName) {
			progUnifLoc.put(prog, glGetUniformLocation(prog, unifName));
		}

		int getUniformLoc(int prog) {
			Object unif = progUnifLoc.get(prog);
			
			return unif == null ? -1 : (int) unif;
		}
	
		

		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
		
		private Map<Integer, Integer> progUnifLoc;		
	};
	
	
	public static class UniformIntBinder extends UniformBinderBase {

		public void setValue(int val) { 
			this.val = val; 
		}
		
		
		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
		
		@Override
		void bindState(int prog) {
			glUniform1i(getUniformLoc(prog), val);	
		}

		@Override
		void unbindState(int prog) {
		};
		
		
		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	
		private int val;
	}

	
	public static class UniformVec3Binder extends UniformBinderBase {

		public void setValue(Vec3 val) { 
			this.val = new Vec3(val); 
		}
		
		
		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
		
		@Override
		void bindState(int prog) {
			glUniform3(getUniformLoc(prog), val.fillAndFlipBuffer(vec3Buffer));
		}

		@Override
		void unbindState(int prog) {
		};
		
		
		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	
		private Vec3 val = new Vec3();
		private FloatBuffer vec3Buffer = BufferUtils.createFloatBuffer(Vec3.SIZE);
	}
	
	
	public static class UniformMat4Binder extends UniformBinderBase {		

		public void setValue(Mat4 val) { 
			this.val = new Mat4(val); 
		}
		
		
		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
		
		@Override
		void bindState(int prog) {
			glUniformMatrix4(getUniformLoc(prog), false, val.fillAndFlipBuffer(mat4Buffer));
		}

		@Override
		void unbindState(int prog) {
		};
		
	
		
		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
		private Mat4 val = new Mat4(1.0f);
		private FloatBuffer mat4Buffer = BufferUtils.createFloatBuffer(Mat4.SIZE);
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public class TextureBinder extends StateBinder {
	
		public TextureBinder() {
			texType = GL_TEXTURE_2D;		
		}
	
		
		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
		
		@Override
		void bindState(int prog) {
			glActiveTexture(GL_TEXTURE0 + texUnit);
			glBindTexture(texType, texObj);
			glBindSampler(texUnit, samplerObj);			
		}

		@Override
		void unbindState(int prog) {
			glActiveTexture(GL_TEXTURE0 + texUnit);
			glBindTexture(texType, 0);
			glBindSampler(texUnit, 0);			
		}
		
		void setTexture(int texUnit, int texType, int texObj, int samplerObj) {
			this.texUnit = texUnit;
			this.texType = texType;
			this.texObj = texObj;
			this.samplerObj = samplerObj;
		}
	
	
		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
		private int texUnit;
		private int texType;
		private int texObj;
		private int samplerObj;
	}
}