package viewer.model.obj;

import java.util.HashMap;

import viewer.model.common.*;

import java.util.ArrayList;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileReader;

public class MtlParser{
    private HashMap<String, Material> mtl = new HashMap<String, Material>();
    private String currentMtl;
    public MtlParser(String path){
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            ArrayList<String> list = new ArrayList<String>();
            String line;
            while ((line = br.readLine()) != null){
                list.add(line);
            }
            load(list.toArray(new String[list.size()]));
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Can't load a mtl file.");
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
        float r, g, b;
        Material m;
        switch(token[0].intern()){
            case "newmtl":
                mtl.put(token[1], new Material());
                currentMtl = token[1];
                break;
            case "Ka":
                m = mtl.get(currentMtl);
                r = Float.parseFloat(token[1]);
                g = Float.parseFloat(token[2]);
                b = Float.parseFloat(token[3]);
                m.Ka = m.new Color(r, g, b);
                break;
            case "Kd":
                m = mtl.get(currentMtl);
                r = Float.parseFloat(token[1]);
                g = Float.parseFloat(token[2]);
                b = Float.parseFloat(token[3]);
                m.Kd = m.new Color(r, g, b);
                break;
            case "Ks":
                m = mtl.get(currentMtl);
                r = Float.parseFloat(token[1]);
                g = Float.parseFloat(token[2]);
                b = Float.parseFloat(token[3]);
                m.Ks = m.new Color(r, g, b);
                break;
            case "Ns":
                m = mtl.get(currentMtl);
                m.Ns = Float.parseFloat(token[1]);
                break;
            case "d":
                m = mtl.get(currentMtl);
                m.d = Float.parseFloat(token[1]);
                break;
            case "map_Kd":
                m = mtl.get(currentMtl);
                m.map_Kd = token[1];
                break;
        }
    }
    public HashMap<String, Material> get(){
        return mtl;
    }
    public Material get(String name){
        return mtl.get(name);
    }
}
