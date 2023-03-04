package viewer.model.pmx.morph;

import java.nio.Buffer;
import java.nio.IntBuffer;
import java.util.HashMap;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;

import viewer.model.pmx.Morph;
import viewer.model.vmd.MorphController;

import static com.jogamp.opengl.GL4.*;

public class MorphBuffer {
    private HashMap<Morph, Object> morphs = new HashMap<Morph, Object>();
    private HashMap<Morph, IntBuffer> bufferName = new HashMap<Morph, IntBuffer>();
    public MorphController controller;

    public void addMorph(Morph morph, Object array){
        morphs.put(morph, array);
    }
    private int getBufferName(Morph morph){
        return bufferName.get(morph).get(0);
    }

    public void init(GL4 gl){
        for (Morph morph : morphs.keySet()){
            switch(morph.type){
                case Morph.VERTEX: {
                    IntBuffer buf = IntBuffer.allocate(1);
                    gl.glGenBuffers(1, buf);
                    Buffer buffer = Buffers.newDirectFloatBuffer((float[])morphs.get(morph));
                    int byteLength = buffer.capacity() * Buffers.SIZEOF_FLOAT;
                    gl.glBindBuffer(GL_ARRAY_BUFFER, buf.get(0));
                    gl.glBufferData(GL_ARRAY_BUFFER, byteLength, buffer, GL_STATIC_DRAW);
                    bufferName.put(morph, buf);
                    break;
                }
            }
        }
    }
    public void setMorphAnimation(float time){
        for (Morph morph : morphs.keySet()){
            if (morph.type == Morph.VERTEX){
                Float w = controller.getWeight(morph.name, time);
                System.out.println(w);
                if (w == null) continue;
                morph.weight = w.floatValue();
            }
        }
    }
    public int enableVertexMorphCount;
    public void enable(GL4 gl){
        enableVertexMorphCount = 0;
        for (Morph morph : morphs.keySet()){
            if (morph.type == Morph.VERTEX){
                if (morph.weight == 0.0f) continue;
                gl.glBindBuffer(GL_ARRAY_BUFFER, getBufferName(morph));
                final int location = 10 + enableVertexMorphCount;
                gl.glEnableVertexAttribArray(location);
                gl.glVertexAttribPointer(location, 3, GL_FLOAT, false, 0, 0);
                gl.glUniform1f(101 + enableVertexMorphCount, morph.weight);
                enableVertexMorphCount++;
            }
        }
        gl.glUniform1i(100, enableVertexMorphCount); 
    }
    public void disable(GL4 gl){
        for (int i = 0; i < enableVertexMorphCount; i++){
            gl.glDisableVertexAttribArray(10 + i);
        }
    }
}