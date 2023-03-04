package viewer.model.obj;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.File;
import java.util.HashMap;

import viewer.model.common.*;

public class ObjParser extends BaseModel{
    private String parentDir;
    private HashMap<String, Material> mtls;
    public ObjParser(String path){
        this.parentDir = new File(path).getAbsoluteFile().getParent();
        f.add(new MtlMesh()); // default
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path)), "SHIFT-JIS"))){
            ArrayList<String> list = new ArrayList<String>();
            String line;
            while ((line = br.readLine()) != null){
                list.add(line);
            }
            load(list.toArray(new String[list.size()]));
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Can't load an obj file.");
            System.exit(0);
        }
    }
    private void load(String[] data){
        for (int i = 0; i < data.length; i++){
            String[] token = data[i].split(" ", 0);
            if (token[0].isEmpty() || token[0].charAt(0) == '#') continue;
            else parse(token);
        }
    }
    private void parse(String[] token){
        switch(token[0].intern()){
            case "mtllib":
                mtls = new MtlParser(parentDir + "/" + token[1]).get();
                break;
            case "g":
                break;
            case "usemtl":
                Material mtl = mtls.get(token[1]);
                if (mtl.map_Kd != null) mtl.map_Kd = parentDir + "/" + mtl.map_Kd;
                f.add(new MtlMesh(mtl));
                break;
            case "v":
                float v1 = Float.parseFloat(token[1]);
                float v2 = Float.parseFloat(token[2]);
                float v3 = Float.parseFloat(token[3]);
                v.add(new Vector3(v1, v2, v3));
                break;
            case "vt":
                float vt1 = Float.parseFloat(token[1]);
                float vt2 = Float.parseFloat(token[2]);
                vt.add(new TexCoord(vt1, vt2));
                break;
            case "vn":
                float vn1 = Float.parseFloat(token[1]);
                float vn2 = Float.parseFloat(token[2]);
                float vn3 = Float.parseFloat(token[3]);
                vn.add(new Vector3(vn1, vn2, vn3));
                break;
            case "f":
                Face face = new Face();
                for (int i = 1; i < token.length; i++){
                    String[] indexToken = token[i].split("/", 0);
                    Face.Index index = face.new Index();
                    index.v = Integer.parseInt(indexToken[0]) - 1;
                    if (indexToken.length != 1){
                        index.vt = Integer.parseInt(indexToken[1]) - 1;
                        index.vn = Integer.parseInt(indexToken[2]) - 1;
                    }
                    face.add(index);
                }
                f.getLast().add(face);
                break;
        }
    }
}
