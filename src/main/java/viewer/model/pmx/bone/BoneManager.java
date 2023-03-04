package viewer.model.pmx.bone;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;

import viewer.model.pmx.Bone;
import viewer.model.vmd.MotionManager;
import viewer.model.vmd.MotionTransform;
import viewer.shader.Memory;

import static com.jogamp.opengl.GL4.*;

public class BoneManager{
    private Bone rootBone;
    private ArrayList<Bone> bones = new ArrayList<Bone>();
    private ArrayList<Bone> IKs = new ArrayList<Bone>();
    private HashMap<Bone, ArrayList<Bone>> parentToChildren = new HashMap<Bone, ArrayList<Bone>>();
    public MotionManager motionManager;
    public BoneManager(ArrayList<Bone> bones){
        this.bones = bones;
        //Initialize
        for (Bone bone : bones){
            parentToChildren.put(bone, new ArrayList<Bone>());
        }
        parentToChildren.put(null, new ArrayList<Bone>());
        //Register IK
        for (Bone bone : bones){
            if (bone.isIK()) IKs.add(bone);
        }
        //Create parent-to-child hashmap
        for (Bone child : bones){
            Bone parent = getParent(child);
            if (parent == null) continue;
            parentToChildren.get(parent).add(child);
        }
        //Search root node
        for (Bone bone : bones){
            if (bone.parentBone == -1){
                rootBone = bone;
                break;
            }
        }
        //add parent-child relation
        for (Bone child : bones){
            Bone parent = getParent(child);
            if (parent == null && child != rootBone) 
                parentToChildren.get(rootBone).add(child);
        }
        //give this and calc relative initMatrix
        for (Bone bone : bones){
            bone.calcRelativeMatrix(this);
        }
    }
    public ArrayList<Bone> getChildren(Bone bone){
        return parentToChildren.get(bone);
    }
    public Bone get(int index){
        return bones.get(index);
    }
    public Bone getParent(Bone bone){
        if (bone.parentBone == -1) return null;
        return bones.get(bone.parentBone);
    }
    public Bone getRootBone(){ return rootBone; }
    //NOTE: this method doesn't be called in all bones.
    public void forEachFromParent(Consumer<Bone> func){
        func.accept(rootBone);
        for (Bone child : parentToChildren.get(rootBone)){
            forEachFromParent(func, child);
        }
    }
    private void forEachFromParent(Consumer<Bone> func, Bone bone){
        func.accept(bone);
        for (Bone child : parentToChildren.get(bone)){
            forEachFromParent(func, child);
        }    
    }

    public void setSkinningAnimation(float time){
        if (motionManager == null) return;
        Consumer<Bone> boneMatrixSetter = (Bone bone) -> {
            MotionTransform mt = motionManager.getMotion(bone.name, time);
            bone.updateTransform(mt);
        };   
        forEachFromParent(boneMatrixSetter);
        Consumer<Bone> updater = (Bone bone) -> { bone.update(this); };
        forEachFromParent(updater);

        updateIK();
    }
    private void updateIK(){
        //Inverse Kinematics
        for (Bone ik : IKs) {
            ik.updateIK(this);
        }
        //Additive Bone Calculation
        Consumer<Bone> additiveUpdater = (Bone bone) -> { bone.additiveUpdate(this); };
        forEachFromParent(additiveUpdater);    

        Consumer<Bone> posture = (Bone bone) -> { bone.calcPosture(); };
        forEachFromParent(posture);
    }

    FloatBuffer skinningBuffer;
    IntBuffer skinningBufferName;
    public void initSkinningBuffer(GL4 gl){
        skinningBuffer = Buffers.newDirectFloatBuffer(bones.size() * 16);
        skinningBufferName = IntBuffer.allocate(1);
        gl.glGenTextures(1, skinningBufferName);
        int id = skinningBufferName.get(0);
        gl.glBindTexture(GL_TEXTURE_2D, id);
        gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA32F, 4, bones.size(), 0, GL_RGBA, GL_FLOAT, null);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE); 
        gl.glBindTexture(GL_TEXTURE_2D, 0);
    }
    public void deleteSkinningBuffer(GL4 gl){
        gl.glDeleteTextures(1, skinningBufferName);
    }
    public void enableSkinningBuffer(GL4 gl){
        /*
        final int firstLoc = 1000;
        int count = 0;
        for (Bone bone : bones){
            bone.skinMatrix.get(Memory.mat4);
            gl.glUniformMatrix4fv(firstLoc + count, 1, false, Memory.mat4);
            count++;
        }*/
        skinningBuffer.position(0);
        int i = 0; 
        for (Bone bone : bones) {
            bone.skinMatrix.get(i * 16, skinningBuffer);
            i++;
        }
        gl.glBindTexture(GL_TEXTURE_2D, skinningBufferName.get(0));
        gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA32F, 4, bones.size(), 0, GL_RGBA, GL_FLOAT, skinningBuffer);

        gl.glActiveTexture(GL_TEXTURE2);
        gl.glBindTexture(GL_TEXTURE_2D, skinningBufferName.get(0));
        gl.glUniform1i(5, bones.size());
    }
}