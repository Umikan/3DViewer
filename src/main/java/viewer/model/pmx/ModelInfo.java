package viewer.model.pmx;

import viewer.io.BinaryReader;

public class ModelInfo {
    public String name;
    public String name_en;
    public String comment;
    public String comment_en;

    public ModelInfo(BinaryReader br, String encode) {
        name = TextBuf.readString(br, encode);
        name_en = TextBuf.readString(br, encode);
        comment = TextBuf.readString(br, encode);
        comment_en = TextBuf.readString(br, encode);
    }

    public String toString(){
        return name + "(" + name_en + ")\n" + comment + "\n" + comment_en;
    }
}