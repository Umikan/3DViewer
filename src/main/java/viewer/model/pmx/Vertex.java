package viewer.model.pmx;

import viewer.io.BinaryReader;
import viewer.model.pmx.skin.BDEF;

public class Vertex {
    private int count;
    private Header header;
    public float[] position, normal, uv;
    public float[] uv1, uv2, uv3, uv4;
    public Object[] deform;
    public float[] edgeMag;

    public Vertex(BinaryReader br, Header header){
        this.header = header;
        count = br.getInt();
        position = new float[count * 3];
        normal = new float[count * 3];
        uv = new float[count * 2];
        if (header.additionalUV >= 1) uv1 = new float[count * 4];
        if (header.additionalUV >= 2) uv2 = new float[count * 4];
        if (header.additionalUV >= 3) uv3 = new float[count * 4];
        if (header.additionalUV >= 4) uv4 = new float[count * 4];
        deform = new Object[count];
        edgeMag = new float[count];
        set(br);
    }
    private void set(BinaryReader br){
        ReadPos rp = new ReadPos();
        for (int i = 0; i < count; i++) {
            setOneVertex(br, rp);
        }
    }
    public void setOneVertex(BinaryReader br, ReadPos rp){
        for (int i = 0; i < 3; i++){
            position[rp.position++] = br.getFloat();
        }
        for (int i = 0; i < 3; i++){
            normal[rp.normal++] = br.getFloat();
        }       

        for (int i = 0; i < 2; i++){
            uv[rp.uv++] = br.getFloat();
        }
        float[][] uvs = {uv1, uv2, uv3, uv4};
        for (int c = 0; c < 4; c++){
            if (uvs[c] == null) continue;
            for (int i = 0; i < 4; i++){
                uvs[c][rp.uvs[c]++] = br.getFloat();
            }
        }
        setOneSkinnable(br, rp);
        edgeMag[rp.edgeMag++] = br.getFloat();
    }
    private void setOneSkinnable(BinaryReader br, ReadPos rp){
        Object deform = null;
        switch (br.getByte()){
            case 0: {//BDEF1 
                BDEF bdef = new BDEF();
                bdef.bone[0] = br.getIntFromByteBuffer(header.boneIndexSize);
                bdef.weights[0] = 1.0f;
                deform = bdef;
                break;
            }
            case 1: {//BDEF2
                BDEF bdef = new BDEF();
                bdef.bone[0] = br.getIntFromByteBuffer(header.boneIndexSize);
                bdef.bone[1] = br.getIntFromByteBuffer(header.boneIndexSize);
                bdef.weights[0] = br.getFloat();
                bdef.weights[1] =  1.0f - bdef.weights[0];
                deform = bdef;
                break;
            }
            case 2: {//BDEF4
                BDEF bdef = new BDEF();
                for (int i = 0; i < 4; i++){
                    bdef.bone[i] = br.getIntFromByteBuffer(header.boneIndexSize);
                }
                for (int i = 0; i < 4; i++){
                    bdef.weights[i] = br.getFloat();
                }
                deform = bdef;
                break;
            }
            case 3: {//SDEF
                //TODO: Support SDEF
                System.out.println("Unsupport SDEF");
                System.exit(0);
                /*SDEF sdef = new SDEF();
                sdef.bone1 = br.getIntFromByteBuffer(header.boneIndexSize);
                sdef.bone2 = br.getIntFromByteBuffer(header.boneIndexSize);
                sdef.weight = br.getFloat();
                for (int i = 0; i < 3; i++){
                    sdef.c[i] = br.getFloat();
                }
                for (int i = 0; i < 3; i++){
                    sdef.r0[i] = br.getFloat();
                }
                for (int i = 0; i < 3; i++){
                    sdef.r1[i] = br.getFloat();
                }  
                deform = sdef;
                break;*/
            }
        }
        this.deform[rp.skinnable++] = deform;
    }

    private static class ReadPos{
        private int position, normal, uv;
        private int[] uvs;
        private int skinnable;
        private int edgeMag;
    }

}


class SDEF{
    public int bone1, bone2;
    public float weight;  
    public float[] c = new float[3];
    public float[] r0 = new float[3];
    public float[] r1 = new float[3];
}