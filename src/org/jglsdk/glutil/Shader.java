package org.jglsdk.glutil;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;


/**
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 *
 * @author integeruser
 */
public class Shader {
    private static class CompileLinkShaderException extends RuntimeException {
        private static final long serialVersionUID = 5490603440382398244L;

        CompileLinkShaderException(int shader) {
            super(glGetShaderInfoLog(
                    shader,
                    glGetShaderi(shader, GL_INFO_LOG_LENGTH)));
        }
    }

    private static class CompileLinkProgramException extends RuntimeException {
        private static final long serialVersionUID = 7321217286524434327L;

        CompileLinkProgramException(int program) {
            super(glGetShaderInfoLog(
                    program,
                    glGetShaderi(program, GL_INFO_LOG_LENGTH)));
        }
    }


    public static int compileShader(int shaderType, String shaderCode) {
        int shader = glCreateShader(shaderType);

        glShaderSource(shader, shaderCode);
        glCompileShader(shader);

        int status = glGetShaderi(shader, GL_COMPILE_STATUS);
        if (status == GL_FALSE) {
            glDeleteShader(shader);
            throw new CompileLinkShaderException(shader);
        }

        return shader;
    }


    public static int linkProgram(ArrayList<Integer> shaders) {
        int program = glCreateProgram();
        return linkProgram(program, shaders);
    }

    private static int linkProgram(int program, ArrayList<Integer> shaders) {
        for (Integer shader : shaders) {
            glAttachShader(program, shader);
        }

        glLinkProgram(program);

        int status = glGetProgrami(program, GL_LINK_STATUS);
        if (status == GL_FALSE) {
            glDeleteProgram(program);
            throw new CompileLinkProgramException(program);
        }

        for (Integer shader : shaders) {
            glDetachShader(program, shader);
        }

        return program;
    }
}
