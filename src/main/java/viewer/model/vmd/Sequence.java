package viewer.model.vmd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Sequence<T extends KeyFrame>{
    private ArrayList<T> sequence;

    protected Sequence(){
        sequence = new ArrayList<T>();
    }

    protected void add(T obj){
        sequence.add(obj);
    }

    protected T get(int index){ return sequence.get(index); }

    protected void sort(){
        Collections.sort(sequence, new Comparator<KeyFrame>() {
            @Override
            public int compare(KeyFrame obj1, KeyFrame obj2) {
                return obj1.frameNum() - obj2.frameNum();
            }
        });
    }
    protected int search(float time){
        final float frame = time * 30.0f;
        int index = -1;
        for (int i = 1; i < sequence.size(); i++){
            if (inInterval(frame, i-1, i)){
                index = i-1;
                break;
            }
        }
        return index;
    }
    protected float t(int index, float time){
        T m1 = get(index);
        T m2 = get(index+1);
        return (float)(time * 30.0f - m1.frameNum()) / (float)(m2.frameNum() - m1.frameNum());
    }

    private boolean inInterval(float frame, int idx1, int idx2){
        return sequence.get(idx1).frameNum() <= frame && frame <= sequence.get(idx2).frameNum();
    }
}   