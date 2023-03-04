package viewer.model.pmx;

import org.joml.Vector3f;

import viewer.io.BinaryReader;

public class Joint {
    public String name;
    public String name_en;
    public byte type;
    public int rigidbodyA;
    public int rigidbodyB;

    //Case1 : type = 0
    public Vector3f position;
    public Vector3f rotation;
    public Vector3f moveLimit_inf;
    public Vector3f moveLimit_sup;
    public Vector3f rotLimit_inf;
    public Vector3f rotLimit_sup;
    public Vector3f springConstMove;
    public Vector3f springConstRot;

    public Joint(BinaryReader br, Header header) throws PMXImportException{
        name = TextBuf.readString(br, header.getEncode());
        name_en = TextBuf.readString(br, header.getEncode());
        type = br.getByte();

        if (type == 0){
            rigidbodyA = br.getIntFromByteBuffer(header.rigidbodyIndexSize);
            rigidbodyB = br.getIntFromByteBuffer(header.rigidbodyIndexSize);
            position = br.getVector3f();
            rotation = br.getVector3f();
            moveLimit_inf = br.getVector3f();
            moveLimit_sup = br.getVector3f();
            rotLimit_inf = br.getVector3f();
            rotLimit_sup = br.getVector3f();
            springConstMove = br.getVector3f();
            springConstRot = br.getVector3f();
        } else {
            throw new PMXImportException("Invalid joint type.");
        }
    }
}