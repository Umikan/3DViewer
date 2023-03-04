package viewer.model.pmx;

import java.util.Arrays;

import viewer.io.BinaryReader;

public class Texture{
    public String[] paths;

    public Texture(BinaryReader br, String encode){
        int textureNum = br.getByteBuffer(4).getInt();
        paths = new String[textureNum];
        for (int i = 0; i < textureNum; i++){
            paths[i] = TextBuf.readString(br, encode);
        }
    }

    @Override
    public String toString(){
        return Arrays.toString(paths);
    }
}