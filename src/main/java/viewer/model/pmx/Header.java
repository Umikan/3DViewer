package viewer.model.pmx;

import java.lang.reflect.Field;
import java.util.Arrays;

import viewer.io.BinaryReader;

public class Header {
    public float version;
    public Encode encode;
    public static enum Encode{UTF16, UTF8};
    public byte additionalUV;
    public byte vertexIndexSize;
    public byte textureIndexSize;
    public byte materialIndexSize;
    public byte boneIndexSize;
    public byte morphIndexSize;
    public byte rigidbodyIndexSize;

    public String getEncode(){
        switch (encode){
            case UTF16: return "UTF-16LE";
            case UTF8: return "UTF-8";
            default: return null;
        }
    }
    public void assignIndexSize(byte[] indexSizes){
        vertexIndexSize = indexSizes[0];
        textureIndexSize = indexSizes[1];
        materialIndexSize = indexSizes[2];
        boneIndexSize = indexSizes[3];
        morphIndexSize = indexSizes[4];
        rigidbodyIndexSize = indexSizes[5];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Class: " + this.getClass().getCanonicalName() + "\n");
        sb.append("Settings:\n");
        for (Field field : this.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                sb.append(field.getName() + " = " + field.get(this) + "\n");
            } catch (IllegalAccessException e) {
                sb.append(field.getName() + " = " + "access denied\n");
            }
        }
        return sb.toString();
    }

    public Header(BinaryReader br) throws PMXImportException{
        byte[] format = br.getBytes(4);
        byte[] correctFormat = {0x50,0x4d,0x58,0x20}; //PMX2.0
        if (!Arrays.equals(format, correctFormat)) 
            throw new PMXImportException("This is not PMX 2.x.");

        version = br.getFloat();

        byte byteSize = br.getByte();
        if (byteSize != 8){
            throw new PMXImportException("Unsupport latest PMX version.");
        }
        switch (br.getByte()){
            case 0: encode = Header.Encode.UTF16; break;
            case 1: encode = Header.Encode.UTF8; break;
            default: throw new PMXImportException("Invalid Encoding Format.");
        }
        
        byte additionalUV = br.getByte();
        if (additionalUV < 0 || additionalUV > 4) {
            throw new PMXImportException("Invalid Additional UV Count.");
        }
        this.additionalUV = additionalUV; 
        
        byte[] indexSizes = new byte[6];
        for (int i = 0; i < indexSizes.length; i++){
            byte indexSize = br.getByte();
            if (indexSize != 1 && indexSize != 2 && indexSize != 4) {
                throw new PMXImportException("Invalid Index Size.");
            }
            indexSizes[i] = indexSize;
        }
        assignIndexSize(indexSizes);
    }
}