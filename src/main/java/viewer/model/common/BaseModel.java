package viewer.model.common;

import java.util.ArrayList;
import java.util.LinkedList;

public class BaseModel{
    public ArrayList<Vector3> v = new ArrayList<Vector3>();
    public ArrayList<Vector3> vn = new ArrayList<Vector3>();
    public ArrayList<TexCoord> vt = new ArrayList<TexCoord>();
    public LinkedList<MtlMesh> f = new LinkedList<MtlMesh>();
}