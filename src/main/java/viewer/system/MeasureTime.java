package viewer.system;

public class MeasureTime{
    private long startTime, startTimeNano;
    private long deltaTime, deltaTimeNano;
    private String target;
    private boolean printFlag = true;
    public MeasureTime(String target){
        this.target = target;
        if (printFlag) System.out.println("created measuring time object");
    }
    public void start(){
        if (printFlag) System.out.printf("starting time measuring of %s...\n", target);
        startTime = System.currentTimeMillis();
        startTimeNano = System.nanoTime();
    }
    public void end(){
        deltaTimeNano = System.nanoTime() - startTimeNano;
        deltaTime = System.currentTimeMillis() - startTime;
        if (!printFlag) return;
        System.out.println("done.");
        System.out.printf("target of measurement: %s\n", target);
        System.out.printf("processing time: %d ms\n", deltaTime);
        System.out.printf("processing time: %d ns\n", deltaTimeNano);
    }
    public boolean threshold(long time){
        return deltaTime > time;
    }
    public long get(){
        return deltaTime;
    }
}