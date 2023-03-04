package viewer.model.pmx;

import java.io.UnsupportedEncodingException;

import viewer.io.BinaryReader;

public class TextBuf {
    public static String readString(BinaryReader br, String encode){
        String str = null;
        int size = br.getByteBuffer(4).getInt();
        try {
            str = new String(br.getBytes(size), encode);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }
}