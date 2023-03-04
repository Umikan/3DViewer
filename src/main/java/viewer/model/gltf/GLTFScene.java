package viewer.model.gltf;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL4;

import org.joml.Matrix4f;
import static com.jogamp.opengl.GL4.*;

public class GLTFScene {
    private List<Node> nodes = new ArrayList<Node>();
    private GLTFBuffer buffer;
    private GLTF gltf;
    private Matrix4f worldTransform;
    private String parentDir;
    public GLTFScene(String parentDir, GLTF gltf){
        this.parentDir = parentDir;
        this.gltf = gltf;
        worldTransform = new Matrix4f();
        addAllNodes();
    }
    private void addAllNodes(){
        for (int idx : gltf.scenes[gltf.scene].nodes){
            nodes.add(new Node(gltf, idx, parentDir));
        }
    }
    public void init(GL4 gl){
        buffer = new GLTFBuffer(gl, parentDir, gltf);
        for (Node node : nodes) node.init(gl, buffer);
    }
    public void destroy(GL4 gl){
        buffer.release(gl);
        for (Node node : nodes) node.destroy(gl);
    }
    public void render(GL4 gl){
        for (Node node : nodes) node.render(gl, worldTransform, animation);
        animation = false;
    }
    public void setWorldTransform(Matrix4f mat){
        worldTransform = mat;
    }
    public void printComposition(){
        for (Node node : nodes) node.printComposition("");
    }
    private boolean animation = false;
    public void setAnimation(GL4 gl, float time, int id){
        if (gltf.animations == null) return;
        if (gltf.animations.length <= id) return;
        animation = true;
        GLTF.Animation anim = gltf.animations[id];
        for (GLTF.Animation.Channel channel : anim.channels){
            int inputAccessor = anim.samplers[channel.sampler].input;
            int outputAccessor = anim.samplers[channel.sampler].output;
            //support only a data type of GL_FLOAT
            if (buffer.getComponentType(inputAccessor) != GL_FLOAT) return;
            if (buffer.getComponentType(outputAccessor) != GL_FLOAT) return;
            int inputName = buffer.getBufferName(inputAccessor);
            int outputName = buffer.getBufferName(outputAccessor);
            AnimInput animInput = getInput(gl, inputName, time);
            reflectOutput(gl, outputName, animInput, channel.target);
        }
    }
    private AnimInput getInput(GL4 gl, int inputBufferName, float t){
        gl.glBindBuffer(GL_ARRAY_BUFFER, inputBufferName);
        AnimInput animInput = new AnimInput(0, 0.0f);
        FloatBuffer buffer = gl.glMapBuffer(GL_ARRAY_BUFFER, GL_READ_ONLY).asFloatBuffer();
        for (int i = 0; i < buffer.capacity() - 1; i++){
            buffer.position(i);
            float t1 = buffer.get();
            float t2 = buffer.get();
            if (t1 <= t && t < t2){
                animInput = new AnimInput(i, (t - t1) / (t2 - t1));
                break;
            }
        }
        gl.glUnmapBuffer(GL_ARRAY_BUFFER);
        return animInput;
    }
    private void reflectOutput(GL4 gl, int outputBufferName, AnimInput input, 
        GLTF.Animation.Channel.Target target){
        gl.glBindBuffer(GL_ARRAY_BUFFER, outputBufferName);
        FloatBuffer buffer = gl.glMapBuffer(GL_ARRAY_BUFFER, GL_READ_ONLY).asFloatBuffer();
        Node node = Node.getNode(gltf.nodes[target.node]);
        switch(target.path){
            case "translation": 
            {   
                buffer.position(input.keyFrame * 3);
                float[] v1 = {buffer.get(), buffer.get(), buffer.get()};
                float[] v2 = {buffer.get(), buffer.get(), buffer.get()};
                float[] w = lerp(v1, v2, input.t);
                node.setTranslation(w[0], w[1], w[2]);
                break;
            }

            case "rotation":
            {
                buffer.position(input.keyFrame * 4);
                float[] v1 = {buffer.get(), buffer.get(), buffer.get(), buffer.get()};
                float[] v2 = {buffer.get(), buffer.get(), buffer.get(), buffer.get()};
                float[] w = qlerp(v1, v2, input.t);
                node.setRotation(w[0], w[1], w[2], w[3]);
                break;
            }
            case "scale":
            {
                buffer.position(input.keyFrame * 3);
                float[] v1 = {buffer.get(), buffer.get(), buffer.get()};
                float[] v2 = {buffer.get(), buffer.get(), buffer.get()};
                float[] w = lerp(v1, v2, input.t);
                node.setScale(w[0], w[1], w[2]);
                break;
            }
            case "weights":
            {
                System.out.println("Weights is not supported.");
                break;
            }
        }
        gl.glUnmapBuffer(GL_ARRAY_BUFFER);
    }
    private float[] lerp(float[] v1, float[] v2, float t){
        assert(v1.length == v2.length);
        float[] result = new float[v1.length];
        for (int i = 0; i < v1.length; i++){
            result[i] = v1[i] * (1.0f - t) + v2[i] * t;
        }
        return result;
    }
    private float[] qlerp(float[] v1, float[] v2, float t){
        float[] result = new float[4];
        float dot = 0;
        for (int i = 0; i < 4; i++) dot += v1[i] * v2[i];
        if (dot >= 0) {
            for (int i = 0; i < 4; i++){
                result[i] = v1[i] * (1.0f - t) + v2[i] * t;
            }
        } else {
            for (int i = 0; i < 4; i++){
                result[i] = v1[i] * (1.0f - t) - v2[i] * t;
            }
        }
        return result;  
    }
    private static class AnimInput{
        public int keyFrame;
        public float t;
        public AnimInput(int keyFrame, float t){
            this.keyFrame = keyFrame;
            this.t = t;
        }
    }
}