package viewer.model.pmx;

import java.lang.reflect.Field;

import viewer.io.BinaryReader;

public class Material{
    public String name;
    public String name_en;

    public float[] diffuse = new float[4];
    public float[] specular = new float[3];
    public float specular_k;
    public float[] ambient = new float[3];
    public byte drawFlag;
    public float[] edgeColor = new float[4];
    public float edgeSize;

    public int texture;
    public int sphereTexture;
    public byte sphereMode;
    public boolean commonToonFlag;
    public int toonTexture;
    public byte commonToonTexture;

    public String memo;
    public int indicesCount;

    public Material(BinaryReader br, Header header){
        name = TextBuf.readString(br, header.getEncode());
        name_en = TextBuf.readString(br, header.getEncode());

        for (int i = 0; i < 4; i++){
            diffuse[i] = br.getFloat();
        }
        for (int i = 0; i < 3; i++){
            specular[i] = br.getFloat();
        }
        specular_k = br.getFloat();
        for (int i = 0; i < 3; i++){
            ambient[i] = br.getFloat();
        }

        drawFlag = br.getByte();
        for (int i = 0; i < 4; i++){
            edgeColor[i] = br.getFloat();
        }
        edgeSize = br.getFloat();

        texture = br.getIntFromByteBuffer(header.textureIndexSize);
        sphereTexture = br.getIntFromByteBuffer(header.textureIndexSize);
        sphereMode = br.getByte();
        byte commonToonFlagNum = br.getByte();
        commonToonFlag = commonToonFlagNum != 0;
        if (!commonToonFlag){
            toonTexture = br.getIntFromByteBuffer(header.textureIndexSize);
        } else {
            commonToonTexture = br.getByte();
        }

        memo = TextBuf.readString(br, header.getEncode());
        indicesCount = br.getInt();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Class: " + this.getClass().getCanonicalName() + "\n");
        sb.append("Settings:\n");
        for (Field field : this.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                sb.append(field.getName() + " = " + field.get(this) + "\n");
            } catch (IllegalAccessException e) {
                sb.append(field.getName() + " = " + "access denied\n");
            }
        }
        return sb.toString();
    }
}