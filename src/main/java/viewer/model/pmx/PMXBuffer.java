package viewer.model.pmx;

import static com.jogamp.opengl.GL4.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.Buffer;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;

import viewer.model.pmx.skin.BDEF;

public class PMXBuffer{
    private VertexBuffer vertex;
    private static class VertexBuffer{
        IntBuffer nameBuffer;
        int indices;
        int position;
        int normal;
        int uv;
        int joint;
        int weight;
        private VertexBuffer(GL4 gl){
            nameBuffer = IntBuffer.allocate(6);
            gl.glGenBuffers(6, nameBuffer);
            indices = nameBuffer.get(0);
            position = nameBuffer.get(1);
            normal = nameBuffer.get(2);
            uv = nameBuffer.get(3);
            joint = nameBuffer.get(4);
            weight = nameBuffer.get(5);
        }
    }
    private TextureBuffer texture;
    private static class TextureBuffer{
        IntBuffer nameBuffer;
        int[] indexToName;
        boolean[] mustFlip;
        private TextureBuffer(GL4 gl, int num){
            indexToName = new int[num];
            mustFlip = new boolean[num];
            nameBuffer = IntBuffer.allocate(num);
            //not glGenBuffers(be careful not to typo.) if so, glBindTexture causes glError. 
            gl.glGenTextures(num, nameBuffer);
            for (int i = 0; i < num; i++){
                indexToName[i] = nameBuffer.get(i);
            }
        }
    }

    public PMXBuffer(GL4 gl, PMX pmx){
        genVertexBuffer(gl, pmx.indices(), pmx.vertex());
        genTextureBuffer(gl, pmx.texture(), pmx.getParentDirectory());
    }

    public void genVertexBuffer(GL4 gl, int[] indices, Vertex vertex){
        this.vertex = new VertexBuffer(gl);
        {
            Buffer buffer = Buffers.newDirectIntBuffer(indices);
            int byteLength = buffer.capacity() * Buffers.SIZEOF_INT;
            gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.vertex.indices);
            gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, byteLength, buffer, GL_STATIC_DRAW);
        }
        {
            Buffer buffer = Buffers.newDirectFloatBuffer(vertex.position);
            int byteLength = buffer.capacity() * Buffers.SIZEOF_FLOAT;
            gl.glBindBuffer(GL_ARRAY_BUFFER, this.vertex.position);
            gl.glBufferData(GL_ARRAY_BUFFER, byteLength, buffer, GL_STATIC_DRAW);
        }
        {
            Buffer buffer = Buffers.newDirectFloatBuffer(vertex.normal);
            int byteLength = buffer.capacity() * Buffers.SIZEOF_FLOAT;
            gl.glBindBuffer(GL_ARRAY_BUFFER, this.vertex.normal);
            gl.glBufferData(GL_ARRAY_BUFFER, byteLength, buffer, GL_STATIC_DRAW);
        }
        {
            Buffer buffer = Buffers.newDirectFloatBuffer(vertex.uv);
            int byteLength = buffer.capacity() * Buffers.SIZEOF_FLOAT;
            gl.glBindBuffer(GL_ARRAY_BUFFER, this.vertex.uv);
            gl.glBufferData(GL_ARRAY_BUFFER, byteLength, buffer, GL_STATIC_DRAW);
        }
        {
            int[] bone = new int[vertex.deform.length * 4];
            for (int i = 0; i < vertex.deform.length; i++){
                BDEF bdef = (BDEF)vertex.deform[i];
                bone[i*4] = bdef.bone[0];
                bone[i*4+1] = bdef.bone[1];
                bone[i*4+2] = bdef.bone[2];
                bone[i*4+3] = bdef.bone[3];
            }
            Buffer buffer = Buffers.newDirectIntBuffer(bone);
            int byteLength = buffer.capacity() * Buffers.SIZEOF_INT;
            gl.glBindBuffer(GL_ARRAY_BUFFER, this.vertex.joint);
            gl.glBufferData(GL_ARRAY_BUFFER, byteLength, buffer, GL_STATIC_DRAW);
        }
        {
            float[] weight = new float[vertex.deform.length * 4];
            for (int i = 0; i < vertex.deform.length; i++){
                BDEF bdef = (BDEF)vertex.deform[i];
                weight[i*4] = bdef.weights[0];
                weight[i*4+1] = bdef.weights[1];
                weight[i*4+2] = bdef.weights[2];
                weight[i*4+3] = bdef.weights[3];
            }
            Buffer buffer = Buffers.newDirectFloatBuffer(weight);
            int byteLength = buffer.capacity() * Buffers.SIZEOF_FLOAT;
            gl.glBindBuffer(GL_ARRAY_BUFFER, this.vertex.weight);
            gl.glBufferData(GL_ARRAY_BUFFER, byteLength, buffer, GL_STATIC_DRAW);
        }
    }

    public void genTextureBuffer(GL4 gl, Texture texture, String parentDir){
        int num = texture.paths.length;
        this.texture = new TextureBuffer(gl, num);
        for (int i = 0; i < num; i++){
            File file = null;
            try {
                String path = parentDir + "\\" + texture.paths[i];
                file = new File(path);
                String name = file.getName();
                String extension = name.substring(name.lastIndexOf(".") + 1).intern();
                TextureData textureData;
                if (extension.equals("tga")){
                    textureData = TextureIO.newTextureData(GLProfile.get(GLProfile.GL4), file, false, extension);
                    this.texture.mustFlip[i] = true;
                 } else {
                    BufferedImage tBufferedImage = ImageIO.read(file);
                    //ImageUtil.flipImageVertically(tBufferedImage);
                    textureData = AWTTextureIO.newTextureData(GLProfile.get(GLProfile.GL4),
                         tBufferedImage, false);
                    this.texture.mustFlip[i] = false;
                }
 
                gl.glBindTexture(GL_TEXTURE_2D, this.texture.indexToName[i]);
                gl.glPixelStorei(GL_UNPACK_ALIGNMENT,1);
                gl.glTexImage2D(GL_TEXTURE_2D, 0, textureData.getInternalFormat(),
                textureData.getWidth(), textureData.getHeight(), 
                0, textureData.getPixelFormat(), textureData.getPixelType(), textureData.getBuffer());
                gl.glGenerateMipmap(GL_TEXTURE_2D);
                gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
                gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
                gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT); 
            } catch (Exception e){
                e.printStackTrace();
                System.out.println("Can't load a texture: "+ file.getName());
            }
        }
    }

    public void enable(GL4 gl){
        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vertex.indices);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vertex.position);   
        gl.glEnableVertexAttribArray(0);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vertex.normal);
        gl.glEnableVertexAttribArray(1);
        gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vertex.uv);
        gl.glEnableVertexAttribArray(2);
        gl.glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vertex.joint);
        gl.glEnableVertexAttribArray(3);
        gl.glVertexAttribPointer(3, 4, GL_UNSIGNED_INT, false, 0, 0);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vertex.weight);
        gl.glEnableVertexAttribArray(4);
        gl.glVertexAttribPointer(4, 4, GL_FLOAT, false, 0, 0);

    }
    public void setTexture(GL4 gl, int index){
        if (index != -1){ 
            gl.glActiveTexture(GL_TEXTURE1);
            gl.glBindTexture(GL_TEXTURE_2D, texture.indexToName[index]);
            gl.glUniform1i(6, 1); //hasTexture
            gl.glUniform1i(200, texture.mustFlip[index] ? 1 : 0); //hasTexture
        } else {
            gl.glUniform1i(6, 0);
        }
    }
        
    public void disable(GL4 gl){
        gl.glDisableVertexAttribArray(0);
        gl.glDisableVertexAttribArray(1);
        gl.glDisableVertexAttribArray(2);
        gl.glDisableVertexAttribArray(3);
        gl.glDisableVertexAttribArray(4);
        gl.glBindBuffer(GL_ARRAY_BUFFER, 0);
        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }
}