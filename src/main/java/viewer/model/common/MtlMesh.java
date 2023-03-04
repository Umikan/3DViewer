package viewer.model.common;

import java.util.LinkedList;
import java.util.Iterator;
import java.util.function.Consumer;

public class MtlMesh implements Iterable<Face>{
    private LinkedList<Face> list = new LinkedList<Face>();
    public Material mtl;
    public MtlMesh(){
        mtl = new Material();
    }
    public MtlMesh(Material mtl){
        this.mtl = mtl;
    }
    public void add(Face face){
        list.add(face);
    }
    public Iterator<Face> iterator(){
        return list.iterator();
    }
    public void forEach(Consumer<? super Face> action){
        for (Face f : list){
            action.accept(f);
        }
    }
}