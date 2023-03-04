package viewer.model.gltf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.jogamp.opengl.GL4;

import viewer.model.common.MVP;

import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class Node{
    private GLTF gltf;
    private int idx;
    private List<Node> children = new ArrayList<Node>();
    private static HashMap<GLTF.Node, Node> nodeObject = new HashMap<GLTF.Node, Node>();
    //It's not enough to give an information of node index. Give one of a GLTF.Node instance.
    public static Node getNode(GLTF.Node node){
        return nodeObject.get(node);
    }
    private GLTF.Node get(){
        return gltf.nodes[idx];
    }

    private Matrix4f globalTransform;
    private Mesh mesh;
    public Node(GLTF gltf, int idx){
        this.gltf = gltf;
        this.idx = idx;
        createCamera(gltf);
        nodeObject.put(gltf.nodes[idx], this);
    }
    public Node(GLTF gltf, int idx, String parentDir){
        this(gltf, idx);
        calcGlobalTransform(new Matrix4f());
        createMesh(parentDir);
        addChildren(gltf, parentDir);
    }
    public Node(GLTF gltf, int idx, String parentDir, Node parent){
        this(gltf, idx);
        //first
        calcGlobalTransform(parent.getGlobalTransform());
        //second
        createMesh(parentDir);
        addChildren(gltf, parentDir);
    }
    private void addChildren(GLTF gltf, String parentDir){
        int[] c = gltf.nodes[idx].children;
        if (c == null) return;
        for (int childIndex : c){
            Node node = new Node(gltf, childIndex, parentDir, this);
            children.add(node);
        }
    }
    private void createMesh(String parentDir){
        mesh = new Mesh(gltf, get(), parentDir);
    }
    private void createCamera(GLTF gltf){
        int camera = get().camera;
        if (camera == GLTF.EMPTY) return;
        new GLTFCamera(gltf, this, camera);
    }
    private void calcGlobalTransform(Matrix4f parent){
        globalTransform = new Matrix4f(); 

        if (animationReflectFlag){
            reflectAnimation(parent);
            return;
        }
        float[] m = get().matrix;
        if (m != null){
            Matrix4f mat = new Matrix4f(
                m[0], m[1], m[2], m[3], 
                m[4], m[5], m[6], m[7], 
                m[8], m[9], m[10], m[11],
                m[12], m[13], m[14], m[15]);
            globalTransform = new Matrix4f(parent).mul(mat);
            return;
        }
        float[] t = get().translation;
        float[] r = get().rotation;
        float[] s = get().scale;
        if (t != null) setTranslation(t[0], t[1], t[2]);
        if (r != null) setRotation(r[0], r[1], r[2], r[3]);
        if (s != null) setScale(s[0], s[1], s[2]);
        globalTransform = new Matrix4f(parent).mul(getTRS());
    }

    private boolean animationReflectFlag = false;
    private Matrix4f T, R, S;
    {
        T = R = S = new Matrix4f();
    }
    private void reflectAnimation(Matrix4f parent){
        globalTransform = new Matrix4f(parent).mul(getTRS());
        resetTRS();
        animationReflectFlag = false;
    }
    private Matrix4f getTRS(){
        if (T == null && R == null && S == null) return new Matrix4f();
        return new Matrix4f(T).mul(R).mul(S);
    }
    private void resetTRS(){
        T = R = S = new Matrix4f();
        //must reset to default transformation.
        float[] t = get().translation;
        float[] r = get().rotation;
        float[] s = get().scale;
        if (t != null) setTranslation(t[0], t[1], t[2]);
        if (r != null) setRotation(r[0], r[1], r[2], r[3]);
        if (s != null) setScale(s[0], s[1], s[2]);
        animationReflectFlag = false;
    }
    public void setTranslation(float x, float y, float z){
        T = new Matrix4f().translate(x, y, z);
        animationReflectFlag = true;
    }
    public void setRotation(float x, float y, float z, float w){
        Quaternionf Q = new Quaternionf(x, y, z, w);
        R = new Matrix4f().rotate(Q);
        animationReflectFlag = true;
    }
    public void setScale(float x, float y, float z){
        S = new Matrix4f().scale(x, y, z);
        animationReflectFlag = true;
    }


    public void render(GL4 gl, Matrix4f worldTransform, boolean animation){
        if (animation) render(gl, worldTransform, new Matrix4f());
        else render(gl, worldTransform);
    }
    private void render(GL4 gl, Matrix4f worldTransform){
        Matrix4f newM = new Matrix4f(worldTransform).mul(getGlobalTransform());
        MVP.setM(newM); 
        MVP.uniformMVP(gl);
        mesh.render(gl);
        for (Node child : children) child.render(gl, worldTransform);
    }
    public void render(GL4 gl, Matrix4f worldTransform, Matrix4f parentTransform){
        calcGlobalTransform(parentTransform);
        Matrix4f newM = new Matrix4f(worldTransform).mul(getGlobalTransform());
        MVP.setM(newM); 
        MVP.uniformMVP(gl);
        mesh.render(gl);
        for (Node child : children) child.render(gl, worldTransform, getGlobalTransform());
    }
    public void destroy(GL4 gl){
        nodeObject.remove(gltf.nodes[idx]);
        mesh.destroy(gl);
        for (Node child : children) child.destroy(gl);
    }
    public void init(GL4 gl, GLTFBuffer buffer){
        mesh.init(gl, buffer);
        for (Node child : children) child.init(gl, buffer);
    }
    protected Matrix4f getGlobalTransform(){
        return globalTransform;
    }
    public void printComposition(String str){
        System.out.println(str + idx);
        MVP.printTransform(globalTransform);
        for (Node child : children) child.printComposition(str + "-");
    }
}