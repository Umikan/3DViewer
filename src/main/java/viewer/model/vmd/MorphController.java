package viewer.model.vmd;

import java.util.ArrayList;
import java.util.HashMap;

public class MorphController {
    public HashMap<String, MorphSequence> morphs;

    public MorphController(ArrayList<Morph> in){
        morphs = new HashMap<String, MorphSequence>();
        for (Morph morph : in){
            if (morphs.get(morph.morphName) == null){
                morphs.put(morph.morphName, new MorphSequence());
            }
            morphs.get(morph.morphName).add(morph);
        }
        for (String name : morphs.keySet()){
            morphs.get(name).sort();
        }
    }
    
    public Float getWeight(String name, float time){
        MorphSequence s = morphs.get(name);
        return s == null ? null : s.getWeight(time);
    }

}

class MorphSequence extends Sequence<Morph>{

    Float getWeight(float time){
        int index = search(time);
        if (index == -1) return null;
        Morph m1 = get(index);
        Morph m2 = get(index+1);
        float t = t(index, time);
        return Float.valueOf(m1.weight * (1.0f - t) + m2.weight * t);
    }
}   