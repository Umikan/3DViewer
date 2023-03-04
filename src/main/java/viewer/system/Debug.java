package viewer.system;

import java.nio.Buffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;

import static com.jogamp.opengl.GL4.*;

import org.joml.Vector3f;

public class Debug{
    public static GL4 gl;
    public static ArrayList<float[]> lines = new ArrayList<float[]>();
    public static void draw(){
        for (float[] line : lines){
            IntBuffer nameBuffer = IntBuffer.allocate(1);
            gl.glGenBuffers(1, nameBuffer);
            Buffer buffer = Buffers.newDirectFloatBuffer(line);
            int byteLength = buffer.capacity() * Buffers.SIZEOF_FLOAT;
            gl.glBindBuffer(GL_ARRAY_BUFFER, nameBuffer.get(0));
            gl.glBufferData(GL_ARRAY_BUFFER, byteLength, buffer, GL_DYNAMIC_DRAW);
    
            gl.glBindBuffer(GL_ARRAY_BUFFER, nameBuffer.get(0));   
            gl.glEnableVertexAttribArray(0);
            gl.glUniform4f(1, 0.0f, 0.0f, 0.0f, 1.0f);
            gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
            gl.glDrawArrays(GL_LINES, 0, 2);
            gl.glDisableVertexAttribArray(0);
            gl.glDeleteBuffers(1, nameBuffer);
        }
        lines.clear();
    }
    public static void line(Vector3f v1, Vector3f v2){
            float[] f = new float[6];
            f[0] = v1.x;
            f[1] = v1.y;
            f[2] = v1.z;
            f[3] = v2.x;
            f[4] = v2.y;
            f[5] = v2.z;
            lines.add(f);
    }
}