package viewer.model.pmx;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import viewer.io.BinaryReader;
import viewer.model.pmx.bone.BoneManager;
import viewer.model.vmd.MotionTransform;


//TODO: ボーンのフラグをもとに変形を実装する
public class Bone {
    private Matrix4f boneOffsetMatrix;
    private Matrix4f initMatrix;
    private Matrix4f boneMatrix = new Matrix4f();
    public Matrix4f skinMatrix = new Matrix4f();
    private Quaternionf rotation = new Quaternionf();
    private Quaternionf rotationWithoutIK = new Quaternionf();
    private Vector3f translation = new Vector3f();
    //called in all Bone at once
    private void matrixInit(){
        //at first time this is world translation
        initMatrix = new Matrix4f().translate(initPosition);
        boneOffsetMatrix = new Matrix4f(initMatrix).invert();
    }
    //called in all Bone at once
    public void calcRelativeMatrix(BoneManager boneManager){
        Bone parent;
        if ((parent = boneManager.getParent(this)) != null){
            //convert to a relative matrix
            initMatrix.mul(parent.boneOffsetMatrix);
        }
        boneMatrix = new Matrix4f(initMatrix);
    }
    //called in all bone per frame before skinning
    public void updateTransform(MotionTransform transform){
        //update if not null
        if (transform != null){
            rotationWithoutIK = transform.rotation;
            translation = transform.translation;
        } 
        //update

        //TODO: check whether it's better to update for IK link bone
        //rotation = new Quaternionf(rotationWithoutIK);
        if (transform != null) rotation = new Quaternionf(rotationWithoutIK);
        calcBoneMatrix();
    }
    private void calcBoneMatrix(){
        boneMatrix = new Matrix4f(initMatrix).translate(translation).rotate(rotation);
    }
    private Matrix4f localBoneMatrix;
    public void update(BoneManager boneManager){
        calcBoneMatrix();
        Bone parent = boneManager.getParent(this);
        Matrix4f parentBoneMatrix = parent == null ? new Matrix4f() : parent.localBoneMatrix;
        localBoneMatrix = new Matrix4f(parentBoneMatrix).mul(boneMatrix);
    }
    public void additiveUpdate(BoneManager boneManager){
        //TODO: check this is correct
        if (addRotation() || addTranslation()){
            Quaternionf q = new Quaternionf();
            Vector3f v = new Vector3f();
            if (addRotation()){
                Bone source = boneManager.get(additiveParent);
                q = new Quaternionf().slerp(source.rotation, additiveRate);
            }
            if (addTranslation()){
                Bone source = boneManager.get(additiveParent);
                v = new Vector3f().lerp(source.translation, additiveRate);
            }
            boneMatrix = new Matrix4f(initMatrix).translate(v).rotate(q)
                .translate(translation).rotate(rotation);
        }
        Bone parent = boneManager.getParent(this);
        Matrix4f parentBoneMatrix = parent == null ? new Matrix4f() : parent.localBoneMatrix;
        localBoneMatrix = new Matrix4f(parentBoneMatrix).mul(boneMatrix);
    }
    public void calcPosture(){
        skinMatrix = new Matrix4f(localBoneMatrix).mul(boneOffsetMatrix);
    }
    public void updateForIK(BoneManager boneManager, Quaternionf newRotation){
        if (newRotation != null){
            rotation = newRotation.normalize();
        }
        update(boneManager);
        for (Bone child : boneManager.getChildren(this)){
            child.updateForIK(boneManager, null);
        }
    }
    public Vector3f getGlobalPosition(){
        return new Matrix4f(localBoneMatrix).transformPosition(new Vector3f());
    }

    public void updateIK(BoneManager boneManager){
        Bone targetBone = boneManager.get(IK.target);

        UPDATE: for (int i = 0; i < IK.iteration; i++){ 
        for (int j = 0; j < IK.link.length; j++){
            /*final int loopCount = i * IK.link.length + j;
            final int maxCount = Parameter.count * IK.link.length + Math.min(Parameter.length, IK.link.length-1);
            if (loopCount > maxCount) break UPDATE;*/

                IK.IKLink link = IK.link[j];
                Bone linkBone = boneManager.get(link.index);
                Vector3f effectorPos = targetBone.getGlobalPosition()
                    .sub(linkBone.getGlobalPosition());
                Vector3f targetPos = getGlobalPosition()
                    .sub(linkBone.getGlobalPosition());
                //NOTE: make bone space.
                //if not doing this, it will be buggy when a model rotates more than 90 degrees.
                Quaternionf parentQ = boneManager.getParent(linkBone).localBoneMatrix.getNormalizedRotation(new Quaternionf());
                parentQ.conjugate();
                parentQ.transform(effectorPos);
                parentQ.transform(targetPos);
                
                Quaternionf q = createQuaternion2Vec(effectorPos, targetPos);
                if (q == null) continue;

                q.mul(linkBone.rotation);
                Vector3f eulerAngle = q.getEulerAnglesXYZ(new Vector3f());
                Quaternionf newRotation = link.getRotation(eulerAngle);
 
                /*if (loopCount == maxCount){
                        Debug.line(linkBone.getGlobalPosition(), targetBone.getGlobalPosition());
                        Debug.line(linkBone.getGlobalPosition(), getGlobalPosition());
                        AxisAngle4f axis = new Matrix4f().rotate(newRotation)
                            .getRotation(new AxisAngle4f());
                        Vector3f v = new Vector3f(axis.x, axis.y, axis.z).mul(1000.0f);
                        Debug.line(linkBone.getGlobalPosition(), linkBone.getGlobalPosition().add(v));
                }*/

                linkBone.updateForIK(boneManager, newRotation);
            }
        }

        //negate twist 
        //if (!Parameter.angleFlag) return;
        for (int j = 0; j < IK.link.length; j++){
            IK.IKLink link = IK.link[j];
            Bone linkBone = boneManager.get(link.index);
            Vector3f effectorPos = targetBone.getGlobalPosition()
                .sub(linkBone.getGlobalPosition());
            Quaternionf parentQ = boneManager.getParent(linkBone).localBoneMatrix.getNormalizedRotation(new Quaternionf());
            parentQ.conjugate();
            parentQ.transform(effectorPos);

            Quaternionf q = getTwist(linkBone.rotation, effectorPos);
            q.conjugate();
            q.mul(linkBone.rotation);
            linkBone.updateForIK(boneManager, q); 
        }
    }

    //https://qiita.com/hibiki8229/items/2034980111eb11d3f837
    public Quaternionf getTwist(Quaternionf q, Vector3f twistAxis) {
        twistAxis = new Vector3f(twistAxis).normalize();
        Vector3f r = new Vector3f(q.x, q.y, q.z);
        if (r.length() < 0.001) {
            return new Quaternionf();
        } else {
            // formula & proof:
            // http://www.euclideanspace.com/maths/geometry/rotations/for/decomposition/
            Vector3f p = new Vector3f(twistAxis).mul(r.dot(twistAxis));
            return new Quaternionf(p.x, p.y, p.z, q.w).normalize();
        }
    }

    private Quaternionf createQuaternion2Vec(Vector3f v1, Vector3f v2){
        v1 = new Vector3f(v1).normalize();
        v2 = new Vector3f(v2).normalize();
        float cos = v1.dot(v2);
        float angle = (float)Math.acos(cos);
        Vector3f rotateAxis = v1.cross(v2, new Vector3f()).normalize();
        if (Float.isNaN(rotateAxis.x) || 
        Float.isNaN(rotateAxis.y) || 
        Float.isNaN(rotateAxis.z)) return null;
        if (Float.isNaN(angle)) return null; 
        if (angle > IK.limitAngle) angle = IK.limitAngle;
        return new Quaternionf().fromAxisAngleRad(rotateAxis, angle);
    }

    public String name;
    public String name_en;
    public Vector3f initPosition;
    public int parentBone;
    public int level;
    public short flag;

    public Vector3f offset;
    public int connection;

    public int additiveParent;
    public float additiveRate;

    public Vector3f axisDirection;
    public Vector3f xAxisDirection;
    public Vector3f zAxisDirection;

    public int keyValue;

    public IK IK;
    public static class IK{
        public int target;
        public int iteration;
        public float limitAngle;
        public int linkCount;
        public IKLink[] link;
        public IK(BinaryReader br, Header header){
            target = br.getIntFromByteBuffer(header.boneIndexSize);
            iteration = br.getInt();
            limitAngle = br.getFloat();
            linkCount = br.getInt();
            link = new IKLink[linkCount];
            for (int i = 0; i < linkCount; i++){
                link[i] = new IKLink(br, header);
            }
        }
        public static class IKLink{
            public int index;
            public boolean limitAngleFlag;
            public Vector3f inf, sup;
            public IKLink(BinaryReader br, Header header){
                index = br.getIntFromByteBuffer(header.boneIndexSize);
                limitAngleFlag = br.getByte() == 1;
                if (limitAngleFlag){
                    inf = br.getVector3f();
                    sup = br.getVector3f();
                }
            }
            public Quaternionf getRotation(Vector3f eulerAngle){
                if (limitAngleFlag){
                    eulerAngle.x = clamp(inf.x, sup.x, eulerAngle.x);
                    eulerAngle.y = clamp(inf.y, sup.y, eulerAngle.y);
                    eulerAngle.z = clamp(inf.z, sup.z, eulerAngle.z);
                }
                return new Quaternionf().rotateXYZ(eulerAngle.x, eulerAngle.y, eulerAngle.z);
            }
            private float clamp(float min, float max, float v){
                if (min > v) return min;
                if (max < v) return max;
                return v;
            }
        }
    }
    
    public Bone(BinaryReader br, Header header){
        name = TextBuf.readString(br, header.getEncode());
        name_en = TextBuf.readString(br, header.getEncode());
        initPosition = br.getVector3f();
        matrixInit();

        parentBone = br.getIntFromByteBuffer(header.boneIndexSize);
        level = br.getInt();
        flag = br.getShort();
        if (connectionType()){
            connection = br.getIntFromByteBuffer(header.boneIndexSize);
        } else {
            offset = br.getVector3f();
        }
        if (addRotation() || addTranslation()){
            additiveParent = br.getIntFromByteBuffer(header.boneIndexSize);
            additiveRate = br.getFloat();
        }

        if (fixAxis()){
            axisDirection = br.getVector3f();
        }
        if (localAxis()){
            xAxisDirection = br.getVector3f();
            zAxisDirection = br.getVector3f();
        }
        if (deformExternalParent()){
            keyValue = br.getInt();
        }
        if (isIK()) {
            IK = new IK(br, header);
        }
    }

    private boolean connectionType(){ return (0x0001 & flag) != 0; }
    private boolean canRotate(){ return (0x0002 & flag) != 0; }
    private boolean canTranslate(){ return (0x0004 & flag) != 0; }
    private boolean display(){ return (0x0008 & flag) != 0; }
    private boolean canControl(){ return (0x0010 & flag) != 0; }  
    public boolean isIK(){ return (0x0020 & flag) != 0; }
    private boolean addLocal(){ return (0x0080 & flag) != 0; }
    private boolean addRotation(){ return (0x0100 & flag) != 0; }
    private boolean addTranslation(){ return (0x0200 & flag) != 0; }
    private boolean fixAxis(){ return (0x0400 & flag) != 0; }
    private boolean localAxis(){ return (0x0800 & flag) != 0; }
    private boolean deformAfterPhysics(){ return (0x1000 & flag) != 0; }
    private boolean deformExternalParent(){ return (0x2000 & flag) != 0; }
}

