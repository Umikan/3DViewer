package viewer.model.common;

import com.jogamp.opengl.GL4;

import viewer.shader.Memory;

import org.joml.Matrix4f;

public class MVP {
    private static Matrix4f matM;
    private static Matrix4f matV;
    private static Matrix4f matP; 
    private static int uMVP, uM, uV;
    static {
        matM = matV = matP = new Matrix4f();
    }
    private static Matrix4f getMVP(){
        //Incorrect: P.mul(V).mul(M) means multipling P per a call
        return new Matrix4f(matP).mul(matV).mul(matM);
    }
    public static void setUniformLocation(GL4 gl, int shaderprogram, String mvp, String m, String v){
        uMVP = gl.glGetUniformLocation(shaderprogram, mvp);
        uM = gl.glGetUniformLocation(shaderprogram, m);
        uV = gl.glGetUniformLocation(shaderprogram, v);
    }
    public static void setM(Matrix4f M){
        MVP.matM = M;
    }
    public static void setVP(Matrix4f V, Matrix4f P){
        MVP.matV = V;
        MVP.matP = P;
    }
    public static void uniformMVP(GL4 gl){
        matM.get(Memory.mat4);
        gl.glUniformMatrix4fv(uM, 1, false, Memory.mat4);
        matV.get(Memory.mat4);
        gl.glUniformMatrix4fv(uV, 1, false, Memory.mat4);
        getMVP().get(Memory.mat4);
        gl.glUniformMatrix4fv(uMVP, 1, false, Memory.mat4);
    }
    public static void printTransform(Matrix4f transform){
        System.out.printf("%f %f %f %f\n", 
            transform.m00(), transform.m10(), transform.m20(), transform.m30());
        System.out.printf("%f %f %f %f\n", 
            transform.m01(), transform.m11(), transform.m21(), transform.m31());
        System.out.printf("%f %f %f %f\n", 
            transform.m02(), transform.m12(), transform.m22(), transform.m32());
        System.out.printf("%f %f %f %f\n", 
            transform.m03(), transform.m13(), transform.m23(), transform.m33());
        System.out.println();
    }
}