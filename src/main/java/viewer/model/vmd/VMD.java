package viewer.model.vmd;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JOptionPane;


import viewer.io.BinaryReader;

public class VMD {
    private Header header;
    private MotionManager motionManager;
    public MotionManager getMotion(){
        return motionManager;
    }
    private MorphController morphController;
    public MorphController getMorph(){
        return morphController;
    }
    private ArrayList<Camera> cameras = new ArrayList<Camera>();
    private ArrayList<Light> lights = new ArrayList<Light>();
    private ArrayList<SelfShadow> selfShadows = new ArrayList<SelfShadow>();
    public VMD(String path){
        BinaryReader br = new BinaryReader(path);
        try {
            //Header
            header = new Header(br);
            //Motion
            int motionCount = br.getInt();
            ArrayList<Motion> motions = new ArrayList<Motion>();
            for (int i = 0; i < motionCount; i++){
                motions.add(new Motion(br));
            }
            motionManager = new MotionManager(motions);
            //Skin
            int skinCount = br.getInt();
            ArrayList<Morph> morphs = new ArrayList<Morph>();
            for (int i = 0; i < skinCount; i++){
                morphs.add(new Morph(br));
            }
            morphController = new MorphController(morphs);
            //Camera
            int cameraCount = br.getInt();
            for (int i = 0; i < cameraCount; i++){
                cameras.add(new Camera(br));
            }
            //Light
            int lightCount = br.getInt();
            for (int i = 0; i < lightCount; i++){
                System.out.println("called");
                lights.add(new Light(br));
            }
            //Self Shadow
            int ssCount = br.getInt();
            for (int i = 0; i < ssCount; i++){
                selfShadows.add(new SelfShadow(br));
            }
        } catch (VMDImportException e){
            System.out.println(e.getMessage());
            System.exit(0);
        } catch (ArrayIndexOutOfBoundsException e){
            JOptionPane.showMessageDialog(new JFrame(), "This VMD file may be corrupt.", "Warning",
            JOptionPane.ERROR_MESSAGE);
        }
    }

    static String readString(BinaryReader br, int length){
        try {
            String str = new String(br.getBytes(length), "SHIFT-JIS");
            return str.split("\0")[0];
        } catch (UnsupportedEncodingException e){
            e.printStackTrace();
            return null;
        }
    }
}