package viewer.model.vmd;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import viewer.io.BinaryReader;

public class Motion implements KeyFrame{
    public String boneName;
    public int frameNum;
    //local transform
    public Vector3f translation;
    public Quaternionf rotation;
    public Bezier[] interpolation = new Bezier[4];
    public Motion(BinaryReader br){
        boneName = VMD.readString(br, 15);
        frameNum = br.getInt();
        translation = br.getVector3f();
        rotation = br.getQuaternionf();   
        byte[] bezier = br.getBytes(16);
        for (int i = 0; i < 4; i++){
            interpolation[i] = new Bezier(bezier[i], bezier[4+i], bezier[8+i], bezier[12+i]);
        }
        br.getBytes(64-16);
    }

    public int frameNum(){ return frameNum; }
}