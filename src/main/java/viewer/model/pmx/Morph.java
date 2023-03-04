package viewer.model.pmx;

import java.util.ArrayList;

import viewer.io.BinaryReader;
import viewer.model.pmx.morph.MorphContainer;

public class Morph{
    public float weight = 0.0f;

    public String name;
    public String name_en;
    public byte panel;
    public byte type;
    public int count;
    public static final byte GROUP = 0;
    public static final byte VERTEX = 1;
    public static final byte BONE = 2;
    public static final byte UV0 = 3;
    public static final byte UV4 = 7;
    public static final byte MATERIAL = 8;

    public Morph(BinaryReader br, Header header, MorphContainer mc) throws PMXImportException{
        name = TextBuf.readString(br, header.getEncode());
        name_en = TextBuf.readString(br, header.getEncode());
        panel = br.getByte();
        type = br.getByte();
        MorphFactory factory = null;
        if (type == GROUP) factory = new GroupFactory();
        else if (type == VERTEX) factory = new VertexFactory(); 
        else if (type == BONE) factory = new BoneFactory();
        else if (type >= UV0 && type <= UV4) factory = new UVFactory();
        else if (type == MATERIAL) factory = new MaterialFactory();
        else throw new PMXImportException("Invalid Morph Type.");
        int count = br.getInt();
        ArrayList<Object> morph = new ArrayList<Object>();
        for (int i = 0; i < count; i++){
            morph.add(factory.create(br, header));
        }
        mc.put(this, morph);
    }
    
    public static class Vertex{
        public int index;
        public float[] offset = new float[3];
        public Vertex(BinaryReader br, Header header){
            index = br.getIntFromByteBuffer(header.vertexIndexSize);
            for (int i = 0; i < 3; i++){
                offset[i] = br.getFloat();
            }
        }
    }
    public static class Bone{
        public int index;
        public float[] translation = new float[3];
        public float[] quaternion = new float[4]; 
        public Bone(BinaryReader br, Header header){
            index = br.getIntFromByteBuffer(header.boneIndexSize);  
            for (int i = 0; i < 3; i++){
                translation[i] = br.getFloat();
            }
            for (int i = 0; i < 4; i++){
                quaternion[i] = br.getFloat();
            }
        }
    }
    public static class UV{
        public int index;
        public float[] offset = new float[4];
        public UV(BinaryReader br, Header header){
            index = br.getIntFromByteBuffer(header.vertexIndexSize);
            for (int i = 0; i < 4; i++){
                offset[i] = br.getFloat();
            }
        }
    }
    //TODO: support material morph
    public static class Material{
        public int index;
        public byte calculation;
        public float[] diffuse = new float[4];
        public float[] specular = new float[3];
        public float specular_k;
        public float[] ambient = new float[3];
        public float[] edgeColor = new float[4];
        public float edgeSize;
        public float[] texture_k = new float[4];
        public float[] sphereTexture_k = new float[4];
        public float[] toonTexture_k = new float[4]; 
        public Material(BinaryReader br, Header header){
            index = br.getIntFromByteBuffer(header.materialIndexSize);
            calculation = br.getByte();
            for (int i = 0; i < 4; i++) diffuse[i] = br.getFloat();
            for (int i = 0; i < 3; i++) specular[i] = br.getFloat();
            specular_k = br.getFloat();
            for (int i = 0; i < 3; i++) ambient[i] = br.getFloat();
            for (int i = 0; i < 4; i++) edgeColor[i] = br.getFloat();
            edgeSize = br.getFloat();
            for (int i = 0; i < 4; i++) texture_k[i] = br.getFloat();
            for (int i = 0; i < 4; i++) sphereTexture_k[i] = br.getFloat();
            for (int i = 0; i < 4; i++) toonTexture_k[i] = br.getFloat();
        }
    }
    public static class Group{
        public int index;
        public float rate;
        public Group(BinaryReader br, Header header){
            index = br.getIntFromByteBuffer(header.morphIndexSize);  
            rate = br.getFloat();
        }
    }
}

interface MorphFactory{
    public Object create(BinaryReader br, Header header);
}

class VertexFactory implements MorphFactory{
    public Object create(BinaryReader br, Header header){
        return new Morph.Vertex(br, header);
    }
}

class UVFactory implements MorphFactory{
    public Object create(BinaryReader br, Header header){
        return new Morph.UV(br, header);
    }
}

class BoneFactory implements MorphFactory{
    public Object create(BinaryReader br, Header header){
        return new Morph.Bone(br, header);
    }
}

class MaterialFactory implements MorphFactory{
    public Object create(BinaryReader br, Header header){
        return new Morph.Material(br, header);
    }
}

class GroupFactory implements MorphFactory{
    public Object create(BinaryReader br, Header header){
        return new Morph.Group(br, header);
    }
}