package viewer.io;

import java.nio.*;
import java.nio.file.*;

import org.joml.*;

public class BinaryReader{
    private byte[] bin;
    private int pos = 0;
    public int position(){ return pos; }
    public void position(int pos){ this.pos = pos; }
    public BinaryReader(String file){
        try {
            Path path = Paths.get(file);
            bin = Files.readAllBytes(path);
        } catch (Exception e){
            System.out.println("Can't read all bytes from a file: " + file);
            System.exit(0);
        }
    }
    public byte[] getBytes(int length){
        byte[] dest = new byte[length];
        for (int i = 0; i < dest.length; i++){
            dest[i] = bin[i + pos];
        }
        pos += length;
        return dest;
    }
    public byte getByte(){ return getBytes(1)[0]; }
    private ByteOrder order;
    public void setByteOrder(ByteOrder order){
        this.order = order;
    }
    public ByteBuffer getByteBuffer(int length){
        byte[] bytes = getBytes(length);
        return ByteBuffer.wrap(bytes).order(order);
    }
    public int getIntFromByteBuffer(byte size){
        ByteBuffer buffer = getByteBuffer(size);
        if (size == 1) return buffer.get();
        if (size == 2) return buffer.getShort();
        if (size == 4) return buffer.getInt();
        return -1;
    }
    public float getFloat(){
        return getByteBuffer(4).getFloat();
    }
    public int getInt(){
        return getByteBuffer(4).getInt();
    }
    public short getShort(){
        return getByteBuffer(2).getShort();
    }
    public Vector3f getVector3f(){
        return new Vector3f(getFloat(), getFloat(), getFloat());
    }
    public Quaternionf getQuaternionf(){
        return new Quaternionf(getFloat(), getFloat(), getFloat(), getFloat());
    }
}