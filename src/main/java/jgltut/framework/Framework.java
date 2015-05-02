package jgltut.framework;

import jglsdk.glutil.Shader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL20.glDeleteShader;


/**
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 *
 * @author integeruser
 */
public class Framework {
    public static String COMMON_DATAPATH = "/jgltut/data/";
    public static String CURRENT_TUTORIAL_DATAPATH = null;


    public static String findFileOrThrow(String fileName) {
        // search the file in '/jgltut/tut##/data/'
        InputStream fileStream = ClassLoader.class.getResourceAsStream(CURRENT_TUTORIAL_DATAPATH + fileName);
        if (fileStream != null) return CURRENT_TUTORIAL_DATAPATH + fileName;

        // search the file in '/jgltut/data/'
        fileStream = ClassLoader.class.getResourceAsStream(COMMON_DATAPATH + fileName);
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
            BufferedReader reader = new BufferedReader(new InputStreamReader(ClassLoader.class.getResourceAsStream(shaderFilePath)));

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

    ////////////////////////////////
    public static float degToRad(float angDeg) {
        final float degToRad = 3.14159f * 2.0f / 360.0f;
        return angDeg * degToRad;
    }
}
