package viewer.model.gltf;

public class AccessorBin{
    public Byte[] binData;
    public int componentType;
    public int componentTypeSize;
    public int componentNumber;
    public int byteStride;
    public int target;
    public int count;

    public AccessorBin(Byte[] binData, BufferViewBin bv, GLTF.Accessor ac){
        this.binData = binData;
        componentType = ac.componentType;
        componentTypeSize = GLTFBuffer.componentTypeSize(ac);
        componentNumber = GLTFBuffer.componentNumber(ac);
        byteStride = bv.byteStride;
        target = bv.target;
        count = ac.count;
    }
}