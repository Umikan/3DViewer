package viewer.model;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import viewer.model.common.*;

import java.io.File;
import java.util.HashMap;

public class TexManager{
    private HashMap<String, Texture> list = new HashMap<String, Texture>();
    public TexManager(BaseModel ...models){
        for (BaseModel model : models){
            loadTextures(model);
        }
    }
    public TexManager(String ...paths){
        for (String path : paths) loadTexture(path);
    }
    private void loadTexture(String path){
        if (path == null) return;
        try {
            File img = new File(path);
            Texture texture = TextureIO.newTexture(img, true);
            list.put(path, texture);
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Can't load a texture");
            System.exit(0);
        }
    }
    public Texture get(String path){
        return list.get(path);
    }
    public void loadTextures(BaseModel model){
        for (MtlMesh f : model.f){
            loadTexture(f.mtl.map_Kd);
        }
    }
}