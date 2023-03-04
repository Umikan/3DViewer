package viewer.model.gltf;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;

import viewer.model.common.MVP;

public class GLTFCamera {
    private Node node;
    private Matrix4f P;
    private static List<GLTFCamera> cameras = new ArrayList<GLTFCamera>();
    public GLTFCamera(GLTF gltf, Node node, int index){
        this.node = node;
        GLTF.Camera camera = gltf.cameras[index];
        switch (camera.type){
            case "perspective": {
                float fovy = camera.perspective.yfov;
                float aspectRatio = camera.perspective.aspectRatio;
                float znear = camera.perspective.znear;
                float zfar = camera.perspective.zfar;   
                P = new Matrix4f().perspective(fovy, aspectRatio, znear, zfar);
                break;
            }
            case "orthographic": {
                float xmag = camera.orthographic.xmag;
                float ymag = camera.orthographic.ymag;
                float znear = camera.orthographic.znear;
                float zfar = camera.orthographic.zfar;  
                P = new Matrix4f().ortho(-xmag, xmag, -ymag, ymag, znear, zfar);
                break;
            }
            default: return;
        }
        cameras.add(this);
    }
    public void apply(){
        //must invert this matrix since a camera is treated as zero-point position
        Matrix4f V = new Matrix4f(node.getGlobalTransform()).invert();
        MVP.setVP(V, P);
    }
    public static void tryToSetFirstCamera(){
        if (cameras.size() == 0) return;
        cameras.get(0).apply();
    }
}