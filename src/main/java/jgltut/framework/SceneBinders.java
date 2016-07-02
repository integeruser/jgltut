package jgltut.framework;

import jgltut.framework.Scene.SceneNode;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;


/**
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 *
 * @author integeruser
 */
public class SceneBinders {
    public static void associateUniformWithNodes(ArrayList<SceneNode> nodes, UniformBinderBase binder, String unifName) {
        for (SceneNode nodeRef : nodes) {
            binder.associateWithProgram(nodeRef.getProgram(), unifName);
        }
    }

    public static void setStateBinderWithNodes(ArrayList<SceneNode> nodes, UniformBinderBase binder) {
        for (SceneNode nodeRef : nodes) {
            nodeRef.setStateBinder(binder);
        }
    }

    ////////////////////////////////
    static abstract class StateBinder {
        abstract void bindState(int prog);

        abstract void unbindState(int prog);
    }


    static abstract class UniformBinderBase extends StateBinder {
        UniformBinderBase() {
            progUnifLoc = new HashMap<>();
        }

        void associateWithProgram(int prog, String unifName) {
            progUnifLoc.put(prog, glGetUniformLocation(prog, unifName));
        }

        int getUniformLoc(int prog) {
            Object unif = progUnifLoc.get(prog);
            return unif == null ? -1 : (int) unif;
        }

        ////////////////////////////////
        private Map<Integer, Integer> progUnifLoc;
    }


    public static class UniformIntBinder extends UniformBinderBase {
        public void setValue(int val) {
            this.val = val;
        }

        ////////////////////////////////
        @Override
        void bindState(int prog) {
            glUniform1i(getUniformLoc(prog), val);
        }

        @Override
        void unbindState(int prog) {
        }

        ////////////////////////////////
        private int val;
    }

    public static class UniformVec3Binder extends UniformBinderBase {
        public void setValue(Vector3f val) {
            this.val = new Vector3f(val);
        }

        ////////////////////////////////
        @Override
        void bindState(int prog) {
            glUniform3fv(getUniformLoc(prog), val.get(vec3Buffer));
        }

        @Override
        void unbindState(int prog) {
        }

        ////////////////////////////////
        private Vector3f val = new Vector3f();
        private FloatBuffer vec3Buffer = BufferUtils.createFloatBuffer(3);
    }

    public static class UniformMat4Binder extends UniformBinderBase {
        public void setValue(Matrix4f val) {
            this.val = new Matrix4f(val);
        }

        ////////////////////////////////
        @Override
        void bindState(int prog) {
            glUniformMatrix4fv(getUniformLoc(prog), false, val.get(mat4Buffer));
        }

        @Override
        void unbindState(int prog) {
        }

        ////////////////////////////////
        private Matrix4f val = new Matrix4f();
        private FloatBuffer mat4Buffer = BufferUtils.createFloatBuffer(16);
    }
}