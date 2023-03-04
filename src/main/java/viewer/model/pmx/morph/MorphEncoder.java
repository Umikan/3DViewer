package viewer.model.pmx.morph;

import java.util.ArrayList;

import viewer.model.pmx.Morph;

public class MorphEncoder {
    private MorphContainer container;
    private ArrayList<Morph> morphs;
    private int vertexArraySize;
    private MorphBuffer buffer;
    public static class BufferInfo{
        public int vertexArraySize;
        public BufferInfo(int v1){
            vertexArraySize = v1;
        }
    }
    public MorphEncoder(MorphContainer container, MorphBuffer buffer, 
        ArrayList<Morph> morphs, BufferInfo info){
        this.container = container; 
        this.buffer = buffer;
        this.morphs = morphs;
        this.vertexArraySize = info.vertexArraySize;
    }
    public float[] getVertexMorphArray(Morph morph){
        float[] dest = new float[vertexArraySize];
        ArrayList<Object> vertexMorph = container.get(morph);
        for (Object obj : vertexMorph){
            Morph.Vertex v = (Morph.Vertex)obj;
            dest[v.index * 3] = v.offset[0];
            dest[v.index * 3 + 1] = v.offset[1];
            dest[v.index * 3 + 2] = v.offset[2];
        }
        return dest;
    }

    public void encode(){
        for (Morph morph : morphs){
            //Unsupport all except vertex morph
            if (morph.type != Morph.VERTEX) continue;
            buffer.addMorph(morph, getVertexMorphArray(morph));
        }
    }
}