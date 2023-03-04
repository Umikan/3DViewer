package viewer.model.pmx.morph;

import java.util.ArrayList;
import java.util.HashMap;

import viewer.model.pmx.Morph;

public class MorphContainer{
    private HashMap<Morph, ArrayList<Object>> morphs = new HashMap<Morph, ArrayList<Object>>();
    public void put(Morph morph, ArrayList<Object> data){
        morphs.put(morph, data);
    }
    public ArrayList<Object> get(Morph morph){
        return morphs.get(morph);
    }
}