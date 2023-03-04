package viewer.model.gltf;

import java.io.File;
import java.nio.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

import viewer.shader.Memory;
import org.joml.Matrix4f;

import static com.jogamp.opengl.GL4.*;

public class Primitive
{
    private GLTFBuffer buffer;

    private int mode;
    private GLTF.Mesh.Primitive primitive;
    private GLTF.Material material;
    private GLTF.Sampler sampler;
    private TextureData textureData;
    private IntBuffer textureBuffer;
    private int textureID;

    public Primitive(String parentDir, GLTF gltf, GLTF.Mesh.Primitive primitive)
    {
        this.primitive = primitive;
        mode = primitive.mode;
        GLTF.Material.PBRMetallicRoughness pbrmr = null;
        if (primitive.material != GLTF.EMPTY){
            material = gltf.materials[primitive.material];
            pbrmr = material.pbrMetallicRoughness;
        }
        if (pbrmr != null){
            GLTF.Material.Texture bcTex = material.pbrMetallicRoughness.baseColorTexture;
            if (bcTex != null){
                GLTF.Texture tex = gltf.textures[bcTex.index];
                int samplerIndex = tex.sampler;
                if (samplerIndex != GLTF.EMPTY) sampler = gltf.samplers[samplerIndex];
                loadTexture(parentDir + "/" + gltf.images[tex.source].uri);
            }
        }
    }
    public void loadTexture(String path){
        try {
            File file = new File(path);
            String name = file.getName();
            textureData = TextureIO.newTextureData(GLProfile.get(GLProfile.GL4),
            file, false, name.substring(name.lastIndexOf(".") + 1));
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Can't load a texture");
            System.exit(0);
        }
    }
    public void init(GL4 gl, GLTFBuffer buffer, List<Matrix4f> jointMatrices){
        this.buffer = buffer;
        createTextureBuffer(gl);
        setJointMatrix(jointMatrices);
    }
    public void setJointMatrix(List<Matrix4f> jointMatrices){
        if (primitive.attributes.JOINTS_0 == GLTF.EMPTY) return;
        createJointMatrixBuffer(jointMatrices);
    }
    public void createTextureBuffer(GL4 gl){
        if (textureData == null) return;

        textureBuffer = IntBuffer.allocate(1);
        gl.glGenTextures(1, textureBuffer);
        textureID = textureBuffer.get(0);
        gl.glBindTexture(GL_TEXTURE_2D, textureID);
        gl.glTexImage2D(GL_TEXTURE_2D, 0, textureData.getInternalFormat(),
        textureData.getWidth(), textureData.getHeight(), 
        0, textureData.getPixelFormat(), textureData.getPixelType(), textureData.getBuffer());
        //glTexImage2D should send an image data.(last variable)
        gl.glGenerateMipmap(GL_TEXTURE_2D);
        if (sampler != null){
            if (sampler.magFilter != GLTF.EMPTY)
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, sampler.magFilter);
            if (sampler.minFilter != GLTF.EMPTY)
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, sampler.minFilter);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, sampler.wrapS);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, sampler.wrapT);
        }
    }
    //Must call this functions to release memory.
    public void destroy(GL4 gl){
        gl.glDeleteTextures(1, textureBuffer);
    }

    public void render(GL4 gl){
        bufferObjects(gl);
        morph(gl);
        skin(gl);
        gl.glActiveTexture(GL_TEXTURE0);
        gl.glBindTexture(GL_TEXTURE_2D, textureID);

        gl.glUniform1i(6, textureData == null ? 0 : 1);
        if (primitive.indices == GLTF.EMPTY) {
            gl.glDrawArrays(this.mode, 0, buffer.getCount(primitive.attributes.POSITION));
        } else {
            int componentType = buffer.getComponentType(primitive.indices);
            gl.glDrawElements(this.mode, buffer.getCount(primitive.indices), componentType, 0);
        }

        gl.glBindBuffer(GL_ARRAY_BUFFER, 0);
        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        disableAll();
    }

    //NOTE: When using VAO, memory leaks.
    private List<Runnable> disable = new LinkedList<Runnable>();
    private void disableAll(){
        for (Runnable r : disable) r.run();
        disable.clear();
    }
    private void addDisable(final GL4 gl, final int i){
        Runnable r = () -> { gl.glDisableVertexAttribArray(i); };
        disable.add(r);
    }
    private void bufferObjects(GL4 gl){
        if (primitive.indices != GLTF.EMPTY)
            gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, buffer.getBufferName(primitive.indices));
        final int[] attributes = {
            primitive.attributes.POSITION, 
            primitive.attributes.NORMAL,
            primitive.attributes.TEXCOORD_0, 
            primitive.attributes.JOINTS_0, 
            primitive.attributes.WEIGHTS_0};
        final int[] size = {3,3,2,4,4};
        for (int i = 0; i < attributes.length; i++){
            if (attributes[i] == GLTF.EMPTY) continue;
            int VBO = buffer.getBufferName(attributes[i]);
            gl.glBindBuffer(GL_ARRAY_BUFFER, VBO);
            gl.glEnableVertexAttribArray(i);
            addDisable(gl, i);
            gl.glVertexAttribPointer(i, size[i], buffer.getComponentType(attributes[i]), false, 0, 0);
        }
    }
    //TODO: add normal and tangent morph
    //check whether normal morph is working
    private void morph(GL4 gl){
        final int maxMorphCount = 8;
        int morphCount = 0;
        final int firstLocation = 10;

        if (primitive.targets != null)
        for (int idx = 0; idx < primitive.targets.length; idx++){
            final int[] attributes = {
                    primitive.targets[idx].POSITION, 
                    primitive.targets[idx].NORMAL};
            final int[] size = {3, 3, 4};
            for (int i = 0; i < 2; i++){
                if (attributes[i] == GLTF.EMPTY) continue;
                int VBO = buffer.getBufferName(attributes[i]);
                int loc = firstLocation + morphCount + i * 10;
                gl.glBindBuffer(GL_ARRAY_BUFFER, VBO);
                gl.glEnableVertexAttribArray(loc);
                addDisable(gl, loc);
                gl.glVertexAttribPointer(loc, size[i], GL_FLOAT, false, 0, 0);
                if (i == 0) morphCount++;
            }
            if (morphCount == maxMorphCount) break;
        }
        gl.glUniform1i(100, morphCount);
        float[] weights = new float[morphCount];
        for (int i = 0; i < morphCount; i++){
            weights[i] = 0.0f;
        }
        FloatBuffer buffer = Buffers.newDirectFloatBuffer(weights);    
        gl.glUniform1fv(101, morphCount, buffer);
    }

    //skin
    private List<Matrix4f> jointMatrices = new ArrayList<Matrix4f>();
    
    private boolean hasSkin(){ return !jointMatrices.isEmpty(); }
    private void createJointMatrixBuffer(List<Matrix4f> matrices){
        jointMatrices = matrices;
    }
    private void skin(GL4 gl){
        if (!hasSkin()) return;
        gl.glUniform1i(5, jointMatrices.size());
        final int firstLocation = 200;
        for (int i = 0; i < jointMatrices.size(); i++){
            jointMatrices.get(i).get(Memory.mat4);
            gl.glUniformMatrix4fv(firstLocation + i, 1, false, Memory.mat4);
        }
    }
}