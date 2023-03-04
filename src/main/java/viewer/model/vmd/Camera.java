package viewer.model.vmd;

import org.joml.Vector3f;

import viewer.io.BinaryReader;

public class Camera{
    public int frameNum;
    public float distance;
    public Vector3f position;
    public Vector3f rotation;
    public byte[] interpolation = new byte[24];
    public int viewingAngle;
    public boolean perspective;

    public Camera(BinaryReader br) throws VMDImportException{
        frameNum = br.getInt();
        distance = br.getFloat();
        position = br.getVector3f();
        rotation = br.getVector3f();
        interpolation = br.getBytes(24);
        viewingAngle = br.getInt();
        throw new VMDImportException("Unsupport camera");
    }
}