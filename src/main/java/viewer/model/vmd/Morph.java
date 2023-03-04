package viewer.model.vmd;

import viewer.io.BinaryReader;

public class Morph implements KeyFrame{
    public String morphName;
    public int frameNum;
    public float weight;
    public Morph(BinaryReader br){
        morphName = VMD.readString(br, 15);
        frameNum = br.getInt();
        weight = br.getFloat();
    }
    public int frameNum(){ return frameNum; }
}