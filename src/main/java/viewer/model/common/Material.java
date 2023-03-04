package viewer.model.common;

public class Material{
    public Color Ka, Kd, Ks;
    public float Ns, d;
    public String map_Kd = null;
    public class Color{
        public float r, g, b;
        public Color(float r, float g, float b){
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }
    public float[] toArray(Color c){
        float[] array = {c.r, c.g, c.b, 1.0f};
        return array;
    }
}
