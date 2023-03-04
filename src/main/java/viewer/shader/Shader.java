package viewer.shader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.IntBuffer;
import java.nio.ByteBuffer;

import com.jogamp.opengl.GL4;
import static com.jogamp.opengl.GL4.*;


public class Shader {
    private static String[] readShader(String path){
        URL resource = Shader.class.getResource(path);
        String tmp = "";
        try (InputStream is = resource.openConnection().getInputStream(); 
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))){
            String line;
            while ((line=br.readLine()) != null) {
                tmp += line + "\n";
            }
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Can't load a shader.");
            System.exit(0);
        }
        return new String[] { tmp };
    }
    public static int compileShader(GL4 gl, int type, String path){
        int shaderID = gl.glCreateShader(type);
        String[] src = readShader(path);
        gl.glShaderSource(shaderID, src.length, src, (int[])null, 0);
        gl.glCompileShader(shaderID);

        final IntBuffer intBuffer = IntBuffer.allocate(1);
        gl.glGetShaderiv(shaderID, GL_COMPILE_STATUS, intBuffer);
        if (intBuffer.get() == GL_FALSE) {
            intBuffer.flip();
            gl.glGetShaderiv(shaderID, GL_INFO_LOG_LENGTH, intBuffer);
            final int logLength = intBuffer.get();
            final ByteBuffer byteBuffer = ByteBuffer.allocate(logLength);
            gl.glGetShaderInfoLog(shaderID, logLength, null, byteBuffer);
            System.out.println("Failed to compile " +
                (type == GL_VERTEX_SHADER ? "vertex shader" : "fragment shader") 
                    + "(" + path + ")" + " :");
            System.out.println(new String(byteBuffer.array()));
         }
        return shaderID;
    }
    public static void printShaderStatus(GL4 gl, int shaderprogram){
        IntBuffer intBuffer = IntBuffer.allocate(1);
        gl.glGetShaderiv(shaderprogram,GL_COMPILE_STATUS,intBuffer);
        if(intBuffer.get(0) == GL_FALSE){
            gl.glGetProgramiv(shaderprogram, GL_INFO_LOG_LENGTH, intBuffer);
           
            int size = intBuffer.get(0);
            if (size > 0) {
                ByteBuffer byteBuffer = ByteBuffer.allocate(size);
                gl.glGetProgramInfoLog(shaderprogram, size, intBuffer, byteBuffer);
                for (byte b : byteBuffer.array()) {
                    System.err.print((char) b);
                }
            } else {
                System.out.println("Unknown");
            }
        }
    }
    public static int loadShaderProgram(GL4 gl, String vShader, String fShader){
        int vShaderID = compileShader(gl, GL_VERTEX_SHADER, vShader);
        int fShaderID = compileShader(gl, GL_FRAGMENT_SHADER, fShader);
        int programID = gl.glCreateProgram();
        gl.glAttachShader(programID, vShaderID);
        gl.glAttachShader(programID, fShaderID);
        gl.glLinkProgram(programID);
        gl.glValidateProgram(programID);
        printShaderStatus(gl, programID);
        return programID;
    }
}
