package viewer.model.vmd;

import viewer.io.BinaryReader;

public class Light {
    public int frameNum;
    public float[] color = new float[3];
    public float[] position = new float[3];
    public Light(BinaryReader br) throws VMDImportException{
        frameNum = br.getInt();
        for (int i = 0; i < 3; i++) color[i] = br.getFloat();
        for (int i = 0; i < 3; i++) position[i] = br.getFloat();   
        throw new VMDImportException("Unsupport light");
    }
}