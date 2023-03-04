package viewer.model.vmd;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class MotionTransform{
    public Vector3f translation = new Vector3f();
    public Quaternionf rotation;

    public MotionTransform(){
        rotation = new Quaternionf();
    }
    public MotionTransform(Motion motion1, Motion motion2, Bezier[] bezier, float x){
        //Linear
        //translation = new Vector3f(motion1.translation).lerp(motion2.translation, x);
        //rotation = new Quaternionf(motion1.rotation).nlerp(motion2.rotation, x);
        //Bezier
        translation.x = lerp(motion1.translation.x, motion2.translation.x, bezier[0].y(x));
        translation.y = lerp(motion1.translation.y, motion2.translation.y, bezier[1].y(x));
        translation.z = lerp(motion1.translation.z, motion2.translation.z, bezier[2].y(x));
        rotation = new Quaternionf(motion1.rotation).slerp(motion2.rotation, bezier[3].y(x));
    }

    private float lerp(float v1, float v2, float t){
        return v1 * (1.0f - t) + v2 * t;
    }
}