package viewer.model.gltf;

import java.nio.*;
import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL4;

import org.joml.Matrix4f;
import static com.jogamp.opengl.GL4.*;

public class Mesh {
    private List<Primitive> primitives = new ArrayList<Primitive>();
    private GLTF.Skin skin;
    private Matrix4f globalTransform;
    private GLTF gltf;
    public Mesh(GLTF gltf, GLTF.Node node, String parentDir){
        int mesh = node.mesh;
        int skin = node.skin;
        if (mesh == GLTF.EMPTY) return;
        this.globalTransform = Node.getNode(node).getGlobalTransform();
        this.gltf = gltf;
        if (skin != GLTF.EMPTY) this.skin = gltf.skins[skin];
        if (mesh != GLTF.EMPTY){
            for (GLTF.Mesh.Primitive primitive : gltf.meshes[mesh].primitives){
                Primitive p = new Primitive(parentDir, gltf, primitive);
                primitives.add(p);
            }
        }
    }
    public void destroy(GL4 gl){
        for (Primitive p : primitives) p.destroy(gl);
    }
    public void render(GL4 gl){
        setGlobalJointTransform();
        for (Primitive p : primitives) {
            p.setJointMatrix(getJointMatrices());
            p.render(gl);
        }
    }
    //TODO: recalc joint matrix while animating
    public void init(GL4 gl, GLTFBuffer buffer){
        setInverseBindMatrix(gl, buffer);
        setGlobalJointTransform();
        for (Primitive p : primitives) {
            p.init(gl, buffer, getJointMatrices());
        }
    }
    private List<Matrix4f> inverseBindMatrix;
    private List<Matrix4f> globalJointTransform;
    private void setInverseBindMatrix(GL4 gl, GLTFBuffer buffer){
        if (skin == null) return;
        inverseBindMatrix = new ArrayList<Matrix4f>();
        int index = buffer.getBufferName(skin.inverseBindMatrices);
        gl.glBindBuffer(GL_ARRAY_BUFFER, index);
        FloatBuffer ibm = gl.glMapBuffer(GL_ARRAY_BUFFER, GL_READ_ONLY).asFloatBuffer();
        for (int i = 0; i < ibm.capacity(); i+=16){
            ibm.position(i);
            inverseBindMatrix.add(new Matrix4f(
                ibm.get(), ibm.get(), ibm.get(), ibm.get(),
                ibm.get(), ibm.get(), ibm.get(), ibm.get(),
                ibm.get(), ibm.get(), ibm.get(), ibm.get(),
                ibm.get(), ibm.get(), ibm.get(), ibm.get()));
        }
        gl.glUnmapBuffer(GL_ARRAY_BUFFER);
    }
    //Must call this when global transform is changed. (e.g. when animating)
    private void setGlobalJointTransform(){
        if (skin == null) return;
        globalJointTransform = new ArrayList<Matrix4f>();
        for (int i = 0; i < skin.joints.length; i++){
            globalJointTransform.add(
                Node.getNode(gltf.nodes[skin.joints[i]]).getGlobalTransform());
        }
    }
    private List<Matrix4f> getJointMatrices(){
        if (skin == null) return null;
        List<Matrix4f> jointMatrix = new ArrayList<Matrix4f>();
        for (int i = 0; i < skin.joints.length; i++){
            jointMatrix.add(new Matrix4f(globalTransform).invert()
                .mul(globalJointTransform.get(i))
                .mul(inverseBindMatrix.get(i)));
        }
        return jointMatrix;
    }
}