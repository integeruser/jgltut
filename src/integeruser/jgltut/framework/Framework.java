package integeruser.jgltut.framework;

import integeruser.jglsdk.glutil.Shader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL20.glDeleteShader;


/**
 * Visit https://github.com/integeruser/jgltut for info and updates.
 * Original: https://bitbucket.org/alfonse/gltut/src/default/framework/framework.cpp
 */
public class Framework {
    public static String COMMON_DATAPATH = "/integeruser/jgltut/data/";
    public static String CURRENT_TUTORIAL_DATAPATH = null;


    public static String findFileOrThrow(String fileName) {
        // search the file in '/jgltut/tut##/data/'
        InputStream fileStream = Framework.class.getResourceAsStream(CURRENT_TUTORIAL_DATAPATH + fileName);
        if (fileStream != null) return CURRENT_TUTORIAL_DATAPATH + fileName;

        // search the file in '/jgltut/data/'
        fileStream = Framework.class.getResourceAsStream(COMMON_DATAPATH + fileName);
        if (fileStream != null) return COMMON_DATAPATH + fileName;

        throw new RuntimeException("Could not find the file " + fileName);
    }

    ////////////////////////////////
    public static int loadShader(int shaderType, String shaderFilename) {
        String filePath = Framework.findFileOrThrow(shaderFilename);
        String shaderCode = loadShaderFile(filePath);
        return Shader.compileShader(shaderType, shaderCode);
    }

    private static String loadShaderFile(String shaderFilePath) {
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(Framework.class.getResourceAsStream(shaderFilePath)));

            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line).append("\n");
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return text.toString();
    }


    public static int createProgram(ArrayList<Integer> shaders) {
        try {
            int prog = Shader.linkProgram(shaders);
            return prog;
        } finally {
            for (Integer shader : shaders) {
                glDeleteShader(shader);
            }
        }
    }
}
