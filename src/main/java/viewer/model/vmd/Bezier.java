package viewer.model.vmd;

public class Bezier{
    public float x1, y1, x2, y2;

    public Bezier(byte x1, byte y1, byte x2, byte y2){
        this.x1 = x1 / 127.0f;
        this.y1 = y1 / 127.0f;
        this.x2 = x2 / 127.0f;
        this.y2 = y2 / 127.0f;
    }

    public float y(float x){
        float s = 0.5f;
        float t = 0.5f;   
        for (int i = 0; i < 15; i++){
            float f = (3 * s * s * t * x1) + (3 * s * t * t * x2) + (t * t * t) - x;
            if (Math.abs(f) < 0.00001f) break;
            else if (f > 0.0f) t -= 1.0f / (4 << i);
            else t += 1.0f / (4 << i);
            s = 1.0f - t;
        }
        return (3 * s * s * t * y1) + (3 * s * t * t * y2) + (t * t * t);
    }
}