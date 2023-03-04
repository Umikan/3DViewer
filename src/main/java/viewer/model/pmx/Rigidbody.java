package viewer.model.pmx;

import org.joml.Vector3f;

import viewer.io.BinaryReader;

public class Rigidbody {
    public String name;
    public String name_en;
    public int bone;
    public byte group;
    public short offCollisionGroupFlag;

    public byte shape;
    public static final byte SPHERE = 0;
    public static final byte BOX = 1;
    public static final byte CAPSULE = 1;

    public Vector3f size;
    public Vector3f position;
    public Vector3f rotation;
    
    public float mass;
    public float moveDecay, rotDecay;
    public float repulsion;
    public float friction;

    public byte physics;
    public static final byte STATIC = 0;
    public static final byte DYNAMIC = 1;
    public static final byte DYNAMIC2 = 2;

    public Rigidbody(BinaryReader br, Header header){
        name = TextBuf.readString(br, header.getEncode());
        name_en = TextBuf.readString(br, header.getEncode());
        bone = br.getIntFromByteBuffer(header.boneIndexSize);
        group = br.getByte();
        offCollisionGroupFlag = br.getShort();
        shape = br.getByte();
        size = br.getVector3f();
        position = br.getVector3f();
        rotation = br.getVector3f();
        mass = br.getFloat();
        moveDecay = br.getFloat();
        rotDecay = br.getFloat();
        repulsion = br.getFloat();
        friction = br.getFloat();
        physics = br.getByte();
    }
}