package fcagnin.jgltut.framework;

import fcagnin.jglsdk.glutil.Shader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL20.glDeleteShader;


/**
 * Visit https://github.com/integeruser/gltut-lwjgl for project info, updates and license terms. info, updates and license terms.
 *
 * @author integeruser
 */
public class Framework {

    public static String COMMON_DATAPATH = "/fcagnin/jgltut/data/";
    public static String CURRENT_TUTORIAL_DATAPATH = null;


    public static String findFileOrThrow(String filename) {
        InputStream fileStream = ClassLoader.class.getResourceAsStream( CURRENT_TUTORIAL_DATAPATH + filename );
        if ( fileStream != null ) {
            return CURRENT_TUTORIAL_DATAPATH + filename;
        }

        fileStream = ClassLoader.class.getResourceAsStream( COMMON_DATAPATH + filename );
        if ( fileStream != null ) {
            return COMMON_DATAPATH + filename;
        }

        throw new RuntimeException( "Could not find the file " + filename );
    }



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    public static int loadShader(int shaderType, String shaderFilename) {
        String filepath = Framework.findFileOrThrow( shaderFilename );
        String shaderCode = loadShaderFile( filepath );

        return Shader.compileShader( shaderType, shaderCode );
    }


    public static int createProgram(ArrayList<Integer> shaders) {
        try {
            int prog = Shader.linkProgram( shaders );
            return prog;
        } finally {
            for ( Integer shader : shaders ) {
                glDeleteShader( shader );
            }
        }
    }



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    public static float degToRad(float angDeg) {
        final float degToRad = 3.14159f * 2.0f / 360.0f;

        return angDeg * degToRad;
    }



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    private static String loadShaderFile(String shaderFilepath) {
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader reader = new BufferedReader( new InputStreamReader( ClassLoader.class.getResourceAsStream( shaderFilepath ) ) );
            String line;

            while ( (line = reader.readLine()) != null ) {
                text.append( line ).append( "\n" );
            }

            reader.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        return text.toString();
    }
}
