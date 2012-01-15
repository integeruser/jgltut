package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.BufferUtils;


public class IOUtils {

	/**
	 * Create and fill a ShortBuffer given a short array.
	 * @param shortarray
	 * @return the created ShortBuffer
	 */
	public static ShortBuffer allocShorts(short[] shortarray) {
		ShortBuffer sb = BufferUtils.createShortBuffer(shortarray.length);
		sb.put(shortarray);
		sb.flip();
        
    	return sb;
    }

	/**
	 * Create and fill a FloatBuffer given a float array.
	 * @param floatarray
	 * @return the created FloatBuffer
	 */
	public static FloatBuffer allocFloats(float[] floatarray) {
		FloatBuffer fb = BufferUtils.createFloatBuffer(floatarray.length);
		fb.put(floatarray);
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
        	BufferedReader reader = new BufferedReader(new FileReader(filepath));
        	String line;
        	
        	while ((line = reader.readLine()) != null) {
        		text.append(line).append("\n");
        	}
        	
        	reader.close();
        } catch (Exception e){
        	System.out.println("Fail reading " + filepath);
        }
        
        return text.toString();
	}
}
