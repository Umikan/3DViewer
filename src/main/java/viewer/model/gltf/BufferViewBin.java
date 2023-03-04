package viewer.model.gltf;

public class BufferViewBin{
    public Byte[] binData;
    public int byteStride;
    public int target;

    public BufferViewBin(Byte[] binData, GLTF.BufferView bv){
        this.binData = binData;
        this.byteStride = bv.byteStride;
        this.target = bv.target;
    }
}
