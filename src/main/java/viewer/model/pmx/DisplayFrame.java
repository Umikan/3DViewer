package viewer.model.pmx;

import java.util.ArrayList;

import viewer.io.BinaryReader;

public class DisplayFrame {
    public String name;
    public String name_en;
    public boolean isSpecial;
    public ArrayList<Element> elements = new ArrayList<Element>();
    
    public DisplayFrame(BinaryReader br, Header header) throws PMXImportException{
        name = TextBuf.readString(br, header.getEncode());
        name_en = TextBuf.readString(br, header.getEncode());
        isSpecial = br.getByte() == 1;
        int count = br.getInt();
        for (int i = 0; i < count; i++){
            elements.add(new Element(br, header));
        }
    }
    public static class Element{
        public boolean type;
        public int index;
        public static final boolean BONE = false;
        public static final boolean MORPH = true;
        public Element(BinaryReader br, Header header) throws PMXImportException{
            type = br.getByte() == 1;
            if (type == BONE) index = br.getIntFromByteBuffer(header.boneIndexSize);
            else if (type == MORPH) index = br.getIntFromByteBuffer(header.morphIndexSize);
            else new PMXImportException("Invalid Display Frame Data.");
        }
    }
}