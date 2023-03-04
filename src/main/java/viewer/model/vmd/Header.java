package viewer.model.vmd;

import viewer.io.BinaryReader;

public class Header {
    public String modelName;
    public Header(BinaryReader br) throws VMDImportException{
        String vmdHeader = new String(br.getBytes(30));
        String correctHeader = "Vocaloid Motion Data 0002\0\0\0\0\0";
        if (!vmdHeader.equals(correctHeader)) {
            throw new VMDImportException("This is not a vmd file.");
        }
        modelName = VMD.readString(br, 20);
    }
}