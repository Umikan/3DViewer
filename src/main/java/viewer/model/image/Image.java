package viewer.model.image;

import java.io.File;
import java.nio.Buffer;
import java.nio.IntBuffer;

import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;
import static com.jogamp.opengl.GL4.*;

public class Image {
    private String path;
    private IntBuffer buffer;
    private IntBuffer texBuffer;
    private int textureID;
    private int vertexID;

    public Image(String path) {
        this.path = path;
    }

    public void init(GL4 gl) {
        buffer = IntBuffer.allocate(1);
        gl.glGenBuffers(1, buffer);
        vertexID = buffer.get(0);

        texBuffer = IntBuffer.allocate(1);
        gl.glGenTextures(1, buffer);
        textureID = texBuffer.get(0);

        vertex(gl);
        loadTexture(gl);
    }

    private void vertex(GL4 gl) {
        float[] quad = {
                0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                1.0f, 1.0f, 0.0f,
                1.0f, 0.0f, 0.0f };
        Buffer buffer = Buffers.newDirectFloatBuffer(quad);
        int byteLength = buffer.capacity() * Buffers.SIZEOF_FLOAT;
        gl.glBindBuffer(GL_ARRAY_BUFFER, vertexID);
        gl.glBufferData(GL_ARRAY_BUFFER, byteLength, buffer, GL_STATIC_DRAW);
    }

    private void loadTexture(GL4 gl) {
        try {
            File file = new File(path);
            String name = file.getName();
            String extension = name.substring(name.lastIndexOf(".") + 1);
            TextureData textureData = TextureIO.newTextureData(GLProfile.get(GLProfile.GL4),
                    file, false, extension);
            gl.glBindTexture(GL_TEXTURE_2D, textureID);
            gl.glTexImage2D(GL_TEXTURE_2D, 0, textureData.getInternalFormat(),
                    textureData.getWidth(), textureData.getHeight(),
                    0, textureData.getPixelFormat(), textureData.getPixelType(), textureData.getBuffer());
            gl.glGenerateMipmap(GL_TEXTURE_2D);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Can't load textures");
            System.exit(0);
        }
    }

    public void render(GL4 gl) {
        gl.glBindBuffer(GL_ARRAY_BUFFER, vertexID);
        gl.glEnableVertexAttribArray(0);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vertexID);
        gl.glEnableVertexAttribArray(1);
        gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);

        gl.glActiveTexture(GL_TEXTURE0);
        gl.glBindTexture(GL_TEXTURE_2D, textureID);

        gl.glDrawArrays(GL_TRIANGLES, 0, 3);
        gl.glDisableVertexAttribArray(0);
        gl.glDisableVertexAttribArray(1);
    }
}