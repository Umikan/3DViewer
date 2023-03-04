package viewer.model.vmd;

import java.util.ArrayList;
import java.util.HashMap;

public class MotionManager {
    public HashMap<String, MotionSequence> motions;

    public MotionManager(ArrayList<Motion> in){
        motions = new HashMap<String, MotionSequence>();
        for (Motion motion : in){
            if (motions.get(motion.boneName) == null){
                motions.put(motion.boneName, new MotionSequence());
            }
            motions.get(motion.boneName).add(motion);
        }
        for (String name : motions.keySet()){
            motions.get(name).sort();
        }
    }
    
    public MotionTransform getMotion(String name, float time){
        MotionSequence s = motions.get(name);
        return s == null ? null : s.getMotion(time);
    }

}

class MotionSequence extends Sequence<Motion>{

    MotionTransform getMotion(float time){
        int index = search(time);
        if (index == -1) return null;
        Motion m1 = get(index);
        Motion m2 = get(index+1);
        float t = t(index, time);
        return new MotionTransform(m1, m2, m1.interpolation, t);
    }
}   