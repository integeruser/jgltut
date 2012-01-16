package rosick.common;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.BufferUtils;


public class IOUtils {

	/**
	 * Create and fill a ShortBuffer given a short array.
	 * @param shorts
	 * @return the created ShortBuffer
	 */
	public static ShortBuffer allocShorts(short[] shorts) {
		ShortBuffer sb = BufferUtils.createShortBuffer(shorts.length);
		sb.put(shorts);
		sb.flip();
        
    	return sb;
    }

	/**
	 * Create and fill a FloatBuffer given a float array.
	 * @param floats
	 * @return the created FloatBuffer
	 */
	public static FloatBuffer allocFloats(float[] floats) {
		FloatBuffer fb = BufferUtils.createFloatBuffer(floats.length);
		fb.put(floats);
		fb.flip();
        
    	return fb;
    }
  	
    
    /**
     * Load a file and return its content as a string.
     * @param filepath the filepath of the file to load
     * @return the content of the file
     */
	public static String loadFileAsString(String filepath) {
        StringBuilder text = new StringBuilder();
        
        try {
        	BufferedReader reader = new BufferedReader(new InputStreamReader(ClassLoader.class.getResourceAsStream(filepath)));
        	String line;
        	
        	while ((line = reader.readLine()) != null) {
        		text.append(line).append("\n");
        	}
        	
        	reader.close();
        } catch (Exception e){
        	System.err.println("Fail reading " + filepath + ": " + e.getMessage());
        }
        
        return text.toString();
	}
}
