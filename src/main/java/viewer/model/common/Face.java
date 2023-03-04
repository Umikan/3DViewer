package viewer.model.common;

import java.util.LinkedList;
import java.util.Iterator;
import java.util.function.Consumer;

public class Face implements Iterable<Face.Index>{
    public LinkedList<Index> list = new LinkedList<Index>();
    public class Index{
        public int v, vn, vt;
    }
    public void add(Index index){
        list.add(index);
    }
    public Iterator<Index> iterator(){
        return list.iterator();
    }
    public void forEach(Consumer<? super Index> action){
        for (Index index : list){
            action.accept(index);
        }
    }
}