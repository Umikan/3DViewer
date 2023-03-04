package viewer.shader;

import java.nio.*;
import com.jogamp.common.nio.Buffers;

//NOTE: static buffer doesn't allocate native memory per a call. This allows using less memory.
public class Memory {
    public final static FloatBuffer mat4;
    public final static FloatBuffer vec3;
    public final static FloatBuffer vec4;
    static {
        mat4 = Buffers.newDirectFloatBuffer(16);
        vec3 = Buffers.newDirectFloatBuffer(3);
        vec4 = Buffers.newDirectFloatBuffer(4);
    }
    public static void putVec4(float[] src){
        vec4.position(0);
        vec4.put(src);
    }
}