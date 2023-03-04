package viewer.model.pmx;

import java.io.File;
import java.nio.Buffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;

import viewer.io.BinaryReader;
import viewer.model.pmx.bone.BoneManager;
import viewer.model.pmx.morph.*;
import viewer.model.vmd.VMD;
import viewer.shader.Memory;

import org.joml.Vector3f;

import static com.jogamp.opengl.GL4.*;

import java.util.function.Consumer;

public class PMX {
    private Header header;
    private ModelInfo modelInfo;
    private Vertex vertex;
    private int[] indices;
    private Texture texture;
    private ArrayList<Material> materials = new ArrayList<Material>();
    private BoneManager boneManager;
    private ArrayList<Morph> morphs = new ArrayList<Morph>();
    private MorphBuffer morphBuffer = new MorphBuffer();
    private ArrayList<DisplayFrame> frames = new ArrayList<DisplayFrame>();
    private ArrayList<Rigidbody> rigidbodys = new ArrayList<Rigidbody>();
    private ArrayList<Joint> joints = new ArrayList<Joint>();

    private String parentDir;
    public String getParentDirectory(){
        return parentDir;
    }
    public PMX(String path){
        parentDir = new File(path).getParent();
        try {
            BinaryReader br = new BinaryReader(path);
            br.setByteOrder(ByteOrder.LITTLE_ENDIAN);
            //Header
            header = new Header(br);
            //Model Infomation
            modelInfo = new ModelInfo(br, header.getEncode());
            //Vertex
            vertex = new Vertex(br, header);
            //Index
            int count = br.getInt();
            indices = new int[count];
            for (int i = 0; i < count; i++){
                indices[i] = br.getIntFromByteBuffer(header.vertexIndexSize);
            }
            //Texture
            texture = new Texture(br, header.getEncode());
            //Material
            int materialCount = br.getInt();
            for (int i = 0; i < materialCount; i++){
                materials.add(new Material(br, header));
            }
            //Bone
            int boneCount = br.getInt();
            ArrayList<Bone> bones = new ArrayList<Bone>();
            for (int i = 0; i < boneCount; i++){
                bones.add(new Bone(br, header));
            }
            boneManager = new BoneManager(bones);
            //Morph
            int morphCount = br.getInt();
            MorphContainer mc = new MorphContainer();
            for (int i = 0; i < morphCount; i++){
                morphs.add(new Morph(br, header, mc));
            }
            MorphEncoder.BufferInfo bi = 
                new MorphEncoder.BufferInfo(vertex.position.length);
            MorphEncoder me = new MorphEncoder(mc, morphBuffer, morphs, bi);
            me.encode();
            //Display Frame
            int frameCount = br.getInt();
            for (int i = 0; i < frameCount; i++){
                frames.add(new DisplayFrame(br, header));
            }
            //Rigidbody
            int rdCount = br.getInt();
            for (int i = 0; i < rdCount; i++){
                rigidbodys.add(new Rigidbody(br, header));
            }
            //Joint
            int jointCount = br.getInt();
            for (int i = 0; i < jointCount; i++){
                joints.add(new Joint(br, header));
            }
        } catch (PMXImportException e){
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }


    private PMXBuffer buffer;
    public void init(GL4 gl){
        buffer = new PMXBuffer(gl, this);
        morphBuffer.init(gl);
        boneManager.initSkinningBuffer(gl);
    }
    public void render(GL4 gl){
        buffer.enable(gl);
        boneManager.enableSkinningBuffer(gl);
        morphBuffer.enable(gl);
        int byteOffset = 0;
        for (Material material : materials){
            buffer.setTexture(gl, material.texture);
            Memory.putVec4(material.diffuse);
            gl.glUniform4fv(7, 1, Memory.vec4); //diffuse
            gl.glDrawElements(GL_TRIANGLES, material.indicesCount, GL_UNSIGNED_INT, byteOffset);
            byteOffset += material.indicesCount * 4;
        }
        morphBuffer.disable(gl);
        buffer.disable(gl);
    }

    public void animation(float time){
        boneManager.setSkinningAnimation(time);
        morphBuffer.setMorphAnimation(time);
    }
    public void attachVMD(VMD vmd){
        boneManager.motionManager = vmd.getMotion();
        morphBuffer.controller = vmd.getMorph();
    }

    public void renderBones(GL4 gl){
        gl.glLineWidth(3.0f);
        Consumer<Bone> func = (Bone bone) -> {
            if (boneManager.getParent(bone) == null) return;
            for (Bone child : boneManager.getChildren(bone)){
                float[] f = new float[6];
                Vector3f v1 = bone.getGlobalPosition();
                Vector3f v2 = child.getGlobalPosition();
                f[0] = v1.x;
                f[1] = v1.y;
                f[2] = v1.z;
                f[3] = v2.x;
                f[4] = v2.y;
                f[5] = v2.z;
                IntBuffer nameBuffer = IntBuffer.allocate(1);
                gl.glGenBuffers(1, nameBuffer);
                Buffer buffer = Buffers.newDirectFloatBuffer(f);
                int byteLength = buffer.capacity() * Buffers.SIZEOF_FLOAT;
                gl.glBindBuffer(GL_ARRAY_BUFFER, nameBuffer.get(0));
                gl.glBufferData(GL_ARRAY_BUFFER, byteLength, buffer, GL_DYNAMIC_DRAW);

                gl.glBindBuffer(GL_ARRAY_BUFFER, nameBuffer.get(0));   
                gl.glEnableVertexAttribArray(0);
                gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
                gl.glDrawArrays(GL_LINES, 0, 2);
                gl.glDisableVertexAttribArray(0);
                gl.glDeleteBuffers(1, nameBuffer);
            }
        };

        boneManager.forEachFromParent(func);
    }

    public void renderIK(GL4 gl){
        gl.glPointSize(10.0f);
        Consumer<Bone> func = (Bone bone) -> {
                if (!bone.isIK()) return;
                for (int i = 0; i < 3; i++){
                    float[] f = new float[3];
                    Vector3f v1 = null;
                    if (i == 0) v1 = bone.getGlobalPosition();
                    if (i == 1) v1 = boneManager.get(bone.IK.target).getGlobalPosition();
                    if (i == 2) {
                        Bone.IK ik = bone.IK;
                        v1 = boneManager.get(ik.link[ik.linkCount-1].index).getGlobalPosition();
                    }
                    f[0] = v1.x;
                    f[1] = v1.y;
                    f[2] = v1.z;
                    IntBuffer nameBuffer = IntBuffer.allocate(1);
                    gl.glGenBuffers(1, nameBuffer);
                    Buffer buffer = Buffers.newDirectFloatBuffer(f);
                    int byteLength = buffer.capacity() * Buffers.SIZEOF_FLOAT;
                    gl.glBindBuffer(GL_ARRAY_BUFFER, nameBuffer.get(0));
                    gl.glBufferData(GL_ARRAY_BUFFER, byteLength, buffer, GL_DYNAMIC_DRAW);
    
                    gl.glBindBuffer(GL_ARRAY_BUFFER, nameBuffer.get(0));   
                    gl.glEnableVertexAttribArray(0);
                    gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
                    if (i == 0) gl.glUniform4f(1, 1.0f, 0.0f, 0.0f, 1.0f);
                    if (i == 1) gl.glUniform4f(1, 0.0f, 0.0f, 1.0f, 1.0f);
                    if (i == 2) gl.glUniform4f(1, 0.0f, 0.0f, 0.0f, 1.0f);
                    gl.glDrawArrays(GL_POINTS, 0, 1);
                    gl.glDisableVertexAttribArray(0);
                    gl.glDeleteBuffers(1, nameBuffer);
                }
        };
        boneManager.forEachFromParent(func);
    }

    Vertex vertex(){ return vertex; }
    int[] indices(){ return indices; }
    Texture texture(){ return texture; }
}

