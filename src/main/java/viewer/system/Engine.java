package viewer.system;

public class Engine {
    public static class Time{
        public static double deltaTime;
        private static long currentTime;
        static {
            currentTime = System.currentTimeMillis();
        }
        public static void update(){
            long prevTime = currentTime;
            currentTime = System.currentTimeMillis();
            deltaTime = (currentTime - prevTime) / 1000.0d;
        }
    }
}