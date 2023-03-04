package viewer.model.vmd;

import viewer.io.BinaryReader;

public class SelfShadow{
    public int frameNum;
    public byte mode;
    public float distance;
    public SelfShadow(BinaryReader br){
        frameNum = br.getInt();
        mode = br.getByte();
        distance = br.getFloat();
    }
}